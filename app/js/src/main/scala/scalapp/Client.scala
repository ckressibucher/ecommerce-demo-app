package scalapp

import scalatags.JsDom.all._
import org.scalajs.dom
import dom.html
import scalajs.js.annotation.JSExport
import autowire._
import scalapp.model._
import upickle.{ default => upick }
import org.scalajs.dom.raw.HTMLUListElement
import scala.concurrent.Future
import scalapp.client.AjaxService
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.ReactComponentB
import diode.react.{ ModelProxy, ReactConnectProxy }
import diode.react.ReactPot._
import scalapp.client.CategoryModel
import scala.language.postfixOps
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import scalapp.client.AppCircuit
import scalapp.client.components.CategoryList
import japgolly.scalajs.react.extra.router.RouterConfigDsl
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.extra.router.Resolution
import scalapp.client.modules.Dashboard
import scalapp.model.Category
import japgolly.scalajs.react.extra.router.Redirect
import japgolly.scalajs.react.extra.router.Router
import japgolly.scalajs.react.extra.router.BaseUrl

@JSExport
object Client extends scalajs.js.JSApp {

  // Define the locations (pages) used in this application
  sealed trait Loc

  case object DashboardLoc extends Loc

  case class CategoryLoc(cat: Category) extends Loc

  case object CartLoc extends Loc

  // configure the router
  val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    val catsList: ReactConnectProxy[CategoryModel] = AppCircuit.connect(_.categories)

    // wrap/connect components to the circuit
    val homeRoute = staticRoute(root, DashboardLoc)
    def homeRule = renderR { ctl => AppCircuit.wrap(_.categories)(p => Dashboard(ctl, catsList)) }

    // now compose the rules
    (homeRoute ~> homeRule).notFound(redirectToPage(DashboardLoc)(Redirect.Replace))

    //    (staticRoute(root, DashboardLoc) ~> renderR(ctl => SPACircuit.wrap(_.motd)(proxy => Dashboard(ctl, proxy)))
    //      | staticRoute("#todo", TodoLoc) ~> renderR(ctl => todoWrapper(Todo(_)))).notFound(redirectToPage(DashboardLoc)(Redirect.Replace))
  }.renderWith(layout _)

  // base layout for all pages
  def layout(c: RouterCtl[Loc], r: Resolution[Loc]) = {
    <.div(
      // here we use plain Bootstrap class names as these are specific to the top level layout defined here
      <.nav(
        <.a(^.href := "#", ^.className := "brand",
          <.img(^.className := "logo", ^.src := "/images/logo.png"),
          <.span("XYZ - Online Shop")),
        <.input(^.id := "bmenub", ^.`type` := "checkbox", ^.className := "show"),
        <.label(^.`for` := "bmenub", ^.className := "burger pseudo button", "menu"),

        <.div(^.className := "menu",
          <.a("To Cart (x articles|TODO)"),
          <.a("Newest products"))),
      // -- END nav
      // currently active page is shown in this container
      <.div(^.className := "container", r.render()))
  }

  @JSExport
  def main() = {
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

    //    case class Props(mproxy: ModelProxy[CategoryModel])
    //    val reactEl = AppCircuit.wrap(_.categories) { proxy =>
    //      ReactComponentB[Props]("CategoryTree")
    //        .render_P { props =>
    //          <.div(
    //            <.h2("Categories"),
    //            // render messages depending on the state of the Pot
    //            props.mproxy().cats.renderPending(_ > 500, _ => <.p("Loading...")),
    //            props.mproxy().cats.renderFailed(ex => <.p("Failed to load")),
    //            props.mproxy().cats.render(m => <.ul(m.map { s: Category => <.li(s.toString()) })),
    //            <.hr)
    //        }
    //        .build
    //
    //    }
    // create the router
    val router = Router(BaseUrl.until_#, routerConfig)
    // tell React to render the router in the document body
    ReactDOM.render(router(), dom.document.getElementById("contents"))
  }
}
