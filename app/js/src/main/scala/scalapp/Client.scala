package scalapp

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

import org.scalajs.dom

import autowire._
import diode.react.ReactConnectProxy
import diode.react.ReactPot._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.{ BaseUrl, Redirect, Resolution, Router, RouterConfigDsl, RouterCtl }
import japgolly.scalajs.react.vdom.prefix_<^._
import scalapp.client.AppCircuit
import scalapp.client.CategoryModel
import scalapp.client.modules.Dashboard
import scalapp.model.{ Category, Product }
import japgolly.scalajs.react.extra.router.StaticDsl.RouteB
import diode.ModelR
import diode.data.Pot
import japgolly.scalajs.react.extra.router.RouterConfig
import scalapp.client._
import scalapp.client.DashboardLoc

import scalapp.client.modules.Page
import japgolly.scalajs.react.extra.router.StaticDsl.Route

@JSExport
object Client extends js.JSApp {

  // react component for a list of categories
  val catsList: ReactConnectProxy[CategoryModel] = AppCircuit.connect(_.categories)

  // read model for:
  //  - the products of the selected category, or:
  //  - all products if no category is selected
  val selectedProducts: ModelR[_, Pot[Seq[Product]]] = AppCircuit.zoom(appM =>
    appM.products.all map { products =>
      appM.categories.cur match {
        case Some(c) => products.filter(_.cat == c)
        case None    => products
      }
    })

  // the react component for a list of products (using the read model above)
  val productsList: ReactConnectProxy[Pot[Seq[Product]]] = AppCircuit.connect(selectedProducts)

  // the main react components (or connect proxies)
  val components = Page.Components(catsList, productsList)
  val dispatch = (a: diode.Action) => Callback { AppCircuit.dispatch(a) }

  // configure the router
  val routerConfig: RouterConfig[Loc] = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    def reactRouterRenderer(page: Loc, r: RouterCtl[Loc]) =
      Page.render(AppCircuit.zoom(identity _), page, components, dispatch, r)

    def commonAction = dynRenderR(reactRouterRenderer _)

    // wrap/connect components to the circuit
    def staticToDynamicRoute(r: Route[Unit], page: Loc) = {
      dynamicRoute(r const page) { case p if page == p => p }
    }
    val dashboardRoute = staticToDynamicRoute(root, DashboardLoc)
    val catRoute = dynamicRouteCT(
      ("#cat" / string("[^/]*"))
        .pmap(c => Some(CategoryLoc(Category(c))))((a: CategoryLoc) => a.cat.name))

    // now compose the rules
    (dashboardRoute ~> commonAction | catRoute ~> commonAction)
      .notFound(redirectToPage(DashboardLoc)(Redirect.Replace))
  }

  @JSExport
  def main() = {
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
