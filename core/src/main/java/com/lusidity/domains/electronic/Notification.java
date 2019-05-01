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
import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.collections.PrincipalEdges;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.common.Email;
import com.lusidity.domains.document.form.Form;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.people.Person;
import com.lusidity.domains.system.primitives.RawString;
import com.lusidity.email.EmailConfiguration;
import com.lusidity.email.EmailTemplate;
import com.lusidity.email.EmailX;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

@AtSchemaClass(name="Notification", discoverable=false, description="A document in which to write, select and record information.")
public class Notification extends Form
{
// Fields
	public static final String DEFAULT_TEMPLATE="templates::value::default";

	private KeyData<Boolean> read=null;

	@AtSchemaProperty(name="Receivers", expectedType=BasePrincipal.class,
		description="Principals that the notification is intended for.")
	private PrincipalEdges<BasePrincipal> receivers=null;
	@AtSchemaProperty(name="Targets", expectedType=DataVertex.class, description="Vertices that the receivers are being notified about.")
	private
	ElementEdges<DataVertex> targets=null;

	@AtSchemaProperty(name="Initiator", description="The person who initiated the notification.",
		expectedType=Person.class, isSingleInstance=true)
	private ElementEdges<Person> initiators=null;

	// Constructors
	public Notification()
	{
		super();
	}
	public Notification(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

// Overrides
	@Override
	public void afterEdgeUpdate(LogEntry.OperationTypes operationType, DataVertex other, Edge edge, boolean success)
	{
		if ((operationType==LogEntry.OperationTypes.delete) && this.getTargets().isEmpty())
		{
			try
			{
				this.delete();
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
		super.afterEdgeUpdate(operationType, other, edge, success);
	}

	public ElementEdges<DataVertex> getTargets()
	{
		if (null==this.targets)
		{
			this.buildProperty("targets");
		}
		return this.targets;
	}

	@SuppressWarnings("MethodWithTooManyParameters")
	protected static boolean send(String template,
	                              String[] whos, String sender, String[] carbonCopies, String[] to, String content, String url, URI imageUri
	)
	{
		boolean sent=false;
		try
		{
			URI uri=new URI(url);
			ApolloVertex vertex=VertexFactory.getInstance().get(DataVertex.class, URI.create(uri.getPath()));
			if (null!=vertex)
			{
				String title=vertex.fetchTitle().getValue();
				String who=StringX.join(";", whos);

				String subject=String.format("%s has sent you a link for %s", who, title);

				RawString txtDescription=vertex.getDescriptions().get();
				String description="";
				if (null!=txtDescription)
				{
					description=txtDescription.fetchValue().getValue();
				}

				@SuppressWarnings("ConstantConditions")
				String body=EmailTemplate.getCustom(template, whos, sender, null, title, content, description, uri, imageUri);

				EmailX.sendMail(
					EmailX.getDefaultServerKey(), EmailX.getDefaultFrom(), to, carbonCopies, new String[]{sender}, subject, body, false, null
				);
				sent=true;
			}
		}
		catch (Exception ignored)
		{
			sent=false;
		}

		return sent;
	}

	@SuppressWarnings("unused")
	public KeyData<Boolean> fetchRead()
	{
		if (null==this.read)
		{
			this.read=new KeyData<>(this, "read", Boolean.class, false, false);
		}
		return this.read;
	}

	public void send(BaseContactDetail.CategoryTypes categoryType, String content)
		throws Exception
	{
		this.send(Notification.DEFAULT_TEMPLATE, categoryType, content);
	}

	public void send(String templateKey, BaseContactDetail.CategoryTypes categoryType, String content)
		throws Exception
	{
		Object o=EmailConfiguration.getInstance().getProperty(templateKey);
		if (o instanceof String)
		{
			String path=(String) o;
			File file=new File(Environment.getInstance().getConfig().getResourcePath(), path);
			if (file.exists())
			{
				String template=StringX.readFile(file.getAbsolutePath(), Charset.forName("UTF-8"));
				if (!StringX.isBlank(template))
				{
					String[] who=this.getWho();
					String sender=EmailX.getDefaultFrom();
					String[] to=this.getTo(categoryType);
					// todo get initiators and pass to getCustom.
					String[] initiator=this.getInitiator();

					String body=EmailTemplate.getCustom(template, who, sender, initiator, this.fetchTitle().getValue(), content, this.getDescription(), null, null);

					EmailX.sendMail(
						EmailX.getDefaultServerKey(), EmailX.getDefaultFrom(), to, null, new String[]{sender}, this.fetchTitle().getValue(), body, false, null
					);
				}
			}
		}
	}

	@SuppressWarnings("Duplicates")
	public String[] getWho()
	{
		Collection<Person> people=this.getPeople();
		String[] results=null;
		if (!people.isEmpty())
		{
			results=new String[people.size()];
			int on=0;
			for (Person person : people)
			{
				results[on]=person.getWesternName().toString();
				on++;
			}
		}
		return results;
	}

	public String[] getTo(BaseContactDetail.CategoryTypes categoryType)
	{
		Collection<String> addresses=new ArrayList<>();
		for (BasePrincipal principal : this.getReceivers())
		{
			for (Email email : principal.getEmailAddresses(categoryType))
			{
				if (!addresses.contains(email.fetchValue().getValue()))
				{
					addresses.add(email.fetchValue().getValue());
				}
			}
		}
		String[] results=null;
		if (!addresses.isEmpty())
		{
			results=new String[addresses.size()];
			int on=0;
			for (String address : addresses)
			{
				results[on]=address;
				on++;
			}
		}
		return results;
	}

	public String[] getInitiator()
	{
		String[] results=new String[this.getInitiators().size()];
		int on=0;
		for (Person person : this.getInitiators())
		{
			results[on]=person.getWesternName().toString();
			on++;
		}
		return results;
	}

	public Collection<Person> getPeople()
	{
		Collection<Person> results=new ArrayList<>();
		for (BasePrincipal principal : this.getReceivers())
		{
			for (Person person : principal.getPeople())
			{
				if (!results.contains(person))
				{
					results.add(person);
				}
			}
		}
		return results;
	}

	public PrincipalEdges<BasePrincipal> getReceivers()
	{
		if (null==this.receivers)
		{
			this.buildProperty("receivers");
		}
		return this.receivers;
	}

	public ElementEdges<Person> getInitiators()
	{
		if (null==this.initiators)
		{
			this.buildProperty("initiators");
		}
		return this.initiators;
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyLongMethod",
		"MagicNumber"
	})
	public void send(Collection<BasePrincipal> recipients, String templateKey, BaseContactDetail.CategoryTypes categoryType, String content)
		throws Exception
	{
		Environment.getInstance().getReportHandler().info("Send email: start line 182");
		Object o=EmailConfiguration.getInstance().getProperty(templateKey);
		if (o instanceof String)
		{
			Environment.getInstance().getReportHandler().info("Send email: found email config for key, line %d.", 186);
			Environment.getInstance().getReportHandler().info(o.toString());
			String path=(String) o;
			File file=new File(Environment.getInstance().getConfig().getResourcePath(), path);
			if (file.exists())
			{
				Environment.getInstance().getReportHandler().info("Send email: template found at path %s, line %d.", o.toString(), 192);
				String template=StringX.readFile(file.getAbsolutePath(), Charset.forName("UTF-8"));
				if (!StringX.isBlank(template))
				{
					Environment.getInstance().getReportHandler().info("Send email: template retrieved at path %s, line %d.", o.toString(), 196);
					String[] who=this.getWho();
					if ((null==who) || (who.length==0))
					{
						Environment.getInstance().getReportHandler().info("Send email: who is empty, line %d.", 199);
					}
					String sender=EmailX.getDefaultFrom();
					if (StringX.isBlank(sender))
					{
						Environment.getInstance().getReportHandler().info("Send email: sender is empty, line %d.", 203);
					}
					String[] to=this.getTo(recipients, categoryType);
					if ((null==to) || (to.length==0))
					{
						Environment.getInstance().getReportHandler().info("Send email: to is empty, line %d.", 207);
					}
					// todo get initiators nad pass to getCustom.
					String[] initiator=this.getInitiator();
					if ((null==initiator) || (initiator.length==0))
					{
						Environment.getInstance().getReportHandler().info("Send email: initiator is empty, line %d.", 212);
					}

					String body=EmailTemplate.getCustom(template, who, sender, initiator, this.fetchTitle().getValue(), content, this.getDescription(), null, null);

					if (!StringX.isBlank(body))
					{
						Environment.getInstance().getReportHandler().info("Send email: email is being sent, line %d.", 219);
						EmailX.sendMail(
							EmailX.getDefaultServerKey(), EmailX.getDefaultFrom(), to, null, new String[]{sender}, this.fetchTitle().getValue(), body, false, null
						);
					}
					else
					{
						Environment.getInstance().getReportHandler().info("Send email: body is empty, line %d.", 224);
					}
				}
				else
				{
					Environment.getInstance().getReportHandler().severe("Could not retrieve template for key, %s.", (StringX.isBlank(templateKey) ? "unknown key" : templateKey));
				}
			}
			else
			{
				Environment.getInstance().getReportHandler().info("Send email: template not found at path %s, line %d.", o.toString(), 214);
			}
		}
		else
		{
			Environment.getInstance().getReportHandler().severe("Could not retrieve email config fir key, %s.", (StringX.isBlank(templateKey) ? "unknown key" : templateKey));
		}
	}

	public String[] getTo(Collection<BasePrincipal> recievers, BaseContactDetail.CategoryTypes categoryType)
	{
		Collection<String> addresses=new ArrayList<>();
		for (BasePrincipal principal : recievers)
		{
			for (Email email : principal.getEmailAddresses(categoryType))
			{
				if (!addresses.contains(email.fetchValue().getValue()))
				{
					addresses.add(email.fetchValue().getValue());
				}
			}
		}
		String[] results=null;
		if (!addresses.isEmpty())
		{
			results=new String[addresses.size()];
			int on=0;
			for (String address : addresses)
			{
				results[on]=address;
				on++;
			}
		}
		return results;
	}

	// Getters and setters
}
