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

package com.lusidity.discover;

import com.lusidity.Environment;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.discover.interfaces.SuggestItem;
import com.lusidity.discover.tasks.ProviderTask;
import com.lusidity.discover.tasks.SearchTask;
import com.lusidity.domains.object.Edge;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.text.StringX;
import com.lusidity.helper.PropertyHelper;
import com.lusidity.system.security.UserCredentials;
import com.lusidity.tasks.TaskManager;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscoveryEngine implements Closeable {

    private static final Collection<DiscoveryProvider> PROVIDERS= new ArrayList<>();

    static {
        Collection<Class<? extends DiscoveryProvider>> results = Environment.getInstance().getReflections().getSubTypesOf(DiscoveryProvider.class);
        if(results!=null){
            for(Class<? extends DiscoveryProvider> result: results){
               try {
                   if(!ClassX.isAbstract(result))
                   {
                       Constructor constructor=result.getConstructor();
                       DiscoveryProvider dp=(DiscoveryProvider) constructor.newInstance();
                       DiscoveryEngine.PROVIDERS.add(dp);
                   }
               }
               catch (Exception ex){
                   Environment.getInstance().getReportHandler().severe(ex);
               }
            }
        }
    }

	private final UserCredentials credentials;

	public DiscoveryEngine(UserCredentials credentials){
        super();
        this.credentials = credentials;
    }

	public Collection<DiscoveryItems> grouped(String phrase, boolean suggest, int start, int limit, Collection<Class<? extends DataVertex>> types) {
		Collection<DiscoveryItems> results = new ArrayList<>();
		try(TaskManager taskManager = new TaskManager())
		{

			taskManager.startFixed(10);
			Collection<Future<DiscoveryItems>> futures=new ArrayList<>();


			String fPhrase=phrase;
			Pattern pattern=Pattern.compile("^(.*)\\:\\[(.*)\\]");
			Matcher matcher=pattern.matcher(fPhrase);
			if (matcher.matches())
			{
				fPhrase=matcher.group(1);
			}

			for (DiscoveryProvider provider : DiscoveryEngine.PROVIDERS)
			{
				boolean process=((null==types) || types.isEmpty() || types.contains(provider.getVertexType()));
				if (process)
				{
					try
					{
						ProviderTask task=new ProviderTask(this.credentials, provider, fPhrase, suggest, start, limit);
						Future<DiscoveryItems> future=taskManager.submit(task);
						futures.add(future);
					}
					catch (Exception ignored)
					{
					}
				}
			}

			Collection<Class<? extends ApolloVertex>> processed = new ArrayList<>();

			for (Map.Entry<String, Class<? extends ApolloVertex>> entry : Environment.getInstance().getApolloVertexTypes().entrySet())
			{
				AtSchemaClass schemaClass=entry.getValue().getAnnotation(AtSchemaClass.class);
				boolean discoverable=(null!=schemaClass) && schemaClass.discoverable();
				boolean process = discoverable && !processed.contains(entry.getValue()) && !ClassX.isKindOf(entry.getValue(), Edge.class)
				                  && !ClassX.isAbstract(entry.getValue()) && !ClassX.isInterface(entry.getValue())
				                  && ((null==types) || types.isEmpty() || types.contains(entry.getValue()));
				if (!process)
				{
					continue;
				}
				processed.add(entry.getValue());

				for (DiscoveryProvider provider : DiscoveryEngine.PROVIDERS)
				{
					if (provider.providerHandles(entry.getValue()))
					{
						process=false;
						break;
					}
				}

				if (!process)
				{
					continue;
				}

				try
				{
					SearchTask task=new SearchTask(this.credentials, entry.getValue(), false, fPhrase, start, limit);
					if (this.isThreadingEnabled())
					{
						Future<DiscoveryItems> future=taskManager.submit(task);
						futures.add(future);
					}
					else
					{
						DiscoveryItems items=task.call();
						if ((null!=items) && !items.isEmpty())
						{
							results.add(items);
						}
					}
				}
				catch (Exception ignored){}
			}

			if (this.isThreadingEnabled())
			{
				for (Future<DiscoveryItems> future : futures)
				{
					try
					{
						DiscoveryItems items=future.get();
						if (null!=items)
						{
							results.add(items);
						}
					}
					catch (Exception ignored)
					{
					}
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}

		return results;
	}

    public DiscoveryItems discover(String phrase, boolean suggest, int start, int limit, Collection<Class<? extends DataVertex>> types) {
	    DiscoveryItems results = new DiscoveryItems(null, 0);
	    try(TaskManager taskManager = new TaskManager())
	    {

		    taskManager.startFixed(10);
		    Collection<Future<DiscoveryItems>> futures=new ArrayList<>();


		    String fPhrase=phrase;
		    Pattern pattern=Pattern.compile("^(.*)\\:\\[(.*)\\]");
		    Matcher matcher=pattern.matcher(fPhrase);
		    if (matcher.matches())
		    {
			    fPhrase=matcher.group(1);
		    }

		    for (DiscoveryProvider provider : DiscoveryEngine.PROVIDERS)
		    {
			    boolean process=((null==types) || types.isEmpty() || types.contains(provider.getVertexType()));
			    if (process)
			    {
				    try
				    {
					    ProviderTask task=new ProviderTask(this.credentials, provider, fPhrase, suggest, start, limit);
					    Future<DiscoveryItems> future=taskManager.submit(task);
					    futures.add(future);
				    }
				    catch (Exception ignored)
				    {
				    }
			    }
		    }

		    for (Map.Entry<String, Class<? extends ApolloVertex>> entry : Environment.getInstance().getApolloVertexTypes().entrySet())
		    {
			    AtSchemaClass schemaClass=entry.getValue().getAnnotation(AtSchemaClass.class);
			    boolean discoverable=(null!=schemaClass) && schemaClass.discoverable();
			    boolean process = discoverable && !ClassX.isKindOf(entry.getValue(), Edge.class) && ((null==types) || types.isEmpty() || types.contains(entry.getValue()));
			    if (!process)
			    {
				    continue;
			    }

			    for (DiscoveryProvider provider : DiscoveryEngine.PROVIDERS)
			    {
				    if (provider.providerHandles(entry.getValue()))
				    {
					    process=false;
					    break;
				    }
			    }

			    if (!process)
			    {
				    continue;
			    }

			    try
			    {
				    SearchTask task=new SearchTask(this.credentials, entry.getValue(), false, fPhrase, start, limit);
				    if (this.isThreadingEnabled())
				    {
					    Future<DiscoveryItems> future=taskManager.submit(task);
					    futures.add(future);
				    }
				    else
				    {
					    DiscoveryItems items=task.call();
					    if ((null!=items) && !items.isEmpty())
					    {
						    results.merge(items);
					    }
				    }
			    }
			    catch (Exception ignored){}
		    }

		    if (this.isThreadingEnabled())
		    {
			    for (Future<DiscoveryItems> future : futures)
			    {
				    try
				    {
					    DiscoveryItems items=future.get();
					    if (null!=items)
					    {
						    results.merge(items);
					    }
				    }
				    catch (Exception ignored)
				    {
				    }
			    }
		    }
	    }
	    catch (Exception ex)
	    {
		    Environment.getInstance().getReportHandler().warning(ex);
	    }

        return results;
    }

    public void suggest(boolean suggest, String phrase, int start, int limit, DiscoveryItems results, Class<? extends DataVertex> cls,
                        String parentKey, Class parentType, String childKey, Class childType
    ) {
        String key = parentKey;
        if(!StringX.isBlank(childKey) && (null!=childType)){
            key = PropertyHelper.getSubPropertyName(parentKey, childType, childKey);
        }

        if(StringX.isNumerical(phrase))
        {
        	Object obj = null;
        	if(StringX.contains(phrase, ".")){
        		obj = Double.parseDouble(phrase);
	        }
	        else{
        		obj = Integer.parseInt(phrase);
	        }
	        BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(cls, cls, start, limit);
	        qb.filter(BaseQueryBuilder.Operators.must, key, BaseQueryBuilder.StringTypes.na, obj);
	        qb.setCredentials(this.credentials);
	        qb.sort(key, BaseQueryBuilder.Sort.asc);
	        QueryResults queryResults=qb.execute();
	        results.addHits(queryResults.getHits());
	        if (!queryResults.isEmpty())
	        {
		        for (IQueryResult queryResult : queryResults)
		        {
			        try
			        {
				        ApolloVertex vertex=queryResult.getVertex();
				        if(suggest)
				        {
					        SuggestItem result=SuggestItem.getSuggestion(vertex, this.credentials, phrase, childKey, childType, parentKey);
					        results.add(result);
				        }
				        else{
				        	String fKey = StringX.replace(key, ".", "::");
				        	Object value = vertex.getVertexData().getObjectFromPath(fKey);
				        	DiscoveryItem item = vertex.getDiscoveryItem(phrase, this.credentials, fKey, value, false);
				        	results.add(item);
				        }
			        }
			        catch (Exception ignored)
			        {
			        }
		        }
	        }
        }

	    BaseQueryBuilder qb2 = Environment.getInstance().getIndexStore().getQueryBuilder(cls, cls, start, limit);
	    qb2.filter(BaseQueryBuilder.Operators.must, key, BaseQueryBuilder.StringTypes.folded, phrase.toLowerCase());
	    qb2.setCredentials(this.credentials);
	    qb2.sort(key, BaseQueryBuilder.Sort.asc);
	    QueryResults queryResults = qb2.execute();
	    results.addHits(queryResults.getHits());
	    if (!queryResults.isEmpty()) {
		    for (IQueryResult queryResult : queryResults) {
			    try {
				    ApolloVertex vertex = queryResult.getVertex();
				    if(suggest)
				    {
					    SuggestItem result=SuggestItem.getSuggestion(vertex, this.credentials, phrase, childKey, childType, parentKey);
					    results.add(result);
				    }
				    else{
					    String fKey = StringX.replace(key, ".", "::");
				    	Object value = vertex.getVertexData().getObjectFromPath(fKey);
					    DiscoveryItem item = vertex.getDiscoveryItem(phrase, this.credentials, fKey, value, false);
					    results.add(item);
				    }
			    }
			    catch (Exception ignored){}
		    }
	    }

        BaseQueryBuilder qb3 = Environment.getInstance().getIndexStore().getQueryBuilder(cls, cls, start, limit);
        qb3.filter(BaseQueryBuilder.Operators.must, key, BaseQueryBuilder.StringTypes.starts_with, phrase.toLowerCase());
	    qb3.setCredentials(this.credentials);
        qb3.sort(key, BaseQueryBuilder.Sort.asc);
        queryResults = qb3.execute();
        results.addHits(queryResults.getHits());
        if (!queryResults.isEmpty()) {
            for (IQueryResult queryResult : queryResults) {
                try {
                    ApolloVertex vertex = queryResult.getVertex();
                    if(suggest)
                    {
	                    SuggestItem result=SuggestItem.getSuggestion(vertex, this.credentials, phrase, childKey, childType, parentKey);
	                    results.add(result);
                    }
                    else{
                    	String fKey = StringX.replace(key, ".", "::");
                    	Object value = vertex.getVertexData().getObjectFromPath(fKey);
	                    DiscoveryItem item = vertex.getDiscoveryItem(phrase, this.credentials, fKey, value, false);
	                    results.add(item);
                    }
                }
                catch (Exception ignored){}
            }
        }
    }

	@Override
    public void close() throws IOException {

    }

	public boolean isThreadingEnabled()
	{
		return true;
	}
}
