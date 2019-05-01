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

package com.lusidity.data.types.interfaces;

public interface Convertible
{
	/**
	 * Load this instance object with values converted from another object. The other object can be of any type supported by the implementing
	 * class. IMPORTANT: A class implementing this interface MUST provide a public default constructor to allow an instance to be created.
	 *
	 * @param srcValue Object to convert.
	 */
	void convertFrom(Object srcValue);

	/**
	 * Convert this instance object to another class. The supported target classes depend on the implementing class.
	 *
	 * @param cls Class to which to convert.
	 * @return Converted object of the specified class.
	 */
	Object convertTo(Class<?> cls)
		throws ClassCastException;

	/**
	 * Can this instance object be converted from an object of the specified class?
	 *
	 * @param cls Class to check.
	 * @return true if conversion is supported, otherwise false.
	 */
	boolean canConvertFrom(Class<?> cls);

	/**
	 * Can this instance object be converted to the specified class?
	 *
	 * @param cls Class to check.
	 * @return true if conversion is supported, otherwise false.
	 */
	boolean canConvertTo(Class<?> cls);
}
