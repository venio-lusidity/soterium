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

package com.lusidity;

import com.lusidity.cache.DummyCache;
import com.lusidity.cache.ICache;
import com.lusidity.components.Module;
import com.lusidity.configuration.BaseSoteriumConfiguration;
import com.lusidity.configuration.IEnvironmentConfiguration;
import com.lusidity.console.Command;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.data.interfaces.data.query.IQueryFactory;
import com.lusidity.domains.system.assistant.worker.BaseAssistantWorker;
import com.lusidity.framework.annotations.AtClassExclude;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.internet.http.ClientConfiguration;
import com.lusidity.framework.internet.http.KeyStoreManager;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.text.StringX;
import com.lusidity.index.interfaces.IIndexStore;
import com.lusidity.jobs.IJob;
import com.lusidity.jobs.JobsEngine;
import com.lusidity.license.LicenseManager;
import com.lusidity.security.authorization.IAuthorizedUpdateHandler;
import com.lusidity.security.data.filters.PrincipalFilterCache;
import com.lusidity.server.IServer;
import com.lusidity.workers.workflow.WorkflowEngine;
import org.reflections.Reflections;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.*;

@SuppressWarnings({
	"RedundantFieldInitialization",
	"ClassWithTooManyFields",
	"OverlyComplexClass",
	"OverlyCoupledClass"
})
public class Environment implements Closeable
{
	/**
	 * Run level (like in UNIX-like operating systems).
	 */
	public enum RunLevel
	{
		Stop,
		Console,
		Server
	}
// Fields
	public static final String DEFAULT_HOST="https://services.lusidity.com";
	public static final int COLLECTIONS_DEFAULT_LIMIT=1000;
	private static final URI SOURCE_URI=URI.create(Environment.DEFAULT_HOST);
	public static final int DEFAULT_MAX_ITERATOR_SIZE=100;
	private static Environment INSTANCE=null;
	private final Map<String, Command> commands=new HashMap<>();
	private final Map<String, Class<? extends ApolloVertex>> apolloVertexTypes=new HashMap<>();
	private final Map<String, BaseAssistantWorker> workers=new HashMap<>();
	private boolean opened=false;
	private IDataStore dataStore=null;
	private IIndexStore indexStore=null;
	private IDataStore reportStore = null;
	private IServer webServer=null;
	private IQueryFactory queryEngine=null;
	private Environment.RunLevel runLevel=Environment.RunLevel.Console;
	private String name=null;
	private Reflections reflections=null;
	private ICache cache=null;
	private BaseSoteriumConfiguration config=null;
	private Collection<Closeable> closeables=new ArrayList<>();
	private ReportHandler reportHandler=null;
	private Map<String, Class<? extends DataVertex>> reservedNames=new HashMap<>();
	private Collection<Class<? extends IJob>> auditPolicies=new ArrayList<>();
	private KeyStoreManager keyStoreManagerApollo= null;
	private List<IAuthorizedUpdateHandler> authorizedUpdateHandlers = new ArrayList<>();
	private volatile PrincipalFilterCache principalFilterCache = null;
	private KeyStoreManager keyStoreManagerServices = null;

	// Constructors
	public Environment()
	{
		super();
		Environment.INSTANCE=this;
	}

// Methods
	public static URI getSourceUri()
	{
		return Environment.SOURCE_URI;
	}

	public static Environment open(String name, IEnvironmentConfiguration config)
		throws Exception
	{
		Environment environment=new Environment();
		environment.start(name, config);
		return environment;
	}

	@SuppressWarnings("unused")
	public static void removeSetting(String key)
	{
		Environment.getInstance().getConfig().removeSetting(key);
	}

	public static File getSchemaFor(Class<? extends DataVertex> cls)
	{
		return new File(Environment.getInstance().getConfig().getResourcePath(), String.format("/schema/excel/%s.json", cls.getName()));
	}

	private static Reflections reflect(String... namespaces)
	{
		Reflections result=null;

		for (String namespace : namespaces)
		{
			Reflections reflector=new Reflections(namespace);
			if (result==null)
			{
				result=reflector;
			}
			else
			{
				result.merge(reflector);
			}
		}

		return result;
	}

