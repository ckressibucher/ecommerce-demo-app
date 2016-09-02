package scalapp

import scalapp.model._

case class FileData(name: String, size: Long)

trait Api {
  /** Returns all categories
    *
    * Important: must have parenthesis for autowire to work!
    */
  def categories(): Seq[Category]

  /** Returns a list of products by category (or all products if no category given)
    */
  def products(category: Option[Category]): Seq[Product]

  /** Returns `Some` error message in case of failure, `None` in case of success.
    */
  def addToCart(productName: String, qty: Int): ResultStatus

  def deleteFromCart(productName: String): ResultStatus

  def showCart(): List[CartItem]
}
