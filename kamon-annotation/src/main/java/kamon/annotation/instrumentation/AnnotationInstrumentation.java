package kamon.annotation.instrumentation;

import kamon.annotation.instrumentation.advisor.CountAnnotationAdvisor;
import kamon.annotation.instrumentation.advisor.RangeSamplerAnnotationAdvisor;
import kamon.annotation.instrumentation.advisor.TraceAnnotationAdvisor;
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
    }
}
