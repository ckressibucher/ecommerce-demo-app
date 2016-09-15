package scalapp.client

import scalapp.model.{ Product, Category }
import diode.data.Pot
import scalapp.model.CartView

case class AppModel(categories: CategoryModel, products: ProductModel, cartView: Pot[CartView])

case class CategoryModel(cats: Pot[Seq[Category]], cur: Option[Category])

// for this simple demo app with not many products we load all
// products at once...
case class ProductModel(all: Pot[Seq[Product]])
