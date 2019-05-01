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

package com.lusidity.core;

import com.lusidity.data.ApolloVertex;
import com.lusidity.framework.annotations.AtSchemaClass;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;


@SuppressWarnings("UnusedDeclaration")
@AtSchemaClass(name="Action Item", discoverable=false)
public class ElementAction extends ApolloVertex
{
// ------------------------------ FIELDS ------------------------------

	public enum ActionType
	{
		Buy, /* buy or similar eCommerce action */
		Watch, /* watch (e.g., a trailer, which requires a player window) */
		Info, /* more information */
		Listen,
		Read /* read a book or article */
	}
	private ElementAction.ActionType actionType=ElementAction.ActionType.Info;
	private URI sourceUri=null;
	private Double price=0.0;
	private String format=null;
	private String currency=null;
	private String label=null;
	private URI actionUri=null;
	private URI thumbnailUri=null;

// --------------------------- CONSTRUCTORS ---------------------------

// Constructors
	/**
	 * Constructor.
	 * <p>
	 * IMPORTANT: You MUST call build() after instantiating an Entity-derived object in order for the Entity
	 * to be usable.
	 *
	 * @param dso Underlying data store object.
	 */
	public ElementAction(JsonData dso, Object indexId)
	{
		super(dso, indexId);
	}

	public ElementAction()
	{
		super();
	}

	public ElementAction(URI sourceUri)
	{
		super();
		this.sourceUri=sourceUri;
	}


	/**
	 * Constructor.
	 *
	 * @param sourceUri  Provider source.
	 * @param actionType Action type.
	 * @param label      Label.
	 * @param actionUri  URI.
	 */
	public ElementAction(URI sourceUri, ElementAction.ActionType actionType, String label, URI actionUri)
	{
		super();
		this.sourceUri=sourceUri;
		this.actionType=actionType;
		this.label=label;
		this.actionUri=actionUri;
	}

	@SuppressWarnings("ConstructorWithTooManyParameters")
	public ElementAction(URI sourceUri, ElementAction.ActionType actionType, String label, URI actionUri, String format, String currency, Double price)
	{
		super();
		this.sourceUri=sourceUri;
		this.actionType=actionType;
		this.label=label;
		this.actionUri=actionUri;
		this.format=format;
		this.currency=currency;
		this.price=price;
	}

// --------------------- GETTER / SETTER METHODS ---------------------

// Overrides
	@Override
	public boolean equals(Object o)
	{
		boolean result;

		if (this==o)
		{
			result=true;
		}
		else if ((o==null) || (this.getClass()!=o.getClass()))
		{
			result=false;
		}
		else
		{
			ElementAction that=(ElementAction) o;
			result=(this.actionUri.equals(that.actionUri) && (this.actionType==that.actionType) && (StringX.equals(this.label, that.label)));
		}

		return result;
	}

	@Override
	public int hashCode()
	{
		return this.actionUri.hashCode();
	}

	@Override
	public String toString()
	{
		return String.format("ActionItem: label=%s", this.label);
	}

// Methods
	/**
	 * Find an action by URI in a collection of actions.
	 *
	 * @param actions Actions.
	 * @param s       URI of action to find.
	 * @return Matching action, or null if not found.
	 * @throws ApplicationException
	 */
	public static ElementAction find(Iterable<ElementAction> actions, String s)
		throws ApplicationException
	{
		try
		{
			return ElementAction.find(actions, new URI(s));
		}
		catch (URISyntaxException e)
		{
			throw new ApplicationException(e);
		}
	}

	/**
	 * Find an action by URI in a collection of actions.
	 *
	 * @param actions Actions.
	 * @param uri     URI of action to find.
	 * @return Matching action, or null if not found.
	 */
	public static ElementAction find(Iterable<ElementAction> actions, URI uri)
	{
		ElementAction result=null;

		for (ElementAction action : actions)
		{
			URI actionUri=action.getActionUri();
			if (actionUri.equals(uri))
			{
				result=action;
				break;
			}
		}

		return result;
	}

	/**
	 * Get URI to execute this action (e.g., http://amazon.com/dp/12345&associateTag=mytag-12).
	 *
	 * @return URI to execute this action.
	 */
	public URI getActionUri()
	{
		return this.actionUri;
	}

	public void setActionUri(URI actionUri)
	{
		this.actionUri=actionUri;
	}

	/**
	 * Convenience method to return an empty collection of ActionItem objects.
	 *
	 * @return Empty collection of ActionItem objects
	 */
	public static Collection<ElementAction> empty()
	{
		return new ArrayList<>();
	}

// Getters and setters
	public URI getSourceUri()
	{
		return this.sourceUri;
	}

	public void setSourceUri(URI sourceUri)
	{
		this.sourceUri=sourceUri;
	}

	/**
	 * Get label (usually a verb, e.g., "Buy" or "Rent") for this action.
	 *
	 * @return Label.
	 */
	public String getLabel()
	{
		return this.label;
	}

	public void setLabel(String label)
	{
		this.label=label;
	}

	/**
	 * Get action type.
	 *
	 * @return Action type.
	 */
	public ElementAction.ActionType getActionType()
	{
		return this.actionType;
	}

	public void setActionType(ElementAction.ActionType actionType)
	{
		this.actionType=actionType;
	}

// ------------------------ CANONICAL METHODS ------------------------

	public String getFormat()
	{
		return this.format;
	}

	public void setFormat(String format)
	{
		this.format=format;
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface JSONFriendly ---------------------

	public Double getPrice()
	{
		return this.price;
	}

	public void setPrice(Double price)
	{
		this.price=price;
	}

	public String getCurrency()
	{
		return this.currency;
	}

	public void setCurrency(String currency)
	{
		this.currency=currency;
	}

	/**
	 * Get thumbnail URI.
	 *
	 * @return Thumbnail URI.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public URI getThumbnailUri()
	{
		return this.thumbnailUri;
	}

	/**
	 * Set thumbnail URI.
	 *
	 * @param thumbnailUri Thumbnail URI.
	 */
	public void setThumbnail(URI thumbnailUri)
	{
		this.thumbnailUri=thumbnailUri;
	}
}
