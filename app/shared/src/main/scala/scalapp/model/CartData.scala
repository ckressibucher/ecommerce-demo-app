package scalapp.model

case class CartData(productItems: List[CartItem], discounts: List[String]) {

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

  def addDiscount(code: String): CartData = {
    val newDiscs = if (discounts.contains(code))
      discounts
    else {
      discounts :+ code
    }
    copy(discounts = newDiscs)
  }

  /** Directly set the new quantity of the product */
  def updateProductQty(product: Product, newQty: Int): CartData = {
    val newItems = if (productItems.exists(_.product.name.name == product.name.name)) {
      // make sure the order stays the same
      productItems.map {
        case i@CartItem(p, _) if p.name == product.name => i.copy(qty = newQty)
        case i => i
      }
    } else {
      productItems :+ CartItem(product, newQty)
    }
    copy(productItems = newItems)
  }

  def deleteProduct(product: Product): CartData = {
    val newItems = productItems.filterNot(_.product.name == product.name)
    copy(productItems = newItems)
  }

  def qtyByProduct(product: Product): Int = productItems.find(_.product.name == product.name) match {
    case Some(CartItem(_, q)) => q
    case None                 => println(s"product ${product.name} not found. products: $productItems"); 0
  }

}

object CartData {
  val empty = CartData(List(), List())
}
