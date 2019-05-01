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


import org.elasticsearch.action.*;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.exists.ExistsRequest;
import org.elasticsearch.action.exists.ExistsRequestBuilder;
import org.elasticsearch.action.exists.ExistsResponse;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.explain.ExplainRequestBuilder;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.fieldstats.FieldStatsRequest;
import org.elasticsearch.action.fieldstats.FieldStatsRequestBuilder;
import org.elasticsearch.action.fieldstats.FieldStatsResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.indexedscripts.delete.DeleteIndexedScriptRequest;
import org.elasticsearch.action.indexedscripts.delete.DeleteIndexedScriptRequestBuilder;
import org.elasticsearch.action.indexedscripts.delete.DeleteIndexedScriptResponse;
import org.elasticsearch.action.indexedscripts.get.GetIndexedScriptRequest;
import org.elasticsearch.action.indexedscripts.get.GetIndexedScriptRequestBuilder;
import org.elasticsearch.action.indexedscripts.get.GetIndexedScriptResponse;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptRequest;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptRequestBuilder;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptResponse;
import org.elasticsearch.action.percolate.*;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.suggest.SuggestRequest;
import org.elasticsearch.action.suggest.SuggestRequestBuilder;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.action.termvectors.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.support.Headers;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.logging.log4j.LogConfigurator;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;

import java.io.Closeable;
import java.util.UUID;

@SuppressWarnings("OverlyComplexClass")
public abstract class BaseClient implements Closeable, Client
{
	protected Client client=null;
	boolean opened=false;
	private String nodeName=null;

	// Constructors
	public BaseClient()
	{
		super();
		this.start();
	}

	protected abstract void start();

	// Overrides
	@Override
	public void close()
	{
		if (null!=this.client)
		{
			this.client.close();
		}
		this.client=null;
		this.opened=false;
	}

	@Override
	public AdminClient admin()
	{
		return this.client.admin();
	}

	@Override
	public ActionFuture<IndexResponse> index(IndexRequest indexRequest)
	{
		return this.client.index(indexRequest);
	}

	@Override
	public void index(IndexRequest indexRequest, ActionListener<IndexResponse> actionListener)
	{
		this.client.index(indexRequest, actionListener);
	}

	@Override
	public IndexRequestBuilder prepareIndex()
	{
		return this.client.prepareIndex();
	}

	@Override
	public ActionFuture<UpdateResponse> update(UpdateRequest updateRequest)
	{
		return this.client.update(updateRequest);
	}

	@Override
	public void update(UpdateRequest updateRequest, ActionListener<UpdateResponse> actionListener)
	{
		this.client.update(updateRequest, actionListener);
	}

	@Override
	public UpdateRequestBuilder prepareUpdate()
	{
		return this.client.prepareUpdate();
	}

	@Override
	public UpdateRequestBuilder prepareUpdate(String s, String s1, String s2)
	{
		return this.client.prepareUpdate(s, s1, s2);
	}

	@Override
	public IndexRequestBuilder prepareIndex(String indexName, String partitionTypeName)
	{
		return this.client.prepareIndex(indexName, partitionTypeName);
	}

	@Override
	public IndexRequestBuilder prepareIndex(String s, String s1, @Nullable String s2)
	{
		return this.client.prepareIndex(s, s1, s2);
	}

	@Override
	public ActionFuture<DeleteResponse> delete(DeleteRequest deleteRequest)
	{
		return this.client.delete(deleteRequest);
	}

	@Override
	public void delete(DeleteRequest deleteRequest, ActionListener<DeleteResponse> actionListener)
	{
		this.client.delete(deleteRequest, actionListener);
	}

	@Override
	public DeleteRequestBuilder prepareDelete()
	{
		return this.client.prepareDelete();
	}

	@Override
	public DeleteRequestBuilder prepareDelete(String s, String s1, String s2)
	{
		return this.client.prepareDelete(s, s1, s2);
	}

	@Override
	public ActionFuture<BulkResponse> bulk(BulkRequest bulkRequest)
	{
		return this.client.bulk(bulkRequest);
	}

	@Override
	public void bulk(BulkRequest bulkRequest, ActionListener<BulkResponse> actionListener)
	{
		this.client.bulk(bulkRequest, actionListener);
	}

	@Override
	public BulkRequestBuilder prepareBulk()
	{
		return this.client.prepareBulk();
	}

