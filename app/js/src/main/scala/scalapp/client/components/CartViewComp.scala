package scalapp.client.components

import diode.react.ModelProxy

import diode.data.Pot
import scalapp.model.{ CartView => CartViewModel, Product }
import scalapp.client.UpdateProducts
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.ReactNode
import diode.react.ReactPot._
import scalapp.model.`package`.Price
import diode.Action
import scalapp.client.AddProduct
import scalapp.client.DiodeDispatcher
import scalapp.client.UpdateCartView

object CartViewComp {

  case class Props(proxy: ModelProxy[Pot[CartViewModel]], dispatcher: DiodeDispatcher)

  val Line = ReactComponentB[CartViewModel.Line]("cart-line")
    .render_P(line =>
      <.tr(
        <.td(line.p.name.name),
        <.td(line.qty),
        <.td(line.taxClass),
        <.td(line.price))) // TODO format price
    .build

  def renderCart(p: Props): ReactNode = {
    p.proxy.value.render(cartView => {
      <.table(^.className := "cart-view",
        <.thead(
          <.tr(
            <.th("Product"),
            <.th("Quantity"),
            <.th("Tax class"),
            <.th("Line sum"))),
        <.tbody(
          cartView.lines.zipWithIndex.map {
            case (ln: CartViewModel.Line, idx: Int) => Line.withKey(idx)(ln)
          }))
    })
  }

  val CartView = ReactComponentB[Props]("cart-view")
    .render_P { p =>
      <.div(
        <.h2("Cart"),
        renderCart(p))
    }
    .componentDidMount(scope => {
      val proxy = scope.props.proxy
      Callback.when(proxy.value.isEmpty)(proxy.dispatch(UpdateCartView()))
    })
    .build

  def apply(proxy: ModelProxy[Pot[CartViewModel]], disp: DiodeDispatcher) = CartView(Props(proxy, disp))
}
