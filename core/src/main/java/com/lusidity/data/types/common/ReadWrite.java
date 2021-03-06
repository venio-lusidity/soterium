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

package com.lusidity.data.types.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Abstract base class for the name of an entity.
 */
public
abstract class ReadWrite
	implements Serializable
{
	private static final long serialVersionUID=1L;

	@SuppressWarnings("MethodMayBeStatic")
	private void readObject(ObjectInputStream inputStream)
		throws ClassNotFoundException, IOException
	{
		inputStream.defaultReadObject();
	}

	private void writeObject(ObjectOutputStream outputStream)
		throws IOException
	{
		outputStream.defaultWriteObject();
	}
}
