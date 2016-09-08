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

/** The main entry point for the "view"
  */
object Page {

  type Dispatcher = (Action) => Callback

  type PageView = PageProps => ReactElement

  case class PageProps(rootModel: ModelR[_, AppModel], page: Loc, components: Components, dp: Dispatcher, router: RouterCtl[Loc])

  case class Components(catList: ReactConnectProxy[CategoryModel], products: ReactConnectProxy[Pot[Seq[Product]]])

  val year = (new js.Date()).getFullYear()

  val Header = ReactComponentB[Loc]("header")
    .render_P(loc =>
      <.header(
        <.nav(^.id := "main-nav",
          <.a(^.href := "#", ^.className := "brand",
            <.img(^.className := "logo", ^.src := "/images/logo.png"),
            <.span("XYZ - Online Shop")),
          <.input(^.id := "bmenub", ^.`type` := "checkbox", ^.className := "show"),
          <.label(^.`for` := "bmenub", ^.className := "burger pseudo button", "menu"),

          <.div(^.className := "menu",
            <.a("To Cart (x articlesTODO)"),
            <.a("Newest products"))),
        <.div(^.className := "spacer-afternav", " "),
        <.div(^.className := "breadcrumb", loc.toString())))
    .build

  val Footer = ReactComponentB[Unit]("footer")
    .render(_ =>
      <.footer(
        <.hr(), "(c) " + year + " xzy"))
    .build

  val DashboardView: PageView = (props) => {
    <.section(
      Header(props.page),
      Dashboard(props.router, props.components.catList, props.components.products),
      Footer())
  }

  def categoryView(cur: Category): PageView = (props) => {
    val catProps = CategoryPage.Props(props.router, cur,
      props.components.catList,
      props.components.products)
    <.section(
      Header(props.page),
      CategoryPage.component(catProps),
      Footer())
  }

  val DefaultLayout = ReactComponentB[PageProps]("main-page-component")
    .render_P(p => p.page match {
      case DashboardLoc   => DashboardView(p)
      case CategoryLoc(c) => categoryView(c)(p)
      case unknown        => <.div("no page definition for this " + (unknown.toString()))
    })
    .build

  def render(model: ModelR[_, AppModel], page: Loc, c: Components, dp: Dispatcher, r: RouterCtl[Loc]) =
    DefaultLayout(PageProps(model, page, c, dp, r))
}
