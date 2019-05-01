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

package com.lusidity.framework.cache.moneta;


import org.joda.time.DateTime;

public class CachedItem {
    private DateTime lastRead=null;
    private DateTime created = null;
    private Object value = null;

    public CachedItem(Object value)
    {
        super();
        this.lastRead = DateTime.now();
        this.created = DateTime.now();
        this.value = value;
    }

    public DateTime getLastRead() {
        return this.lastRead;
    }

    public void setLastRead(DateTime lastRead) {
        this.lastRead = lastRead;
    }

    public DateTime getCreated() {
        return this.created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void touch() {
        this.lastRead = DateTime.now();
    }
}
