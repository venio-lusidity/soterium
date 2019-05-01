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

package com.lusidity.domains.location.discovery;

import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.discover.interfaces.BaseItem;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.location.Location;
import com.lusidity.framework.json.JsonData;
import com.lusidity.security.data.filters.PrincipalFilterEngine;
import com.lusidity.system.security.UserCredentials;

public class LocationItem extends BaseItem
{
// Constructors
	public LocationItem(String phrase, Location location, UserCredentials credentials, String key, Object value, int hits)
	{
		super(phrase, location, credentials, key, value, hits);
	}

// Overrides
	@Override
	public JsonData toJson()
	{
		JsonData result=super.toJson();
		Location location=(Location) this.getVertex();

		if(null!=location.fetchNameId().getValue())
		{
			String desc=String.format("COAMS ID: %d", location.fetchNameId().getValue());
			result.put("description", desc);
		}

		String key = "technology_security_vulnerabilities_vulnerability_details";
		Class<? extends DataVertex> cls = BaseDomain.getDomainType(key);
		JsonData types = JsonData.createArray();
		if(null!=cls)
		{
			String prefix = location.fetchPrefixTree().getValue();
			JsonData action=JsonData.createObject();

			BaseQueryBuilder qb=Environment.getInstance().getIndexStore().getQueryBuilder(cls, cls, 0, 0);
			qb.filter(BaseQueryBuilder.Operators.wildcard, "prefixLocation", BaseQueryBuilder.StringTypes.raw, String.format("%s*", prefix));
			qb.setApi(BaseQueryBuilder.API._count);

			PrincipalFilterEngine.getInstance().applyFor(Location.class, this.getUserCredentials(), qb, false);

			int hits=qb.execute().getCount();

			types.put("location");
			action.put("location", hits);

			action.put("fn", "aggSummary");
			action.put("types", types);
			action.put("data", location.toJson(false));
			result.put("action", action);
		}
		return result;
	}
}
