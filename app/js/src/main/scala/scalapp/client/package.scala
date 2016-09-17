package scalapp

import diode.Action
import japgolly.scalajs.react.Callback

import scalapp.model._

package object client {

  // Pages
  sealed trait Loc
  case object DashboardLoc extends Loc
  case object CartLoc extends Loc
  case class CategoryLoc(cat: Category) extends Loc

  type DiodeDispatcher = (Action) => Callback
}
