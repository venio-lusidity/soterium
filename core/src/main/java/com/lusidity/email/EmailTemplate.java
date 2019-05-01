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

package com.lusidity.email;

import com.lusidity.Environment;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.system.FileX;
import com.lusidity.framework.text.StringX;

import java.io.File;
import java.net.URI;
import java.util.Objects;

@SuppressWarnings("UnusedDeclaration")
public class EmailTemplate
{
// Fields
	public static final String EMAIL_TEMPLATE=
		"<table style=\"border-collapse: collapse; width: 98%;color: #333333;font-size: 12px; font-family: Helvetica, Arial, sans-serif;\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">"+
		"<tr>"+
		"<td >"+
		"<table style=\"border-collapse: collapse; width: 620px\" cellspacing=\"0\" cellpadding=\"0\">"+
		"<tr>"+
		"<td style=\"background-color: #f2f2f2; padding: 5px 10px 5px 10px; text-align: left;\">"+
		"<a href=\"https://www.lusidity.com\" style=\"text-decoration:none;\" target=\"_blank\">"+
		"<img src=\"https://www.lusidity.com/assets/img/logos/logo32.png\" alt=\"lusidity\" style=\"height:32px;margin:2px 0 0 10px;\"/>"+
		"</a>"+
		"</td>"+
		"</tr>"+
		"</table>"+
		"<table style=\"border-collapse: collapse; width: 620px\" cellspacing=\"0\" cellpadding=\"0\" width=\"620\" border=\"0\">"+
		"<tr>"+
		"<td style=\"border: medium none;padding: 0px;\">"+
		"<table style=\"border-collapse: collapse\" cellspacing=\"0\" cellpadding=\"0\" width=\"620\">"+
		"<tr>"+
		"<td style=\"padding: 0px; width: 620px\">"+
		"<table style=\"border-collapse: collapse; width: 100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">"+
		"<tr>"+
		"<td style=\"border: medium none;padding: 20px 20px 20px 20px;\">"+
		"<table style=\"border-collapse: collapse\" cellspacing=\"0\" cellpadding=\"0\">"+
		"<tr>"+
		"[imageDisplay]"+
		"<td style=\"text-align: left; width: 100%\" valign=\"top\">"+
		"<table style=\"border-collapse: collapse; width: 100%\" cellspacing=\"0\" cellpadding=\"0\">"+
		"<tr>"+
		"<td style=\"padding-bottom: 10px\">"+
		"<table style=\"border-collapse: collapse; width: 100%\" cellspacing=\"0\" cellpadding=\"0\">"+
		"<tr>"+
		"<td style=\"padding-bottom: 5px;font-size: 14px;\"><span style=\"font-size: 14px;font-family: Helvetica, Arial, sans-serif;\">"+
		"<a href=\"mailto:[emailAddress]\" target=\"_blank\" style=\"font-size: 14px;\">[who]</a> has sent you a link to " +
		"<a href=\"[entityUrl]\" target=\"_blank\" style=\"font-size: 14px;\">[title]</a>.</span></td>"+
		"</tr>"+
		"<tr>"+
		"<td style=\"padding: 5px;font-family: Helvetica, Arial, sans-serif;font-size: 12px;\">[content]</td>"+
		"</tr>"+
		"<tr>"+
		"<td style=\"font-family: Helvetica, Arial, sans-serif;font-size: 12px;\">[description]</td>"+
		"</tr>"+
		"</table>"+
		"</td>"+
		"</tr>"+
		"</table>"+
		"</td>"+
		"</tr>"+
		"</table>"+
		"</td>"+
		"</tr>"+
		"</table>"+
		"</td>"+
		"</tr>"+
		"</table>"+
		"</td>"+
		"</tr>"+
		"</table>"+
		"<table style=\"border-collapse: collapse; width: 620px\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">"+
		"<tr>"+
		"<td style=\"font-size: 10px; border: medium none;color: #A8A8A8; padding: 20px 10px 20px 10px;font-family: Helvetica, Arial, sans-serif;\">"+
		"This message was sent on behalf of <a href=\"mailto:[emailAddress]\">[who]</a> by <a href=\"https://www.lusidity.com\">lusidty</a>."+
		"<br />"+
		"Replying to this email will send it to [who]."+
		"</td>"+
		"</tr>"+
		"</table>"+
		"</td>"+
		"</tr>"+
		"</table>";
	private JsonData data=null;

