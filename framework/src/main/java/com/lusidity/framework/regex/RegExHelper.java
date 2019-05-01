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

package com.lusidity.framework.regex;

import com.lusidity.framework.text.StringX;

import java.util.regex.Pattern;

public
class RegExHelper
{
	public static final Pattern SPECIAL_CHARACTERS_PATTERN=Pattern.compile("[^\\w\\s]");
	public static final Pattern WHITE_SPACE=Pattern.compile("\\s");
	public static final Pattern HTML_TAGS_PATTERN=Pattern.compile("\\<.*?\\>");
	public static final Pattern STARTS_WITH_DIGIT=Pattern.compile("^\\d\\w*");
	public static final Pattern SUBTITLE_PATTERN=Pattern.compile(
		"([\\w\\s]+?)([^\\w\\s]"+".+)"
	);
	public static final Pattern PATTERN_HTML=Pattern.compile(".+<.+?>.+");
	public static final Pattern URL_SPLIT_PATTERN=Pattern.compile("/");
	public static final Pattern ENTITY_URI_PATTERN=Pattern.compile("^/entities/(\\d+)");
	public static final Pattern DOT=Pattern.compile("\\.");
	public static final Pattern MAC=Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
	public static final Pattern MAC_OUI=Pattern.compile("^([0-9A-Fa-f]{2}[:-]){2}([0-9A-Fa-f]{2})$");
	public static final Pattern IP=Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."+
	                                               "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."+
	                                               "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."+
	                                               "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	public static final Pattern MAC_RANGE=
		Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})\\/([0-9A-Fa-f]{2})$");
	;
	private static final Pattern EMAIL_PATTERN=Pattern.compile(
		"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
	public static final Pattern UNICODE_PATTERN=Pattern.compile("(.*)([^\\x20-\\x7E])(.*)");
	public static final Pattern BASE64_ENCODED_PATTERN=Pattern.compile(
		"^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$"
	);

	private static final String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
	public static final Pattern TIME_24_HOURS_PATTERN = Pattern.compile(RegExHelper.TIME24HOURS_PATTERN);

	/**
	 * Private default constructor for utility class.
	 */
	private
	RegExHelper()
	{
		super();
	}

/*
 *
 * Static Methods
 *
 */

	/**
	 * Dynamic create a regular expression that matches any of the specified strings.
	 *
	 * @param strings Strings to match.
	 * @return Regular expression for matching any of the strings.
	 */
	public static
	String anyOf(Iterable<String> strings)
	{
		boolean first=true;
		StringBuffer sb=new StringBuffer("(");
		for (String str : strings)
		{
			if (!first)
			{
				sb.append('|');
			}
			else
			{
				first=false;
			}
			sb.append(Pattern.quote(str));
		}
		sb.append(')');
		return sb.toString();
	}

	/**
	 * Validate hex with regular expression
	 *
	 * @param value hex for validation
	 * @return true valid hex, false invalid hex
	 */
	public static
	boolean isValidEmail(String value)
	{
		return !StringX.isBlank(value) && RegExHelper.EMAIL_PATTERN.matcher(value).matches();
	}
}
