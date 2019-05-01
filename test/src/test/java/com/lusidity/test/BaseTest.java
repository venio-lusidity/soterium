package com.lusidity.test;

import com.lusidity.Environment;
import com.lusidity.LogHandlerCallback;
import com.lusidity.configuration.SoteriumConfiguration;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.text.StringX;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;

/** Base class for unit tests. */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public
abstract class BaseTest implements Closeable
{
    private static boolean SCOPING=false;
    protected static String LIVE_CONFIG = "server_console_remote.json";

    private static boolean CLEAR_STORES = true;
    private static boolean Initialize = true;
    private static boolean Initialize_WEB = true;

    public static String getTestConfig(){
        return BaseTest.LIVE_CONFIG;
    }
    public static void setTestConfig(String configPath){
        BaseTest.LIVE_CONFIG = configPath;
    }
	public static void setClearStores(boolean value) { BaseTest.CLEAR_STORES = value;}
    public static void setInitialize(boolean value) { BaseTest.Initialize = value;}
    public static void setInitializeWebServer(boolean value) { BaseTest.Initialize_WEB = value;}
    public static void setScoping(boolean value) { BaseTest.SCOPING = value;}

    @BeforeClass
    public static
    void beforeClass()
            throws Exception
    {
        try {
            String path = String.format("../resource/config/%s", BaseTest.getTestConfig());

            System.out.println("\n********************** Warning **********************\n ");
            System.out.println(String.format("Using %s file for testing.", path));
            System.out.println("\n****************************************************\n");

            File configFile = new File(path);

            // This creates an instance of the ReportHandler which is needed.
            @SuppressWarnings("unused")
            ReportHandler reportHandler = new ReportHandler(Level.FINE, true, true, true, new LogHandlerCallback());

            SoteriumConfiguration config = new SoteriumConfiguration(configFile);
            config.setClearStores(BaseTest.CLEAR_STORES);
            config.setServerMode(false);
            config.setInitialize(BaseTest.Initialize);
            config.setInitializeWebServer(BaseTest.Initialize_WEB);

            //  Open environment
            Environment.open("Soterium Test", config);
        }
        catch (Exception ex)
        {
            throw new ApplicationException(ex);
        }
    }

    @AfterClass
    public static void afterClass(){
        BaseTest.setTestConfig(BaseTest.getTestConfig());
        BaseTest.setClearStores(true);
        BaseTest.setInitialize(true);
    }

    private static boolean ask(String ask, Boolean defaultValue) {
        boolean result;
        try {
            Scanner scan = new Scanner(System.in);
            System.out.println(ask);
            String answer = scan.next();
            result = (!StringX.isBlank(answer) && answer.equalsIgnoreCase("yes"));
        }
        catch (Exception ignored){
            result = defaultValue;
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        Environment environment = Environment.getInstance();
        if(null!=environment){
            environment.close();
        }
    }

    public static
    boolean getClearStores()
    {
        return BaseTest.CLEAR_STORES;
    }

    public abstract boolean isDisabled();
}