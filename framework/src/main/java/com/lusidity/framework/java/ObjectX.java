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

package com.lusidity.framework.java;

import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.time.DateTimeX;
import org.joda.time.DateTime;

import java.io.*;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utility/helper methods for Java Object class.
 *
 * @author jjszucs
 */
public class ObjectX
{
    private ObjectX()
    {
	    super();
    }
/*************************************************************************************
 * Static Methods
 *************************************************************************************/
    /**
     * Convert an object to its string representation or return null if the specified object is null.
     *
     * @param object
     *     Object.
     * @return String representation or null.
     */
    public static String toStringOrNull(Object object)
    {
        return (object != null) ? (object.toString()) : null;
    }

    public static boolean isDifferent(Object actual, Object expected) {
        boolean result = false;

        if ((null==actual) && (null!=expected)) {
            result = true;
        }
        else if((actual instanceof DateTime) || (expected instanceof DateTime)){
	        DateTime a = (null==actual) ? null : (DateTime)actual;
	        DateTime b = (null==expected) ? null : (DateTime)expected;
	        if((null!=a) && (null!=b)){
		        result = !DateTimeX.equals(a,b);
	        }
        }
        else if ((null!=actual) && (null!=expected) && !Objects.equals(actual, expected)) {
            result = true;
        }
        return result;
    }

	public static
	void writeObject(String fileName, Object value)
	{
		try(FileOutputStream fout = new FileOutputStream(fileName)){
			try(GZIPOutputStream gzip = new GZIPOutputStream(fout))
			{
				try(ObjectOutputStream objectOut=new ObjectOutputStream(gzip))
				{
					objectOut.writeObject(value);
				}
				catch (Exception ex){
					ReportHandler.getInstance().severe(ex);
				}
			}
			catch(Exception ex){
				ReportHandler.getInstance().severe(ex);
			}
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().severe(ex);
		}
	}

	public static Object readObject(String fileName){
		Object result = null;
		try(FileInputStream fin = new FileInputStream(fileName)){
			try(GZIPInputStream gzip = new GZIPInputStream(fin))
			{
				try(ObjectInputStream input = new ObjectInputStream(gzip))
				{
					result = input.readObject();
				}
				catch (Exception ex){
					ReportHandler.getInstance().severe(ex);
				}
			}
			catch(Exception ex){
				ReportHandler.getInstance().severe(ex);
			}
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().severe(ex);
		}
		return result;
	}

	public static byte[] toByteArray(Object value){
		byte[] result = null;

		try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
			try(GZIPOutputStream gzip = new GZIPOutputStream(baos))
			{
				try(ObjectOutputStream output=new ObjectOutputStream(gzip))
				{
					output.writeObject(value);
					result = baos.toByteArray();
				}
				catch (Exception ex){
					ReportHandler.getInstance().severe(ex);
				}
			}
			catch(Exception ex){
				ReportHandler.getInstance().severe(ex);
			}
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().severe(ex);
		}

		return result;
	}

	public static Object fromByteArray(byte[] bytes){
		Object result = null;

		try(ByteArrayInputStream baos = new ByteArrayInputStream(bytes)){
			try(GZIPInputStream gzip = new GZIPInputStream(baos))
			{
				try(ObjectInputStream input = new ObjectInputStream(gzip))
				{
					result = input.readObject();
				}
				catch (Exception ex){
					ReportHandler.getInstance().severe(ex);
				}
			}
			catch(Exception ex){
				ReportHandler.getInstance().severe(ex);
			}
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().severe(ex);
		}

		return result;
	}

	@SuppressWarnings("OverlyStrongTypeCast")
	public static boolean isDifferentAndNotEmpty(Object newValue, Object oldValue)
	{
		boolean result = false;
		if((null!=newValue) && !StringX.isBlank(newValue.toString()))
		{
			if ((newValue instanceof String) || (oldValue instanceof String))
			{
				String n=String.valueOf(newValue);
				String o=(null!=oldValue) ? String.valueOf(oldValue) : "";
				result=!StringX.isBlank(n) && !StringX.equalsIgnoreCase(n, o);
			}
			else if ((newValue instanceof DateTime) && (oldValue instanceof DateTime))
			{
				result=!((DateTime) oldValue).isEqual((DateTime) newValue);
			}
			else
			{
				result=(!Objects.equals(newValue, oldValue));
			}
		}
		return result;
	}

	public static boolean equalsOneOf(Object value, Object... matches){
		boolean result = false;
		if((null!=value) && (null!=matches)){
			for(Object match: matches){
				result = Objects.equals(value, match);
				if(result){
					break;
				}
			}
		}
		return result;
	}

	public static boolean isNull(Object obj)
	{
		boolean result = (null==obj);
		if(!result && (obj instanceof String)){
			result = StringX.isBlank(obj.toString());
		}
		return result;
	}
}
