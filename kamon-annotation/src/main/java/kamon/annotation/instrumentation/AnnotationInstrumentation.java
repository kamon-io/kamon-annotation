/*
 * =========================================================================================
 * Copyright © 2013-2019 the kamon project <http://kamon.io/>
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
import kanela.agent.api.instrumentation.InstrumentationBuilder;

import static kanela.agent.libs.net.bytebuddy.matcher.ElementMatchers.*;

public class AnnotationInstrumentation extends InstrumentationBuilder {
    public AnnotationInstrumentation() {

        onTypesWithMethodsAnnotatedWith("kamon.annotation.api.Trace")
                .advise(isAnnotatedWith(named("kamon.annotation.api.Trace")), TraceAnnotationAdvisor.class);

        onTypesWithMethodsAnnotatedWith("kamon.annotation.api.SpanCustomizer")
                .advise(isAnnotatedWith(named("kamon.annotation.api.SpanCustomizer")), SpanCustomizerAnnotationAdvisor.class);

        onTypesWithMethodsAnnotatedWith("kamon.annotation.api.Count")
                .advise(isAnnotatedWith(named("kamon.annotation.api.Count")), CountAnnotationAdvisor.class);

        onTypesWithMethodsAnnotatedWith("kamon.annotation.api.RangeSampler")
                .advise(isAnnotatedWith(named("kamon.annotation.api.RangeSampler")), RangeSamplerAnnotationAdvisor.class);

        onTypesWithMethodsAnnotatedWith("kamon.annotation.api.Timer")
                .advise(isAnnotatedWith(named("kamon.annotation.api.Timer")), TimerAnnotationAdvisor.class);

        onTypesWithMethodsAnnotatedWith("kamon.annotation.api.Histogram")
                .advise(isAnnotatedWith(named("kamon.annotation.api.Histogram")).and(withReturnTypes(long.class, double.class, int.class, float.class)),HistogramAnnotationAdvisor.class);

        onTypesWithMethodsAnnotatedWith("kamon.annotation.api.Gauge")
                .advise(isAnnotatedWith(named("kamon.annotation.api.Gauge")).and(withReturnTypes(long.class, double.class, int.class, float.class)), GaugeAnnotationAdvisor.class);
    }
}
