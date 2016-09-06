package scalapp.client

import scalapp.client.{ AppModel => RootModel }
import diode.Circuit
import diode.react.ReactConnector
import diode._
import diode.data.Pot
import diode.data.PotState._
import scalapp.model.Category
import autowire._
import scalapp.Api
import diode.data.PotAction
import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue

class AppCircuit[RootModel] extends Circuit[AppModel] with ReactConnector[AppModel] {

  override protected def initialModel = AppModel(CategoryModel(Pot.empty, None),
    ProductModel(Pot.empty),
    CartModel(Pot.empty))

  override protected val actionHandler = composeHandlers(
    new CategoryHandler(zoomRW(_.categories)((m, v) => m.copy(categories = v))))

}

class CategoryHandler[M](modelRW: ModelRW[M, CategoryModel]) extends ActionHandler(modelRW) {

  // TODO read this
  /* http://ochrons.github.io/diode/advanced/PotActions.html */
  override def handle = {
    case SelectCategory(newCat) => updated(value.copy(cur = Some(newCat)))
    case action: UpdateCategories =>
      {
        val updateEffect = action.effect(AjaxService[Api].categories().call())(identity _)
        //action.handleWith(this, updateEffect)(PotAction.handler())
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
