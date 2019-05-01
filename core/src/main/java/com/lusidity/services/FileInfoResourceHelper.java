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

package com.lusidity.services;

import com.lusidity.data.DataVertex;
import com.lusidity.domains.system.io.FileInfo;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.system.security.UserCredentials;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class FileInfoResourceHelper extends BaseExtendedData
{
	@Override
	public void getExtendedData(UserCredentials userCredentials, DataVertex context, JsonData result, Map<String, Object> options)
	{
		if (ClassX.isKindOf(context, FileInfo.class))
		{
			FileInfo fileInfo=(FileInfo) context;
			result.put("kb", String.format("%d kb", fileInfo.getFileInKb()));
			result.put("mb", String.format("%d mb", fileInfo.getFileInMb()));
			result.put("name", String.format("%s.%s", fileInfo.fetchTitle().getValue(), fileInfo.fetchExtension().getValue()));
			if (!result.hasKey("download"))
			{
				result.put("download", fileInfo.getWebUrl());
			}
			result.remove("path");
		}
	}

	@Override
	public Collection<Class<? extends DataVertex>> forTypes()
	{
		Collection<Class<? extends DataVertex>> result = new ArrayList<>();
		result.add(FileInfo.class);
		return result;
	}
}
