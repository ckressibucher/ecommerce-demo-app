package scalapp

import akka.actor.{Actor, Props}
import scalapp.model._

object CartActor {

  sealed trait Msg

  final case class AddToCart(product: Product, qty: Int) extends Msg

  final case class DeleteProduct(product: Product) extends Msg

  final case object ClearCart extends Msg

  final case object GetCartView extends Msg

  final case class ApplyDiscount(code: String) extends Msg

  final case class RemoveDiscount(code: String) extends Msg

  // the result type of cart update actions (a message returned to the sender)
  final case class CartUpdateResult(either: Either[String, CartView])

  def props = Props(new CartActor(CartData.empty))
}

class CartActor(var cart: CartData) extends Actor {

  import CartActor._

  def receive = {
    case AddToCart(product, qty) =>
      cart = cart.addProduct(product, qty)
      sender() ! CartUpdateResult(CartService.caluclateCart(cart))
    case DeleteProduct(product) =>
      cart = cart.deleteProduct(product)
      sender() ! CartUpdateResult(CartService.caluclateCart(cart))
    case ClearCart =>
      cart = CartData.empty
      sender() ! CartUpdateResult(CartService.caluclateCart(cart))
    case ApplyDiscount(code) =>
      val result = cart.addDiscount(code)
      if (result.isRight) {
        cart = result.right.get
        sender() ! CartUpdateResult(CartService.caluclateCart(cart))
      } else {
        // don't update cart
        sender() ! CartUpdateResult(Left(result.left.get))
      }
    case RemoveDiscount(code) =>
      cart = cart.removeDiscount(code)
      sender() ! CartUpdateResult(CartService.caluclateCart(cart))

    case GetCartView =>
      sender() ! CartUpdateResult(CartService.caluclateCart(cart))
  }
}
