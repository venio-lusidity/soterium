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

package com.lusidity.console;

import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.text.StringX;
import com.lusidity.framework.time.Stopwatch;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.Duration;
import org.reflections.Reflections;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("MethodMayBeStatic")
public class Console implements Closeable
{
	private static final String HELP_OPTION="help";
	protected final SortedMap<String, Command> commandsMap;
	protected boolean listening = false;
	private final InputStreamReader consoleReader=new InputStreamReader(System.in);
	private final BufferedReader bufferedConsoleReader=new BufferedReader(this.consoleReader);

// Constructors
	public Console(Reflections reflections)
		throws ApplicationException
	{
		super();
		this.commandsMap=new TreeMap<>();

		Set<Class<? extends Command>> commandClasses=reflections.getSubTypesOf(Command.class);
		for (Class<? extends Command> commandCls : commandClasses)
		{
			try
			{
				if (!Modifier.isAbstract(commandCls.getModifiers()) && !Modifier.isInterface(commandCls.getModifiers()))
				{
					Command cmd=commandCls.getConstructor().newInstance();
					String cmdName=cmd.getName();
					this.commandsMap.put(cmdName, cmd);
				}
			}
			catch (Exception e)
			{
				throw new ApplicationException(e);
			}
		}
	}

// Overrides
	@Override
	public void close()
		throws IOException
	{
		IOUtils.closeQuietly(this.bufferedConsoleReader);
		IOUtils.closeQuietly(this.consoleReader);
	}

	/**
	 * Show help.
	 *
	 * @param commandName Command for which to show help, or null to list commands.
	 */
	public void help(String commandName)
	{
		if (null==commandName)
		{
			this.say("Commands:");
			for (Command cmd : this.commandsMap.values())
			{
				String cmdName=cmd.getName();
				String cmdDesc=cmd.getDescription();
				this.say(String.format("%-16s %s", cmdName, cmdDesc));
			}
		}
		else
		{
			Command command=this.commandsMap.get(commandName);
			if (null!=command)
			{
				Options commandOptions=command.getOptions();
				HelpFormatter helpFormatter=new HelpFormatter();
				helpFormatter.printHelp(
					commandName, commandOptions
				);
			}
			else
			{
				this.say(String.format("Help is not available for '%s'.", commandName));
			}
		}
	}

	public void say(String message)
	{
		System.out.println(message);
	}

	/**
	 * Show help.
	 *
	 * @param commandOptions Command for which to show help, or null to list commands.
	 */
	public void help(String commandName, Options commandOptions)
	{
		HelpFormatter helpFormatter=new HelpFormatter();
		helpFormatter.printHelp(
			commandName, commandOptions
		);
	}

	/**
	 * Interpret a command line.
	 *
	 * @param commandLine   Command line.
	 * @param isInteractive true if operating in interactive mode, false if operating in bulk/script mode.
	 */
	public void interpret(CharSequence commandLine, boolean isInteractive)
	{
		Collection<String> matchList=new ArrayList<>();
		Pattern regex=Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
		Matcher regexMatcher=regex.matcher(commandLine);
		while (regexMatcher.find())
		{
			if (regexMatcher.group(1)!=null)
			{
				// Add double-quoted string without the quotes
				matchList.add(regexMatcher.group(1));
			}
			else if (regexMatcher.group(2)!=null)
			{
				// Add single-quoted string without the quotes
				matchList.add(regexMatcher.group(2));
			}
			else
			{
				// Add unquoted word
				matchList.add(regexMatcher.group());
			}
		}

		@SuppressWarnings("ZeroLengthArrayAllocation") String[] matches=new String[0];
		matches=matchList.toArray(matches);
		this.interpret(matches, isInteractive);
	}

	/**
	 * Interpret a command line.
	 *
	 * @param tokens        Tokenized command line.
	 * @param isInteractive true if this command is from an interactive console session.
	 */
	public void interpret(String[] tokens, boolean isInteractive)
	{
		Command command=null;

		int nTokens=tokens.length;
		if (nTokens>0)
		{
			String commandToken=tokens[0];

			String[] args=new String[nTokens-1];
			System.arraycopy(tokens, 1, args, 0, nTokens-1);

			command=this.commandsMap.get(commandToken);

			if (null!=command)
			{
				Options options=command.getOptions();
				if (null==options)
				{
					options=new Options();
				}

				if (!options.hasOption(Console.HELP_OPTION))
				{
					options.addOption(Console.HELP_OPTION, false, "Help for this command.");
				}

				Stopwatch stopwatch=new Stopwatch();
				stopwatch.start();

				@SuppressWarnings("deprecation")
				CommandLineParser parser=new GnuParser();

				try
				{
					boolean helped = false;
					if(args.length>0){
						String help = args[0];
						if(StringX.containsIgnoreCase(help, "-help")){
							helped = true;
							command.showHelp(this, null);
						}
					}

					if(!helped)
					{
						CommandLine commandLine=parser.parse(options, args);

						command.execute(this, commandLine);

						Duration elapsed=stopwatch.stop();

						if (isInteractive)
						{
							this.say("OK");
						}
					}

				}
				catch (Exception e)
				{
					//  Show com.lusidity.mind.client.exceptions, but do not crash
					String str=ExceptionUtils.getStackTrace(e);
					this.say(str);
				}
			}
		}

		if (isInteractive && (null==command))
		{
			this.say("Command not recognized. Try 'help' for help.");
		}
	}

	public String listen(String prompt)
		throws IOException
	{
		this.say(prompt);
		return this.listen();
	}

	public String listen()
		throws IOException
	{
		return this.bufferedConsoleReader.readLine();
	}

	public String listen(String prompt, Object... args)
		throws IOException
	{
		this.say(prompt, args);
		return this.listen();
	}

	public void say(String format, Object... args)
	{
		System.out.format(format+'\n', args);
	}

	public void stopListening()
	{
		this.listening=false;
	}
}
