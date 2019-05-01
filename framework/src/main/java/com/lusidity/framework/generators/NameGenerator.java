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

package com.lusidity.framework.generators;

import com.lusidity.framework.system.FileX;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NameGenerator
{
	public List<Integer> used = new ArrayList<>();
	public List<String> names = new ArrayList<>();

	// Constructors
	public NameGenerator(){
		super();
		this.load();
	}

	private void load()
	{
		try
		{
			URL url=this.getClass().getClassLoader().getResource("firstName.txt");
			if (null!=url)
			{
				File file=new File(url.getFile());
				if (file.exists())
				{
					FileX.readLines(file, new NameLineHandler(this.names));
				}
			}
		}
		catch (Exception ignored){}
	}

	// Getters and setters
	private String getRandomName(){
		boolean found = false;
		Random rnd = new Random();
		int len = this.names.size();
		if(this.used.size()>=len){
			this.used = new ArrayList<>();
		}

		String result = null;
		while(!found){
			int index = rnd.nextInt(len);
			if(!this.used.contains(index)){
				result = this.names.get(index);
				this.used.add(index);
				found = true;
			}
		}
		return result;
	}
}
