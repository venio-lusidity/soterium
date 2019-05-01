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

package com.lusidity.components;

import com.lusidity.framework.exceptions.ApplicationException;

public abstract class BaseModule
	implements Module
{
	private boolean opened=false;

// --------------------- GETTER / SETTER METHODS ---------------------


// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Module ---------------------

// Overrides
	@Override
	public void open(Object... params)
		throws ApplicationException
	{
		this.opened=true;
	}

	@Override
	public void close()
	{
	}

	@Override
	public boolean isOpened()
	{
		return this.opened;
	}

	@Override
	public boolean isEnabled()
	{
		//  Modules are enabled by default
		return true;
	}

	@Override
	public int hashCode()
	{
		return this.getClass().getName().hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		return (o!=null) && o.getClass().equals(this.getClass());
	}
}
