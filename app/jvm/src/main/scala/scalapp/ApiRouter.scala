package scalapp

import akka.actor.{Actor, Props}

class ApiRouter extends Actor {
  import ApiRouter._
  implicit val execContext = context.system.dispatcher

  // The cart factory is responsible to manage one cart per user session.
  private val cartFactory = context.actorOf(Props[CartFactory], "cart-factory")


  def receive = {
    case Request(segments, args, session) =>
      // get a handle to the sender ActorRef
      val sdr = sender()
      val apiImpl = new ApiImpl(cartFactory, session)
      val f = Router.route[Api](apiImpl) {
        autowire.Core.Request(segments, args)
      }
      f.onSuccess {
        case x => sdr ! x
      }
      f.onFailure {
        // TODO how to handle failure?
        case y => println(y)
      }
  }
}

object ApiRouter {
  // Message protocol
  final case class Request(pathSegments: List[String], args: Map[String, String], session: String)
}
