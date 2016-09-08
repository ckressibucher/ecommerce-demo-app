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

object Dashboard {

  case class Props(router: RouterCtl[Loc],
    catsComponent: ReactConnectProxy[CategoryModel],
    prodComponent: ReactConnectProxy[Pot[Seq[Product]]])

  val component = ReactComponentB[Props]("Dashboard")
    .render_P { props =>

      <.section(
        <.header(
          <.h2("Welcome to the xyz online shop!")),
        <.aside(props.catsComponent(CategoryList(props.router, _))),
        <.main(props.prodComponent(ProductsList(_))))
    }
    .build

  def apply(router: RouterCtl[Loc], catsComponent: ReactConnectProxy[CategoryModel],
    productsC: ReactConnectProxy[Pot[Seq[Product]]]) =
    component(Props(router, catsComponent, productsC))
}
