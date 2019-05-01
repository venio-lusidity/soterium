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

package com.lusidity.system;

import com.lusidity.Environment;
import com.lusidity.cache.ICache;
import com.lusidity.domains.system.assistant.worker.BaseAssistantWorker;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.math.MathX;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.system.SystemProperties;
import com.sun.management.OperatingSystemMXBean;
import org.joda.time.DateTime;

import java.lang.management.ManagementFactory;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class SystemInfo
{
	private static final SystemInfo INSTANCE=new SystemInfo();
	private final Environment environment=Environment.getInstance();
	private OperatingSystemMXBean osMBean=null;

	private SystemInfo()
	{
		super();
		this.osMBean=ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
	}

// Methods
	@SuppressWarnings({
		"CollectionDeclaredAsConcreteClass",
		"TypeMayBeWeakened"
		,
		"UnusedDeclaration",
		"OverlyLongMethod"
	})
	public static JsonData toJsonData()
		throws ApplicationException
	{
		JsonData result=JsonData.createObject();
		try
		{
			/*************************************************************/
		    /*                                                           */
            /*        Do not touch the values of CPU or Memory           */
            /*        I do not want to waste TIME AGAIN trying to        */
            /*        figure out why the UI doesn't work!!!!!!!          */
            /*                                                           */
			/*************************************************************/

			SystemInfo systemInfo=SystemInfo.getInstance();

			JsonData os=JsonData.createObject();
			os.put("time", SystemInfo.create(DateTime.now().toString("dd-MMM-yyy HH:mm"), "System Time"));
			os.put("name", SystemInfo.create(systemInfo.getName(), "OS"));
			os.put("version", SystemInfo.create(systemInfo.getVersion(), "Version"));
			result.put("os", os);

			JsonData app=JsonData.createObject();
			app.put("name", SystemInfo.create(systemInfo.getAppName(), "Application"));
			app.put("version", SystemInfo.create(systemInfo.getAppVersion(), "Application Version"));
			app.put("build", SystemInfo.create(systemInfo.getAppBuild(), "Application Build"));
			result.put("application", app);

			try
			{
				JsonData processor=JsonData.createObject();
				processor.put("architecture", SystemInfo.create(systemInfo.getArchitecture(), "Architecture"));
				processor.put("available", SystemInfo.create(systemInfo.getAvailableProcessors(), "Available"));
				processor.put("max", SystemInfo.create(100, "Max"));
				processor.put("used", SystemInfo.create(systemInfo.getProcessorLoad(), "Used"));
				processor.put("load", SystemInfo.create(systemInfo.getProcessorLoad(), "Load"));
				processor.put("unit", SystemInfo.create("Range", "Unit"));
				result.put("processor", processor);
			}
			catch (Exception ex)
			{
				ReportHandler.getInstance().warning(ex);
			}

			try
			{
				JsonData memory=JsonData.createObject();
				memory.put("free", SystemInfo.create((systemInfo.getFreeMemory()/MathX.BINARY_MEGA), "Free"));
				memory.put("used", SystemInfo.create((systemInfo.getMemoryUsed()/MathX.BINARY_MEGA), "Used"));
				memory.put("max", SystemInfo.create((systemInfo.getMaxMemory()/MathX.BINARY_MEGA), "Max"));
				memory.put("unit", SystemInfo.create("MB", "Unit"));
				memory.put("load", SystemInfo.create(systemInfo.getMemoryLoad(), "Load"));
				result.put("memory", memory);
			}
			catch (Exception ex)
			{
				ReportHandler.getInstance().warning(ex);
			}

			JsonData assistant=JsonData.createObject();

			Map<String, BaseAssistantWorker> workers=Environment.getInstance().getWorkers();

			for (Map.Entry<String, BaseAssistantWorker> entry : workers.entrySet())
			{
				try
				{
					BaseAssistantWorker assistantWorker=entry.getValue();
					assistant.put("worker", assistantWorker.getReport(false));
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().warning(ex);
				}
			}

			result.put("assistant", assistant);

			try
			{
				result.put("cache", Environment.getInstance().getCache().getStats());
				result.put("extended", SystemProperties.toLinkedHashMap(false));
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}

			try
			{
				JsonData stats=JsonData.createObject();

            /* Fix the below currently to intensive to do.
			long nodes = ds.getNodeCount();
			long relationships = ds.getRelationshipCount();
            long labels = ds.getLabelCount();

			JsonData n = JsonData.createObject();
			n.put("label", "Nodes");
			n.put("value", nodes);
			stats.put("nodes", n);

			JsonData r = JsonData.createObject();
			r.put("label", "Relationships");
			r.put("value", relationships);
			stats.put("relationships", r);

            JsonData l = JsonData.createObject();
            l.put("label", "Labels");
            l.put("value", labels);
            stats.put("labels", l);
            */


				int threads=Thread.activeCount();
				JsonData t=JsonData.createObject();
				t.put("label", "Threads");
				t.put("value", threads);
				stats.put("relationships", t);

				result.put("stats", stats);
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
		catch (Exception e)
		{
			throw new ApplicationException(e);
		}

		return result;
	}

	@SuppressWarnings("WeakerAccess")
	public static SystemInfo getInstance()
	{
		return SystemInfo.INSTANCE;
	}

	@SuppressWarnings("WeakerAccess")
	public static JsonData create(Object value, String label)
	{
		JsonData result=JsonData.createObject();
		result.put("value", value);
		result.put("label", label);
		return result;
	}

	/**
	 * The name of the operating system
	 *
	 * @return The name of the operating system
	 */
	@SuppressWarnings("WeakerAccess")
	public String getName()
	{
		return this.osMBean.getName();
	}

	/**
	 * The operating system version.
	 *
	 * @return The operating system version.
	 */
	@SuppressWarnings("WeakerAccess")
	public String getVersion()
	{
		return this.osMBean.getVersion();
	}

	@SuppressWarnings("WeakerAccess")
	public String getAppName()
	{
		return this.environment.getAppProperties().get("application.name");
	}

	@SuppressWarnings("WeakerAccess")
	public String getAppVersion()
	{
		return this.environment.getAppProperties().get("application.version");
	}

	@SuppressWarnings("WeakerAccess")
	public String getAppBuild()
	{
		return this.environment.getAppProperties().get("application.build");
	}

	/**
	 * CPU architecture
	 *
	 * @return AMD or Intel
	 */
	@SuppressWarnings("WeakerAccess")
	public String getArchitecture()
	{
		return this.osMBean.getArch();
	}

	/**
	 * How many processors are available to the JVM.
	 *
	 * @return Available processors that the JVM can use.
	 */
	@SuppressWarnings("WeakerAccess")
	public int getAvailableProcessors()
	{
		return this.osMBean.getAvailableProcessors();
	}

	@SuppressWarnings("WeakerAccess")
	public double getProcessorLoad()
	{
		double nCPU=this.osMBean.getAvailableProcessors();
		double loadAverage=this.osMBean.getSystemLoadAverage();

		double relativeLoad=loadAverage/nCPU;

		return (relativeLoad*100);
	}

	/**
	 * Memory not used.
	 *
	 * @return Memory not used.
	 */
	@SuppressWarnings("WeakerAccess")
	public long getFreeMemory()
	{
		return Runtime.getRuntime().freeMemory();
	}

	/**
	 * Total memory used by the JVM including page filing.
	 *
	 * @return Total memory used by the JVM including page filing.
	 */
	@SuppressWarnings("WeakerAccess")
	public long getMemoryUsed()
	{
		return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
	}

	/**
	 * Max amount of the memory that the JVM can use.
	 *
	 * @return Max amount of the memory that the JVM can use.
	 */
	@SuppressWarnings("WeakerAccess")
	public long getMaxMemory()
	{
		return Runtime.getRuntime().maxMemory();
	}

	/**
	 * The percentage of memory used.
	 *
	 * @return The percentage of memory used.
	 */
	@SuppressWarnings("WeakerAccess")
	public double getMemoryLoad()
	{
		double memoryUsed=this.getMemoryUsed();
		double memoryTotal=this.getMaxMemory();
		return (memoryUsed/memoryTotal)*100;
	}

// Getters and setters
	@SuppressWarnings({
		"MethodMayBeStatic",
		"CollectionDeclaredAsConcreteClass",
		"unused"
	})
	private JsonData getCacheStats()
	{
		Environment environment=Environment.getInstance();
		ICache cacheProvider=environment.getCache();
		return cacheProvider.getStats();
	}
}
