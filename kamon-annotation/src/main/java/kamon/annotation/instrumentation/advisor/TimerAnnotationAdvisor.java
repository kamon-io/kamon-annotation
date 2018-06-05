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

import kamon.metric.Timer;
import kanela.agent.libs.net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;

public class TimerAnnotationAdvisor {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void start(@Advice.This Object obj,
                             @Advice.Origin Method method,
                             @Advice.Origin("#t") String className,
                             @Advice.Origin("#m") String methodName,
                             @Advice.Local("startedTimer") kamon.metric.StartedTimer startedTimer) {

        final Timer timer = AnnotationCache.getTimer(method, obj, className, methodName);
        startedTimer = timer.start();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void end(@Advice.Local("startedTimer") kamon.metric.StartedTimer startedTimer) {
        startedTimer.stop();
    }
}