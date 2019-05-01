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

package com.lusidity.data.types.identifiers;

import com.lusidity.Environment;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.text.StringX;

import java.io.*;
import java.net.URI;
import java.util.regex.Pattern;

/**
 * Library of Congress Control Number.
 */

//  TODO: Support for traditional (non-normalized) forms
//  See http://www.loc.gov/marc/lccn-namespace.html for more information.
public class Lccn
	implements Serializable, UriAware
{
// ------------------------------ FIELDS ------------------------------

	private static final long serialVersionUID=1L;

	private static final String LCCN_URI_FORMAT="urn:lccn:%s";
	private static final Pattern PATTERN_HYPHEN=Pattern.compile("-");

	private String value;

// --------------------------- CONSTRUCTORS ---------------------------

// Constructors
	public Lccn(String value)
	{
		super();

		this.value=Lccn.isValid(value) ? value : null;
	}

	public static boolean isValid(String value)
	{
		boolean result=false;

		if (!StringX.isBlank(value))
		{
			//  Rigorous validation of LCCNs is out-of-scope and potentially dangerous; for our purposes, it is
			//  sufficient for the LCCN (without any embedded hyphens) to be a total of 8 or 10 digits long.
			//  see http://en.wikipedia.org/wiki/Library_of_Congress_Control_Number
			String str=Lccn.PATTERN_HYPHEN.matcher(value).replaceAll("");
			int n=str.length();
			result=(n==8) || (n==10);
			if (!result)
			{
				Environment.getInstance().getReportHandler().fine("Invalid LCCN %s", value);
			}
		}

		return result;
	}

	public Lccn(URI uri)
		throws ApplicationException
	{
		super();
		if (Lccn.isValid(uri))
		{
			String schemeSpecificPart=uri.getSchemeSpecificPart();
			this.value=StringX.substringAfter(schemeSpecificPart, ":");
		}
		else
		{
			throw new ApplicationException("Not a valid LCCN URI.");
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
				if (StringX.equals(subScheme, "lccn"))
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
			Lccn that=(Lccn) o;

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
		return (null!=this.value) ? URI.create(String.format(Lccn.LCCN_URI_FORMAT, this.value)) : null;
	}

// -------------------------- OTHER METHODS --------------------------

	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
	}

	private void readObjectNoData()
		throws ObjectStreamException
	{
		this.value=null;
	}

	private void writeObject(ObjectOutputStream out)
		throws IOException
	{
		out.defaultWriteObject();
	}
}
