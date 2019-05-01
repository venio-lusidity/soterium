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

import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.office.ExcelSchema;

import java.net.URI;
import java.net.URLEncoder;
import java.util.regex.Pattern;

@SuppressWarnings("ClassWithTooManyConstructors")
@AtSchemaClass(name="URI", discoverable=false, writable=true)
public class UriValue extends Primitive
{
	private static final Pattern COMPILE=Pattern.compile("\\%2F");

// --------------------------- CONSTRUCTORS ---------------------------

	private KeyData<URI> value=null;
	private KeyData<String> label=null;

	// Constructors
	public UriValue(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	@SuppressWarnings("UnusedDeclaration")
	public UriValue()
	{
		super();
	}

	public UriValue(URI uri, String label)
	{
		this(Environment.getSourceUri(), uri, label);
	}
	public UriValue(URI sourceUri, URI uri, String label)
	{
		super(sourceUri);
		this.fetchValue().setValue(uri);
		this.fetchLabel().setValue(label);
	}

	public KeyData<URI> fetchValue()
	{
		if (null==this.value)
		{
			this.value=new KeyData<>(this, "value", URI.class, true, null);
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

	public UriValue(URI sourceUri, URI uri)
	{
		super(sourceUri);
		this.fetchValue().setValue(uri);
	}

	@SuppressWarnings("UnusedDeclaration")
	public UriValue(URI uri)
	{
		super();
		this.fetchValue().setValue(uri);
	}

	@SuppressWarnings("UnusedDeclaration")
	public UriValue(String strUri)
	{
		super();
		if (!StringX.isBlank(strUri))
		{
			try
			{
				this.fetchValue().setValue(URI.create(strUri));
			}
			catch (Exception ignored)
			{
				try
				{
					String encoded=URLEncoder.encode(strUri, "UTF-8");
					encoded=UriValue.COMPILE.matcher(encoded).replaceAll("/");
					this.fetchValue().setValue(URI.create(encoded));
				}
				catch (Exception ignore)
				{
				}
			}
		}
	}

	// Overrides
	@Override
	public int hashCode()
	{
		return (this.fetchValue().isNotNullOrEmpty()) ? this.fetchValue().getValue().hashCode() : 0;
	}

// ------------------------ CANONICAL METHODS ------------------------

	@Override
	public boolean equals(Object o)
	{
		boolean result = super.equals(o);

		if(!result)
		{
			if (this==o)
			{
				result=true;
			}
			else if (!(o instanceof UriValue))
			{
				result=false;
			}
			else
			{
				UriValue that=(UriValue) o;

				if (this.fetchValue().isNullOrEmpty() && that.fetchValue().isNullOrEmpty())
				{
					result=true;
				}
				else if (this.fetchValue().isNullOrEmpty() && that.fetchValue().isNotNullOrEmpty())
				{
					result=false;
				}
				else if (that.fetchValue().isNullOrEmpty() && this.fetchValue().isNotNullOrEmpty())
				{
					result=false;
				}
				else
				{
					String aUri=this.fetchValue().isNotNullOrEmpty() ? this.fetchValue().getValue().toString() : null;
					String bUri=that.fetchValue().isNotNullOrEmpty() ? that.fetchValue().getValue().toString() : null;
					//noinspection NestedConditionalExpression
					result=StringX.equalsIgnoreCase(aUri, bUri);
				}
			}
		}

		return result;
	}

	@Override
	public ExcelSchema getExcelSchema(int index)
	{
		JsonData result=JsonData.createObject();
		result.put("value", JsonData.createObject().put("s_label", "URI").put("idx", index));
		result.put("label", JsonData.createObject().put("s_label", "Label").put("idx", index+1));
		return new ExcelSchema(result);
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"StandardVariableNames"
	})
	@Override
	public int compareTo(DataVertex o)
	{
		int result=0;
		if (o instanceof UriValue)
		{
			UriValue that=(UriValue) o;
			String a=((this.fetchValue().isNotNullOrEmpty()) && !StringX.isBlank(this.fetchValue().getValue().toString())) ?
				this.fetchValue().getValue().toString() : null;
			String b=((that.fetchValue().isNotNullOrEmpty()) && !StringX.isBlank(that.fetchValue().getValue().toString())) ?
				that.fetchValue().getValue().toString() : null;
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
			if ((null!=a) && (null!=b))
			{
				result=a.compareTo(b);

			}
		}
		return result;
	}
}
