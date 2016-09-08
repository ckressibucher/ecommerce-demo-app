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

object ProductsList {

  case class Props(proxy: ModelProxy[Pot[Seq[Product]]])

  def renderProducts(proxy: ModelProxy[Pot[Seq[Product]]]): ReactNode = {
    proxy.value.render(ps => ps.map { p: Product => <.div(^.key := p.name.name, p.name.name) })
  }

  val ProductList = ReactComponentB[Props]("Products")
    .render_P { p =>
      <.div(
        <.h2("Products"),
        <.div(^.className := "product-list"), renderProducts(p.proxy))
    }
    .componentDidMount(scope => {
      val proxy = scope.props.proxy
      Callback.when(proxy.value.isEmpty)(proxy.dispatch(UpdateProducts()))
    })
    .build

  def apply(proxy: ModelProxy[Pot[Seq[Product]]]) = ProductList(Props(proxy))
}
