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
import kamon.annotation.api.RangeSampler;
import kamon.annotation.instrumentation.StringEvaluator;
import kamon.annotation.instrumentation.TagsEvaluator;
import kamon.metric.RangeSamplerMetric;
import kanela.agent.libs.net.bytebuddy.asm.Advice;
import scala.collection.immutable.Map;

import java.lang.reflect.Method;

public class RangeSamplerAnnotationAdvisor {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void increment(@Advice.This Object obj,
                                 @Advice.Origin Method method,
                                 @Advice.Origin("#t") String className,
                                 @Advice.Origin("#m") String methodName,
                                 @Advice.Local("rangeSampler") kamon.metric.RangeSamplerMetric sampler) {

        final RangeSampler rangeSamplerAnnotation = method.getAnnotation(RangeSampler.class);
        final String evaluatedString = StringEvaluator.evaluate(obj, rangeSamplerAnnotation.name());
        final String name = (evaluatedString.isEmpty() || evaluatedString.equals("unknown")) ? className + "." + methodName: evaluatedString;
        final Map<String, String> tags = TagsEvaluator.evaluate(obj, rangeSamplerAnnotation.tags());

        sampler = Kamon.rangeSampler(name);
        if(tags.nonEmpty()) sampler = (RangeSamplerMetric) sampler.refine(tags);
        sampler.increment();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void decrement(@Advice.Local("rangeSampler") kamon.metric.RangeSamplerMetric sampler) {
        sampler.decrement();
    }
}