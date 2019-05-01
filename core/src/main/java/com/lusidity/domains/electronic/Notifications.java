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

package com.lusidity.domains.electronic;

import com.lusidity.Environment;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.domains.object.edge.PrincipalEdge;
import com.lusidity.framework.json.JsonData;
import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

@SuppressWarnings("StandardVariableNames")
public class Notifications implements Collection<Notification>
{
	private final BasePrincipal receiver;
	private final Integer start;
	private final Integer limit;
	private Collection<Notification> underlying=new ArrayList<>();
	@SuppressWarnings("InstanceVariableNamingConvention")
	private int hits=0;

// Constructors
	public Notifications(BasePrincipal receiver, Integer start, Integer limit)
	{
		super();
		this.receiver=receiver;
		this.start=start;
		this.limit=limit;
		this.load();
	}

// Overrides
	@Override
	public int size()
	{
		return this.underlying.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.underlying.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return this.underlying.contains(o);
	}

	@Override
	public @NotNull
	Iterator<Notification> iterator()
	{
		return this.underlying.iterator();
	}

	@Override
	public @NotNull
	Object[] toArray()
	{
		return this.underlying.toArray();
	}

	@Override
	public @NotNull <T> T[] toArray(T[] a)
	{
		//noinspection SuspiciousToArrayCall
		return this.underlying.toArray(a);
	}

	@Override
	public boolean add(Notification notification)
	{
		return this.underlying.add(notification);
	}

	@Override
	public boolean remove(Object o)
	{
		return this.underlying.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return this.underlying.containsAll(c);
	}

	@Override
	public boolean addAll(@Flow(sourceIsContainer=true, targetIsContainer=true) Collection<? extends Notification> c)
	{
		return this.underlying.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return this.underlying.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return this.underlying.retainAll(c);
	}

	@Override
	public void clear()
	{
		this.underlying.clear();
	}

	public JsonData toJson()
	{
		JsonData result=JsonData.createObject();
		result.put("hits", this.hits);
		result.put("next", this.start+this.limit);
		result.put("limit", this.limit);
		JsonData results=JsonData.createArray();

		for (Notification notification : this.underlying)
		{
			JsonData item=notification.toJson(false);
			results.put(item);
		}

		result.put("results", results);
		return result;
	}

	private BaseQueryBuilder getQuery(BasePrincipal principal, Class<? extends Notification> partitionType)
	{
		String endpointLabel=Endpoint.KEY_TO_EP_ID;
		String key=ClassHelper.getPropertyKey(BasePrincipal.class, "receivers");
		BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(PrincipalEdge.class, partitionType, this.start, this.limit);
		qb.filter(BaseQueryBuilder.Operators.must, "label", BaseQueryBuilder.StringTypes.raw, key);
		qb.filter(BaseQueryBuilder.Operators.must, endpointLabel, BaseQueryBuilder.StringTypes.raw, principal.fetchId().getValue());
		qb.filter(BaseQueryBuilder.Operators.must, "deprecated", BaseQueryBuilder.StringTypes.raw, false);
		qb.sort("createdWhen", BaseQueryBuilder.Sort.asc);
		return qb;
	}

	private void load()
	{
		this.loadClass(Notification.class);
		Set<Class<? extends Notification>> subTypes=Environment.getInstance().getReflections().getSubTypesOf(Notification.class);
		if (null!=subTypes)
		{
			for (Class<? extends Notification> cls : subTypes)
			{
				this.loadClass(cls);
			}
		}
		this.underlying=ClassHelper.sortCreatedWhen(this.underlying, BaseQueryBuilder.Sort.asc);
	}

	private void loadClass(Class<? extends Notification> cls)
	{
		Collection<Notification> notifications=this.get(this.receiver, cls);
		this.underlying.addAll(notifications);
		this.loadParents(this.receiver, cls);
	}

	private void loadParents(BasePrincipal principal, Class<? extends Notification> cls)
	{
		for (BasePrincipal parent : principal.getParentPrincipals())
		{
			Collection<Notification> notifications=this.get(parent, cls);
			this.underlying.addAll(notifications);
			this.loadParents(parent, cls);
		}
	}

	private Collection<Notification> get(BasePrincipal principal, Class<? extends Notification> cls)
	{
		BaseQueryBuilder qb=this.getQuery(principal, cls);
		QueryResults queryResults=qb.execute();
		this.hits+=queryResults.getHits();
		return queryResults.toCollection(principal.fetchId().getValue());
	}
}
