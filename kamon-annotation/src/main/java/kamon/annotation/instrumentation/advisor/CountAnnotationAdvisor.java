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

import kamon.annotation.instrumentation.cache.AnnotationCache;
import kamon.metric.Counter;
import kanela.agent.libs.net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;

public final class CountAnnotationAdvisor {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void count(@Advice.This(optional = true) Object obj,
                             @Advice.Origin Class<?> clazz,
                             @Advice.Origin Method method   ,
                             @Advice.Origin("#t") String className,
                             @Advice.Origin("#m") String methodName) {

        final Counter counter = AnnotationCache.getCounter(method, obj, clazz, className, methodName);
        counter.increment();
    }
}