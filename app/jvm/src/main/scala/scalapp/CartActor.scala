package scalapp

import akka.actor.Actor
import scalapp.model._
import akka.actor.Props

object CartActor {

  // Message protocol
  sealed trait Msg
  case class AddToCart(product: Product, qty: Int) extends Msg
  case class DeleteProduct(product: Product) extends Msg
  case object GetCartCopy extends Msg

  def props = Props(new CartActor(CartData(List())))
}

class CartActor(var cart: CartData) extends Actor {

  import CartActor._

  def receive = {
    case AddToCart(product, qty) => cart = cart.addProduct(product, qty)
    case DeleteProduct(product)  => cart = cart.deleteProduct(product)
    case GetCartCopy             => sender() ! cart.copy()
  }
}