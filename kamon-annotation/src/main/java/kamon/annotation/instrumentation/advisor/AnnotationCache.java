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

import joptsimple.internal.Strings;
import kamon.Kamon;
import kamon.annotation.instrumentation.StringEvaluator;
import kamon.annotation.instrumentation.TagsEvaluator;
import kamon.metric.*;
import kamon.trace.Tracer;
import scala.Some;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AnnotationCache {
    private final static Map<String, Object> metrics = new ConcurrentHashMap<>();

    public static Gauge getGauge(Method method, Object obj, String className, String methodName) {
        return (Gauge) metrics.computeIfAbsent(getKey("Gauge", method), (key) -> {
            final kamon.annotation.api.Gauge gaugeAnnotation = method.getAnnotation(kamon.annotation.api.Gauge.class);
            final String name = getOperationName(gaugeAnnotation.name(), obj, className, methodName);
            final Map<String, String> tags = TagsEvaluator.eval(obj, gaugeAnnotation.tags());

            if (tags.isEmpty()) return Kamon.gauge(name);
            else return Kamon.gauge(name).refine(tags);
        });
    }

    public static Counter getCounter(Method method, Object obj, String className, String methodName) {
        return (Counter) metrics.computeIfAbsent(getKey("Counter", method), (key) -> {
            final kamon.annotation.api.Count countAnnotation = method.getAnnotation(kamon.annotation.api.Count.class);
            final String name = getOperationName(countAnnotation.name(), obj, className, methodName);
            final Map<String, String> tags = TagsEvaluator.eval(obj, countAnnotation.tags());

            if(tags.isEmpty()) return Kamon.counter(name);
            else return Kamon.counter(name).refine(tags);
        });
    }


    public static Histogram getHistogram(Method method, Object obj, String className, String methodName) {
        return (Histogram) metrics.computeIfAbsent(getKey("Histogram", method), (key) -> {
            final kamon.annotation.api.Histogram histogramAnnotation = method.getAnnotation(kamon.annotation.api.Histogram.class);
            final String name = getOperationName(histogramAnnotation.name(), obj, className, methodName);
            final Map<String, String> tags = TagsEvaluator.eval(obj, histogramAnnotation.tags());

            final HistogramMetric histogram = Kamon.histogram(name, MeasurementUnit.none(), new Some<>(new DynamicRange(histogramAnnotation.lowestDiscernibleValue(), histogramAnnotation.highestTrackableValue(), histogramAnnotation.precision())));

            if(tags.isEmpty()) return histogram;
            else return histogram.refine(tags);
        });
    }

    public static RangeSampler getRangeSampler(Method method, Object obj, String className, String methodName) {
        return (RangeSampler) metrics.computeIfAbsent(getKey("Sampler", method), (key) -> {
            final kamon.annotation.api.RangeSampler rangeSamplerAnnotation = method.getAnnotation(kamon.annotation.api.RangeSampler.class);
            final String name = getOperationName(rangeSamplerAnnotation.name(), obj, className, methodName);
            final Map<String, String> tags = TagsEvaluator.eval(obj, rangeSamplerAnnotation.tags());

            if(tags.isEmpty())return Kamon.rangeSampler(name);
            else return Kamon.rangeSampler(name).refine(tags);
        });
    }

    public static Timer getTimer(Method method, Object obj, String className, String methodName) {
        return (Timer) metrics.computeIfAbsent(getKey("Timer", method), (key) -> {
            final kamon.annotation.api.Timer timeAnnotation = method.getAnnotation(kamon.annotation.api.Timer.class);
            final String name = getOperationName(timeAnnotation.name(), obj, className, methodName);
            final Map<String, String> tags = TagsEvaluator.eval(obj, timeAnnotation.tags());

            if(tags.isEmpty()) return Kamon.timer(name);
            else return Kamon.timer(name).refine(tags);
        });
    }

    public static Tracer.SpanBuilder getSpanBuilder(Method method, Object obj, String className, String methodName) {
        return (Tracer.SpanBuilder) metrics.computeIfAbsent(getKey("Span", method), (key) -> {
            final kamon.annotation.api.Trace traceAnnotation = method.getAnnotation(kamon.annotation.api.Trace.class);
            final String operationName = getOperationName(traceAnnotation.operationName(), obj, className, methodName);
            final Map<String, String> tags = TagsEvaluator.eval(obj, traceAnnotation.tags());
            final Tracer.SpanBuilder builder = Kamon.buildSpan(operationName);
            tags.forEach(builder::withTag);

            return builder;
        });
    }

    private static String getOperationName(String name, Object obj, String className, String methodName) {
        final String evaluatedString = StringEvaluator.evaluate(obj, name);
        return (evaluatedString.isEmpty() || evaluatedString.equals("unknown")) ? className + "." + methodName: evaluatedString;
    }

    private static String getKey(String prefix, Method method) {
        final String methodName = method.getName();
        final String parameterCount = String.valueOf(method.getParameterCount());
        final String parameterTypes = Strings.join(Arrays.stream(method.getParameterTypes()).map(Class::toString).collect(Collectors.toList()), ":");
        final String returnType = method.getReturnType().toString();

        String s = prefix + "|" + methodName + "|" + parameterCount + "|" + parameterTypes + "|" + returnType;
        System.out.println(s);
        return s;
    }
}
