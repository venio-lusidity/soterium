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

package com.lusidity.helper;

import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImportHistoryHelper
{
	public static final String TOPIC_PATH="Notify::NotificationMessage::Topic::content";
	private ImportHistoryHelper(){}
	public enum ScanType{
		unknown,
		full,
		complete,
		incremental,
		delta,
		baseline,
		details;

		public static ImportHistoryHelper.ScanType get(JsonData data){
			ImportHistoryHelper.ScanType result= ImportHistoryHelper.ScanType.unknown;
			if(!StringX.isBlank(data.getString("scanType"))){
				String scanType = data.getString("scanType");
				result = ImportHistoryHelper.getScanType(scanType);
			}
			else{
				String topic=data.getString(ImportHistoryHelper.TOPIC_PATH);
				if(!StringX.isBlank(topic))
				{
					result = ImportHistoryHelper.parseTopic(topic);
				}
			}
			return result;
		}

	}

	public static boolean isCheckableFindingsType(ImportHistoryHelper.ScanType type){
		boolean result;
		//modify as required
		switch(type)
		{
			case full:
			case baseline:
			case incremental:
			{
				result = true;
				break;
			}
			case delta:
			case unknown:
			case details:
			case complete:
			default:
			{
				result = false;
				break;
			}
		}
		return result;
	}
	private static List<String> getScanTypes() {
		return Stream.of(ImportHistoryHelper.ScanType.values())
		             .map(ImportHistoryHelper.ScanType::name)
		             .collect(Collectors.toList());
	}
	private static ImportHistoryHelper.ScanType getScanType(String scanType){
		ImportHistoryHelper.ScanType result= ImportHistoryHelper.ScanType.unknown;
		for(ImportHistoryHelper.ScanType type: ImportHistoryHelper.ScanType.values()){
			if(StringX.equalsAnyIgnoreCase(type.toString(), scanType)){
				result = type;
				break;
			}
		}
		return result;
	}
	private static ImportHistoryHelper.ScanType parseTopic(String topic) {
		ImportHistoryHelper.ScanType result= ImportHistoryHelper.ScanType.unknown;
		List<String> types = ImportHistoryHelper.getScanTypes();
		for(String type: types){
			if(StringX.containsIgnoreCase(topic, type)){
				result = ImportHistoryHelper.getScanType(type);
				break;
			}
		}
		return result;
	}
}
