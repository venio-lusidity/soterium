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

package com.lusidity.framework.system;

import com.lusidity.framework.exceptions.ApplicationException;

import java.io.*;

public class ReadLines implements Closeable
{
	private boolean stop = false;

	// Constructors
	public ReadLines(){
		super();
	}

	// Overrides
	@Override
	public void close()
		throws IOException
	{
		this.stop();
	}

	public void stop()
	{
		this.stop=true;
	}

	public void read(File file, LineHandler lineHandler) throws ApplicationException
	{
		try (FileInputStream fis=new FileInputStream(file))
		{
			try (BufferedReader br=new BufferedReader(new InputStreamReader(fis)))
			{
				String line;
				while ((line=br.readLine())!=null)
				{
					if(this.stop){
						break;
					}
					boolean exit=lineHandler.handle(line);
					lineHandler.incrementLinesRead();
					if (exit)
					{
						break;
					}
				}
			}
			catch (Exception ex)
			{
				//noinspection ThrowCaughtLocally
				throw new ApplicationException(ex);
			}
		}
		catch (Exception ex)
		{
			throw new ApplicationException(ex);
		}
	}
}
