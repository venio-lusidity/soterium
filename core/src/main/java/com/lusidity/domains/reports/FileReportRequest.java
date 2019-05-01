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

package com.lusidity.domains.reports;

import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.people.Person;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;

@AtSchemaClass(name="File Report Request", discoverable=false)
public class FileReportRequest extends BaseDomain
{
	private KeyData<String> category=null;
	private KeyData<String> infId=null;
	private KeyData<Class<? extends DataVertex>> infType=null;
	private KeyData<String> principalId=null;

	// Constructors
	public FileReportRequest()
	{
		super();
	}

	@SuppressWarnings("unused")
	public FileReportRequest(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Methods
	public static FileReportRequest create(ApolloVertex vertex, BasePrincipal principal, String category)
	{
		FileReportRequest result=new FileReportRequest();
		try
		{
			result.fetchPrincipalId().setValue(principal.fetchId().getValue());
			result.fetchCategory().setValue(category);
			result.fetchInfId().setValue(vertex.fetchId().getValue());
			result.fetchInfType().setValue(vertex.getClass());
			result.save();
		}
		catch (Exception ex)
		{
			result=null;
		}
		return result;
	}

	public KeyData<String> fetchPrincipalId()
	{
		if (null==this.principalId)
		{
			this.principalId=new KeyData<>(this, "principalId", String.class, false, null);
		}
		return this.principalId;
	}

	public KeyData<String> fetchCategory()
	{
		if (null==this.category)
		{
			this.category=new KeyData<>(this, "category", String.class, false, null);
		}
		return this.category;
	}

	public KeyData<String> fetchInfId()
	{
		if (null==this.infId)
		{
			this.infId=new KeyData<>(this, "infId", String.class, false, null);
		}
		return this.infId;
	}

	public KeyData<Class<? extends DataVertex>> fetchInfType()
	{
		if (null==this.infType)
		{
			this.infType=new KeyData<>(this, "infType", Class.class, false, null);
		}
		return this.infType;
	}

	// Getters and setters
	public ApolloVertex getVertex()
	{
		ApolloVertex result=null;
		if (ClassX.isKindOf(this.fetchInfType().getValue(), DataVertex.class))
		{
			Class<? extends DataVertex> cls=this.fetchInfType().getValue();
			result = VertexFactory.getInstance().get(cls, this.fetchInfId().getValue());
		}
		return result;
	}

	public Person getPerson()
	{
		return VertexFactory.getInstance().get(Person.class, this.fetchPrincipalId().getValue());
	}
}
