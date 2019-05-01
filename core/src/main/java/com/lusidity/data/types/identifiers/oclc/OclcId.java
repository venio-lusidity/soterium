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

package com.lusidity.data.types.identifiers.oclc;

import com.lusidity.data.types.identifiers.Lccn;
import com.lusidity.data.types.identifiers.UriAware;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.text.StringX;

import java.net.URI;
import java.util.regex.Pattern;

/**
 * Online Computer Library Center (OCLC) identifier.
 */
public class OclcId
	implements UriAware
{
// ------------------------------ FIELDS ------------------------------

	//  TODO: Find and implement real pattern
	private static final Pattern OCLC_ID_PATTERN=Pattern.compile("\\w*");

	private static final String OCLC_URI_FORMAT="urn:oclc:%s";

	private String value;

// --------------------------- CONSTRUCTORS ---------------------------

// Constructors
	public OclcId(String value)
	{
		super();

		this.value=OclcId.isValid(value) ? value : null;
	}

	public static boolean isValid(String value)
	{
		return !StringX.isBlank(value) && OclcId.OCLC_ID_PATTERN.matcher(value).matches();
	}

	public OclcId(URI uri)
		throws ApplicationException
	{
		super();
		if (OclcId.isValid(uri))
		{
			String schemeSpecificPart=uri.getSchemeSpecificPart();
			this.value=StringX.substringAfter(schemeSpecificPart, ":");
		}
		else
		{
			throw new ApplicationException("Not a valid OCLC URI.");
		}
	}

	public static boolean isValid(URI uri)
	{
		boolean result=false;

		if (null!=uri)
		{
			String scheme=uri.getScheme();
			if (scheme.equalsIgnoreCase("urn"))
			{
				String schemeSpecificPart=uri.getSchemeSpecificPart();
				String subScheme=StringX.substringBefore(schemeSpecificPart, ":");
				if (StringX.equals(subScheme, "oclc"))
				{
					String value=StringX.substringAfter(schemeSpecificPart, ":");
					result=Lccn.isValid(value);
				}
			}
		}

		return result;
	}

// ------------------------ CANONICAL METHODS ------------------------

// Overrides
	@Override
	public int hashCode()
	{
		return this.value.hashCode();
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
			OclcId that=(OclcId) o;

			result=this.value.equals(that.value);
		}

		return result;
	}

	@Override
	public String toString()
	{
		return this.value;
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface UriAware ---------------------

	@Override
	public URI toUri()
	{
		return (null!=this.value) ? URI.create(String.format(OclcId.OCLC_URI_FORMAT, this.value)) : null;
	}
}
