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
import com.lusidity.framework.text.StringX;

public class KeyDataHandlerEncoding implements IKeyDataHandler
{

// Overrides
	@Override
	public KeyDataTransformed handleSetterBefore(Object newValue, KeyData keyData)
	{
		KeyDataTransformed result=new KeyDataTransformed(newValue, true);
		if ((newValue instanceof String) && !StringX.isBase64Encoded(((String) newValue)))
		{
			result=new KeyDataTransformed(StringX.encode((String) newValue), true);
		}
		return result;
	}

	@Override
	public void handleSetterAfter(Object value, KeyData keyData)
	{
	}

	@Override
	public KeyDataTransformed handleGetterAfter(Object value, KeyData keyData)
	{
		KeyDataTransformed result=new KeyDataTransformed(value, true);
		if ((value instanceof String) && StringX.isBase64Encoded(((String) value)))
		{
			result=new KeyDataTransformed(StringX.decode((String) value), true);
		}
		return result;
	}

	@Override
	public KeyDataTransformed getDefaultValue(Object value, KeyData keyData)
	{
		return new KeyDataTransformed(value, true);
	}
}
