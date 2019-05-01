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

import com.lusidity.data.field.KeyData;
import com.lusidity.domains.BaseDomain;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;


@SuppressWarnings({
	"UnusedDeclaration",
	"EqualsAndHashcode"
})
@AtSchemaClass(name="Contact Info", discoverable=false, description="Contact Information for a Person", writable = true)
public abstract class BaseContactDetail extends BaseDomain
{
	public enum Locations
	{

	}

	public enum CategoryTypes
	{
		work_cell,
		home_cell,
		work_dsn,
		home_email,
		work_email,
		home_fax,
		work_fax,
		home_mobile,
		work_mobile,
		home_phone,
		work_phone;

		// Methods
		public static String getName(BaseContactDetail.CategoryTypes type)
		{
			return StringX.toTitle(StringX.replace(type.toString(), "_", " "));
		}
	}

	protected transient boolean valid=false;
	private KeyData<String> note=null;
	private KeyData<String> extension=null;
	private KeyData<String> value=null;
	private KeyData<BaseContactDetail.CategoryTypes> category=null;

	protected BaseContactDetail()
	{
		super();
	}

	protected BaseContactDetail(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	protected BaseContactDetail(BaseContactDetail.CategoryTypes category)
	{
		this.fetchCategory().setValue(category);
	}

	public KeyData<BaseContactDetail.CategoryTypes> fetchCategory()
	{
		if (null==this.category)
		{
			this.category=new KeyData<>(this, "category", BaseContactDetail.CategoryTypes.class, false, null);
		}
		return this.category;
	}

	// Overrides
	@Override
	public boolean equals(Object o)
	{
		boolean result=super.equals(o);
		if (!result)
		{
			if (ClassX.isKindOf(o, BaseContactDetail.class))
			{
				BaseContactDetail other=(BaseContactDetail) o;
				result=StringX.equalsIgnoreCase(other.fetchValue().getValue(), this.fetchValue().getValue());
			}
		}
		return result;
	}

	public KeyData<String> fetchValue()
	{
		if (null==this.value)
		{
			this.value=new KeyData<>(this, "value", String.class, false, null);
		}
		return this.value;
	}

	public KeyData<String> fetchNote()
	{
		if (null==this.note)
		{
			this.note=new KeyData<>(this, "note", String.class, false, null);
		}
		return this.note;
	}

	public KeyData<String> fetchExtension()
	{
		if (null==this.extension)
		{
			this.extension=new KeyData<>(this, "extension", String.class, false, null);
		}
		return this.extension;
	}

	@SuppressWarnings("ParameterHidesMemberVariable")
	protected abstract void load(String value);

	// Getters and setters
	public boolean isValid()
	{
		return this.valid;
	}

	public String getMetaSummary()
	{
		return StringX.smartAppend(String.format("%s: %s", BaseContactDetail.CategoryTypes.getName(this.fetchCategory().getValue()), this.fetchValue().getValue()));
	}
}
