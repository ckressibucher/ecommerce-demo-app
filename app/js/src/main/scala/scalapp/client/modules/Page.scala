package scalapp.client.modules

import scala.language.existentials
import scala.scalajs.js

import diode.Action
import diode.ModelR
import diode.data.Pot
import diode.react.ReactConnectProxy
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.ReactElement
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import scalapp.client._

import scalapp.client.AppModel
import scalapp.client.CategoryModel
import scalapp.model.Category
import scalapp.model.Product
import scalapp.client.ReactHelper._
import diode.Dispatcher
import scalapp.model.CartView

/** The main entry point for the "view"
  */
object Page {

  type Dispatcher = (Action) => Callback

  type PageView = PageProps => ReactElement

  case class PageProps(rootModel: ModelR[_, AppModel], page: Loc, components: Components, dispatcher: Dispatcher, router: RouterCtl[Loc])

  case class Components(catList: ReactConnectProxy[CategoryModel],
    products: ReactConnectProxy[Pot[Seq[Product]]],
    cartView: ReactConnectProxy[Pot[CartView]])

  val year = (new js.Date()).getFullYear()

  case class HeaderProps(page: Loc, router: RouterCtl[Loc], dp: Dispatcher)

  object HeaderProps {
    def apply(allProps: PageProps) = new HeaderProps(allProps.page, allProps.router, allProps.dispatcher)
  }

  val Header = ReactComponentB[HeaderProps]("header")
    .render_P(props =>
      <.header(
        <.nav(^.id := "main-nav",
          <.a(^.href := "#", ^.className := "brand",
            ^.onClick ==> addToRouterCallback(props.router.setEH(DashboardLoc), props.dp(ResetCategory)),
            <.img(^.className := "logo", ^.src := "/images/logo.png"),
            <.span("XYZ - Online Shop")),
          <.input(^.id := "bmenub", ^.`type` := "checkbox", ^.className := "show"),
          <.label(^.`for` := "bmenub", ^.className := "burger pseudo button", "menu"),

          <.div(^.className := "menu",
            props.router.link(CartLoc)("To Cart"), // TODO show some main cart data here...
            <.a("Newest products"))),
        <.div(^.className := "spacer-afternav", " "),
        <.div(^.className := "breadcrumb", props.page.toString())))
    .build

  val Footer = ReactComponentB[Unit]("footer")
    .render(_ =>
      <.footer(
        <.hr(), "(c) " + year + " xzy"))
    .build

  val DashboardView: PageView = (props) => {
    <.section(
      Header(HeaderProps(props)),
      Dashboard(props.router, props.components.catList, props.components.products, props.dispatcher),
      Footer())
  }

  val CartView: PageView = (props) => {
    <.section(
      Header(HeaderProps(props)),
      CartPage.component(CartPage.Props(props.router, props.components.cartView, props.dispatcher)),
      Footer())
  }

  def categoryView(cur: Category): PageView = (props) => {
    val catProps = CategoryPage.Props(props.router, cur,
      props.components.catList,
      props.components.products,
      props.dispatcher)
    <.section(
      Header(HeaderProps(props)),
      CategoryPage.component(catProps),
      Footer())
  }

  val DefaultLayout = ReactComponentB[PageProps]("main-page-component")
    .render_P(p => p.page match {
      case DashboardLoc   => DashboardView(p)
      case CategoryLoc(c) => categoryView(c)(p)
      case CartLoc        => CartView(p)
      case unknown        => <.div("no page definition for this " + (unknown.toString()))
    })
    .build

  def render(model: ModelR[_, AppModel], page: Loc, c: Components, dp: Dispatcher, r: RouterCtl[Loc]) =
    DefaultLayout(PageProps(model, page, c, dp, r))

}
