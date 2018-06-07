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
import kamon.testkit.{MetricInspection, Reconfigure, TestSpanReporter}
import kamon.trace.Span
import kamon.trace.Span.TagValue
import kamon.util.Registration
import org.scalatest.concurrent.Eventually
import org.scalatest.time.SpanSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

class StaticAnnotationInstrumentationSpec extends WordSpec
  with Matchers
  with Eventually
  with SpanSugar
  with Reconfigure
  with MetricInspection
  with BeforeAndAfterAll
  with TimerMetricInspection
  with OptionValues {

  "the Kamon Annotation module" should {
    "create a new trace when is invoked a method annotated with @Trace in a Scala Object" in {
      for (_<- 1 to 10) AnnotatedObject.trace()

      eventually(timeout(10 seconds)) {
        val span = reporter.nextSpan().value
        val spanTags = stringTag(span) _
        span.operationName shouldBe "trace"
        spanTags("slow-service") shouldBe "service"
        spanTags("env") shouldBe "prod"
      }
    }

    "count the invocations of a method annotated with @Count in a Scala Object" in {
      for (_ <- 1 to 10) AnnotatedObject.count()

      Kamon.counter("count").value() should be(10)
    }

    "count the invocations of a method annotated with @Count and evaluate EL expressions in a Scala Object" in {
      for (_ <- 1 to 2) AnnotatedObject.countWithEL()

      Kamon.counter("count:10").refine(Map("counter" -> "1", "env" -> "prod")).value()should be(2)
    }

    "count the current invocations of a method annotated with @RangeSampler in a Scala Object" in {
      for (_ <- 1 to 10) {
        AnnotatedObject.countMinMax()
      }

      eventually(timeout(5 seconds)) {
        Kamon.rangeSampler("minMax").distribution().max should be(0)
      }
    }

    "count the current invocations of a method annotated with @RangeSampler and evaluate EL expressions in a Scala Object" in {
      for (_ ← 1 to 10) AnnotatedObject.countMinMaxWithEL()

      eventually(timeout(5 seconds)) {
        Kamon.rangeSampler("minMax:10").refine(Map("minMax" -> "1", "env" -> "dev")).distribution().sum should be(0)
      }
    }

    "measure the time spent in the execution of a method annotated with @Timer in a Scala Object" in {
      AnnotatedObject.time()

      Kamon.timer("time").distribution().count should be(1)
    }

    "measure the time spent in the execution of a method annotated with @Timer and evaluate EL expressions in a Scala Object" in {
      AnnotatedObject.timeWithEL()

      Kamon.timer("time:10").refine(Map("slow-service" -> "service", "env" -> "prod")).distribution().count should be(1)
    }

    "record the operationName returned by a method annotated with @Histogram in a Scala Object" in {
      for (operationName ← 1 to 5) AnnotatedObject.histogram(operationName)

      val snapshot = Kamon.histogram("histogram").distribution()
      snapshot.count should be(5)
      snapshot.min should be(1)
      snapshot.max should be(5)
      snapshot.sum should be(15)
    }

    "record the operationName returned by a method annotated with @Histogram and evaluate EL expressions in a Scala Object" in {
      for (operationName ← 1 to 2) AnnotatedObject.histogramWithEL(operationName)

      val snapshot = Kamon.histogram("histogram:10").refine(Map("histogram" -> "hdr", "env" -> "prod")).distribution()
      snapshot.count should be(2)
      snapshot.min should be(1)
      snapshot.max should be(2)
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

object AnnotatedObject {

  val Id = "10"

  @Trace(operationName = "trace", tags = "${'slow-service':'service', 'env':'prod'}")
  def trace(): Unit = {}

  @Count(name = "count")
  def count(): Unit = {}

  @Count(name = "${'count:' += AnnotatedObject$.MODULE$.Id}", tags = "${'counter':'1', 'env':'prod'}")
  def countWithEL(): Unit = {}

  @RangeSampler(name = "minMax")
  def countMinMax(): Unit = {}

  @RangeSampler(name = "#{'minMax:' += AnnotatedObject$.MODULE$.Id}", tags = "#{'minMax':'1', 'env':'dev'}")
  def countMinMaxWithEL(): Unit = {}

  @Timer(name = "time")
  def time(): Unit = {}

  @Timer(name = "${'time:' += AnnotatedObject$.MODULE$.Id}", tags = "${'slow-service':'service', 'env':'prod'}")
  def timeWithEL(): Unit = {}

  @Histogram(name = "histogram")
  def histogram(operationName: Long): Long = operationName

  @Histogram(name = "#{'histogram:' += AnnotatedObject$.MODULE$.Id}", tags = "${'histogram':'hdr', 'env':'prod'}")
  def histogramWithEL(operationName: Long): Long = operationName
}