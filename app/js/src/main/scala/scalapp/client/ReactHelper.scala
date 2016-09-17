package scalapp.client

import japgolly.scalajs.react.{Callback, SyntheticEvent}

object ReactHelper {
  /** Composes a router callback (which needs an input event) with another callback.
    * The router callback is run first.
    */
  def addToRouterCallback[E <: SyntheticEvent[_]](routerCb: E => Callback, otherCb: Callback): E => Callback = {
    routerCb.andThen { _ >> otherCb }
  }

  /** Composes a normal callback with a router callback
    */
  def addRouterCallbackToAction[E <: SyntheticEvent[_]](otherCb: Callback, routerCb: E => Callback): E => Callback = {
    routerCb.andThen { otherCb >> _ }
  }

}
