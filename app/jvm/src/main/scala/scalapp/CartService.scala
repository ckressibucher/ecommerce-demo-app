package scalapp

import java.math.{BigDecimal, MathContext, RoundingMode}

import plus.coding.ckrecom.TaxSystem.DefaultTaxClass
import plus.coding.ckrecom.impl.FixedDiscountCalc
import plus.coding.ckrecom.impl.Priceable.{FixedDiscount, Line => PriceLine}
import plus.coding.ckrecom.{Product => EcmProduct, _}

import scala.collection.immutable
import scala.collection.immutable.Seq
import scala.util.Try
import scalapp.jvm.Data
import scalapp.model._

/** Used to calculate a given cart ([[CartData]]) and return the result
  * as [[CartView]]
  */
object CartService {

  // shadow scala.Product
  import scalapp.model.Product

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
      case Right(successCart) =>
        mapCartResultToCartView(successCart)
      case Left(failedCart) =>
        val msg = "The cart could not be calculated successfully. Errors: " +
          failedCart.failedItems.map(_.error).mkString("")
        Left(msg)
    }
  }

  /**
    * @param result A validated cart (as returned by `Cart.fromItems`)
    * @return
    */
  def collectMainLines(result: SuccessCart[TaxCls])(implicit ts: TaxSystem[TaxCls]): Seq[CartView.Line] = {
    result.contents.collect {
      // main lines always have exactly one tax class
      case SuccessItem(PriceLine(article: Product, qty), prices, true) if prices.size == 1 =>
        CartView.Line(article, qty.intValue(), Price(prices.head._2), Data.taxClassString(prices.head._1))
    }
  }

  def collectDiscountLines(result: SuccessCart[TaxCls])(implicit ts: TaxSystem[TaxCls]): Seq[CartView.Discount] = {
    result.contents.collect {
      case SuccessItem(FixedDiscount(code, amount), prices, _) =>
        CartView.Discount(code, Price(amount), prices.keys.map(Data.taxClassString).toList)
    }
  }

  def mapTaxLines(result: SuccessCart[TaxCls])(implicit ts: TaxSystem[TaxCls]): Seq[CartView.TaxLine] = {
    (result.taxes(RoundingMode.HALF_UP) map {
       case (taxClass: TaxCls, Cart.TaxClassSumAndTaxAmount(sum, taxAmnt)) =>
         val taxClsLabel = Data.taxClassString(taxClass)
         val taxRate = ts.rate(taxClass)
         val taxRateAsDouble = taxRate.num.doubleValue() / taxRate.denom.doubleValue()
         CartView.TaxLine(taxClsLabel, taxRateAsDouble, Price(sum), Price(taxAmnt))
     }).toList
  }

  def mapCartResultToCartView(result: SuccessCart[TaxCls]): Either[String, CartView] = {
    implicit val productImpl = theProductImpl
    implicit val ts = theTaxSystem // used to convert tax class to a string (via `TaxRate`)
    val mainLines = collectMainLines(result)
    val discountLines = collectDiscountLines(result)
    val taxLines = mapTaxLines(result)

    val taxTotal = result.taxSum()
    val grandTotal = result.grandTotal()
    Right(
      CartView(mainLines, discountLines,
      CartView.TaxResult(taxLines, Price(taxTotal)),
      Price(grandTotal)))
  }


  case class Article(product: Product, price: Cents)(implicit taxSystem: TaxSystem[TaxCls])

  /** By implementing a `CartSystem`, we define all properties needed by
    * the library to do its calculations.
    *
    * See the `CartSystem` type to see what abstract members it defines.
    */
  class CartCalculation(cartData: CartData) extends CartSystem[TaxCls, Product] {

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
      discs.map { priceable => new FixedDiscountCalc[DefaultTaxClass](priceable) }
    }
  }

}
