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

import com.lusidity.email.EmailX;
import com.lusidity.framework.reports.ReportHandlerCallback;
import com.lusidity.framework.text.StringX;

import java.util.Objects;
import java.util.logging.Level;

public class LogHandlerCallback implements ReportHandlerCallback
{
// Constructors
	public LogHandlerCallback()
	{
	}

// Overrides
public boolean processed(Level level, String message, Object... args)
{
	return this.processed(level, String.format(message, args));
}

	@Override
	public boolean processed(Level level, String message)
	{
		boolean result=false;
		try
		{
			Level lvl=Environment.getInstance().getConfig().getEmailLogLevel();
			if (null!=lvl)
			{
				int expected=this.getLevel(lvl);
				int actual=this.getLevel(level);

				result=(actual>=expected);
				if (result && !StringX.isBlank(message) && !StringX.containsIgnoreCase(message, "javax.mail"))
				{
					String subject=String.format("%s: %s", level.toString(), Environment.getInstance().getConfig().getServerName());
					result=EmailX.sendMail(EmailX.getDefaultServerKey(),
						EmailX.getDefaultFrom(), EmailX.getSystemAdmins(), null, null, subject, message, true, null
					);
				}
			}
		}
		catch (Exception ignored){}
		return result;
	}

	private int getLevel(Level level)
	{
		//noinspection MagicNumber
		return (Objects.equals(level, Level.ALL)) ? 1200 : level.intValue();
	}
}
