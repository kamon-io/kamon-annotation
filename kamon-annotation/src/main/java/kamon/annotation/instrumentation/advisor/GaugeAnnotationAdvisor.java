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
import kamon.annotation.api.Gauge;
import kamon.annotation.instrumentation.StringEvaluator;
import kamon.annotation.instrumentation.TagsEvaluator;
import kanela.agent.libs.net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;
import java.util.Map;

public class GaugeAnnotationAdvisor {
    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void set(@Advice.This Object obj,
                           @Advice.Origin Method method,
                           @Advice.Origin("#t") String className,
                           @Advice.Origin("#m") String methodName,
                           @Advice.Return Object result) {

        final Gauge gaugeAnnotation = method.getAnnotation(Gauge.class);


        final String evaluatedString = StringEvaluator.evaluate(obj, gaugeAnnotation.name());
        final String name = (evaluatedString.isEmpty() || evaluatedString.equals("unknown")) ? className + "." + methodName: evaluatedString;
        final Map<String, String> tags = TagsEvaluator.eval(obj, gaugeAnnotation.tags());

        if(tags.isEmpty()) Kamon.gauge(name).set(((Number)result).longValue());
        else Kamon.gauge(name).refine(tags).set(((Number)result).longValue());
    }
}