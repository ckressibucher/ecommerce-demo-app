package scalapp

import scalapp.model._
import akka.actor.ActorRef
import akka.pattern.ask
import ApiHandler._
import akka.util.Timeout
import scala.concurrent.duration._
import com.sksamuel.scapegoat.inspections.unsafe.AsInstanceOf
import scala.util.Success
import scala.concurrent.{ ExecutionContext, Future }
import scalapp.CartActor.DeleteProduct

/** @param handler Reference to the actor handling the actual requests. Must be of type `ApiHandler`.
  */
class ApiImpl(val handler: ActorRef)(implicit val execCxt: ExecutionContext) extends Api {
  implicit val timeout = Timeout(3.seconds)

  /** Returns all categories
    *
    * Important: must have parenthesis for autowire to work!
    */
  def categories() = (handler ? GetCategories).map(_.asInstanceOf[Seq[Category]])

  /** Returns a list of products by category (or all products if no category given)
    */
  def products(category: Option[Category]) = (handler ? GetProducts(category)).map(_.asInstanceOf[Seq[Product]])

  /** Returns `Some` error message in case of failure, `None` in case of success.
    */
  def addToCart(productName: String, qty: Int) = {
    for {
      c <- cartActor
      p <- handler.ask(GetProduct(productName))
      result <- p match {
        case Some(product: Product) => c.ask(CartActor.AddToCart(product, qty))
        case None                   => Future.successful(errProductDoesNotExist(productName))
      }
    } yield result.asInstanceOf[ResultStatus]
  }

  def deleteFromCart(productName: String) = {
    for {
      c <- cartActor
      p <- handler.ask(GetProduct(productName))
      result <- p match {
        case Some(product: Product) => c.ask(CartActor.DeleteProduct(product))
        case None                   => Future.successful(errProductDoesNotExist(productName))
      }
    } yield result.asInstanceOf[ResultStatus]
  }

  def showCart() = {
    cartActor.flatMap(_ ? CartActor.GetCartCopy).map(_.asInstanceOf[CartData])
  }

  def cartActor: Future[ActorRef] = (handler ? GetCartActor("some-session-id-TODO")).map(_.asInstanceOf[ActorRef])
}
