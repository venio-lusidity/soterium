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

package com.lusidity.blockers;


import com.lusidity.Environment;
import com.lusidity.framework.time.Stopwatch;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Example usage...
 * <p>
 * <p>
 * private static final ValueBlocker VALUE_BLOCKER = new ValueBlocker();
 * <p>
 * public static ValueBlocker getValueBlocker(){
 * return SomeClass.VALUE_BLOCKER;
 * }
 * <p>
 * Then to block and release...
 * <p>
 * UUID id = SomeClass.getValueBlocker().block(indexedValue);
 * <p>
 * if(null!=id){
 * SomeClass.getValueBlocker().unblock(indexedValue, id);
 * }
 */
public class ValueBlocker
{

	public final LinkedHashMap<Object, Collection<UUID>> queue=new LinkedHashMap<>();

	public UUID block(Object obj)
	{
		UUID uuid=null;

		if (null!=obj)
		{
			uuid=UUID.randomUUID();
			this.addOrRemove(obj, uuid, true);

			Stopwatch stopwatch=new Stopwatch();
			stopwatch.start();

			while (!this.isNext(obj, uuid))
			{
				if (stopwatch.elapsed().getMillis()>30000)
				{
					Environment.getInstance().getReportHandler().severe(String.format("The obj, %s, is not being unblocked.", obj));
					this.addOrRemove(obj, uuid, false);
					break;
				}
			}
			stopwatch.stop();
		}

		return uuid;
	}

	private synchronized void addOrRemove(Object key, UUID uuid, boolean add)
	{
		Collection<UUID> working=this.queue.get(key);
		if (add)
		{
			if (null!=working)
			{
				working.add(uuid);
			}
			else
			{
				Collection<UUID> ids=new ArrayList<>();
				ids.add(uuid);
				this.queue.put(key, ids);
			}
		}
		else
		{
			if (null!=working)
			{
				working.remove(uuid);
				if (working.isEmpty())
				{
					this.queue.remove(key);
				}
			}
		}
	}

	public boolean isNext(Object key, UUID uuid)
	{
		// if the hash code is not in the queue immediately release this object.
		boolean result=false;
		try
		{
			if (this.queue.containsKey(key))
			{
				UUID check=(UUID) CollectionUtils.get(this.queue.get(key), 0);
				result=uuid.equals(check);
			}
		}
		catch (Exception ignored)
		{
			if (!this.queue.containsKey(key))
			{
				this.addOrRemove(key, uuid, true);
			}
		}

		return result;
	}

	public void unblock(Object obj, UUID uuid)
	{
		this.addOrRemove(obj, uuid, false);
	}
}
