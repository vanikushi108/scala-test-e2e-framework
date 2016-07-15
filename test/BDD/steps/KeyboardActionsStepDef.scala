package features.steps

import cucumber.api.scala.{EN, ScalaDsl}
import org.openqa.selenium.Keys
import org.scalatest.Matchers

class KeyboardActionsStepDef extends ScalaDsl with EN with Matchers {


  Then("""^I type "(.*?)" in "(.*?)"""") { (search_term: String, pseudoElement: String) =>
    val superText = search_term match {
      case "ENTER" => Keys.ENTER
      case _ => search_term
    }
  }

  When("""^I hit "(.*?)" on Keyboard$""") { (keys: String) =>
    val keyValue = keys match {
      case "ENTER" => Keys.ENTER
      case "TAB" => Keys.TAB
      case "RETURN" => Keys.RETURN
      case "BACKSPACE" => Keys.BACK_SPACE
      case "CANCEL" => Keys.CANCEL
      case "CLEAR" => Keys.CLEAR
      case "SPACE" => Keys.SPACE
    }
  }
}
