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

package com.lusidity.apollo.elasticSearch;

import com.floragunn.searchguard.ssl.SearchGuardSSLPlugin;
import com.floragunn.searchguard.ssl.util.SSLConfigConstants;
import com.lusidity.Environment;
import com.lusidity.framework.internet.http.KeyStoreManager;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.util.List;

/**
 * http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/client.html
 */
public class EsClientTransport extends BaseClient
{

	static EsClientTransport INSTANCE=null;

	// Constructors
	public EsClientTransport()
	{
		super();
	}

	// Overrides
	@Override
	public void start()
	{
		try
		{
			Settings.Builder builder=Settings.settingsBuilder()
			                                 .put("client.transport.sniff", true)
			                                 .put("cluster.name", this.getConfig().getClusterName())
			                                 .put("transport.client", true);

			if (this.getConfig().isHttps())
			{
				KeyStoreManager ksm=Environment.getInstance().getKeyStoreManagerApollo();
				builder
					.put("path.home", ".")
					.put("searchguard.ssl.transport.enabled", true)
					.put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_TRUSTSTORE_FILEPATH, ksm.getTrustStore().getAbsolutePath())
					.put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_TRUSTSTORE_PASSWORD, ksm.getTrustStorePwd())
					.put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_KEYSTORE_PASSWORD, ksm.getKeyStorePwd())
					.put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_KEYSTORE_FILEPATH, ksm.getKeyStore().getAbsolutePath())
					.put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_ENFORCE_HOSTNAME_VERIFICATION, false)
					.put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_ENFORCE_HOSTNAME_VERIFICATION_RESOLVE_HOST_NAME, false)
					.put(SSLConfigConstants.SEARCHGUARD_SSL_HTTP_CLIENTAUTH_MODE, "NONE");
			}
			else
			{
				builder
					.put("transport.tcp.port", this.getConfig().getClientPortRange())
					.put("http.enabled", false);
			}

			Settings settings=builder.build();

			// this.setLogs();
			TransportClient transportClient;
			if (this.getConfig().isHttps())
			{
				transportClient=TransportClient.builder().settings(settings).addPlugin(SearchGuardSSLPlugin.class).build();
			}
			else
			{
				transportClient=TransportClient.builder().settings(settings).build();
			}

			if ((null!=this.getConfig().getClientHosts()) && this.getConfig().getClientHosts().isJSONArray())
			{
				for (Object o : this.getConfig().getClientHosts())
				{
					if (o instanceof String)
					{
						try
						{
							transportClient.addTransportAddress(
								new InetSocketTransportAddress(InetAddress.getByName(o.toString()), this.getConfig().getClientPort())
							);
						}
						catch (Exception ex)
						{
							Environment.getInstance().getReportHandler().severe(ex);
						}
					}
				}
			}
			else
			{
				transportClient.addTransportAddress(
					new InetSocketTransportAddress(InetAddress.getByName(this.getConfig().getHttpHost()), this.getConfig().getClientPort())
				);
			}
			this.client=transportClient;
			this.opened=this.isConnected();
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
			EsClientTransport.INSTANCE=null;
		}
	}

	public boolean isConnected()
	{
		boolean result=false;
		try
		{
			List<DiscoveryNode> nodes=((TransportClient) this.client).connectedNodes();
			result=!nodes.isEmpty();
			if (!result)
			{
				Environment.getInstance().getReportHandler().severe("No nodes available. Verify ElasticSearch is running on, %s:%d.",
					this.getConfig().getHttpHost(), this.getConfig().getClientPort()
				);
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
		}
		if (result)
		{
			Environment.getInstance().getReportHandler().info("Athena can connect to the index store at %s:%d",
				this.getConfig().getHttpHost(), this.getConfig().getClientPort()
			);
		}
		return result;
	}

	// Getters and setters
	protected static EsClientTransport getInstance()
	{
		if (EsClientTransport.isDegraded())
		{
			EsClientTransport.makeNode();
		}
		return EsClientTransport.INSTANCE;
	}

	public static boolean isDegraded()
	{
		return ((null==EsClientTransport.INSTANCE) || !EsClientTransport.INSTANCE.isOpen());
	}

	private static synchronized void makeNode()
	{
		if (EsClientTransport.isDegraded())
		{
			if (null!=EsClientTransport.INSTANCE)
			{
				try
				{
					Environment.getInstance().getReportHandler().info("EsClientTransport closing transport node.");
					EsClientTransport.INSTANCE.close();
				}
				catch (Exception ignored)
				{
				}
			}
			Environment.getInstance().getReportHandler().info("EsClientTransport create new transport node.");
			EsClientTransport.INSTANCE=new EsClientTransport();
		}
	}
}
