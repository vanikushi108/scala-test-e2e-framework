package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import com.misfit.ms.modules.stream._

class Application @Inject()(publisher: StreamPublisherAbstract) extends Controller {

	def putmail = Action {
		publisher.publish("ms.backend.stream.mail", "this is for mail stream.")
		Ok
	}

	def putlog = Action {
		publisher.publish("ms.backend.stream.log", "this is for log stream.")
		Ok
	}
}
