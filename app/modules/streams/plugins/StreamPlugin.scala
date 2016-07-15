package com.misfit.ms.plugins

import javax.inject._
import play.api.Play
import play.api.{Plugin, Application, Logger}
import com.misfit.ms.modules.stream._
import com.misfit.ms.modules.stream.jobs._

class StreamPlugin @Inject()(consumer: StreamConsumerAbstract) extends Plugin {

	override def onStart() {

		val consumerJobsListOpt = Play.current.configuration
															 		.getStringSeq("module.ms.module.stream.jobs")
		consumerJobsListOpt.foreach { jobsList =>
			val consumerJobsClassLoader = Play.current.classloader
			jobsList.foreach { job =>
				// load class by self-defined job class path
				val jobInstance = consumerJobsClassLoader
												.loadClass(job)
												.newInstance()
												.asInstanceOf[StreamJob]
				consumer.register(
					jobInstance.registerService,
					jobInstance.onEvent
				)
			}
		}

		// start all registered consumers
		consumer.start()
		Logger.info("Simple queue plugin has started.")
	}

	override def onStop() {
		Logger.info("Simple queue plugin has stopped.")
	}

}
