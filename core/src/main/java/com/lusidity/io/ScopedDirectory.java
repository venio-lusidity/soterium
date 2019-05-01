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

package com.lusidity.io;

import com.lusidity.framework.text.StringX;

import java.io.File;

public class ScopedDirectory
{
	private final File file;
	private final String category;

	public ScopedDirectory(String path, String category){
		super();
		this.file = new File(path);
		this.category = category;
	}

	public boolean hasCategory(String category){
		return StringX.equalsIgnoreCase(category, this.category);
	}

	public File getFile()
	{
		return this.file;
	}

	public String getCategory()
	{
		return this.category;
	}
}
