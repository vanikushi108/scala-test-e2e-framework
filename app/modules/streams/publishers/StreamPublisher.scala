package com.misfit.ms.modules.stream

import javax.inject._
import java.nio.ByteBuffer
import scala.concurrent.Future
import scala.util.Try
import scala.collection.mutable.HashMap
import com.google.inject.AbstractModule
import play.api._
import play.api.inject.ApplicationLifecycle
import play.api.{ Logger, Environment, Configuration }

import com.gilt.gfc.kinesis.KinesisFactory
import com.gilt.gfc.kinesis.publisher._


class FakePublisher @Inject()(lifecycle: ApplicationLifecycle) extends StreamPublisherAbstract {
	// release connections when app stop
	lifecycle.addStopHook { () =>
		Future.successful(None)
	}

	override def publish(service: String, content: String): Unit = {
		// fake publish for the sake of local testing
	}

	override def shutdown(): Unit = {
		// fake shutdown
	}
}

class KafkaPublisher @Inject()(lifecycle: ApplicationLifecycle) extends StreamPublisherAbstract {
	// release connections when app stop
	lifecycle.addStopHook { () =>
		Future.successful(None)
	}

	override def publish(service: String, content: String): Unit = {}

	override def shutdown(): Unit = {}
}

class KinesisPublisher @Inject()(lifecycle: ApplicationLifecycle) extends StreamPublisherAbstract {
	// release connections when app stop
	lifecycle.addStopHook { () =>
		Future.successful(shutdown())
	}

	def TIMESTAMP: String = System.currentTimeMillis().toString

	private lazy val publisherConfig = new KinesisPublisherConfig {
		override val regionName = Play.current.configuration
																	.getString("module.ms.module.stream.region")
																	.getOrElse("us-east-1")
	}

	private val	publishersMutex = new Object
	private val publishersPool = new HashMap[String, EventPublisher[String]]()

	private def convert(content: String) =
		RawRecord(
			content.getBytes,
			PartitionKey(TIMESTAMP))

	override def publish(service: String, content: String): Unit = {
		// match the service if it has been alloced in the HashMap
		if(publishersPool.contains(service)) {
			// service has been added into HashMap
			// retrieve it from the HashMap
			publishersPool.get(service).foreach { publisher =>
				publisher.publish(content)
			}
		} else {
			// service has not been init yet
			// init & add a singleton into HashMap
			publishersMutex.synchronized {
				// use mutex to avoid conflict alloc&insert publisher
				val publisher = KinesisFactory.newPublisher(service, publisherConfig, convert)
				publishersPool += (service -> publisher)
			}

			// get the publisher from publishersPool again
			// and it should be here right now
			if (publishersPool.contains(service)) {
				publishersPool.get(service).foreach { publisher =>
					publisher.publish(content)
				}
			} else {
				// error handling about add publisher into HashMap
				Logger.error("Service init failed : " + service)
			}
		}
	}

	override def shutdown(): Unit = {
		publishersMutex.synchronized {
			publishersPool.foreach {
				case (service, publisher) => {
					publisher.shutdown()
				}
			}
		}
	}
}

class StreamPublisher(
	environment: Environment,
	configuration: Configuration) extends AbstractModule {
	def configure() = {
		val isEnabledPublisher: Boolean =
					configuration.getBoolean("module.ms.stream.publisher.enabled")
						.getOrElse(true)
		if (isEnabledPublisher) {
			val typeOfPublisherOpt: Option[String] =
						configuration.getString(
							"module.ms.module.stream.mode",
							Some(Set("local", "kinesis", "kafka")))
			typeOfPublisherOpt match {
				case Some("local") => {
					bind(classOf[StreamPublisherAbstract])
						.to(classOf[FakePublisher])
						.asEagerSingleton
					Logger.info("Bind fake stream publisher.")
				}
				case Some("kinesis") => {
					bind(classOf[StreamPublisherAbstract])
						.to(classOf[KinesisPublisher])
						.asEagerSingleton
					Logger.info("Bind Kinesis stream publisher.")
				}
				case Some("kafka") => {
					bind(classOf[StreamPublisherAbstract])
						.to(classOf[KafkaPublisher])
						.asEagerSingleton
					Logger.info("Bind Kafka stream publisher.")
				}
				case _ => {
					Logger.warn("None stream pre-defined can be binded.")
				}
			}
		}
	}
}
