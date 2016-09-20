package scalapp.client

import autowire._
import diode.{Circuit, _}
import diode.data.PotState._
import diode.data.{Pot, PotAction, Ready}
import diode.react.ReactConnector

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.Success
import scalapp.{ActionFailedMsg, Api}
import scalapp.model.{CartView, Product}

object AppCircuit extends Circuit[AppModel] with ReactConnector[AppModel] {

  override protected def initialModel = AppModel(CategoryModel(Pot.empty, None),
    ProductModel(Pot.empty),
    Pot.empty)

  val categoryHandler = new CategoryHandler(zoomRW(_.categories)((m, v) => m.copy(categories = v)))
  val productHandler = new ProductHandler(zoomRW(_.products)((m, v) => m.copy(products = v))
    .zoomRW(_.all)((m, v) => m.copy(all = v)))
  val cartHandler = new CartHandler(zoomRW(_.cartView)((m, v) => m.copy(cartView = v)))
  val cartViewHandler = new CartViewHandler(zoomRW(_.cartView)((m, v) => m.copy(cartView = v)))
  val initHandler = new InitHandler(zoomRW(identity _)((m, v) => v))

  val logHandler = new ActionHandler(zoomRW(x => x)((_, v) => v)) {
    override def handle: PartialFunction[Any, ActionResult[AppModel]] = {
      case action => println("action: " + action); noChange
    }
  }

  val realActionHandler = composeHandlers(
    categoryHandler,
    productHandler,
    cartHandler,
    cartViewHandler,
    initHandler)

  val actionHandler = foldHandlers(logHandler, realActionHandler)

}

class InitHandler[M](modelRW: ModelRW[M, AppModel]) extends ActionHandler(modelRW) {

  override def handle = {
    case InitializeApp =>
      val effect = Effect[Action](AjaxService[Api].showCart("session").call().map {
        case Right(cartViewResult) => UpdateCartView(Ready(cartViewResult))
        case Left(err) => UpdateCartView(diode.data.Failed(ActionFailedMsg(err)))
      })
      effectOnly(effect)
  }
}

class CartHandler[M](modelRW: ModelRW[M, Pot[CartView]]) extends ActionHandler(modelRW) {

  val console = js.Dynamic.global.console

  def handleAjaxResult(effect: => Future[Either[String, CartView]], prevVal: Pot[CartView]): Effect =
    Effect[Action](effect.map {
      case Right(result) => UpdateCartView(prevVal.ready(result))
      case Left(err) => UpdateCartView(prevVal.fail(ActionFailedMsg(err)))
    })

  override def handle = {
    case AddProduct(product: Product, qty: Int) =>
      val interimState = value.pending()
      val effect = handleAjaxResult(AjaxService[Api].addToCart("session", product.name.name, qty).call(), interimState)
      updated(interimState, effect)
    case RemoveProduct(product: Product) =>
      val interimState = value.pending()
      val effect = handleAjaxResult(AjaxService[Api].deleteFromCart("session", product.name.name).call(), interimState)
      updated(interimState, effect)
    case UpdateProductQty(product: Product, qty: Int) =>
      if (!value.isEmpty) {
        val oldQty = value.get.qtyByProduct(product)
        val diffQty = qty - oldQty
        val interimState = value.pending()
        val effect = handleAjaxResult(AjaxService[Api].addToCart("session", product.name.name, diffQty).call(), interimState)
        updated(interimState, effect)
      } else {
        console.warn("cart is not ready yet, update action is dropped...")
        noChange
      }
    case ClearCart =>
      val interimState = value.pending()
      updated(interimState,
        handleAjaxResult(AjaxService[Api].clearCart("session").call(), interimState))
    case ApplyDiscount(code) =>
      val interimState = value.pending()
      updated(interimState,
        handleAjaxResult(AjaxService[Api].applyDiscount("session", code).call(), interimState))
    case RemoveDiscount(code) =>
      val interimState = value.pending()
      updated(interimState,
        handleAjaxResult(AjaxService[Api].removeDiscount("session", code).call(), interimState))
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

class CartViewHandler[M](modelRW: ModelRW[M, Pot[CartView]]) extends ActionHandler(modelRW) {
  override def handle = {
    case action: UpdateCartView => {
      val updateF = action.effect(AjaxService[Api].showCart("session").call().flatMap {
        case Right(cartView) => Future.successful(cartView)
        case Left(err) => Future.failed(ActionFailedMsg(err))
      })(identity _)
      action.handleWith(this, updateF)(PotAction.handler())
    }
  }
}

class CategoryHandler[M](modelRW: ModelRW[M, CategoryModel]) extends ActionHandler(modelRW) {
  // TODO read this
  /* http://ochrons.github.io/diode/advanced/PotActions.html */
  override def handle = {
    case SelectCategory(newCat) => updated(value.copy(cur = Some(newCat)))
    case ResetCategory => updated(value.copy(cur = None))
    case action: UpdateCategories =>
      val updateEffect = action.effect(AjaxService[Api].categories().call())(identity _)
      action.handle {
        case PotEmpty =>
          updated(value.copy(cats = value.cats.pending()), updateEffect)
        case PotPending =>
          noChange
        case PotUnavailable =>
          updated(CategoryModel(value.cats.unavailable(), None))
        case PotReady =>
          updated(CategoryModel(action.potResult, value.cur.filter(action.potResult.get.contains(_))))
        case PotFailed =>
          val ex = action.result.failed.get
          updated(CategoryModel(value.cats.fail(ex), None))
      }
  }

}
