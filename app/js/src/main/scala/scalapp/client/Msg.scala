package scalapp.client

import diode.Action
import diode.data.{ Pot, PotAction }
import scalapp.model._

case class SelectCategory(c: Category) extends Action

case class UpdateCategories(potResult: Pot[Seq[Category]]) extends PotAction[Seq[Category], UpdateCategories] {
  def next(newResult: Pot[Seq[Category]]) = UpdateCategories(newResult)
}

case class UpdateProducts(potResult: Pot[Seq[Product]]) extends PotAction[Seq[Product], UpdateProducts] {
  def next(newResult: Pot[Seq[Product]]) = UpdateProducts(newResult)
}

case class AddProduct(product: Product, qty: Int) extends Action

case class RemoveProduct(product: Product) extends Action

case class UpdateProductQty(product: Product, newQty: Int) extends Action
