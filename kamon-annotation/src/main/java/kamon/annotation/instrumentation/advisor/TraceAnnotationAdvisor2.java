package kamon.annotation.instrumentation.advisor;

import kamon.Kamon;
import kamon.annotation.api.Trace;
import kamon.annotation.instrumentation.StringEvaluator;
import kamon.annotation.instrumentation.TagsEvaluator;
import kamon.context.Storage;
import kamon.trace.Span;
import kamon.trace.Tracer;
import kanela.agent.libs.net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;
import java.util.Map;

public class TraceAnnotationAdvisor2 {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void startSpan(@Advice.This Object obj,
                                 @Advice.Origin Method method,
                                 @Advice.Origin("#t") String className,
                                 @Advice.Origin("#m") String methodName,
                                 @Advice.Local("span") Span span,
                                 @Advice.Local("scope") Storage.Scope scope) {

        final Trace traceAnnotation = method.getAnnotation(Trace.class);
        final String evaluatedString = StringEvaluator.evaluate(obj, traceAnnotation.operationName());
        final String operationName = (evaluatedString.isEmpty() || evaluatedString.equals("unknown")) ? className + "." + methodName: evaluatedString;
        final Map<String, String> tags = TagsEvaluator.eval(obj, traceAnnotation.tags());
        final Tracer.SpanBuilder builder = Kamon.buildSpan(operationName);
        tags.forEach(builder::withTag);

        span = builder.start();
        scope = Kamon.storeContext(Kamon.currentContext().withKey(Span.ContextKey(), span));
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void stopSpan(@Advice.Local("span") Span span,
                                @Advice.Local("scope") Storage.Scope scope,
                                @Advice.Thrown Throwable throwable) {
        scope.close();
        if (throwable != null)
            span.addError(throwable.getMessage(), throwable);
        span.finish();
    }
}