	@Override
	public ActionFuture<GetResponse> get(GetRequest getRequest)
	{
		return this.client.get(getRequest);
	}

	@Override
	public void get(GetRequest getRequest, ActionListener<GetResponse> actionListener)
	{
		this.client.get(getRequest, actionListener);
	}

	@Override
	public GetRequestBuilder prepareGet()
	{
		return this.client.prepareGet();
	}

	@Override
	public GetRequestBuilder prepareGet(String s, @Nullable String s1, String s2)
	{
		return this.client.prepareGet(s, s1, s2);
	}

	@Override
	public PutIndexedScriptRequestBuilder preparePutIndexedScript()
	{
		return this.client.preparePutIndexedScript();
	}

	@Override
	public PutIndexedScriptRequestBuilder preparePutIndexedScript(@Nullable String s, String s1, String s2)
	{
		return this.client.preparePutIndexedScript(s, s1, s2);
	}

	@Override
	public void deleteIndexedScript(DeleteIndexedScriptRequest deleteIndexedScriptRequest, ActionListener<DeleteIndexedScriptResponse> actionListener)
	{
		this.client.deleteIndexedScript(deleteIndexedScriptRequest, actionListener);
	}

	@Override
	public ActionFuture<DeleteIndexedScriptResponse> deleteIndexedScript(DeleteIndexedScriptRequest
		                                                                     deleteIndexedScriptRequest)
	{
		return this.client.deleteIndexedScript(deleteIndexedScriptRequest);
	}

	@Override
	public DeleteIndexedScriptRequestBuilder prepareDeleteIndexedScript()
	{
		return this.client.prepareDeleteIndexedScript();
	}

	@Override
	public DeleteIndexedScriptRequestBuilder prepareDeleteIndexedScript(@Nullable String s, String s1)
	{
		return this.client.prepareDeleteIndexedScript(s, s1);
	}

	@Override
	public void putIndexedScript(PutIndexedScriptRequest putIndexedScriptRequest, ActionListener<PutIndexedScriptResponse> actionListener)
	{
		this.client.putIndexedScript(putIndexedScriptRequest, actionListener);
	}

	@Override
	public ActionFuture<PutIndexedScriptResponse> putIndexedScript(PutIndexedScriptRequest putIndexedScriptRequest)
	{
		return this.client.putIndexedScript(putIndexedScriptRequest);
	}

	@Override
	public GetIndexedScriptRequestBuilder prepareGetIndexedScript()
	{
		return this.client.prepareGetIndexedScript();
	}

	@Override
	public GetIndexedScriptRequestBuilder prepareGetIndexedScript(@Nullable String s, String s1)
	{
		return this.client.prepareGetIndexedScript(s, s1);
	}

	@Override
	public void getIndexedScript(GetIndexedScriptRequest getIndexedScriptRequest, ActionListener
		<GetIndexedScriptResponse> actionListener)
	{
		this.client.getIndexedScript(getIndexedScriptRequest, actionListener);
	}

	@Override
	public ActionFuture<GetIndexedScriptResponse> getIndexedScript(GetIndexedScriptRequest getIndexedScriptRequest)
	{
		return this.client.getIndexedScript(getIndexedScriptRequest);
	}

	@Override
	public ActionFuture<MultiGetResponse> multiGet(MultiGetRequest multiGetRequest)
	{
		return this.client.multiGet(multiGetRequest);
	}

	@Override
	public void multiGet(MultiGetRequest multiGetRequest, ActionListener<MultiGetResponse> actionListener)
	{
		this.client.multiGet(multiGetRequest, actionListener);
	}

	@Override
	public MultiGetRequestBuilder prepareMultiGet()
	{
		return this.client.prepareMultiGet();
	}

