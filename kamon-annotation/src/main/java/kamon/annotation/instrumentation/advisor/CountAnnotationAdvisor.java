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
import kamon.annotation.api.Count;
import kamon.annotation.instrumentation.StringEvaluator;
import kamon.annotation.instrumentation.TagsEvaluator;
import kanela.agent.libs.net.bytebuddy.asm.Advice;
import scala.collection.immutable.Map;

import java.lang.reflect.Method;

public class CountAnnotationAdvisor {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void count(@Advice.This Object obj,
                             @Advice.Origin Method method,
                             @Advice.Origin("#t") String className,
                             @Advice.Origin("#m") String methodName) {

        final Count countAnnotation = method.getAnnotation(Count.class);
        final String evaluatedString = StringEvaluator.evaluate(obj, countAnnotation.name());
        final String name = (evaluatedString.isEmpty() || evaluatedString.equals("unknown")) ? className + "." + methodName: evaluatedString;
        final Map<String, String> tags = TagsEvaluator.evaluate(obj, countAnnotation.tags());

        System.out.println(Kamon.counter(name).refine(tags));
    }
}