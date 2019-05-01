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

package com.lusidity.workers.importer;

import com.lusidity.Environment;
import com.lusidity.configuration.SoteriumConfiguration;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.domains.data.importer.ImporterHistory;
import com.lusidity.domains.system.assistant.message.FileImportMessage;
import com.lusidity.domains.system.io.FileInfo;
import com.lusidity.domains.system.primitives.UriValue;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.IJsonDataCallBack;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.json.JsonDataLineHandler;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.system.FileX;
import com.lusidity.framework.system.UnzipX;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.time.Stopwatch;
import com.lusidity.framework.xml.LazyXmlNode;
import com.lusidity.framework.xml.XmlFileItem;
import com.lusidity.framework.xml.XmlFileSplitter;
import com.lusidity.office.ExcelX;
import com.lusidity.process.IPreprocessor;
import com.lusidity.reports.ExceptionReport;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.lusidity.domains.data.importer.ImporterHistory.Status.*;

@SuppressWarnings({
	"BooleanMethodNameMustStartWithQuestion",
	"OverlyComplexClass"
})
public abstract class BaseFileImporter extends BaseImporter implements IJsonDataCallBack
{
	private static final int XML_BATCH_FILE_SIZE=50000;
	private static final long XML_MAX_FILE_SIZE_KB=10000L;
	private static ExceptionReport exceptionReport=null;
	protected String fileName="";
	protected File origin=null;
	private FileImportMessage fileImportMessage=null;
	private File unzipped=null;
	private Collection<String> fileNames=new ArrayList<>();
	@SuppressWarnings("CollectionDeclaredAsConcreteClass")
	private LinkedHashMap<String, ImporterHistory> histories=new LinkedHashMap<>();
	private ImporterHistory history=null;
	private boolean recycle=false;

// Constructors
	public BaseFileImporter(FileImportMessage fileImportMessage, int maxThreads, int maxItems)
	{
		super(maxThreads, maxItems);
		this.fileImportMessage=fileImportMessage;
		if (null!=fileImportMessage)
		{
			this.origin=fileImportMessage.getFile();
		}
		BaseFileImporter.exceptionReport=new ExceptionReport();
	}

	public BaseFileImporter(FileImportMessage fileImportMessage, int maxThreads, int maxItems, int maxBatch, int maxWait)
	{
		super(maxThreads, maxItems, maxBatch, maxWait);
		this.fileImportMessage=fileImportMessage;
		this.origin=fileImportMessage.getFile();
		BaseFileImporter.exceptionReport=new ExceptionReport();
	}

// Overrides
	@Override
	public File writeExceptionReport()
	{
		File result=null;
		if ((null!=this.getFileImportMessage()) && (null!=BaseFileImporter.getExceptionReport()) && !BaseFileImporter.getExceptionReport().isEmpty())
		{
			BaseFileImporter.getExceptionReport().prependMessage("Skipped: %d", this.combineProcessStatus().fetchSkipped().getValue().getCount());
			BaseFileImporter.getExceptionReport().prependMessage("Unique Items: %d", BaseFileImporter.getExceptionReport().getMessages().size());
			BaseFileImporter.getExceptionReport().prependMessage("FileInfo Used: %s", this.getFileImportMessage().getFile().getAbsolutePath());
			BaseFileImporter.getExceptionReport().prependMessage("FileInfo Origin: %s", this.getFileImportMessage().fetchOriginalName().getValue());
			BaseFileImporter.getExceptionReport().prependMessage("Importer: %s", this.getClass().getName());

			result=BaseFileImporter.getExceptionReport().write(ExceptionReport.getDefaultDirectory(), this.getClass());
		}
		return result;
	}

	@Override
	public IPreprocessor getPreprocessor()
	{
		return null;
	}

	@Override
	public boolean isRecycle()
	{
		return this.recycle;
	}

	@Override
	public ProcessStatus combineProcessStatus()
	{
		ProcessStatus result=new ProcessStatus();
		if (!this.histories.isEmpty())
		{
			for (Map.Entry<String, ImporterHistory> entry : this.histories.entrySet())
			{
				ImporterHistory history=entry.getValue();
				result.combine(history.fetchProcessStatus().getValue());
			}
			result.combine(this.getProcessStatus());
			result.setMessage(this.getProcessStatus().getMessage());
		}
		return result;
	}

