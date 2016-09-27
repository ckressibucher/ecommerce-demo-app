package scalapp.client

import diode.ActionResult.{ModelUpdate, NoChange}
import diode.RootModelRW
import diode.data.Ready

import scalapp.model.Category
import utest._

object AppCircuitTests extends TestSuite {
  val tests = this {

    'CategoryHandler - {
      val model = CategoryModel(Ready(Category("a") :: Category("b") :: Nil), None)

      def build = new CategoryHandler(new RootModelRW[CategoryModel](model))

      'SelectCategory - {
        val h = build

        'withExistingCat - {
          val result = h.handle(SelectCategory(Category("b")))
          result ==> ModelUpdate(model.copy(cur = Some(Category("b"))))
        }

        'withNonExistingCat - {
          val result = h.handle(SelectCategory(Category("x")))
          result ==> NoChange
        }
      }

      'ResetCategory - {
        val modelWithCategory = model.copy(cur = Some(Category("a")))
        val h = new CategoryHandler(new RootModelRW[CategoryModel](modelWithCategory))
        h.handle(ResetCategory) ==> ModelUpdate(model.copy(cur = None))
      }
    }

  }
}