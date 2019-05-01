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

package com.lusidity.services.common;

import com.lusidity.Environment;
import com.lusidity.framework.text.StringX;
import org.apache.commons.io.IOUtils;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public
class DynamicFileRepresentation
	extends OutputRepresentation
{
	@SuppressWarnings("FieldMayBeFinal")
	private byte[] fileData;

	private
	DynamicFileRepresentation(MediaType mediaType, long expectedSize, byte[] fileData)
	{
		super(mediaType, expectedSize);
		this.fileData = fileData;
	}

	@Override
	public
	void write(final OutputStream outputStream)
		throws IOException
	{
		outputStream.write(this.fileData);
	}

	public static
	DynamicFileRepresentation get(final URL url, Response response)
		throws IOException
	{
		DynamicFileRepresentation result = null;
		boolean isFile = StringX.startsWith(url.toString(), "file");

		URLConnection connection = url.openConnection();
		MediaType mediaType = MediaType.valueOf(connection.getContentType());

		try
		{
			if (isFile)
			{
				//noinspection IOResourceOpenedButNotSafelyClosed
				File file=new File(StringX.replace(url.toString(), "file:/", ""));
				if (file.exists())
				{
					try (FileInputStream fis=new FileInputStream(file))
					{
						result=DynamicFileRepresentation.getRepresentation(fis, mediaType, file.length());
					}
					catch (Exception ignored)
					{
					}
				}
			}
			else
			{
				try (InputStream is=connection.getInputStream())
				{
					long length=connection.getContentLength();
					result=DynamicFileRepresentation.getRepresentation(is, mediaType, length);
				}
				catch (Exception ignored)
				{
				}
			}
			if (null!=result)
			{
				response.getHeaders().set("content-type", mediaType.toString());
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().info(ex);
		}
		return result;
	}

	private static DynamicFileRepresentation getRepresentation(InputStream inputStream, MediaType mediaType, long length)
		throws IOException
	{
		DynamicFileRepresentation result = null;
		try(BufferedInputStream bis = new BufferedInputStream(inputStream)){
			result = new DynamicFileRepresentation(mediaType, length, IOUtils.toByteArray(bis));
		}
		catch (Exception ex){
			//noinspection ProhibitedExceptionThrown
			throw ex;
		}
		return result;
	}
}