package scalapp.model

import utest._

object PriceTest extends TestSuite {
  val tests = this{
    'format {
      val price = Price(119) // 119 cents

      "with default options" - {
        price.format() ==> "1.19 Euro"
      }

      "with decimal separator" - {
        price.format(",", suffix = "") ==> "1,19"
      }

      "with prefix" - {
        price.format(prefix = "USD ", suffix = "") ==> "USD 1.19"
      }

      "with suffix" - {
        price.format(suffix = " EURO") ==> "1.19 EURO"
      }

      "with all options" - {
        price.format(",", "+", " CHF") ==> "+1,19 CHF"
      }
    }
  }
}