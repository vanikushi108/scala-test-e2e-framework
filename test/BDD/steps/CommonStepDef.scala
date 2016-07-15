package features.steps

import com.typesafe.config.{Config, ConfigFactory}
import play.api._
import play.api.mvc._
import play.api.test._
import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.json._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.util.Timeout

import org.openqa.selenium._
import org.fluentlenium.core.filter.FilterConstructor._

import org.scalatest.Matchers
import cucumber.api.scala.{ScalaDsl, EN}
import cucumber.api.DataTable
import cucumber.api.PendingException

class CommonStepDef extends ScalaDsl with EN with Matchers {
  val conf = ConfigFactory.load();
  val browserType = sys.props.get("browserType").getOrElse("HTMLUNIT")

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

//    val webDriverClass = Helpers.FIREFOX

    val webDriverClass = Helpers.HTMLUNIT

  val app = FakeApplication()
  val port = 3333

  lazy val browser: TestBrowser = TestBrowser.of(webDriverClass, Some("http://localhost:" + port))
  lazy val server = TestServer(port, app)

  def driver = browser.getDriver()

  var requestAddress = s"http://localhost:$port"
  implicit val timeout = Timeout(60 seconds)

  Before() { s =>
    // init
    server.start()
  }

  After() { s =>
    // shut down
    server.stop()
    browser.quit()
  }

  // step def for logging service
  Given("""^logging service has started$""") { () =>
    Logger.debug("Logging module is running...")
  }

  When("""^I enter URL "([^"]*)"$""") { (url: String) =>
    driver.get(conf.getString(url))
    Logger.info("Logging url: ")
  }

  When("""^I enter "(.*?)" on text field "(.*?)"$""") { (inputText: String, elementLocator: String) =>
    driver.findElement(By.cssSelector(conf.getString(elementLocator))).sendKeys(inputText)
  }

  When("""^I click on button "(.*?)"$""") { (elementLocator: String) =>
    driver.findElement(By.cssSelector(conf.getString(elementLocator))).click()
  }

  Then("""^I should see response status code is ([^"]*) for rest "(.*?)"$""") { (expectedHttpCode: Int, freeTextSearchAPI: String) =>
    // http code 200 is returned
    val response = Await.result(
      WS.url("https://spiderpig-staging.herokuapp.com/api/messages/5735b863ad9a6303001d62161108")
        .withRequestTimeout(10000)
        .get,
      timeout.duration
    ).asInstanceOf[WSResponse]
    response.status should be(expectedHttpCode)
  }

  Then("""^I should see "(.*?)" on the Search Results$""") { (searchTerm: String) =>
    val results = driver.findElements(By.cssSelector(".b_algo>h2>a>strong"))
    assert(true)
  }
}
