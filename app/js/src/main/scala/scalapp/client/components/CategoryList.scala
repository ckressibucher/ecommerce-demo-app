package scalapp.client.components

import diode.react.ReactPot._
import diode.react._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._

import scalapp.client.ReactHelper._
import scalapp.client.{CategoryLoc, DashboardLoc, Loc, _}
import scalapp.model._

object CategoryList {

  case class Props(r: RouterCtl[Loc], proxy: ModelProxy[CategoryModel])

  case class ItemProps(c: Category, isActive: Boolean, onClick: TagMod)

  val CategoryItem = ReactComponentB[ItemProps]("Category")
    .render_P { props =>
      <.li(props.isActive ?= (^.className := "active"), props.onClick,
        props.c.name)
    }
    .build

  def renderCat(cat: Category, current: Option[Category], onClick: TagMod): ReactNode = {
    val props = ItemProps(cat, current.exists(_ == cat), onClick)
    CategoryItem.withKey(cat.name)(props)
  }

  def renderCategories(r: RouterCtl[Loc], proxy: ModelProxy[CategoryModel]) = {
    val cats = proxy.value.cats
    val cur = proxy.value.cur
    def onClickMod(c: Category) = addToRouterCallback(r.setEH(CategoryLoc(c)), proxy.dispatch(SelectCategory(c)))
    def onClickTagMod(c: Category) = ^.onClick ==> onClickMod(c)
    r.link(DashboardLoc)
    cats.render(cs =>
      <.ul(
        cs.map(c => renderCat(c, cur, onClickTagMod(c)))))
  }

  val CategoryList = ReactComponentB[Props]("Categories")
    .render_P { p =>
      <.div(
        <.h2("Categories"),
        <.div(^.className := "category-list",
          renderCategories(p.r, p.proxy)))
    }
    .componentDidMount(scope => {
      // sending an `updateCategories` action with empty categories triggers an ajax request
      val proxy = scope.props.proxy
      Callback.when(proxy.value.cats.isEmpty)(proxy.dispatch(UpdateCategories()))
    })
    .build

  def apply(r: RouterCtl[Loc], proxy: ModelProxy[CategoryModel]) =
    CategoryList(Props(r, proxy))
}
