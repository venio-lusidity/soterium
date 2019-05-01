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

import com.lusidity.Environment;
import com.lusidity.data.types.identifiers.UriAware;
import com.lusidity.data.types.identifiers.isbn.Isbn10;
import com.lusidity.data.types.identifiers.isbn.Isbn13;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.regex.RegExHelper;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.xml.LazyXmlNode;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Global Trade Item Number, an identification scheme for trade items (physical objects) developed by GS1, that encompasses the Universal
 * Product Code (UPC), European Article Number (EAN), International Standard Book Number (ISBN), International Standard Serial Number (ISSN),
 * and other product identification schemes.
 */

@SuppressWarnings("unused")
public class GlobalTradeItemNumber
	implements UriAware
{
// ------------------------------ FIELDS ------------------------------

	private static final Pattern PATTERN_SEPARATORS=Pattern.compile("[\\.\\- ]");
	private static final Pattern PATTERN_PACKED_GTIN=Pattern.compile("^\\d+(?:\\d|X)$");
	private static final String GCP_PREFIX_FORMAT_LIST="resource/data/GCPPrefixFormatList.xml";
	private static final String GTIN_URI_FORMAT="urn:epc:id:gtin:%s.%s";
	private static final int GTIN13_LENGTH=13;
	private static Map<String, Integer> gcpPrefixes=null;
	private String value;

// -------------------------- STATIC METHODS --------------------------

// Constructors
	public GlobalTradeItemNumber(String str)
	{
		this();
		this.setValue(str);
	}

	public GlobalTradeItemNumber()
	{
		super();
		this.value=null;
	}

	public static synchronized void resetGcpPrefixes()
	{
		GlobalTradeItemNumber.gcpPrefixes=new HashMap<>();
	}

// --------------------------- CONSTRUCTORS ---------------------------

	private static String clean(CharSequence str)
	{
		return GlobalTradeItemNumber.PATTERN_SEPARATORS.matcher(str).replaceAll("");
	}

	@SuppressWarnings("MagicNumber")
	public static boolean isValid(CharSequence str)
	{
		boolean result=true;

		String s=GlobalTradeItemNumber.clean(str);

		//  After cleaning, the string should contain 8, 12, 13, or 14 characters
		int length=s.length();
		switch (length)
		{
			case 8:
			case 10:
			case 12:
			case 13:
			case 14:
				break;
			default:
			{
				result=false;
				break;
			}
		}

		if(result)
		{
			result=RegExHelper.STARTS_WITH_DIGIT.matcher(str).matches();

			if (result)
			{
				//  All of the characters must be digits, except for the last character, which
				//  may be a digit or an 'X' (for an ISBN)
				result=GlobalTradeItemNumber.PATTERN_PACKED_GTIN.matcher(s).matches();

				//  TODO: Validate check digit
				//  For more information, see
				//      http://stackoverflow.com/questions/10143547/how-do-i-validate-a-upc-or-ean-code
			}
		}

		return result;
	}

	public GlobalTradeItemNumber(Isbn10 isbn10)
	{
		super();
		//  Convert from ISBN-10 to ISBN-13, then to GTIN-13
		Isbn13 isbn13=new Isbn13(isbn10);
		this.setValue(isbn13.getValue());
	}

	public GlobalTradeItemNumber(Isbn13 isbn13)
	{
		super();
		this.setValue(isbn13.getValue());
	}

// Overrides
	/**
	 * Get GTIN as a URI.
	 *
	 * @return GTIN as a URI, or null if none.
	 */
	@Override
	public URI toUri()
	{
		URI result=null;
		//  See http://bibwild.wordpress.com/2009/05/28/upcs-eans-and-isbns-the-verdict/
		Gs1CompanyPrefix gcp=this.getGcp();
		String itemReference=this.getItemReference();
		if ((null!=gcp) && (null!=itemReference))
		{
			String s=String.format(GlobalTradeItemNumber.GTIN_URI_FORMAT, gcp.toString(), itemReference);
			try
			{
				result=new URI(s);
			}
			catch (URISyntaxException ignored)
			{
			}
		}
		return result;
	}

// --------------------- GETTER / SETTER METHODS ---------------------

	@Override
	public String toString()
	{
		return this.value;
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface UriAware ---------------------

	private static Integer lookupGcpLength(String gtin14)
	{
		Integer result=null;

		Map<String, Integer> gcpPrefixes=GlobalTradeItemNumber.getGcpPrefixes();
		Set<Map.Entry<String, Integer>> gcpEntries=gcpPrefixes.entrySet();
		for (Map.Entry<String, Integer> gcpEntry : gcpEntries)
		{
			String prefix=gcpEntry.getKey();
			Integer gcpLength=gcpEntry.getValue();
			if (gtin14.startsWith(prefix))
			{
				result=gcpLength;
				break;
			}
		}

		return result;
	}

// -------------------------- OTHER METHODS --------------------------

	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
	}

	private void readObjectNoData()
	{
		this.value=null;
	}

	private void writeObject(ObjectOutputStream out)
		throws IOException
	{
		out.defaultWriteObject();
	}

// Getters and setters
	/**
	 * Get value.
	 *
	 * @return Get value.
	 */
	public String getValue()
	{
		return this.value;
	}

	private void setValue(String str)
	{
		String val=GlobalTradeItemNumber.clean(str);
		if (GlobalTradeItemNumber.isValid(val))
		{
			this.value=val;
			if (this.value.length()==10)
			{
				this.value=String.format("%s%s", Isbn13.ISBN10_PREFIX, val);
			}
		}
		else
		{
			throw new IllegalArgumentException("GTIN is invalid.");
		}

	}

	/**
	 * Get GS1 Company Prefix.
	 *
	 * @return GS1 Company Prefix (GCP), or null if none.
	 */
	public Gs1CompanyPrefix getGcp()
	{
		String s=null;
		if (this.isValid())
		{
			String gtin14=this.getGtin14();
			Integer length=GlobalTradeItemNumber.lookupGcpLength(gtin14);
			if (null!=length)
			{
				s=gtin14.substring(1, 1+length);
			}
		}
		return (s!=null) ? new Gs1CompanyPrefix(s) : null;
	}

	/**
	 * Is this GTIN valid?
	 *
	 * @return true if valid.
	 */
	public boolean isValid()
	{
		return GlobalTradeItemNumber.isValid(this.value);
	}

	/**
	 * Get GTIN-14.
	 *
	 * @return GTIN-14.
	 */
	@SuppressWarnings("MagicNumber")
	public String getGtin14()
	{
		//  Pad out to 14 digits
		String s=this.getValue();
		return StringX.leftPad(s, 14, '0');
	}

	private static Map<String, Integer> getGcpPrefixes()
	{
		if (null==GlobalTradeItemNumber.gcpPrefixes)
		{
			GlobalTradeItemNumber.resetGcpPrefixes();

			try
			{
				File lookupFile=new File(GlobalTradeItemNumber.GCP_PREFIX_FORMAT_LIST);
				LazyXmlNode lookupTable=LazyXmlNode.load(lookupFile, false);
				Collection<LazyXmlNode> lookupNodes=lookupTable.getNodesByRelativePath("GCPPrefixFormatList/entry");
				for (LazyXmlNode lookupNode : lookupNodes)
				{
					String prefix=lookupNode.getAttribute("prefix");
					String s=lookupNode.getAttribute("gcpLength");
					Integer gcpLength=Integer.parseInt(s);
					GlobalTradeItemNumber.gcpPrefixes.put(prefix, gcpLength);
				}
			}
			catch (ApplicationException e)
			{
				Environment.getInstance().getReportHandler().severe("Could not load GCP prefixes: %s", e.toString());
			}
		}
		return GlobalTradeItemNumber.gcpPrefixes;
	}

	/**
	 * Get item reference.
	 *
	 * @return Item reference, or null if none.
	 */
	public String getItemReference()
	{
		String result=null;

		String gtin14=this.getGtin14();

		Integer gcpLength=GlobalTradeItemNumber.lookupGcpLength(gtin14);
		if (null!=gcpLength)
		{
			/* Item reference is part of GTIN-14 after GCP */
			result=gtin14.substring(1+gcpLength);
		}

		return result;
	}
}
