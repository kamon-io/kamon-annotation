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
import kamon.annotation.api.Histogram;
import kamon.annotation.instrumentation.StringEvaluator;
import kamon.annotation.instrumentation.TagsEvaluator;
import kamon.metric.DynamicRange;
import kamon.metric.HistogramMetric;
import kamon.metric.MeasurementUnit;
import kanela.agent.libs.net.bytebuddy.asm.Advice;
import scala.Some;
import scala.collection.immutable.Map;

import java.lang.reflect.Method;

public class HistogramAnnotationAdvisor {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void create(@Advice.This Object obj,
                              @Advice.Origin Method method,
                              @Advice.Origin("#t") String className,
                              @Advice.Origin("#m") String methodName,
                              @Advice.Local("histogram") kamon.metric.HistogramMetric histogram) {

        final Histogram histogramAnnotation = method.getAnnotation(Histogram.class);

        final String evaluatedString = StringEvaluator.evaluate(obj, histogramAnnotation.name());
        final String name = (evaluatedString.isEmpty() || evaluatedString.equals("unknown")) ? className + "." + methodName: evaluatedString;
        final Map<String, String> tags = TagsEvaluator.evaluate(obj, histogramAnnotation.tags());

        histogram = Kamon.histogram(name, MeasurementUnit.none(), new Some<>(new DynamicRange(histogramAnnotation.lowestDiscernibleValue(), histogramAnnotation.highestTrackableValue(), histogramAnnotation.precision())));

        if(tags.nonEmpty()) histogram = (HistogramMetric) histogram.refine(tags);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void record(@Advice.Local("histogram") kamon.metric.HistogramMetric histogram,
                              @Advice.Return Object result) {

        histogram.record(((Number)result).longValue());
    }
}