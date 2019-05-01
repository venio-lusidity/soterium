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

package com.lusidity.domains.inspections;

import com.lusidity.annotations.AtIndexedField;
import com.lusidity.data.field.KeyData;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;

@AtIndexedField(key = "required", type = Boolean.class)
@AtSchemaClass(name="POAM Audit Status", discoverable = false)
public
class PoamAuditStatus extends AuditStatus
{
	private KeyData<Boolean> required = null;

	// Constructors
	public PoamAuditStatus(){
		super();
	}
	public PoamAuditStatus(JsonData dso, Object indexId){
		super(dso, indexId);
	}

	public KeyData<Boolean> fetchRequired()
	{
		if (null==this.required)
		{
			this.required=new KeyData<>(this, "required", Boolean.class, true, null);
		}
		return this.required;
	}
}
