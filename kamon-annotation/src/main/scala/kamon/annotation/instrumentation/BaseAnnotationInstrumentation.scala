/*
 * =========================================================================================
 * Copyright © 2013-2015 the kamon project <http://kamon.io/>
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

package kamon.annotation.instrumentation

import java.util
import java.util.concurrent.atomic.AtomicReferenceArray

import kamon.annotation.el.ELProcessorFactory
//
import javax.el.ELProcessor
//import kamon.Kamon
//import kamon.annotation.el.{ELProcessorFactory, EnhancedELProcessor}
//import kamon.annotation.{Histogram, _}
//import kamon.metric.instrument.Histogram.DynamicRange
//import kamon.metric.instrument.{Counter, MinMaxCounter}
//import kamon.metric._
//import kamon.trace.Tracer
//import kamon.util.Latency
//import org.aspectj.lang.{JoinPoint, ProceedingJoinPoint}
//import org.aspectj.lang.annotation.{Aspect, DeclareMixin}
//import org.aspectj.lang.reflect.MethodSignature
import kamon.annotation.el.EnhancedELProcessor.Syntax
//import kamon.annotation.api.{Time, _}
//
//class BaseAnnotationInstrumentation {
//
//  @inline final def registerTime(jps: JoinPoint.StaticPart, histograms: AtomicReferenceArray[instrument.Histogram], evalString: StringEvaluator, evalTags: TagsEvaluator): instrument.Histogram = {
//    val method = jps.getSignature.asInstanceOf[MethodSignature].getMethod
//    val time = method.getAnnotation(classOf[Time])
//    val name = evalString(time.name())
//    val tags = evalTags(time.tags())
//    val currentHistogram = Kamon.metrics.histogram(name, tags)
//    histograms.set(jps.getId, currentHistogram)
//    currentHistogram
//  }
//
//  @inline final def registerHistogram(jps: JoinPoint.StaticPart, histograms: AtomicReferenceArray[instrument.Histogram], evalString: StringEvaluator, evalTags: TagsEvaluator): instrument.Histogram = {
//    val method = jps.getSignature.asInstanceOf[MethodSignature].getMethod
//    val histogram = method.getAnnotation(classOf[Histogram])
//    val name = evalString(histogram.name())
//    val tags = evalTags(histogram.tags())
//    val dynamicRange = DynamicRange(histogram.lowestDiscernibleValue(), histogram.highestTrackableValue(), histogram.precision())
//    val currentHistogram = Kamon.metrics.histogram(name, tags, dynamicRange)
//    histograms.set(jps.getId, currentHistogram)
//    currentHistogram
//  }
//
//  @inline final def registerMinMaxCounter(jps: JoinPoint.StaticPart, minMaxCounters: AtomicReferenceArray[MinMaxCounter], evalString: StringEvaluator, evalTags: TagsEvaluator): instrument.MinMaxCounter = {
//    val method = jps.getSignature.asInstanceOf[MethodSignature].getMethod
//    val minMaxCount = method.getAnnotation(classOf[RangeSampler])
//    val name = evalString(minMaxCount.name())
//    val tags = evalTags(minMaxCount.tags())
//    val minMaxCounter = Kamon.metrics.minMaxCounter(name, tags)
//    minMaxCounters.set(jps.getId, minMaxCounter)
//    minMaxCounter
//  }
//
//  @inline final def registerCounter(jps: JoinPoint.StaticPart, counters: AtomicReferenceArray[Counter], evalString: StringEvaluator, evalTags: TagsEvaluator): instrument.Counter = {
//    val method = jps.getSignature.asInstanceOf[MethodSignature].getMethod
//    val count = method.getAnnotation(classOf[Count])
//    val name = evalString(count.name())
//    val tags = evalTags(count.tags())
//    val counter = Kamon.metrics.counter(name, tags)
//    counters.set(jps.getId, counter)
//    counter
//  }
//
//  @inline final def registerTrace(jps: JoinPoint.StaticPart, traces: AtomicReferenceArray[TraceContextInfo], evalString: StringEvaluator, evalTags: TagsEvaluator): TraceContextInfo = {
//    val method = jps.getSignature.asInstanceOf[MethodSignature].getMethod
//    val trace = method.getAnnotation(classOf[Trace])
//    val name = evalString(trace.operationName())
//    val tags = evalTags(trace.tags())
//    val traceContextInfo = TraceContextInfo(name, tags)
//    traces.set(jps.getId, traceContextInfo)
//    traceContextInfo
//  }
//
//  @inline final def registerSegment(jps: JoinPoint.StaticPart, segments: AtomicReferenceArray[SegmentInfo], evalString: StringEvaluator, evalTags: TagsEvaluator): SegmentInfo = {
//    val method = jps.getSignature.asInstanceOf[MethodSignature].getMethod
//    val segment = method.getAnnotation(classOf[Segment])
//    val name = evalString(segment.name())
//    val category = evalString(segment.category())
//    val library = evalString(segment.library())
//    val tags = evalTags(segment.tags())
//    val segmentInfo = SegmentInfo(name, category, library, tags)
//    segments.set(jps.getId, segmentInfo)
//    segmentInfo
//  }
//
//  @inline final def processTrace(traceInfo: TraceContextInfo, pjp: ProceedingJoinPoint): AnyRef = {
//    Tracer.withContext(Kamon.tracer.newContext(traceInfo.name)) {
//      traceInfo.tags.foreach { case (key, operationName) ⇒ Tracer.currentContext.addTag(key, operationName) }
//      val result = pjp.proceed()
//      Tracer.currentContext.finish()
//      result
//    }
//  }
//
//  @inline final def processSegment(segmentInfo: SegmentInfo, pjp: ProceedingJoinPoint): AnyRef = {
//    Tracer.currentContext.collect { ctx ⇒
//      val currentSegment = ctx.startSegment(segmentInfo.name, segmentInfo.category, segmentInfo.library)
//      segmentInfo.tags.foreach { case (key, operationName) ⇒ currentSegment.addMetadata(key, operationName) }
//      val result = pjp.proceed()
//      currentSegment.finish()
//      result
//    } getOrElse pjp.proceed()
//  }
//
//  @inline final def processTime(histogram: instrument.Histogram, pjp: ProceedingJoinPoint): AnyRef = {
//    Latency.measure(histogram)(pjp.proceed)
//  }
//
//  @inline final def processHistogram(histogram: instrument.Histogram, result: AnyRef, jps: JoinPoint.StaticPart): Unit = {
//    histogram.record(result.asInstanceOf[Number].longValue())
//  }
//
//  final def processCount(counter: instrument.Counter, pjp: ProceedingJoinPoint): AnyRef = {
//    try pjp.proceed() finally counter.increment()
//  }
//
//  final def processMinMax(minMaxCounter: instrument.MinMaxCounter, pjp: ProceedingJoinPoint): AnyRef = {
//    minMaxCounter.increment()
//    try pjp.proceed() finally minMaxCounter.decrement()
//  }
//
//  private[annotation] def declaringType(signature: org.aspectj.lang.Signature) = signature.getDeclaringType
//}
//
//@Aspect
//class ClassToAnnotationInstrumentsMixin {
//  @DeclareMixin("(@kamon.annotation.api.EnableKamon *)")
//  def mixinClassToAnnotationInstruments: AnnotationInstruments = new AnnotationInstruments {}
//}
//
//trait AnnotationInstruments {
//  var traces: AtomicReferenceArray[TraceContextInfo] = _
//  var segments: AtomicReferenceArray[SegmentInfo] = _
//  var histograms: AtomicReferenceArray[instrument.Histogram] = _
//  var timeHistograms: AtomicReferenceArray[instrument.Histogram] = _
//  var counters: AtomicReferenceArray[Counter] = _
//  var minMaxCounters: AtomicReferenceArray[MinMaxCounter] = _
//}
//
//case class SegmentInfo(name: String, category: String, library: String, tags: Map[String, String])
//case class TraceContextInfo(name: String, tags: Map[String, String])

//abstract class StringEvaluator(val processor: ELProcessor) extends (String ⇒ String)

object StringEvaluator {
  def evaluate(obj: AnyRef)(str:String): String =
    ELProcessorFactory.withObject(obj).evalToString(str)

  def evaluate(clazz: Class[_])(str:String): String =
    ELProcessorFactory.withClass(clazz).evalToString(str)
}

object TagsEvaluator {
  def evaluate(obj:AnyRef)(str:String): Map[String, String] =
    ELProcessorFactory.withObject(obj).evalToMap(str)

  def eval(obj:AnyRef)(str:String): util.Map[String, String] = {
    import scala.collection.JavaConverters._
    evaluate(obj)(str).asJava
  }

  def evaluate(clazz: Class[_])(str:String): Map[String, String] =
    ELProcessorFactory.withClass(clazz).evalToMap(str)

}