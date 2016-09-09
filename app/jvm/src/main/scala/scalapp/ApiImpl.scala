package scalapp

import scalapp.model._
import akka.actor.ActorRef
import akka.pattern.ask
import CartFactory._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import com.sksamuel.scapegoat.inspections.unsafe.AsInstanceOf
import scala.util.Success
import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.Future.successful
import scalapp.CartActor.DeleteProduct
import scalapp.CartActor.GetCartView

class ApiImpl(cartFactory: ActorRef)(implicit val exCxt: ExecutionContext) extends Api {
  import ApiImpl._

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
      val result: Either[String, CartView] = (cartActor ? GetCartView).asInstanceOf[Either[String, CartView]]
      result match {
        case Left(err) => Future.failed(new RuntimeException(err))
        case Right(cv) => Future.successful(cv)
      }
    }

  def cartBySessId(id: String): Future[ActorRef] = {
    (cartFactory ? CartFactory.GetCartActor(id)).map(_.asInstanceOf[ActorRef])
  }
}

object ApiImpl {
  val dummyCategories = List(Category("Shoes"), Category("Bags & Luggage"), Category("Glasses"), Category("Jewelry"))

  val dummyProducts = List(
    Product(ProductName("black shoes"), Price(8900l), ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/m/ams000a_2.jpg"), Category("Shoes")),
    Product(ProductName("Suede loafer, navy"), Price(11900l), ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/m/ams010a_2.jpg"), Category("Shoes")),
    Product(ProductName("Wingtip, Cognac"), Price(9900l), ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/m/ams005a_2.jpg"), Category("Shoes")),
    Product(ProductName("Isla Handbag"), Price(18900l), ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/b/abl000_4.jpg"), Category("Bags & Luggage")),
    Product(ProductName("Houston travel wallet"), Price(5900l), ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/b/abl004a_1.jpg"), Category("Bags & Luggage")),
    Product(ProductName("Retro glasses"), Price(8900l), ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/c/ace002a_1.jpg"), Category("Glasses")),
    Product(ProductName("Blue horizon bracelets"), Price(7900l), ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/c/acj006_2.jpg"), Category("Jewelry")),
    Product(ProductName("Silver desert necklace"), Price(18900l), ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/c/acj000_2.jpg"), Category("Jewelry")));

  def productByName(name: String): Option[Product] =
    dummyProducts.find(_.name.name == name)

  def errProductDoesNotExist(p: String): ResultStatus =
    Some(s"product $p does not exist")

}
