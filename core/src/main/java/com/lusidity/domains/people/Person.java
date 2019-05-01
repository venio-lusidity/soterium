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

package com.lusidity.domains.people;

import com.lusidity.Environment;
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.handler.KeyDataHandlerPersonName;
import com.lusidity.data.interfaces.data.query.*;
import com.lusidity.data.types.names.PersonalName;
import com.lusidity.data.types.names.WesternName;
import com.lusidity.discover.DiscoveryItem;
import com.lusidity.discover.interfaces.SuggestItem;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.common.ContactDetailHelper;
import com.lusidity.domains.common.Email;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.domains.object.edge.BlobEdge;
import com.lusidity.domains.people.discovery.PersonItem;
import com.lusidity.domains.people.person.Personalization;
import com.lusidity.domains.system.io.FileInfo;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.collections.CollectionX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.security.PersonCredentials;
import com.lusidity.system.security.UserCredentials;
import com.lusidity.system.security.UserPositions;
import org.joda.time.DateTime;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;

@AtSchemaClass(name="Person", discoverable=true, writable=true)
public class Person extends BasePrincipal
{
	@SuppressWarnings({
		"UtilityClassWithoutPrivateConstructor",
		"NonFinalUtilityClass"
	})
	public static class Queries
	{
		// Methods
		public static Collection<Person> discover(String name)
		{
			Collection<Person> results=Person.Queries.getFirstName(name);
			Collection<Person> ln=Person.Queries.getLastName(name);
			//noinspection unchecked
			CollectionX.addAllIfUnique(results, ln);
			return results;
		}

