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

package com.lusidity.framework.reports;

import com.lusidity.framework.diagnostics.DiagnosticsX;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.system.logging.LoggerX;
import com.lusidity.framework.text.StringX;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

public
class ReportHandler implements Closeable
{
	public static final int MAX_EXCEPTIONS=20;
	private static ReportHandler instance = null;
	private final ReportHandlerCallback reportHandlerCallback;
	private boolean debug = false;
	private boolean logTime=false;
	private boolean outputToConsole=false;
	private Level loggingLevel=Level.FINE;
	private LoggerX logger=null;
	private boolean opened=false;
	private boolean enabled = true;
	private ReportHandlerOccurrences occurrences = new ReportHandlerOccurrences();

	public
	ReportHandler(Level level, boolean logTime, boolean outputToConsole, boolean debug, ReportHandlerCallback reportHandlerCallback)
	{
		super();
		this.loggingLevel=level;
		this.outputToConsole=outputToConsole;
		this.logTime=logTime;
		this.debug = debug;
		this.reportHandlerCallback = reportHandlerCallback;
		this.open();
		ReportHandler.instance = this;
	}

	public static ReportHandler getInstance()
	{
		return ReportHandler.instance;
	}

	public
	void debug(String format, String... parameters)
	{
		if(this.debug){
			this.fine(format, (Object[]) parameters);
		}
	}

	@Override
	public
	void close()
		throws IOException
	{
		ReportHandler.instance = null;
		this.opened = false;
		this.logger.close();
	}

	@SuppressWarnings("unused")
	public
	void progress(String format, Object... parameters)
	{
		if (Objects.equals(this.loggingLevel, Level.FINE))
		{
			System.out.print(String.format("\r%s", String.format(format, parameters)));
		}
	}

	private
	void open()
	{
		try
		{
			if (null!=this.logger)
			{
				this.logger.close();
				this.logger=null;
			}
			this.logger=LoggerX.open(this.getClass().getPackage().getName(), new File("logs"), this.getLoggingLevel(),
				this.outputToConsole
			);
			this.opened=true;
		}
		catch (ApplicationException ignore)
		{
			System.err.println("The logger failed to initialize.");
			//noinspection CallToSystemExit
			System.exit(-1);
		}
	}

	public
	Level getLoggingLevel()
	{
		return this.loggingLevel;
	}

	public
	LoggerX getLogger()
	{
		return this.logger;
	}

	public
	boolean isOpened()
	{
		return this.opened;
	}

	public
	void timed(String format, Object... params)
	{
		if (this.logTime)
		{
			this.fine(format, params);
		}
	}

	public
	void fine(String format, Object... parameters)
	{
		this.log(Level.FINE, format, parameters);
	}

	public
	void finer(String format, Object... parameters)
	{
		this.log(Level.FINER, format, parameters);
	}

	@SuppressWarnings("UnusedDeclaration")
	public
	void finest(String format, Object... parameters)
	{
		this.log(Level.FINEST, format, parameters);
	}

	public
	void info(String format, Object... parameters)
	{
		this.log(Level.INFO, format, parameters);
	}

	public
	void info(Exception e)
	{
		this.log(Level.INFO, e);
	}

	public
	void warning(Exception e)
	{
		this.log(Level.WARNING, e);
	}

	public
	void warning(String format, Object... parameters)
	{
		this.log(Level.WARNING, format, parameters);
	}

	public
	void severe(Exception e)
	{
		this.log(Level.SEVERE, e);
	}

	public
	void severe(String format, Object... parameters)
	{
		this.log(Level.SEVERE, format, parameters);
	}

	public
	void critical(Exception e)
	{
		this.logReportable(Level.SEVERE, e);
	}

	public
	void critical(String format, Object... parameters)
	{
		this.logReportable(Level.SEVERE, format, parameters);
	}
	public
	void notImplemented()
	{
		String location=DiagnosticsX.formatStack(3);
		this.say("%s: %s", location, "Not implemented.");
	}

