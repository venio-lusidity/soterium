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

package com.lusidity.jobs.importer;

import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.system.assistant.message.FileImportMessage;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.collections.CollectionX;
import com.lusidity.framework.json.JsonData;

import java.util.Collection;

@AtSchemaClass(name="Importer Preprocessor", discoverable=false, description="")
public class ImporterPreprocessor extends BaseDomain
{
	public static ImporterPreprocessor instance=null;
	@AtSchemaProperty(name="FileInfo Import Messages", expectedType=FileImportMessage.class)
	private ElementEdges<FileImportMessage> messages = null;

// Constructors
	public ImporterPreprocessor()
	{
		super();
	}

	public ImporterPreprocessor(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

// Overrides
	@Override
	public void initialize()
		throws Exception
	{
		super.initialize();
		Collection<ImporterPreprocessor> preprocessors=VertexFactory.getInstance().getAll(ImporterPreprocessor.class, 0, 10);
		ImporterPreprocessor preprocessor;
		if (preprocessors.isEmpty())
		{
			preprocessor=new ImporterPreprocessor();
			preprocessor.fetchTitle().setValue(ImporterPreprocessor.class.getSimpleName());
			preprocessor.save();
		}
		else
		{
			preprocessor=CollectionX.getFirst(preprocessors);
		}
		ImporterPreprocessor.instance=preprocessor;
	}

	@Override
	public int getInitializeOrdinal()
	{
		return 1000;
	}

// Methods
	public static ImporterPreprocessor getInstance()
	{
		return ImporterPreprocessor.instance;
	}

// Getters and setters
	public ElementEdges<FileImportMessage> getMessages()
	{
		if (null==this.messages)
		{
			this.buildProperty("messages");
		}
		return this.messages;
	}


}
