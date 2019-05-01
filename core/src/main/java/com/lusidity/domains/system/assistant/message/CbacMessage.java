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

package com.lusidity.domains.system.assistant.message;


import com.lusidity.configuration.ScopedConfiguration;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.handler.KeyDataHandlerStringToClass;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.authorization.IAuthorizationPolicy;

@AtSchemaClass(name="CBAC Message", discoverable=false, description="Permissions to be added or removed.", writable=true)
public class CbacMessage extends AssistantMessage
{

	private KeyData<String> contextRelatedId = null;
	private KeyData<String> positionRelatedId = null;
	private KeyData<LogEntry.OperationTypes> operationType  = null;
	private KeyData<Class<? extends IAuthorizationPolicy>> policy = null;

	// Constructors
	public CbacMessage()
	{
		super();
	}

	@SuppressWarnings("unused")
	public CbacMessage(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	// Overrides
	@Override
	public boolean save(Class<? extends DataVertex> store)
		throws Exception
	{
		boolean result=false;
		if(null!=this.getCredentials())
		{
			boolean authorized=ScopedConfiguration.getInstance().isAccountManager(this.getCredentials());
			if (authorized)
			{
				result=super.save(store);
			}
		}
		return result;
	}

	public KeyData<String> fetchContextRelatedId(){
		if(null==this.contextRelatedId){
			this.contextRelatedId = new KeyData<>(this, "contextRelatedId", String.class, false, null);
		}
		return this.contextRelatedId;
	}
	public KeyData<String> fetchPositionRelatedId(){
		if(null==this.positionRelatedId){
			this.positionRelatedId = new KeyData<>(this, "positionRelatedId", String.class, false, null);
		}
		return this.positionRelatedId;
	}

	public KeyData<LogEntry.OperationTypes> fetchOperationType(){
		if(null==this.operationType){
			this.operationType = new KeyData<>(this, "operationType", LogEntry.OperationTypes.class, false, null);
		}
		return this.operationType;
	}

	public KeyData<Class<? extends IAuthorizationPolicy>> fetchPolicy(){
		if(null==this.policy){
			this.policy = new KeyData<>(this, "policy", Class.class, false, null, new KeyDataHandlerStringToClass());
		}
		return this.policy;
	}
}