	@SuppressWarnings("unused")
	public
	void notVerified()
	{
		String location=DiagnosticsX.formatStack(3);
		this.say("%s: %s", location, "Not verified.");
	}

	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}

	private
	void log(Level level, Exception e)
	{
		if(this.enabled)
		{
			StringBuilder message=new StringBuilder();
			int on=0;
			for (StackTraceElement stackTraceElement : e.getStackTrace())
			{
				String msg=e.toString();
				String st=DiagnosticsX.format(stackTraceElement);
				if (!StringX.isBlank(msg) || !StringX.isBlank(st))
				{
					if (on==0)
					{
						message.append(String.format("%s at %s", msg, st));
					}
					else
					{
						message.append(String.format("%s", st));
					}
					on++;
					if (on==ReportHandler.MAX_EXCEPTIONS)
					{
						break;
					}
				}
			}
			if ((message.length()>0) && this.occurrences.log(this.getLogger(), level, message.toString()))
			{
				this.getLogger().log(level, message.toString());
				if (null!=this.reportHandlerCallback)
				{
					this.reportHandlerCallback.processed(level, message.toString());
				}
			}
		}
	}
	private
	void logReportable(Level level, Exception e)
	{
		if(this.enabled && Objects.equals(level, Level.SEVERE))
		{
			StringBuilder message=new StringBuilder();
			int on=0;
			for (StackTraceElement stackTraceElement : e.getStackTrace())
			{
				String msg=e.toString();
				String st=DiagnosticsX.format(stackTraceElement);
				if (!StringX.isBlank(msg) || !StringX.isBlank(st))
				{
					if (on==0)
					{
						message.append(String.format("critical: %s at %s", msg, st));
					}
					else
					{
						message.append(String.format("critical: %s", st));
					}
					on++;
					if (on==ReportHandler.MAX_EXCEPTIONS)
					{
						break;
					}
				}
			}
			if ((message.length()>0) && this.occurrences.log(this.getLogger(), level, message.toString()))
			{
				this.getLogger().log(level, message.toString());
				if (null!=this.reportHandlerCallback)
				{
					this.reportHandlerCallback.processed(level, message.toString());
				}
			}
		}
	}

	@SuppressWarnings("OverlyComplexMethod")
	private
	void logReportable(Level level, String format, Object... parameters)
	{
		if(this.enabled)
		{
			String message="";
			if (Objects.equals(level, Level.SEVERE))
			{
				int len=DiagnosticsX.getStackSize()-1;
				for (int i=len; i>ReportHandler.MAX_EXCEPTIONS; i--)
				{
					String txt=DiagnosticsX.formatStack(i);
					if (!StringX.isBlank(txt) && !StringX.containsIgnoreCase(txt, "DiagnosticsX") &&
					    !StringX.containsIgnoreCase(txt, "ReportHandler") && !StringX.containsIgnoreCase(message, txt))
					{
						message=String.format("%s%s%s", txt, System.lineSeparator(), message);
					}
				}
				String value=String.format(format, parameters);
				message=String.format("critical: %s%s%s", value, StringX.isBlank(value) ? "" : value, message);
				if (!StringX.isBlank(message) && this.occurrences.log(this.getLogger(), level, message))
				{
					this.getLogger().log(level, message);
				}
			}
			if (null!=this.reportHandlerCallback)
			{
				this.reportHandlerCallback.processed(level, message);
			}
		}
	}
	@SuppressWarnings("OverlyComplexMethod")
	private
	void log(Level level, String format, Object... parameters)
	{
		if(this.enabled)
		{
			String message="";
			if ((Objects.equals(level, Level.WARNING)) || (Objects.equals(level, Level.SEVERE)))
			{
				int len=DiagnosticsX.getStackSize()-1;
				for (int i=len; i>ReportHandler.MAX_EXCEPTIONS; i--)
				{
					String txt=DiagnosticsX.formatStack(i);
					if (!StringX.isBlank(txt) && !StringX.containsIgnoreCase(txt, "DiagnosticsX") &&
					    !StringX.containsIgnoreCase(txt, "ReportHandler") && !StringX.containsIgnoreCase(message, txt))
					{
						message=String.format("%s%s%s", txt, System.lineSeparator(), message);
					}
				}
				String value=String.format(format, parameters);
				message=String.format("%s%s%s", value, StringX.isBlank(value) ? "" : value, message);
				if (!StringX.isBlank(message))
				{
					this.getLogger().log(level, message);
				}
			}
			else
			{
				String location=DiagnosticsX.formatStack(5);
				message=String.format("%s: %s", location, String.format(format, parameters));
				if (!StringX.isBlank(message) && this.occurrences.log(this.getLogger(), level, message))
				{
					this.getLogger().log(level, message);
				}
			}
			if (null!=this.reportHandlerCallback)
			{
				this.reportHandlerCallback.processed(level, message);
			}
		}
	}

	public
	void say(String format, Object... parameters)
	{
		if (Objects.equals(this.loggingLevel, Level.FINE))
		{
			System.out.println(String.format("%s", String.format(format, parameters)));
		}
	}

	/**
	 * Report a stupid exception. AutoClosable.close() throwing an exception is a good example of a stupid exception.
	 *
	 * @param e Exception.
	 */

	//  This is my favorite method in the entire code base -- jjszucs, 27 April 2016
	@SuppressWarnings("unused")
	public
	void stupid(Exception e)
	{
		this.severe("STUPID: "+e.toString());
	}
}
