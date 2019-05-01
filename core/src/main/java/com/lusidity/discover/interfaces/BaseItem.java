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

package com.lusidity.discover.interfaces;


import com.lusidity.data.ApolloVertex;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.discover.DiscoveryItem;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.system.primitives.RawString;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.system.security.UserCredentials;

import java.net.URI;
import java.util.*;

@SuppressWarnings({
	"EqualsAndHashcode",
	"AbstractClassWithoutAbstractMethods"
})
public abstract class BaseItem implements DiscoveryItem
{

	// Fields
	public static final double RELEVANCY_MIN=0.25;
	protected String description="";
	String vertexType=null;
	private final String key;
	private final Object value;
	private UserCredentials credentials=null;
	private String title="";
	private URI uri=null;
	private Double relevancy=null;
	private String phrase=null;
	private URI externalUri=null;
	private String name=null;
	private List<SubItem> related=new ArrayList<>();
	private DataVertex vertex=null;
	private int hits=0;

	// Constructors
	public BaseItem(String phrase, DataVertex vertex, UserCredentials credentials, String key, Object value, int hits)
	{
		super();
		this.vertex=vertex;
		this.phrase=phrase;
		this.credentials=credentials;
		this.key = key;
		this.value = value;
		if (ClassX.isKindOf(vertex, ApolloVertex.class))
		{
			ApolloVertex apolloVertex=(ApolloVertex) vertex;
			this.build(((ApolloVertex) vertex).fetchTitle().getValue(), apolloVertex.getDescription(), vertex.getUri(), null, BaseItem.RELEVANCY_MIN, apolloVertex.getClass(), hits);
		}
	}

	@SuppressWarnings("ParameterHidesMemberVariable")
	@Override
	public void build(String title, String description, URI internalUri, URI externalUri, Double relevancy, Class<? extends DataVertex> cls, int hits)
	{
		this.title=title;
		this.description=description;
		this.uri=internalUri;
		this.externalUri=externalUri;
		this.relevancy=relevancy;
		AtSchemaClass schema=ClassHelper.getSchema(cls);
		this.name=(null!=schema) ? schema.name() : cls.getSimpleName();
		this.vertexType=ClassHelper.getClassKey(cls);
		this.hits=hits;
	}

	@SuppressWarnings("ParameterHidesMemberVariable")
	@Override
	public void build(String title, RawString description, URI internalUri, URI externalUri, Double relevancy, Class<? extends DataVertex> type, int hits)
	{
		this.title=title;
		this.description=(null!=description) ? description.fetchValue().getValue() : null;
		this.uri=internalUri;
		this.externalUri=externalUri;
		this.relevancy=relevancy;
		AtSchemaClass schema=ClassHelper.getSchema(type);
		this.name=(null!=schema) ? schema.name() : type.getSimpleName();
		this.vertexType=ClassHelper.getClassKey(type);
		this.hits=hits;
	}

	/**
	 * Add items that can be related to this item.
	 *
	 * @param displayType The class type used for displaying to the user.
	 * @param store       The store to search.
	 * @param edgeType    The type of edge that connects these objects.
	 * @param qb          The query to use to search for the related objects.
	 * @param score       The relevancy of this object to the found objects.
	 * @param direction   Information about the item.
	 * @param hits        How many child items found.
	 */
	@SuppressWarnings("ParameterHidesMemberVariable")
	@Override
	public void addRelated(Class<? extends DataVertex> displayType, Class<? extends DataVertex> store, Class<? extends Edge> edgeType, BaseQueryBuilder qb, Double score, Common.Direction direction,
	                       int hits)
	{
		SubItem item=new SubItem(displayType, store, edgeType, qb, direction, score, hits);
		this.related.add(item);
	}

	@SuppressWarnings("ParameterHidesMemberVariable")
	@Override
	public void addRelated(Class<? extends DataVertex> displayType, Class<? extends DataVertex> store, Class<? extends Edge> edgeType, String label, BaseQueryBuilder qb, Double score, Common
		.Direction direction, int hits)
	{
		SubItem item=new SubItem(displayType, store, edgeType, label, qb, direction, score, hits);
		this.related.add(item);
	}

	@Override
	public JsonData toJson()
	{
		JsonData result=new JsonData();

		if (!StringX.isBlank(this.phrase))
		{
			result.put("phrase", this.phrase);
		}
		if (!StringX.isBlank(this.phrase) && !StringX.startsWithIgnoreCase(this.phrase, "/domain") && !StringX.startsWithIgnoreCase(this.getTitle(), this.phrase))
		{
			result.put("title", String.format("%s :[%s]", this.phrase, this.getTitle()));
		}
		else
		{
			result.put("title", this.getTitle());
		}
		result.put("description", StringX.isBlank(this.getDescription()) ? false : this.getDescription());
		result.put("uri", (null==this.getUri()) ? false : this.getUri());
		result.put("externalUri", (null==this.getExternalUri()) ? false : this.getExternalUri());
		result.put("relevancy", this.getRelevancy());
		result.put("name", this.getName());
		result.put("vertexType", this.getVertexType());
		result.put("hits", this.getHits());
		result.put("keyMatchedOn", this.getKey());
		result.put("valueMatchedOn", this.getValue());
		result.put("matchedOnHighlighted", String.format("%s: %s", this.getKey(), this.getValueHighlighted()));

		JsonData items=JsonData.createArray();
		if (!this.related.isEmpty())
		{
			//noinspection Java8ListSort
			Collections.sort(this.related, new Comparator<SubItem>()
			{
				// Overrides
				@Override
				public int compare(SubItem o1, SubItem o2)
				{
					return StringX.compare(o1.getTitle(), o2.getTitle());
				}
			});

			for (SubItem subItem : this.related)
			{
				items.put(subItem.toJson());
			}
		}

		result.put("related", items);

		return result;
	}

	// Overrides
	@Override
	public boolean equals(Object obj)
	{
		boolean result=false;
		if (ClassX.isKindOf(obj.getClass(), DiscoveryItem.class))
		{
			DiscoveryItem other=(DiscoveryItem) obj;
			result=Objects.equals(this.getUri(), other.getUri());
		}
		return result;
	}

	// Getters and setters	@Override
	public void setDescription(String desc)
	{
		this.description=desc;
	}

	public UserCredentials getUserCredentials()
	{
		return this.credentials;
	}


	@Override
	public DataVertex getVertex()
	{
		return this.vertex;
	}

	@Override
	public URI getUri()
	{
		return this.uri;
	}

	@Override
	public URI getExternalUri()
	{
		return this.externalUri;
	}

	@Override
	public String getTitle()
	{
		return this.title;
	}

	@Override
	public void setTitle(String title)
	{
		this.title=title;
	}

	@Override
	public String getDescription()
	{
		return this.description;
	}

	@Override
	public Double getRelevancy()
	{
		return this.relevancy;
	}

	@Override
	public void setRelevancy(double relevancy)
	{
		this.relevancy=relevancy;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public String getVertexType()
	{
		return this.vertexType;
	}

	@Override
	public int getHits()
	{
		return this.hits;
	}

	@Override
	public String getKey()
	{
		return this.key;
	}

	@Override
	public String getValue()
	{
		String result = null;
		if(null!=this.value){
			result = this.value.toString();
		}
		return result;
	}

	@Override
	public String getValueHighlighted()
	{
		String result = this.getValue();
		if(!StringX.isBlank(result)){
			try
			{
				result=StringX.highlight(result, this.phrase);
			}
			catch (Exception ignored){}
		}
		return result;
	}
}
