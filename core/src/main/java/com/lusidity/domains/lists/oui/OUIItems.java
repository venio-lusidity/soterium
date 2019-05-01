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

import com.lusidity.Environment;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.system.FileX;
import com.lusidity.framework.text.StringX;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


@SuppressWarnings({
	"NullableProblems",
	"StandardVariableNames"
})
public class OUIItems implements Set<OUIItem>
{

	private static OUIItems instance=null;
	private Set<OUIItem> underlying=new HashSet<>();

	// Constructors
	public OUIItems()
	{
		super();
		this.load();
	}

	private void load()
	{
		this.underlying=new HashSet<>();
		try
		{
			File file=new File(Environment.getInstance().getConfig().getResourcePath(), "/data/oui.txt");
			if (file.exists())
			{
				OUIHandler ouiHandler=new OUIHandler(this);
				FileX.readLines(file, ouiHandler);
			}
			else
			{
				//noinspection ThrowCaughtLocally
				throw new ApplicationException("The resource/data/oui.txt file does not exist.");
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

	// Overrides
	@Override
	public int size()
	{
		return this.underlying.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.underlying.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return this.underlying.contains(o);
	}

	@Override
	public Iterator<OUIItem> iterator()
	{
		return this.underlying.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return this.underlying.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		//noinspection SuspiciousToArrayCall
		return this.underlying.toArray(a);
	}

	@Override
	public boolean add(OUIItem ouiItem)
	{
		return this.underlying.add(ouiItem);
	}

	@Override
	public boolean remove(Object o)
	{
		return this.underlying.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return this.underlying.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends OUIItem> c)
	{
		return this.underlying.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return this.underlying.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return this.underlying.retainAll(c);
	}

	@Override
	public void clear()
	{
		this.underlying.clear();
	}

	// Methods
	public static synchronized OUIItems getInstance()
	{
		if ((null==OUIItems.instance) || OUIItems.instance.isEmpty())
		{
			OUIItems.instance=new OUIItems();
		}
		return OUIItems.instance;
	}

	public OUIItem getMatch(String macAddress)
	{
		OUIItem result=null;
		try
		{
			if (!StringX.isBlank(macAddress))
			{
				for (OUIItem item : this)
				{
					String oui=item.getOui();
					String range=oui;
					if (item.getOui().length()>=14)
					{
						range=StringX.substring(item.getOui(), 0, 14);
					}
				/*
				this seems to be expensive to run.
				if (RegExHelper.MAC_OUI.matcher(item.getOui()).matches())
				{
					oui=item.getOui();
				}
				else if (RegExHelper.MAC_RANGE.matcher(item.getOui()).matches())
				{
					oui=StringX.substring(item.getOui(), 0, 14);
				}
				*/
					if ((!StringX.isBlank(oui) && StringX.startsWithIgnoreCase(macAddress, oui))
					    || (!StringX.isBlank(range) && StringX.startsWithIgnoreCase(macAddress, range)))
					{
						result=item;
						break;
					}
				}
			}
		}
		catch (Exception ignored){}
		return result;
	}
}
