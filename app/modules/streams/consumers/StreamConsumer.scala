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
import com.gilt.gfc.kinesis.consumer._
import scala.concurrent.duration._

class FakeConsumer @Inject()(lifecycle: ApplicationLifecycle) extends StreamConsumerAbstract {
	// release connections when app stop
	lifecycle.addStopHook { () =>
		Future.successful(shutdown())
	}

	override def register(service: String, onEvent: String => Unit): Unit = {
		// fake register consumer
	}

	override def start(): Unit = {
		// fake start consumers
	}

	override def shutdown(): Unit = {
		// fake shutdown
	}
}

class KafkaConsumer @Inject()(lifecycle: ApplicationLifecycle) extends StreamConsumerAbstract {
	// release connections when app stop
	lifecycle.addStopHook { () =>
		Future.successful(shutdown())
	}

	override def register(service: String, onEvent: String => Unit): Unit = {
		// kafka register consumer
	}

	override def start(): Unit = {
		// kafka start consumers
	}

	override def shutdown(): Unit = {
		// kafka shutdown
	}
}

class KinesisConsumer @Inject()(lifecycle: ApplicationLifecycle) extends StreamConsumerAbstract {
	// release connections when app stop
	lifecycle.addStopHook { () =>
		Future.successful(shutdown())
	}

	private lazy val consumerConfig = new KinesisConsumerConfig {
		override val appName = Play.current.configuration
															.getString("module.ms.module.stream.app")
															.getOrElse("ms.backend.stream.demo")
		override val regionName = Play.current.configuration
																	.getString("module.ms.module.stream.region")
																	.getOrElse("us-east-1")
		override val idleTimeBetweenReads = 200.milliseconds
	}

	private val consumersMutex = new Object
	private val consumersPool = new HashMap[String, EventReceiver[String]]()

	private def convert(
		record: RawRecord): Option[String] =
		Some(new String(record.data))

	override def register(service: String, onEvent: String => Unit): Unit = {
		// kinesis register consumer
		if (consumersPool.contains(service)) {
			// service has been added into hashmap
			// retrieve it from the HashMap
			consumersPool.get(service).foreach { consumer =>
				consumer.registerConsumer(onEvent)
			}
		} else {
			consumersMutex.synchronized {
				// use mutex to avoid conflict alloc&insert consumer
				val consumer = KinesisFactory.newReceiver(
					service, consumerConfig,
					convert, CheckpointingStrategy.AfterBatch)
				consumersPool += (service -> consumer)
				// register onEvent function
			}
			if (consumersPool.contains(service)) {
				consumersPool.get(service).foreach { consumer =>
					consumer.registerConsumer(onEvent)
				}
			} else {
				Logger.error("Service init failed : " + service)
			}
		}
	}

	override def start(): Unit = {
		// kinesis consumers start
		consumersMutex.synchronized {
			consumersPool.foreach {
				case (service, consumer) => {
					consumer.start()
				}
			}
		}
	}

	override def shutdown(): Unit = {
		// kinesis consumers shutdown
		consumersMutex.synchronized {
			consumersPool.foreach {
				case (service, consumer) => {
					consumer.shutdown()
				}
			}
		}
	}
}

class StreamConsumer(
	environment: Environment,
	configuration: Configuration) extends AbstractModule {
	def configure() = {
		val isEnabledConsumer: Boolean =
					configuration.getBoolean("module.ms.stream.consumer.enabled")
						.getOrElse(true)
		if (isEnabledConsumer) {
			val typeOfConsumerOpt: Option[String] =
						configuration.getString(
							"module.ms.module.stream.mode",
							Some(Set("local", "kinesis", "kafka")))
			typeOfConsumerOpt match {
				case Some("local") => {
					bind(classOf[StreamConsumerAbstract])
						.to(classOf[FakeConsumer])
						.asEagerSingleton
					Logger.info("Bind fake stream consumer.")
				}
				case Some("kinesis") => {
					bind(classOf[StreamConsumerAbstract])
						.to(classOf[KinesisConsumer])
						.asEagerSingleton
					Logger.info("Bind Kinesis stream consumer.")
				}
				case Some("kafka") => {
					bind(classOf[StreamConsumerAbstract])
						.to(classOf[KafkaConsumer])
						.asEagerSingleton
					Logger.info("Bind Kafka stream consumer.")
				}
				case _ => {
					Logger.warn("None stream pre-defined can be binded.")
				}
			}
		}
	}
}
