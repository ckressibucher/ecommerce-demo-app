package scalapp

import scalapp.model._
import akka.actor.Actor
import akka.actor.ActorRef

class ApiHandler extends Actor {
  import ApiHandler._

  // TODO one cart per session
  val _cart = context.actorOf(CartActor.props)

  def receive = {
    case GetCartActor(sessId) => sender() ! cartBySession(sessId)
    case GetCategories        => sender() ! dummyCategories
    case GetProduct(name)     => sender() ! productByName(name)
    case GetProducts(categoryFilter) => sender() ! (categoryFilter match {
      case Some(c) => dummyProducts.filterNot { _.cat == c }
      case None    => dummyProducts
    })
  }

  def cartBySession(id: String): ActorRef = _cart // ignore session for now...
}

object ApiHandler {

  val dummyCategories = List("Shoes", "Bags & Luggage", "Glasses", "Jewelry")

  val dummyProducts = List(
    Product("black shoes", 8900l, "http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/m/ams000a_2.jpg", "Shoes"),
    Product("Suede loafer, navy", 11900l, "http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/m/ams010a_2.jpg", "Shoes"),
    Product("Wingtip, Cognac", 9900l, "http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/m/ams005a_2.jpg", "Shoes"),
    Product("Isla Handbag", 18900l, "http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/b/abl000_4.jpg", "Bags & Luggage"),
    Product("Houston travel wallet", 5900l, "http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/b/abl004a_1.jpg", "Bags & Luggage"),
    Product("Retro glasses", 8900l, "http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/c/ace002a_1.jpg", "Glasses"),
    Product("Blue horizon bracelets", 7900l, "http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/c/acj006_2.jpg", "Jewelry"),
    Product("Silver desert necklace", 18900l, "http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/c/acj000_2.jpg", "Jewelry"));

  def productByName(name: String): Option[Product] =
    dummyProducts.find(_.name == name)

  def errProductDoesNotExist(p: String): ResultStatus =
    Some(s"product $p does not exist")

  // Message protocol
  case class GetCartActor(sessId: String)
  case object GetCategories
  case class GetProduct(name: String)
  case class GetProducts(categoryFilter: Option[Category])
}
