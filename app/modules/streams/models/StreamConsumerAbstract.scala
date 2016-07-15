package com.misfit.ms.modules.stream

trait StreamConsumerAbstract {
	def register(service: String, consumer: String => Unit): Unit
	def start(): Unit
	def shutdown(): Unit
}
