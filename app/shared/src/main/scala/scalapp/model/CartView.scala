package scalapp.model

import scala.collection.immutable

case class CartView(lines: immutable.Seq[CartView.Line], taxes: CartView.TaxResult, grandTotal: Price)

/** Representation of a cart result. Mapped from [[CartBase]], but
  * with some simplifications...
  */
object CartView {

  case class Line(p: Product, qty: Int, price: Price, taxClass: String)

  case class TaxLine(cls: String, rate: Double, sum: Price)

  case class TaxResult(lines: immutable.Seq[TaxLine], total: Price)

}
