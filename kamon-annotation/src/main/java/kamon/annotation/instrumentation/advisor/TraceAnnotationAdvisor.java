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

package kamon.annotation.instrumentation.advisor;

import kamon.Kamon;
import kamon.annotation.api.Trace;
import kamon.annotation.instrumentation.StringEvaluator;
import kamon.annotation.instrumentation.TagsEvaluator;
import kamon.context.Storage;
import kamon.trace.Span;
import kamon.trace.Tracer;
import kanela.agent.libs.net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;
import java.util.Map;

public class TraceAnnotationAdvisor {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void startSpan(@Advice.This Object obj,
                                 @Advice.Origin Method method,
                                 @Advice.Origin("#t") String className,
                                 @Advice.Origin("#m") String methodName,
                                 @Advice.Local("span") Span span,
                                 @Advice.Local("scope") Storage.Scope scope) {

        final Trace traceAnnotation = method.getAnnotation(Trace.class);
        final String evaluatedString = StringEvaluator.evaluate(obj, traceAnnotation.operationName());
        final String operationName = (evaluatedString.isEmpty() || evaluatedString.equals("unknown")) ? className + "." + methodName: evaluatedString;
        final Map<String, String> tags = TagsEvaluator.eval(obj, traceAnnotation.tags());
        final Tracer.SpanBuilder builder = Kamon.buildSpan(operationName);
        tags.forEach(builder::withTag);

        span = builder.start();
        scope = Kamon.storeContext(Kamon.currentContext().withKey(Span.ContextKey(), span));
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void stopSpan(@Advice.Local("span") Span span,
                                @Advice.Local("scope") Storage.Scope scope,
                                @Advice.Thrown Throwable throwable) {
        scope.close();
        if (throwable != null)
            span.addError(throwable.getMessage(), throwable);
        span.finish();
    }
}