package scalapp.client.components

import diode.data.Pot
import diode.react.ModelProxy
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.ReactNodeFrag
import japgolly.scalajs.react.{Callback, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._

import scalapp.client.{DiodeDispatcher, Loc, UpdateCartView}
import scalapp.model.{Price, CartView => CartViewModel}

object CartViewComp {

  case class Props(proxy: ModelProxy[Pot[CartViewModel]], dispatcher: DiodeDispatcher)

  case class MiniCartProps(proxy: ModelProxy[Pot[CartViewModel]], router: RouterCtl[Loc])

  val Line = ReactComponentB[CartViewModel.Line]("cart-line")
    .render_P(line =>
      <.tr(
        <.td(line.p.name.name),
        <.td(line.qty),
        <.td(line.taxClass),
        <.td(^.className := "price-col", PriceBox.PriceBox(line.price))))
    .build

  val TotalsLine = ReactComponentB[CartViewModel.TaxLine]("cart-totals-line")
    .render_P(line => <.tr(
      <.td(line.cls), <.td(line.rate), <.td(PriceBox.PriceBox(line.sum))
    ))
    .build

  val CartTotals = ReactComponentB[CartViewModel]("cart-totals")
    .render_P((cartView: CartViewModel) => {
      val net = Price(cartView.grandTotal.cents - cartView.taxes.total.cents)
      <.div(^.className := "cart-totals",
        <.h3("Totals"),
        <.table(
          <.tbody(
            <.tr(
              <.td("Net total:"), <.td(^.colSpan := 2, PriceBox.PriceBox(net))
            ),
            cartView.taxes.lines.zipWithIndex.map {
              case (ln, idx) => TotalsLine.withKey(idx)(ln)
            }
          ),
          <.tfoot(
            <.tr(
            <.td("Total: "), <.td(^.colSpan := 2, PriceBox.PriceBox(cartView.grandTotal))
            )
          )
        )
      )
    })
    .build

  def renderCart(p: Props) = {
    p.proxy.value.render(cartView => {
      if (cartView.lines.isEmpty) {
        <.p("Your cart is empty.")
      } else {
        <.div(
          <.h2("Your cart"),
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
              })),
          CartTotals(cartView))
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

  val MiniCart = ReactComponentB[MiniCartProps]("mini-cart")
    .render_P { p =>
      val toCart = p.router.setOnClick(scalapp.client.CartLoc)
      <.div(
        ^.className := "mini-cart",
        p.proxy.value.render(cartView => {
          val totalNumProducts = cartView.lines.map(_.qty).sum
          <.div(
            <.span(toCart, "Cart: "),
            <.button(s"$totalNumProducts products"), " - ", PriceBox.PriceBox(cartView.grandTotal))
        }),
        p.proxy.value.renderEmpty(<.button(toCart, "To Cart"))
      )
    }
    .build

  def apply(proxy: ModelProxy[Pot[CartViewModel]], disp: DiodeDispatcher) =
    CartView(Props(proxy, disp))

  def minicart(proxy: ModelProxy[Pot[CartViewModel]], router: RouterCtl[Loc]) =
    MiniCart(MiniCartProps(proxy, router))
}
