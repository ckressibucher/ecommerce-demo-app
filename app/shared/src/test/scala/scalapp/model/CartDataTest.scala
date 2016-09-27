package scalapp.model

import utest._

object CartDataTest extends TestSuite {

  val tests = this {

    val productA =
      Product(ProductName("product A"), Price(100), ImgPath("img"), Category("cat"), TaxRegular)

    val productB =
      Product(ProductName("product B"), Price(200), ImgPath("img"), Category("cat"), TaxRegular)

    'addProduct - {
      val newCart = CartData.empty.addProduct(productA, 2)
      newCart.productItems ==> List(CartItem(productA, 2))

      'addProductAgain - {
        val nextCart = newCart.addProduct(productA, 1)
        nextCart.productItems ==> List(CartItem(productA, 3))
      }

      'addOtherProduct - {
        newCart.addProduct(productB, 1).productItems ==> CartItem(productA, 2) :: CartItem(productB, 1) :: Nil
      }
    }

    'deleteProduct - {
      val cartWithA = CartData.empty.addProduct(productA, 2)

      'removeExisting - {
        cartWithA.deleteProduct(productA).productItems ==> Nil
      }

      'removeNonExisting - {
        CartData.empty.deleteProduct(productA).productItems ==> Nil
      }
    }

    'updateQty - {
      val cartWithA = CartData.empty.addProduct(productA, 2)

      'ofExistingProduct - {
        cartWithA.updateProductQty(productA, 3).productItems ==> List(CartItem(productA, 3))
      }

      'ofNonExistingProduct - {
        cartWithA.updateProductQty(productB, 2).productItems ==> CartItem(productA, 2) :: CartItem(productB, 2) :: Nil
      }
    }

    'qtyByProduct - {
      val cartWithA = CartData.empty.addProduct(productA, 2)
      cartWithA.qtyByProduct(productA) ==> 2
      cartWithA.qtyByProduct(productB) ==> 0
    }

    'addDiscount - {
      val newCart = CartData.empty.addDiscount("demo-100").right.get
      newCart.discounts ==> List("demo-100")

      'addAgain - {
        newCart.addDiscount("demo-100") ==> Left("Discount 'demo-100' already exists in this cart")
      }
    }
  }

}
