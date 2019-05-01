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
class ComparatorX
{
	/**
	 * Private default constructor; cannot instantiate this utility class.
	 */
	private ComparatorX()
	{
		super();
	}

	public static final int EQUAL=0;
	public static final int OBJECT1_GREATER=1;
	public static final int OBJECT2_GREATER=-1;
	public static final int OBJECT1_LESSER=-1;
	public static final int OBJECT2_LESSER=1;

	public static
	int compare(Object value1, Object value2)
	{
		int result = ComparatorX.OBJECT2_LESSER;

		if((null!=value1) && (null==value2)){
			result = ComparatorX.OBJECT1_GREATER;
		}
		else if((null==value1) && (null!=value2)){
			result = ComparatorX.OBJECT2_GREATER;
		}
		else if((null!=value1))
		{
			Comparable a=(Comparable) value1;
			Comparable b=(Comparable) value2;
			//noinspection unchecked
			result = a.compareTo(b);
		}
		return result;
	}
}