	@SuppressWarnings("deprecation")
	@Override
	public ActionFuture<CountResponse> count(CountRequest countRequest)
	{
		return this.client.count(countRequest);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void count(CountRequest countRequest, ActionListener<CountResponse> actionListener)
	{
		this.client.count(countRequest, actionListener);
	}

	@SuppressWarnings("deprecation")
	@Override
	public CountRequestBuilder prepareCount(String... strings)
	{
		return this.client.prepareCount(strings);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ActionFuture<ExistsResponse> exists(ExistsRequest existsRequest)
	{
		return this.client.exists(existsRequest);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void exists(ExistsRequest existsRequest, ActionListener<ExistsResponse> actionListener)
	{
		this.client.exists(existsRequest, actionListener);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ExistsRequestBuilder prepareExists(String... strings)
	{
		return this.client.prepareExists(strings);
	}

	@Override
	public ActionFuture<SuggestResponse> suggest(SuggestRequest suggestRequest)
	{
		return this.client.suggest(suggestRequest);
	}

	@Override
	public void suggest(SuggestRequest suggestRequest, ActionListener<SuggestResponse> actionListener)
	{
		this.client.suggest(suggestRequest, actionListener);
	}

	@Override
	public SuggestRequestBuilder prepareSuggest(String... strings)
	{
		return this.client.prepareSuggest(strings);
	}

	@Override
	public ActionFuture<SearchResponse> search(SearchRequest searchRequest)
	{
		return this.client.search(searchRequest);
	}

	@Override
	public void search(SearchRequest searchRequest, ActionListener<SearchResponse> actionListener)
	{
		this.client.search(searchRequest, actionListener);
	}

	@Override
	public SearchRequestBuilder prepareSearch(String... strings)
	{
		return this.client.prepareSearch(strings);
	}

	@Override
	public ActionFuture<SearchResponse> searchScroll(SearchScrollRequest searchScrollRequest)
	{
		return this.client.searchScroll(searchScrollRequest);
	}

	@Override
	public void searchScroll(SearchScrollRequest searchScrollRequest, ActionListener<SearchResponse> actionListener)
	{
		this.client.searchScroll(searchScrollRequest, actionListener);
	}

	@Override
	public SearchScrollRequestBuilder prepareSearchScroll(String s)
	{
		return this.client.prepareSearchScroll(s);
	}

	@Override
	public ActionFuture<MultiSearchResponse> multiSearch(MultiSearchRequest multiSearchRequest)
	{
		return this.client.multiSearch(multiSearchRequest);
	}

	@Override
	public void multiSearch(MultiSearchRequest multiSearchRequest, ActionListener<MultiSearchResponse> actionListener)
	{
		this.client.multiSearch(multiSearchRequest, actionListener);
	}

	@Override
	public MultiSearchRequestBuilder prepareMultiSearch()
	{
		return this.client.prepareMultiSearch();
	}

	@Override
	public ActionFuture<TermVectorsResponse> termVectors(TermVectorsRequest termVectorsRequest)
	{
		return this.client.termVectors(termVectorsRequest);
	}

	@Override
	public void termVectors(TermVectorsRequest termVectorsRequest, ActionListener<TermVectorsResponse> actionListener)
	{
		this.client.termVectors(termVectorsRequest, actionListener);
	}

	@Override
	public TermVectorsRequestBuilder prepareTermVectors()
	{
		return this.client.prepareTermVectors();
	}

	@Override
	public TermVectorsRequestBuilder prepareTermVectors(String s, String s1, String s2)
	{
		return this.client.prepareTermVectors(s, s1, s2);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ActionFuture<TermVectorsResponse> termVector(TermVectorsRequest termVectorsRequest)
	{
		return this.client.termVector(termVectorsRequest);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void termVector(TermVectorsRequest termVectorsRequest, ActionListener<TermVectorsResponse> actionListener)
	{
		this.client.termVector(termVectorsRequest, actionListener);
	}

	@SuppressWarnings("deprecation")
	@Override
	public TermVectorsRequestBuilder prepareTermVector()
	{
		return this.client.prepareTermVector();
	}

	@SuppressWarnings("deprecation")
	@Override
	public TermVectorsRequestBuilder prepareTermVector(String s, String s1, String s2)
	{
		return this.client.prepareTermVector(s, s1, s2);
	}

	@Override
	public ActionFuture<MultiTermVectorsResponse> multiTermVectors(MultiTermVectorsRequest multiTermVectorsRequest)
	{
		return this.client.multiTermVectors(multiTermVectorsRequest);
	}

	@Override
	public void multiTermVectors(MultiTermVectorsRequest multiTermVectorsRequest, ActionListener<MultiTermVectorsResponse> actionListener)
	{
		this.client.multiTermVectors(multiTermVectorsRequest, actionListener);
	}

	@Override
	public MultiTermVectorsRequestBuilder prepareMultiTermVectors()
	{
		return this.client.prepareMultiTermVectors();
	}

	@Override
	public ActionFuture<PercolateResponse> percolate(PercolateRequest percolateRequest)
	{
		return this.client.percolate(percolateRequest);
	}

	@Override
	public void percolate(PercolateRequest percolateRequest, ActionListener<PercolateResponse> actionListener)
	{
		this.client.percolate(percolateRequest, actionListener);
	}

	@Override
	public PercolateRequestBuilder preparePercolate()
	{
		return this.client.preparePercolate();
	}

	@Override
	public ActionFuture<MultiPercolateResponse> multiPercolate(MultiPercolateRequest multiPercolateRequest)
	{
		return this.client.multiPercolate(multiPercolateRequest);
	}

	@Override
	public void multiPercolate(MultiPercolateRequest multiPercolateRequest, ActionListener<MultiPercolateResponse>
		actionListener)
	{
		this.client.multiPercolate(multiPercolateRequest, actionListener);
	}

	@Override
	public MultiPercolateRequestBuilder prepareMultiPercolate()
	{
		return this.client.prepareMultiPercolate();
	}

	@Override
	public ExplainRequestBuilder prepareExplain(String s, String s1, String s2)
	{
		return this.client.prepareExplain(s, s1, s2);
	}

	@Override
	public ActionFuture<ExplainResponse> explain(ExplainRequest explainRequest)
	{
		return this.client.explain(explainRequest);
	}

	@Override
	public void explain(ExplainRequest explainRequest, ActionListener<ExplainResponse> actionListener)
	{
		this.client.explain(explainRequest, actionListener);
	}

	@Override
	public ClearScrollRequestBuilder prepareClearScroll()
	{
		return this.client.prepareClearScroll();
	}

	@Override
	public ActionFuture<ClearScrollResponse> clearScroll(ClearScrollRequest clearScrollRequest)
	{
		return this.client.clearScroll(clearScrollRequest);
	}

	@Override
	public void clearScroll(ClearScrollRequest clearScrollRequest, ActionListener<ClearScrollResponse> actionListener)
	{
		this.client.clearScroll(clearScrollRequest, actionListener);
	}

	@Override
	public FieldStatsRequestBuilder prepareFieldStats()
	{
		return this.client.prepareFieldStats();
	}

	@Override
	public ActionFuture<FieldStatsResponse> fieldStats(FieldStatsRequest fieldStatsRequest)
	{
		return this.client.fieldStats(fieldStatsRequest);
	}

	@Override
	public void fieldStats(FieldStatsRequest fieldStatsRequest, ActionListener<FieldStatsResponse> actionListener)
	{
		this.client.fieldStats(fieldStatsRequest, actionListener);
	}

	@Override
	public Settings settings()
	{
		return this.client.settings();
	}

	@Override
	public Headers headers()
	{
		return this.client.headers();
	}

	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> ActionFuture<Response> execute
		(Action<Request, Response, RequestBuilder> action, Request request)
	{
		return this.client.execute(action, request);
	}

	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> void execute(Action<Request, Response,
		RequestBuilder> action, Request request, ActionListener<Response> actionListener)
	{
		this.client.execute(action, request, actionListener);
	}

	@Override
	public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> RequestBuilder prepareExecute
		(Action<Request, Response, RequestBuilder> action)
	{
		return this.client.prepareExecute(action);
	}

	@Override
	public ThreadPool threadPool()
	{
		return this.client.threadPool();
	}

	@SuppressWarnings("unused")
	private synchronized void setLogs()
	{
		if (!this.isOpen())
		{
			Settings settings=Settings.settingsBuilder()
			                          .put("path.conf", this.getConfig().getLogConfFile().getAbsolutePath())
			                          .put("path.logs", this.getConfig().getLogDir().getAbsolutePath())
			                          .put("path.home", this.getConfig().getLogConfigDir().getAbsolutePath())
			                          .put("cluster.name", this.getConfig().getClusterName())
			                          .build();
			LogConfigurator.configure(settings, false);
		}
	}

	public boolean isOpen()
	{
		return this.opened;
	}

	protected EsConfiguration getConfig()
	{
		return EsConfiguration.getInstance();
	}

	// Getters and setters
	@SuppressWarnings("unused")
	public String getNodeName()
	{
		if (null==this.nodeName)
		{
			this.nodeName=String.format("lusidity-%s-%s", this.getClass().getSimpleName(), UUID.randomUUID().toString());
		}
		return this.nodeName;
	}
}
