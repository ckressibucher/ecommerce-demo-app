package scalapp

package object model {

  case class Category(name: String) extends AnyVal

  /** `Some` error message or `None` on success.
    */
  type ResultStatus = Option[String]

  /** A product, and its quantity in the cart.
    */
  case class CartItem(product: Product, qty: Int)

  case class ProductName(name: String) extends AnyVal

  case class Price(cents: Long) extends AnyVal {
    def format(dec: String = ".", prefix: String = "", suffix: String = " Euro"): String = {
      val main = cents / 100
      val cs = cents % 100
      List(prefix, main, dec, cs, suffix).mkString("")
    }
  }

  case class ImgPath(url: String) extends AnyVal

  case class Product(name: ProductName, price: Price, img: ImgPath, cat: Category)

}
