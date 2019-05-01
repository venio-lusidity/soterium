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

package com.lusidity.services.security.authentication.acs;

import com.lusidity.Environment;
import com.lusidity.data.ClassHelper;
import com.lusidity.data.DataVertex;
import com.lusidity.data.types.names.WesternName;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.common.BaseContactDetail;
import com.lusidity.domains.common.Email;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.security.TripleDES;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.server.resources.BaseServerResource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.representation.Representation;

import java.net.URLDecoder;
import java.util.Random;
import java.util.regex.Pattern;


public
class AccessControlHandler
{
	private static final String COOKIE_NAME = "lusidity";
	private static final int MAX_COOKIE_AGE = 604800000;
	private static final Pattern SPLIT_EMAIL = Pattern.compile("@");
	private boolean isAuthenticated = false;
	private Person user = null;
	private Identity identity = null;
	private JSONObject token = null;
	private BaseServerResource serverResource;

	public
	AccessControlHandler(BaseServerResource baseServerResource)
	{
		super();
		this.serverResource = baseServerResource;
	}

    private static boolean isAdministrator(String email) {
        boolean result = false;
        if (!StringX.isBlank(email)) {
	        Environment.getInstance().getReportHandler().notImplemented();
        }
        return result;
    }

    private static String generateName() {
        String lexicon = "abcdefghijklmnopqrstuvwxyz12345674890";
        @SuppressWarnings("UnsecureRandomNumberGeneration") Random random = new Random();
        StringBuilder builder = new StringBuilder();
        while (builder.toString().isEmpty()) {
            int length = random.nextInt(5) + 5;
            for (int i = 0; i < length; i++) {
                builder.append(lexicon.charAt(random.nextInt(lexicon.length())));
            }
        }
        return "lusidity_" + builder.toString();
    }

    private static String parseEmail(CharSequence email) {
        return AccessControlHandler.SPLIT_EMAIL.split(email)[0];
    }

    private static JSONObject getToken(JSONObject token)
            throws JSONException {
        JSONObject tokenResponse = token.getJSONObject("t:RequestSecurityTokenResponse");
        JSONObject securityToken = tokenResponse.getJSONObject("t:RequestedSecurityToken");
        JSONObject assertion = securityToken.getJSONObject("Assertion");
        JSONObject attributeStatement = assertion.getJSONObject("AttributeStatement");
        JSONArray attributes = attributeStatement.getJSONArray("Attribute");
        JSONObject subject = assertion.getJSONObject("Subject");

        JSONObject results = new JSONObject();
        results.put("id", subject.get("NameID").toString());

        int len = attributes.length();
        for (int i = 0; i < len; i++) {
            JSONObject item = attributes.getJSONObject(i);
            @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern") String[] schema = item.getString("Name").split("/");
            String name = schema[schema.length - 1];
            results.put(name, item.getString("AttributeValue"));
        }

        return results;
    }

    /**
     * Create a new principal and associated identity.
     *
     * @param provider     Identity provider (e.g., "Google").
     * @param identifier   Provider-specific identifier.
     * @param name         Principal's human-readable name.
     * @param emailAddress email address (may be null).
     * @param cls          Class to use for principal; currently, this MUST be Person.class, but other classes may be allowed in future releases.
     * @return Newly-created principal.
     */
    @SuppressWarnings({
            "FeatureEnvy",
            "ConstantConditions"
    })
    public static <T extends BasePrincipal> T getOrCreateIdentity(
            String provider, String identifier, String name, String emailAddress, Class<? extends DataVertex> cls, Identity.LoginType loginType
    )
            throws ApplicationException {
		T result = null;
        try {
			//  Use existing identity if possible
			Identity identity = Identity.get(provider, identifier);
			if (null == identity)
			{
				//  Create a new identity if needed
				identity = Identity.create(provider, identifier, loginType);
			}

			//  Get existing principal associated with entity
			BasePrincipal principal = identity.getPrincipal();
			if ((null == principal) && cls.equals(Person.class))
			{
				//  Create a new principal if needed
				Person person = new Person();

				WesternName personalName = new WesternName(name);
				person.fetchFirstName().setValue(personalName.getFirstName());
				person.fetchMiddleName().setValue(personalName.getMiddleName());
				person.fetchLastName().setValue(personalName.getLastName());
				person.fetchPrefix().setValue(personalName.getPrefix());

				person.save();

				Email email = new Email(BaseContactDetail.CategoryTypes.home_email, emailAddress);
				boolean added = person.getContactDetails().add(email);
				if(!added){
					Environment.getInstance().getReportHandler().warning("Failed to add contact information to %s.", person.fetchId().getValue());
				}

				added = identity.getPrincipals().add(person);
				if(!added){
					Environment.getInstance().getReportHandler().warning("Failed to link principal and identity.");
				}

				principal = person;
			}

			if(null!=principal){
				//noinspection unchecked
				result = (T)principal;
			}
        } catch (Exception e) {
            throw new ApplicationException(e);
        }

        return result;
    }

