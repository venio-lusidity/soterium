/*
 * Copyright 2018 lusidity inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.lusidity.framework.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AtWebResource
{
    public static final int MODE_BEST_MATCH = 1;
    public static final int MODE_FIRST_MATCH = 2;
    public static final int MODE_LAST_MATCH = 3;
    public static final int MODE_NEXT_MATCH = 4;
    public static final int MODE_RANDOM_MATCH = 5;
    public static final int MODE_CUSTOM = 6;

    String pathTemplate();
    int matchingMode();
    boolean indexed() default true;

    String methods();
    String requiredParams() default "";
    String optionalParams() default "";
    String bodyFormat() default "";
    String description();
}