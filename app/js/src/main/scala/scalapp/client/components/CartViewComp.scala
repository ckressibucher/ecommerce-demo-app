package scalapp.client.components

import diode.data.Pot
import diode.react.ModelProxy
import diode.react.ReactPot._
import japgolly.scalajs.react.{Callback, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._

import scalapp.client.{DiodeDispatcher, UpdateCartView}
import scalapp.model.{CartView => CartViewModel}

object CartViewComp {

  case class Props(proxy: ModelProxy[Pot[CartViewModel]], dispatcher: DiodeDispatcher)

  val Line = ReactComponentB[CartViewModel.Line]("cart-line")
    .render_P(line =>
      <.tr(
        <.td(line.p.name.name),
        <.td(line.qty),
        <.td(line.taxClass),
        <.td(^.className := "price-col", PriceBox.PriceBox(line.price))))
    .build

  def renderCart(p: Props) = {
    p.proxy.value.render(cartView => {
      if (cartView.lines.isEmpty) {
        <.p("Your cart is empty.")
      } else {
        <.h2("Your cart")
        <.table(^.className := "cart-view",
          <.thead(
            <.tr(
              <.th("Product"),
              <.th("Quantity"),
              <.th("Tax class"),
              <.th(^.className := "price-col", "Line sum"))),
          <.tbody(
            cartView.lines.zipWithIndex.map {
              case (ln: CartViewModel.Line, idx: Int) => Line.withKey(idx)(ln)
            }))
      }
    })
  }

  val CartView = ReactComponentB[Props]("cart-view")
    .render_P { p =>
      <.div(^.className := "cart-view",
        renderCart(p))
    }
    .componentDidMount(scope => {
      val proxy = scope.props.proxy
      Callback.when(proxy.value.isEmpty)(proxy.dispatch(UpdateCartView()))
    })
    .build

  def apply(proxy: ModelProxy[Pot[CartViewModel]], disp: DiodeDispatcher) = CartView(Props(proxy, disp))
}
