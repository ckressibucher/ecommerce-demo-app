package scalapp

import scalatags.JsDom.all._
import org.scalajs.dom
import dom.html
import scalajs.js.annotation.JSExport
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import autowire._
import scalapp.model._
import upickle.{ default => upick }
import org.scalajs.dom.raw.HTMLUListElement

object Ajaxer extends autowire.Client[String, upick.Reader, upick.Writer] {
  override def doCall(req: Request) = {
    dom.ext.Ajax.post(
      url = "/api/" + req.path.mkString("/"),
      data = upick.write(req.args)).map(_.responseText)
  }

  def read[Result: upick.Reader](p: String) = upick.read[Result](p)
  def write[Result: upick.Writer](r: Result) = upick.write(r)
}

@JSExport
object Client {
  @JSExport
  def main(container: html.Div) = {
    val catList = ul.render
    val inputBox = input.render
    val outputBox = ul.render
    //val category = if (inputBox.value.isEmpty()) None else Some(inputBox.value)
    //    def update() = Ajaxer[Api].list(inputBox.value).call().foreach { data =>
    //      outputBox.innerHTML = ""
    //      for (FileData(name, size) <- data) {
    //        outputBox.appendChild(
    //          li(
    //            b(name), " - ", size.toString(), " bytes" /*img(src := imgSrc)*/ ).render)
    //      }
    //    }
    def formatPrice(cents: Long): String = {
      val main = (cents / 100).toString()
      val c = (cents % 100).toString()
      main + "." + c + " Euro"
    }

    //    def update() = Ajaxer[Api].products(Some("some category")).call().foreach { data =>
    //      outputBox.innerHTML = ""
    //      for (Product(name, price, imgSrc, cat) <- data) {
    //        outputBox.appendChild(
    //          li(
    //            b(name), " - ", formatPrice(price), br,
    //            "category: " + cat, br,
    //            img(src := imgSrc)).render)
    //      }
    //    }
    //    def updateCategories() = Ajaxer[Api].categories().call().foreach { data =>
    //      outputBox.innerHTML = ""
    //      for (cat <- data) {
    //        outputBox.appendChild(
    //          li(
    //            b(cat)).render)
    //      }
    //    }
    //    inputBox.onkeyup = (e: dom.Event) => update()
    //    update()
    def makeCatList = Ajaxer[Api].categories().call().map { cs =>
      val htmlUl = ul(
        cs.map(li(_)))
      htmlUl.render
    }
    makeCatList.foreach { ls =>
      container.appendChild(ls)
    }
  }
}