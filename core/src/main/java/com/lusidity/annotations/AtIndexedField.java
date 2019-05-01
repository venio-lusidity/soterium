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

package com.lusidity.annotations;

import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AtProperties.class)
public
@interface AtIndexedField
{
    /**
     * Property key.
     *
     * @return Property key.
     */
    String key();

    /**
     * Data type.
     * If you set the data type to Object.class.  It will not be indexable and should be a UTF-8 encoded string.
     * If not set to a UTF-8 encoded string the data store may complain about mismatched data types.
     * @return Data type.
     */
    Class<?> type();

    String regex() default "";

    /**
     * Data type to use for indexing.
     *
     * @return Indexing data type.
     */
    Class indexedAs() default Object.class;

    /**
     * Indexing analyzers to use.
     * @return Array of indexing analyzers.
     */
    BaseQueryBuilder.StringTypes[] analyzers() default {BaseQueryBuilder.StringTypes.na};

    /**
     * Used only when the type is specified as Object.  This object may contain multiple types and could cause poor performance when indexing.
     */
    boolean indexable() default true;
}
