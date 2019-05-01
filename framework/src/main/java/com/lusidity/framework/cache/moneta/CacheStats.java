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


import java.text.DecimalFormat;

public class CacheStats {
    private int totalGetAttempts =0;
    private int totalLoaded = 0;
    private int totalCachedHits = 0;
    private int totalCachedMissed = 0;
    private int totalWrites = 0;
    private int totalCached = 0;

    protected int getTotalGetAttempts() {
        return this.totalGetAttempts;
    }

    protected void incrementTotalGetAttempts() {
        synchronized (CacheStats.getSemaphore())
        {
            this.totalGetAttempts++;
        }
    }

    public int getTotalCachedHits() {
        return this.totalCachedHits;
    }

    protected void incrementTotalCachedHits() {
        synchronized (CacheStats.getSemaphore())
        {
            this.totalCachedHits++;
        }
    }

    public int getTotalCachedMissed() {
        return this.totalCachedMissed;
    }

    protected void incrementTotalCacheMissed() {
        synchronized (CacheStats.getSemaphore())
        {
            this.totalCachedMissed++;
        }
    }

    public int getTotalWrites() {
        return this.totalWrites;
    }

    protected void incrementTotalWrites() {
        synchronized (CacheStats.getSemaphore())
        {
            this.totalWrites++;
        }
    }

    public int getTotalLoaded() {
        return this.totalLoaded;
    }

    protected void incrementTotalLoaded() {
        synchronized (CacheStats.getSemaphore())
        {
            this.totalLoaded++;
        }
    }

    public double hitRate()
    {
        int dividend = this.totalCachedHits;
        int divisor = this.totalGetAttempts;
        @SuppressWarnings("UnnecessaryLocalVariable")
        Float result = (dividend > 0) ? ((float)dividend / divisor) : 0f;
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.valueOf(df.format(result));
    }

    public double missRate()
    {
        int dividend = this.totalCachedMissed;
        int divisor = this.totalGetAttempts;
        @SuppressWarnings("UnnecessaryLocalVariable")
        Float result = (dividend > 0) ? ((float)dividend / divisor) : 0f;
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.valueOf(df.format(result));
    }

    public int getTotalCached() {
        return this.totalCached;
    }

    protected void setTotalCached(int totalCached) {
        this.totalCached = totalCached;
    }

    private static
    Object getSemaphore()
    {
        return ObjectHolder.SEMAPHORE;
    }

    @SuppressWarnings("UtilityClassWithoutPrivateConstructor")
    private static
    class ObjectHolder
    {
        private static final Object SEMAPHORE = new Object();
    }
}
