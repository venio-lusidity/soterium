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

package com.lusidity.athena.console;

import com.lusidity.Environment;
import com.lusidity.console.Console;
import com.lusidity.framework.exceptions.ApplicationException;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/** Text-based console for Skynet. */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public
class AthenaConsole extends Console
{
	private static final String DEFAULT_PROMPT = "> ";
	private final InputStreamReader consoleReader = new InputStreamReader(System.in);
	private final BufferedReader consoleBuffer = new BufferedReader(this.consoleReader);
	private final IProgram program;
	private String prompt = AthenaConsole.DEFAULT_PROMPT;
	private boolean opened = true;

	/**
	 * Default constructor.
	 *
	 * @param program
	 * 	Environment instance.
	 */
	public
	AthenaConsole(IProgram program)
		throws ApplicationException
	{
		super(program.getReflections());
        this.program = program;
	}

	public
	void run(String[] args)
		throws ApplicationException
	{
		DateTime startedWhen = DateTime.now();
		this.say(String.format("Start: %s.%n", startedWhen.toString()));

		/**
		 * Process any command line arguments.
		 */

		if (null != args)
		{
			this.interpret(args, false);
		}

        Map<String, String> properties = this.getEnvironment().getAppProperties();
        if (!properties.isEmpty())
        {
            for (Map.Entry<String, String> property : properties.entrySet())
            {
                this.say(String.format("%s: %s", property.getKey(), property.getValue()));
            }
        }

		if (this.getEnvironment().getRunLevel() == Environment.RunLevel.Console)
		{
			this.say("Greetings, Professor Falken.");
            this.listening = true;

			this.start();
        }
	}

	private
	void start()
	{
		String inputLine;
		inputLine = this.listen();
		if (null!=inputLine)
		{
			this.interpret(inputLine, true /* isInteractive */);
		}
		if(this.isOpened())
		{
			this.start();
		}
		else{
			this.say("No longer listening.");
		}
	}

	/**
	 * Listen for one line of user input on console.
	 *
	 * @return User input.
	 */
	public
	String listen()
	{
		System.out.print(this.prompt);
		String str;
		try
		{
			str = this.consoleBuffer.readLine();
		}
		catch (IOException ignored)
		{
			//  Ignore com.lusidity.mind.client.exceptions
			str = "";
		}
		return str;
	}


	/**
	 * Output a message to the console.
	 *
	 * @param message
	 * 	Message to WriteConsoleJob.
	 */
	@SuppressWarnings("MethodMayBeStatic")
	public
	void say(String message)
	{
		System.out.printf(
			"%s%n", message
		);
	}

    @Override
    public void close() throws IOException {
	    this.opened = false;
        super.close();
        this.program.close();
    }

    /**
	 * Output a printf/format-style formatted message to the console.
	 *
	 * @param format
	 * 	Format (as defined by Java printf and format methods).
	 * @param args
	 * 	Arguments.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public
	void format(String format, Object... args)
	{
		String s = String.format(format, args);
		this.say(s);
	}

	@SuppressWarnings("UnusedDeclaration")
	public
	String setPrompt(String s)
	{
		String old = this.prompt;
		this.prompt = s;
		return old;
	}

    public void runCommands(String[] args) {
        if (null != args)
        {
            this.interpret(args, false);
        }
    }

    public Environment getEnvironment() {
        return this.program.getEnvironment();
    }

	public
	boolean isOpened()
	{
		return opened;
	}
}