	@SuppressWarnings("unused")
	private static boolean canRegister(Class<? extends Module> moduleClass)
	{
		AtClassExclude classExclude=moduleClass.getAnnotation(AtClassExclude.class);
		return (null==classExclude) && ClassX.canInstantiate(moduleClass);
	}

	public String getSetting(String key)
	{
		return this.getConfig().getSetting(key);
	}

	public IEnvironmentConfiguration getConfig()
	{
		return this.config;
	}

	@SuppressWarnings("unused")
	public boolean hasRun(Class<? extends DataVertex> cls)
	{
		File file=new File(Environment.getInstance().getConfig().getResourcePath(), String.format("resource/init_%s.json", cls.getName()));
		return file.exists();
	}

	public static Environment getInstance()
	{
		return Environment.INSTANCE;
	}

	@SuppressWarnings("CallToPrintStackTrace")
	public void close(int exitStatus)
	{
		try
		{
			this.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.exit(exitStatus);
	}

	@SuppressWarnings("OverlyComplexMethod")
	@Override
	public void close()
		throws IOException
	{
		ReportHandler handler = (null==Environment.getInstance()) ? null : Environment.getInstance().getReportHandler();
		if(null!=handler){
			handler.severe("The Environment is being shut down.");
		}
		if ((null!=this.getDataStore()) && this.getDataStore().isOpened())
		{
			if(null!=handler){
				handler.severe("The data store is being shut down.");
			}
			this.getDataStore().close();
		}

		if ((null!=this.getIndexStore()) && this.getIndexStore().isOpened())
		{
			if(null!=handler){
				handler.severe("The index store is being shut down.");
			}
			this.getIndexStore().close();
		}

		if ((null!=this.getReportStore()) && this.getReportStore().isOpened())
		{
			if(null!=handler){
				handler.severe("The report store is being shut down.");
			}
			this.getReportStore().close();
		}

		for (Closeable closeable : this.getCloseables())
		{
			closeable.close();
		}
		this.opened = false;
		Environment.getInstance().getReportHandler().say("\n\nThe Environment has been shutdown.\n\n");
	}

	/**
	 * Get the data store.
	 *
	 * @return A server.
	 */
	public IDataStore getDataStore()
	{
		return this.dataStore;
	}

	/**
	 * Get the report data store.
	 *
	 * @return A server.
	 */
	public IDataStore getReportStore()
	{
		return this.reportStore;
	}

	/**
	 * Get the external index store.
	 *
	 * @return An IndexStore.
	 */
	public IIndexStore getIndexStore()
	{
		return this.indexStore;
	}

	@SuppressWarnings("unused")
	public ReportHandler getReportHandler()
	{
		this.reportHandler.setEnabled(Environment.getInstance().getConfig().isLogEnabled());
		return this.reportHandler;
	}

	@SuppressWarnings("unused")
	public String getAppProperty(String key)
	{
		return this.getAppProperties().get(key);
	}

	/**
	 * Get the applications properties.
	 *
	 * @return The applications properties.
	 */
	public Map<String, String> getAppProperties()
	{
		Map<String, String> properties=new HashMap<>();
		try
		{
			Properties prop=new Properties();
			prop.load(ClassLoader.getSystemResourceAsStream("application.properties"));

			if (null!=prop.propertyNames())
			{
				for (String property : prop.stringPropertyNames())
				{
					properties.put(property, prop.getProperty(property));
				}
			}
		}
		catch (Exception ignored){}
		return properties;
	}

	public synchronized void registerCloseable(Closeable closeable)
	{
		this.closeables.add(closeable);
	}

	@SuppressWarnings("unused")
	public synchronized void closeAndRemove(Closeable closeable)
	{
		try
		{
			closeable.close();
		}
		catch (IOException ignored)
		{
		}
		this.getCloseables().remove(closeable);
	}

	public Class<? extends DataVertex> getApolloVertexType(String id)
	{
		return this.apolloVertexTypes.get(id);
	}

	public Command getWebCommand(String cmdName)
	{
		Command result=null;
		if (this.commands.containsKey(cmdName))
		{
			result=this.commands.get(cmdName);
		}
		return result;
	}

	@SuppressWarnings("UnusedDeclaration")
	public <T extends BaseAssistantWorker> T getWorker(Class<? extends BaseAssistantWorker> clsWorker)
	{
		return this.getWorker(clsWorker.getSimpleName());
	}

	public <T extends BaseAssistantWorker> T getWorker(String className)
	{
		//noinspection unchecked
		return (!StringX.isBlank(className) && this.workers.containsKey(className))
			? (T) this.workers.get(className) : null;
	}

	@SuppressWarnings("unused")
	public Class getClassById(String vertexType)
	{
		Class result=null;
		Collection<Class<?>> classes=this.reflections.getTypesAnnotatedWith(AtSchemaClass.class);
		for (Class cls : classes)
		{
			@SuppressWarnings("unchecked")
			String classType=ClassHelper.getClassKey(cls);
			if (StringX.equals(classType, vertexType))
			{
				result=cls;
				break;
			}
		}
		return result;
	}

	public boolean isReserved(String reservedName, Class<? extends DataVertex> cls)
	{
		boolean result=false;
		if (!StringX.isBlank(reservedName) && (null!=cls))
		{
			String fName=reservedName.toLowerCase();
			if (this.reservedNames.containsKey(fName))
			{
				result=(Objects.equals(cls, this.reservedNames.get(fName)));
			}
		}
		return result;
	}

	@SuppressWarnings("unused")
	protected void setSetting(String key, Object value)
	{
		this.getConfig().setSetting(key, value);
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyLongMethod"
	})
	private void start(String applicationName, IEnvironmentConfiguration environmentConfiguration)
		throws Exception
	{
		if (environmentConfiguration instanceof BaseSoteriumConfiguration)
		{
			this.name=applicationName;
			this.config=(BaseSoteriumConfiguration) environmentConfiguration;
			LogHandlerCallback logHandlerCallback=new LogHandlerCallback();
			this.reportHandler=new ReportHandler(this.config.getLogLevel(), this.config.isTimeLogged(), this.config.isOutputToConsole(), true, logHandlerCallback);
			Environment.getInstance().getReportHandler().say("ReportHandler created.");

			// We are going open source.
			//this.validateLicense();

			this.principalFilterCache = new PrincipalFilterCache();

			Environment.getInstance().getReportHandler().say("Reflecting com.lusidity.");
			//  Open Reflections
			this.reflections=Environment.reflect(
				"com.lusidity"
			);

			Environment.getInstance().getReportHandler().say("Getting authorization handlers.");
			this.loadAuthorizedUpdateHandlers();

			Environment.getInstance().getReportHandler().say("Registering Apollo Vertices");
			this.registerApolloVertices();

			Environment.getInstance().getReportHandler().say("Registering Commands");
			this.registerCommands();

			Environment.getInstance().getReportHandler().say("Attaching shutdown hook.");
			this.attachShutDownHook();

			//  Open cache
			Environment.getInstance().getReportHandler().say("Opening cache connection.");
			this.cache=this.config.open(IEnvironmentConfiguration.ServerRoles.cache);
			if ((null==this.cache) || !this.cache.isOpened())
			{
				this.cache=new DummyCache();
				Environment.getInstance().getReportHandler().say("Cache is not being used.");
			}

			if(!StringX.isBlank(this.config.getKeyStoreConfigPathApollo()))
			{
				this.trustStore();
			}

			Environment.getInstance().getReportHandler().say("Opening data store");
			this.dataStore=this.config.open(IEnvironmentConfiguration.ServerRoles.data);

			Environment.getInstance().getReportHandler().say("Opening report store");
			this.reportStore=this.config.open(IEnvironmentConfiguration.ServerRoles.report);

			Environment.getInstance().getReportHandler().say("Opening index store");
			this.indexStore=this.config.open(IEnvironmentConfiguration.ServerRoles.index);

			Environment.getInstance().getReportHandler().say("Initializing the query engine");
			this.queryEngine=
				((null!=this.indexStore) && this.indexStore.isOpened()) ? this.indexStore.getQueryFactory() : null;

			// The below methods can only be processed after the data and index stores have been started.
			if ((this.isReady()))
			{
				Environment.getInstance().getReportHandler().say("Registering workers");
				this.registerWorkers();

				Environment.getInstance().getReportHandler().say("Starting the workflow engine.");
				WorkflowEngine.next();

				Environment.getInstance().getReportHandler().say("Registering blob storage");
				Environment.getInstance().getReportHandler().say("Initializing domains");
				if (this.getConfig().isInitializeDomains())
				{
					this.initialize();
				}
				if (Environment.getInstance().getCache().isOpened() && Environment.getInstance().getConfig().isWarmUpEnabled())
				{
					DataVertex.warmUp();
				}
				Environment.getInstance().getReportHandler().say("Resetting Cache");
				Environment.getInstance().getCache().resetCacheAttempts();

				if(this.getConfig().isInitializeWebServer())
				{
					Environment.getInstance().getReportHandler().say("Opening web server");
					this.webServer=this.config.open(IEnvironmentConfiguration.ServerRoles.web);
				}

				Environment.getInstance().getDataStore().getStatistics(true);

				Environment.getInstance().getReportHandler().say("Starting the jobs engine.");
				@SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
				JobsEngine jobsEngine=new JobsEngine();
				jobsEngine.start();

				this.opened = true;

				Environment.getInstance().getReportHandler().say("The Environment has been opened.");
			}
			else
			{
				throw new ApplicationException("The %s store is not opened!",
					((null!=this.dataStore) && this.dataStore.isOpened()) ? "index" : "data"
				);
			}
		}
		else
		{
			throw new ApplicationException("The configuration is not of type EnvironmentConfiguration.");
		}
	}

