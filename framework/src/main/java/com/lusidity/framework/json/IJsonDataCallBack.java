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

package com.lusidity.framework.json;

public interface IJsonDataCallBack
{
	/**
	 * A callback method used to further process the data.
	 * @param lineHandler The JsonDataLineHandler.
	 * @param item The JsonData object constructed from the line.
	 * @return true or false, if set to false the reader will stop reading lines.
	 */
	boolean jdCallBack(JsonDataLineHandler lineHandler, JsonData item);
}
