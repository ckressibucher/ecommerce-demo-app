package scalapp

import scalapp.model.{CartData, Price, Product, TaxRegular, TaxReduced}
import scalapp.jvm.Data
import plus.coding.ckrecom.{CartContentItem, CartItemCalculator, TaxSystem}
import plus.coding.ckrecom.impl.Priceable.{Line => PriceLine}
import plus.coding.ckrecom.{Product => EcmProduct}
import java.math.{BigDecimal, MathContext}

import plus.coding.ckrecom.CartSystem
import plus.coding.ckrecom.PriceMode
import plus.coding.ckrecom.CartBase

import scala.collection.immutable
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

  def mapCartResultToCartView(result: CartBase[TaxCls]): Either[String, CartView] = {
    val lines = result.contents.collect {
      case CartContentItem(PriceLine(article, qty), prices) =>
        prices match {
          case e @ Left(err)                        => e
          case Right(priceMap) if priceMap.size > 1 => Left("Unexpected result: more than one tax class")
          case Right(priceMap) if priceMap.size < 1 => Left("Unexpected result: no prices")
          case Right(priceMap) => {
            implicit val ts = theTaxSystem // used to convert tax class to a string (via `TaxRate`)
            val priceSum = priceMap.values.sum
            val taxClass = priceMap.keys.head
            val articleObj = article.asInstanceOf[Article]
            val taxClsStr = Data.taxClassString(articleObj.taxClass)
            Right(CartView.Line(articleObj.product, qty.intValue(), Price(priceSum), taxClsStr))
          }
        }
    }
    if (lines.count(_.isLeft) > 0) {
      val allErrors = lines collect {
        case Left(e) => e
      }
      Left(allErrors.mkString("; "))
    } else {
      val okLines = lines.collect {
        case Right(ln: CartView.Line) => ln
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
      val grandTotal = if (result.mode == PriceMode.PRICE_GROSS)
        sumLines
      else
        taxTotal + sumLines
      Right(CartView(okLines, CartView.TaxResult(taxLines.toList, Price(taxTotal)), Price(grandTotal)))
    }
  }

  /** A simple implementation of a `Product`.
    *
    * TODO maybe implement this as typeclass in the ecom library, so we
    * can "add implementations"
    * of unrelated types more easily?
    */
  case class Article(product: Product, price: Cents)(implicit taxSystem: TaxSystem[TaxCls]) extends EcmProduct[TaxCls] {
    def netPrice: Option[java.math.BigDecimal] = {
      implicit val mc = MathContext.DECIMAL128

      val taxRate = taxSystem.rate(taxClass)
      val res = taxRate.netAmount(new BigDecimal(price))
      Some(res)
    }

    // map the "scalapp" tax class enumerable to the `TaxCls` implementation
    def taxClass = product.taxClass match {
      case `TaxRegular` => Data.taxRegular
      case `TaxReduced` => Data.taxReduced
    }
  }

  /** By implementing a `CartSystem`, we define all properties needed by
    * the library to do its calculations.
    *
    * See the `CartSystem` type to see what abstract members it defines.
    */
  class CartCalculation(cartData: CartData) extends CartSystem[TaxCls] {

    // for some (java) BigDecimal calculations, we need a `MathContext` available
    implicit val mc: MathContext = MathContext.DECIMAL128

    implicit val taxSystem = theTaxSystem

    val priceMode: PriceMode.Value = PriceMode.PRICE_GROSS

    // here we define the cart lines for our articles.
    // we only need to define the lines containing product and quantity,
    // the `CartSystem` uses the default calculation logic (i.e. builds a `LineCalc` item
    // from the `Line`s).
    override def buildCartLines: immutable.Seq[PriceLine[TaxCls]] = {
      cartData.productItems map { item =>
        val article = Article(item.product, item.product.price.cents)
        PriceLine(article, item.qty)
      }
    }

    // This method is used to add any adjustment items, such as discounts or fees.
    // They are appended to the items generated from the result of `buildCartLines`.
    override def buildAdjustmentItems: immutable.Seq[CalcItem] = {
      // not yet implemented...
      immutable.Seq.empty
    }
  }
}
