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

package com.lusidity.data.field;

import com.lusidity.Environment;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.java.ObjectX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.text.StringX;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * A class used to make fields that use a JSON key value pair as the underlying data, managing a single value.
 */
@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
public class KeyData<T>
{
	protected String keyName=null;
	private Class<?> fieldType=null;
	private boolean discoverable=false;
	private Object defaultValue=null;
	private T underlying=null;
	private Collection<IKeyDataHandler> callbacks=new ArrayList<>();
	private DataVertex vertex=null;
	private boolean loaded = false;
	private boolean hasdefaultValue = false;

	// Constructors
	@SuppressWarnings("ConstructorWithTooManyParameters")
	public KeyData(DataVertex vertex, String keyName, Class<?> fieldType, boolean discoverable, Object defaultValue, IKeyDataHandler... fieldCallbacks)
	{
		super();
		this.load(true, vertex, keyName, fieldType, discoverable, defaultValue, fieldCallbacks);
	}

	@SuppressWarnings("ConstructorWithTooManyParameters")
	public KeyData(boolean load, DataVertex vertex, String keyName, Class<?> fieldType, boolean discoverable, Object defaultValue, IKeyDataHandler... fieldCallbacks)
	{
		super();
		this.load(load, vertex, keyName, fieldType, discoverable, defaultValue, fieldCallbacks);
	}

	// Overrides
	@Override
	public boolean equals(Object obj)
	{
		boolean result=false;
		if ((obj instanceof KeyData) && !this.isNullOrEmpty())
		{
			KeyData other=(KeyData) obj;
			result=Objects.equals(this.getValue(), other.getValue());
		}
		else if (null!=obj)
		{
			result=Objects.equals(this.getValue(), obj);
		}
		return result;
	}

	/**
	 * The underlying value as a string.
	 *
	 * @return The underlying value as a string.
	 */
	@Override
	public String toString()
	{
		String result="";
		if (null!=this.getValue())
		{
			try
			{
				result=this.getValue().toString();
			}
			catch (Exception ignored)
			{
			}
		}
		return result;
	}

	/**
	 * The underlying value.
	 * Makes calls to getBeforeFieldCallback and getFieldCallback.
	 *
	 * @return The underlying value.
	 */
	public T getValue()
	{
		T result=null;
		boolean authorized=true;
		try
		{
			result=this.underlying;

			int len=this.callbacks.size();
			for (int i=0; i<len; i++)
			{
				IKeyDataHandler callback=(IKeyDataHandler) CollectionUtils.get(this.callbacks, i);
				if (null!=callback)
				{
					KeyDataTransformed transformed=callback.handleGetterAfter(result, this);
					Object value=transformed.getValue();
					if (ClassX.isKindOf(value, this.getFieldType()))
					{
						//noinspection unchecked
						result=(T) value;
					}
					authorized=transformed.isAuthorized();
					if (!authorized)
					{
						break;
					}
				}
			}
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().warning(ex);
		}

		//noinspection unchecked
		return authorized ? result : null;
	}

	/**
	 * The expected Object type.
	 *
	 * @return The expected Object type.
	 */
	public Class<?> getFieldType()
	{
		return this.fieldType;
	}

