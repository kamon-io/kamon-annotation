package kamon.annotation.instrumentation;

import kamon.annotation.instrumentation.advisor.TraceAnnotationAdvisor;
import kanela.agent.api.instrumentation.KanelaInstrumentation;

public class AnnotationInstrumentation extends KanelaInstrumentation {
    public AnnotationInstrumentation() {
        forTypesWithMethodsAnnotatedWith(() -> "kamon.annotation.api.Trace", (builder, annotatedMethods) ->
                builder
                    .withAdvisorFor(annotatedMethods, () -> TraceAnnotationAdvisor.class)
                    .build());
    }
}
