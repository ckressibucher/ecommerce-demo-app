package scalapp.client.modules

import japgolly.scalajs.react.extra.router.RouterCtl
import scalapp.Client.Loc
import diode.react.ModelProxy
import scalapp.client.CategoryModel
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import diode.react.ReactConnectProxy
import scalapp.client.components.CategoryList

object Dashboard {

  case class Props(router: RouterCtl[Loc], catsComponent: ReactConnectProxy[CategoryModel])

  val component = ReactComponentB[Props]("Dashboard")
    .render_P { props =>
      <.div("TODO: render dashboard",
        props.catsComponent(CategoryList(_)))
    }
    .build

  def apply(router: RouterCtl[Loc], catsComponent: ReactConnectProxy[CategoryModel]) =
    component(Props(router, catsComponent))
}
