package scalapp.client.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import diode.react._
import diode.react.ReactPot._
import scalapp.client._
import scalapp.model._

object CategoryList {

  val CategoryItem = ReactComponentB[Category]("Category")
    .render_P { x => <.p(x.name) }
    .build

  val CategoryList = ReactComponentB[ModelProxy[CategoryModel]]("Categories")
    .render_P { proxy =>
      <.div(
        <.h2("Categories"),
        proxy().cats.render(cs => cs.zipWithIndex.map((ci: (Category, Int)) => CategoryItem.withKey(ci._2)(ci._1))))
    }
    .componentDidMount(scope =>
      // sending an `updateCategories` action with empty categories triggers an ajax request
      Callback.when(scope.props.value.cats.isEmpty)(scope.props.dispatch(UpdateCategories())))
    .build

  def apply(proxy: ModelProxy[CategoryModel]) = CategoryList(proxy)
}
