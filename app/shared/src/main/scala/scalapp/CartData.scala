package scalapp

import scalapp.model._

case class CartData(productItems: List[CartItem]) {

  /** change the quantity of the given product by `diffQty`
    *
    * @param product The product
    * @param diffQty The increment to add to the existing quantity (may be negative or positive)
    */
  def updateProduct(product: Product, diffQty: Int): CartData = {
    val oldQty = qtyByProduct(product)
    val newQty = oldQty + diffQty
    updateProductQty(product, newQty)
  }

  /** Alias for `updateProduct` */
  def addProduct(product: Product, qty: Int): CartData = updateProduct(product, qty)

  /** Directly set the new quantity of the product */
  def updateProductQty(product: Product, newQty: Int): CartData = {
    // remove old item for this product (if found) and add new item
    val newItems = productItems.filterNot(_._1.name == product.name) :+ (product, newQty)
    copy(productItems = newItems)
  }

  def deleteProduct(product: Product): CartData = {
    val newItems = productItems.filterNot(_._1.name == product.name)
    copy(productItems = newItems)
  }

  def qtyByProduct(product: Product): Int = productItems.find(_._1.name == product.name) match {
    case Some((_, q)) => q
    case None         => 0
  }

}