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

package com.lusidity.domains.system.assistant.message;

import java.io.File;

public class MessageObject
{
	private final String vertexId;
	private final String types;
	private final String fileName;
	private final String originalName;
	private final String extension;
	private final String path;
	private final File file;

	// Constructors
	public MessageObject(String vertexId, String types, String fileName, String originalName, String extension, String path, File file){
		super();

		this.vertexId = vertexId;
		this.types = types;
		this.fileName = fileName;
		this.originalName = originalName;
		this.extension = extension;
		this.path = path;
		this.file = file;
	}

	public void create()
	{
		FileImportMessage.create(this.getVertexId(), this.getTypes(), this.getFileName(), this.getOriginalName(), this.getExtension(), this.getPath(), this.getFile());
	}

	public String getVertexId()
	{
		return this.vertexId;
	}

	public String getTypes()
	{
		return this.types;
	}

	public String getFileName()
	{
		return this.fileName;
	}

	public String getOriginalName()
	{
		return this.originalName;
	}

	public String getExtension()
	{
		return this.extension;
	}

	public String getPath()
	{
		return this.path;
	}

	public File getFile()
	{
		return this.file;
	}
}
