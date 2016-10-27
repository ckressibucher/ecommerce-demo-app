package scalapp

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import upickle.default.{Reader, Writer}
import upickle.{default => upick}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn
import com.softwaremill.session.{SessionConfig, SessionManager, SessionResult}
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._

import scala.util.Random

object Router extends autowire.Server[String, Reader, Writer] {
  def read[Result: Reader](p: String) = upick.read[Result](p)

  def write[Result: Writer](r: Result) = upick.write(r)
}

object Server {

  val sessionConfig = SessionConfig.fromConfig()

  implicit val sessionManager = new SessionManager[String](sessionConfig)

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("example-ecom-shop")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete
    implicit val execContext = system.dispatcher
    implicit val timeout = Timeout(3.seconds) // needed for `?` ask pattern

    val apiRouter = system.actorOf(Props[ApiRouter],"api-router")

    val route = get {
      pathSingleSlash {
        val entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          Page.skeleton.render)

        // create session if not available already
        optionalSession(oneOff, usingCookies) {
          case Some(_) =>
            complete(entity)
          case None =>
            // this is not the actual session id, but the "content" of the session
            // TODO sessAppId is redundant, session id (cookie value) is enough
            val sessAppId = Random.alphanumeric.take(32).mkString
            setSession[String](oneOff, usingCookies, sessAppId) { ctx =>
              ctx.complete(entity)
            }
        }
      } ~
        getFromResourceDirectory("")
    } ~ post {
      requiredSession(oneOff, usingCookies) { session =>
        path("api" / Segments) { s =>
          extract {
            _.request.entity.toStrict(3.seconds).map(_.data.decodeString("UTF-8"))
          } { bodyFuture =>
            val r: Future[String] = bodyFuture.flatMap { body =>
              val args = upick.read[Map[String, String]](body)
              (apiRouter ? ApiRouter.Request(s, args, session)).map(_.asInstanceOf[String])
            }
            onComplete(r) { x =>
              complete(x)
            }
          }
        }
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
