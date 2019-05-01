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

package com.lusidity.domains.location;

import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.collections.TermEdges;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.discover.DiscoveryItem;
import com.lusidity.discover.interfaces.SuggestItem;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.location.discovery.LocationItem;
import com.lusidity.domains.object.edge.LocationEdge;
import com.lusidity.domains.object.edge.ScopedEdge;
import com.lusidity.domains.organization.PersonnelPosition;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.system.security.UserCredentials;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings({
	"UnusedDeclaration",
	"EqualsAndHashcode"
})
@AtSchemaClass(name="Location", discoverable=true)
public class Location extends BaseContactDetail
{
	public enum Types
	{
		country,
		state,
		county,
		city,
		town,
		place
	}

	@SuppressWarnings("NonFinalUtilityClass")
	public static class Queries
	{

		private Queries()
		{
		}

		// Methods
		public static Collection<Location> getStartsWith(String phrase)
		{
			return Location.Queries.getStartsWith(phrase, Location.class);
		}

		public static <T extends Location> Collection<T> getStartsWith(String phrase, Class<? extends Location> type)
		{
			Collection<T> results=new ArrayList<>();
			try
			{
				if (!StringX.isBlank(phrase))
				{
					results=VertexFactory.getInstance().startsWith(type, phrase, 0, 0);
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
			return results;
		}

		public static QueryResults get(Class<? extends Location> cls, BaseQueryBuilder.Sort sort, int start, int limit)
		{
			BaseQueryBuilder builder=Environment.getInstance().getIndexStore().getQueryBuilder(Location.class, Location.class, start, limit);
			builder.sort("title", BaseQueryBuilder.Sort.asc);
			builder.filter(BaseQueryBuilder.Operators.matchAll, "phrase", BaseQueryBuilder.StringTypes.raw, null);
			return builder.execute();
		}

		public static Location getByTitle(String title)
		{
			Location result=null;
			Collection<Class<? extends Location>> types=Environment.getInstance().getReflections().getSubTypesOf(Location.class);
			types.add(Location.class);
			for (Class<? extends Location> type : types)
			{
				try
				{
					result=Location.Queries.getByTitle(type, title);
					if (null!=result)
					{
						break;
					}
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().severe(ex);
				}
			}

			return result;
		}

		public static <T extends Location> T getByTitle(Class<? extends DataVertex> store, String title)
		{
			T result=null;
			try
			{
				if (ClassX.isKindOf(store, Location.class) && !StringX.isBlank(title))
				{
					result=VertexFactory.getInstance().getByTitle(Location.class, title);

					if (null==result)
					{
						result=VertexFactory.getInstance().getByPropertyIgnoreCase(Location.class, "abbreviation", title);
					}
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
			return result;
		}
	}
	// Fields
	@SuppressWarnings("WeakerAccess")
	public static final String KEY_SYSTEM_ROOT_LOC="Locations";
	public static final String KEY_SYSTEM_TAG_ISSUES="Location Tag Issues";
	public static final int INIT_ORDINAL=1100;
	private static final String COUNTRIES_XML="data/countries.xml";
	private static final String STATES_XML="data/states.xml";
	private static Location tagIssues=null;
	private static Location root=null;
	private KeyData<String> abbreviation=null;
	private KeyData<Location.Types> type=null;
	private KeyData<String> prefixTree= null;

	/*
		This value is specific to the COAMS owned by the government
    */
	private KeyData<Integer> nameId = null;
	/*
		This value is specific to the COAMS owned by the government
	 */
	private KeyData<Integer> superiorNameId = null;

	@AtSchemaProperty(name="Location", expectedType=Location.class, edgeType=LocationEdge.class,
		description="Child locations")
	private ElementEdges<Location> places=null;

	@AtSchemaProperty(
		name="Location", expectedType=Location.class, edgeType=LocationEdge.class,
		direction=Common.Direction.IN, fieldName="places", description="Parent locations."
	)
	private ElementEdges<Location> parentPlaces=null;

	@AtSchemaProperty(name="Scoped Positions", edgeType=ScopedEdge.class, expectedType=PersonnelPosition.class,
		description="A position with an area of responsibility within this location.")
	private TermEdges<PersonnelPosition> scopedPositions=null;


	// Constructors
	public Location()
	{
		super();
	}

	public Location(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Overrides
	@Override
	public void initialize()
		throws Exception
	{
		super.initialize();
		Location.root=Location.Queries.getByTitle(Location.class, Location.KEY_SYSTEM_ROOT_LOC);
		if (null==Location.root)
		{
			//noinspection NonThreadSafeLazyInitialization
			Location.root=new Location();
			Location.root.fetchTitle().setValue(Location.KEY_SYSTEM_ROOT_LOC);
			if (!Location.root.save())
			{
				throw new ApplicationException("The Global Location could not be created");
			}
			Environment.getInstance().getReservedNames().put(Location.KEY_SYSTEM_ROOT_LOC, Location.class);
		}

		Location.tagIssues=Location.Queries.getByTitle(Location.class, Location.KEY_SYSTEM_TAG_ISSUES);
		if (null==Location.tagIssues)
		{
			//noinspection NonThreadSafeLazyInitialization
			Location.tagIssues=new Location();
			Location.tagIssues.fetchTitle().setValue(Location.KEY_SYSTEM_TAG_ISSUES);
			if (!Location.tagIssues.save())
			{
				throw new ApplicationException("The Location tag issues could not be created");
			}
		}
		if((null!=Location.getTagIssues()) && !Location.root.getPlaces().contains(Location.getTagIssues())){
			Location.root.getPlaces().add(Location.tagIssues);
			Environment.getInstance().getReservedNames().put(Location.KEY_SYSTEM_TAG_ISSUES, Location.class);
		}
	}

	@Override
	public int getInitializeOrdinal()
	{
		return Location.INIT_ORDINAL;
	}

	public static synchronized Location getTagIssues()
	{
		if (null==Location.tagIssues)
		{
			Location.tagIssues=Location.Queries.getByTitle(Location.class, Location.KEY_SYSTEM_TAG_ISSUES);
		}
		return Location.tagIssues;
	}

	public ElementEdges<Location> getPlaces()
	{
		if (null==this.places)
		{
			this.buildProperty("places");
		}
		return this.places;
	}

	@Override
	public boolean enforcePolicy()
	{
		return !this.equals(Location.getRoot());
	}

	@Override
	public boolean equals(Object o)
	{
		boolean result=false;
		if (ClassX.isKindOf(o, Location.class))
		{
			Location that=(Location) o;
			if (!this.fetchId().isNullOrEmpty() && !that.fetchId().isNullOrEmpty())
			{
				String thisId=String.valueOf(this.fetchId().getValue());
				String thatId=String.valueOf(that.fetchId().getValue());
				result=thisId.equals(thatId);
			}
			else
			{
				result=super.equals(o);
			}
		}
		return result;
	}

	@Override
	protected void load(String value)
	{
		// Do nothing load as normal
	}

	public static synchronized Location getRoot()
	{
		if (null==Location.root)
		{
			Location.root=Location.Queries.getByTitle(Location.class, Location.KEY_SYSTEM_ROOT_LOC);
		}
		return Location.root;
	}

	@Override
	public DiscoveryItem getDiscoveryItem(String phrase, UserCredentials userCredentials, String key, Object value, boolean suggest)
	{
		DiscoveryItem result;
		if (suggest)
		{
			result=SuggestItem.getSuggestion(this, userCredentials, phrase);
		}
		else
		{
			result=new LocationItem(phrase, this, userCredentials, key, value, 0);
		}
		return result;
	}

	public KeyData<String> fetchAbbreviation()
	{
		if (null==this.abbreviation)
		{
			this.abbreviation=new KeyData<>(this, "abbreviation", String.class, false, null);
		}
		return this.abbreviation;
	}

	public KeyData<String> fetchPrefixTree()
	{
		if (null==this.prefixTree)
		{
			this.prefixTree= new KeyData<>(this, "prefixTree", String.class, false, false);
		}
		return this.prefixTree;
	}

	public KeyData<Location.Types> fetchType()
	{
		if (null==this.type)
		{
			this.type=new KeyData<>(this, "type", Location.Types.class, false, Location.Types.place);
		}
		return this.type;
	}

	/**
	 * This value is specific to COAMS a government owned organizational system.
	 * @return A COAMS Id.
	 */
	public KeyData<Integer> fetchNameId()
	{
		if (null==this.nameId)
		{
			this.nameId=new KeyData<>(this, "nameId", Integer.class, true, null);
		}
		return this.nameId;
	}

	/**
	 * This value is specific to COAMS a government owned organizational system and relates to a parent organization.
	 * @return A COAMS Id.
	 */
	public KeyData<Integer> fetchSuperiorNameId()
	{
		if (null==this.superiorNameId)
		{
			this.superiorNameId=new KeyData<>(this, "superiorNameId", Integer.class, true, null);
		}
		return this.superiorNameId;
	}

	@SuppressWarnings("ParameterHidesMemberVariable")
	public void addLocation(Location location)
	{
		if (null!=location)
		{
			boolean added=this.getPlaces().add(location);
			if (!added)
			{
				Environment.getInstance().getReportHandler().warning("Could not add location %s.", location.fetchTitle().getValue());
			}
		}
	}

// Getters and setters
	/**
	 * Get this Location's parents places.
	 *
	 * @return Parent places.
	 */
	public ElementEdges<Location> getParentPlaces()
	{
		if (null==this.parentPlaces)
		{
			this.buildProperty("parentPlaces");
		}
		return this.parentPlaces;
	}

	public TermEdges<PersonnelPosition> getScopedPositions()
	{
		if (null==this.scopedPositions)
		{
			this.buildProperty("scopedPositions");
		}
		return this.scopedPositions;
	}

}
