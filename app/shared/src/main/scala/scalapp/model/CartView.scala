package scalapp.model

import scala.collection.immutable

case class CartView(lines: immutable.Seq[CartView.Line],
                    discounts: immutable.Seq[CartView.Discount],
                    taxes: CartView.TaxResult,
                    grandTotal: Price) {

  def qtyByProduct(product: Product): Int = {
    lines.find(_.p.name.name == product.name.name) match {
    case Some(line) => line.qty
    case None => println(s"product ${product.name} not found. products: ${lines.map(_.p)}"); 0
  }}
}

/** Representation of a cart result. Mapped from [[plus.coding.ckrecom.CartBase]], but
  * with some simplifications...
  */
object CartView {

  case class Line(p: Product, qty: Int, price: Price, taxClass: String)

  case class Discount(code: String, amount: Price, taxClasses: immutable.Seq[String])

  case class TaxLine(cls: String, rate: Double, totalSum: Price, taxAmount: Price)

  case class TaxResult(lines: immutable.Seq[TaxLine], total: Price)

}
