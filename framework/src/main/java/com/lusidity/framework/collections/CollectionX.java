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

import com.lusidity.framework.text.StringX;
import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Collection helper/utility class.
 *
 * @author jjszucs
 */
public class CollectionX
{
// -------------------------- STATIC METHODS --------------------------

	/**
     * Private constructor. This is a utility class and should not be instantiated.
     */
	public CollectionX() {
        super();
    }

    /**
     * Add all entities in one set to another set.
	 * @param addTo Destination collection.
	 * @param items Source items.
	 * @param <T> Template.
	 */
	public static
	<T> boolean addAllIfUnique(Collection<T> addTo, Iterable<T> items)
	{
        boolean result = false;
        if((null!=items) && (null!=addTo)) {
        	result = true;
            for (T item : items) {
            	boolean added = CollectionX.addIfUnique(addTo, item);
	            if(!added){
	            	result = false;
	            }
            }
        }
        return result;
	}

    /**
     * Add all entities from all collections in one set to another set.
     * @param addTo Destination collection.
     * @param items Source collections.
     * @param <T> Template.
     */
    @SuppressWarnings({"unchecked", "unused"})
	public static <T> boolean addAllIfUnique(Collection<T> addTo, Iterable<T>... items) {
        boolean added = true;
        for (Iterable<T> item : items) {
            boolean add = CollectionX.addAllIfUnique(addTo, item);
            if (!add) {
                added = false;
            }
        }
        return added;
    }

    /**
     * Add all entities in one set to another set.
     * @param addTo Destination collection.
     * @param items Source items.
     * @param <T> Template.
     */
    public static synchronized
    <T> boolean addAllIfUniqueSynchronized(Collection<T> addTo, Iterable<T> items)
    {
	    boolean result = false;
	    if((null!=items) && (null!=addTo)) {
		    result = true;
		    for (T item : items) {
			    boolean added = CollectionX.addIfUnique(addTo, item);
			    if(!added){
				    result = false;
			    }
		    }
	    }
	    return result;
    }

	public static <T>
	boolean addIfUnique(Collection<T> addTo, T item)
	{
		boolean result = false;
		if ((null!=item) && (null!=addTo) && !addTo.contains(item))
		{
			result = addTo.add(item);
		}
		return result;
	}

	private static <T> boolean contains(Collection<T> addTo, T item) {
		boolean result = false;
		for(T check: addTo){
			result = (check.equals(item));
			if(result){
				break;
			}
		}
		return result;
	}

	/**
	 * Collect iterable objects into a collection.
	 * @param iterable Iterable objects.
	 * @param <T> Type.
	 * @return Collection of objects.
	 */
	public static
	<T> Collection<T> collect(Iterable<T> iterable)
	{
		Collection<T> results = new ArrayList<T>();
		for (T result : iterable)
		{
			results.add(result);
		}
		return results;
	}

	/**
	 * Get single object from an iterable set.
	 * @param objects Objects.
	 * @return Single object in the set; null if set is empty or contains more than one object.
	 */
	public static
	<T> T getSingleOrNull(Iterable<T> objects)
	{
		T result = null;
		if(null!=objects)
		{
			Iterator<T> iterator = objects.iterator();
			if (iterator.hasNext())
			{
				result = iterator.next();
				if (iterator.hasNext())
				{
					//  If iterable set has more than one object, return null
					result = null;
				}
			}
		}
		return result;
	}

    public static <K, V> Map.Entry<K, V> getEntryFirstOrNull(Iterable items) {
        Map.Entry<K, V> result = null;
	    if(null!=items)
	    {
		    Iterator iterator = items.iterator();
		    if (iterator.hasNext())
		    {
			    Object obj = iterator.next();
			    if (obj instanceof Map.Entry)
			    {
				    try
				    {
					    //noinspection unchecked
					    result = (Map.Entry<K, V>) obj;
				    } catch (Exception ignored)
				    {
				    }
			    }
		    }
	    }
        return result;
    }

    public static <K, V> Map.Entry<K, V> getEntrySingleOrNull(Iterable items) {
        Map.Entry<K, V> result = null;
	    if(null!=items)
	    {
		    Iterator iterator = items.iterator();
		    if (iterator.hasNext())
		    {
			    Object obj = iterator.next();
			    if (iterator.hasNext())
			    {
				    //  If iterable set has more than one object, return null
				    result = null;
			    }
			    else if (obj instanceof Map.Entry)
			    {
				    try
				    {
					    //noinspection unchecked
					    result = (Map.Entry<K, V>) obj;
				    } catch (Exception ignored)
				    {
				    }
			    }
		    }
	    }
        return result;
    }

// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Convert a typed collection to a typed array.
	 * @param collection Collection.
	 * @param cls Type of members.
	 * @param <T> Type.
	 * @return Typed array.
	 */
	public static <T>
	T[] toArray(Collection<T> collection, Class<T> cls)
	{
		T[] a = null;
		if(null!=collection)
		{
			int n = collection.size();
			//noinspection unchecked
			a = (T[]) Array.newInstance(cls, n);

			int i = 0;
			for (T e : collection)
			{
				a[i++] = e;
			}
		}

		return a;
	}

