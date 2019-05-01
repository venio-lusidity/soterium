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
public class Isbn13
	extends IsbnAware
{
// ------------------------------ FIELDS ------------------------------


// Fields
	public static final String ISBN10_PREFIX="978";
	public static final int SIZE=13;
	public static final int TRUNCATED_SIZE=12;
	private static final Pattern ISBN13_PATTERN=Pattern.compile("([\\dX]{13})|([\\dX]{3}-[\\dX]{10})");
	private static final Pattern ISBN13_URI_PATTERN=Pattern.compile("^urn:isbn:([\\dX]{13})|([\\dX]{3}-[\\dX]{10})$");
	private static final String ISBN13_URI_FORMAT="urn:isbn:%s";

// --------------------------- CONSTRUCTORS ---------------------------

// Constructors
	public Isbn13(String value)
	{
		super();

		if (!StringX.isBlank(value))
		{
			String val=value.replace("-", "");

			if (val.length()==10)
			{
				val=String.format("%s%s", Isbn13.ISBN10_PREFIX, val);
			}

			if (Isbn13.isValid(val))
			{
				this.setValue(val);
			}
			else
			{
				throw new IllegalArgumentException("Invalid ISBN-13.");
			}
		}
		else
		{
			throw new IllegalArgumentException("Invalid ISBN-13.");
		}
	}

	public static boolean isValid(String value)
	{
		return !StringX.isBlank(value)
		       && RegExHelper.STARTS_WITH_DIGIT.matcher(value).matches()
		       && (Isbn13.ISBN13_PATTERN.matcher(value).matches() || (value.length()==10));
	}

	public Isbn13(Isbn10 isbn10)
	{
		super();

		//  Convert from ISBN-10 to ISBN-13 by prepending the constant prefix "978"
		this.setValue(Isbn13.ISBN10_PREFIX+isbn10.getValue());
	}

	public Isbn13(URI uri)
		throws ApplicationException
	{
		super();
		if (Isbn13.isValid(uri))
		{
			String schemeSpecificPart=uri.getSchemeSpecificPart();
			this.setValue(StringX.substringAfter(schemeSpecificPart, ":"));
		}
		else
		{
			throw new ApplicationException("Not a valid ISBN-13 URI.");
		}
	}

	public static boolean isValid(URI uri)
	{
		return (uri!=null) && Isbn13.ISBN13_URI_PATTERN.matcher(uri.toString()).matches();
	}


// ------------------------ CANONICAL METHODS ------------------------

// Overrides
	// --------------------- Interface UriAware ---------------------
	@SuppressWarnings("RefusedBequest")
	@Override
	public URI toUri()
	{
		URI result;
		try
		{
			result=(!StringX.isBlank(this.getValue())) ? new URI(String.format(Isbn13.ISBN13_URI_FORMAT, this.getValue())) : null;
		}
		catch (URISyntaxException ignored)
		{
			result=null;
		}
		return result;
	}

	@Override
	public int hashCode()
	{
		return (!StringX.isBlank(this.getValue())) ? this.getValue().hashCode() : 0;
	}


// ------------------------ INTERFACE METHODS ------------------------

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
			Isbn13 that=(Isbn13) o;

			result=this.getValue().equals(that.getValue());
		}

		return result;
	}

// -------------------------- OTHER METHODS --------------------------

	@Override
	public String toString()
	{
		return this.getValue().substring(0, 3)+'-'+this.getValue().substring(3);
	}
}
