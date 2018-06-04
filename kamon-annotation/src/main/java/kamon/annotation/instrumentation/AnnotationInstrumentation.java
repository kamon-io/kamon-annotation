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

package kamon.annotation.instrumentation;

import kamon.annotation.instrumentation.advisor.*;
import kanela.agent.api.instrumentation.KanelaInstrumentation;

import static kanela.agent.libs.net.bytebuddy.matcher.ElementMatchers.returns;

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

        forTypesWithMethodsAnnotatedWith(() -> "kamon.annotation.api.Time", (builder, annotatedMethods) ->
                builder
                    .withAdvisorFor(annotatedMethods, () -> TimeAnnotationAdvisor.class)
                    .build());

        forTypesWithMethodsAnnotatedWith(() -> "kamon.annotation.api.Histogram", (builder, annotatedMethods) ->
                builder
                    .withAdvisorFor(annotatedMethods.and(returns(long.class).or(returns(double.class).or(returns(int.class).or(returns(float.class))))), () -> HistogramAnnotationAdvisor.class)
                    .build());
    }
}
