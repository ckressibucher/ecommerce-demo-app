package scalapp

import scalapp.model._
import akka.actor.Actor
import akka.actor.ActorRef

/** Manages cart actors
  */
class CartFactory extends Actor {
  import CartFactory._

  // TODO one cart per session
  val _cart = context.actorOf(CartActor.props)

  def receive = {
    case GetCartActor(sessId) => sender() ! cartBySession(sessId)
  }

  def cartBySession(id: String): ActorRef = _cart // ignore session for now...
}

object CartFactory {

  // Message protocol
  case class GetCartActor(sessId: String)
}
