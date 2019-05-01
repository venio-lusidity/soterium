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

package com.lusidity.framework.system.logging;

import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.system.FileX;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.joda.time.DateTime;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.*;

@SuppressWarnings("ThrowCaughtLocally")
public
class LoggerX
	extends Logger
{
	private static final int MAX_FILE_SIZE = (1000000*10);
	private static final int MAX_NUM_FILES = 500;
// ------------------------------ FIELDS ------------------------------

	private String filename=null;
	private File directory=null;

// -------------------------- STATIC METHODS --------------------------

	public static
	void logStackTrace(Logger logger, Level level, Exception e)
	{
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		String str=baos.toString();
		logger.log(level, str);
	}

	public static
	LoggerX open(String logName, File directory, Level level, boolean outputToConsole)
		throws ApplicationException
	{
		LoggerX.backup(directory);
		LoggerX logger=new LoggerX(logName, null);
		logger.setFilename(logName);
		logger.setDirectory(directory);

		//  Set logging level on logger itself
		logger.setLevel(level);

		logger.create(outputToConsole, level);

		return logger;
	}

	public void backup(){
		LoggerX.backup(this.directory);
	}

	private static void backup(File directory)
	{
		try
		{
			File backup=new File(directory, "backup");
			if (!backup.exists())
			{
				//noinspection ResultOfMethodCallIgnored
				backup.mkdir();
			}
			long total = 0;
			File[] files=directory.listFiles();
			if (null!=files)
			{
				Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
				for (File file : files)
				{
					if(file.isFile()){
						total+=file.length();
					}
					//noinspection ResultOfMethodCallIgnored
					file.renameTo(new File(backup, String.format("log_%d_%s", DateTime.now().getMillis(), file.getName())));
				}

				long max = ((long)LoggerX.MAX_FILE_SIZE*LoggerX.MAX_NUM_FILES)*2;
				if(total>max){
					for (File file : files)
					{
						try{
							Long len = file.length();
							boolean deleted = file.delete();
							if(deleted)
							{
								total-=len;
								if (total<max)
								{
									break;
								}
							}
						}
						catch (Exception ignored){}
					}
				}
			}
		}
		catch (Exception ignore)
		{
		}
	}

	private
	void setFilename(String filename)
	{
		this.filename=filename;
	}

	private
	void setDirectory(File directory)
	{
		this.directory=directory;
	}

	private
	void create(boolean outputToConsole, Level level)
		throws ApplicationException
	{
		try
		{

			if (!this.directory.exists())
			{
				this.directory.mkdir();
			}

			FileHandler fileHandler=new FileHandler(
				LoggerX.getOrCreatePath(this.directory, this.filename, level), LoggerX.MAX_FILE_SIZE, LoggerX.MAX_NUM_FILES
			);
			this.addHandler(fileHandler);

			// Create JSON Formatter
			Formatter formatterJSON = new JSONLogFormatter();
			fileHandler.setFormatter(formatterJSON);

			if (outputToConsole)
			{
				//  Create console handler
				ConsoleHandler consoleHandler=new ConsoleHandler();
				consoleHandler.setLevel(level);
				this.addHandler(consoleHandler);
			}

		}
		catch (Exception e)
		{
			throw new ApplicationException(e);
		}
	}

	private static
	String getOrCreatePath(File directory, String filename, Level level)
		throws ApplicationException
	{
		if (!directory.exists())
		{
			boolean status=directory.mkdir();
			if (!status)
			{
				throw new ApplicationException("Could not create directory '%s'.", directory.getAbsolutePath());
			}
		}
		else
		{
			if (!directory.isDirectory())
			{
				throw new ApplicationException(String.format("'%s' is not a directory.",
					directory.getAbsolutePath()
				));
			}
		}

		return directory.getAbsolutePath()+'/'+LoggerX.getFileName(filename, level);
	}

	private static
	String getFileName(String filename, Level level)
	{
		return LoggerX.getLoggerName(filename, level);
	}

	private static
	String getLoggerName(String filename, Level level)
	{
		return filename+'.'+level.toString().toLowerCase();
	}

// --------------------------- CONSTRUCTORS ---------------------------

	protected
	LoggerX(String name, String resourceBundleName)
	{
		super(name, resourceBundleName);
	}

// -------------------------- OTHER METHODS --------------------------

	@SuppressWarnings("NestedAssignment")
	public
	JsonData getLog(int start, int limit)
		throws ApplicationException
	{
		JsonData results=JsonData.createArray();

		File file = FileX.getNewestFile(this.directory.getAbsolutePath());

		limit=(limit<=0) ? 50 : limit;
		if (file.exists())
		{
			int total=0;
			try (InputStream inputStream=new FileInputStream(file))
			{
				try (BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))))
				{
					@SuppressWarnings("unused")
					String line;
					while ((line=reader.readLine())!=null)
					{
						total++;
					}
				}
				catch (Exception e)
				{
					throw new ApplicationException(e);
				}
			}
			catch (Exception e)
			{
				throw new ApplicationException(e);
			}

			int begin=(total-(start+limit))+1;
			int end=begin+limit;
			begin=(begin<0) ? 0 : begin;

			if (end>0)
			{
				try (InputStream inputStream=new FileInputStream(file))
				{
					try (BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))))
					{
						int on=0;
						String items="";
						String line;
						while ((line=reader.readLine())!=null)
						{
							if ((on>=begin) && (on<end))
							{
								if (!items.isEmpty())
								{
									items=String.format(",%s", items);
								}
								items=String.format("%s%s", line, items);
							}
							on++;
						}
						items=String.format("[%s]", items);
						results=new JsonData(items);
					}
					catch (Exception e)
					{
						throw new ApplicationException(e);
					}

				}
				catch (Exception e)
				{
					throw new ApplicationException(e);
				}
			}
		}

		return results;
	}

	public
	void close()
	{
		this.filename=null;
		this.directory=null;
	}

	public
	File getFile()
		throws ApplicationException
	{
		return FileX.getFile(LoggerX.getOrCreatePath(this.directory, this.filename, this.getLevel()));
	}
}