	@SuppressWarnings("AccessOfSystemProperties")
	private void trustStore()
	{
		File file=new File(this.config.getResourcePath(), this.config.getKeyStoreConfigPathApollo());
		if (file.exists())
		{
			try
			{
				ClientConfiguration cc=new ClientConfiguration(file, true, true);
				this.keyStoreManagerApollo=new KeyStoreManager(
					cc.getKeystorePwd(),
					cc.getKeystore(),
					cc.getTrustStorePwd(),
					cc.getTrustStore()
				);

				System.setProperty("javax.net.ssl.trustStore", this.getKeyStoreManagerApollo().getTrustStore().getAbsolutePath());
				System.setProperty("javax.net.ssl.trustStorePassword", this.getKeyStoreManagerApollo().getTrustStorePwd());
			}
			catch (Exception ex)
			{
				this.getReportHandler().warning(ex);
			}
		}

		file=new File(this.config.getResourcePath(), this.config.getKeyStoreConfigPath());
		if (file.exists())
		{
			try
			{
				ClientConfiguration cc=new ClientConfiguration(file, true, true);
				this.keyStoreManagerServices=new KeyStoreManager(
					cc.getKeystorePwd(),
					cc.getKeystore(),
					cc.getTrustStorePwd(),
					cc.getTrustStore()
				);

				System.setProperty("javax.net.ssl.trustStore", this.keyStoreManagerServices.getTrustStore().getAbsolutePath());
				System.setProperty("javax.net.ssl.trustStorePassword", this.getKeyStoreManagerApollo().getTrustStorePwd());
			}
			catch (Exception ex)
			{
				this.getReportHandler().warning(ex);
			}
		}
	}

