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

package com.lusidity.system.security.cbac;


import com.lusidity.Environment;
import com.lusidity.cache.acs.AcsCachedItem;
import com.lusidity.cache.acs.AcsExpiredCache;
import com.lusidity.configuration.ScopedConfiguration;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.system.security.UserCredentials;
import org.joda.time.DateTime;

import java.time.LocalTime;

public class PolicyDecisionPoint implements ISecurityPolicy
{
	private static AcsExpiredCache cache = new AcsExpiredCache(LocalTime.MIDNIGHT);
	private final BasePrincipal principal;
	private final ISecurityPolicy[] policies;
	@SuppressWarnings({
		"FieldCanBeLocal",
		"unused"
	})
	private final DataVertex context;
	private AcsCachedItem cachedItem = null;
	// Constructors
	public PolicyDecisionPoint(DataVertex context, BasePrincipal principal, ISecurityPolicy... policies)
	{
		super();
		this.principal=principal;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.policies=policies;
		this.context=context;
		if(null!=this.context)
		{
			this.load();
		}
	}

	// Overrides
	/**
	 * Can the current principal delete the context?
	 *
	 * @return true or false
	 */
	@Override
	public boolean canDelete()
	{
		boolean result=this.isDenied();

		if (!result && (null!=this.principal))
		{
			result=this.principal.canDelete(true);

			if (result && (null!=this.policies) && ScopedConfiguration.getInstance().isEnabled())
			{
				for (ISecurityPolicy control : this.policies)
				{
					result=control.canDelete();
					if (!result)
					{
						break;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Can the current principal read the context?
	 *
	 * @return true or false
	 */
	@Override
	public boolean canRead()
	{
		boolean result=this.isDenied();

		if (!result && (null!=this.principal))
		{
			if(null!=this.cachedItem){
				result = this.cachedItem.isRead();
			}
			else
			{
				result=this.principal.canRead(true);

				if (result && (null!=this.policies) && ScopedConfiguration.getInstance().isEnabled())
				{
					for (ISecurityPolicy control : this.policies)
					{
						result=control.canRead();
						if (!result)
						{
							break;
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Can the current principal write back to the context?
	 *
	 * @return true or false
	 */
	@Override
	public boolean canWrite()
	{
		boolean result=this.isDenied();

		if (!result && (null!=this.principal))
		{
			if(null!=this.cachedItem){
				result = this.cachedItem.isWrite();
			}
			else
			{
				result=this.principal.canWrite(true);

				if (result && (null!=this.policies) && ScopedConfiguration.getInstance().isEnabled())
				{
					for (ISecurityPolicy control : this.policies)
					{
						result=control.canWrite();
						if (!result)
						{
							break;
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public boolean shouldMultiThread()
	{
		boolean result=false;
		if (null!=this.policies)
		{
			for (ISecurityPolicy policy : this.policies)
			{
				result=policy.shouldMultiThread();
				if (result)
				{
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Does the current principal have access to the context?
	 *
	 * @return true or false
	 */
	@Override
	public boolean isAuthorized()
	{
		// First part of being authorized is the ability to read the object.  Further authorization will be determined
		// by the context based access control passed to this class.
		boolean result=this.canRead();
		if (result && (null!=this.policies) && ScopedConfiguration.getInstance().isEnabled())
		{
			if(null!=this.cachedItem){
				result = this.cachedItem.isAuthorized();
			}
			else
			{
				for (ISecurityPolicy control : this.policies)
				{
					result=control.isAuthorized();
					if (!result)
					{
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Is the current principal explicitly denied access to the context?
	 *
	 * @return true or false
	 */
	@Override
	public boolean isDenied()
	{
		boolean result=(null==this.principal) || this.principal.isDenied(true);
		//noinspection OverlyComplexBooleanExpression
		if (result && (null!=this.principal) && (null!=this.policies) && ScopedConfiguration.getInstance().isEnabled())
		{
			if(null!=this.cachedItem){
				result = this.cachedItem.isDenied();
			}
			else
			{
				for (ISecurityPolicy control : this.policies)
				{
					result=control.isDenied();
					if (result)
					{
						break;
					}
				}
			}
		}
		return result;
	}

	@Override
	public boolean isInScope()
	{
		boolean result = (ScopedConfiguration.getInstance().isEnabled()) &&  ScopedConfiguration.getInstance().isPowerUser(this.principal);
		if (!result)
		{
			if (null!=this.cachedItem)
			{
				result=this.cachedItem.isInScope();
			}
			else
			{
				result=((null==this.policies) || (this.policies.length<=0));
				if ((null!=this.principal) && (null!=this.policies))
				{
					for (ISecurityPolicy policy : this.policies)
					{
						// TODO: this is stupid figure it out, it should never be null.
						if(null!=policy)
						{
							result=policy.isInScope();
							if (!result)
							{
								break;
							}
						}
					}
				}
			}
		}
		return result;
	}

	// Methods
	public static AcsExpiredCache getCache()
	{
		return PolicyDecisionPoint.cache;
	}

	public static PolicyDecisionPoint create(DataVertex context, BasePrincipal principal,
	                                         ISecurityPolicy... controls)
	{
		return new PolicyDecisionPoint(context, principal, controls);
	}

	public static boolean isInScope(DataVertex vertex, UserCredentials credentials)
	{
		return PolicyDecisionPoint.isInScope(vertex, credentials.getPrincipal());
	}

	public static boolean isInScope(DataVertex vertex, BasePrincipal principal)
	{
		boolean result = ScopedConfiguration.getInstance().isEnabled();
		if ((null!=vertex) && result)
		{
			DataVertex context = vertex;
			if (ClassX.isKindOf(vertex, Edge.class))
			{
				Endpoint toEndpoint=((Edge) vertex).fetchEndpointTo().getValue();
				context=toEndpoint.getVertex();
			}
			PolicyDecisionPoint policy = context.getSecurityPolicy(principal);
			result = policy.isInScope();
		}
		return result;
	}

	public static void clear(BasePrincipal principal)
	{
		Environment.getInstance().getReportHandler().info("PolicyDecisionPoint clear called");

		if((null!=principal))
		{
			Environment.getInstance().getReportHandler().info("The cache for %s is being cleared.", principal.fetchId().getValue());
			PolicyDecisionPoint.cache.clear(principal);
		}
		else{
			Environment.getInstance().getReportHandler().info("The principal is null cannot clear cache.");
		}
	}

	private void load()
	{
		try
		{
			if (ScopedConfiguration.getInstance().isCacheable(this.context))
			{
				this.cachedItem=PolicyDecisionPoint.cache.get(this.principal, this.context.fetchId().getValue());
				if (null==this.cachedItem)
				{
					DateTime expires=DateTime.now().plusDays(Environment.getInstance().getConfig().getAcsCacheExpireInDays());
					this.cachedItem=new AcsCachedItem(this.context,
						this.isAuthorized(),
						this.isInScope(),
						this.canRead(),
						this.canWrite(),
						this.canDelete(),
						this.isDenied(),
						expires
					);
					PolicyDecisionPoint.cache.remove(this.principal, this.cachedItem.getKey());
					PolicyDecisionPoint.cache.put(this.principal, this.cachedItem);
				}
			}
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().warning(ex);
		}
	}

	// Getters and setters
	public String getCacheKey()
	{
		return this.principal.fetchId().getValue();
	}
}
