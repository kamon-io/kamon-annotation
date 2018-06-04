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
import kamon.annotation.api.Timer;
import kamon.annotation.instrumentation.StringEvaluator;
import kamon.annotation.instrumentation.TagsEvaluator;
import kanela.agent.libs.net.bytebuddy.asm.Advice;
import scala.collection.immutable.Map;

import java.lang.reflect.Method;

public class TimerAnnotationAdvisor {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void start(@Advice.This Object obj,
                             @Advice.Origin Method method,
                             @Advice.Origin("#t") String className,
                             @Advice.Origin("#m") String methodName,
                             @Advice.Local("startedTimer") kamon.metric.StartedTimer timer) {

        final Timer timeAnnotation = method.getAnnotation(Timer.class);

        final String evaluatedString = StringEvaluator.evaluate(obj, timeAnnotation.name());
        final String name = (evaluatedString.isEmpty() || evaluatedString.equals("unknown")) ? className + "." + methodName: evaluatedString;
        final Map<String, String> tags = TagsEvaluator.evaluate(obj, timeAnnotation.tags());

        if(tags.isEmpty()) timer = Kamon.timer(name).start();
        else timer = Kamon.timer(name).refine(tags).start();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void end(@Advice.Local("startedTimer") kamon.metric.StartedTimer timer) {
        timer.stop();
    }
}