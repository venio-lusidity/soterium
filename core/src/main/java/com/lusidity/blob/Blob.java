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

package com.lusidity.blob;

import com.lusidity.Environment;
import com.lusidity.data.DataVertex;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.electronic.Notification;
import com.lusidity.domains.people.Person;
import com.lusidity.domains.reports.FileReportRequest;
import com.lusidity.domains.system.io.FileInfo;
import com.lusidity.domains.system.primitives.RawString;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings({
	"UtilityClassWithoutPrivateConstructor",
	"NonFinalUtilityClass"
})
public class Blob
{
	// Methods
	public static void addAndNotify(FileReportRequest request, Person person, DataVertex vertex, FileInfo fileInfo)
		throws Exception
	{
		if ((null!=fileInfo) && fileInfo.exists())
		{
			String name = fileInfo.getFile().getName();
			boolean added=person.getBlobFiles().add(fileInfo);
			if (added)
			{
				Environment.getInstance().getReportHandler()
				           .info("Creating notification, vertex: %s file name: %s requested by: %s", vertex.getUri().toString(), name,
					           request.fetchPrincipalId().getValue()
				           );
				Notification notification=new Notification();
				notification.fetchTitle().setValue(String.format("file download ready: %s", name));
				boolean saved=notification.save();
				if (saved)
				{
					Environment.getInstance().getReportHandler()
					           .info("Notification created, vertex: %s file name: %s requested by: %s", vertex.getUri().toString(), name,
						           request.fetchPrincipalId().getValue()
					           );
					notification.getReceivers().add(person);
					notification.getTargets().add(fileInfo);
					String content=String.format("The file, %s, is ready for download at...\r%n\r%n%s", fileInfo.getFile().getName(), fileInfo.getWebUrl());
					RawString description=new RawString(content);
					notification.getDescriptions().add(description);
					Collection<BasePrincipal> receivers=new ArrayList<>();
					receivers.add(person);
					notification.send(receivers, Notification.DEFAULT_TEMPLATE, BaseContactDetail.CategoryTypes.work_email, content);
				}
				else
				{
					Environment.getInstance().getReportHandler()
					           .info("Create notification failed, vertex: %s file name: %s requested by: %s", vertex.getUri().toString(),
						           name,
						           request.fetchPrincipalId().getValue()
					           );
				}
			}
			else
			{
				Environment.getInstance().getReportHandler()
				           .info("Exported file already exists for user, vertex: %s file name: %s requested by: %s", vertex.getUri().toString(),
					           name,
					           request.fetchPrincipalId().getValue()
				           );
			}
		}
		else
		{
			Environment.getInstance().getReportHandler()
			           .info("Exporting file failed for, vertex: %s frequested by: %s", vertex.getUri().toString(),
				           request.fetchPrincipalId().getValue()
			           );
		}
	}
}
