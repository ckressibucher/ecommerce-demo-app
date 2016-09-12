package scalapp

import plus.coding.ckrecom.TaxSystem
import plus.coding.ckrecom.TaxSystem.{DefaultTaxClass, SimpleTax}

import scalapp.model._

package object jvm {

  object Data {
    val dummyCategories = List(Category("Shoes"), Category("Bags & Luggage"), Category("Glasses"), Category("Jewelry"))

    // we define 2 tax classes
    val taxRegular = SimpleTax(19, 100)
    val taxReduced = SimpleTax(7, 100)

    val dummyProducts = List(
      Product(
        ProductName("black shoes"),
        Price(8900l),
        ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/m/ams000a_2.jpg"),
        Category("Shoes"),
        TaxRegular),
      Product(
        ProductName("Suede loafer, navy"),
        Price(11900l),
        ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/m/ams010a_2.jpg"),
        Category("Shoes"),
        TaxRegular),
      Product(
        ProductName("Wingtip, Cognac"),
        Price(9900l),
        ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/m/ams005a_2.jpg"),
        Category("Shoes"),
        TaxRegular),
      Product(
        ProductName("Isla Handbag"),
        Price(18900l),
        ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/b/abl000_4.jpg"),
        Category("Bags & Luggage"),
        TaxRegular),
      Product(
        ProductName("Houston travel wallet"),
        Price(5900l),
        ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/b/abl004a_1.jpg"),
        Category("Bags & Luggage"),
        TaxRegular),

      // and then some products with "reduced" tax classes (does not match reality, but we don't care..)
      Product(
        ProductName("Retro glasses"),
        Price(8900l),
        ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/c/ace002a_1.jpg"),
        Category("Glasses"),
        TaxReduced),
      Product(
        ProductName("Blue horizon bracelets"),
        Price(7900l),
        ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/c/acj006_2.jpg"),
        Category("Jewelry"),
        TaxReduced),
      Product(
        ProductName("Silver desert necklace"),
        Price(18900l),
        ImgPath("http://cdn.magento-demo.lexiconn.com/media/catalog/product/cache/1/small_image/210x/9df78eab33525d08d6e5fb8d27136e95/a/c/acj000_2.jpg"),
        Category("Jewelry"),
        TaxReduced))

    def productByName(name: String): Option[Product] =
      dummyProducts.find(_.name.name == name)

    def errProductDoesNotExist(p: String): ResultStatus =
      Some(s"product $p does not exist")

    def taxClassString(tc: DefaultTaxClass)(implicit taxSystem: TaxSystem[DefaultTaxClass]): String = tc match {
      case `taxRegular` => s"B (${taxRegular.num}%)"
      case `taxReduced` => s"A (${taxReduced.num}%)"
      case x => taxSystem.rate(x).toString
    }
  }

}
