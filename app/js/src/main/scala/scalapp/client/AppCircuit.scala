package scalapp.client

import scalapp.client.{ AppModel => RootModel }
import diode.Circuit
import diode.react.{ ReactConnector, ReactConnectProxy }
import diode._
import diode.data.Pot
import diode.data.PotState._
import scalapp.model.Category
import autowire._
import scalapp.Api
import diode.data.PotAction
import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import scalajs.js
import scalapp.Api
import scalapp.model.{ Product }
import scalapp.model.CartData
import diode.data.AsyncAction
import scalapp.model.ResultStatus
import scalapp.model.ResultStatus

object AppCircuit extends Circuit[AppModel] with ReactConnector[AppModel] {

  override protected def initialModel = AppModel(CategoryModel(Pot.empty, None),
    ProductModel(Pot.empty),
    CartData.empty)

  override protected val actionHandler = composeHandlers(
    new CategoryHandler(zoomRW(_.categories)((m, v) => m.copy(categories = v))),
    new ProductHandler(zoomRW(_.products)((m, v) => m.copy(products = v))
      .zoomRW(_.all)((m, v) => m.copy(all = v))),
    new CartHandler(zoomRW(_.cart)((m, v) => m.copy(cart = v))))

}

class CartHandler[M](modelRW: ModelRW[M, CartData]) extends ActionHandler(modelRW) {

  val console = js.Dynamic.global.console

  def ignoreResult(effect: => Future[ResultStatus]): Effect =
    Effect[Action](effect.map(_ => NoAction))

  // TODO handel ajax results
  override def handle = {
    case AddProduct(product: Product, qty: Int) => {
      console.log("add product " + product.name + ": " + qty)
      val effect = ignoreResult(AjaxService[Api].addToCart("session", product.name.name, qty).call())
      updated(value.addProduct(product, qty), effect)
    }
    case RemoveProduct(product: Product) => {
      console.log("remove product " + product.name)
      val effect = ignoreResult(AjaxService[Api].deleteFromCart("session", product.name.name).call())
      updated(value.deleteProduct(product), effect)
    }
    case UpdateProductQty(product: Product, qty: Int) => {
      console.log("update qty of product " + product.name + " to " + qty)
      val oldQty = value.qtyByProduct(product)
      val diffQty = qty - oldQty
      val effect = ignoreResult(AjaxService[Api].addToCart("session", product.name.name, diffQty).call())
      updated(value.updateProductQty(product, qty), effect)
    }
  }

}

class ProductHandler[M](modelRW: ModelRW[M, Pot[Seq[Product]]]) extends ActionHandler(modelRW) {
  override def handle = {
    case action: UpdateProducts => {
      val updateF = action.effect(AjaxService[Api].products(None).call())(p => p)
      action.handleWith(this, updateF)(PotAction.handler())
    }
  }
}

class CategoryHandler[M](modelRW: ModelRW[M, CategoryModel]) extends ActionHandler(modelRW) {
  // TODO read this
  /* http://ochrons.github.io/diode/advanced/PotActions.html */
  override def handle = {
    case SelectCategory(newCat) => updated(value.copy(cur = Some(newCat)))
    case ResetCategory          => updated(value.copy(cur = None))
    case action: UpdateCategories =>
      {
        val updateEffect = action.effect(AjaxService[Api].categories().call())(identity _)
        action.handle {
          case PotEmpty =>
            updated(value.copy(cats = value.cats.pending()), updateEffect)
          case PotPending =>
            noChange
          case PotUnavailable =>
            updated(CategoryModel(value.cats.unavailable(), None))
          case PotReady =>
            updated(CategoryModel(action.potResult, updateCurrentCat(value.cur, action.potResult.get)))
          case PotFailed =>
            val ex = action.result.failed.get
            updated(CategoryModel(value.cats.fail(ex), None))
        }
      }
  }

  def updateCurrentCat(oldSelection: Option[Category], newCats: Seq[Category]) = oldSelection match {
    case Some(cat) if !newCats.filter(_ == cat).isEmpty => oldSelection
    case None => None
  }

}
