package com.misfit.ms.modules.stream.jobs

import play.api.Logger
import com.misfit.ms.modules.stream._

class PrintMailSampleJob extends StreamJob {
	override def registerService = "ms.backend.stream.mail"
	override def onEvent(event: String) = {
		val output = "Received event from mail stream: " + event
		Logger.info(output)
	}
}
