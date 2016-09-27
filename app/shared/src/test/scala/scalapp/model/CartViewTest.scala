package scalapp.model

import utest._

import scalapp.model.CartView.TaxResult

object CartViewTest extends TestSuite {

  val tests = this {
    def product(name: String, unitPrice: Long, qty: Int = 1, cat: String = "c",
                tax: TaxClassEnum = TaxReduced): (Product, CartView.Line) = {
      val product =
        Product(ProductName(name), Price(unitPrice), ImgPath("img"), Category(cat), tax)
      val line = CartView.Line(
        product,
        qty,
        Price(unitPrice * qty),
        tax.tc
      )
      (product, line)
    }
    val (productA, lineA) = product("product A", unitPrice = 100, qty = 1)
    val (productB, lineB) = product("product B", unitPrice = 410, qty = 2)
    val lines = lineA :: lineB :: Nil
    val cart = CartView(lines, List(), TaxResult(List(), Price(0)), Price(0))

    'qtyByProduct - {
      'found - {
        cart.qtyByProduct(productA) ==> 1
        cart.qtyByProduct(productB) ==> 2
      }
      'notFound - {
        cart.qtyByProduct(Product(ProductName("c"), Price(1), ImgPath("i"), Category("c"), TaxReduced)) ==> 0
      }
    }
  }
}
