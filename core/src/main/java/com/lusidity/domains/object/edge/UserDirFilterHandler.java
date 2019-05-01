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

package com.lusidity.domains.object.edge;

import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.framework.system.FileX;
import com.lusidity.io.ScopedDirectory;
import com.lusidity.security.data.filters.IFilterHandler;

import java.io.File;
import java.util.Collection;
import java.util.Objects;

public class UserDirFilterHandler implements IFilterHandler
{
	private final Collection<ScopedDirectory> directories;
	private final BasePrincipal[] principals;
	private final String category;

	@SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
	public UserDirFilterHandler(String category, Collection<ScopedDirectory> directories, BasePrincipal... principals)
	{
		super();
		this.directories = directories;
		this.principals = principals;
		this.category = category;
	}

	@Override
	public void handle(IFilterHandler.Action action, BasePrincipal principal, Objects... objects)
	{
		if(null != this.directories){
			for(ScopedDirectory sd: this.directories){
				if(sd.hasCategory(this.category)){
					this.handle(sd, principal);
				}
			}
		}
	}

	private void handle(ScopedDirectory sd, BasePrincipal principal)
	{
		if(sd.getFile().exists())
		{
			if (null!=principal)
			{
				File file=new File(sd.getFile(), principal.fetchId().getValue());
				if (file.exists())
				{
					this.delete(file);
				}
			}
			else if ((null!=this.principals) || (this.principals.length==0))
			{
				for (BasePrincipal bp : this.principals)
				{
					File file=new File(sd.getFile(), bp.fetchId().getValue());
					if (file.exists())
					{
						this.delete(file);
					}
				}
			}
			else {
				this.delete(sd.getFile());
			}
		}
	}

	private void delete(File file)
	{
		boolean dir = file.isDirectory();
		FileX.deleteRecursively(file);
		if(dir){
			file.mkdirs();
		}
	}
}
