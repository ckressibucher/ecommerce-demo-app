package scalapp.client

import scalapp.model._
import diode.data.Pot
import scalapp.model.CartData

case class AppModel(categories: CategoryModel, products: ProductModel, cart: CartModel)

case class CategoryModel(cats: Pot[Seq[Category]], cur: Option[Category])

// for this simple demo app with not many products we load all
// products at once...
case class ProductModel(products: Pot[Seq[Product]])

case class CartModel(cartData: Pot[CartData])

// TODO define message bucket, e.g. for (ajax) status messages
