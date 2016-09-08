package scalapp

import scalapp.model.{ Product, Category, ResultStatus }
import scala.concurrent.Future
import scalapp.model.CartData

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
  def addToCart(sessId: String, productName: String, qty: Int): Future[ResultStatus]

  def deleteFromCart(sessId: String, productName: String): Future[ResultStatus]

  def showCart(sessId: String): Future[CartData]
}
