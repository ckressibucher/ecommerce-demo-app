package scalapp.client

import diode.Action
import diode.data.{Pot, PotAction}

import scalapp.model.{CartView, Category, Product}

case class SelectCategory(c: Category) extends Action

case object ResetCategory extends Action

case class UpdateCategories(potResult: Pot[Seq[Category]] = Pot.empty) extends PotAction[Seq[Category], UpdateCategories] {
  def next(newResult: Pot[Seq[Category]]) = UpdateCategories(newResult)
}

case class UpdateProducts(potResult: Pot[Seq[Product]] = Pot.empty) extends PotAction[Seq[Product], UpdateProducts] {
  def next(newResult: Pot[Seq[Product]]) = UpdateProducts(newResult)
}

case class UpdateCartView(potResult: Pot[CartView] = Pot.empty) extends PotAction[CartView, UpdateCartView] {
  def next(newResult: Pot[CartView]) = UpdateCartView(newResult)
}

case class AddProduct(product: Product, qty: Int) extends Action

case class RemoveProduct(product: Product) extends Action

case class UpdateProductQty(product: Product, newQty: Int) extends Action

case object ClearCart extends Action

case class ApplyDiscount(code: String) extends Action

case class RemoveDiscount(code: String) extends Action

// message to initialize the most important data when application loads...
case object InitializeApp extends Action