		public static <T extends Person> Collection<T> getFirstName(String name)
		{
			Collection<T> results=new ArrayList<>();
			try
			{
				if (!StringX.isBlank(name))
				{
					QueryResults queryResults=Environment.getInstance().getQueryFactory().startsWith(Person.class, Person.class,
						ValueObjects.create("firstName", name), SortObjects.create("firstName", BaseQueryBuilder.Sort.asc), 0, Environment.COLLECTIONS_DEFAULT_LIMIT
					);
					if (null!=queryResults)
					{
						for (IQueryResult queryResult : queryResults)
						{
							T t=queryResult.getVertex();
							if (!results.contains(t))
							{
								results.add(t);
							}
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

		public static <T extends Person> Collection<T> getLastName(String name)
		{
			Collection<T> results=new ArrayList<>();
			try
			{
				if (!StringX.isBlank(name))
				{
					QueryResults queryResults=Environment.getInstance().getQueryFactory().startsWith(Person.class, Person.class,
						ValueObjects.create("lastName", name), SortObjects.create("lastName", BaseQueryBuilder.Sort.asc), 0, Environment.COLLECTIONS_DEFAULT_LIMIT
					);
					if (null!=queryResults)
					{
						for (IQueryResult queryResult : queryResults)
						{
							T t=queryResult.getVertex();
							if (!results.contains(t))
							{
								results.add(t);
							}
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

		public static <T extends Person> Collection<T> get(String firstName, String lastName)
		{
			Collection<T> results=new ArrayList<>();
			try
			{
				if (!StringX.isBlank(firstName) && !StringX.isBlank(lastName))
				{
					ValueObjects vo=ValueObjects.create("firstName", firstName).add("lastName", lastName);
					QueryResults queryResults=Environment.getInstance().getQueryFactory().byValue(Person.class, Person.class,
						vo, SortObjects.create("firstName", BaseQueryBuilder.Sort.asc), 0, Environment.COLLECTIONS_DEFAULT_LIMIT
					);
					if (null!=queryResults)
					{
						for (IQueryResult queryResult : queryResults)
						{
							T t=queryResult.getVertex();
							if (!results.contains(t))
							{
								results.add(t);
							}
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

		public static Person getByEmail(String emailAddress)
		{
			Person result=null;
			Email email=ContactDetailHelper.getEmail(emailAddress);
			if (null!=email)
			{
				BaseQueryBuilder builder=Environment.getInstance().getIndexStore().getQueryBuilder(Edge.class, Person.class, 0, 0);
				builder.filter(BaseQueryBuilder.Operators.must, "label", BaseQueryBuilder.StringTypes.raw, Person.KEY_CONTACT_DETAILS);
				builder.filter(BaseQueryBuilder.Operators.must, Endpoint.KEY_TO_EP_ID, BaseQueryBuilder.StringTypes.raw, email.fetchId().getValue());

				QueryResults results=builder.execute();
				if (!results.isEmpty() && (results.size()==1))
				{
					IQueryResult queryResult=results.get(0);
					result=queryResult.getOtherEnd(email.fetchId().getValue());
				}
			}
			return result;
		}
	}

	// Fields
	public static final String KEY_CONTACT_DETAILS=ClassHelper.getPropertyKey(BaseContactDetail.class, "contactDetails");
	private KeyData<DateTime> dob=null;
	private KeyData<String> firstName=null;
	private KeyData<String> middleName=null;
	private KeyData<String> lastName=null;
	private KeyData<String> prefix=null;
	@AtSchemaProperty(name="Personalizations", expectedType=Personalization.class,
		description="Generally used to create personalized setting within an application.")
	private ElementEdges<Personalization> personalizations=null;
	@AtSchemaProperty(name="Contact Details", expectedType=BaseContactDetail.class,
		description="Contact information for a person.")
	private ElementEdges<BaseContactDetail> contactDetails=null;
	@AtSchemaProperty(name="Blob Files", expectedType=FileInfo.class, edgeType=BlobEdge.class,
		description="A collection of files or binary large objects")
	private ElementEdges<FileInfo> blobFiles=null;

	// Constructors
	public Person(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public Person()
	{
		super();
	}

	public Person(WesternName name)
	{
		this.fetchFirstName().setValue(name.getFirstName());
		this.fetchMiddleName().setValue(name.getMiddleName());
		this.fetchLastName().setValue(name.getLastName());
		this.fetchPrefix().setValue(name.getPrefix());
	}

	public KeyData<String> fetchFirstName()
	{
		if (null==this.firstName)
		{
			this.firstName=new KeyData<>(this, "firstName", String.class, true, null, new KeyDataHandlerPersonName());
		}
		return this.firstName;
	}

	public KeyData<String> fetchMiddleName()
	{
		if (null==this.middleName)
		{
			this.middleName=new KeyData<>(this, "middleName", String.class, true, null, new KeyDataHandlerPersonName());
		}
		return this.middleName;
	}

	public KeyData<String> fetchLastName()
	{
		if (null==this.lastName)
		{
			this.lastName=new KeyData<>(this, "lastName", String.class, true, null, new KeyDataHandlerPersonName());
		}
		return this.lastName;
	}

	public KeyData<String> fetchPrefix()
	{
		if (null==this.prefix)
		{
			this.prefix=new KeyData<>(this, "prefix", String.class, false, null, new KeyDataHandlerPersonName());
		}
		return this.prefix;
	}

	@Override
	public void beforeUpdate(LogEntry.OperationTypes operationType)
	{
		super.beforeUpdate(operationType);
		if (operationType==LogEntry.OperationTypes.delete)
		{
			this.getIdentities().clearAndDelete();
		}
	}

	@Override
	public DiscoveryItem getDiscoveryItem(String phrase, UserCredentials userCredentials, String key, Object value, boolean suggest)
	{
		DiscoveryItem result;
		if(suggest){
			result =SuggestItem.getSuggestion(this, userCredentials, phrase);
		}
		else{
			result = new PersonItem(phrase, this, userCredentials, key, value, 0);
		}
		return result;
	}

	// Methods
	public static Person fromWesternName(WesternName name)
	{
		return new Person(name);
	}

	@SuppressWarnings("unused")
	public KeyData<DateTime> fetchDob()
	{
		if (null==this.dob)
		{
			this.dob=new KeyData<>(this, "dob", DateTime.class, false, null);
		}
		return this.dob;
	}

	@SuppressWarnings("unused")
	public <T extends PersonalName> T getTypedName(Class<? extends PersonalName> cls)
	{
		T result=null;
		if (!StringX.isBlank(this.fetchFirstName().getValue()))
		{
			try
			{
				Constructor constructor=cls.getConstructor(String.class);
				//noinspection unchecked
				result=(T) constructor.newInstance(this.fetchFirstName().getValue());
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}

		return result;
	}

	@SuppressWarnings("unused")
	public boolean hasContactInfo(String value)
	{
		boolean result=false;
		for (BaseContactDetail contactDetail : this.getContactDetails())
		{
			result=StringX.equalsIgnoreCase(contactDetail.fetchValue().getValue(), value);
			if (result)
			{
				break;
			}
		}
		return result;
	}

	public ElementEdges<BaseContactDetail> getContactDetails()
	{
		if (null==this.contactDetails)
		{
			this.buildProperty("contactDetails");
		}
		return this.contactDetails;
	}

	// Getters and setters
	public WesternName getWesternName()
	{
		return new WesternName(
			this.getVertexData().getString("prefix"),
			this.getVertexData().getString("firstName"),
			this.getVertexData().getString("middleName"),
			this.getVertexData().getString("lastName"),
			null);
	}

	public String getBio()
	{
		String result=StringX.smartPrepend(
			"Title:", this.fetchPrefix().getValue(),
			"First Name: ", this.fetchFirstName().getValue(),
			"Middle Name:", this.fetchMiddleName().getValue(),
			"Last Name:", this.fetchLastName().getValue()
		);
		if (!this.getContactDetails().isEmpty())
		{
			StringBuffer sb=new StringBuffer();
			for (BaseContactDetail contactDetail : this.contactDetails)
			{
				sb.append("<br/>");
				String metaSummary=contactDetail.getMetaSummary();
				sb.append(metaSummary);
			}
			if (sb.length()>0)
			{
				result+=sb.toString();
			}
		}

		UserPositions userPositions = new UserPositions(this);
		String html = userPositions.toHtml();

		if (!StringX.isBlank(html))
		{
			result+=html;
		}

		return result;
	}

	public ElementEdges<FileInfo> getBlobFiles()
	{
		if (null==this.blobFiles)
		{
			this.buildProperty("blobFiles");
		}
		return this.blobFiles;
	}

	public ElementEdges<Personalization> getPersonalizations()
	{
		if (null==this.personalizations)
		{
			this.buildProperty("personalizations");
		}
		return this.personalizations;
	}

	@Override
	public UserCredentials getCredentials()
	{
		UserCredentials result = super.getCredentials();
		if(null==result){
			PersonCredentials pc = new PersonCredentials(this, null);
			this.setCredentials(pc);
		}
		return super.getCredentials();
	}
}
