package scalapp

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.Success

/** Manages cart actors
  */
class CartFactory extends Actor {
  import CartFactory._

  // TODO one cart per session
  val _cart = context.actorOf(CartActor.props)

  implicit val timeout = Timeout(5.seconds)
  implicit val excCtxt = context.system.dispatcher

  def receive = {
    case CartFacadeAction(sessId, msg) =>
      val sdr = sender()
      (cartBySession(sessId) ? msg).onComplete { futureResult =>
        sdr ! futureResult
      }
  }

  def cartBySession(id: String): ActorRef = _cart // ignore session for now...
}

object CartFactory {

  // Message protocol
  case class CartFacadeAction(sessId: String, msg: CartActor.Msg)
}