	private void validateLicense()
		throws Exception
	{
		File privateKeyFile=this.getConfig().getPrivateKeyPath();
		File licenseFile=this.getConfig().getLicenseFile();
		LicenseManager manager=new LicenseManager(null, privateKeyFile, licenseFile);
		if (!manager.isLicenseValid())
		{
			throw new ApplicationException("The license for using Soterium is no longer valid or missing.");
		}
		else{
			String msg = String.format("Valid License: Access Level: %s Expires On: %s", manager.getAccessLevel(), manager.getExpiresOn().toString("dd-MMM-YYYY HH:mm"));
			this.getReportHandler().say(msg);
			this.getReportHandler().info(msg);
		}
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyNestedMethod"
	})
	private void initialize()
	{
		if (!this.opened && this.config.isInitialize())
		{
			this.opened=true;
			try
			{
				Collection<Class<? extends Initializer>> items=this.reflections.getSubTypesOf(Initializer.class);
				if ((null!=items) && !items.isEmpty())
				{
					List<Initializer> initializers=new ArrayList<>();
					for (Class<? extends Initializer> cls : items)
					{
						if (!ClassX.isAbstract(cls))
						{
							try
							{
								Constructor constructor=cls.getConstructor();
								Initializer init=(Initializer) constructor.newInstance();
								initializers.add(init);
							}
							catch (Exception ex)
							{
								Environment.getInstance().getReportHandler().severe("Could not construct %s.", cls.getName());
								Environment.getInstance().getReportHandler().severe(ex);
							}
						}
					}
					initializers.sort(new Comparator<Initializer>()
					{
						@Override
						public int compare(Initializer o1, Initializer o2)
						{
							return Integer.compare(o1.getInitializeOrdinal(), o2.getInitializeOrdinal());
						}
					});
					for (Initializer initializer : initializers)
					{
						try
						{
							Environment.getInstance().getReportHandler().say("Initializing %s.", initializer.getClass().getSimpleName());
							initializer.initialize();
							Environment.getInstance().getReportHandler().say("done.");
						}
						catch (Exception ex)
						{
							Environment.getInstance().getReportHandler().critical("Could not initialize %s.", initializer.getClass().getName());
							Environment.getInstance().getReportHandler().severe(ex);
						}
					}
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
			}
		}
	}

	private void registerWorkers()
	{
		Collection<Class<? extends BaseAssistantWorker>> clsWorkers=this.reflections.getSubTypesOf(BaseAssistantWorker.class);
		if (null!=clsWorkers)
		{
			for (Class<? extends BaseAssistantWorker> clsWorker : clsWorkers)
			{
				try
				{
					BaseAssistantWorker assistantWorker=BaseAssistantWorker.getOrCreate(clsWorker);
					this.workers.put(clsWorker.getSimpleName(), assistantWorker);
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().severe(ex);
				}
			}
		}
	}

	private void registerCommands()
	{
		Collection<Class<? extends Command>> items=this.reflections.getSubTypesOf(Command.class);
		if (null!=items)
		{
			for (Class<? extends Command> item : items)
			{
				try
				{
					if (!Modifier.isAbstract(item.getModifiers()) && !Modifier.isInterface(item.getModifiers()))
					{
						Command command=item.getConstructor().newInstance();
						if (command.isWebEnabled())
						{
							this.commands.put(command.getName(), command);
						}
					}
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().severe(ex);
				}
			}
		}
	}

	private void registerApolloVertices()
	{
		if (this.apolloVertexTypes.isEmpty())
		{
			Set<Class<?>> filtered=this.reflections.getTypesAnnotatedWith(AtSchemaClass.class);
			if ((null!=filtered) && !filtered.isEmpty())
			{
				for (Class<?> cls : filtered)
				{
					if (ClassX.isKindOf(cls, ApolloVertex.class))
					{
						@SuppressWarnings("unchecked")
						Class<? extends ApolloVertex> aCls=(Class<? extends ApolloVertex>) cls;
						this.apolloVertexTypes.put(ClassHelper.getClassKey(aCls), aCls);
						this.apolloVertexTypes.put(ClassHelper.getIndexKey(aCls), aCls);
					}
					else if (!Objects.equals(cls, DataVertex.class))
					{
						Environment.getInstance().getReportHandler().severe("Found class that does not extend ApolloVertex, %s.", cls.getName());
					}
				}
			}
		}
	}

	private void attachShutDownHook()
	{
		//noinspection ClassExplicitlyExtendsThread
		// Overrides
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try
			{
				Environment.getInstance().close();
			}
			catch (IOException e)
			{
				//noinspection CallToPrintStackTrace
				e.printStackTrace();
			}

		}));
		System.out.println("Shut Down Hook Attached.");
	}

