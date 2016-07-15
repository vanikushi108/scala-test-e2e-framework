package com.misfit.ms.modules.stream.jobs

import play.api.Logger
import com.misfit.ms.modules.stream._

class PrintLogSampleJob extends StreamJob {
	override def registerService = "ms.backend.stream.log"
	override def onEvent(event: String) = {
		val output = "Received event from log stream: " + event
		Logger.info(output)
	}
}
