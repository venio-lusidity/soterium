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

package com.lusidity.security.data.filters;


import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.system.security.UserCredentials;
import org.joda.time.DateTime;

import java.util.Collection;

@SuppressWarnings({
	"unused",
	"AbstractClassNeverImplemented"
})
public abstract class BasePrincipalFilter
{
	private final DateTime createdWhen;
	private Class<? extends DataVertex> contextClass = null;
	private BasePrincipal basePrincipal= null;
	private BaseQueryBuilder.Operators operator = BaseQueryBuilder.Operators.should;
	private String property = null;
	private BaseQueryBuilder.StringTypes stringType = BaseQueryBuilder.StringTypes.raw;
	private String value = null;

	/**
	 * Empty constructor is required for dynamic construction.
	 */
	public BasePrincipalFilter(){
		super();
		this.createdWhen =DateTime.now();
	}

	/**
	 * Constructor required for creating initiating the process of creating filters.
	 * @param basePrincipal The BasePrincipal in context.
	 */
	public BasePrincipalFilter(Class<? extends DataVertex> contextClass, BasePrincipal basePrincipal){
		super();
		this.contextClass = contextClass;
		this.basePrincipal=basePrincipal;
		this.createdWhen =DateTime.now();
	}

	/**
	 * Constructor required for creating initiating the process of creating filters.
	 * @param userCredentials The BasePrincipal in context.
	 */
	public BasePrincipalFilter(Class<? extends DataVertex> contextClass, UserCredentials userCredentials){
		super();
		this.contextClass = contextClass;
		this.basePrincipal=userCredentials.getPrincipal();
		this.createdWhen =DateTime.now();
	}


	/**
	 * Constructor required for creating a filter.
	 * @param basePrincipal The BasePrincipal in context.
	 * @param operator A BaseQueryBuilder.Operators.
	 * @param property The property the filter applies to.
	 * @param stringType A BaseQueryBuilder.StringTypes or BaseQueryBuilder.StringTypes.na if not a string.
	 * @param value The value being evaluated.
	 */
	@SuppressWarnings("ConstructorWithTooManyParameters")
	public BasePrincipalFilter(Class<? extends DataVertex> contextClass, BasePrincipal basePrincipal, BaseQueryBuilder.Operators operator, String property, BaseQueryBuilder.StringTypes stringType, String value){
		super();
		this.contextClass = contextClass;
		this.operator = operator;
		this.property = property;
		this.stringType = stringType;
		this.value = value;
		this.basePrincipal=basePrincipal;
		this.createdWhen =DateTime.now();
	}

	public Class<? extends DataVertex> getContext()
	{
		return this.contextClass;
	}

	public BaseQueryBuilder.Operators getOperator() {
		return this.operator;
	}

	public String getProperty() {
		return this.property;
	}

	public BaseQueryBuilder.StringTypes getStringType() {
		return this.stringType;
	}

	public String getValue() {
		return this.value;
	}

	/**
	 * Get filters based on a vertex policy.
	 * @param params Optional parameters to use when retrieving the filters, submitted as key value pairs.
	 * @param <T> Specified Type.
	 * @return An empty collection or usable filters for a query.
	 */
	@SuppressWarnings("ParameterHidesMemberVariable")
	public abstract <T extends BasePrincipalFilter> Collection<T> getFilters(Class<? extends DataVertex> context, Object... params);

	/**
	 * @return The classes in which this BasePrincipalFilter applies to.
	 */
	public abstract Collection<Class<? extends DataVertex>> forClasses();


	/**
	 * @param qb The QueryBuilder to apply the filters to.
	 * @param params Optional parameters to use when retrieving the filters, submitted as key value pairs.
	 */
	public void apply(Class<? extends DataVertex> context, BaseQueryBuilder qb, Object... params) {
		if(null!=params){
			Collection<BasePrincipalFilter> principalFilters = this.getFilters(context, params);
			this.apply(qb, principalFilters);
		}
	}

	/**
	 *
	 * @param queryBuilder The QueryBuilder to apply the filters to.
	 * @param principalFilters  The filters to apply to the QueryBuilder.
	 */
	private void apply(BaseQueryBuilder queryBuilder, Collection<BasePrincipalFilter> principalFilters)
	{
		if((null!=queryBuilder) && (null!=principalFilters)) {
			for (BasePrincipalFilter filter : principalFilters) {
				queryBuilder.filter(filter.getOperator(), filter.getProperty(), filter.getStringType(), filter.getValue());
			}
		}
	}

	public BasePrincipal getBasePrincipal()
	{
		return this.basePrincipal;
	}

	public DateTime getCreatedWhen(){
		return this.createdWhen;
	}
}
