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

package com.lusidity.reports;

import com.lusidity.Environment;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.system.FileX;
import com.lusidity.framework.text.StringX;
import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class ExceptionReport
{
	private ExceptionReport.ExtensionTypes extensionType = ExceptionReport.ExtensionTypes.txt;

	public enum ExtensionTypes{
		txt,
		csv
	}

// Fields
	public static final String LINE_SEP="\r\n";
	private Collection<String> messages=new ArrayList<>();

// Constructors
	public ExceptionReport()
	{
		super();
	}

// Methods
	public static File getDefaultDirectory()
	{
		return new File(Environment.getInstance().getConfig().getExceptionReportPath());
	}

	public synchronized boolean contains(String format, Object... values)
	{
		return this.contains(String.format(format, values));
	}

	public synchronized boolean contains(String msg)
	{
		return this.messages.contains(msg);
	}

	public synchronized boolean addMessage(String... values)
	{
		StringBuffer sb = new StringBuffer();
		for(String value: values){
			if(sb.length()>0){
				sb.append("\t");
			}
			sb.append(value);
		}
		return this.addMessage(StringX.removeLast(sb.toString(), "\t"), false);
	}

	public synchronized boolean addMessage(String msg, boolean duplicates)
	{
		boolean result = this.messages.contains(msg);
		if(!result || duplicates){
			result = this.messages.add(msg);
		}
		return result;
	}

	public boolean prependMessage(String format, Object... values)
	{
		return this.prependMessage(String.format(format, values));
	}

	public synchronized boolean prependMessage(String msg)
	{
		Set<String> temp=new LinkedHashSet<>();
		boolean result=temp.add(msg);
		for (String str : this.messages)
		{
			temp.add(str);
		}
		this.messages=temp;
		return result;
	}

	public File write(File directory, Class cls)
	{
		File result = null;
		try
		{
			StringBuilder builder=new StringBuilder();
			for (String msg : this.getMessages())
			{
				builder.append(msg).append(ExceptionReport.LINE_SEP);
			}
			String path=String.format("%s/exception_report_%s_%s.%s",
				directory.getAbsolutePath(),
				cls.getSimpleName(),
				DateTime.now().toString("yyyy_MM_dd_HH_mm_ss"),
				this.getExtensionType().toString()
			);
			result=new File(path);
			FileX.write(result, builder.toString());
			ReportHandler.getInstance().info("%s created exception report file at %s.", this.getClass().getSimpleName(), path);
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return result;
	}

	public synchronized Collection<String> getMessages()
	{
		return this.messages;
	}

// Getters and setters
	public boolean isEmpty()
	{
		return this.getMessages().isEmpty();
	}

	public ExceptionReport.ExtensionTypes getExtensionType()
	{
		return this.extensionType;
	}

	public void setExtensionType(ExceptionReport.ExtensionTypes extensionType){
		this.extensionType = extensionType;
	}
}
