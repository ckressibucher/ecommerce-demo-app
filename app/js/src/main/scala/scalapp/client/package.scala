package scalapp

import scalapp.model._
import diode.Action
import japgolly.scalajs.react.Callback

package object client {

  // Pages
  sealed trait Loc
  case object DashboardLoc extends Loc
  case object CartLoc extends Loc
  case class CategoryLoc(cat: Category) extends Loc

  type DiodeDispatcher = (Action) => Callback
}
