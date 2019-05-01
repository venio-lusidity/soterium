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

import com.lusidity.framework.cache.CacheLoader;
import com.lusidity.framework.cache.CacheProvider;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.math.MathX;
import com.lusidity.framework.text.StringX;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.ReadableInterval;

import java.util.*;

public class Moneta implements CacheProvider {

    private CacheStats cacheStats = null;
    private static final int CLEAN_INTERVAL = ((MathX.HOURS_IN_DAY/4) * MathX.MINUTES_IN_HOUR); //minutes
    private static final int MAX_ELAPSED = (MathX.HOURS_IN_DAY * MathX.MINUTES_IN_HOUR); //minutes
    @SuppressWarnings("FieldCanBeLocal")
    private boolean cleaning = false;
    private DateTime lastCleaned = null;

    private static
    Object getObjectLock()
    {
        return LockHolder.LOCK;
    }

    @SuppressWarnings("UtilityClassWithoutPrivateConstructor")
    private static
    class LockHolder
    {
        private static final Object LOCK = new Object();
    }

    public Moneta()
    {
        super();
        this.cacheStats = new CacheStats();
        this.lastCleaned = DateTime.now();
    }

    protected static Map<String, CachedItem> getCache()
    {
        return InstanceCache.CACHE;
    }

    private static
    class InstanceCache
    {
        private static final Map<String, CachedItem> CACHE = new HashMap<>();
    }

    @Override
    public Object get(String key, CacheLoader loader) throws ApplicationException {
        Object result = null;

        if(!StringX.isBlank(key))
        {
            this.cacheStats.incrementTotalGetAttempts();
            if(Moneta.getCache().containsKey(key))
            {
                CachedItem cachedItem = Moneta.getCache().get(key);
                cachedItem.touch();
                this.cacheStats.incrementTotalCachedHits();
                result = cachedItem.getValue();
            }
            else if(null != loader)
            {
                result = loader.load(key);
                if(null!=result) {
                    this.cacheStats.incrementTotalLoaded();
                    this.cacheStats.incrementTotalCacheMissed();
                    this.put(key, result);
                }
            }
            else
            {
                this.cacheStats.incrementTotalCacheMissed();
            }
        }

        return result;
    }

    @Override
    public <T> void put(String key, T value) throws ApplicationException {
        if(!StringX.isBlank(key))
        {
            if(Moneta.getCache().containsKey(key))
            {
                this.invalidate(key);
            }

            this.cacheStats.incrementTotalWrites();
            CachedItem cachedItem = new CachedItem(value);
            Moneta.getCache().put(key, cachedItem);

            try
            {
                Thread thread = new Thread(new CleanUp(this));
                thread.start();
            }
            catch (Exception ex)
            {
                throw new ApplicationException(ex);
            }
        }
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
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
    
    public class CleanUp implements Runnable
    {
        private final Moneta moneta;

        public CleanUp(Moneta moneta) {
            super();
            this.moneta = moneta;
        }
        @Override
        public void run() {
            this.moneta.cleanUp();
        }
    }

    @Override
    public void invalidate(String key) {
       if(!StringX.isBlank(key))
       {
           Moneta.getCache().remove(key);
       }
    }

    @Override
    public void open() {
        // there is nothing to do here.
    }

    @Override
    public void close() {
        Moneta.getCache().clear();
    }

    @Override
    public void cleanUp() {
        synchronized (Moneta.getSemaphore())
        {
            DateTime end = DateTime.now();
            ReadableInterval interval = new Interval(this.lastCleaned, end);
            Duration duration = interval.toDuration();

            if((duration.getStandardMinutes() > Moneta.CLEAN_INTERVAL) && !this.cleaning)
            {
                this.cleaning = true;
                Collection<String> trash = new ArrayList<>();
                for(Map.Entry<String, CachedItem> entry: Moneta.getCache().entrySet())
                {
                    CachedItem cachedItem = entry.getValue();

                    if(Moneta.clean(cachedItem, end))
                    {
                        trash.add(entry.getKey());
                    }
                }

                for(String key: trash)
                {
                    Moneta.getCache().remove(key);
                }

                this.lastCleaned = DateTime.now();
                this.cleaning = false;
            }
        }
    }

    private static boolean clean(CachedItem cachedItem, DateTime end) {
        ReadableInterval interval = new Interval(cachedItem.getCreated(), end);
        Duration duration = interval.toDuration();
        return (duration.getStandardMinutes() > Moneta.MAX_ELAPSED);
    }

    @Override
    public JsonData getStats() {
        this.cacheStats.setTotalCached(Moneta.getCache().size());

        @SuppressWarnings("CollectionDeclaredAsConcreteClass")
        LinkedHashMap<String, Object> hitRate = Moneta.create(String.format("%.1f%%", (this.cacheStats.hitRate() * 100)), "Cache Hit Rate");
        @SuppressWarnings("CollectionDeclaredAsConcreteClass")
        LinkedHashMap<String, Object> totalCached = Moneta.create(String.format("%d", this.cacheStats.getTotalCached()), "Total Cached");

        JsonData cache = JsonData.createObject();
        cache.put("hit_rate", hitRate);
        cache.put("total_cached", totalCached);

        return cache;
    }

    private static
    LinkedHashMap<String, Object> create(Object value, String label)
    {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("value", value);
        result.put("label", label);
        return result;
    }

    @Override
    public Object getLock() {
        return Moneta.getObjectLock();  //To change body of implemented methods use File | Settings | File Templates.
    }
}