	@Override
	public void close()
		throws IOException
	{
		try
		{
			if ((null!=this.unzipped) && this.unzipped.exists())
			{
				FileX.deleteRecursively(this.unzipped);
			}
		}
		catch (Exception ignored)
		{
		}
		super.close();
	}

	@Override
	public void process()
	{
		this.timer=new Stopwatch();
		this.timer.start();
		ImporterHistory.Status status=success;
		try
		{
			this.start();
			if (this.getMaxThreads()>1)
			{
				this.waitAll(true);
			}

			this.timer.stop();
			if (this.isStopping())
			{
				status=partial;
			}
			this.stopping=false;
		}
		catch (Exception ex)
		{
			status=failed;
			ReportHandler.getInstance().warning(ex);
		}

		this.done();
		File file=this.writeExceptionReport();
		if (!this.isRecycle())
		{
			this.makeHistory(file, status);
			this.report();
		}
	}

	@Override
	public void start()
	{
		this.handle();
		for (Class<? extends DataVertex> cls : this.getMakeAvailable())
		{
			Environment.getInstance().getIndexStore().makeAvailable(cls, true);
		}
	}

	@Override
	public String getStatusText()
	{
		return null;
	}

	@Override
	public boolean jdCallBack(JsonDataLineHandler lineHandler, JsonData item)
	{
		boolean result=false;
		try
		{
			if ((null!=item) && item.isValid())
			{
				if ((lineHandler.getHandlerType()==JsonDataLineHandler.HandlerTypes.count) && (lineHandler.getLinesRead()==1))
				{
					JsonData data=lineHandler.getFirstItem();
					int total=data.getInteger(0, "totalItems");
					if (total>0)
					{
						this.getProcessStatus().fetchTotal().getValue().add(total);
						result=true;
					}
				}
				else if (lineHandler.getHandlerType()==JsonDataLineHandler.HandlerTypes.count)
				{
					this.getProcessStatus().fetchTotal().getValue().increment();
				}
				else if ((lineHandler.getHandlerType()==JsonDataLineHandler.HandlerTypes.process) && (lineHandler.getLinesRead()>1))
				{
					if (!this.isStopping())
					{
						String originFileName=this.getFileName(item);
						ProcessStatus processStatus=this.getProcessStatus(originFileName);
						this.before(item, processStatus);
						this.extract(item, lineHandler.getFirstItem(), processStatus);
						this.after(item, processStatus);
					}
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return result;
	}

	public String getFileName(JsonData data)
	{
		String result=data.getString("originFileName");
		if (StringX.isBlank(result))
		{
			if (StringX.isBlank(this.getFileName()))
			{
				if (null!=this.getOrigin())
				{
					result=this.getOrigin().getName();
				}
				else
				{
					result="";
				}
			}
			data.update("originFileName", result);
		}
		return result;
	}

	public ProcessStatus getProcessStatus(String key)
	{
		ImporterHistory history=this.getHistory(key);
		history.fetchProcessStatus().getValue().setHistory(history);
		return history.fetchProcessStatus().getValue();
	}

	protected abstract void extract(JsonData item, JsonData parentItem, ProcessStatus processStatus)
		throws Exception;

	public String getFileName()
	{
		return this.fileName;
	}

	public File getOrigin()
	{
		return this.origin;
	}

	public ImporterHistory getHistory(String key)
	{
		ImporterHistory result=null;
		String tKey=this.getOriginKey(key);
		if (!StringX.isBlank(tKey))
		{
			String source=this.getSourceName(tKey);
			if (StringX.isBlank(source))
			{
				source=StringX.insertSpaceAtCapitol(this.getClass().getSimpleName());
			}
			String fKey=StringX.removeNonAlphaNumericCharacters(source.toLowerCase());
			result=this.histories.get(fKey);
			if (null==result)
			{
				String name=StringX.toTitle(StringX.insertSpaceAtCapitol(this.getClass().getSimpleName()));
				result=new ImporterHistory();
				result.fetchSource().setValue(source);
				result.fetchImporter().setValue(this.getClass());
				result.fetchImporterName().setValue(name);
				this.histories.put(fKey, result);
			}
			result.fetchOriginalFileNames().add(tKey);
		}
		return result;
	}

	public String getOriginKey(String key)
	{
		String result=key;
		if (StringX.isBlank(result))
		{
			result=StringX.isBlank(this.getFileName()) ? this.getOrigin().getName() : this.getFileName();
		}
		return result;
	}

	public abstract String getSourceName(String key);

// Methods
	public static synchronized ExceptionReport getExceptionReport()
	{
		return BaseFileImporter.exceptionReport;
	}

	public UriValue createSourceUri(String source)
	{
		String n2=StringX.makeKey(source);
		String n1=StringX.makeKey(StringX.insertSpaceAtCapitol(this.getClass().getSimpleName()));
		UriValue result=new UriValue(String.format("lid://%s/%s", n1, n2));
		result.fetchLabel().setValue(source);
		return result;
	}

	public void recycle(boolean recycle)
	{
		this.recycle=true;
		this.stop();
	}

	public abstract JsonData getCommonData(JsonData item);

	public abstract boolean canPreprocess();

	public void handleExcelFile(File working, boolean process)
	{
		try
		{
			ExcelX excelX=new ExcelX();
			excelX.convertExcelToXml(working.getAbsolutePath());
			File parent=working.getParentFile();
			this.handleExcelFileResults(parent, process);
		}
		catch (Exception e)
		{
			Environment.getInstance().getReportHandler().warning(e);
		}
	}

	public void handleExcelFileResults(File working, boolean process)
	{
		try
		{
			File[] allFiles=working.listFiles(FileX.filter(true, "xml"));
			if ((null!=allFiles) && (allFiles.length>0))
			{
				Arrays.sort(allFiles);
				for (File xmlFile : allFiles)
				{
					this.handleFile(xmlFile, process);
				}
			}
		}
		catch (Exception e)
		{
			Environment.getInstance().getReportHandler().warning(e);
		}
	}

	@SuppressWarnings("ConstantConditions")
	public void handle()
	{
		if (!this.isStopping())
		{
			this.getProcessStatus().setMessage("Getting Totals");
			this.handle(false, this.origin);
			this.fileNames=new ArrayList<>();
			this.getProcessStatus().setMessage("Processing");
			this.startTimer();
			this.getTimer().start();

			String name=StringX.toTitle(StringX.insertSpaceAtCapitol(this.getClass().getSimpleName()));
			String source=this.getClass().getName();

			this.history=new ImporterHistory();
			this.history.fetchRoot().setValue(true);
			this.history.fetchImporter().setValue(this.getClass());
			this.history.fetchImporterName().setValue(name);
			this.history.fetchSource().setValue(source);
			this.history.fetchOriginalFileName().setValue(this.origin.getName());
			this.history.fetchStarted().setValue(this.getTimer().getStartedWhen());
			this.histories=new LinkedHashMap<>();

			this.handle(true, this.origin);

			if (this.getMaxThreads()>1)
			{
				this.getProcessStatus().setMessage("Waiting on threaded tasks to complete.");
				this.waitAll(true);
			}
			this.getProcessStatus().setMessage("Processing");
			this.getTimer().stop();

			this.history.fetchStopped().setValue(this.getTimer().getStoppedWhen());
			this.history.fetchProcessStatus().setValue(this.combineProcessStatus());
			this.history.fetchElapsed().setValue(this.getTimer().elapsedToString());
		}
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"UnusedReturnValue"
	})
	public boolean handleDirectory(boolean process, File working)
	{
		boolean result=true;
		if (!this.isStopping())
		{
			File[] files=working.listFiles(FileX.filter(true, "csv", "txt", "xml", "xls", "xlsx", "zip", "json", "jd"));
			if ((null!=files) && (files.length>0))
			{
				Arrays.sort(files);
				for (File current : files)
				{
					if (!this.isStopping())
					{
						boolean isDirectory=current.isDirectory();
						if (isDirectory)
						{
							this.handleDirectory(process, current);
						}
						else if (StringX.endsWithIgnoreCase(current.getName(), "zip"))
						{
							this.handleZip(current, process);
						}
						else if (StringX.endsWithIgnoreCase(current.getName(), "xls")
						         || StringX.endsWithIgnoreCase(current.getName(), "xlsx"))
						{
							this.handleExcelFile(current, process);
						}
						else if (StringX.endsWithIgnoreCase(current.getName(), "txt"))
						{
							this.handleTextFile(current, process);
						}
						else
						{
							result=this.handleFile(current, process);
						}
						if (!result)
						{
							break;
						}
					}
					else
					{
						break;
					}
				}
			}

			String[] directories=working.list();
			if (null!=directories)
			{
				for (String directory : directories)
				{
					File sub=new File(working.getAbsolutePath(), directory);
					if (sub.isDirectory())
					{
						this.handleDirectory(process, sub);
					}
				}
			}
		}
		return result;
	}

	public void handleTextFile(File working, boolean process)
	{
		// override this method in your importer to use.
		// create handler and pass to FileX.readlines()
	}

	public boolean preProcessFiles(File working)
	{
		if (!this.isStopping())
		{
			if (working.isDirectory())
			{
				File[] files=working.listFiles(FileX.filter(true, "xml", "json"));
				if ((null!=files) && (files.length>0))
				{
					Arrays.sort(files);
					for (File current : files)
					{
						if (StringX.endsWithAnyIgnoreCase(current.getName(), ".xml"))
						{
							this.splitFile(current);
						}
					}
				}
				String[] directories=working.list();
				if ((null!=directories) && working.isDirectory())
				{
					for (String directory : directories)
					{
						if (!this.isStopping())
						{
							File sub=new File(working.getAbsolutePath(), directory);
							if (sub.isDirectory())
							{
								this.preProcessFiles(sub);
							}
							else
							{
								if (StringX.endsWithAnyIgnoreCase(sub.getName(), ".xml"))
								{
									this.splitFile(sub);
								}
							}

						}
						else
						{
							break;
						}
					}
				}
			}

		}
		return true;
	}

	/**
	 * The transformation of the inbound data into something meaningful.
	 *
	 * @param process Actually import the file otherwise count how many to process.
	 * @param working The current file being imported.
	 * @param item    the item to step3
	 */
	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyNestedMethod"
	})
	public void transform(boolean process, File working, JsonData item)
	{
		if (null!=item)
		{
			JsonData items=item.getFromPath(this.getObjectsPath());
			if (null==items)
			{
				items=item;
			}
			if (!this.isStopping())
			{
				if (!process)
				{
					this.getTotal(items);
				}
				else if (items.isJSONObject())
				{
					this.transformObject(item, items);
				}
				else if (!items.isEmpty())
				{
					for (Object o : items)
					{
						if (!this.isStopping())
						{
							if (o instanceof JSONObject)
							{
								JsonData sub=JsonData.create(o);
								this.transformObject(item, sub);
							}
						}
						else
						{
							break;
						}
					}
					if (this.getMaxThreads()>1)
					{
						String phase=this.getProcessStatus().getMessage();
						this.getProcessStatus().setMessage("Waiting on open tasks.");
						this.waitAll(true);
						this.getProcessStatus().setMessage(phase);
					}
				}
			}
		}
	}

