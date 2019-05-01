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

package com.lusidity.domains.system.primitives;


import com.lusidity.Environment;
import com.lusidity.data.ApolloVertex;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.field.KeyData;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

@SuppressWarnings("AbstractClassWithoutAbstractMethods")
@AtSchemaClass(name="Primitive", discoverable=false)
public class Primitive extends ApolloVertex
{
	private static Collection<Class<? extends Primitive>> PRIMITIVES=null;
	private KeyData<URI> sourceUri=null;

// Constructors
	public Primitive()
	{
		super();
	}

	public Primitive(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	protected Primitive(URI sourceUri)
	{
		super();
		this.fetchSourceUri().setValue(sourceUri);
	}

	private KeyData<URI> fetchSourceUri()
	{
		if (null==this.sourceUri)
		{
			this.sourceUri=new KeyData<>(this, "sourceUri", URI.class, false, Environment.getSourceUri());
		}
		return this.sourceUri;
	}

// Methods
	public static boolean isPrimitive(DataVertex dataVertex)
	{
		boolean result=false;
		String match=dataVertex.fetchVertexType().getValue();

		if (!StringX.isBlank(match))
		{
			Collection<Class<? extends Primitive>> primitives=Primitive.getPrimitives();
			for (Class<? extends Primitive> primitive : primitives)
			{
				String elementType=ClassHelper.getClassKey(primitive);
				result=(!StringX.isBlank(elementType) && match.equals(elementType));
				if (result)
				{
					break;
				}
			}
		}

		return result;
	}

	public static synchronized Collection<Class<? extends Primitive>> getPrimitives()
	{
		if (null==Primitive.PRIMITIVES)
		{
			Primitive.PRIMITIVES=new ArrayList<>();
			Set<Class<? extends Primitive>> filtered=Environment.getInstance().getReflections().getSubTypesOf(Primitive.class);
			if ((null!=filtered) && !filtered.isEmpty())
			{
				for (Class<? extends Primitive> annotation : filtered)
				{
					//noinspection unchecked
					Primitive.PRIMITIVES.add(annotation);
				}
			}
		}
		return Primitive.PRIMITIVES;
	}
}
