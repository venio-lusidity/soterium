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

package com.lusidity.data;

import com.lusidity.Environment;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.data.interfaces.data.query.IQueryFactory;
import com.lusidity.data.interfaces.data.query.IQueryResult;
import com.lusidity.data.interfaces.data.query.QueryResults;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.object.Endpoint;
import com.lusidity.framework.data.Common;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.text.StringX;
import com.lusidity.helper.PropertyHelper;
import com.lusidity.process.BaseProgressHandler;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;

public class MergeVertices extends BaseProgressHandler
{
	private final DataVertex from;
	private final DataVertex to;
	private final boolean delete;
	private boolean merged=false;

	// Constructors
	public MergeVertices(DataVertex from, DataVertex to, int maxThreads, int maxItems, boolean delete)
	{
		super(maxThreads, maxItems);
		this.from=from;
		this.to=to;
		this.delete=delete;
	}

	// Overrides
	@Override
	public File writeExceptionReport()
	{
		return null;
	}

	@Override
	public void start()
	{
		try
		{
			if (!(this.from.fetchId().getValue().equals(this.to.fetchId().getValue())))
			{
				// Load all lazy load fields.
				this.to.build();
				this.from.build();

				this.moveProperties();
				this.moveEdges();
				if (this.delete && this.from.delete())
				{
					this.to.save();
					this.merged=true;
				}
				else
				{
					this.to.save();
					this.merged=true;
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

	@Override
	protected void done()
	{

	}

	@Override
	public String getStatusText()
	{
		return null;
	}

	@SuppressWarnings("OverlyComplexMethod")
	private void moveProperties()
	{
		try
		{
			Collection<Field> fromFields=PropertyHelper.getAllFields(this.from.getClass());
			Collection<Field> toFields=PropertyHelper.getAllFields(this.to.getClass());
			boolean save=false;
			for (Field fromField : fromFields)
			{
				try
				{
					if (PropertyHelper.isUsable(fromField))
					{
						for (Field toField : toFields)
						{
							if ((null!=toField) && fromField.getType().equals(toField.getType())
							    && StringX.equals(fromField.getName(), toField.getName()))
							{
								boolean updated=this.updateField(fromField, toField);
								if (updated)
								{
									save=true;
								}
								break;
							}
						}
					}
				}
				catch (Exception ignored)
				{
				}
			}
			if (save)
			{
				this.to.save();
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
	}

	@SuppressWarnings("OverlyStrongTypeCast")
	private boolean updateField(Field fromField, Field toField)
	{
		boolean result=false;
		try
		{
			fromField.setAccessible(true);
			toField.setAccessible(true);
			Object fromValue=fromField.get(this.from);
			Object toValue=toField.get(this.to);
			Object value=null;
			if (!Objects.equals(fromValue, null))
			{
				if (ClassX.isKindOf(fromField.getType(), KeyDataCollection.class))
				{
					KeyDataCollection fromWorking=(KeyDataCollection) fromValue;
					KeyDataCollection toWorking=(KeyDataCollection) toValue;
					if ((null!=toWorking) && (null!=fromValue) && !fromWorking.isEmpty())
					{
						for (Object obj : fromWorking)
						{
							@SuppressWarnings("unchecked")
							boolean added=toWorking.add(obj);
							if (added)
							{
								result=true;
							}
						}
					}
				}
				else if ((null==toValue) && (null!=fromValue))
				{
					value=fromValue;
					result=true;
				}

				if (result)
				{
					if (!Objects.equals(value, null))
					{
						toField.set(this.to, value);
					}
					else
					{
						result=false;
					}
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}

		return result;
	}

	private boolean moveEdges()
	{
		boolean result=false;
		IQueryFactory factory=Environment.getInstance().getQueryFactory();
		QueryResults queryResults=factory.getAllProperties(this.from, 0, 1000);
		if ((null!=queryResults) && !queryResults.isEmpty())
		{
			this.getProcessStatus().fetchTotal().getValue().add(queryResults.size());
			int len=queryResults.size();
			for (IQueryResult queryResult : queryResults)
			{
				try
				{
					Edge edge=queryResult.getVertex();
					if ((null!=edge) && !queryResult.isDeleted())
					{
						boolean isFrom=edge.isFrom(this.from.fetchId().getValue());
						Endpoint other=edge.getOther(this.from.fetchId().getValue());
						Edge test=this.from.getEdgeHelper().getEdge(edge.getClass(), other.getVertex(), edge.fetchLabel().getValue(), isFrom ? Common.Direction.OUT : Common.Direction.IN);
						if (null!=test)
						{
							Endpoint toEndpoint=edge.getEndpoint(this.from.fetchId().getValue());
							toEndpoint.setVertex(this.to);
							edge.save();
							len--;
						}
					}
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().severe(ex);
				}
				this.getProcessStatus().fetchProcessed().getValue().increment();
			}
			if (len==0)
			{
				// move all edges until there is nothing related to the this object.
				result=this.moveEdges();
			}
			else
			{
				Environment.getInstance().getReportHandler().severe("An edge could not be moved, %s", this.from.fetchId().getValue());
			}
		}
		else
		{
			result=true;
		}
		return result;
	}

	// Getters and setters
	public <T extends DataVertex>
	T getMerged()
	{
		//noinspection unchecked
		return (this.merged) ? (T) this.to : null;
	}
}
