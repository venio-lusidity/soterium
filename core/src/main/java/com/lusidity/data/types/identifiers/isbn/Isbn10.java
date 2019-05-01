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

package com.lusidity.data.types.identifiers.isbn;

import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.regex.RegExHelper;
import com.lusidity.framework.text.StringX;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

@SuppressWarnings("UnusedDeclaration")
public class Isbn10
	extends IsbnAware
{
// ------------------------------ FIELDS ------------------------------

// Fields
	public static final int SIZE=10;
	public static final int TRUNCATED_SIZE=9;
	private static final Pattern ISBN10_PATTERN=Pattern.compile("[\\dX]{10}");
	private static final Pattern ISBN10_URI_PATTERN=Pattern.compile("^urn:isbn:[\\dX]{10}");
	private static final String ISBN10_URI_FORMAT="urn:isbn:%s";

// --------------------------- CONSTRUCTORS ---------------------------

// Constructors
	public Isbn10(URI uri)
		throws ApplicationException
	{
		super();
		if (Isbn10.isValid(uri))
		{
			String schemeSpecificPart=uri.getSchemeSpecificPart();
			this.setValue(StringX.substringAfter(schemeSpecificPart, ":"));
		}
		else
		{
			throw new ApplicationException("Not a valid ISBN-10 URI.");
		}
	}

	public static boolean isValid(URI uri)
	{
		return (uri!=null) && Isbn10.ISBN10_URI_PATTERN.matcher(uri.toString()).matches();
	}

	public Isbn10(String value)
	{
		super();
		if (Isbn10.isValid(value))
		{
			this.setValue(value);
		}
	}

	@SuppressWarnings("TypeMayBeWeakened")
	public static boolean isValid(String str)
	{
		return !StringX.isBlank(str) && Isbn10.ISBN10_PATTERN.matcher(str).matches()
		       && RegExHelper.STARTS_WITH_DIGIT.matcher(str).matches();
	}

// ------------------------ CANONICAL METHODS ------------------------

// Overrides
	@Override
	public int hashCode()
	{
		return this.getValue().hashCode();
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
			Isbn10 that=(Isbn10) o;

			result=this.getValue().equals(that.getValue());
		}

		return result;
	}

	@Override
	public String toString()
	{
		return this.getValue();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface UriAware ---------------------

	@SuppressWarnings("RefusedBequest")
	@Override
	public URI toUri()
	{
		URI result;
		try
		{
			result=(!StringX.isBlank(this.getValue())) ? new URI(String.format(Isbn10.ISBN10_URI_FORMAT, this.getValue())) : null;
		}
		catch (URISyntaxException ignored)
		{
			result=null;
		}
		return result;
	}
}
