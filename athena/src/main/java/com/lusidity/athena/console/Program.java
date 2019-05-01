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

package com.lusidity.athena.console;

import com.lusidity.Environment;
import com.lusidity.configuration.BaseSoteriumConfiguration;
import com.lusidity.configuration.CommandItem;
import com.lusidity.configuration.SoteriumConfiguration;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.text.StringX;
import org.apache.commons.cli.*;
import org.apache.log4j.PropertyConfigurator;
import org.reflections.Reflections;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;
import java.util.logging.Level;

/** Main executable program class. */
public
class Program implements IProgram
{
    private static final String PROGRAM_NAME = "lusidity";
    private static final String OPTION_CONFIG = "config";
    private static final String OPTION_SERVER = "server";
    private static final String OPTION_CLEAR = "clear";

    private static Program instance = null;
    private Environment environment = null;
    private Reflections reflections = null;
    private AthenaConsole console = null;

    /**
     * Private constructor for main entry point class.
     */
    private Program() {
        super();
    }

    protected static Program getInstance()
    {
        if(null==Program.instance)
        {
            //noinspection NonThreadSafeLazyInitialization
            Program.instance = new Program();
        }

        return Program.instance;
    }

    /**
     * Main entry point.
     *
     * @param args Command line arguments.
     * @throws Throwable
     */
    public static void main(String[] args)
            throws Throwable {

        // turn reflections logging off
        Reflections.log = null;

        Program program = Program.getInstance();

        Program.registerShutdownHook();

        Options options = Program.getOptions();

        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = parser.parse(options, args);

        File configuration = new File(cmdLine.getOptionValue(Program.OPTION_CONFIG));
        boolean serverMode = Boolean.parseBoolean(cmdLine.getOptionValue(Program.OPTION_SERVER));
        boolean clear = Boolean.parseBoolean(cmdLine.getOptionValue(Program.OPTION_CLEAR));

        if(configuration.exists() && configuration.isFile()) {

            System.out.println("\n********************** Warning **********************\n ");
            System.out.println(String.format("Using %s file for configuration.", configuration.getAbsolutePath()));
            System.out.println("\n****************************************************\n");

            ReportHandler reportHandler = new ReportHandler(Level.FINE, true, true, true, null);

            BaseSoteriumConfiguration config = new SoteriumConfiguration(configuration);
            config.setClearStores(clear);
            config.setServerMode(serverMode);

            reportHandler.close();

            if(!StringX.isBlank(config.getResourcePath())){
                File file = new File(config.getResourcePath(), "log4j/log4j.properties");
                if(file.exists()){
                    PropertyConfigurator.configure(file.getCanonicalPath());
                }
            }

            if(config.clearStores() && !config.isServerMode()) {
                System.out.println("\n\n");
                System.out.println("\n?????????????????????? Warning ??????????????????????\n ");
                System.out.println("You are about to delete all data, which is not recoverable.");
                boolean clearStores = Program.ask(System.console(), "Are you sure you want to do this? (yes/no)", false);
                if(!config.isServerMode()) {
                    System.out.println("\n\n");
                    System.out.println(String.format("All data will%s be deleted.", clearStores ? "" : " not"));
                    System.out.println("\n****************************************************\n");
                }
                config.setClearStores(clearStores);
            }

            //  Open environment
            program.environment = Environment.open(Program.PROGRAM_NAME, config);

            if (config.isServerMode()) {
                program.environment.setRunLevel(Environment.RunLevel.Server);
            }

            //  Launch shell
            String[] shellArgs = cmdLine.getArgs();
            program.console = new AthenaConsole(program);

            Collection<CommandItem> commands = config.getCommands();
            if ((null != commands) && !commands.isEmpty()) {
                for(CommandItem cmd: commands)
                {
                    program.console.runCommands(cmd.getParts());
                }
            }

            program.console.run(shellArgs);
        }
    }

    private static boolean ask(Console sysConsole, String ask, Boolean defaultValue) {
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

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    ReportHandler handler = (null==Environment.getInstance()) ? null : Environment.getInstance().getReportHandler();
                    if(null!=handler){
                        handler.critical("Running shutdown hook.");
                    }
                    else{
                        System.out.println("Running shutdown hook.");
                    }
                    Program.getInstance().close();
                    if(null!=handler){
                        handler.critical("Shutdown completed.");
                    }
                    else{
                        System.out.println("Shutdown completed.");
                    }
                }
                catch (IOException e) {
                    System.out.println("Shutdown hook failed.");
                    e.printStackTrace();
                }
            }
        });
    }

    public static Options getOptions() {
        Options options = new Options();

        Option option = new Option(Program.OPTION_CONFIG, true, "Location of configuration file.");
        option.setRequired(true);
        options.addOption(option);

        Option server = new Option(Program.OPTION_SERVER, true, "Run Athena with its command window.");
        server.setRequired(true);
        server.setType(Boolean.class);
        options.addOption(server);

        Option clear = new Option(Program.OPTION_CLEAR, true, "Clear the Apollo data and index stores on startup.");
        clear.setRequired(true);
        clear.setType(Boolean.class);
        options.addOption(clear);

        return options;
    }

    @Override
    public Environment getEnvironment()
    {
        return this.environment;
    }

    @Override
    public Reflections getReflections() {
        if(null==this.reflections)
        {
            this.reflections = this.environment.getReflections();
        }
        return this.reflections;
    }

    @Override
    public void close() throws IOException {
        if(null!=this.environment) {
            this.environment.close();
        }
    }
}