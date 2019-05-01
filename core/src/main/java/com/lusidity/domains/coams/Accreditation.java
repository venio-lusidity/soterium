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

package com.lusidity.domains.coams;

import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.system.primitives.Text;
import com.lusidity.domains.system.primitives.UriValue;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;

@AtSchemaClass(name="Accreditation", discoverable=false)
public class Accreditation extends BaseDomain
{
	public static class Queries
	{
		// Methods
		public static Accreditation getByOne(UriValue uri, Integer ditprId, Integer nameId)
		{
			Accreditation result=null;
			if (null!=uri)
			{
				result=VertexFactory.getInstance().getByIdentifier(Accreditation.class, uri);
			}
			if ((null==result) && (null!=nameId))
			{
				result=VertexFactory.getInstance().getByPropertyIgnoreCase(Accreditation.class, "nameId", nameId);
			}
			if ((null==result) && (null!=ditprId))
			{
				result=VertexFactory.getInstance().getByPropertyIgnoreCase(Accreditation.class, "ditprId", ditprId);
			}
			return result;
		}

		public static Accreditation getByTitle(Class<? extends DataVertex> store, String phrase)
		{
			Accreditation result=VertexFactory.getInstance().getByPropertyIgnoreCase(store, "title", phrase);
			if (null==result)
			{
				result=VertexFactory.getInstance().getByPropertyIgnoreCase(store, "abbreviation", phrase);
			}
			return result;
		}
	}

	// Fields
	public static final String KEY_SYSTEM_ROOT_ACC="Root Accreditation";
	private static String ROOT_ID=null;
	/*private String abbreviation=null;
	private Integer ditprId;
	private String rollupType;*/
	private KeyData<String> abbreviation=null;
	private KeyData<Integer> ditprId=null;
	private KeyData<String> strDitprId=null;
	private KeyData<String> rollupType=null;
	private KeyData<Integer> nameId=null;
	private KeyData<String> strNameId=null;
	private KeyData<Integer> superiorNameId=null;
	@AtSchemaProperty(
		name="Parent Systems",
		expectedType=Accreditation.class,
		limit=5,
		direction=Common.Direction.IN,
		fieldName="enclaves")
	private ElementEdges<Accreditation> parents=null;
	@AtSchemaProperty(name="Systems", expectedType=Accreditation.class)
	private ElementEdges<Accreditation> enclaves=null;

	// Constructors
	public Accreditation()
	{
		super();
	}

	public Accreditation(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Overrides
	@Override
	public void initialize()
		throws Exception
	{
		super.initialize();
		Accreditation root=Accreditation.Queries.getByTitle(Accreditation.class, Accreditation.KEY_SYSTEM_ROOT_ACC);
		if (null==root)
		{
			root=new Accreditation();
			root.fetchTitle().setValue(Accreditation.KEY_SYSTEM_ROOT_ACC);
			root.fetchTypes().add(new Text("global"));
			root.save();
			if (root.fetchId().isNullOrEmpty())
			{
				throw new ApplicationException("The Global Organization could not be created");
			}
			Environment.getInstance().getReservedNames().put(Accreditation.KEY_SYSTEM_ROOT_ACC, Accreditation.class);
		}
		Accreditation.ROOT_ID=root.fetchId().getValue();
	}

	@Override
	public int getInitializeOrdinal()
	{
		return 1200;
	}

	// Methods
	public static Accreditation getRoot()
	{
		if (null==Accreditation.ROOT_ID)
		{
			Accreditation root=Accreditation.Queries.getByTitle(Accreditation.class, Accreditation.KEY_SYSTEM_ROOT_ACC);
			if (null!=root)
			{
				Accreditation.ROOT_ID=root.fetchId().getValue();
			}
		}
		return Environment.getInstance().getDataStore().getObjectById(Accreditation.class, Accreditation.class, Accreditation.ROOT_ID);
	}

	public KeyData<String> fetchAbbreviation()
	{
		if (null==this.abbreviation)
		{
			this.abbreviation=new KeyData<>(this, "abbreviation", String.class, true, null);
		}
		return this.abbreviation;
	}

	public KeyData<String> fetchRollupType()
	{
		if (null==this.rollupType)
		{
			this.rollupType=new KeyData<>(this, "rollupType", String.class, true, null);
		}
		return this.rollupType;
	}

	public KeyData<Integer> fetchDitprId()
	{
		if (null==this.ditprId)
		{
			this.ditprId=new KeyData<>(this, "ditprId", Integer.class, true, null);
		}
		return this.ditprId;
	}

	public KeyData<String> fetchStrDitprId()
	{
		if (null==this.strDitprId)
		{
			this.strDitprId=new KeyData<>(this, "strDitprId", String.class, true, null);
		}
		return this.strDitprId;
	}

	public KeyData<Integer> fetchNameId()
	{
		if (null==this.nameId)
		{
			this.nameId=new KeyData<>(this, "nameId", Integer.class, true, null);
		}
		return this.nameId;
	}

	public KeyData<String> fetchStrNameId()
	{
		if (null==this.strNameId)
		{
			this.strNameId=new KeyData<>(this, "strNameId", String.class, true, null);
		}
		return this.strNameId;
	}

	public KeyData<Integer> fetchSuperiorNameId()
	{
		if (null==this.superiorNameId)
		{
			this.superiorNameId=new KeyData<>(this, "superiorNameId", Integer.class, true, null);
		}
		return this.superiorNameId;
	}

	// Getters and setters
	public ElementEdges<Accreditation> getParents()
	{
		if (null==this.parents)
		{
			this.buildProperty("parents");
		}
		return this.parents;
	}

	public ElementEdges<Accreditation> getEnclaves()
	{
		if (null==this.enclaves)
		{
			this.buildProperty("enclaves");
		}
		return this.enclaves;
	}
}
