package scalapp

import scala.concurrent.Future
import scalapp.model.{CartView, Category, Product}

case class FileData(name: String, size: Long)

// User message to send from the server side to the client side, in case something went wrong
case class ActionFailedMsg(msg: String) extends Exception(msg)

trait Api {
  // to inform sender about the update result
  type UpdateResult = Either[String, CartView]

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
  def addToCart(sessId: String, productName: String, qty: Int): Future[UpdateResult]

  def deleteFromCart(sessId: String, productName: String): Future[UpdateResult]

  def clearCart(sessId: String): Future[UpdateResult]

  def applyDiscount(sessId: String, code: String): Future[UpdateResult]

  def removeDiscount(sessId: String, code: String): Future[UpdateResult]

  def showCart(sessId: String): Future[UpdateResult]
}
