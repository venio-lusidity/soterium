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

package com.lusidity.helper;

import com.lusidity.Environment;
import com.lusidity.collections.IVertexHandler;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.domains.object.Edge;
import com.lusidity.framework.java.ClassX;

public class EdgeDeprecationHandler implements IVertexHandler
{
	private final boolean deprecated;

	// Constructors
	public EdgeDeprecationHandler(boolean deprecated)
	{
		super();
		this.deprecated = deprecated;
	}

	// Overrides
	@Override
	public boolean handle(DataVertex vertex, ProcessStatus processStatus, int on, int hits, int start, int limit)
	{
		if(ClassX.isKindOf(vertex, Edge.class)){
			Edge edge = (Edge)vertex;
			try
			{
				edge.setImmediate(false);
				edge.fetchDeprecated().setValue(this.deprecated);
				edge.save();
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().warning("Could not set deprecation of edge %s to %s. from: %s type %s to: %s type %s\r\n%s",
					edge.fetchId().getValue(),
					this.deprecated,
					edge.fetchEndpointFrom().getValue().fetchRelatedId().getValue(),
					edge.fetchEndpointFrom().getValue().fetchRelatedType().getValue(),
					edge.fetchEndpointTo().getValue().fetchRelatedId().getValue(),
					edge.fetchEndpointTo().getValue().fetchRelatedType().getValue(),
					ex.getMessage()
				);
			}
		}
		return false;
	}
}
