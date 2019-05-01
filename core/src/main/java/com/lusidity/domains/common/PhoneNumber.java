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
import com.lusidity.framework.text.StringX;

@SuppressWarnings("UnusedDeclaration")
@AtSchemaClass(name="Phone Number", discoverable=true, description="A phone number for a contact", writable = true)
public class PhoneNumber extends BaseContactDetail
{
// Fields
	public static final int MAX_LENGTH=11;

// Constructors
	public PhoneNumber()
	{
		super();
	}

	public PhoneNumber(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public PhoneNumber(BaseContactDetail.CategoryTypes category, String value, String extension)
	{
		super();
		this.fetchCategory().setValue(category);
		this.fetchExtension().setValue(extension);
		this.fetchValue().setValue(StringX.toPhoneNumber(value));
		this.load(value);
	}

	// Overrides
	@Override
	public boolean equals(Object o)
	{
		boolean result=super.equals(o);
		if (!result && ClassX.isKindOf(o, PhoneNumber.class))
		{
			PhoneNumber other=(PhoneNumber) o;
			result=StringX.equalsIgnoreCase(
				StringX.removeNonAlphaNumericCharacters(this.fetchValue().getValue()),
				StringX.removeNonAlphaNumericCharacters(other.fetchValue().getValue())
			);
		}
		return result;
	}

	@Override
	protected void load(String value)
	{
		String fValue=this.fetchValue().getValue();
		if (StringX.isBlank(fValue))
		{
			Environment.getInstance().getReportHandler().info("The ContactInfo value cannot be null.");
		}
		fValue=StringX.getNumeric(fValue);
		if (!StringX.isBlank(fValue) && (
			(fValue.length()==7) ||
			(fValue.length()==10) ||
			((fValue.length()==PhoneNumber.MAX_LENGTH) && StringX.startsWith(fValue, "1"))
		))
		{
			this.fetchValue().setValue(fValue);
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
			StringX.toPhoneNumber(this.fetchValue().getValue()),
			"Ext:", this.fetchExtension().getValue()
		);
	}

}
