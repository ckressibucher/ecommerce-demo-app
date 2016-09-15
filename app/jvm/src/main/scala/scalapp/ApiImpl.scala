package scalapp

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future.successful
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scalapp.CartFactory.CartFacadeAction
import scalapp.model._

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

  def addToCart(sessId: String, productName: String, qty: Int): Future[CartView] =
    productByName(productName) match {
      case Some(p: Product) =>
        cartFactory ! CartFacadeAction(sessId, CartActor.AddToCart(p, qty))
        mapToCartView(cartFactory ? CartFacadeAction(sessId, CartActor.GetCartView))
      case None => Future.failed(new RuntimeException(s"product $productName does not exist"))
    }

  def deleteFromCart(sessId: String, productName: String): Future[CartView] =
    productByName(productName) match {
      case Some(p: Product) =>
        cartFactory ! CartFacadeAction(sessId, CartActor.DeleteProduct(p))
        mapToCartView(cartFactory ? CartFacadeAction(sessId, CartActor.GetCartView))
      case None => Future.failed(new RuntimeException(s"product $productName does not exist"))
    }

  def showCart(sessId: String): Future[CartView] =
    mapToCartView(cartFactory ? CartFacadeAction(sessId, CartActor.GetCartView))

  // `Any` should be `Either[String, CartView]`
  def mapToCartView(result: Future[Any]): Future[CartView] = result.flatMap {
    case Right(cartView) => Future.successful(cartView.asInstanceOf[CartView])
    case Left(err: String) => Future.failed(new RuntimeException(err))
  }
}
