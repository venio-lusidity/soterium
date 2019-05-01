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

package com.lusidity.collections;

import com.lusidity.domains.data.ProcessStatus;
import org.apache.commons.collections.CollectionUtils;

import java.security.InvalidParameterException;
import java.util.Collection;

public class SiblingsIterator
{
	private final ISiblingsIteratorHandler handler;
	private final ProcessStatus processStatus;
	private boolean stopping = false;

	// Constructors
	public SiblingsIterator(ISiblingsIteratorHandler handler, ProcessStatus processStatus){
		super();
		this.handler = handler;
		this.processStatus = processStatus;
	}

	public void iterate(Collection items, int start, int limit)
		throws Exception
	{
		if(null==items){
			throw new InvalidParameterException("The items collection cannot be null.");
		}

		int len = items.size();
		int max =((len<limit) || (limit==0)) ? len : limit;
		for(int i = start; i < len; i++) {
			if(this.stopping){
				break;
			}
			Object parent = CollectionUtils.get(items, i);
			SiblingsResult sr = null;
			for(int j = (i+1); j < len; j++){
				if(this.stopping){
					break;
				}
				Object sibling = CollectionUtils.get(items, j);
				sr = this.handler.handle(parent, sibling, i, this.processStatus);
				if(sr.isInnerStop()){
					break;
				}
			}
			if((null!=sr) && sr.isOuterStop()) {
				break;
			}
			if((i+1)>=max){
				break;
			}
		}
	}

	public void stop()
	{
		this.stopping = true;
	}
}
