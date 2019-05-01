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

package com.lusidity.framework.collections;

import java.util.*;

public class MapX
{

	public static LinkedHashMap<String, Float> sortByFloatValue(Map<String, Float> unsorted, boolean asc){
		List<Map.Entry<String, Float>> entries = new LinkedList<>(unsorted.entrySet());
		entries.sort(new Comparator<Map.Entry<String, Float>>()
		{
			@Override
			public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2)
			{
				int result = o1.getValue().compareTo(o2.getValue());
				if(!asc){
					result*=-1;
				}
				return result;
			}
		});
		LinkedHashMap<String, Float> results = new LinkedHashMap<>();
		for(Map.Entry<String, Float> entry: entries){
			results.put(entry.getKey(), entry.getValue());
		}
		return results;
	}
	
	public static LinkedHashMap<String, Long> sortByLongValue(Map<String, Long> unsorted, boolean asc){
		List<Map.Entry<String, Long>> entries = new LinkedList<>(unsorted.entrySet());
		entries.sort(new Comparator<Map.Entry<String, Long>>()
		{
			@Override
			public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2)
			{
				int result = o1.getValue().compareTo(o2.getValue());
				if(!asc){
					result*=-1;
				}
				return result;
			}
		});
		LinkedHashMap<String, Long> results = new LinkedHashMap<>();
		for(Map.Entry<String, Long> entry: entries){
			results.put(entry.getKey(), entry.getValue());
		}
		return results;
	}
	
	public static LinkedHashMap<String, Double> sortByDoubleValue(Map<String, Double> unsorted, boolean asc){
		List<Map.Entry<String, Double>> entries = new LinkedList<>(unsorted.entrySet());
		entries.sort(new Comparator<Map.Entry<String, Double>>()
		{
			@Override
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2)
			{
				int result = o1.getValue().compareTo(o2.getValue());
				if(!asc){
					result*=-1;
				}
				return result;
			}
		});
		LinkedHashMap<String, Double> results = new LinkedHashMap<>();
		for(Map.Entry<String, Double> entry: entries){
			results.put(entry.getKey(), entry.getValue());
		}
		return results;
	}
	
	public static LinkedHashMap<String, Integer> sortByIntegerValue(Map<String, Integer> unsorted, boolean asc){
		List<Map.Entry<String, Integer>> entries = new LinkedList<>(unsorted.entrySet());
		entries.sort(new Comparator<Map.Entry<String, Integer>>()
		{
			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
			{
				int result = o1.getValue().compareTo(o2.getValue());
				if(!asc){
					result*=-1;
				}
				return result;
			}
		});
		LinkedHashMap<String, Integer> results = new LinkedHashMap<>();
		for(Map.Entry<String, Integer> entry: entries){
			results.put(entry.getKey(), entry.getValue());
		}
		return results;
	}

	public static LinkedHashMap<String, String> sortByStringValue(Map<String, String> unsorted, boolean asc){
		List<Map.Entry<String, String>> entries = new LinkedList<>(unsorted.entrySet());
		entries.sort(new Comparator<Map.Entry<String, String>>()
		{
			@Override
			public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2)
			{
				int result = o1.getValue().compareTo(o2.getValue());
				if(!asc){
					result*=-1;
				}
				return result;
			}
		});
		LinkedHashMap<String, String> results = new LinkedHashMap<>();
		for(Map.Entry<String, String> entry: entries){
			results.put(entry.getKey(), entry.getValue());
		}
		return results;
	}
}
