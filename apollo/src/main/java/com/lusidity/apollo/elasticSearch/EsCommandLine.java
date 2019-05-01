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

package com.lusidity.apollo.elasticSearch;

import com.lusidity.framework.reports.ReportHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EsCommandLine
{
	public enum Commands
	{
		initialize
	}

	private static boolean result=false;
	private final String command;

	protected EsCommandLine(EsCommandLine.Commands command)
	{
		super();
		this.command=EsConfiguration.getInstance().getCommand(command.toString());
	}

	protected boolean execute()
		throws IOException
	{
		String output=EsCommandLine.executeCommand(this.command);
		System.out.println(output);
		return EsCommandLine.result;
	}

	@SuppressWarnings("NestedAssignment")
	private static String executeCommand(String cmd)
		throws IOException
	{
		StringBuilder out=new StringBuilder();

		Process process=Runtime.getRuntime().exec(cmd);
		try (BufferedReader reader=new BufferedReader(new InputStreamReader(process.getInputStream())))
		{
			String line="";
			while ((line=reader.readLine())!=null)
			{
				out.append(line).append("\n");
			}
			EsCommandLine.result=true;
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().severe(ex);
			System.out.println(ex.getMessage());
		}
		return out.toString();
	}
}
