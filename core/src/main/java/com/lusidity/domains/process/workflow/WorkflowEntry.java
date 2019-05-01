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

package com.lusidity.domains.process.workflow;

import com.lusidity.Environment;
import com.lusidity.data.field.KeyData;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;


/* WARNING!!! do not use any of the collection classes for this class, you could create an endless recursive loop.*/
@AtSchemaClass(name="Log Entry", discoverable=false)
public class WorkflowEntry extends BaseDomain
{
	private KeyData<String> whoId = null;
	private KeyData<String> whatId = null;
	private KeyData<String> whatType = null;
	private KeyData<String> origin = null;
	private KeyData<String> referrer = null;
	private KeyData<String> comment = null;
	private KeyData<String> action = null;

// Constructors
	public WorkflowEntry()
	{
		super();
	}

	@SuppressWarnings("unused")
	public WorkflowEntry(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

// Methods
	/* WARNING!!! do not use any of the collection classes for this class, you could create an endless recursive loop.*/
	public static synchronized void makeEntry(WorkflowItem what, BasePrincipal principal, String action, String comment)
	{
		try
		{
			WorkflowEntry result=new WorkflowEntry();
			result.fetchWhoId().setValue((null!=principal) ? principal.fetchId().getValue() : null);
			result.fetchAction().setValue(action);
			result.fetchWhatId().setValue(what.fetchId().getValue());
			result.fetchTitle().setValue(what.fetchTitle().getValue());
			result.fetchWhatType().setValue(what.fetchVertexType().getValue());
			result.fetchOrigin().setValue((null!=what.getCredentials()) ? what.getCredentials().getOrigin() : null);
			result.fetchReferrer().setValue((null!=what.getCredentials()) ? what.getCredentials().getReferrer() : null);
			result.fetchComment().setValue(comment);
			result.save();
			what.getActions().add(result);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

// Getters and setters
	public KeyData<String> fetchComment()
	{
		if(null==this.comment){
			this.comment = new KeyData<>(this, "comment", String.class, false, null);
		}
		return this.comment;
	}

	@SuppressWarnings("unused")
	public KeyData<String> fetchWhatId()
	{

		if(null==this.whatId){
			this.whatId = new KeyData<>(this, "whatId", String.class, false, null);
		}
		return this.whatId;
	}

	@SuppressWarnings("unused")
	public KeyData<String> fetchWhatType()
	{

		if(null==this.whatType){
			this.whatType = new KeyData<>(this, "whatType", String.class, false, null);
		}
		return this.whatType;
	}

	@SuppressWarnings("unused")
	public KeyData<String> fetchWhoId()
	{

		if(null==this.whoId){
			this.whoId = new KeyData<>(this, "whoId", String.class, false, null);
		}
		return this.whoId;
	}

	public KeyData<String> fetchAction()
	{

		if(null==this.action){
			this.action = new KeyData<>(this, "action", String.class, false, null);
		}
		return this.action;
	}

	public KeyData<String> fetchOrigin()
	{

		if(null==this.origin){
			this.origin = new KeyData<>(this, "origin", String.class, false, null);
		}
		return this.origin;
	}

	public KeyData<String> fetchReferrer()
	{

		if(null==this.referrer){
			this.referrer = new KeyData<>(this, "referrer", String.class, false, null);
		}
		return this.referrer;
	}
}
