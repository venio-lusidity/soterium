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

import com.floragunn.searchguard.ssl.util.SSLConfigConstants;
import com.lusidity.Environment;
import com.lusidity.framework.internet.http.KeyStoreManager;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

/**
 * http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/client.html
 */
public class EsClientNode extends BaseClient
{
	static EsClientNode INSTANCE=null;
	private static boolean LOADED=false;

	// Constructors
	public EsClientNode()
	{
		super();
	}

	// Overrides
	public void start()
	{
		try
		{
			if (null!=EsClientNode.INSTANCE)
			{
				EsClientNode.INSTANCE.close();
			}
			String hosts=this.getConfig().getClientHosts().toList(",");
			Settings.Builder builder=Settings.settingsBuilder()
			                                 .put("path.home", "/")
			                                 .put("http.enabled", false)
			                                 .put("node.master", false)
			                                 .put("node.data", false)
			                                 .put("network.host", this.getConfig().getLocalHostAddress())
			                                 //.put("transport.tcp.port", this.getConfig().getClientPortRange())
			                                 .put("discovery.zen.ping.multicast.enabled", false)
			                                 .put("discovery.zen.ping.unicast.hosts", hosts);

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

			// this.setLogs();
			Node node=NodeBuilder.nodeBuilder()
			                     .clusterName("apollo-i")
			                     .settings(builder.build())
			                     .local(false)
			                     .client(true) /* do not allow this node to store data *, same as "node.data: false"*/
			                     .node();

			this.client=node.client();
			this.opened=true;
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().severe(ex);
			EsClientNode.INSTANCE=null;
		}
	}

	// Getters and setters
	protected static synchronized EsClientNode getInstance()
	{
		if (EsClientNode.isDegraded())
		{
			EsClientNode.makeNode();
		}
		return EsIndexStore.getInstance().getConfig().clientEnabled() ? EsClientNode.INSTANCE : null;
	}

	private static boolean isDegraded()
	{
		return EsIndexStore.getInstance().getConfig().clientEnabled() && ((null==EsClientNode.INSTANCE) || !EsClientNode.INSTANCE.isOpen());
	}

	private static synchronized void makeNode()
	{
		if (EsClientNode.isDegraded())
		{
			EsClientNode.INSTANCE=new EsClientNode();
		}
	}
}
