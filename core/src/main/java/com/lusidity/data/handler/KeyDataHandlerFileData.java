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

package com.lusidity.data.handler;

import com.lusidity.data.field.IKeyDataHandler;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.field.KeyDataTransformed;
import com.lusidity.framework.math.MathX;
import com.lusidity.framework.text.StringX;

import java.io.File;

@SuppressWarnings("unused")
public class KeyDataHandlerFileData implements IKeyDataHandler
{

	// Overrides
	@Override
	public KeyDataTransformed handleSetterBefore(Object newValue, KeyData keyData)
	{
		Object result=newValue;
		if (result instanceof String)
		{
			result=new File((String) result);
		}
		return new KeyDataTransformed(result, true);
	}

	@Override
	public void
	handleSetterAfter(Object value, KeyData keyData)
	{
		if (value instanceof File)
		{
			File file=(File) value;
			keyData.getVertex().getVertexData().update("kb", this.getFileInKb(file));
			keyData.getVertex().getVertexData().update("mb", this.getFileInMb(file));
			keyData.getVertex().getVertexData().update("bytes", this.getBytes(file));
			keyData.getVertex().getVertexData().update("path", file.getAbsolutePath());
			String ext=StringX.getLast(file.getName(), ".");
			keyData.getVertex().getVertexData().update("extension", ext);
		}
	}

	public long getFileInKb(File file)
	{
		return (this.getBytes(file)/MathX.BYTE_FACTOR);
	}

	public long getFileInMb(File file)
	{
		return (this.getFileInKb(file)/MathX.BYTE_FACTOR);
	}

	public long getBytes(File file)
	{
		return (file.exists()) ? file.length() : 0;
	}

	@Override
	public KeyDataTransformed handleGetterAfter(Object value, KeyData keyData)
	{
		return new KeyDataTransformed(value, true);
	}

	@Override
	public KeyDataTransformed getDefaultValue(Object value, KeyData keyData)
	{
		return new KeyDataTransformed(value, true);
	}
}
