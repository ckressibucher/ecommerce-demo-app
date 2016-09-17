package scalapp

import akka.actor.{Actor, Props}

import scalapp.model._

object CartActor {

  // Message protocol
  sealed trait Msg

  case class AddToCart(product: Product, qty: Int) extends Msg

  case class DeleteProduct(product: Product) extends Msg

  case object ClearCart extends Msg

  case object GetCartView extends Msg

  case class ApplyDiscount(code: String) extends Msg

  def props = Props(new CartActor(CartData.empty))
}

class CartActor(var cart: CartData) extends Actor {

  import CartActor._

  def receive = {
    case AddToCart(product, qty) =>
      cart = cart.addProduct(product, qty)
    case DeleteProduct(product) =>
      cart = cart.deleteProduct(product)
    case ClearCart =>
      cart = CartData.empty
    case ApplyDiscount(code) =>
      cart = cart.addDiscount(code)
    case GetCartView =>
      val result = CartService.caluclateCart(cart)
      sender() ! result
  }
}
