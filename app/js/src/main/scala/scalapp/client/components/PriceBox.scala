package scalapp.client.components

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

import scalapp.model.Price

object PriceBox {
  val PriceBox = ReactComponentB[Price]("price-box")
    .render_P(price => {
      val prefix = if (price.cents < 0) "-" else ""
      val (main, c) = price.divModAbs
      <.div(^.className := "price-box",
        <.span(^.className := "price-value", s"$prefix$main.${"%02d".format(c)}"),
        " ",
        <.span(^.className := "price-currency", "Euro"))
    })
    .build
}