// Getters and setters
	public boolean isDebugMode()
	{
		return this.config.isDebug();
	}

	@SuppressWarnings("unused")
	public boolean isWorkerDebugMode()
	{
		return this.config.isWorkerDebugMode();
	}

	public Environment.RunLevel getRunLevel()
	{
		return this.runLevel;
	}

	public boolean isDevOnly()
	{
		return this.config.isDevOnly();
	}

	public synchronized Collection<Closeable> getCloseables()
	{
		return this.closeables;
	}

	public KeyStoreManager getKeyStoreManagerApollo()
	{
		return this.keyStoreManagerApollo;
	}

	public KeyStoreManager getKeyStoreManagerServices()
	{
		return this.keyStoreManagerServices;
	}

	public List<IAuthorizedUpdateHandler> getAuthorizedUpdateHandlers()
	{
		return this.authorizedUpdateHandlers;
	}

	private void loadAuthorizedUpdateHandlers(){
		Collection<Class<? extends IAuthorizedUpdateHandler>> items=this.reflections.getSubTypesOf(IAuthorizedUpdateHandler.class);
		if ((null!=items) && !items.isEmpty())
		{
			for (Class<? extends IAuthorizedUpdateHandler> cls : items)
			{
				if (!ClassX.isAbstract(cls))
				{
					try
					{
						Constructor constructor=cls.getConstructor();
						IAuthorizedUpdateHandler auth=(IAuthorizedUpdateHandler) constructor.newInstance();
						this.authorizedUpdateHandlers.add(auth);
					}
					catch (Exception ex)
					{
						Environment.getInstance().getReportHandler().severe("Could not construct %s.", cls.getName());
						Environment.getInstance().getReportHandler().severe(ex);
					}
				}
			}
		}
	}

	public String getSecurityClassification()
	{
		return this.getConfig().getSecurityClassification();
	}

	public PrincipalFilterCache getPrincipalFilterCache()
	{
		return this.principalFilterCache;
	}

	public void setRunLevel(Environment.RunLevel runLevel)
	{
		this.runLevel=runLevel;
	}

	public boolean isOpened()
	{
		if (!this.opened)
		{
			//noinspection OverlyComplexBooleanExpression
			this.opened=(null!=this.dataStore) && this.dataStore.isOpened()
			            && (null!=this.indexStore) && this.indexStore.isOpened();
		}
		return this.opened;
	}

	public boolean isReady()
	{
		//noinspection OverlyComplexBooleanExpression
		return (null!=this.dataStore) && this.dataStore.isOpened()
		       && (null!=this.indexStore) && this.indexStore.isOpened();
	}

	@SuppressWarnings("unused")
	public Collection<Class<? extends IJob>> getAuditPolicies()
	{
		if (this.auditPolicies.isEmpty())
		{
			Set<Class<? extends IJob>> subs=this.reflections.getSubTypesOf(IJob.class);
			if (null!=subs)
			{
				for (Class<? extends IJob> sub : subs)
				{
					this.auditPolicies.add(sub);
				}
			}
		}
		return this.auditPolicies;
	}

	@SuppressWarnings("unused")
	public boolean isSandboxMode()
	{
		return this.config.isSandbox();
	}

	/**
	 * Get cache provider.
	 *
	 * @return Cache provider.
	 */
	public ICache getCache()
	{
		return this.cache;
	}

	public IServer getWebServer()
	{
		return this.webServer;
	}

	public IQueryFactory getQueryFactory()
	{
		IQueryFactory result;
		if (null==this.indexStore)
		{
			result=this.dataStore.getQueries();
		}
		else
		{
			result=this.queryEngine;
		}
		return result;
	}

	public Reflections getReflections()
	{
		return this.reflections;
	}

	public boolean isBatchMode()
	{
		return this.config.isBatchMode();
	}

	@SuppressWarnings("unused")
	public void setBatchMode(boolean batchMode)
	{
		this.config.setBatchMode(batchMode);
	}

	public String getName()
	{
		return this.name;
	}

	public Map<String, BaseAssistantWorker> getWorkers()
	{
		return Collections.unmodifiableMap(this.workers);
	}

	public Map<String, Class<? extends ApolloVertex>> getApolloVertexTypes()
	{
		return this.apolloVertexTypes;
	}

	public Map<String, Class<? extends DataVertex>> getReservedNames()
	{
		return this.reservedNames;
	}
}