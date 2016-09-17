package scalapp.client.components

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

import scalapp.model.Price

object PriceBox {
  val PriceBox = ReactComponentB[Price]("price-box")
    .render_P(price => {
      val (main, c) = price.divMod
      <.div(^.className := "price-box",
        <.span(^.className := "price-value", main + "." + "%02d".format(c)),
        " ",
        <.span(^.className := "price-currency", "Euro"))
    })
    .build
}
