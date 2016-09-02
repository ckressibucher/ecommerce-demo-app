package scalapp

import akka.actor.{ ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.collection.immutable.Seq
import upickle.default.{ Reader, Writer }
import upickle.{ default => upick }
import scala.io.StdIn
import akka.http.scaladsl.model.HttpEntity.Strict
import scala.concurrent.Future
import akka.http.scaladsl.server.Directive

object Router extends autowire.Server[String, Reader, Writer] {
  def read[Result: Reader](p: String) = upick.read[Result](p)
  def write[Result: Writer](r: Result) = upick.write(r)
}

object Server {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("example-ecom-shop")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete
    implicit val execContext = system.dispatcher
    implicit val timeout = Timeout(3.seconds) // needed for `?` ask pattern

    val apiRouter = system.actorOf(Props(classOf[ApiRouter]))

    val route = get {
      pathSingleSlash {
        complete {
          HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            Page.skeleton.render)
        }
      } ~
        getFromResourceDirectory("")
    } ~ post {
      path("api" / Segments) { s =>
        extract { _.request.entity.toStrict(3.seconds).map(_.data.decodeString("UTF-8")) } { bodyFuture =>
          val r: Future[String] = bodyFuture.flatMap { body =>
            println("body: " + body)
            val args = upick.read[Map[String, String]](body)
            (apiRouter ? ApiRouter.Request(s, args)).map(_.asInstanceOf[String])
          }
          onComplete(r) { x =>
            complete(x)
          }
        }
        //        extract { r => r.request.entity.toStrict(3.seconds) } { e: Future[Strict] =>
        //          e.flatMap { es =>
        //            val body = es.data.decodeString("UTF-8")
        //            val args = upick.read[Map[String, String]](body)
        //            (apiRouter ? ApiRouter.Request(s, args)).map(_.asInstanceOf[String])
        //          }
        //        }
        //        val responseFuture = extract(_.request.entity.toStrict(3.seconds)).flatMap { e: Strict =>
        //          val args = upick.read[Map[String, String]](e.data.decodeString("UTF-8"))
        //          (apiRouter ? ApiRouter.Request(s, args))
        //          onComplete(responseFuture) { r =>
        //            complete(r.asInstanceOf[String])
        //          }
        //        }
      }
    }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown the actor system when done
  }
}