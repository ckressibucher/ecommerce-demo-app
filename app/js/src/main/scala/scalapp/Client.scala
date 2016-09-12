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

import scalapp.client.modules.Page
import japgolly.scalajs.react.extra.router.StaticDsl.Route
import scalapp.model.CartView

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

  val cartView: ReactConnectProxy[Pot[CartView]] = AppCircuit.connect(_.cartView)

  // the main react components (or connect proxies)
  val components = Page.Components(catsList, productsList, cartView)
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
    val cartRoute = staticToDynamicRoute("#cart", CartLoc)
    val catRoute = dynamicRouteCT(
      ("#cat" / string("[^/]*"))
        .pmap(c => Some(CategoryLoc(Category(c))))((a: CategoryLoc) => a.cat.name))

    // now compose the rules
    (dashboardRoute ~> commonAction | catRoute ~> commonAction | cartRoute ~> commonAction)
      .notFound(redirectToPage(DashboardLoc)(Redirect.Replace))
  }

  @JSExport
  def main() = {
    // create the router
    val router = Router(BaseUrl.until_#, routerConfig)
    // tell React to render the router in the document body
    ReactDOM.render(router(), dom.document.getElementById("contents"))
  }
}
