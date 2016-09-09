package scalapp.client.components

import diode.react.ModelProxy

import diode.data.Pot
import scalapp.model.{ Product, Category }
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
import scalapp.client.DiodeDispatcher

object ProductsList {

  case class Props(proxy: ModelProxy[Pot[Seq[Product]]], dispatcher: DiodeDispatcher)

  case class ProductBoxProps(product: Product, dispatcher: Action => Callback)

  val PriceBox = ReactComponentB[Price]("price-box")
    .render_P(price => {
      val main = (price.cents / 100).toString()
      val c = (price.cents % 100).toString()
      <.div(
        <.span(^.className := "price-value", main + "." + c),
        " ",
        <.span(^.className := "price-currency", "Euro"))
    })
    .build

  val ProductBox = ReactComponentB[ProductBoxProps]("product-box")
    .render_P(props => {
      val p = props.product
      val dispatcher = props.dispatcher
      <.div(
        ^.className := "card",
        <.header(<.h3(p.name.name)),
        <.img(^.src := p.img.url),
        <.p("Category: " + p.cat.name),
        PriceBox(p.price),
        <.footer(
          <.button(^.onClick --> dispatcher(AddProduct(p, 1)), "Add to cart")))
    })
    .build

  def renderProducts(props: Props): ReactNode = {
    props.proxy.value.render(ps => {
      <.div(^.className := "product-list one flex two-800 three-1000 four-1200",
        ps.map { p: Product => ProductBox.withKey(p.name.name)(ProductBoxProps(p, props.dispatcher)) })
    })
  }

  val ProductList = ReactComponentB[Props]("Products")
    .render_P { p =>
      <.div(
        <.h2("Products"),
        renderProducts(p))
    }
    .componentDidMount(scope => {
      val proxy = scope.props.proxy
      Callback.when(proxy.value.isEmpty)(proxy.dispatch(UpdateProducts()))
    })
    .build

  def apply(proxy: ModelProxy[Pot[Seq[Product]]], disp: DiodeDispatcher) = ProductList(Props(proxy, disp))
}
