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

package com.lusidity.domains.lists.oui;

import com.lusidity.framework.regex.RegExHelper;
import com.lusidity.framework.system.LineHandler;
import com.lusidity.framework.text.StringX;

import java.util.regex.Pattern;

public class OUIHandler implements LineHandler
{
	private final OUIItems ouiItems;
	private int on=0;
	private Object value=null;

	// Constructors
	public OUIHandler(OUIItems ouiItems)
	{
		super();
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.ouiItems=ouiItems;
	}

	// Overrides
	@Override
	public void incrementLinesRead()
	{
		this.on++;
	}

	@Override
	public int getLinesRead()
	{
		return this.on;
	}

	@Override
	public Object getValue()
	{
		return this.value;
	}

	@SuppressWarnings("OverlyComplexMethod")
	@Override
	public boolean handle(String line)
	{
		try
		{
			if (!StringX.isBlank(line))
			{
				String[] parts=Pattern.compile("\\t").split(line);
				OUIItem item=null;
				if ((null!=parts) && (parts.length==2))
				{
					String shortName=null;
					String name=parts[1].trim();
					if (StringX.contains(name, "#"))
					{
						String[] names=StringX.split(name, "#");
						assert names!=null;
						if (names.length==2)
						{
							shortName=names[0].trim();
							name=names[1].trim();
						}
					}
					item=new OUIItem(parts[0], shortName, name);
				}
				if (null!=item)
				{
					String oui=null;
					if (RegExHelper.MAC_OUI.matcher(item.getOui()).matches())
					{
						oui=item.getOui();
					}
					else if (RegExHelper.MAC_RANGE.matcher(item.getOui()).matches())
					{
						//noinspection MagicNumber
						oui=StringX.substring(item.getOui(), 0, 13);
					}
					if (!StringX.isBlank(oui))
					{
						this.ouiItems.add(item);
					}
				}

			}
		}
		catch (Exception ignored)
		{
		}
		return false;
	}

	@SuppressWarnings("MethodCanBeVariableArityMethod")
	@Override
	public boolean handle(String[] values)
	{
		return false;
	}
}