	/**
	 * If the file is in xml can it be split without disrupting the import.
	 *
	 * @return true or false, default true.
	 */
	public boolean canSplit()
	{
		return true;
	}

	private void makeHistory(File exceptionReport, ImporterHistory.Status status)
	{
		try
		{
			FileInfo fileInfo;
			if (null!=exceptionReport)
			{
				fileInfo=new FileInfo(exceptionReport);
				fileInfo.save();
				if (!this.history.hasId())
				{
					this.history.save();
				}
				this.history.getBlobFiles().add(fileInfo);
			}
			this.history.fetchStatus().setValue(status);
			this.history.save();
			if (!this.histories.isEmpty() && this.history.hasId())
			{
				for (Map.Entry<String, ImporterHistory> entry : this.histories.entrySet())
				{
					try
					{
						ImporterHistory history=entry.getValue();
						if (history.fetchProcessStatus().getValue().fetchProcessed().getValue().getCount()<=0)
						{
							continue;
						}
						history.fetchStarted().setValue(this.history.fetchStarted().getValue());
						history.fetchStopped().setValue(this.history.fetchStopped().getValue());
						history.fetchElapsed().setValue(this.history.fetchElapsed().getValue());
						history.fetchFileSize().setValue(history.fetchProcessStatus().getValue().fetchFileSize().getValue().getCount());
						history.fetchStatus().setValue(status);
						history.save();
						this.history.getHistories().add(history);
					}
					catch (Exception ex)
					{
						Environment.getInstance().getReportHandler().warning(ex);
					}
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
	}

	@SuppressWarnings("OverlyComplexMethod")
	protected JsonData load(File working)
		throws ApplicationException
	{
		JsonData result=null;
		if ((null!=working) && working.isFile())
		{
			this.fileName=working.getName().toLowerCase();

			if (!this.fileNames.contains(this.fileName))
			{
				this.fileNames.add(this.fileName);
				boolean valid=this.isValidFile(this.fileName, this.getClass());
				if (valid)
				{
					ProcessStatus processStatus=this.getProcessStatus(this.fileName);
					if (null!=processStatus)
					{
						processStatus.fetchFileSize().getValue().add(working.length());
					}
				}
				if (valid && StringX.endsWithIgnoreCase(working.getAbsolutePath(), ".json"))
				{
					try
					{
						result=new JsonData(working);
					}
					catch (Exception ex)
					{
						Environment.getInstance().getReportHandler().warning(ex);
					}
				}
				else if (valid && StringX.endsWithAnyIgnoreCase(working.getAbsolutePath(), ".xml", ".nessus"))
				{
					try
					{
						boolean dtdDisabled=SoteriumConfiguration.getInstance().isXmlDtdDisabled();
						LazyXmlNode node=LazyXmlNode.load(working, dtdDisabled);
						if (node!=null)
						{
							result=new JsonData(node);
						}
					}
					catch (Exception ex)
					{
						Environment.getInstance().getReportHandler().warning(ex);
					}
				}
			}
		}

		if ((null!=result) && !result.isValid())
		{
			result=null;
		}
		return result;
	}

	protected abstract boolean isValidFile(String fileName, Class<? extends BaseImporter> cls);

	protected void handle(boolean process, File file)
	{
		if (file.isDirectory())
		{
			this.handleDirectory(process, file);
		}
		else
		{
			if (StringX.endsWithIgnoreCase(file.getAbsolutePath(), ".zip"))
			{
				this.handleZip(file, process);
			}
			else
			{
				if (StringX.endsWithIgnoreCase(file.getName(), "xls")
				    || StringX.endsWithIgnoreCase(file.getName(), "xlsx"))
				{
					this.handleExcelFile(file, process);
				}
				else
				{
					this.handleFile(file, process);
				}
			}
		}
	}

	protected boolean handleFile(File working, boolean process)
	{
		boolean result=true;
		try
		{
			if (!this.isStopping() && working.isFile())
			{
				if (StringX.endsWithIgnoreCase(working.getAbsolutePath(), ".jd"))
				{
					this.transformJd(working, process);
				}
				else if (StringX.endsWithIgnoreCase(working.getName(), "txt"))
				{
					this.handleTextFile(working, process);
				}
				else
				{
					JsonData item=this.load(working);
					boolean loaded=(null!=item) || StringX.endsWithIgnoreCase(working.getAbsolutePath(), ".csv");
					if (loaded)
					{
						this.transform(process, working, item);

						if (this.getMaxThreads()>1)
						{
							String phase=this.getProcessStatus().getMessage();
							this.getProcessStatus().setMessage("Waiting on open tasks.");
							this.waitAll(true);
							this.getProcessStatus().setMessage(phase);
						}
					}
				}
			}
			if ((this.getMaxItems()>0) && (this.combineProcessStatus().fetchProcessed().getValue().getCount()>=this.getMaxItems()))
			{
				result=false;
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		return result;
	}

	protected void handleZip(File working, boolean process)
	{
		if (null!=working)
		{
			try
			{
				String fName=StringX.replace(working.getName(), ".ZIP", ".zip");
				File current=new File(working.getParentFile().getAbsolutePath(), StringX.stripEnd(fName, ".zip"));

				if (!process && current.exists())
				{
					FileX.deleteRecursively(current);
				}

				if (!current.exists())
				{
					boolean made=current.mkdir();
					if (made && (null==this.unzipped))
					{
						this.unzipped=current;
					}
				}

				if (!process && current.exists() && working.exists())
				{
					UnzipX.unzip(working.getAbsolutePath(), current.getAbsolutePath());
					this.preProcessFiles(current);
				}

				this.handleDirectory(process, current);
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
	}

	private boolean splitFile(File working)
	{
		boolean result=false;
		if (this.canSplit())
		{
			String groupNamespace=this.getGroupNameSpace();
			String itemNamespace=this.getItemNameSpace();
			XmlFileItem xmlFileItem=new XmlFileItem(working, groupNamespace, itemNamespace);
			if (xmlFileItem.isValid())
			{
				XmlFileSplitter splitter=new XmlFileSplitter(xmlFileItem, BaseFileImporter.XML_MAX_FILE_SIZE_KB, this.getXmlBatchFileSize());
				result=splitter.split(true);
			}
		}
		return result;
	}

	private void transformJd(File working, boolean process)
	{
		try
		{
			if (!this.isStopping())
			{
				this.fileName=working.getName().toLowerCase();
				if (!this.fileNames.contains(this.fileName))
				{
					this.fileNames.add(this.fileName);
					boolean valid=this.isValidFile(this.fileName, this.getClass());
					if (valid)
					{
						if (!process)
						{
							JsonData.readLines(working, new JsonDataLineHandler(this, JsonDataLineHandler.HandlerTypes.count));
						}
						else
						{
							ProcessStatus processStatus=this.getProcessStatus(this.fileName);
							if (null!=processStatus)
							{
								processStatus.fetchFileSize().getValue().add(working.length());
							}
							JsonData.readLines(working, new JsonDataLineHandler(this, JsonDataLineHandler.HandlerTypes.process));
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
	}

	private synchronized void getTotal(JsonData items)
	{
		Integer count=this.getTotalToProcess(items);
		if (null!=items)
		{
			if ((null==count) && items.isJSONArray())
			{
				count=items.length();
			}
			else if (null==count)
			{
				count=1;
			}
		}
		else
		{
			count=0;
		}
		this.getProcessStatus().fetchTotal().getValue().add(count);
	}

	private void transformObject(JsonData item, JsonData items)
	{
		try
		{
			String originFileName=this.getFileName(items);
			if (null==originFileName)
			{
				originFileName=this.getFileName();
			}
			ProcessStatus processStatus=this.getProcessStatus(originFileName);
			this.before(items, processStatus);
			this.extract(items, item, processStatus);
			this.after(items, processStatus);
		}
		catch (Exception ex)
		{
			this.getProcessStatus().fetchErrors().getValue().increment();
			this.getProcessStatus().fetchProcessed().getValue().increment();
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

	protected abstract Integer getTotalToProcess(JsonData items);

	protected abstract void before(JsonData item, ProcessStatus processStatus);

	protected abstract void after(JsonData item, ProcessStatus processStatus);

// Getters and setters
	public abstract String getObjectsPath();

	protected abstract String getGroupNameSpace();

	protected abstract String getItemNameSpace();

	public int getXmlBatchFileSize()
	{
		return BaseFileImporter.XML_BATCH_FILE_SIZE;
	}

	public Collection<Class<? extends DataVertex>> getMakeAvailable()
	{
		return new ArrayList<>();
	}

	public FileImportMessage getFileImportMessage()
	{
		return this.fileImportMessage;
	}

	public ImporterHistory getHistory()
	{
		return this.history;
	}
}
