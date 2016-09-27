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

    'divMod - {
      'positive - {
        val price = Price(119)
        price.divMod ==> (1, 19)
      }
      'zero - {
        val price = Price(0)
        price.divMod ==> (0, 0)
      }
      'negative - {
        val price = Price(-10)
        price.divMod ==> (0, -10)
      }
      'negative2 - {
        val price = Price(-119)
        price.divMod ==> (-1, -19)
      }
    }

    'divModAbs - {
       'positive - {
        val price = Price(119)
        price.divModAbs ==> (1, 19)
      }
      'zero - {
        val price = Price(0)
        price.divModAbs ==> (0, 0)
      }
      'negative - {
        val price = Price(-10)
        price.divModAbs ==> (0, 10)
      }
      'negative2 - {
        val price = Price(-119)
        price.divModAbs ==> (1, 19)
      }
    }

    'negate - {
      'positive - {
        Price(119).negate ==> Price(-119)
      }
      'zero - {
        Price(0).negate ==> Price(0)
      }
      'negative - {
        Price(-119).negate ==> Price(119)
      }
    }

    "priceTuple returns sign, main, cents" - {
      'positive - {
        Price(119).priceTuple ==> (PriceSignum.Plus, 1, 19)
      }
      'zero - {
        Price(0).priceTuple ==> (PriceSignum.Plus, 0, 0)
      }
      'negative - {
        Price(-119).priceTuple ==> (PriceSignum.Minus, 1, 19)
      }
    }
  }
}