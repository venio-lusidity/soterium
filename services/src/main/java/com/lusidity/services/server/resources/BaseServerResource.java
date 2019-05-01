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

package com.lusidity.services.server.resources;

import com.lusidity.Environment;
import com.lusidity.configuration.ScopedConfiguration;
import com.lusidity.data.DataVertex;
import com.lusidity.data.interfaces.data.IDataStore;
import com.lusidity.data.interfaces.data.query.BaseQueryBuilder;
import com.lusidity.data.interfaces.data.query.IQueryResultHandler;
import com.lusidity.data.json.DataLevel;
import com.lusidity.domains.BaseDomain;
import com.lusidity.domains.acs.security.AnonymousCredentials;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.authorization.Permission;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.object.Edge;
import com.lusidity.domains.people.Person;
import com.lusidity.domains.people.person.Personalization;
import com.lusidity.factories.VertexFactory;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.BaseExtendedData;
import com.lusidity.services.security.annotations.AtAuthorization;
import com.lusidity.services.security.authentication.acs.AccessControlHandler;
import com.lusidity.services.security.authentication.pki.PKICredentials;
import com.lusidity.services.server.WebServerConfig;
import com.lusidity.services.server.WebServices;
import com.lusidity.services.server.resources.helper.VertexHelper;
import com.lusidity.system.security.UserCredentials;
import com.lusidity.system.security.cbac.PolicyDecisionPoint;
import org.joda.time.DateTime;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.*;
import org.restlet.util.Series;

import java.util.*;

/**
 * Abstract base class for server resources, provides access control and other support functions.
 */
