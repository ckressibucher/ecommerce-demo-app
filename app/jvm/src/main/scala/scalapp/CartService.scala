package scalapp

import scalapp.model.{CartData, Price, Product, TaxReduced, TaxRegular}
import scalapp.jvm.Data
import plus.coding.ckrecom.{CartContentItem, CartItemCalculator, TaxSystem}
import plus.coding.ckrecom.impl.Priceable.{FixedDiscount, Line => PriceLine}
import plus.coding.ckrecom.impl.FixedDiscountCalc
import plus.coding.ckrecom.{Product => EcmProduct}
import java.math.{BigDecimal, MathContext}

import plus.coding.ckrecom.Product.ProductOps
import plus.coding.ckrecom.TaxSystem.DefaultTaxClass
import plus.coding.ckrecom.{CartBase, CartSystem, PriceMode}

import scala.collection.immutable
import scala.util.Try
import scalapp.model.CartView

/** Used to calculate a given cart ([[CartData]]) and return the result
  * as [[CartView]]
  */
object CartService {

  // We use the `DefaultTaxClass` and `DefaultTaxSystem` here.
  type TaxCls = TaxSystem.DefaultTaxClass

  // A type alias to simplify some type annotations.
  // A `CartItemPre` holds some cart content (e.g. a product) and
  // an algorithm to calculate prices. It depends on a tax system.
  //
  // Here, we define our specialized type for our tax class `TaxCls`
  // and generalize over the cart content (using `_`).
  type CalcItem = CartItemCalculator[_, TaxCls]

  // we use `Long`s in our `Article`s to define prices
  type Cents = Long

  val theTaxSystem = TaxSystem.DefaultTaxSystem

  val theProductImpl = new EcmProduct[TaxCls, Product] {
      def netPrice(product: Product, qty: BigDecimal): Option[BigDecimal] = {
        implicit val mc = MathContext.DECIMAL128
        val taxRate = theTaxSystem.rate(taxClass(product))
        val res = taxRate.netAmount(new BigDecimal(product.price.cents))
        Some(res)
      }

      // map the "scalapp" tax class enumerable to the `TaxCls` implementation
      def taxClass(product: Product) = product.taxClassId match {
        case `TaxRegular` => Data.taxRegular
        case `TaxReduced` => Data.taxReduced
      }
    }

  def caluclateCart(cartData: CartData): Either[String, CartView] = {
    val system = new CartCalculation(cartData)
    system.run match {
      case Right(c) => mapCartResultToCartView(c)
      case Left(errs) => {
        val msg = "The cart could not be calculated successfully. Errors: " + errs.mkString("; ")
        Left(msg)
      }
    }
  }

  // TODO refactor, too long...
  def mapCartResultToCartView(result: CartBase[TaxCls]): Either[String, CartView] = {
    implicit val productImpl = theProductImpl
    val lines = result.contents.collect {
      case CartContentItem(PriceLine(article, qty), prices) =>
        prices match {
          case e@Left(err) => e
          case Right(priceMap) if priceMap.size > 1 => Left("Unexpected result: more than one tax class")
          case Right(priceMap) if priceMap.size < 1 => Left("Unexpected result: no prices")
          case Right(priceMap) => {
            implicit val ts = theTaxSystem // used to convert tax class to a string (via `TaxRate`)
            val priceSum = priceMap.values.sum
            val taxClass = priceMap.keys.head
            val articleP = article.asInstanceOf[Product]
            val tc = articleP.taxClass
            val taxClsStr = Data.taxClassString(tc)
            Right(CartView.Line(articleP, qty.intValue(), Price(priceSum), taxClsStr))
          }
        }
    }
    val discounts = result.contents.collect {
      case CartContentItem(FixedDiscount(code, amount), prices) => prices match {
        case e@Left(_) => e
        case Right(priceMap) if priceMap.size < 1 => Left("err")
        case Right(priceMap) if priceMap.size > 1 => Left("err")
        case Right(priceMap) => {
          implicit val ts = theTaxSystem // used to convert tax class to a string (via `TaxRate`)
          val (taxCls, p) = (priceMap.keys.head, priceMap.values.head)
          val taxClsStr = Data.taxClassString(taxCls)
          Right(CartView.Discount(code, Price(p), taxClsStr))
        }
      }
    }
    if (lines.count(_.isLeft) > 0 || discounts.count(_.isLeft) > 0) {
      val allErrors = (lines collect {
        case Left(e) => e
      }) ++ (discounts collect {
        case Left(e) => e
      })
      Left(allErrors.mkString("; "))
    } else {
      val okLines = lines.collect {
        case Right(ln: CartView.Line) => ln
      }
      val okDiscounts = discounts collect {
        case Right(d: CartView.Discount) => d
      }
      val taxLines = result.taxes(java.math.RoundingMode.HALF_UP) map {
        case (taxClass: TaxCls, value) => {
          val taxRate = theTaxSystem.rate(taxClass)
          val taxRateAsDouble = taxRate.num.doubleValue() / taxRate.denom.doubleValue()
          CartView.TaxLine(taxClass.toString, taxRateAsDouble, Price(value))
        }
      }
      val taxTotal = taxLines.foldLeft(0L) {
        case (total, item) => total + item.sum.cents
      }
      val sumLines = okLines.foldLeft(0L) {
        case (lineTotal, ln) => lineTotal + ln.price.cents
      }
      // TODO use CartBase result instead??
      val grandTotal = if (result.mode == PriceMode.PRICE_GROSS)
        sumLines
      else
        taxTotal + sumLines
      Right(CartView(okLines, okDiscounts, CartView.TaxResult(taxLines.toList, Price(taxTotal)), Price(grandTotal)))
    }
  }


  case class Article(product: Product, price: Cents)(implicit taxSystem: TaxSystem[TaxCls])

  /** By implementing a `CartSystem`, we define all properties needed by
    * the library to do its calculations.
    *
    * See the `CartSystem` type to see what abstract members it defines.
    */
  class CartCalculation(cartData: CartData) extends CartSystem[TaxCls, Product] { cartCalc =>

    // for some (java) BigDecimal calculations, we need a `MathContext` available
    implicit val mc: MathContext = MathContext.DECIMAL128

    implicit val taxSystem = theTaxSystem

    val priceMode: PriceMode.Value = PriceMode.PRICE_GROSS

    /** A simple implementation of the [[plus.coding.ckrecom.Product]] typeclass
      */
    implicit val productImpl = theProductImpl

    // here we define the cart lines for our articles.
    // we only need to define the lines containing product and quantity,
    // the `CartSystem` uses the default calculation logic (i.e. builds a `LineCalc` item
    // from the `Line`s).
    override def buildCartLines = {
      cartData.productItems map { item =>
        PriceLine[TaxCls, Product](item.product, item.qty)
      }
    }

    // This method is used to add any adjustment items, such as discounts or fees.
    // They are appended to the items generated from the result of `buildCartLines`.
    override def buildAdjustmentItems: immutable.Seq[CalcItem] = {
      val discs = cartData.discounts flatMap { discCode =>
        if (discCode.startsWith("demo-")) {
          val amount = Try {
            List(FixedDiscount(discCode, discCode.substring("demo-".length).toLong))
          }
          amount.getOrElse(List())
        } else {
          List()
        }
      }
      discs.map { priceable => new FixedDiscountCalc[DefaultTaxClass](priceable, Data.taxRegular) }
    }
  }

}