	/**
	 * Sets the value of the underlying object.
	 * Makes calls to setBeforeFieldCallback and setAfterFieldCallback.
	 *
	 * @param value The value of the field.
	 */
	public synchronized void setValue(T value)
	{
		try
		{
			if ((null==value) || (ClassX.isKindOf(value, this.getFieldType()) && ObjectX.isDifferent(this.underlying, value)))
			{
				T fValue=value;
				int len=this.callbacks.size();
				boolean authorized=true;
				for (int i=0; i<len; i++)
				{
					IKeyDataHandler callback=(IKeyDataHandler) CollectionUtils.get(this.callbacks, i);
					if (null!=callback)
					{
						KeyDataTransformed transform=callback.handleSetterBefore(fValue, this);
						authorized=transform.isAuthorized();
						if (!authorized)
						{
							break;
						}
						Object tValue=transform.getValue();
						if ((null==tValue) ||
						    ClassX.isKindOf(tValue, this.getFieldType()))
						{
							//noinspection unchecked
							fValue=(T) tValue;
						}
					}
				}

				boolean isEmptyEndpoint=(ClassX.isKindOf(this.fieldType, Endpoint.class) && (null==fValue));
				try
				{
					if (!isEmptyEndpoint)
					{
						this.setUnderlying(fValue);
					}
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().info(ex);
				}

				//noinspection ConstantConditions
				if (authorized)
				{
					for (int i=0; i<len; i++)
					{
						IKeyDataHandler callback=(IKeyDataHandler) CollectionUtils.get(this.callbacks, i);
						if (null!=callback)
						{
							callback.handleSetterAfter(fValue, this);
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			if (Environment.getInstance().getConfig().isDevOnly())
			{
				ReportHandler.getInstance().warning(ex);
			}
		}
	}

	/**
	 * This can be used to add additional handlers so that a subclass can modify a property belonging to a superclass.
	 * Lets say you want the title to be all upper case which is not the default behavior of the super class.
	 * You could call the below method fetchTitle.addHandler(new FiToUpperCaseHandler()) or create another custom handler.
	 * Remember all handlers are executed in the order added.
	 *
	 * @param callback An IkeyDataCallBack that will continue a custom process on this object.
	 */
	public void addHandler(IKeyDataHandler callback)
	{
		this.callbacks.add(callback);
	}

	private synchronized void load(boolean load, DataVertex vertex, String keyName, Class<?> fieldType, boolean discoverable, Object defaultValue, IKeyDataHandler... fieldCallbacks)
	{
		if(!this.isLoaded())
		{
			try
			{
				this.vertex=vertex;
				//noinspection unchecked
				this.keyName=ClassX.isKindOf(fieldType, DataVertex.class) ? ClassHelper.getPropertyKey((Class<? extends DataVertex>) fieldType, keyName) : keyName;
				this.fieldType=fieldType;
				this.discoverable=discoverable;
				this.defaultValue=Common.getTypeFor(defaultValue, this.fieldType);
				if (null!=fieldCallbacks)
				{
					//noinspection ManualArrayToCollectionCopy
					for (IKeyDataHandler fieldCallback : fieldCallbacks)
					{
						this.callbacks.add(fieldCallback);
					}
				}
				if (load)
				{
					this.load();
				}
			}
			catch (Exception ex){
				Environment.getInstance().getReportHandler().warning(ex);
			}
		}
		this.loaded = true;
	}

	@SuppressWarnings("OverlyComplexMethod")
	protected void load()
	{
		try
		{
			Object o=this.getVertex().getVertexData().getObjectFromPath(this.getKeyName());
			Object value=null;
			if (null!=o)
			{
				value=this.transform(o);
				// if null set default value.
			}

			if ((null==value))
			{
				this.setValue(this.getDefaultValue());
			}

			boolean authorized=true;
			int len=this.callbacks.size();
			for (int i=0; i<len; i++)
			{
				IKeyDataHandler callback=(IKeyDataHandler) CollectionUtils.get(this.callbacks, i);
				if (null!=callback)
				{
					KeyDataTransformed transformed=callback.getDefaultValue(value, this);

					authorized=transformed.isAuthorized();
					if (authorized && (null!=transformed.getValue()))
					{
						value=transformed.getValue();
					}
					if (!authorized)
					{
						break;
					}
				}
			}

			if (authorized && ClassX.isKindOf(value, this.getFieldType()))
			{
				//noinspection unchecked
				this.setUnderlying((T) value);
			}
		}
		catch (Exception ex){
			Environment.getInstance().getReportHandler().severe(ex);
		}
		this.loaded = true;
	}

	protected Object transform(Object o)
	{
		Object result = null;
		if(null!=o)
		{
			if(o instanceof JSONObject)  {
				JsonData item= JsonData.create(o);
				Class<? extends DataVertex> cls=null;
				if (item.hasKey("vertexType"))
				{
					String vertexType=item.getString("vertexType");
					cls=Environment.getInstance().getApolloVertexType(vertexType);
					if (null==cls)
					{
						cls=Environment.getInstance().getApolloVertexType(vertexType);
					}
				}
				else if (ClassX.isKindOf(this.getFieldType(), DataVertex.class))
				{
					//noinspection unchecked
					cls=(Class<? extends DataVertex>) this.getFieldType();
				}

				if (null!=cls)
				{
					result=ClassHelper.as(item, cls);
				}
			}
			else if (o instanceof JsonData)
			{
				JsonData item= (JsonData)o;
				Class<? extends DataVertex> cls=null;
				if (item.hasKey("vertexType"))
				{
					String vertexType=item.getString("vertexType");
					cls=Environment.getInstance().getApolloVertexType(vertexType);
					if (null==cls)
					{
						cls=Environment.getInstance().getApolloVertexType(vertexType);
					}
				}
				else if (ClassX.isKindOf(this.getFieldType(), DataVertex.class))
				{
					//noinspection unchecked
					cls=(Class<? extends DataVertex>) this.getFieldType();
				}

				if (null!=cls)
				{
					result=ClassHelper.as(item, cls);
				}
			}
			else
			{
				result=Common.getTypeFor(o, this.getFieldType());
			}
		}
		return result;
	}

	protected synchronized void nullifyUnderlying()
	{
		this.underlying = null;
	}

	// Getters and setters
	/**
	 * The name of the key used in the JSON object.
	 * @return The name of the key used in the JSON object.
	 */
	public String getKeyName()
	{
		return this.keyName;
	}

	/**
	 * If this value is null and the default value is set, this value will be used to set the object value.
	 * @return The default value of the field.
	 */
	public T getDefaultValue()
	{
		T result=null;
		try
		{
			//noinspection NestedConditionalExpression,unchecked
			result=(null==this.defaultValue) ? null : (T) this.defaultValue;
			hasdefaultValue = (null!=result);
		}
		catch (Exception ex)
		{
			ReportHandler.getInstance().warning(ex);
		}

		return result;
	}

	/**
	 * The underlying data of the vertex.
	 * @return The underlying data of the vertex.
	 */
	public JsonData getData()
	{
		return this.vertex.getVertexData();
	}

	public boolean isLoaded()
	{
		return this.loaded;
	}

	/**
	 * For boolean values this could mean that if the value is false or true then the value is not null therefore it would return true.
	 @return true if the value is not null or an empty string.
	 */
	public boolean isNotNullOrEmpty()
	{
		return !this.isNullOrEmpty();
	}

	/**
	 * For boolean values this could mean that if the value is false or true then the value is not null therefore it would return true.
	 @return false if the value is not null or an empty string.
	 */
	public boolean isNullOrEmpty()
	{
		boolean result=(null==this.getValue());
		if (this.fieldType.equals(String.class))
		{
			result=StringX.isBlank((String)this.getValue());
		}
		return result;
	}

	/**
	 * If the value of this is of type Boolean then it will return the value of the property.
	 * If the boolean value is null it will return false.
	 * @return If the object is a Boolean returns the actual value.
	 */
	public boolean isTrue(){
		boolean result = false;
		if((this.getValue() instanceof Boolean)){
			result =(Boolean) this.getValue();
		}
		return result;
	}

	public boolean isDiscoverable()
	{
		return this.discoverable;
	}

	public DataVertex getVertex()
	{
		return this.vertex;
	}

	protected Collection<IKeyDataHandler> getCallBacks()
	{
		return this.callbacks;
	}

	private synchronized void setUnderlying(T value)
	{
		// Do not change this without some serious testing.
		@SuppressWarnings("UnusedAssignment")
		Object fValue = value;
		if(this.isLoaded() || this.hasdefaultValue)
		{
			// Delta changes do not seem to be currently working.
			if ((null!=value) && ClassX.isKindOf(this.getFieldType(), DataVertex.class))
			{
				DataVertex item=(DataVertex) value;
				fValue=item.toJson(true);
				this.getData().update(this.getKeyName(), fValue);
				/*
				if (this.isLoaded() && this.getVertex().hasId())
				{
					this.getVertex().delta(this.getKeyName(), fValue);
				}
				*/
			}
			else if (null!=value)
			{
				this.getData().update(this.getKeyName(), value);
				/*
				if (this.isLoaded() && this.getVertex().hasId())
				{
					this.getVertex().delta(this.getKeyName(), value);
				}
				*/
			}
			else if (this.isLoaded())
			{
				this.getData().remove(this.getKeyName());
			}
		}
		this.underlying = value;
		if(this.isLoaded() && !this.getVertex().isDirty())
		{
			this.getVertex().setDirty(true);
		}
		this.loaded = true;
	}
}
