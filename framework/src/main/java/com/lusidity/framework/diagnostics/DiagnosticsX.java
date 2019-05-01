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

package com.lusidity.framework.diagnostics;

public
class DiagnosticsX
{
	private static int stackSize;

	/**
	 * Get a stack entry as a human-readable string.
	 * @param depth Stack depth (0 for top of stack from the caller's perspective).
	 * @return Human-readable stack entry.
	 */
	public static
	String formatStack(int depth)
	{
		StackTraceElement[] elements=Thread.currentThread().getStackTrace();
		StackTraceElement element=elements[depth];

		return DiagnosticsX.format(element);
	}

	/**
	 * Format a stack trace element as a human-readable string.
	 * @param element Stack trace element.
	 * @return Human-readable stack entry.
	 */
	public static
	String format(StackTraceElement element)
	{
		return String.format("%s.%s (%s:%d)", element.getClassName(), element.getMethodName(), element.getFileName()
				, element.getLineNumber());
	}

	public static int getStackSize() {
		StackTraceElement[] elements=Thread.currentThread().getStackTrace();
		return elements.length;
	}
}
