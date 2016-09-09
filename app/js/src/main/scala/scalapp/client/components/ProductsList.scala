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

object ProductsList {

  case class Props(proxy: ModelProxy[Pot[Seq[Product]]])

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

  val ProductBox = ReactComponentB[Product]("product-box")
    .render_P(p => <.div(
      ^.className := "card",
      <.header(<.h3(p.name.name)),
      <.img(^.src := p.img.url),
      <.p("Category: " + p.cat.name),
      PriceBox(p.price),
      <.footer(<.button("Add to cart"))))
    .build

  def renderProducts(proxy: ModelProxy[Pot[Seq[Product]]]): ReactNode = {
    proxy.value.render(ps => {
      <.div(^.className := "product-list one flex two-800 three-1000 four-1200",
        ps.map { p: Product => ProductBox.withKey(p.name.name)(p) })
    })
  }

  val ProductList = ReactComponentB[Props]("Products")
    .render_P { p =>
      <.div(
        <.h2("Products"),
        renderProducts(p.proxy))
    }
    .componentDidMount(scope => {
      val proxy = scope.props.proxy
      Callback.when(proxy.value.isEmpty)(proxy.dispatch(UpdateProducts()))
    })
    .build

  def apply(proxy: ModelProxy[Pot[Seq[Product]]]) = ProductList(Props(proxy))
}
