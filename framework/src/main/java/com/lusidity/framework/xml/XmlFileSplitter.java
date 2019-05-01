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

import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.system.FileX;
import com.lusidity.framework.text.StringX;

import java.io.*;

@SuppressWarnings({
	"NestedAssignment",
	"TooBroadScope"
	,
	"ThrowCaughtLocally"
})
public
class XmlFileSplitter
{

	private final XmlFileItem xmlFileItem;
	private long maxSizeInKb = 10000;
	private int batchSize = 10000;

	// Constructors
	public XmlFileSplitter(XmlFileItem xmlFileItem, long maxSizeInKb, int batchSize){
		super();
		this.xmlFileItem = xmlFileItem;
		this.maxSizeInKb = maxSizeInKb;
		this.batchSize = batchSize;
	}

	public boolean split(boolean deleteOriginal)
	{
		boolean result=!this.getXmlFileItem().isMaxed(this.maxSizeInKb);
		try
		{
			if (!result)
			{
				result=this.getXmlFileItem().isValid();
				if (result)
				{
					String name=this.getXmlFileItem().getFile().getName();
					String workingName=String.format("%s_working.xml", StringX.replace(name, ".xml", "").replace(".XML", ""));

					// We must repair the document so each element is on one line.
					File working=new File(this.getXmlFileItem().getFile().getParent(), workingName);
					if (working.exists())
					{
						FileX.deleteRecursively(working);
					}
					working.createNewFile();
					try (BufferedWriter writer=new BufferedWriter(new FileWriter(working, true)))
					{
						try (FileInputStream fis=new FileInputStream(this.getXmlFileItem().getFile()))
						{
							try (BufferedReader br=new BufferedReader(new InputStreamReader(fis)))
							{
								String line;
								String complete="";
								while ((line=br.readLine())!=null)
								{
									String value=line;
									if (!StringX.isBlank(value))
									{
										value=value.trim();
										if (StringX.startsWith(value, "<") && StringX.endsWith(value, ">"))
										{
											writer.newLine();
											writer.append(value);
										}
										else if (StringX.isBlank(complete) && !StringX.startsWith(value, "<"))
										{
											writer.newLine();
											writer.append(value);
										}
										else if (StringX.equals(value, "<!--") && StringX.equals(value, "-->"))
										{
											writer.newLine();
											writer.append(value);
										}
										else if (StringX.equals(value, "<!--"))
										{
											writer.newLine();
											writer.append(value);
										}
										else if (StringX.equals(value, "-->"))
										{
											writer.newLine();
											writer.append(value);
										}
										else if (StringX.startsWith(value, "<"))
										{
											complete=String.format("%s%s", complete, line);
										}
										else if (!StringX.isBlank(complete) && StringX.endsWith(value, ">"))
										{
											complete=String.format("%s%s", complete, line);
											writer.newLine();
											writer.append(complete);
											complete="";
										}
										else if (!StringX.isBlank(complete))
										{
											complete=String.format("%s %s", complete, line);
										}
										else
										{
											ReportHandler.getInstance().info(value);
										}
									}
								}
							}
							catch (Exception ex)
							{
								throw new ApplicationException(ex);
							}
						}
						catch (Exception ex)
						{
							throw new ApplicationException(ex);
						}
					}

					String top="";
					String bottom="";
					boolean stop=false;
					boolean last=false;

					// This will get the top and bottom of the xml document.
					try (FileInputStream fis=new FileInputStream(working))
					{
						try (BufferedReader br=new BufferedReader(new InputStreamReader(fis)))
						{
							String line;
							while ((line=br.readLine())!=null)
							{
								if (!StringX.isBlank(line))
								{
									if (!stop)
									{
										top=String.format("%s%s%s", top, top.isEmpty() ? "" : System.lineSeparator(), line);
										stop=this.isStart(line, this.getXmlFileItem().getGroupNamespace());
									}
									else if (last || this.isEnd(line, this.getXmlFileItem().getGroupNamespace()))
									{
										last=true;
										bottom=String.format("%s%s%s", bottom, bottom.isEmpty() ? "" : System.lineSeparator(), line);
									}
								}
							}
						}
						catch (Exception ex)
						{
							throw new ApplicationException(ex);
						}
					}
					catch (Exception ex)
					{
						throw new ApplicationException(ex);
					}

					int batchOn=1;
					int on=0;
					boolean found=false;
					StringBuilder middle=new StringBuilder();
					//Now get the middle section in badges and save to parent directory of original xml file.
					try (FileInputStream fis=new FileInputStream(working))
					{
						try (BufferedReader br=new BufferedReader(new InputStreamReader(fis)))
						{
							String line;
							while ((line=br.readLine())!=null)
							{
								if (!found)
								{
									found=this.isStart(line, this.getXmlFileItem().getGroupNamespace());
									if (found)
									{
										middle=new StringBuilder();
										continue;
									}
								}
								else if (this.isEnd(line, this.getXmlFileItem().getGroupNamespace()))
								{
									break;
								}

								if (found)
								{
									if (on>=this.batchSize)
									{
										boolean failed=!this.makeFile(top, middle.toString(), bottom, batchOn);
										if (failed)
										{
											throw new ApplicationException("The file was not split");
										}
										batchOn++;
										on=0;
										middle=new StringBuilder();
									}
									middle.append((middle.length()>0) ? System.lineSeparator() : "").append(line);
									if (this.isEnd(line, this.getXmlFileItem().getItemNamespace()))
									{
										on++;
									}
								}
							}
							//noinspection ConstantConditions
							if ((null!=middle) && (middle.length()>0))
							{
								this.makeFile(top, middle.toString(), bottom, batchOn);
							}
						}
						catch (Exception ex)
						{
							throw new ApplicationException(ex);
						}
					}
					catch (Exception ex)
					{
						throw new ApplicationException(ex);
					}

					FileX.deleteRecursively(working);
					if (deleteOriginal)
					{
						FileX.deleteRecursively(this.getXmlFileItem().getFile());
					}
				}
			}
		}
		catch (Exception ex)
		{
			result=false;
			ReportHandler.getInstance().severe(ex);
		}

		return result;
	}

	public XmlFileItem getXmlFileItem()
	{
		return this.xmlFileItem;
	}

	public boolean isStart(String line, String namespace){
		boolean result = false;

		if(!StringX.isBlank(line)){
			line = StringX.replace(line, " ", "");
			result = StringX.startsWith(line, String.format("<%s>", namespace));
		}

		return result;
	}

	public boolean isEnd(String line, String namespace){
		boolean result = false;

		if(!StringX.isBlank(line)){
			line = StringX.replace(line, " ", "");
			String end = String.format("</%s>", namespace);
			result =StringX.startsWith(line, end) || StringX.endsWith(line, end);
		}

		return result;
	}

	private boolean makeFile(String top, String middle, String bottom, int batchOn)
	{
		boolean result=true;
		try
		{
			String xml=String.format("%s%s%s%s%s", top, System.lineSeparator(), middle, System.lineSeparator(), bottom);
			String name=this.getXmlFileItem().getFile().getName();
			String workingName=String.format("%s_working.xml", StringX.replace(name, ".xml", "").replace(".XML", ""));
			String fileName=String.format("%s_split_%d.xml", workingName, batchOn);
			File file=new File(this.getXmlFileItem().getFile().getParent(), fileName);
			FileX.write(file, xml);
		}
		catch (Exception ex)
		{
			result=false;
			ReportHandler.getInstance().severe(ex);
		}

		return result;
	}
}
