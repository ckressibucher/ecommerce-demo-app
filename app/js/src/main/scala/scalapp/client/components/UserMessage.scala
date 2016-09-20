package scalapp.client.components

import japgolly.scalajs.react.{BackendScope, ReactComponentB, vdom}
import japgolly.scalajs.react.vdom.prefix_<^._

import scalapp.client.DiodeDispatcher

object UserMessage {

  object Level extends Enumeration {
    val Error, Warn, Success = Value

    def cssClass(level: Value) = level match {
      case Error => "error"
      case Warn => "warn"
      case Success => "success"
    }
  }

  case class Props(msg: String, level: Level.Value)

  class Backend($: BackendScope[Props, Boolean]) {

    def closeAction = $.setState(false)

    def render(p: Props, show: Boolean) = {
      if (show)
        <.div(^.className := s"message ${Level.cssClass(p.level)}",
          <.button(^.onClick --> closeAction, "close"),
          <.div(p.msg))
      else
      // only hide element... the component will be
      // removed when the state of the parent changes,
      // e.g. the state is consistent again
        <.span(^.className := "no-display")
    }
  }

  val UserMessage = ReactComponentB[Props]("user-message")
    .initialState(true)
    .renderBackend[Backend]
    .build

  def apply(msg: String, level: Level.Value) = UserMessage(Props(msg, level))
}
