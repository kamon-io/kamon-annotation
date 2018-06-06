/*
 * =========================================================================================
 * Copyright © 2013-2018 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package kamon.annotation

import kamon.Kamon
import kamon.annotation.api._
import kamon.metric.{Histogram => _, RangeSampler => _, Timer => _, _}
import kamon.testkit.{MetricInspection, Reconfigure, TestSpanReporter}
import kamon.trace.Span
import kamon.trace.Span.TagValue
import kamon.util.Registration
import org.scalatest.concurrent.Eventually
import org.scalatest.time.SpanSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

class AnnotationInstrumentationSpec extends WordSpec
  with Matchers
  with Eventually
  with SpanSugar
  with Reconfigure
  with MetricInspection
  with BeforeAndAfterAll
  with TimerMetricInspection
  with OptionValues {

  "the Kamon Annotation module" should {
    "create a new trace when is invoked a method annotated with @Trace" in {
     for (id ← 1 to 10) Annotated(id).trace()

      eventually(timeout(10 seconds)) {
        val span = reporter.nextSpan().value
        val spanTags = stringTag(span) _
        span.operationName shouldBe "trace"
        spanTags("slow-service") shouldBe "service"
        spanTags("env") shouldBe "prod"
      }
    }

    "count the invocations of a method annotated with @Count" in {
      for (id ← 1 to 10) Annotated(id).count()

      Kamon.counter("count").value() should be(10)
    }

    "count the invocations of a method annotated with @Count and evaluate EL expressions" in {
      for (id ← 1 to 2) Annotated(id).countWithEL()

      Kamon.counter("counter:1").refine(Map("counter" -> "1", "env" -> "prod")).value()should be(1)
      Kamon.counter("counter:2").refine(Map("counter" -> "1", "env" -> "prod")).value()should be(1)
    }

    "count the current invocations of a method annotated with @RangeSampler" in {
      for (id ← 1 to 10) {
        Annotated(id).countMinMax()
      }
      Kamon.rangeSampler("minMax").distribution().max should be(1)
    }

    "count the current invocations of a method annotated with @RangeSampler and evaluate EL expressions" in {
      for (id ← 1 to 10) Annotated(id).countMinMaxWithEL()

      Kamon.rangeSampler("minMax:1").refine(Map("minMax" -> "1", "env" -> "dev")).distribution().sum should be(1)
      Kamon.rangeSampler("minMax:2").refine(Map("minMax" -> "1", "env" -> "dev")).distribution().sum should be(1)
    }

    "measure the time spent in the execution of a method annotated with @Timer" in {
      for (id ← 1 to 1) Annotated(id).time()

      Kamon.timer("time").distribution().count should be(1)
    }

    "measure the time spent in the execution of a method annotated with @Timer and evaluate EL expressions" in {
      for (id ← 1 to 1) Annotated(id).timeWithEL()

      Kamon.timer("time:1").refine(Map("slow-service" -> "service", "env" -> "prod")).distribution().count should be(1)
    }

    "record the operationName returned by a method annotated with @Histogram" in {
      for (operationName ← 1 to 5) Annotated().histogram(operationName)

      val snapshot = Kamon.histogram("histogram").distribution()
      snapshot.count should be(5)
      snapshot.min should be(1)
      snapshot.max should be(5)
      snapshot.sum should be(15)
    }

    "record the operationName returned by a method annotated with @Histogram and evaluate EL expressions" in {
      for (operationName ← 1 to 2) Annotated(operationName).histogramWithEL(operationName)

      val snapshot1 = Kamon.histogram("histogram:1").refine(Map("histogram" -> "hdr", "env" -> "prod")).distribution()
      snapshot1.count should be(1)
      snapshot1.min should be(1)
      snapshot1.max should be(1)
      snapshot1.sum should be(1)

      val snapshot2 = Kamon.histogram("histogram:2").refine(Map("histogram" -> "hdr", "env" -> "prod")).distribution()
      snapshot2.count should be(1)
      snapshot2.min should be(2)
      snapshot2.max should be(2)
      snapshot2.sum should be(2)
    }
  }

  @volatile var registration: Registration = _
  val reporter = new TestSpanReporter()

  override protected def beforeAll(): Unit = {
    enableFastSpanFlushing()
    sampleAlways()
    registration = Kamon.addReporter(reporter)
  }

  override protected def afterAll(): Unit = {
    registration.cancel()
  }

  def stringTag(span: Span.FinishedSpan)(tag: String): String = {
    span.tags(tag).asInstanceOf[TagValue.String].string
  }
}

case class Annotated(id: Long) {
  @Trace(operationName = "trace", tags = "${'slow-service':'service', 'env':'prod'}")
  def trace(): Unit = {}

  @Count(name = "count")
  def count(): Unit = {}

  @Count(name = "${'counter:' += this.id}", tags = "${'counter':'1', 'env':'prod'}")
  def countWithEL(): Unit = {}

  @RangeSampler(name = "minMax")
  def countMinMax(): Unit = {}

  @RangeSampler(name = "#{'minMax:' += this.id}", tags = "#{'minMax':'1', 'env':'dev'}")
  def countMinMaxWithEL(): Unit = {}

  @Timer(name = "time")
  def time(): Unit = {}

  @Timer(name = "${'time:' += this.id}", tags = "${'slow-service':'service', 'env':'prod'}")
  def timeWithEL(): Unit = {}

  @Histogram(name = "histogram")
  def histogram(operationName: Long): Long = operationName

  @Histogram(name = "#{'histogram:' += this.id}", tags = "${'histogram':'hdr', 'env':'prod'}")
  def histogramWithEL(operationName: Long): Long = operationName
}

object Annotated {
  def apply(): Annotated = new Annotated(0L)
}

trait TimerMetricInspection extends MetricInspection{
  implicit class TimerMetricSyntax(metric: kamon.metric.Timer) {
    def distribution(resetState: Boolean = true): Distribution =
      metric match {
        case t: TimerImpl     => t.histogram.distribution(resetState)
        case t: TimerMetricImpl     => t.underlyingHistogram.distribution(resetState)
      }
  }
}

