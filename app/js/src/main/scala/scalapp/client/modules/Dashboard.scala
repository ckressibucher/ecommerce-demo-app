package scalapp.client.modules

import japgolly.scalajs.react.extra.router.RouterCtl
import scalapp.client.Loc
import diode.react.ModelProxy
import scalapp.client.CategoryModel
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import diode.react.ReactConnectProxy
import scalapp.client.components.CategoryList
import scala.language.existentials
import diode.data.Pot
import scalapp.model.Product
import scalapp.client.components.ProductsList
import scalapp.client.DiodeDispatcher
import scalapp.client.DiodeDispatcher

object Dashboard {

  case class Props(router: RouterCtl[Loc],
    catsComponent: ReactConnectProxy[CategoryModel],
    prodComponent: ReactConnectProxy[Pot[Seq[Product]]],
    dispatcher: DiodeDispatcher)

  val component = ReactComponentB[Props]("Dashboard")
    .render_P { props =>
      <.section(
        <.header(
          <.h2("Welcome to the xyz online shop!")),
        <.div(^.className := "flex one three-800 four-1000",
          <.aside(^.className := "third-800 fourth-1000",
            props.catsComponent(CategoryList(props.router, _))),
          <.main(^.className := "two-third-800 three-fourth-1000",
            props.prodComponent(ProductsList(_, props.dispatcher)))))
    }
    .build

  def apply(router: RouterCtl[Loc], catsComponent: ReactConnectProxy[CategoryModel],
    productsC: ReactConnectProxy[Pot[Seq[Product]]], disp: DiodeDispatcher) =
    component(Props(router, catsComponent, productsC, disp))
}
