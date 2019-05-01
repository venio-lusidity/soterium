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

package com.lusidity.data.types.interfaces;

import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * Orchestrator for type conversion.
 */
public class Converter
{
	private static final Pattern PATTERN_BACKSLASH=Pattern.compile("\\.");
	private static final Pattern PATTERN_UTC_OFFSET=Pattern.compile("\\+");
	private static final Collection<Class<?>> WRAPPER_TYPES=Converter.getWrapperTypes();

	/**
	 * Private constructor. This is a utility class and cannot be instantiated.
	 */
	private Converter()
	{
		super();
	}

// Methods
	/**
	 * Convert an object to the specified type, using the Convertible interface implementations (if any) of the classes involved.
	 *
	 * @param srcValue  Value to convert.
	 * @param targetCls Target class.
	 * @return Converted object.
	 */
	public static Object convert(Object srcValue, Class<?> targetCls)
		throws ApplicationException
	{
		Object result=srcValue;

		Class srcClass=((null==srcValue) ? null : srcValue.getClass());
		Class dtClass=DateTime.class;

		if (((null==srcValue) || Converter.isPrimitiveType(srcClass)) && Converter.isPrimitiveType(targetCls))
		{
			result=(null==srcValue) ? null : targetCls.cast(srcValue);
		}
		else if (ClassX.isKindOf(targetCls, URI.class))
		{
			if (null!=srcValue)
			{
				result=URI.create(srcValue.toString());
			}
		}
		else if (ClassX.isKindOf(targetCls, dtClass))
		{
			if (null==srcValue)
			{
				result=null;
			}
			else
			{
				String date=srcValue.toString().toLowerCase();
				date=date.replace("/date(", "");
				date=date.replace(")/", "");

				//  Remove the timezone offset value
				if (date.contains("+"))
				{
					String[] parts=Converter.PATTERN_UTC_OFFSET.split(date);
					date=parts[0];
					//  The second value is the time zone offset but we will always use UTC.
				}

				// double values can not be parsed to DateTime
				if (date.contains(""))
				{
					String[] parts=Converter.PATTERN_BACKSLASH.split(date);
					date=parts[0];
				}
				Long dtLong=Long.valueOf(date);
				result=new DateTime(dtLong, DateTimeZone.UTC);
			}
		}
		else if (ClassX.implementsInterface(targetCls, Convertible.class))
		{
			Convertible convertibleTarget;
			try
			{
				convertibleTarget=(Convertible) targetCls.getConstructor().newInstance();
			}
			catch (Exception e)
			{
				throw new ApplicationException(e);
			}
			if ((null==srcClass) || convertibleTarget.canConvertFrom(srcClass))
			{
				convertibleTarget.convertFrom(srcValue);
				result=convertibleTarget;
			}
			else
			{
				throw new ClassCastException(
					String.format(
						"'%s' does not support converting from '%s'.", targetCls.getName(), srcClass.getName()
					)
				);
			}
		}
		else if (ClassX.implementsInterface(srcClass, Convertible.class))
		{
			Convertible convertibleSrc=(Convertible) srcValue;
			if ((null!=srcClass) && convertibleSrc.canConvertTo(srcClass))
			{
				result=convertibleSrc.convertTo(targetCls);
			}
			else
			{
				throw new ClassCastException(
					String.format(
						"'%s' does not support converting to '%s'.", ((null==srcClass) ? "unknown source Class" : srcClass.getName()),
						(targetCls!=null) ? targetCls.getName() : "unknown target class"
					)
				);
			}
		}

		return result;
	}

	public static boolean isPrimitiveType(Class<?> cls)
	{
		return Converter.WRAPPER_TYPES.contains(cls);
	}

// Getters and setters
	private static Collection<Class<?>> getWrapperTypes()
	{
		Collection<Class<?>> hashSet=new HashSet<Class<?>>();
		hashSet.add(String.class);
		hashSet.add(Boolean.class);
		hashSet.add(Character.class);
		hashSet.add(Byte.class);
		hashSet.add(Short.class);
		hashSet.add(Integer.class);
		hashSet.add(Long.class);
		hashSet.add(Float.class);
		hashSet.add(Double.class);
		hashSet.add(Void.class);
		return hashSet;
	}
}
