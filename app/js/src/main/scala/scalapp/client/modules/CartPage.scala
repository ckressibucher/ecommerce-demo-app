package scalapp.client.modules

import diode.data.Pot
import diode.react.ReactConnectProxy
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.language.existentials
import scalapp.client.{DiodeDispatcher, Loc}
import scalapp.client.components.CartViewComp
import scalapp.model.CartView

object CartPage {

  case class Props(router: RouterCtl[Loc],
    cartComp: ReactConnectProxy[Pot[CartView]],
    dispatcher: DiodeDispatcher)

  val component = ReactComponentB[Props]("cart-page")
    .render_P { props =>
      <.section(
        <.h2("Cart"),
            props.cartComp(CartViewComp(_, props.dispatcher)))
    }
    .build

}