@SuppressWarnings({
	"OverlyComplexClass",
	"OverlyCoupledClass"
})
public abstract class BaseServerResource
        extends ServerResource {
	public static final int DEFAULT_QUERY_LIMIT=20;
	// ------------------------------ FIELDS ------------------------------

    /*
         * Fields
         */
    private final AccessControlHandler accessControlHandler;
    private Environment environment = null;
    private UserCredentials credentials=null;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Constructor.
     */
    protected BaseServerResource() {
        super();
        this.accessControlHandler = new AccessControlHandler(this);
        this.environment = Environment.getInstance();
    }

// --------------------- GETTER / SETTER METHODS ---------------------

// Overrides
    @SuppressWarnings("OverlyLongMethod")
    @Override
    protected void doInit()
            throws ResourceException {
        super.doInit();

        try {
            this.accessControlHandler.addOrUpdateCookie();
        } catch (ApplicationException ex) {
            Environment.getInstance().getReportHandler().warning(ex);
        }

        AtAuthorization authorization = this.getClass().getAnnotation(AtAuthorization.class);
        if (null == authorization) {
            throw new ResourceException(
                    Status.SERVER_ERROR_INTERNAL, String.format(
                    "'%s' must be annotated with an Authorization.", this.getClass().toString()
            ));
        }

        WebServerConfig.AuthMode authMode = WebServices.getInstance().getConfiguration().getAuthMode();
        //noinspection StatementWithEmptyBody
        boolean authorized = true;
        if (authorization.required()) {
            switch (authMode) {
                case x509:
                    PKICredentials pkiInfo = new PKICredentials(this.getRequest(), this.getParameter("apiKey"));
                    authorized = pkiInfo.isAuthenticated();
                    if (!authorized) {
                        this.getRequest().setClientInfo(null);
                    } else {
                        this.setCredentials(pkiInfo);
                    }
                    break;
                case azureACS:
                    break;
                case basic:
                case digest:
                case form:
                    Environment.getInstance().getReportHandler().notImplemented();
                    authorized = false;
                    break;
	            case none:
	            default:
		            break;

            }
        }

        if (!authorized) {
            this.getRequest().getClientInfo().setAuthenticated(false);
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, "Client is not authorized.");
        }
    }

    /**
     * Get the value of a parameter from the request.
     *
     * @param param the name of the parameter.
     * @return The value of the parameter as a string.
     */
    public String getParameter(String param) {
        String result = null;
        try
        {
            Form form=this.getReference().getQueryAsForm();
            if (!form.isEmpty())
            {
                result=form.getValues(param);
            }
        }
        catch (Exception ignored){}
        return result;
    }

    // -------------------------- OTHER METHODS --------------------------

    @Delete
    public abstract Representation remove();

    @Get
    public abstract Representation retrieve();

    @Put
    public abstract Representation store(Representation representation);

    @Post
    public abstract Representation update(Representation representation);

    public Boolean getBoolean(String param) {
        String b = this.getParameter(param);
        return !StringX.isBlank(b) && StringX.equalsIgnoreCase(b, "true");
    }

    public Representation getRepresentation(JsonData result) {
        if(!this.getResponse().getStatus().equals(Status.SERVER_ERROR_INTERNAL)) {
            //noinspection NestedConditionalExpression
            this.getResponse().setStatus((null!=result) ? ((result.isEmpty()) ? Status.SUCCESS_NO_CONTENT : Status.SUCCESS_OK) : Status.CLIENT_ERROR_NOT_FOUND);
        }
        Method method = this.getRequest().getMethod();
        if((null!=this.getUserCredentials()) && Environment.getInstance().getConfig().isLoggable(method)){
            boolean success = this.getResponse().getStatus().isSuccess();
            UserActivity.logActivity(this.getUserCredentials(), LogEntry.OperationTypes.valueOf(method), this.getRequest().getOriginalRef(), success);
        }
        return (null!=result) ? result.toJsonRepresentation() : null;
    }

    public
    PolicyDecisionPoint getSecurityPolicy(DataVertex vertex)
    {
        vertex.setCredentials(this.getUserCredentials());
        return VertexHelper.getSecurityPolicy(vertex);
    }

    public void handleWebResource(DataVertex actual, DataVertex other, JsonData result, List<IQueryResultHandler> handlers) {
        BaseExtendedData.handleWebResource(this.getUserCredentials().getPrincipal(), actual, other, result, handlers);
    }

	public boolean authorized(DataVertex vertex, Permission.Types permission)
	{
		return vertex.isAuthorized(this.getUserCredentials(), permission);
	}

    public boolean elevatePermissions()
    {
        boolean result = this.getBoolean("pu");
        if(result){
            result = this.isPowerUser();
        }
        return result;
    }

    public UserCredentials getUserCredentials() {
        if((null==this.credentials) && !Environment.getInstance().getWebServer().credentialsRequired()){
            this.credentials = new AnonymousCredentials();
        }
        return this.credentials;
    }

    protected Collection<Personalization> getPersonalizations() throws ApplicationException {
        Collection<Personalization> results = new ArrayList<>();
        Person person = this.getPerson();
        if(null!=person){
            for (Personalization working : person.getPersonalizations()) {
                //noinspection UseBulkOperation
                results.add(working);
            }
        }
        return results;
    }

    protected Person getPerson(){
        Person result = null;
        if ((null!=this.getClientInfo()) && this.getClientInfo().isAuthenticated() &&
            (null!=this.getClientInfo().getUser()))
        {
            PKICredentials pkiInfo=new PKICredentials(this.getRequest(), this.getParameter("apiKey"));
            BasePrincipal principal=pkiInfo.getIdentity().getPrincipal();
            if (principal instanceof Person)
            {
                result=(Person) principal;
            }
        }
        return result;
    }

    public Personalization getCommonPersonalization()
        throws ApplicationException
    {
        Collection<Personalization> personalizations = this.getPersonalizations();
        Personalization result = null;
        for(Personalization working: personalizations){
            if(StringX.equalsIgnoreCase(working.fetchTitle().getValue(), "common")){
                result = working;
                break;
            }
        }
        if(null==result){
            Person person = this.getPerson();
            if(null!=person)
            {
                result=new Personalization();
                result.fetchTitle().setValue("common");
                person.getPersonalizations().add(result);
            }
        }

        if((null!=result) && result.fetchCreatedWhen().getValue().isBefore(DateTime.parse("2017-09-17T13:00:00.000-04:00"))){
            Set<Class<? extends Personalization>> types = Environment.getInstance().getReflections().getSubTypesOf(Personalization.class);
            Environment.getInstance().getDataStore().drop(Personalization.class);
            for(Class<? extends Personalization> type: types){
            	Environment.getInstance().getDataStore().drop(type);
            }
            result = this.getCommonPersonalization();
        }

        return result;
    }

    public boolean isPowerUser()
    {
        return ScopedConfiguration.getInstance().isPowerUser(this.getUserCredentials().getPrincipal());
    }

    public DataVertex getVertex()
    {
        String webId=this.getAttribute("webId");
        Class<? extends DataVertex> store=BaseDomain.getDomainType(this.getAttribute("domain"));
        return this.getVertex(store, webId);
    }

	public DataVertex getVertex2()
	{
		String webId=this.getAttribute("webId2");
		Class<? extends DataVertex> store=BaseDomain.getDomainType(this.getAttribute("domain2"));
		return this.getVertex(store, webId);
	}


	public DataVertex getVertex(Class<? extends DataVertex> store, String id) {
		DataVertex result = null;
		try
		{
			Class<? extends DataVertex> fStore = store;
			if((null==fStore) && StringX.startsWith(id, "/domains")){
				String key = StringX.replace(id, "/domains/", "");
				key = StringX.getFirst(key, "/");
				fStore = BaseDomain.getDomainType(key);
			}

			String fId = Environment.getInstance().getDataStore().formatDataStoreId(id);

			if(ClassX.isKindOf(store, Edge.class)){
				BaseQueryBuilder qb = Environment.getInstance().getIndexStore().getQueryBuilder(store, null, 0, 1);
				qb.filter(BaseQueryBuilder.Operators.must, IDataStore.DATA_STORE_ID, BaseQueryBuilder.StringTypes.raw, id);
				Edge edge = qb.execute().getFirst();
				if(null!=edge){
					result = edge;
				}
			}
			else
			{
				result=VertexFactory.getInstance().get(fStore, fId);
			}

			if(null!=result){
				result.setCredentials(this.getUserCredentials());
			}
		}
		catch (Exception ex)
		{
			Environment.getInstance().getReportHandler().warning(ex);
		}
		return result;
	}

	public DataVertex getVertex(String id) {
		return this.getVertex(null, id);
	}

    public void setCredentials(UserCredentials credentials) {
        this.credentials = credentials;
    }

