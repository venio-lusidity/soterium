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
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.sun.mail.smtp.SMTPSSLTransport;
import com.sun.mail.smtp.SMTPTransport;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.Socket;
import java.util.*;

@SuppressWarnings("unused")
public class EmailX
{
	private static final String PLAINTEXT_FORMAT="%n%s%s:  %s";
	private static final String PLAINTEXT_SPACER="     ";
	private static String defaultSubject = null;

	private EmailX()
	{
		super();
	}

	// Methods
	public static String getDefaultSubject()
	{
		String serverKey=EmailX.getDefaultServerKey();
		EmailConfiguration emailConfiguration=EmailConfiguration.getInstance();
		EmailServer server=(EmailServer) CollectionUtils.get(emailConfiguration.getServers(serverKey), 0);
		return server.getDefaultSubject();
	}

	public static String getDefaultServerKey()
	{
		String result="_default";
		Object o=EmailConfiguration.getInstance().getProperty("server_key");
		if (o instanceof String)
		{
			result=o.toString();
		}
		return result;
	}

	public static String[] getSystemAdmins()
	{
		String serverKey=EmailX.getDefaultServerKey();
		EmailConfiguration emailConfiguration=EmailConfiguration.getInstance();
		EmailServer server=(EmailServer) CollectionUtils.get(emailConfiguration.getServers(serverKey), 0);
		return server.getSystemAdmins().split(";");
	}

