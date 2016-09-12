package scalapp

import akka.actor.Actor
import akka.pattern.ask
import akka.actor.ActorRef

import scala.concurrent.duration._
import akka.util.Timeout

import scala.util.{Failure, Success}

/** Manages cart actors
  */
class CartFactory extends Actor {
  import CartFactory._

  // TODO one cart per session
  val _cart = context.actorOf(CartActor.props)

  implicit val timeout = Timeout(5.seconds)
  implicit val excCtxt = context.system.dispatcher

  def receive = {
    case CartFacadeAction(sessId, msg @ CartActor.GetCartView) =>
      // respond to sender on `GetCartView` actions only
      val sdr = sender()
      (cartBySession(sessId) ? msg).onSuccess {
        case cartView => sdr ! cartView
      }
    case CartFacadeAction(sessId, msg) =>
      cartBySession(sessId) ! msg
  }

  def cartBySession(id: String): ActorRef = _cart // ignore session for now...
}

object CartFactory {

  // Message protocol
  case class CartFacadeAction(sessId: String, msg: CartActor.Msg)
}
