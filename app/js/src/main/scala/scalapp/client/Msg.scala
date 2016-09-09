package scalapp.client

import diode.Action
import diode.data.{ Pot, PotAction, AsyncAction }
import scalapp.model.{ Product, Category, ResultStatus }
import scalapp.model.CartView

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
