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

package com.lusidity.domains.data.importer;

import com.lusidity.annotations.AtSchemaProperty;
import com.lusidity.collections.ElementEdges;
import com.lusidity.data.field.KeyData;
import com.lusidity.data.field.KeyDataCollection;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.data.ProcessStatus;
import com.lusidity.domains.object.edge.BlobEdge;
import com.lusidity.domains.system.io.FileInfo;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.math.MathX;
import com.lusidity.helper.ImportHistoryHelper;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import static com.lusidity.domains.data.importer.ImporterHistory.Status.waiting;

@AtSchemaClass(name = "Importer History", discoverable = false)
public class ImporterHistory extends BaseDomain
{
	public enum Status{
		waiting,
		failed,
		partial,
		success
	}

	private KeyData<ImporterHistory.Status> status = null;
	private KeyData<ProcessStatus> processStatus = null;
	private KeyDataCollection<String> originalFileNames= null;
	private KeyDataCollection<ImportHistoryHelper.ScanType> scanTypes = null;
	private KeyData<String> originalFileName= null;
	private KeyData<String> elapsed= null;
	private KeyData<String> source = null;
	private KeyData<DateTime> scanned = null;
	private KeyData<DateTime> started = null;
	private KeyData<DateTime> stopped= null;
	private KeyData<Class> importer = null;
	private KeyData<String> importerName = null;
	private KeyData<Boolean> root = null;
	private KeyData<Long> fileSize = null;
	@AtSchemaProperty(name="Blob Files", expectedType=FileInfo.class, edgeType=BlobEdge.class,
		description="A collection of files or binary large objects")
	private ElementEdges<FileInfo> blobFiles=null;

	@AtSchemaProperty(name="Importer Child History", expectedType=ImporterHistory.class,
		description="Child importer history vertices.")
	private ElementEdges<ImporterHistory> histories = null;

	// Constructors
	public ImporterHistory(){
		super();
	}

	public ImporterHistory(JsonData dso, Object indexId){
		super(dso, indexId);
	}

	// Overrides
	@Override
	public JsonData toJson(boolean storing, String... languages)
	{
		JsonData result=super.toJson(storing, languages);
		if (!storing)
		{
			ProcessStatus ps=this.fetchProcessStatus().getValue();
			int p=ps.fetchProcessed().getValue().fetchCount().getValue()+ps.fetchInnerProcessed().getValue().fetchCount().getValue();
			Interval interval=new Interval(this.fetchStarted().getValue(), this.fetchStopped().getValue());
			long perSecond=MathX.getPerSecond((interval.toDurationMillis()/1000), p);
			result.put("perSecond", perSecond);
		}
		return result;
	}

	public KeyData<ProcessStatus> fetchProcessStatus()
	{
		if (null==this.processStatus)
		{
			this.processStatus=new KeyData<>(this, "processStatus", ProcessStatus.class, false, new ProcessStatus());
		}
		return this.processStatus;
	}

	public KeyData<DateTime> fetchStarted()
	{
		if (null==this.started)
		{
			this.started=new KeyData<>(this, "started", DateTime.class, false, null);
		}
		return this.started;
	}

	public KeyData<DateTime> fetchStopped()
	{
		if (null==this.stopped)
		{
			this.stopped=new KeyData<>(this, "stopped", DateTime.class, false, null);
		}
		return this.stopped;
	}

	public KeyDataCollection<String> fetchOriginalFileNames(){
		if(null==this.originalFileNames){
			this.originalFileNames= new KeyDataCollection<>(this, "originalFileNames", String.class, false, false, false, null);
		}
		return this.originalFileNames;
	}

	public KeyDataCollection<ImportHistoryHelper.ScanType> fetchScanTypes(){
		if(null==this.scanTypes){
			this.scanTypes= new KeyDataCollection<>(this, "scanTypes", ImportHistoryHelper.ScanType.class, false, false, false, ImportHistoryHelper.ScanType.unknown);
		}
		return this.scanTypes;
	}

	public KeyData<ImporterHistory.Status> fetchStatus(){
		if(null==this.status){
			this.status= new KeyData<>(this, "status", ImporterHistory.Status.class, false, waiting);
		}
		return this.status;
	}

	public KeyData<Boolean> fetchRoot(){
		if(null==this.root){
			this.root= new KeyData<>(this, "root", Boolean.class, false, false);
		}
		return this.root;
	}

	public KeyData<String> fetchOriginalFileName(){
		if(null==this.originalFileName){
			this.originalFileName= new KeyData<>(this, "originalFileName", String.class, false, null);
		}
		return this.originalFileName;
	}

	public KeyData<String> fetchElapsed(){
		if(null==this.elapsed){
			this.elapsed= new KeyData<>(this, "elapsed", String.class, false, null);
		}
		return this.elapsed;
	}

	public KeyData<Class> fetchImporter(){
		if(null==this.importer){
			this.importer = new KeyData<>(this, "importer", Class.class, false, null);
		}
		return this.importer;
	}

	public KeyData<String> fetchImporterName(){
		if(null==this.importerName){
			this.importerName = new KeyData<>(this, "importerName", String.class, false, null);
		}
		return this.importerName;
	}

	public KeyData<DateTime> fetchScanned(){
		if(null==this.scanned){
			this.scanned = new KeyData<>(this, "scanned", DateTime.class, false, null);
		}
		return this.scanned;
	}

	public KeyData<String> fetchSource(){
		if(null==this.source){
			this.source = new KeyData<>(this, "source", String.class, false, null);
		}
		return this.source;
	}
	public KeyData<Long> fetchFileSize(){
		if(null==this.fileSize){
			this.fileSize = new KeyData<>(this, "fileSize", Long.class, false, null);
		}
		return this.fileSize;
	}

	// Getters and setters
	public ElementEdges<FileInfo> getBlobFiles()
	{
		if (null==this.blobFiles)
		{
			this.buildProperty("blobFiles");
		}
		return this.blobFiles;
	}

	public ElementEdges<ImporterHistory> getHistories()
	{
		if (null==this.histories)
		{
			this.buildProperty("histories");
		}
		return this.histories;
	}
}
