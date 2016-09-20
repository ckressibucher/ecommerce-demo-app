package scalapp

package object model {

  case class Category(name: String) extends AnyVal

  // it seems that autowire cannot create macros for
  // enums, so we model this with a case class and two
  // concrete, globally available instances.
  case class TaxClassEnum(tc: String) extends AnyVal
  val TaxRegular = TaxClassEnum("B")
  val TaxReduced = TaxClassEnum("A")

  /** `Some` error message or `None` on success.
    */
  type ResultStatus = Option[String]

  /** A product, and its quantity in the cart.
    */
  case class CartItem(product: Product, qty: Int)

  case class ProductName(name: String) extends AnyVal

  /**
    * A price is an integer value of cents.
    */
  case class Price(cents: Long) extends AnyVal {
    def format(dec: String = ".", prefix: String = "", suffix: String = " Euro"): String = {
      val (main, cs) = divMod
      List(prefix, main, dec, cs, suffix).mkString("")
    }

    def divMod: (Long, Long) = (cents / 100, cents % 100)

    def divModAbs: (Long, Long) = abs.divMod

    private def abs: Price =
      copy(cents = scala.math.abs(cents))
  }

  case class ImgPath(url: String) extends AnyVal

  case class Product(name: ProductName, price: Price, img: ImgPath, cat: Category, taxClassId: TaxClassEnum)

}
