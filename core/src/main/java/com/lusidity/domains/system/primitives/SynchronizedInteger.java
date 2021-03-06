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

package com.lusidity.domains.system.primitives;

import com.lusidity.data.field.KeyData;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;

@AtSchemaClass(name="Synchronized Integer", discoverable = false, description = "Allows for thread safe changes of an integer.")
public class SynchronizedInteger extends Primitive
{
	private KeyData<Integer> count = null;

	// Constructors
	public SynchronizedInteger(){
		super();
	}

	public SynchronizedInteger(JsonData dso, Object indexId){
		super(dso, indexId);
	}

	public synchronized void set(int value)
	{
		this.fetchCount().setValue(value);
	}

	public synchronized KeyData<Integer> fetchCount(){
		if(null==this.count){
			this.count =new KeyData<>(this, "count", Integer.class, false, 0);
		}
		return this.count;
	}

	public synchronized void reset(){
		this.fetchCount().setValue(0);
	}

	public synchronized void increment(){
		this.add(1);
	}

	public synchronized void add(int add){
		int on = this.fetchCount().getValue();
		on+=add;
		this.fetchCount().setValue(on);
	}

	public synchronized void decrement(){
		this.subtract(1);
	}

	public synchronized void subtract(int subtract){
		int on = this.fetchCount().getValue();
		on-=subtract;
		if(on<0){
			on=0;
		}
		this.fetchCount().setValue(on);
	}

	// Getters and setters
	public synchronized int getCount()
	{
		return this.fetchCount().getValue();
	}
}
