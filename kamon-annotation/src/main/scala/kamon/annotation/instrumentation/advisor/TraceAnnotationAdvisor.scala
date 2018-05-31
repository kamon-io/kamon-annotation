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

package kamon.annotation.instrumentation.advisor

import java.lang.reflect.Method

import kamon.Kamon
import kamon.annotation.api.Trace
import kamon.annotation.instrumentation.{StringEvaluator, TagsEvaluator}
import kamon.context.Storage
import kamon.trace.Span
import kanela.agent.libs.net.bytebuddy.asm.Advice

class TraceAnnotationAdvisor
object TraceAnnotationAdvisor {
  @Advice.OnMethodEnter(suppress = classOf[Throwable])
  def startSpan(@Advice.Origin obj: Object,
                @Advice.Origin method: Method,
                @Advice.Origin("#t") className: String,
                @Advice.Origin("#m") methodName: String): (Storage.Scope, Span) = {

    val traceAnnotation = method.getAnnotation(classOf[Trace])
    val operationName = if (traceAnnotation.operationName().nonEmpty) StringEvaluator(obj)(traceAnnotation.operationName()) else s"$className.$methodName"
    val tags = TagsEvaluator(obj)(traceAnnotation.tags())

    val builder = Kamon.buildSpan(operationName)
    tags.foreach { case (key, value) => builder.withTag(key, value) }

    val span = builder.start()
    val scope = Kamon.storeContext(Kamon.currentContext().withKey(Span.ContextKey, span))
    (scope, span)
  }

  @Advice.OnMethodExit(onThrowable = classOf[Throwable], suppress = classOf[Throwable])
  def stopSpan(@Advice.Enter traveler: (Storage.Scope, Span),
               @Advice.Thrown throwable: Throwable): Unit = {
    val (scope, span) = traveler
    scope.close()
    if (throwable != null)
      span.addError(throwable.getMessage, throwable)
    span.finish()
  }
}

