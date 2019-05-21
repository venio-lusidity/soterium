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

	private boolean result=false;
	private final String command;

	protected EsCommandLine(EsCommandLine.Commands command)
	{
		super();
		this.command=EsConfiguration.getInstance().getCommand(command.toString());
	}

	protected EsCommandLine.CommandItem execute()
		throws IOException
	{
		return this.executeCommand(this.command);
	}

	@SuppressWarnings("NestedAssignment")
	private EsCommandLine.CommandItem executeCommand(String cmd)
		throws IOException
	{
		StringBuilder response=new StringBuilder();
		boolean success = false;

		Process process=Runtime.getRuntime().exec(cmd);
		try (BufferedReader reader=new BufferedReader(new InputStreamReader(process.getInputStream())))
		{
			String line="";
			while ((line=reader.readLine())!=null)
			{
				response.append(line).append("\n");
			}
			success=true;
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().severe(ex);
		}
		return new EsCommandLine.CommandItem(success, ((response.length()>0) ? response.toString() : "no response"));
	}

	public class CommandItem
	{
		private final boolean success;
		private final String response;

		public CommandItem(boolean success, String response){
			super();
			this.success = success;
			this.response = response;
		}

		public boolean isSuccess()
		{
			return this.success;
		}

		public String getResponse()
		{
			return this.response;
		}
	}
}
