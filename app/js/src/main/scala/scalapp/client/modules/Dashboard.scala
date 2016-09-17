package scalapp.client.modules

import diode.data.Pot
import diode.react.ReactConnectProxy
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.language.existentials
import scalapp.client.{CategoryModel, DiodeDispatcher, Loc}
import scalapp.client.components.{CategoryList, ProductsList}
import scalapp.model.Product

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
