package scalapp.client.modules

import diode.data.Pot
import diode.react.ReactConnectProxy
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.language.existentials
import scalapp.client.{CategoryModel, DiodeDispatcher, Loc}
import scalapp.client.components.{CategoryList, ProductsList}
import scalapp.model.{Category, Product}

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
