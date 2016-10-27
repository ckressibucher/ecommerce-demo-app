package scalapp

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.collection.mutable
import scalapp.model.CartData

/** Manages cart actors
  */
class CartFactory extends Actor {

  import CartFactory._

  private val carts: mutable.Map[String, ActorRef] = mutable.Map[String, ActorRef]()

  implicit val timeout = Timeout(5.seconds)
  implicit val excCtxt = context.system.dispatcher

  def receive = {
    case CartFacadeAction(sessId, msg @ CartActor.GetCartView) =>
      // we need to send the result back to the sender
      val sdr = sender()
      (cartBySession(sessId) ? msg).onComplete { futureResult =>
        sdr ! futureResult
      }
    case CartFacadeAction(sessId, msg) =>
      cartBySession(sessId) ! msg
  }

  private def cartBySession(id: String): ActorRef = {
    carts.get(id) match {
      case Some(ar) => ar
      case None =>
        val ar = context.actorOf(
          Props(classOf[CartActor], CartData.empty),
          "cart-" + id)
        carts.update(id, ar)
        ar
    }
  }
}

object CartFactory {

  // Message protocol
  case class CartFacadeAction(sessId: String, msg: CartActor.Msg)

}
