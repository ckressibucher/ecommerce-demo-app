package scalapp

package object model {

  case class Category(name: String) extends AnyVal

  // it seems that autowire's macros cannot work with enums,
  // so we model this with a case class and two
  // concrete, globally available instances.
  case class TaxClassEnum(tc: String) extends AnyVal
  val TaxRegular = TaxClassEnum("B")
  val TaxReduced = TaxClassEnum("A")

  /** A product, and its quantity in the cart.
    */
  case class CartItem(product: Product, qty: Int)

  case class ProductName(name: String) extends AnyVal

  object PriceSignum extends Enumeration {
    val Plus, Minus = Value
  }

  /**
    * A price is an integer value of cents.
    */
  case class Price(cents: Long) extends AnyVal {
    def format(dec: String = ".", prefix: String = "", suffix: String = " Euro"): String = {
      val (main, cs) = divMod
      List(prefix, main, dec, cs, suffix).mkString("")
    }

    /** Get the division quotient and modulo values of divisor "100".
      *
      * Useful to get the "main" part and "cents" part of currencies
      * which have this two units.
      *
      * @return tuple of d
      */
    def divMod: (Long, Long) = (cents / 100, cents % 100)

    /** Gets the divMod of the absolute price
      */
    def divModAbs: (Long, Long) = abs.divMod

    def negate: Price =
      copy(cents = cents * (-1))

    def priceTuple: (PriceSignum.Value, Long, Long) = {
      val sig = if (cents >= 0) PriceSignum.Plus else PriceSignum.Minus
      val (main, cts) = divModAbs
      (sig, main, cts)
    }

    private def abs: Price =
      copy(cents = scala.math.abs(cents))
  }

  case class ImgPath(url: String) extends AnyVal

  case class Product(name: ProductName, price: Price, img: ImgPath, cat: Category, taxClassId: TaxClassEnum)

}
