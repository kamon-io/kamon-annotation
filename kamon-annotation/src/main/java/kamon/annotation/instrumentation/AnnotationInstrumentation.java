package kamon.annotation.instrumentation;

import kamon.annotation.instrumentation.advisor.CountAnnotationAdvisor;
import kamon.annotation.instrumentation.advisor.TraceAnnotationAdvisor2;
import kanela.agent.api.instrumentation.KanelaInstrumentation;

public class AnnotationInstrumentation extends KanelaInstrumentation {
    public AnnotationInstrumentation() {
        forTypesWithMethodsAnnotatedWith(() -> "kamon.annotation.api.Trace", (builder, annotatedMethods) ->
                builder
//                    .withAdvisorFor(annotatedMethods, () -> TraceAnnotationAdvisor.class)
                    .withAdvisorFor(annotatedMethods, () -> TraceAnnotationAdvisor2.class)
                    .build());

        forTypesWithMethodsAnnotatedWith(() -> "kamon.annotation.api.Count", (builder, annotatedMethods) ->
                builder
                    .withAdvisorFor(annotatedMethods, () -> CountAnnotationAdvisor.class)
                    .build());
    }
}
