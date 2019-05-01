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

@AtSchemaClass(name="Text", discoverable=false, writable=true)
public class Text
	extends Primitive
{
// Fields
	// ------------------------------ FIELDS ------------------------------
	public static final String DEFAULT_LANGUAGE_CODE="en";

	private KeyData<String> value=null;
	private KeyData<String> languageCode=null;
	private KeyData<String> label=null;

// Constructors
	public Text(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public Text()
	{
		this(null, null, null);
	}

// -------------------------- STATIC METHODS --------------------------

	public Text(URI source, String languageCode, String text)
	{
		super(source);
		this.setValues(languageCode, text);
	}

	private void setValues(String languageCode, String text)
	{
		this.fetchLanguageCode().setValue(languageCode);
		this.fetchValue().setValue(text);
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

	public Text(String text)
	{
		this(null, Text.DEFAULT_LANGUAGE_CODE, text);
	}

	public Text(URI source, String text)
	{
		this(source, Text.DEFAULT_LANGUAGE_CODE, text);
	}

// --------------------------- CONSTRUCTORS ---------------------------

	public Text(String languageCode, String text)
	{
		this(null, languageCode, text);
	}

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
		boolean result=super.equals(o);
		if(!result && ClassX.isKindOf(o, Text.class))
		{
			Text that=(Text) o;

			if (this.fetchValue().isNullOrEmpty() && that.fetchValue().isNullOrEmpty())
			{
				result=true;
			}
			else if(this.fetchValue().isNullOrEmpty() && that.fetchValue().isNotNullOrEmpty())
			{
				result=false;
			}
			else if (that.fetchValue().isNullOrEmpty() && this.fetchValue().isNotNullOrEmpty())
			{
				result=false;
			}
			else
			{
				String aUri=this.fetchValue().isNotNullOrEmpty() ? this.fetchValue().getValue() : null;
				String bUri=that.fetchValue().isNotNullOrEmpty() ? that.fetchValue().getValue() : null;
				//noinspection NestedConditionalExpression
				result=StringX.equalsIgnoreCase(aUri, bUri);
			}
		}

		return result;
	}

// ------------------------ CANONICAL METHODS ------------------------

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
	public ExcelSchema getExcelSchema(int index)
	{
		JsonData result=JsonData.createObject();
		result.put("value", JsonData.createObject().put("s_label", "Value").put("idx", index));
		result.put("languageCode", JsonData.createObject().put("s_label", "Language Code").put("idx", index+1));
		return new ExcelSchema(result);
	}

	@Override
	public int compareTo(DataVertex o)
	{
		int result=0;
		if (o instanceof Text)
		{
			Text that=(Text) o;
			String a=StringX.isBlank(this.fetchValue().getValue()) ? null : this.fetchValue().getValue();
			String b=StringX.isBlank(that.fetchValue().getValue()) ? null : that.fetchValue().getValue();
			result=StringX.compare(a, b);
		}
		return result;
	}

// Methods
	public static String getPrimaryIndexValue(String text)
	{
		return text;
	}

	public KeyData<String> fetchLabel()
	{
		if (null==this.label)
		{
			this.label=new KeyData<>(this, "label", String.class, false, null);
		}
		return this.label;
	}

// Getters and setters
	public boolean isEnglish()
	{
		String lc=StringX.isBlank(this.fetchLanguageCode().getValue()) ? null : this.fetchLanguageCode().getValue();
		//noinspection OverlyComplexBooleanExpression
		return (!StringX.isBlank(lc) && ("en".equals(lc) || "eng".equals(lc) || "english".equals(lc)));
	}
}