	/*
		Convert a JSONObject to a plain text to use in a body of an email.
	 */
	public static String toPlainTextFormat(JsonData data, int level)
	{
		StringBuilder results=new StringBuilder();

		try
		{
			@SuppressWarnings("unchecked")
			StringBuilder tabs=new StringBuilder();
			for (int i=0; i<level; i++)
			{
				tabs.append("\t");
			}

			String[] commons={
				"subject",
				"feedback",
				"who",
				"email",
				"phone",
				"url",
				"description"
			};
			List<String> keys=new ArrayList<>();
			//noinspection CollectionAddAllCanBeReplacedWithConstructor
			keys.addAll(data.keys());
			keys.sort(String::compareTo);
			List<String> fKeys=new ArrayList<>();
			for (String common : commons)
			{
				keys.remove(common);
				fKeys.add(common);
			}
			for (String key : keys)
			{
				fKeys.add(key);
			}
			for (String key : fKeys)
			{
				Object object=data.getObjectFromPath(key);
				if (object instanceof JsonData)
				{
					JsonData item=(JsonData) object;
					if (item.isJSONArray())
					{
						results.append(String.format(EmailX.PLAINTEXT_FORMAT, tabs.toString(), key, ""));
						for (Object o : item)
						{
							if (o instanceof JSONObject)
							{
								results.append(String.format(EmailX.toPlainTextFormat(new JsonData(o), level+1)));
							}
						}
					}
					else if (item.isJSONObject())
					{
						results.append(String.format(EmailX.PLAINTEXT_FORMAT, tabs.toString(), key, ""));
						results.append(EmailX.toPlainTextFormat(item, level+1));
					}
				}
				else if (null!=object)
				{
					results.append(String.format(EmailX.PLAINTEXT_FORMAT, tabs.toString(), key, object.toString()));
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}

		return results.toString();
	}

	public static void sendMail(String defaultServerKey, String subject, String body)
	{
		EmailX.sendMail(defaultServerKey, EmailX.getDefaultFrom(), EmailX.getDefaultTo(), null, null, subject, body, true, null);
	}

	@SuppressWarnings({
		"OverlyLongMethod",
		"MagicNumber",
		"UnusedParameters"
	})
	public static boolean sendMail(String serverKey, String from,
	                               String[] recipients,
	                               String[] ccTo,
	                               String[] replyTo,
	                               String subject,
	                               String body,
	                               boolean isPlainText,
	                               Collection<byte[]> attachments)
	{
		boolean result=false;

		try
		{
			Collection<EmailServer> servers=EmailConfiguration.getInstance().getServers(serverKey);
			if (!servers.isEmpty())
			{
				for (EmailServer server : servers)
				{
					try
					{
						result=EmailX.tryServer(server, from, recipients, ccTo, replyTo, subject, body, isPlainText, attachments);
						if (result)
						{
							break;
						}
					}
					catch (Exception ex)
					{
						Environment.getInstance().getReportHandler().severe(ex);
					}
				}
			}
			else
			{
				Environment.getInstance().getReportHandler().severe("The email servers are empty, cannot requested email.");
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
			result=false;
		}

		return result;
	}

	public static String getDefaultFrom()
	{
		String serverKey=EmailX.getDefaultServerKey();
		EmailConfiguration emailConfiguration=EmailConfiguration.getInstance();
		EmailServer server=(EmailServer) CollectionUtils.get(emailConfiguration.getServers(serverKey), 0);
		return server.getDefaultFrom();
	}

	public static String[] getDefaultTo()
	{
		String serverKey=EmailX.getDefaultServerKey();
		EmailConfiguration emailConfiguration=EmailConfiguration.getInstance();
		EmailServer server=(EmailServer) CollectionUtils.get(emailConfiguration.getServers(serverKey), 0);
		return server.getDefaultTo().split(";");
	}

	private static boolean tryServer(EmailServer server, String from,
	                                 String[] recipients,
	                                 String[] ccTo,
	                                 String[] replyTo,
	                                 String subject,
	                                 String body,
	                                 boolean isPlainText,
	                                 Collection<byte[]> attachments)
	{
		boolean result=false;
		if (!Environment.getInstance().getConfig().isTesting() && !EmailConfiguration.getInstance().isDisabled())
		{
			try
			{
				Properties props=server.getProperties();
				Session session=Session.getDefaultInstance(props);
				session.setDebug(false);

				MimeMessage msg=new MimeMessage(session);

				if (!StringX.isBlank(from))
				{
					InternetAddress addressFrom=new InternetAddress(from);
					msg.setFrom(addressFrom);
				}
				else
				{
					throw new ApplicationException("from is a required parameter");
				}

				if ((null!=replyTo) && (replyTo.length>0))
				{
					InternetAddress[] addresses=new InternetAddress[replyTo.length];
					int on=0;
					for (String email : replyTo)
					{
						InternetAddress internetAddress=new InternetAddress(email);
						addresses[on]=internetAddress;
						on++;
					}
					msg.setReplyTo(addresses);
				}

				if ((null!=ccTo) && (ccTo.length>0))
				{
					InternetAddress[] addresses=new InternetAddress[ccTo.length];
					int on=0;
					for (String email : ccTo)
					{
						InternetAddress internetAddress=new InternetAddress(email);
						addresses[on]=internetAddress;
						on++;
					}
					msg.addRecipients(Message.RecipientType.CC, addresses);
				}

				if ((null!=recipients) && (recipients.length>0))
				{
					InternetAddress[] addresses=new InternetAddress[recipients.length];
					int on=0;
					for (String email : recipients)
					{
						InternetAddress internetAddress=new InternetAddress(email);
						addresses[on]=internetAddress;
						on++;
					}
					msg.addRecipients(Message.RecipientType.TO, addresses);
				}
				else
				{
					throw new ApplicationException("recipients is a required parameter.");
				}

				if (!StringX.isBlank(subject))
				{
					msg.setSubject(subject);
				}
				else
				{
					throw new ApplicationException("subject is a required parameter.");
				}

				if (!StringX.isBlank(body))
				{
					body = StringX.replace(body, "[new line]", "\\r\\n");
					if (isPlainText)
					{
						msg.setContent(body, "text/plain");
					}
					else
					{
						msg.setContent(body, "text/html");
					}
				}
				else
				{
					throw new ApplicationException("body is a required parameter.");
				}

				SMTPTransport transport;
				if(server.isTLS()){

					SMTPSSLTransport tls = (SMTPSSLTransport) session.getTransport("smtps");
					transport = tls;
				}
				else
				{
					transport=(SMTPTransport) session.getTransport();
				}

				try
				{
					transport.setLocalHost(server.getReferer());
					if (StringX.isAnyBlank(server.getUserName(), server.getPassword()))
					{
						Socket socket=new Socket(server.getHost(), server.getPort());
						transport.connect(socket);
					}
					else
					{
						transport.connect(server.getHost(), server.getPort(), server.getUserName(), server.getPassword());
					}

					transport.sendMessage(msg, msg.getAllRecipients());
					result=true;
				}
				catch (Exception ex)
				{
					Environment.getInstance().getReportHandler().severe(ex);
					result=false;
				}
				finally
				{
					if (null!=transport)
					{
						transport.close();
					}
				}
			}
			catch (Exception ex)
			{
				Environment.getInstance().getReportHandler().severe(ex);
				result=false;
			}
		}
		return result;
	}

	@SuppressWarnings("unused")
	private static String enumerateChildren(JSONObject jsonObject, int deep)
	{
		StringBuilder results=new StringBuilder();

		try
		{
			@SuppressWarnings("unchecked")
			Iterator<String> keys=jsonObject.keys();

			StringBuilder spacer=new StringBuilder();

			for (int i=0; i<deep; i++)
			{
				spacer.append(EmailX.PLAINTEXT_SPACER);
			}

			while (keys.hasNext())
			{
				String key=keys.next();
				if (jsonObject.get(key).getClass().equals(JSONObject.class))
				{
					results.append(String.format(EmailX.PLAINTEXT_FORMAT, String.format("%s%s", spacer.toString(), key),
						String.format("%n%s",EmailX.enumerateChildren(jsonObject.getJSONObject(key), (deep+1)))
					));
					results.append('\n');
				}
				else if (jsonObject.get(key).getClass().equals(JSONArray.class))
				{
					JSONArray jsonArray=jsonObject.getJSONArray(key);
					results.append(String.format(EmailX.PLAINTEXT_FORMAT, String.format("%s%s", spacer.toString(), key), ""));
					int len=jsonArray.length();
					for (int i=0; i<len; i++)
					{
						results.append(String.format(EmailX.PLAINTEXT_FORMAT, String.format("%s%s", spacer.toString(), key),
							String.format("%n%s", EmailX.enumerateChildren(jsonArray.getJSONObject(i), (deep+1)))
						));
					}
					results.append('\n');
				}
				else
				{
					String value=jsonObject.getString(key);
					if (!StringX.isBlank(value))
					{
						results.append(String.format(EmailX.PLAINTEXT_FORMAT, String.format("%s%s", spacer.toString(), key), jsonObject.getString(key)));
						results.append('\n');
					}
				}
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}

		return results.toString();
	}
}
