package scalapp

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future.successful
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scalapp.CartActor.CartUpdateResult
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
    case None => successful(dummyProducts)
  }

  def addToCart(sessId: String, productName: String, qty: Int): Future[UpdateResult] =
    productByName(productName) match {
      case Some(p: Product) =>
        cartFactory ! CartFacadeAction(sessId, CartActor.AddToCart(p, qty))
        getCartView(sessId)
      case None => Future.failed(new RuntimeException(s"product $productName does not exist"))
    }

  def deleteFromCart(sessId: String, productName: String): Future[UpdateResult] =
    productByName(productName) match {
      case Some(p: Product) =>
        cartFactory ! CartFacadeAction(sessId, CartActor.DeleteProduct(p))
        getCartView(sessId)
      case None => Future.failed(new RuntimeException(s"product $productName does not exist"))
    }

  def clearCart(sessId: String): Future[UpdateResult] = {
    mapToCartView(cartFactory ? CartFacadeAction(sessId, CartActor.ClearCart))
  }

  def applyDiscount(sessId: String, code: String): Future[UpdateResult] =
    mapToCartView(cartFactory ? CartFacadeAction(sessId, CartActor.ApplyDiscount(code)))

  def removeDiscount(sessId: String, code: String): Future[UpdateResult] =
    mapToCartView(cartFactory ? CartFacadeAction(sessId, CartActor.RemoveDiscount(code)))

  def showCart(sessId: String): Future[UpdateResult] =
    getCartView(sessId)

  private def getCartView(sessId: String) =
    mapToCartView(cartFactory ? CartFacadeAction(sessId, CartActor.GetCartView))

  // `Any` should be `Future[CartUpdateResult]`
  private def mapToCartView(result: Future[Any]): Future[UpdateResult] = result.flatMap {
    case Success(CartUpdateResult(cartViewResult)) => Future.successful(cartViewResult)
    //case failAction => Future.failed(ActionFailedMsg("Server error"))
    case failAction => println("\nerror:\n"); println(failAction); println("\n\n"); Future.failed(ActionFailedMsg("Server error"))
  }
}
