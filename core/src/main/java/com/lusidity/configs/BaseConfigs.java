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

package com.lusidity.configs;

import com.lusidity.framework.json.JsonData;

import java.io.File;
import java.security.InvalidParameterException;

public abstract class BaseConfigs implements IConfigs
{
	private final File file;
	private Long lastChecked = null;
	private JsonData config = null;

	private static BaseConfigs instance = null;

	public static <T extends BaseConfigs> T getInstance(){
		//noinspection unchecked
		return (null==BaseConfigs.instance) ? null : (T)BaseConfigs.instance;
	}

	public BaseConfigs(File file, boolean assignToInstance){
		super();
		this.file = file;
		this.load();
		if(assignToInstance)
		{
			BaseConfigs.instance=this;
		}
	}

	/**
	 * Verifies the file and then loads and verifies it as a JsonData object.
	 */
	private void load()
	{
		if(!this.file.exists()){
			throw new InvalidParameterException(String.format("The configuration file, %s, does not exist.", this.file.getAbsolutePath()));
		}
		this.config= new JsonData(this.file);
		if(!this.config.isValid()){
			throw new InvalidParameterException(String.format("The configuration file is either not a proper JSON format, %s.", this.file.getAbsolutePath()));
		}
		this.lastChecked=this.file.lastModified();
	}

	@Override
	public boolean isOpen()
	{
		return (null!=this.config) && this.config.isValid();
	}

	/**
	 * Check the file's modified when date to determine if it has been updated.
	 * @return true or false
	 */
	@Override
	public boolean isUpdated()
	{
		boolean result = false;
		long check = (this.file.exists()) ? this.file.lastModified() : 0;
		result = (null!=this.lastChecked) && (check>this.lastChecked);
		this.lastChecked = check;
		return result;
	}
}