	// Constructors
	public EmailTemplate(JsonData data)
	{
		super();
		this.data=data;
	}
	private EmailTemplate()
	{
		super();
	}

// Methods
	@SuppressWarnings({
		"MethodWithTooManyParameters",
		"TypeMayBeWeakened",
		"Duplicates"
	})
	public static String getEntityMail(String who, String emailAddress, String title, String content, String description, URI entityUrl, URI imageUrl)
	{
		String originalFormat=EmailTemplate.EMAIL_TEMPLATE;

		String result=originalFormat;
		result=EmailTemplate.replace(result, "[who]", who);
		result=EmailTemplate.replace(result, "[emailAddress]", emailAddress);
		result=EmailTemplate.replace(result, "[title]", title);
		result=EmailTemplate.replace(result, "[content]", !StringX.isBlank(content) ? (who+" wrote:  "+content) : "");
		result=EmailTemplate.replace(result, "[description]", !StringX.isBlank(description) ? description : "");
		result=EmailTemplate.replace(result, "[entityUrl]", entityUrl.toString());
		if (null!=imageUrl)
		{
			result=EmailTemplate.replace(result, "[imageDisplay]", "<td style=\"text-align: left; padding-right: 15px;\" valign=\"top\">"+
			                                                       "<a style=\"COLOR: #3b5998; tex-decoration: none\" href=\"[entityUrl]\" target=\"_blank\">"+
			                                                       "<img style=\"width:96px !important;\" src=\"[imageUrl]\" alt=\"[title]\"/>"+
			                                                       "</a>"+
			                                                       "</td>");
			result=EmailTemplate.replace(result, "[imageUrl]", imageUrl.toString());
			result=EmailTemplate.replace(result, "[entityUrl", entityUrl.toString());
			result=EmailTemplate.replace(result, "[title]", title);
		}
		else
		{
			result=EmailTemplate.replace(result, "[imageDisplay]", "");
		}

		return result;
	}

	private static String replace(String text, String key, Object what)
	{
		String result=text;
		if (!StringX.isBlank(text) && (!StringX.isBlank(key) || Objects.equals(key, "\n") || Objects.equals(key, "\n")) && (null!=what))
		{
			result=StringX.replace(text, key, what.toString());
		}
		return result;
	}

	@SuppressWarnings({
		"MethodWithTooManyParameters",
		"TypeMayBeWeakened",
		"Duplicates"
		,
		"HardcodedLineSeparator"
	})
	public static String getCustom(String originalFormat, String[] who, String emailAddress, String[] initiators, String title, String content, String description, URI entityUrl, URI imageUrl)
	{
		String result=originalFormat;
		if (null!=imageUrl)
		{
			result=EmailTemplate.replace(result, "[imageDisplay]", "<div style=\"float: left;text-align: left; margin: 5px; height: 96px\" >"+
			                                                       "<a style=\"COLOR: #3b5998; tex-decoration: none\" href=\"[entityUrl]\" target=\"_blank\">"+
			                                                       "<img style=\"width:96px !important;\" src=\"[imageUrl]\" alt=\"[title]\"/>"+
			                                                       "</a>"+
			                                                       "</div>");
			result=EmailTemplate.replace(result, "[imageUrl]", imageUrl.toString());
			result=EmailTemplate.replace(result, "[entityUrl]", (null==entityUrl) ? "" : entityUrl.toString());
			result=EmailTemplate.replace(result, "[title]", title);
		}
		else
		{
			result=EmailTemplate.replace(result, "[imageDisplay]", "");
		}
		if ((null!=initiators) && (initiators.length>0))
		{
			String value="";
			for (String str : initiators)
			{
				if (!value.isEmpty())
				{
					//noinspection HardcodedLineSeparator
					value=String.format("%s%s", value, System.lineSeparator());
				}
				value=String.format("%s%s", value, str);
			}
			result=EmailTemplate.replace(result, "[initiator]", value);
		}
		if ((null!=who) && (who.length>0))
		{
			String value="";
			for (String str : who)
			{
				if (!value.isEmpty())
				{
					//noinspection HardcodedLineSeparator
					value=String.format("%s%s", value, System.lineSeparator());
				}
				value=String.format("%s%s", value, str);
			}
			result=EmailTemplate.replace(result, "[who]", value);
		}
		result=EmailTemplate.replace(result, "[emailAddress]", emailAddress);
		result=EmailTemplate.replace(result, "[title]", title);
		result=EmailTemplate.replace(result, "[content]", StringX.isBlank(content) ? "" : content);
		result=EmailTemplate.replace(result, "[description]", StringX.isBlank(description) ? "" : description);
		result=EmailTemplate.replace(result, "[entityUrl]", (null==entityUrl) ? "" : entityUrl.toString());
		result=EmailTemplate.replace(result, "\n", "[line-break]");
		result=EmailTemplate.replace(result, "\r", "[line-break]");
		result=EmailTemplate.replace(result, "[line-break]", "<br/>\r\n");
		return result;
	}

	// Getters and setters
	public String getSubject()
	{
		return this.data.getString("subject");
	}

	public String getBody()
	{
		String result = this.data.getString("body");
		if(StringX.startsWith(result, "file")){
			String path = StringX.substringAfter(result, "file://");
			if(null != path) {
				try
				{
					File file = new File(Environment.getInstance().getConfig().getResourcePath(), path);
					if(file.exists()){
						result = FileX.getAsPlainText(file);
					}
				}
				catch (Exception e)
				{
					Environment.getInstance().getReportHandler().warning(e);
				}
			}
		}
		return result;
	}
}
