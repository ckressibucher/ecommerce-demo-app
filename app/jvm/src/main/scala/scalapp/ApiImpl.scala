package scalapp

import scalapp.model._

object ApiImpl extends Api {

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

  /** Returns all categories
    */
  def categories(): Seq[Category] = dummyCategories

  /** Returns a list of products by category (or all products if no category given)
    */
  def products(category: Option[Category]): Seq[Product] =
    category match {
      case Some(c) => dummyProducts filter { _.cat == c }
      case None    => dummyProducts
    }

  /** Returns `Some` error message in case of failure, `None` in case of success.
    * TODO
    */
  def addToCart(productName: String, qty: Int) = None

  /** TODO
    */
  def deleteFromCart(productName: String) = None

  /** TODO
    */
  def showCart() = List[CartItem]()

}
