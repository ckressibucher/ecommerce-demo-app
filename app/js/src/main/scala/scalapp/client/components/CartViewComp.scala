package scalapp.client.components

import diode.data.Pot
import diode.react.ModelProxy
import diode.react.ReactPot._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, ReactEventAliases}
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.ext.KeyCode

import scala.util.Try
import scalapp.client._
import scalapp.model.{Price, Product, CartView => CartViewModel}

object CartViewComp extends ReactEventAliases {

  case class Props(proxy: ModelProxy[Pot[CartViewModel]], dispatcher: DiodeDispatcher)

  case class MiniCartProps(proxy: ModelProxy[Pot[CartViewModel]], router: RouterCtl[Loc])

  type Qty = Int

  case class QtyProps(qty: Qty, product: Product, dp: DiodeDispatcher)

  class QtyBackend($: BackendScope[QtyProps, Qty]) {

    def handleChange(e: ReactEventI): Callback = {
      Try(e.target.value.toInt).map { v: Int =>
        $.setState(v)
      }.toOption.getOrElse(Callback.empty)
    }

    def fire: Callback = (for {
      props <- $.props
      qty <- $.state
    } yield props.dp(UpdateProductQty(props.product, qty))).flatten

    def handleKeyPress(e: ReactKeyboardEvent): Callback = {
      if (e.which == KeyCode.Enter) fire
      else Callback.empty
    }

    def render(s: Qty) =
      <.div(
        <.input(
          ^.className := "qty",
          ^.`type` := "number",
          ^.value := s,
          ^.onChange ==> handleChange,
          ^.onKeyPress ==> handleKeyPress
        ),
        <.button(
          ^.onClick --> fire,
          ^.className := "update-qty",
          ^.title := "update quantity",
          "↻"
        )
      )
  }

  val QtyComponent = ReactComponentB[QtyProps]("cart-line-qty")
    .initialState_P(props => props.qty)
    .renderBackend[QtyBackend]
    .build

  val Line = ReactComponentB[(CartViewModel.Line, DiodeDispatcher)]("cart-line")
    .render_P {
      case (line, dp) =>
        <.tr(
          <.td(line.p.name.name),
          <.td(QtyComponent(QtyProps(line.qty, line.p, dp))),
          <.td(PriceBox.PriceBox(line.p.price)),
          <.td(line.taxClass),
          <.td(^.className := "price-col", PriceBox.PriceBox(line.price)),
          <.td(
            <.button(
              ^.className := "remove",
              ^.title := "remove",
              ^.onClick --> dp(RemoveProduct(line.p)),
              "(x)")))

    }
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
            cartView.taxes.lines.map {
              case (ln) => TotalsLine.withKey(ln.cls)(ln)
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

  val DiscountForm = ReactComponentB[Props]("discount-form")
    .render_P(p =>
      <.div(^.className := "discount-code",
        <.input(^.`type` := "text",
          ^.defaultValue := "discount-code",
          ^.onKeyPress --> Callback.empty // TODO send on enter
        ),
        <.button(^.onClick --> Callback.empty,
          "Apply discount")
      )
    )
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
                <.th("Unit price"),
                <.th("Tax class"),
                <.th(^.className := "price-col", "Line sum"),
                <.th(^.className := "button-col"))),
            <.tbody(
              cartView.lines.map {
                case (ln: CartViewModel.Line) => Line.withKey(ln.p.name.name)((ln, p.dispatcher))
              },
              cartView.discounts.map {
                case (d: CartViewModel.Discount) => <.tr(
                  <.td("discount: " + d.code),
                  <.td("1"),
                  <.td(PriceBox.PriceBox(d.amount)),
                  <.td(d.taxClass),
                  <.td(PriceBox.PriceBox(d.amount)),
                  <.td(<.button("remove TODO")))
              }
            )),
          CartTotals(cartView),
          DiscountForm(p),
          <.button(^.onClick --> p.dispatcher(ClearCart), "Clear Cart"))
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
            <.span("Cart: "),
            <.button(toCart, s"$totalNumProducts products", " - ", PriceBox.PriceBox(cartView.grandTotal)))
        }),
        p.proxy.value.renderEmpty(<.button(toCart, "To Cart"))
      )
    }
    .componentDidMount(scope => {
      val proxy = scope.props.proxy
      Callback.when(proxy.value.isEmpty)(proxy.dispatch(UpdateCartView()))
    })
    .build

  def apply(proxy: ModelProxy[Pot[CartViewModel]], disp: DiodeDispatcher) =
    CartView(Props(proxy, disp))

  def minicart(proxy: ModelProxy[Pot[CartViewModel]], router: RouterCtl[Loc]) =
    MiniCart(MiniCartProps(proxy, router))
}
