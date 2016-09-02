package scalapp

package object model {

  type Category = String

  /** `Some` error message or `None` on success.
    */
  type ResultStatus = Option[String]

  /** A product, and its quantity in the cart.
    */
  type CartItem = (Product, Int)

  case class Product(name: String, price: Long, img: String, cat: Category)

}
