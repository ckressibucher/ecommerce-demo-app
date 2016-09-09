package scalapp.client.modules

import scala.language.existentials

import diode.react.ReactConnectProxy
import japgolly.scalajs.react.ReactComponentB

import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import scalapp.client.CategoryModel
import scalapp.client.Loc
import scalapp.client.ProductModel
import scalapp.model.{ Category, Product }
import scalapp.client.components.CategoryList
import scalapp.client.components.ProductsList
import diode.data.Pot
import scalapp.client.DiodeDispatcher

/** Show the category list and the products of the current category
  */
object CategoryPage {

  case class Props(router: RouterCtl[Loc], currenct: Category,
    catsComp: ReactConnectProxy[CategoryModel],
    prodComp: ReactConnectProxy[Pot[Seq[Product]]],
    dispatcher: DiodeDispatcher)

  val component = ReactComponentB[Props]("CategoryPage")
    .render_P { props =>
      <.section(
        <.header(
          <.h2("Products of category " + props.currenct.name)),
        <.div(^.className := "flex one three-800 four-1000",
          <.aside(^.className := "third-800 fourth-1000",
            props.catsComp(CategoryList(props.router, _))),
          <.main(^.className := "two-third-800 three-fourth-1000",
            props.prodComp(ProductsList(_, props.dispatcher)))))
    }
    .build

}
