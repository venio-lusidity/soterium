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

package com.lusidity.domains.system.primitives;

import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.handler.KeyDataHandlerEncoding;
import com.lusidity.data.handler.KeyDataHandlerLowerCase;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.office.ExcelSchema;

import java.net.URI;

@SuppressWarnings("ClassWithTooManyConstructors")
@AtSchemaClass(name="Text", writable=true, indexable=false, discoverable=false)
public class RawString
	extends Primitive
{
// Fields
	// ------------------------------ FIELDS ------------------------------
	public static final String DEFAULT_LANGUAGE_CODE="en";

	private KeyData<String> value=null;
	private KeyData<String> languageCode=null;
	private KeyData<String> label=null;

// Constructors
	public RawString(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public RawString()
	{
		this(null, null, null, null);
	}
// -------------------------- STATIC METHODS --------------------------

	public RawString(URI source, String languageCode, String text, String label)
	{
		super(source);
		this.setValues(languageCode, text, label);
	}
	private void setValues(String languageCode, String text, String label)
	{
		this.fetchLanguageCode().setValue(languageCode);
		this.fetchValue().setValue(text);
		this.fetchLabel().setValue(label);
	}
	public KeyData<String> fetchLanguageCode()
	{
		if (null==this.languageCode)
		{
			this.languageCode=new KeyData<>(this, "languageCode", String.class, false, RawString.DEFAULT_LANGUAGE_CODE, new KeyDataHandlerLowerCase());
		}
		return this.languageCode;
	}

	public KeyData<String> fetchValue()
	{
		if (null==this.value)
		{
			this.value=new KeyData<>(this, "value", String.class, false, null, new KeyDataHandlerEncoding());
		}
		return this.value;
	}
	public KeyData<String> fetchLabel()
	{
		if (null==this.label)
		{
			this.label=new KeyData<>(this, "label", String.class, true, null);
		}
		return this.label;
	}
// --------------------------- CONSTRUCTORS ---------------------------
public RawString(String text)
{
	this(null, RawString.DEFAULT_LANGUAGE_CODE, text, null);
}

	public RawString(URI source, String text)
	{
		this(source, RawString.DEFAULT_LANGUAGE_CODE, text, null);
	}

	public RawString(String languageCode, String text)
	{
		this(null, languageCode, text,null);
	}

	public RawString(String languageCode,String text,String label)
	{
		this(null, languageCode, text, label);
	}

// ------------------------ CANONICAL METHODS ------------------------

// Overrides
	@Override
	public int hashCode()
	{
		int result=(this.fetchLanguageCode().isNotNullOrEmpty()) ? this.fetchLanguageCode().getValue().hashCode() : 0;
		result=(31*result)+((this.fetchValue().isNotNullOrEmpty()) ? this.fetchValue().getValue().hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object o)
	{
		boolean result=false;
		if (ClassX.isKindOf(o.getClass(), RawString.class))
		{
			RawString that=(RawString) o;
			result=(this.fetchValue().isNotNullOrEmpty()) ? this.fetchValue().getValue().equals(that.fetchValue().getValue()) : (that.fetchValue().isNullOrEmpty());
		}
		return result;
	}

	@Override
	public String toString()
	{
		StringBuffer sb=new StringBuffer();
		if (this.fetchLanguageCode().isNotNullOrEmpty())
		{
			sb.append('[');
			sb.append((String) this.fetchLanguageCode().getValue());
			sb.append(']');
		}
		if (this.fetchValue().isNotNullOrEmpty())
		{
			sb.append(this.fetchValue().getValue());
		}
		return (sb.length()>0) ? sb.toString().trim() : null;
	}

	@Override
	public JsonData toJson(boolean storing, String... languages)
	{
		JsonData result=super.toJson(storing, languages);
		if (!storing)
		{
			result.update("value", this.fetchValue().getValue());
		}
		return result;
	}

	@Override
	public ExcelSchema getExcelSchema(int index)
	{
		JsonData result=JsonData.createObject();
		result.put("value", JsonData.createObject().put("s_label", "Value").put("idx", index));
		result.put("label", JsonData.createObject().put("s_label", "Label").put("idx", index+1));
		result.put("createdWhen", JsonData.createObject().put("s_label", "Created").put("idx", index+2));
		return new ExcelSchema(result);
	}

	@SuppressWarnings("StandardVariableNames")
	@Override
	public int compareTo(DataVertex o)
	{
		int result=0;
		if (o instanceof RawString)
		{
			RawString that=(RawString) o;
			String a=StringX.isBlank(this.fetchValue().getValue()) ? null : this.fetchValue().getValue();
			String b=StringX.isBlank(that.fetchValue().getValue()) ? null : that.fetchValue().getValue();
			if (StringX.equals(a, b))
			{
				result=0;
			}
			else if (StringX.isBlank(a))
			{
				result=-1;
			}
			else if (StringX.isBlank(b))
			{
				result=1;
			}
			else if (!StringX.isAnyBlank(a, b))
			{
				//noinspection ConstantConditions
				result=a.toLowerCase().compareTo(b.toLowerCase());
			}
		}
		return result;
	}
}