// Getters and setters
    /**
     * Get access control handler for this resource.
     *
     * @return Access control handler.
     */
    public AccessControlHandler getAccessControlHandler() {
        return this.accessControlHandler;
    }

    /**
     * Get Environment instance.
     *
     * @return Context instance.
     */
    public Environment getEnvironment() {
        return this.environment;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String[] getLanguages() {
	    StringBuffer sb = new StringBuffer();
        String languages = this.getHeader("Accept-Language");
        sb.append(StringX.isBlank(languages) ? "en" : languages);

        String[] results = StringX.split(languages, ",");
        if(null!=results)
        {
            for (String result : results)
            {
                String language = StringX.getFirst(result, ";");
                if (sb.length() > 0)
                {
                    sb.append(",");
                }
                sb.append(language);
            }
        }
        return StringX.split(sb.toString(), ",");
    }

    @SuppressWarnings("SameParameterValue")
    public String getHeader(String key){
        String result = null;
        try {
            Series series = (Series) this.getRequest().getAttributes().get("org.restlet.http.headers");
            result = series.getFirstValue(key);
        }
        catch (Exception ignored){}
        return result;
    }

    public Integer getStart() {
        Integer result = this.getInteger("start");
        return (null==result) ? 0 : result;
    }

    public Integer getInteger(String param) {
        Integer result = null;
        if(!StringX.isBlank(param)){
            try{
                result = Integer.parseInt(this.getParameter(param));
            }
            catch (Exception ignored){}
        }
        return result;
    }

    public Integer getLimit() {
        Integer result = this.getInteger("limit");
        return (null==result) ? BaseServerResource.DEFAULT_QUERY_LIMIT : result;
    }

    public DataLevel getLevel() {
        String sLevel =this.getParameter("level");
        DataLevel result = DataLevel.Summary;
        if(!StringX.isBlank(sLevel)) {
            if (null != DataLevel.parse(sLevel)) {
                result = DataLevel.parse(sLevel);
            }
        }
        return result;
    }

    public JsonData getJsonContent() {
        boolean found = false;
        JsonData result = null;
        try
        {
            CharacterSet characterSet=this.getRequest().getEntity().getCharacterSet();
            Collection<String> allowed=WebServices.getInstance().getConfiguration().getAllowedCharacterEncodings();
            if ((null!=characterSet) && (null!=allowed))
            {
                for (String type : allowed)
                {
                    CharacterSet allow=CharacterSet.valueOf(type);
                    found=Objects.equals(allow, characterSet);
                    if (found)
                    {
                        break;
                    }
                }
            }
            else
            {
                found=true;
            }

            Representation representation=this.getRequest().getEntity();
            if (found && (null!=representation))
            {
                try
                {
                    result=new JsonData(representation.getText());
                    if (!result.isValid())
                    {
                        result=null;
                    }
                }
                catch (Exception ignored){}
            }
        }
        catch (Exception ignored){}
        return result;
    }
}
