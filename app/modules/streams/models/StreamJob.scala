package com.misfit.ms.modules.stream

trait StreamJob {
	def registerService: String
	def onEvent(event: String): Unit
}
