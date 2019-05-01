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

package com.lusidity.framework.xml;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

/**
 * Simple pass-through namespace context.
 *
 * See http://stackoverflow.com/questions/6390339/how-to-query-xml-using-namespaces-in-java-with-xpath for
 * more information.
 */
public
class SimpleNamespaceContext implements NamespaceContext
{
	@Override
	public
	String getNamespaceURI(String prefix)
	{
		return "http://tempuri.org/"+prefix;
	}

	@Override
	public
	String getPrefix(String namespaceURI)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public
	Iterator getPrefixes(String namespaceURI)
	{
		throw new UnsupportedOperationException();
	}
}
