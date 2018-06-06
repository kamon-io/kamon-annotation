/*
 * =========================================================================================
 * Copyright © 2013-2018 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law withReturnValues agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express withReturnValues implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package kamon.annotation.instrumentation;

import kamon.annotation.instrumentation.advisor.*;
import kanela.agent.api.instrumentation.KanelaInstrumentation;

public class AnnotationInstrumentation extends KanelaInstrumentation {
    public AnnotationInstrumentation() {
        forTypesWithMethodsAnnotatedWith(() -> "kamon.annotation.api.Trace", (builder, annotatedMethods) ->
                builder
                    .withAdvisorFor(annotatedMethods, () -> TraceAnnotationAdvisor.class)
                    .build());

        forTypesWithMethodsAnnotatedWith(() -> "kamon.annotation.api.Count", (builder, annotatedMethods) ->
                builder
                    .withAdvisorFor(annotatedMethods, () -> CountAnnotationAdvisor.class)
                    .build());

        forTypesWithMethodsAnnotatedWith(() -> "kamon.annotation.api.RangeSampler", (builder, annotatedMethods) ->
                builder
                    .withAdvisorFor(annotatedMethods, () -> RangeSamplerAnnotationAdvisor.class)
                    .build());

        forTypesWithMethodsAnnotatedWith(() -> "kamon.annotation.api.Timer", (builder, annotatedMethods) ->
                builder
                    .withAdvisorFor(annotatedMethods, () -> TimerAnnotationAdvisor.class)
                    .build());

        forTypesWithMethodsAnnotatedWith(() -> "kamon.annotation.api.Histogram", (builder, annotatedMethods) ->
                builder
                    .withAdvisorFor(annotatedMethods.and(withReturnTypes(long.class, double.class, int.class, float.class)), () -> HistogramAnnotationAdvisor.class)
                    .build());

        forTypesWithMethodsAnnotatedWith(() -> "kamon.annotation.api.Gauge", (builder, annotatedMethods) ->
                builder
                    .withAdvisorFor(annotatedMethods.and(withReturnTypes(long.class, double.class, int.class, float.class)), () -> GaugeAnnotationAdvisor.class)
                    .build());
    }
}
