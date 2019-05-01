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

package com.lusidity.framework.java;

public
class ArrayX
{
	/**
	 * Private constructor for utility class.
	 */
	private ArrayX()
	{
		super();
	}

	/**
	 * Does the array contain any instances of the specified class?
	 * @param objects Objects.
	 * @param cls Class.
	 * @return true if the array contains at least one instance of the specified class.
	 */
	public static
	boolean contains(Object[] objects, Class cls)
	{
		boolean result=false;

		for (Object object : objects)
		{
			if (ClassX.isKindOf(object, cls))
			{
				result=true;
				break;
			}
		}

		return result;
	}

	/**
	 * Does the array contain the specified class or one of its super-classes?
	 * @param classes Classes.
	 * @param cls Class to check.
	 * @return true if the array contains the specified class or one of its super-classes.
	 */
	public static boolean contains(Class[] classes, Class cls)
	{
		boolean result=false;

		for (Class c :classes)
		{
			if (ClassX.isKindOf(c, cls))
			{
				result=true;
				break;
			}
		}

		return result;
	}


	/**
	 * Get the first element in an array, or null if the array is empty or null.
	 *
	 * @param array
	 * 	Array.
	 * @param <T>
	 * 	Element type.
	 *
	 * @return First element in the array, or null if the array is empty or null.
	 */
	public static
	<T> T getFirstOrNull(T[] array)
	{
		return ArrayX.getAtOrNull(array, 0);
	}

	/**
	 * Get an array element specified by index, or null if the array is null or empty or if the index is out of
	 * bounds.
	 *
	 * @param array
	 * 	Array.
	 * @param index
	 * 	Index.
	 * @param <T>
	 * 	Element type.
	 *
	 * @return Element at the specified index, or null if the array is null or empty or if the index is out of bounds.
	 */
	public static
	<T> T getAtOrNull(T[] array, int index)
	{
		return ((array != null) && (index < array.length)) ? array[index] : null;
	}
}
