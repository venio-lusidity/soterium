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

/**
 * This class expects the property below to be included.
 private KeyData<Class> clsType = null;
 */
public class KeyDataObjectEncoderHandler implements IKeyDataHandler
{
	@Override
	public KeyDataTransformed handleSetterBefore(Object newValue, KeyData keyData)
	{
		String sValue = null;
		Object value = newValue;
		String clsType = null;
		if(newValue instanceof Class){
			Class cls = (Class)newValue;
			value = cls.getName();
			clsType = Class.class.getName();
		}
		else if(null!=newValue){
			clsType = newValue.getClass().getName();
		}

		keyData.getVertex().getVertexData().update("clsType", clsType);

		if(null!=value){
			sValue =StringX.encode(value.toString());
		}
		return new KeyDataTransformed(sValue, true);
	}

	@Override
	public void handleSetterAfter(Object value, KeyData keyData)
	{

	}

	@Override
	public KeyDataTransformed handleGetterAfter(Object value, KeyData keyData)
	{
		String result = null;
		if(value instanceof String){
			result = StringX.decode(value.toString());
		}
		return new KeyDataTransformed(result, true);
	}

	@Override
	public KeyDataTransformed getDefaultValue(Object value, KeyData keyData)
	{
		return new KeyDataTransformed(value, true);
	}
}
