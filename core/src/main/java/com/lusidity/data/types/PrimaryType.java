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

package com.lusidity.data.types;


import java.util.ArrayList;
import java.util.Collection;

public class PrimaryType
{

	private static final Collection<String> PRIMARY_TYPES=new ArrayList<>();

	static
	{
		// These to be in order of most important.
		// This is because a /people/person can also be a /music/artist.
		// So if you want /people/person to return then put it higher in the list.
		PrimaryType.PRIMARY_TYPES.add("/people/person");
		PrimaryType.PRIMARY_TYPES.add("/book/book_edition");
		PrimaryType.PRIMARY_TYPES.add("/book/book");
		PrimaryType.PRIMARY_TYPES.add("/film/film_series");
		PrimaryType.PRIMARY_TYPES.add("/film/film");
		PrimaryType.PRIMARY_TYPES.add("/music/album");
		PrimaryType.PRIMARY_TYPES.add("/music/artist");
	}
}
