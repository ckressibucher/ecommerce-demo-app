package scalapp

import scalapp.model._
import akka.actor.Actor
import akka.actor.Props

class ApiRouter extends Actor {
  import ApiRouter._
  implicit val execContext = context.system.dispatcher

  val apiHandler = context.actorOf(Props(classOf[ApiHandler]))
  val apiImpl = new ApiImpl(apiHandler)

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
