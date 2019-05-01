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

package com.lusidity.security.data.filters;

import com.lusidity.Environment;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.system.security.UserCredentials;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

public class PrincipalFilterCache implements Closeable
{
	private static final int DEFAULT_HEAP=100000;
	private List<Class<? extends IFilterHandler>> handlers = new ArrayList<>();
	@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
	private Map<String, PrincipalFilterItems> cache = null;

	public PrincipalFilterCache(){
		super();
		this.init();
	}

	public synchronized void addHandler(Class<? extends IFilterHandler> cls){
		if(!this.handlers.contains(cls)){
			this.handlers.add(cls);
		}
	}

	public synchronized boolean removeHandler(Class<? extends IFilterHandler> cls){
		return this.handlers.remove(cls);
	}

	public synchronized void clearHandlers(){
		this.handlers.clear();
	}

	private void init()
	{
		this.cache = new HashMap<>(PrincipalFilterCache.DEFAULT_HEAP);
	}

	public synchronized void add(String key, UserCredentials userCredentials, Class<? extends BasePrincipalFilter> cls, Collection<? extends BasePrincipalFilter> filters, IFilterHandler handler){
		 this.add(key, userCredentials.getPrincipal(), cls, filters, handler);
	}

	public synchronized void add(String key, BasePrincipal basePrincipal, Class<? extends BasePrincipalFilter> cls, Collection<? extends BasePrincipalFilter> filters, IFilterHandler handler)
	{
		PrincipalFilterItems items = this.cache.get(basePrincipal.fetchId().getValue());
		if(null==items){
			items = new PrincipalFilterItems();
			this.cache.put(basePrincipal.fetchId().getValue(), items);
		}
		items.put(key, new PrincipalFilterItem(key, basePrincipal, cls, filters));
		if (null!=handler)
		{
			handler.handle(IFilterHandler.Action.add, basePrincipal);
		}
	}

	public PrincipalFilterItem get(BasePrincipal basePrincipal, String key)
	{
		PrincipalFilterItem result = null;
		PrincipalFilterItems items = this.cache.get(basePrincipal.fetchId().getValue());
		if(null!=items){
			result = items.get(key);
		}
		return result;
	}

	public synchronized void remove(BasePrincipal basePrincipal){
		this.cache.remove(basePrincipal.fetchId().getValue());
	}

	public synchronized PrincipalFilterItem remove(BasePrincipal basePrincipal, String key)
	{
		PrincipalFilterItem result = null;
		PrincipalFilterItems items = this.cache.get(basePrincipal.fetchId().getValue());
		if(null!=items){
			result = items.remove(key);
		}
		return result;
	}

	public synchronized void reset(BasePrincipal basePrincipal, IFilterHandler handler){
		this.cache.remove(basePrincipal.fetchId().getValue());
		if(null!=handler){
			handler.handle(IFilterHandler.Action.remove, basePrincipal);
		}
		for(Class<? extends IFilterHandler> cls: this.handlers){
			try{
				Constructor constructor = cls.getConstructor();
				IFilterHandler ifh =(IFilterHandler) constructor.newInstance();
				ifh.handle(IFilterHandler.Action.remove, basePrincipal);
			}
			catch (Exception ex){
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
	}

	@SuppressWarnings("unused")
	public String makeKey(BasePrincipal basePrincipal, String... keys){
		StringBuilder sb = new StringBuilder();
		sb.append(basePrincipal.fetchId().getValue());
		if(null!=keys){
			for(String key: keys){
				sb.append("_").append(key);
			}
		}
		return sb.toString();
	}

	@Override
	public void close()
		throws IOException
	{
		this.cache = null;
	}
}
