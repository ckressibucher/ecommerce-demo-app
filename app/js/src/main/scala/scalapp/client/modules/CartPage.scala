package scalapp.client.modules

import scala.language.existentials

import diode.react.ReactConnectProxy
import japgolly.scalajs.react.ReactComponentB

import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import scalapp.client.CategoryModel
import scalapp.client.Loc
import scalapp.client.ProductModel
import scalapp.model.{ Category, Product }
import scalapp.client.components.CategoryList
import scalapp.client.components.ProductsList
import diode.data.Pot
import scalapp.client.DiodeDispatcher
import scalapp.model.CartView
import scalapp.client.components.CartViewComp

object CartPage {

  case class Props(router: RouterCtl[Loc],
    cartComp: ReactConnectProxy[Pot[CartView]],
    dispatcher: DiodeDispatcher)

  val component = ReactComponentB[Props]("CategoryPage")
    .render_P { props =>
      <.section(
        <.header(
          <.h2("the current cart state")),
        <.div(
          <.main(^.className := "",
            props.cartComp(CartViewComp(_, props.dispatcher)))))
    }
    .build

}
