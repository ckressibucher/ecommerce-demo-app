package scalapp

import scalapp.model.{ Product, Category }
import scala.concurrent.Future
import scalapp.model.CartView

case class FileData(name: String, size: Long)

trait Api {
  /** Returns all categories
    *
    * Important: must have parenthesis for autowire to work!
    */
  def categories(): Future[Seq[Category]]

  /** Returns a list of products by category (or all products if no category given)
    */
  def products(category: Option[Category]): Future[Seq[Product]]

  /** Returns `Some` error message in case of failure, `None` in case of success.
    */
  def addToCart(sessId: String, productName: String, qty: Int): Future[CartView]

  def deleteFromCart(sessId: String, productName: String): Future[CartView]

  def clearCart(sessId: String): Future[CartView]

  def applyDiscount(sessId: String, code: String): Future[CartView]

  def showCart(sessId: String): Future[CartView]
}
