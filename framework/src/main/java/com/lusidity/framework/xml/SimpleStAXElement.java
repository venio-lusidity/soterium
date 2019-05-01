/*
 * Copyright (c) 2008-2012, Venio, Inc.
 * All Rights Reserved Worldwide.
 *
 * This computer software is protected by copyright law and international treaties.
 * It may not be duplicated, reproduced, distributed, compiled, executed,
 * reverse-engineered, or used in any other way, in whole or in part, without the
 * express written consent of Venio, Inc.
 *
 * Portions of this computer software also embody trade secrets, patents, and other
 * protected intellectual property of Venio, Inc. and third parties and are subject to
 * applicable laws, regulations, treaties, agreements, and other legal mechanisms.
 */

package com.lusidity.framework.xml;

import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.text.StringX;
import org.joda.time.DateTime;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import java.net.URI;
import java.net.URL;
import java.util.Date;

/** User: jjszucs Date: 8/1/12 Time: 14:55 */
@SuppressWarnings("unused")
public
class SimpleStAXElement
{
	/*
	 *
	 * Fields
	 *
	 */
	private AttributesImpl attributes;
	private String localName;
/*
 *
 * Constructors
 *
 */

	/** Default constructor. */
	@SuppressWarnings("unused")
	public
	SimpleStAXElement()
	{
		super();

		this.initialize();
	}

	private
	void initialize()
	{
		this.attributes = new AttributesImpl();
		this.localName = null;
	}

	@SuppressWarnings("unused")
	public
	SimpleStAXElement(String localName)
	{
		super();

		this.initialize();

		this.localName = localName;
	}

	/*
	 *
	 * Getter/Setter Methods
	 *
	 */
	public
	Attributes getAttributes()
	{
		return this.attributes;
	}

	@SuppressWarnings("unused")
	public
	String getLocalName()
	{
		return this.localName;
	}

	@SuppressWarnings("unused")
	public
	void setLocalName(String localName)
	{
		this.localName = localName;
	}

/*
 *
 * Other Methods
 *
 */

	/**
	 * Add an attribute, automatically determining the appropriate type. IMPORTANT: Only string, decimal, dateTime, and
	 * anyURI types are currently supported.
	 *
	 * @param localName
	 * 	Local name of attribute.
	 * @param value
	 * 	Value of attribute.
	 */
	@SuppressWarnings("unused")
	public
	void addAttribute(String localName, Object value)
	{
		String xmlValue, xmlType;
		Class valueType = value.getClass();
		if (ClassX.isNumeric(valueType)) {
			xmlValue = value.toString();
			xmlType = "decimal";
		}
		else if (ClassX.isKindOf(valueType, URI.class) || ClassX.isKindOf(valueType, URL.class)) {
			xmlValue = value.toString();
			xmlType = "anyURI";
		}
		else if (ClassX.isKindOf(valueType, DateTime.class) || ClassX.isKindOf(valueType, Date.class)) {
			xmlValue = value.toString();
			xmlType = "dateTime";
		}
		else {
			xmlValue = value.toString();
			xmlType = "string";
		}

		this.attributes.addAttribute(
			StringX.EMPTY /* uri */, localName, StringX.EMPTY /* qName */, xmlType, xmlValue
		);
	}
}