	/**
	 * Get a value by index from a generic collection.
	 * @param collection Collection.
	 * @param index Ordinal index of element to get.
	 * @param <T> Type.
	 * @return Value, null if collection is null or empty.
	 */
	public static
	<T> T get(Collection<T> collection, int index)
	{
		//noinspection unchecked
		return ((collection != null) && !collection.isEmpty()) ? (T) CollectionUtils.get(collection, index) : null;
	}

	/**
	 * Get the first value in a collection, or null if the collection is null or empty.
	 * @param values Collection.
	 * @param <T> Collection member type.
	 * @return First value in the collection, or null if the collection is null or empty.
	 */
	public static <T>
	T getFirst(Collection<T> values)
	{
		T result=null;
		if (null!=values)
		{
			result=CollectionX.get(values, 0);
		}
		return result;
	}

	/**
	 * Create a new collection from the specified elements.
	 * @param elements Elements to add to new collection.
	 * @param <T> Element type.
	 * @return New collection containing the specified elements.
	 */
	public static
	<T> Collection<T> newFrom(Iterable<T> elements)
	{
		Collection<T> results=new ArrayList<>();
		if (null!=elements)
		{
			for (T element : elements)
			{
				results.add(element);
			}
		}
		return results;
	}

	/**
	 * Get the first element, if any, in an iterable set.
	 * @param iterable Iterable set.
	 * @param <T> Type.
	 * @return First element, or null if the set is empty.
	 */
	public static
	<T> T getFirstOrNull(Iterable<T> iterable)
	{
		Iterator<T> iterator=iterable.iterator();
		return iterator.hasNext() ? iterator.next() : null;
	}

    public static <T>  boolean hasMatch(Iterable<T> a, Iterable<T> b) {
        boolean foundMatch = false;

        if((null != a) && (null != b)) {
            for (T ta : a) {
                for (T tb : b) {
                    foundMatch = ta.equals(tb);
                    if (foundMatch) {
                        break;
                    }
                }

                if (foundMatch) {
                    break;
                }
            }

	        if(!foundMatch){
		        for (T tb : b) {
			        for (T ta : a) {
				        foundMatch = ta.equals(tb);
				        if (foundMatch) {
					        break;
				        }
			        }

			        if (foundMatch) {
				        break;
			        }
		        }
	        }
        }

        return foundMatch;
    }

	public static <T>  boolean hasMatchIgnoreCase(Iterable<T> a, Iterable<T> b) {
		boolean foundMatch = false;

		if((null != a) && (null != b)) {
			for (T ta : a) {
				for (T tb : b) {
					if((null!=ta) && (null!=tb)){
						foundMatch = StringX.equalsIgnoreCase(ta.toString(), tb.toString());
					}
					if (foundMatch) {
						break;
					}
				}

				if (foundMatch) {
					break;
				}
			}
			if(!foundMatch){
				for (T tb : b) {
					for (T ta : a) {
						if((null!=ta) && (null!=tb)){
							foundMatch = StringX.equalsIgnoreCase(ta.toString(), tb.toString());
						}
						if (foundMatch) {
							break;
						}
					}

					if (foundMatch) {
						break;
					}
				}
			}
		}

		return foundMatch;
	}

    public static <T> List<T> reverse(Collection<T> list) {
        List<T> temp = new ArrayList<>();             ;
        for(int i = (list.size()-1);i>-1;i--){
            @SuppressWarnings("unchecked")
            T item = (T) CollectionUtils.get(list, i);
            temp.add(item);
        }
        return temp;
    }


    public static <T> Collection<T> addAll(Object... objects) {
        Collection<T> results = new ArrayList<>();
        CollectionUtils.addAll(results, objects);
        return results;
    }

	/**
	 * Returns the matching item in the collection.  Ensure that the equals method is relevant otherwise override it.
	 * @param items The collection.
	 * @param matcher The item to match.
	 * @return the matching item in the collection.
	 */
	public static <T> T geMatch(Collection<T> items, T matcher)
	{
		T result = null;
		for(T t: items){
			if(t.equals(matcher)){
				result = t;
				break;
			}
		}
		return result;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map)
	{
		return map.entrySet()
		          .stream()
		          .sorted(Map.Entry.comparingByValue(/*Collections.reverseOrder()*/))
		          .collect(Collectors.toMap(
			          Map.Entry::getKey,
			          Map.Entry::getValue,
			          (e1, e2) -> e1,
			          LinkedHashMap::new
		          ));
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValueReverse(Map<K, V> map)
	{
		return map.entrySet()
		          .stream()
		          .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
		          .collect(Collectors.toMap(
			          Map.Entry::getKey,
			          Map.Entry::getValue,
			          (e1, e2) -> e1,
			          LinkedHashMap::new
		          ));
	}
}
