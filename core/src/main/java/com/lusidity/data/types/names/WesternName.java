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

package com.lusidity.data.types.names;

import com.lusidity.data.types.interfaces.Convertible;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.text.StringBuilderX;
import com.lusidity.framework.text.StringX;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WesternName
	extends PersonalName
	implements Convertible
{
// ------------------------------ FIELDS ------------------------------

	private static final Pattern WHITESPACE_PATTERN=Pattern.compile("\\s+");
	private static final Pattern LAST_NAME_SUFFIX_PATTERN=Pattern.compile("^[A-z].+\\,$");
	private static final Pattern STANDARD_PREFIX_PATTERN=Pattern.compile("^[A-Z][a-z]{1,2}\\.$");
	private static final Pattern UPPERCASE_PREFIX_PATTERN=Pattern.compile("^[A-Z]{2,4}\\.?$");
	private static final Pattern COMMA_PREFIX_PATTERN=Pattern.compile(",");
	private static final Pattern ENDS_WITH_PATTERN=Pattern.compile("\\b[\\.\\,]");
	private static final Pattern NUMERIC_PATTERN=Pattern.compile("[0-9][0-9\\-0-9]");

	private static final long serialVersionUID=1L;

	private String firstName=null;
	private String lastName=null;
	private String middleName=null;
	private String prefix=null;
	private String suffix=null;

// -------------------------- STATIC METHODS --------------------------

/*
 *
 * Static Methods
 *
 */

// Constructors
	/**
	 * Initializing constructor.
	 *
	 * @param str String to parse as a Western name.
	 */
	public WesternName(String str)
	{
		super(str);
		this.convertFrom(str);
	}

	public WesternName()
	{
		super();
	}

// --------------------------- CONSTRUCTORS ---------------------------

/*
 *
 * Constructors
 *
 */

	@Override
	public void convertFrom(Object srcValue)
	{
		Class srcClass=(null==srcValue) ? null : srcValue.getClass();

		//noinspection StatementWithEmptyBody
		if (null==srcClass)
		{
			//  This is an empty Text and is required for updating.
		}
		else if (ClassX.isKindOf(srcClass, String.class))
		{
			this.parse(srcValue);
		}
		else if (ClassX.isKindOf(srcClass, JSONObject.class))
		{
			JSONObject jsonObject=(JSONObject) srcValue;
			Iterator keys=jsonObject.keys();
			Field[] fields=this.getClass().getDeclaredFields();
			boolean valSet=false;
			try
			{
				//noinspection MethodCallInLoopCondition
				while (keys.hasNext())
				{
					String key=(String) keys.next();
					Object val=null;

					if (!jsonObject.isNull(key))
					{
						val=jsonObject.get(key);
					}

					for (Field field : fields)
					{
						String fieldName=field.getName();
						if (fieldName.equals(key))
						{
							if (jsonObject.isNull(key))
							{
								//  null from json object is not supported.
								val=null;
							}
							field.set(this, val);
							valSet=true;
							break;
						}
					}
				}
				if (!valSet)
				{
					throw new ClassCastException(
						String.format("Cannot convert from '%s'.", srcClass.getName())
					);
				}
			}
			catch (Exception ignored)
			{
				throw new ClassCastException(
					String.format("Cannot convert from '%s'.", srcClass.getName())
				);
			}
		}
		else
		{
			throw new ClassCastException(
				String.format("Cannot convert from '%s'.", srcClass.getName())
			);
		}
	}

	@Override
	public Object convertTo(Class<?> cls)
		throws ClassCastException
	{
		Object result;

		if (ClassX.isKindOf(cls, String.class))
		{
			result=this.toString();
		}
		else
		{
			throw new ClassCastException(
				String.format("Cannot convert from '%s' to '%s'.", this.getClass().getName(), cls.getName())
			);
		}

		return result;
	}

	@Override
	public boolean canConvertFrom(Class<?> cls)
	{
		return (null==cls) || cls.isAssignableFrom(String.class) || (cls.isAssignableFrom(JSONObject.class));
	}

	@Override
	public boolean canConvertTo(Class<?> cls)
	{
		return cls.isAssignableFrom(String.class);
	}

// --------------------- GETTER / SETTER METHODS ---------------------

	private void parse(Object str)
	{
		String name=WesternName.cleanup(str.toString());
		String[] srcValues=WesternName.WHITESPACE_PATTERN.split(name);
		int current=0;
		boolean hasPrefix=false;
		boolean hasSuffix=false;
		Matcher lastNameSuffixMatcher;
		for (String value : srcValues)
		{
			switch (current)
			{
				case 0:
					Matcher prefixStandardMatcher=WesternName.STANDARD_PREFIX_PATTERN.matcher(value);
					Matcher prefixUpperCaseMatcher=WesternName.UPPERCASE_PREFIX_PATTERN.matcher(value);
					if (prefixStandardMatcher.matches() || prefixUpperCaseMatcher.matches())
					{
						this.prefix=value;
						hasPrefix=true;
					}
					else
					{
						this.firstName=value;
					}
					break;
				case 1:
					if (StringX.isBlank(this.firstName))
					{
						this.firstName=value;
					}
					else if (srcValues.length==2)
					{
						this.lastName=value;
					}
					else if (srcValues.length>=3)
					{
						lastNameSuffixMatcher=WesternName.LAST_NAME_SUFFIX_PATTERN.matcher(value);
						if (!lastNameSuffixMatcher.matches() &&
						    ((hasPrefix && (srcValues.length>=4)) || (!hasPrefix && (srcValues.length>=3))))
						{
							this.middleName=value;
						}
						else
						{
							if (lastNameSuffixMatcher.matches())
							{
								this.lastName=value.substring(0, value.length()-1);
								hasSuffix=true;
							}
							else
							{
								this.lastName=value;
							}
						}
					}
					break;
				case 2:
					lastNameSuffixMatcher=WesternName.LAST_NAME_SUFFIX_PATTERN.matcher(value);
					if (hasSuffix)
					{
						this.suffix=value;
					}
					else if (!lastNameSuffixMatcher.matches() && StringX.isBlank(this.middleName) &&
					         ((hasPrefix && (srcValues.length>=4)) || (!hasPrefix && (srcValues.length>=3))))
					{
						this.middleName=value;
					}
					else
					{
						if (lastNameSuffixMatcher.matches())
						{
							this.lastName=value.substring(0, value.length()-1);
							hasSuffix=true;
						}
						else
						{
							this.lastName=value;
						}
					}
					break;
				case 3:
					lastNameSuffixMatcher=WesternName.LAST_NAME_SUFFIX_PATTERN.matcher(value);
					if (hasSuffix)
					{
						this.suffix=value;
					}
					else
					{
						if (lastNameSuffixMatcher.matches())
						{
							this.lastName=value.substring(0, value.length()-1);
							hasSuffix=true;
						}
						else
						{
							this.lastName=value;
						}
					}
					break;
				case 4:
					if (hasSuffix)
					{
						this.suffix=value;
					}
					else if (!StringX.isBlank(value))
					{
						this.middleName=
							(StringX.isBlank(this.middleName)) ? this.lastName : String.format("%s %s", this.middleName, this.lastName);
						this.lastName=value;
					}
					break;
				case 5:
					if (hasSuffix)
					{
						this.suffix=value;
					}

				default:
					break;
			}
			current++;
		}
	}

	private static String cleanup(String str)
	{
		StringBuffer sb=new StringBuffer();
		String name=str;

		if (name.endsWith(","))
		{
			name=name.substring(name.length()-1);
		}

		name=WesternName.NUMERIC_PATTERN.matcher(name).replaceAll("");

		if (str.contains(","))
		{
			String[] split=name.split(WesternName.COMMA_PREFIX_PATTERN.pattern());
			name=WesternName.COMMA_PREFIX_PATTERN.matcher(name).replaceAll("");
			String[] names=WesternName.WHITESPACE_PATTERN.split(name);

			Matcher prefixStandardMatcher=WesternName.STANDARD_PREFIX_PATTERN.matcher(split[0]);
			boolean hasPrefix=false;
			if (prefixStandardMatcher.matches())
			{
				sb.append(split[0]);
				hasPrefix=true;
			}

			String last=null;
			for (int i=(names.length-1); i>-1; i--)
			{
				if (hasPrefix && (i==0))
				{
					continue;
				}

				String part=names[i];

				if (i==(names.length-1))
				{
					Matcher lastNameSuffixMatcher=WesternName.LAST_NAME_SUFFIX_PATTERN.matcher(part);
					if (lastNameSuffixMatcher.matches())
					{
						last=part;
						continue;
					}
				}

				if (0<sb.length())
				{
					sb.append(' ');
				}

				sb.append(WesternName.ENDS_WITH_PATTERN.matcher(part).replaceAll(""));
			}

			if (!StringX.isBlank(last))
			{
				sb.append(last);
			}

			name=sb.toString();
		}

		return name;
	}

	@SuppressWarnings("unused")
	public WesternName(JSONObject jsonObject)
	{
		super();
		this.convertFrom(jsonObject);
	}

	/**
	 * Initializing constructor. Any initializer may be null.
	 *
	 * @param prefix     Prefix.
	 * @param firstName  First name.
	 * @param middleName Middle name.
	 * @param lastName   Last name.
	 * @param suffix     Suffix.
	 */
	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public WesternName(String prefix, String firstName, String middleName, String lastName, String suffix)
	{
		super();
		this.setPrefix(prefix);
		this.setFirstName(firstName);
		this.setMiddleName(middleName);
		this.setLastName(lastName);
		this.setSuffix(suffix);
	}

// Overrides
	@Override
	public int hashCode()
	{
		int result=(this.firstName!=null) ? this.firstName.hashCode() : 0;
		result=(31*result)+((this.lastName!=null) ? this.lastName.hashCode() : 0);
		result=(31*result)+((this.middleName!=null) ? this.middleName.hashCode() : 0);
		result=(31*result)+((this.prefix!=null) ? this.prefix.hashCode() : 0);
		result=(31*result)+((this.suffix!=null) ? this.suffix.hashCode() : 0);
		return result;
	}

	@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
	@Override
	public boolean equals(Object o)
	{
		if (this==o)
		{
			return true;
		}
		if ((o==null) || (this.getClass()!=o.getClass()))
		{
			return false;
		}

		WesternName that=(WesternName) o;

		if ((this.firstName!=null) ? !this.firstName.equals(that.firstName) : (that.firstName!=null))
		{
			return false;
		}
		if ((this.lastName!=null) ? !this.lastName.equals(that.lastName) : (that.lastName!=null))
		{
			return false;
		}
		if ((this.middleName!=null) ? !this.middleName.equals(that.middleName) : (that.middleName!=null))
		{
			return false;
		}
		if ((this.prefix!=null) ? !this.prefix.equals(that.prefix) : (that.prefix!=null))
		{
			return false;
		}
		//noinspection RedundantIfStatement
		if ((this.suffix!=null) ? !this.suffix.equals(that.suffix) : (that.suffix!=null))
		{
			return false;
		}

		return true;
	}

	@Override
	public String toString()
	{
		StringBuffer sb=new StringBuffer();
		StringBuilderX.smartAppend(sb, this.prefix, null);
		StringBuilderX.smartAppend(sb, this.firstName, " ");
		StringBuilderX.smartAppend(sb, this.middleName, " ");
		StringBuilderX.smartAppend(sb, this.lastName, " ");
		StringBuilderX.smartAppend(sb, this.suffix, ", ");
		return sb.toString();
	}

	@Override
	public String toIndex()
	{
		return this.toString();
	}

// Methods
	/**
	 * @param value The object that contains the value to convert.
	 * @return Returns a name.
	 */
	@SuppressWarnings("unused")
	public static WesternName convert(Object value)
	{
		WesternName result=null;
		if (null!=value)
		{
			Class valueClass=value.getClass();
			if (WesternName.class.equals(valueClass))
			{
				result=(WesternName) value;
			}
			else if (JSONObject.class.equals(valueClass))
			{
				result=new WesternName();
				JSONObject jsonObject=(JSONObject) value;
				Iterator keys=jsonObject.keys();
				Field[] fields=result.getClass().getDeclaredFields();
				boolean valSet=false;
				try
				{
					//noinspection MethodCallInLoopCondition
					while (keys.hasNext())
					{
						String key=(String) keys.next();
						Object val=jsonObject.get(key);
						if (null!=val)
						{
							for (Field field : fields)
							{
								String fieldName=field.getName();
								if (fieldName.equals(key))
								{
									field.set(result, val);
									valSet=true;
									break;
								}
							}
						}
					}
					if (!valSet)
					{
						result=null;
					}
				}
				catch (Exception e)
				{
					result=null;
					/*  TODO: The error should be logged or returned. */
				}
			}
			else if (String.class.equals(valueClass))
			{
				result=null;
				/*  TODO: Parsing the String could return negative results. */
			}
		}
		return result;
	}

	public static WesternName fromEmail(String email)
	{
		String first=StringX.getFirst(email, "@");
		return new WesternName(StringX.replace(first, ".", " "));
	}

	@SuppressWarnings("MethodMayBeStatic")
	private void readObject(ObjectInputStream inputStream)
		throws ClassNotFoundException, IOException
	{
		inputStream.defaultReadObject();
	}

// ------------------------ CANONICAL METHODS ------------------------

	private void writeObject(ObjectOutputStream outputStream)
		throws IOException
	{
		outputStream.defaultWriteObject();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Convertible ---------------------

// Getters and setters
	public String getFirstName()
	{
		return this.firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName=firstName;
	}

	public String getLastName()
	{
		return this.lastName;
	}


// -------------------------- OTHER METHODS --------------------------

	public void setLastName(String lastName)
	{
		this.lastName=lastName;
	}

	public String getMiddleName()
	{
		return this.middleName;
	}

	public void setMiddleName(String middleName)
	{
		this.middleName=middleName;
	}

	public String getPrefix()
	{
		return this.prefix;
	}

	public void setPrefix(String prefix)
	{
		this.prefix=prefix;
	}

	@SuppressWarnings("unused")
	public String getSuffix()
	{
		return this.suffix;
	}

	public void setSuffix(String suffix)
	{
		this.suffix=suffix;
	}
}