	/** Removes existing cooking and adds a new one. */
	public
	void addOrUpdateCookie()
		throws ApplicationException
	{
		try
		{
			TripleDES des = new TripleDES(Environment.getInstance().getSetting("cookie_key"));
			this.serverResource.getResponse().getCookieSettings().removeAll(AccessControlHandler.COOKIE_NAME);

			String cookieValue = null;
			Cookie cookie = this.serverResource.getRequest().getCookies().getFirst(AccessControlHandler.COOKIE_NAME, true);

			if ((null != cookie) && !StringX.isBlank(cookie.getValue()))
			{
				try
				{
					cookieValue = des.decrypt(cookie.getValue());
				}
				catch (Exception ignore)
				{
					cookieValue = null;
				}
			}

			if ((null != this.token) && this.token.has("id"))
			{
				cookieValue = this.getToken().toString();
			}

			if (!StringX.isBlank(cookieValue))
			{
				@SuppressWarnings("DynamicRegexReplaceableByCompiledPattern") String[] domains =
					this.serverResource.getRequest().getOriginalRef().getHostDomain().split("\\.");
				CookieSetting cs = new CookieSetting(AccessControlHandler.COOKIE_NAME, des.encrypt(cookieValue));
				cs.setDomain(String.format(".%s.%s", domains[1], domains[2]));
				cs.setMaxAge(AccessControlHandler.MAX_COOKIE_AGE); // 7 days
				//cs.setSecure(true);

				this.serverResource.getResponse().getCookieSettings().add(cs);
				this.isAuthenticated = true;
			}
		}
		catch (Exception ex)
		{
			throw new ApplicationException(ex);
		}
	}

	public
	boolean isAuthenticated()
	{
		if (!this.isAuthenticated && (null != this.getToken()))
		{
			Cookie cookie = this.serverResource.getRequest().getCookies().getFirst(AccessControlHandler.COOKIE_NAME, true);
			this.isAuthenticated = ((null != cookie) && !StringX.isBlank(cookie.getValue()));
		}
		return this.isAuthenticated;
	}

	public
	void handle(Representation representation)
		throws ApplicationException
	{
		try
		{
			String token = representation.getText();
			if (StringX.isBlank(token))
			{
				throw new ApplicationException("The token is empty.");
			}
			token = URLDecoder.decode(token, "UTF-8");
			token = token.replace("wa=wsignin1.0&wresult=", "");
			JSONObject jsonObject = XML.toJSONObject(token);
			this.token = AccessControlHandler.getToken(jsonObject);

			String id = this.token.getString("id");
			String provider = this.token.getString("identityprovider");
			String name = this.token.has("name") ? this.token.getString("name") : null;
			String email = this.token.has("emailaddress") ? this.token.getString("emailaddress") : null;

			if (StringX.isBlank(email) || !AccessControlHandler.isAdministrator(email))
			{
				// TODO: remove this when accounts are allowed.
				throw new ApplicationException("Remove this when user accounts are allowed.");
			}

			//noinspection NestedConditionalExpression
			name=StringX.isBlank(name) ? (StringX.isBlank(email) ? AccessControlHandler.generateName() : AccessControlHandler.parseEmail(email)) : name;

			@SuppressWarnings("UnusedDeclaration") Person person = AccessControlHandler.getOrCreateIdentity(provider, id, name, email, Person.class, Identity.LoginType.token);

			this.addOrUpdateCookie();
		}
		catch (Exception ignored)
		{
			this.isAuthenticated = false;
		}
	}

	public
	JSONObject getToken()
	{
		try
		{
			Cookie cookie = this.serverResource.getRequest().getCookies().getFirst(AccessControlHandler.COOKIE_NAME, true);
			if (null != cookie)
			{

				TripleDES des = new TripleDES(Environment.getInstance().getSetting("cookie_key"));
				String cookieValue = cookie.getValue();
				cookieValue = des.decrypt(cookieValue);
				this.token = new JSONObject(cookieValue);
			}
		}
		catch (Exception ignore)
		{
		}
		return this.token;
	}

	public
	JsonData logout()
	{
		this.serverResource.getResponse().getCookieSettings().getFirst(AccessControlHandler.COOKIE_NAME).setMaxAge(0);
		JsonData result = JsonData.createObject();
		result.put("authenticated", false);
		return result;
	}

	private
	Person getUser()
		throws ApplicationException
	{
		if ((null == this.user) && (null != this.getIdentity()))
		{
			this.user = ClassHelper.as(this.getIdentity().getPrincipal(), Person.class);
		}
		return this.user;  //To change body of created methods use FileInfo | Settings | FileInfo Templates.
	}

	public
	Identity getIdentity()
		throws ApplicationException
	{
		if (null == this.identity)
		{
			this.isAuthenticated = this.isAuthenticated();

			if (this.isAuthenticated && (null != this.getToken()))
			{
				try
				{
					String id = this.getToken().getString("id");
					String provider = this.getToken().getString("identityprovider");
					this.identity = Identity.get(provider, id);
				}
				catch (Exception ex)
				{
					throw new ApplicationException(ex);
				}
			}
		}
		return this.identity;
	}

	public
	JsonData getUserAsJSON()
		throws JSONException
	{
		JsonData result;
		try
		{
			this.isAuthenticated = this.isAuthenticated();

			if (this.isAuthenticated && (null != this.getToken()))
			{
				String provider = this.getToken().getString("identityprovider");
				result = this.getUser().toJson(false);
				result.put("authenticated", this.isAuthenticated);
				result.put("provider", provider);
			}
			else
			{
				throw new ApplicationException("The cookie is invalid.");
			}
		}
		catch (Exception ignore)
		{
			result = JsonData.createObject();
			this.isAuthenticated = false;
			result.put("authenticated", this.isAuthenticated());
			this.logout();
		}

		return result;
	}
}
