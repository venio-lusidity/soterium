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

package com.lusidity.data.types.identifiers.gs1;


import com.lusidity.data.types.identifiers.UriAware;

import java.net.URI;
import java.net.URISyntaxException;

public class Gs1CompanyPrefix
	implements UriAware
{
// ------------------------------ FIELDS ------------------------------

	private static final long serialVersionUID=1L;

	private static final String GCP_URI_FORMAT="urn:epc:id:gcp:%s";
	private String value;

// --------------------------- CONSTRUCTORS ---------------------------

// Constructors
	public Gs1CompanyPrefix(String value)
	{
		super();
		this.value=value;
	}

// --------------------- GETTER / SETTER METHODS ---------------------

// Overrides
	@Override
	public int hashCode()
	{
		return (this.value!=null) ? this.value.hashCode() : 0;
	}

	@Override
	public boolean equals(Object o)
	{
		boolean result;

		if (this==o)
		{
			result=true;
		}
		else if ((o==null) || (this.getClass()!=o.getClass()))
		{
			result=false;
		}
		else
		{
			Gs1CompanyPrefix that=(Gs1CompanyPrefix) o;

			result=(this.value!=null) ? !this.value.equals(that.value) : (that.value==null);
		}

		return result;
	}

// ------------------------ CANONICAL METHODS ------------------------

	@Override
	public String toString()
	{
		return this.value;
	}

	@Override
	public URI toUri()
	{
		URI result;
		if (this.value!=null)
		{
			String s=String.format(Gs1CompanyPrefix.GCP_URI_FORMAT, this.value);
			try
			{
				result=new URI(s);
			}
			catch (URISyntaxException ignored)
			{
				result=null;
			}
		}
		else
		{
			result=null;
		}
		return result;
	}

// Getters and setters
	public String getValue()
	{
		return this.value;
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface UriAware ---------------------

	public void setValue(String value)
	{
		this.value=value;
	}
}
