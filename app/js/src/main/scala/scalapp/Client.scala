package scalapp

import diode.ModelR
import diode.data.Pot
import diode.react.ReactConnectProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.StaticDsl.Route
import japgolly.scalajs.react.extra.router.{BaseUrl, Redirect, Router, RouterConfig, RouterConfigDsl, RouterCtl}
import org.scalajs.dom

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scalapp.client.{AppCircuit, CategoryModel, _}
import scalapp.client.modules.Page
import scalapp.model.{CartView, Category, Product}

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

  AppCircuit.subscribe(AppCircuit.zoom(identity)) { model: ModelR[AppModel, AppModel] =>
    println("debug: updated model:")
    println(model.value.cartView)
  }

  AppCircuit.dispatch(InitializeApp)

  // configure the router
  val routerConfig: RouterConfig[Loc] = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    def reactRouterRenderer(page: Loc, r: RouterCtl[Loc]) =
      Page.render(AppCircuit.zoom(identity), page, components, dispatch, r)

    def commonAction = dynRenderR(reactRouterRenderer)

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
