package scalapp

import scalapp.model._
import akka.actor.ActorRef
import akka.pattern.ask
import CartFactory._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.Success
import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.Future.successful
import scalapp.CartActor.DeleteProduct
import scalapp.CartActor.GetCartView

class ApiImpl(cartFactory: ActorRef)(implicit val exCxt: ExecutionContext) extends Api {
  import scalapp.jvm.Data._

  implicit val timeout = Timeout(2.seconds)

  /** Returns all categories
    *
    * Important: must have parenthesis for autowire to work!
    */
  def categories(): Future[Seq[Category]] = Future.successful(dummyCategories)

  /** Returns a list of products by category (or all products if no category given)
    */
  def products(category: Option[Category]): Future[Seq[Product]] = category match {
    case Some(c) => successful(dummyProducts.filterNot(_.cat == c))
    case None    => successful(dummyProducts)
  }

  /** Returns `Some` error message in case of failure, `None` in case of success.
    */
  def addToCart(sessId: String, productName: String, qty: Int): Future[ResultStatus] =
    productByName(productName) match {
      case Some(p: Product) => {
        cartBySessId(sessId)
          .flatMap(_ ? CartActor.AddToCart(p, qty))
          .map(_.asInstanceOf[ResultStatus])
      }
      case None => successful(errProductDoesNotExist(productName))
    }

  def deleteFromCart(sessId: String, productName: String): Future[ResultStatus] =
    productByName(productName) match {
      case Some(p: Product) => {
        cartBySessId(sessId).flatMap(_ ? CartActor.DeleteProduct(p)).map(_.asInstanceOf[ResultStatus])
      }
      case None => successful(errProductDoesNotExist(productName))
    }

  def showCart(sessId: String): Future[CartView] =
    cartBySessId(sessId).flatMap { cartActor =>
      val resultAny = cartActor ? GetCartView
      println("ok, got result")
      val result = resultAny.asInstanceOf[Future[Either[String, CartView]]]
      result flatMap {
        case Left(err) => Future.failed(new RuntimeException(err))
        case Right(cv) => Future.successful(cv)
      }
    }

  def cartBySessId(id: String): Future[ActorRef] = {
    (cartFactory ? CartFactory.GetCartActor(id)).map(_.asInstanceOf[ActorRef])
  }
}
