package scalapp

import scalapp.model._
import akka.actor.{ Actor, Props, ActorRef }

class ApiRouter extends Actor {
  import ApiRouter._
  implicit val execContext = context.system.dispatcher

  // The cart factory is responsible to manage one cart per user session.
  val cartFactory = context.actorOf(Props(classOf[CartFactory]))

  val apiImpl = new ApiImpl(cartFactory)

  def receive = {
    case Request(segments, args) => {
      println("handle request...")
      val sdr = sender()
      val f = Router.route[Api](apiImpl) {
        println(args)
        autowire.Core.Request(segments, args)
      }
      f.onSuccess {
        case x =>
          println(x)
          sdr ! x
      }
      f.onFailure {
        case y => println(y.getMessage)
      }
    }
  }
}

object ApiRouter {
  // Message protocol
  final case class Request(pathSegments: List[String], args: Map[String, String])
}
