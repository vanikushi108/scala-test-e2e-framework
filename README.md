# Nüwa

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/truman-misfit/nvwa?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
![Travis-CI](https://travis-ci.org/truman-misfit/nvwa.svg)
[![Coverage Status](https://coveralls.io/repos/truman-misfit/nvwa/badge.svg?branch=master)](https://coveralls.io/r/truman-misfit/nvwa?branch=master)

This is a archetype project (or seed project) for building up micro-service.

Nüwa, also known as Nügua, is a goddess in ancient Chinese mythology best known for creating mankind and repairing the pillar of heaven. (from [Wikipedia](https://en.wikipedia.org/wiki/N%C3%BCwa))

# Features
* BDD with cucumber-jvm
* Stream module
* Travis-CI & Coveralls support

# Requirements
* Java 1.8.0_05 or above
* Scala 2.11.6 or above
* Activator 1.3.4 or above
* sbt 0.13.8 or above

# Template Usage
## 1. Pre-installation
#### 1.1 Install [JavaSE](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (Java 1.8.0_05 or above)**

#### 1.2 Install [Scala](http://www.scala-lang.org/download/) (Scala 2.11.6 or above)**

#### 1.3 Install [Activator](https://www.typesafe.com/get-started) (Activator 1.3.4 or above)**

Both mini-package and full-package are OK.

#### 1.4 Git pull repo

* _Step 1_. pull repo
```
git clone https://github.com/truman-misfit/nvwa.git
```

* _Step 2_. activator run
```
cd nvwa
activator run
```

Or you can test cucumber by following the commands below:
```
activator cucumber
```
## 2. Configuration
#### 2.1 Stream
Stream is a AWS Kinesis Publisher/Consumer module.

Using dependency injection, you can integrate Stream module into your self-customized services.

* _Step 1_. add StreamPlugin in play.plugins
append the line below the play.plugins file:
```scala
1000:com.misfit.microservices.plugins.StreamPlugin
```

* _Step 2_. define yourself StreamJob classes
For example:
One job for Logging

```scala
import play.api.Logger
import com.misfit.microservices.modules._

object PrintLogSampleJob extends StreamJob {
	override def registerService = "ms.backend.stream.log"
	override def onEvent(event: String) = {
		val output = "Received event from log stream: " + event
		Logger.info(output)
	}
}
```
Another job for mailing
```scala
import play.api.Logger
import com.misfit.microservices.modules._

object PrintMailSampleJob extends StreamJob {
	override def registerService = "ms.backend.stream.mail"
	override def onEvent(event: String) = {
		val output = "Received event from mail stream: " + event
		Logger.info(output)
	}
}
```

You can define your own consumer jobs with extending StreamJob trait and overriding the methods below:
```scala
// Your stream name. Each consumer should be assigned a stream
override def registerService = ""
// your real job procedure is defined in this callback function
override def onEvent(event: String) = {}
```

* _Step 3_. register your jobs in the application.conf file
Add the lines below into applicaion.conf:

```
# Stream Module
# ~~~~~
# Stream module enabled
play.modules.enabled += "com.misfit.ms.modules.stream.StreamPublisher"
play.modules.enabled += "com.misfit.ms.modules.stream.StreamConsumer"

# You can manually disable publisher or consumer
# module.ms.module.stream.publisher.enabled = false
# module.ms.module.stream.consumer.enabled = false

# Stream mode and connection info
module.ms.module.stream.mode = "kinesis"
module.ms.module.stream.region = "us-east-1"
module.ms.module.stream.app = "ms.backend.stream.demo"

# Stream jobs
module.ms.module.stream.jobs += "com.misfit.ms.modules.stream.jobs.PrintLogSampleJob"
module.ms.module.stream.jobs += "com.misfit.ms.modules.stream.jobs.PrintMailSampleJob"
```

* _Step 4_. publish to a specific stream

```scala
class Application @Inject()(publisher: StreamPublisherAbstract) extends Controller {

	def pushToMailStream = Action {
		publisher.publish("ms.backend.stream.mail", "this is for mail stream.")
		Ok
	}

	def pushToLogStream = Action {
		publisher.publish("ms.backend.stream.log", "this is for log stream.")
		Ok
	}
}
```

#### 2.2 BDD
BDD is implemented via cucumber-java. For details you can see the cucumber-jvm Github Page: [cucumber/cucumber-jvm](https://github.com/cucumber/cucumber-jvm)

You can self-define the cucumber default directory by modify the configs in build.sbt. Here is sample:
```
// Play2-cucumber integration
cucumberSettings

cucumberFeaturesLocation := "./test/BDD/features"

cucumberStepsBasePackage := "features.steps"
```

# Future
* Local mode for Stream module(a internal message queue)
* Kafka integration in Stream module
* Cache module(a centralized cache system powered by Redis/ElastiCache)

# Author
truman@misfit.com
