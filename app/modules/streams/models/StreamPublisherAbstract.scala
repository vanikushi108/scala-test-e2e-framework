package com.misfit.ms.modules.stream

import java.nio.ByteBuffer

trait StreamPublisherAbstract {
	def publish(service: String, content: String): Unit
	def shutdown(): Unit
}
