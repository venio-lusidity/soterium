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

package com.lusidity.domains.organization.discovery;

import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.discover.interfaces.BaseItem;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.organization.Organization;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.UserCredentials;

public class OrganizationItem extends BaseItem
{
// Constructors
	public OrganizationItem(String phrase, Organization organization, UserCredentials credentials, String key, Object value, int hits)
	{
		super(phrase, organization, credentials, key, value, hits);
	}

// Overrides
	@Override
	public JsonData toJson()
	{
		JsonData result=super.toJson();
		Organization organization=(Organization) this.getVertex();

		if(null!=organization.fetchNameId().getValue())
		{
			String desc=String.format("COAMS ID: %d", organization.fetchNameId().getValue());
			result.put("description", desc);
		}

		String key = "technology_security_vulnerabilities_vulnerability_details";
		Class<? extends DataVertex> cls = BaseDomain.getDomainType(key);
		JsonData types = JsonData.createArray();
		if(null!=cls)
		{
			String prefix = organization.fetchPrefixTree().getValue();
			JsonData action=JsonData.createObject();

			BaseQueryBuilder qb1=Environment.getInstance().getIndexStore().getQueryBuilder(cls, cls, 0, 0);
			qb1.filter(BaseQueryBuilder.Operators.wildcard, "prefixManaged", BaseQueryBuilder.StringTypes.raw, String.format("%s*", prefix));
			qb1.setApi(BaseQueryBuilder.API._count);

			int hits=qb1.execute().getCount();

			types.put("managed");
			action.put("managed", hits);

			BaseQueryBuilder qb2=Environment.getInstance().getIndexStore().getQueryBuilder(cls, cls, 0, 0);
			qb2.filter(BaseQueryBuilder.Operators.wildcard, "prefixOwned", BaseQueryBuilder.StringTypes.raw, String.format("%s*", prefix));
			qb2.setApi(BaseQueryBuilder.API._count);

			hits=qb2.execute().getCount();

			types.put("owned");
			action.put("owned", hits);

			action.put("fn", "aggSummary");
			action.put("types", types);
			action.put("data", organization.toJson(false));
			result.put("action", action);
		}
		return result;
	}
}
