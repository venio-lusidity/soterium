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

package com.lusidity.domains.common;

import com.lusidity.Environment;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.regex.RegExHelper;
import com.lusidity.framework.text.StringX;

@SuppressWarnings("UnusedDeclaration")
@AtSchemaClass(name="Email", discoverable=false, writable=true, description="An Email for a contact")
public class Email extends BaseContactDetail
{

// Constructors
	public Email()
	{
		super();
	}

	public Email(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public Email(BaseContactDetail.CategoryTypes category, String value)
	{
		super(category);
		this.load(value);
	}

	@Override
	public boolean equals(Object o)
	{
		boolean result = super.equals(o);
		if( !result && ClassX.isKindOf(o, Email.class)){
			Email other = (Email)o;
			result = StringX.equalsIgnoreCase(this.fetchValue().getValue(), other.fetchValue().getValue());
		}
		return result;
	}

	@Override
	protected void load(String value)
	{
		if (StringX.isBlank(value))
		{
			Environment.getInstance().getReportHandler().info("The ContactInfo value cannot be null.");
		}

		if (RegExHelper.isValidEmail(value))
		{
			this.fetchValue().setValue(value);
			this.valid=true;
		}
		else
		{
			this.fetchValue().setValue(null);
			Environment.getInstance().getReportHandler().info("The PhoneNumber is not in an expected format");
		}
	}

	@Override
	public String getMetaSummary()
	{
		return StringX.smartAppend(String.format("%s:",
			BaseContactDetail.CategoryTypes.getName(this.fetchCategory().getValue())),
			String.format("<a href=\"mailto:%s\" style=\"cursor: pointer;\">%s</a>", this.fetchValue().getValue(), this.fetchValue().getValue()),
			"Ext:", this.fetchExtension().getValue()
		);
	}
}
