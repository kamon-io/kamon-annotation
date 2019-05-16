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

package kamon.annotation.instrumentation.cache;

import kamon.Kamon;
import kamon.annotation.el.StringEvaluator;
import kamon.annotation.el.TagsEvaluator;
import kamon.metric.*;
import kamon.tag.TagSet;
import kamon.trace.SpanBuilder;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotationCache {
    private final static Map<MetricKey, Object> metrics = new ConcurrentHashMap<>();

    public static Gauge getGauge(Method method, Object obj, Class<?> clazz, String className, String methodName) {
        return (Gauge) metrics.computeIfAbsent(MetricKey.from("Gauge", method, obj, clazz), (key) -> {
            final kamon.annotation.api.Gauge gaugeAnnotation = method.getAnnotation(kamon.annotation.api.Gauge.class);
            final String name = getOperationName(gaugeAnnotation.name(), obj, clazz, className, methodName);
            final Map<String, Object> tags = getTags(obj, clazz, gaugeAnnotation.tags());

            if (tags.isEmpty()) return Kamon.gauge(name).withoutTags();
            return Kamon.gauge(name).withTags(TagSet.from(tags));
        });
    }

    public static Counter getCounter(Method method, Object obj, Class<?> clazz, String className, String methodName) {
        return (Counter) metrics.computeIfAbsent(MetricKey.from("Counter", method, obj, clazz), (key) -> {
            final kamon.annotation.api.Count countAnnotation = method.getAnnotation(kamon.annotation.api.Count.class);
            final String name = getOperationName(countAnnotation.name(), obj, clazz, className, methodName);
            final Map<String, Object> tags = getTags(obj, clazz, countAnnotation.tags());

            if(tags.isEmpty()) return Kamon.counter(name).withoutTags();
            return Kamon.counter(name).withTags(TagSet.from(tags));
        });
    }

    public static Histogram getHistogram(Method method, Object obj, Class<?> clazz, String className, String methodName) {
        return (Histogram) metrics.computeIfAbsent(MetricKey.from("Histogram", method, obj, clazz), (key) -> {
            final kamon.annotation.api.Histogram histogramAnnotation = method.getAnnotation(kamon.annotation.api.Histogram.class);
            final String name = getOperationName(histogramAnnotation.name(), obj, clazz, className, methodName);
            final Map<String, Object> tags = getTags(obj, clazz, histogramAnnotation.tags());

            final Metric.Histogram histogram = Kamon.histogram(name, MeasurementUnit.none(), new DynamicRange(histogramAnnotation.lowestDiscernibleValue(), histogramAnnotation.highestTrackableValue(), histogramAnnotation.precision()));

            if(tags.isEmpty()) return histogram.withoutTags();
            return histogram.withTags(TagSet.from(tags));
        });
    }

    public static RangeSampler getRangeSampler(Method method, Object obj, Class<?> clazz, String className, String methodName) {
        return (RangeSampler) metrics.computeIfAbsent(MetricKey.from("Sampler", method, obj, clazz), (key) -> {
            final kamon.annotation.api.RangeSampler rangeSamplerAnnotation = method.getAnnotation(kamon.annotation.api.RangeSampler.class);
            final String name = getOperationName(rangeSamplerAnnotation.name(), obj, clazz, className, methodName);
            final Map<String, Object> tags = getTags(obj, clazz, rangeSamplerAnnotation.tags());

            if(tags.isEmpty())return Kamon.rangeSampler(name).withoutTags();
            return Kamon.rangeSampler(name).withTags(TagSet.from(tags));
        });
    }

    public static Timer getTimer(Method method, Object obj, Class<?> clazz, String className, String methodName) {
        return (Timer) metrics.computeIfAbsent(MetricKey.from("Timer", method, obj, clazz), (key) -> {
            final kamon.annotation.api.Timer timeAnnotation = method.getAnnotation(kamon.annotation.api.Timer.class);
            final String name = getOperationName(timeAnnotation.name(), obj, clazz, className, methodName);
            final Map<String, Object> tags = getTags(obj, clazz, timeAnnotation.tags());


            if(tags.isEmpty()) return Kamon.timer(name).withoutTags();
            return Kamon.timer(name).withTags(TagSet.from(tags));
        });
    }

    public static SpanBuilder getSpanBuilder(Method method, Object obj, Class<?> clazz, String className, String methodName) {
        return (SpanBuilder) metrics.computeIfAbsent(MetricKey.from("Trace", method, obj, clazz), (key) -> {
            final kamon.annotation.api.Trace traceAnnotation = method.getAnnotation(kamon.annotation.api.Trace.class);
            final String operationName = getOperationName(traceAnnotation.operationName(), obj, clazz, className, methodName);
            final Map<String, Object> tags = getTags(obj, clazz, traceAnnotation.tags());
            final SpanBuilder builder = Kamon.spanBuilder(operationName);
            tags.forEach((k, v)-> builder.tag(k, v.toString()));

            return builder;
        });
    }

    private static  Map<String, Object> getTags(Object obj, Class<?> clazz, String tags) {
        final Map<String, String> tagMap = (obj != null) ? TagsEvaluator.eval(obj, tags) : TagsEvaluator.eval(clazz, tags);
        return Collections.unmodifiableMap(tagMap);
    }

    private static String getOperationName(String name, Object obj, Class<?> clazz, String className, String methodName) {
        final String evaluatedString = (obj != null) ? StringEvaluator.evaluate(obj, name) : StringEvaluator.evaluate(clazz, name);
        return (evaluatedString.isEmpty() || evaluatedString.equals("unknown")) ? className + "." + methodName: evaluatedString;
    }

    private static class MetricKey {
        private final String prefix;
        private final Method method;
        private final Object instance;
        private final Class<?> clazz;

        private  MetricKey(String prefix, Method method, Object instance, Class<?> clazz) {
            this.prefix = prefix;
            this.method = method;
            this.instance = instance;
            this.clazz = clazz;
        }

        public static MetricKey from(String prefix, Method method, Object instance, Class<?> clazz){
            return new MetricKey(prefix, method, instance, clazz);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final MetricKey metricKey = (MetricKey) o;
            return Objects.equals(prefix, metricKey.prefix) &&
                   Objects.equals(method, metricKey.method) &&
                   Objects.equals(instance, metricKey.instance) &&
                   Objects.equals(clazz, metricKey.clazz);
        }

        @Override
        public int hashCode() {
            return Objects.hash(prefix, method, instance, clazz);
        }
    }
}
