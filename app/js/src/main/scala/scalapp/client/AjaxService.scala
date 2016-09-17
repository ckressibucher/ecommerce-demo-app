package scalapp.client

import org.scalajs.dom
import upickle.{default => upick}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object AjaxService extends autowire.Client[String, upick.Reader, upick.Writer] {
  override def doCall(req: Request) = {
    dom.ext.Ajax.post(
      url = "/api/" + req.path.mkString("/"),
      data = upick.write(req.args)).map(_.responseText)
  }

  def read[Result: upick.Reader](p: String) = upick.read[Result](p)
  def write[Result: upick.Writer](r: Result) = upick.write(r)
}
