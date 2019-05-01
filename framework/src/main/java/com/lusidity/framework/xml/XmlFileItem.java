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

package com.lusidity.framework.xml;

import com.lusidity.framework.text.StringX;

import java.io.File;

public
class XmlFileItem
{
	private File file= null;
	private String groupNamespace= null;
	private String itemNamespace= null;

	// Constructors
	public XmlFileItem(File xmlFile, String groupNamespace, String itemNamespace){
		super();
		this.file= xmlFile;
		this.groupNamespace = groupNamespace;
		this.itemNamespace = itemNamespace;
	}

	@SuppressWarnings("MagicNumber")
	public boolean isMaxed(long maxSizeInKb)
	{
		boolean result=false;
		try
		{
			long fileBytes=this.getFile().length();
			long fileMb=(fileBytes/10000);
			result=fileMb>maxSizeInKb;
		}
		catch (Exception ignored)
		{

		}
		return result;
	}

	// Getters and setters
	public boolean isValid()
	{
		return (this.getFile().exists() && !StringX.isBlank(this.getGroupNamespace()) && !StringX.isBlank(this.getItemNamespace()));
	}

	public File getFile()
	{
		return this.file;
	}

	public
	void setFile(File file)
	{
		this.file=file;
	}

	public
	String getGroupNamespace()
	{
		return this.groupNamespace;
	}

	public
	String getItemNamespace()
	{
		return this.itemNamespace;
	}
}
