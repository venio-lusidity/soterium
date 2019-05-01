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

package com.lusidity.services;

import com.lusidity.Environment;
import com.lusidity.Initializer;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.IQueryResultHandler;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.UserCredentials;

import java.lang.reflect.Constructor;
import java.util.*;

public abstract class BaseExtendedData implements Initializer
{
	public static final Map<Class<? extends DataVertex>, Set<BaseExtendedData>> cache = new HashMap<>();

	public BaseExtendedData(){
		super();
	}
	
	public abstract void getExtendedData(UserCredentials userCredentials, DataVertex context, JsonData result, Map<String, Object> options);
	public abstract Collection<Class<? extends DataVertex>> forTypes();

	public static synchronized void cacheAdd(Class<? extends BaseExtendedData> cls){
		if(!BaseExtendedData.cache.containsKey(cls))
		{
			try
			{
				Constructor constructor=cls.getConstructor();
				BaseExtendedData bed=(BaseExtendedData) constructor.newInstance();
				for(Class<? extends DataVertex> type: bed.forTypes()){
					Set<BaseExtendedData> beds = BaseExtendedData.cache.get(type);
					if(null==beds){
						beds = new HashSet<>();
						BaseExtendedData.cache.put(type, beds);
					}
					boolean add = true;
					for(BaseExtendedData bd: beds){
						if(Objects.equals(bd.getClass(), bed.getClass())){
							add = false;
							break;
						}
					}
					if(add && !(ClassX.isAbstract(bed.getClass()) && ClassX.isInterface(bed.getClass())))
					{
						beds.add(bed);
					}
				}
			}
			catch (Exception ex){
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
	}

	public static synchronized Set<BaseExtendedData> getFor(Class<? extends DataVertex> type)
	{
		return BaseExtendedData.cache.get(type);
	}

	@Override
	public void initialize()
		throws Exception
	{
		Set<Class<? extends BaseExtendedData>> subtypes = Environment.getInstance().getReflections().getSubTypesOf(BaseExtendedData.class);

		for(Class<? extends BaseExtendedData> subtype: subtypes){
			BaseExtendedData.cacheAdd(subtype);
		}
	}

	@Override
	public int getInitializeOrdinal()
	{
		return 0;
	}

	public static void handleWebResource(BasePrincipal basePrincipal, DataVertex actual, DataVertex other, JsonData result, List<IQueryResultHandler> handlers) {
		Set<BaseExtendedData> beds = BaseExtendedData.getFor(actual.getActualClass());
		if(null!=beds){
			Map<String, Object> options=new HashMap<>();
			options.put("other", other);
			for(BaseExtendedData bed: beds)
			{
				bed.getExtendedData(basePrincipal.getCredentials(), actual, result, options);
			}
		}
		if(null != handlers){
			for(IQueryResultHandler handler: handlers){
				handler.handle(actual, result);
			}
		}
	}
}
