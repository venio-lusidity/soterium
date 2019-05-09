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

package com.lusidity.services.security.authentication.pki;


import com.lusidity.Environment;
import com.lusidity.blockers.VertexBlocker;
import com.lusidity.data.types.names.WesternName;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.acs.security.Identity;
import com.lusidity.domains.acs.security.SystemCredentials;
import com.lusidity.domains.acs.security.loging.UserActivity;
import com.lusidity.domains.book.record.log.LogEntry;
import com.lusidity.domains.people.Person;
import com.lusidity.framework.java.ClassX;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.text.StringX;
import com.lusidity.services.server.WebServices;
import com.lusidity.system.security.UserCredentials;
import org.joda.time.DateTime;
import org.restlet.Request;
import org.restlet.data.ClientInfo;
import org.restlet.security.User;

import java.net.URI;

public class PKICredentials implements UserCredentials {

    private String apiKey = "";
    private String referrer = "";
    private String provider = "x509";
    private String origin = "";
    private String firstName = null;
    private String middleName = null;
    private String lastName = null;
    private String identifier = null;
    private  Identity identity = null;
    private final ClientInfo clientInfo;
    private String organizationalUnit1 = "";
    private String organizationalUnit2 = "";
    private String organizationalUnit3 = "";
    private String organization = "";
    private String country="";
    private BasePrincipal principal=null;
    private boolean serverCertificate = false;
    private String commonName = null;
    private final transient Request request;
    private static VertexBlocker blocker = new VertexBlocker();
    private LogEntry.OperationTypes activity = LogEntry.OperationTypes.none;

    public PKICredentials(Request request, String apiKey) {
        super();
        this.clientInfo = request.getClientInfo();
        this.referrer = (null!=request.getReferrerRef()) ? request.getReferrerRef().toString() : null;
        this.origin = this.clientInfo.getAddress();
        this.request = request;
        if(!StringX.isBlank(apiKey)){
            this.load(apiKey);
        }
        else
        {
            this.load();
        }
    }

    public void log()
    {
        Identity identity=null;
        try
        {
            DateTime current=DateTime.now();
            identity = this.getIdentity();
            if(null!=identity)
            {
                if(Environment.getInstance().getConfig().isDebug()){
                    Environment.getInstance().getReportHandler().info("PKICredentials 74: logging started for %s", identity.fetchTitle().getValue());
                }
                PKICredentials.blocker.start(identity);
                identity.setCredentials(this);
                if (null!=identity)
                {
                    boolean success = false;
                    String msg = null;
                    if(identity.fetchDeprecated().getValue()){
                        if(Environment.getInstance().getConfig().isDebug()){
                            Environment.getInstance().getReportHandler().info("PKICredentials 83: identity deprecated for %s", identity.fetchTitle().getValue());
                        }
                        identity.fetchLastAttempt().setValue(current);
                        if(!identity.save() && Environment.getInstance().getConfig().isDebug())
                        {
                            Environment.getInstance().getReportHandler().info("PKICredentials 89: identity did not save for %s", identity.fetchTitle().getValue());
                        }
                        else if (Environment.getInstance().getConfig().isDebug()){
                            Environment.getInstance().getReportHandler().info("PKICredentials 92: identity did save for %s", identity.fetchTitle().getValue());
                        }
                        msg = "%s attempted to log in at %s and was not able to as their account is disabled.";
                    }
                    else if ((null==identity.getLastLoggedIn()) || current.isAfter(identity.getLastLoggedIn().plusMinutes(Environment.getInstance().getConfig().getUserLogInterval())))
                    {
                        identity.fetchLastLoggedIn().setValue(current);
                        identity.fetchLastAttempt().setValue(current);
                        identity.save();
                        msg = "%s last logged in at %s.";
                        success = true;
                    }
                    if(!StringX.isBlank(msg) && (ClassX.isKindOf(this.getPrincipal(), Person.class)))
                    {
                        Person person=(Person) this.getPrincipal();
                        person.setCredentials(SystemCredentials.getInstance());
                        UserActivity
                            .logActivity(this.getPrincipal(), LogEntry.OperationTypes.login, String.format(msg, person.fetchTitle().getValue(), current.toString()), success);
                    }
                    else if(!StringX.isBlank(this.apiKey)){
                        UserActivity
                            .logActivity(this.getPrincipal(), LogEntry.OperationTypes.login, String.format("API Key: %s", this.apiKey), success);
                    }
                }
            }
            else if(Environment.getInstance().getConfig().isDebug()){
                Environment.getInstance().getReportHandler().info("PKICredentials 117: identity is null");
            }
        }
        catch (Exception ex){
            Environment.getInstance().getReportHandler().critical(ex);
        }
        finally
        {
            if (null!=identity)
            {
                PKICredentials.blocker.finished(identity);
            }
        }
    }

    private void load(String apiKey)
    {
        try
        {
            this.apiKey = apiKey;
            this.serverCertificate = true;
            User user = this.clientInfo.getUser();
            String txt = user.getIdentifier();
            if(StringX.isBlank(txt)){
                txt = user.getName();
            }

            if(!StringX.isBlank(txt)){
                String[] parts = StringX.split(txt, ",");
                if(null!=parts)
                {
                    int on=0;
                    for (String part : parts)
                    {
                        String[] keyValuePair = StringX.split(part, "=");

                        if ((null!=keyValuePair) && (keyValuePair.length==2))
                        {
                            String key = keyValuePair[0].toLowerCase();
                            String value = keyValuePair[1];
                            if (StringX.equals(key, "cn"))
                            {
                                this.identifier = Identity.composeKey("x509", value);
                                this.commonName = value;
                            }
                            if (StringX.equals(key, "ou"))
                            {
                                switch (on)
                                {
                                    case 1:
                                        this.organizationalUnit1 = value;
                                        break;
                                    case 2:
                                        this.organizationalUnit2 = value;
                                        break;
                                    case 3:
                                        this.organizationalUnit3 = value;
                                        break;
                                }
                            }
                            if (StringX.equals(key, "o"))
                            {
                                this.organization = value;
                            }
                            if (StringX.equals(key, "o"))
                            {
                                this.country = value;
                            }
                        }
                        on++;
                    }
                }
            }

        }
        catch (Exception ex){
            Environment.getInstance().getReportHandler().critical(ex);
        }
    }

    private void load() {
        User user = this.clientInfo.getUser();
        String txt = user.getIdentifier();
        if(StringX.isBlank(txt)){
            txt = user.getName();
        }

        if(!StringX.isBlank(txt)){
            String[] parts = StringX.split(txt, ",");
            int on = 0;
            if(null!=parts)
            {
                for (String part : parts)
                {
                    String[] keyValuePair = StringX.split(part, "=");

                    if ((null!=keyValuePair) && (keyValuePair.length==2))
                    {
                        String key = keyValuePair[0].toLowerCase();
                        String value = keyValuePair[1];
                        if (StringX.equals(key, "cn"))
                        {
                            String[] names = StringX.split(value, ".");
                            if(null!=names)
                            {
                                int size = names.length;
                                this.identifier = names[size-1];
                                if(StringX.isBlank(this.identifier) || (this.identifier.length()<=3)){
                                    break;
                                }
                                StringBuilder name =new StringBuilder();
                                for (int i = 1; i<(size-1); i++)
                                {
                                    if (name.length()>0)
                                    {
                                        name.append(" ");
                                    }
                                    name.append(StringX.toTitle(names[i]));
                                }
                                name.append(String.format(" %s", StringX.toTitle(names[0])));
                                WesternName westernName = new WesternName(name.toString());
                                this.firstName = westernName.getFirstName();
                                this.middleName = westernName.getMiddleName();
                                this.lastName = westernName.getLastName();
                            }
                        }
                        if (StringX.equals(key, "ou"))
                        {
                            switch (on)
                            {
                                case 1:
                                    this.organizationalUnit1 = value;
                                    break;
                                case 2:
                                    this.organizationalUnit2 = value;
                                    break;
                                case 3:
                                    this.organizationalUnit3 = value;
                                    break;
                            }
                        }
                        if (StringX.equals(key, "o"))
                        {
                            this.organization = value;
                        }
                        if (StringX.equals(key, "o"))
                        {
                            this.country = value;
                        }
                    }
                    on++;
                }
            }
        }
    }

	@Override
	public Request getRequest()
	{
		return this.request;
	}

	@Override
    public Identity getIdentity(){
        if(null==this.identity){
            try{
                this.identity = Identity.get(this.provider, this.identifier);
            }
            catch (Exception ex){
                Environment.getInstance().getReportHandler().warning(ex);
            }
        }
        return this.identity;
    }

    @Override
    public Boolean isRegistered(){
        return ((null!=this.getIdentity()));
    }

    @Override
    public Boolean isValidated() {
        boolean result = this.clientInfo.isAuthenticated() && !StringX.isBlank(this.identifier);
        if(result) {
            if(!WebServices.getInstance().getConfiguration().getAllowedClientCertificateTypes().isEmpty()){
                result = (this.allowed(this.getOrganization()) ||
                           this.allowed(this.getOrganizationalUnit1()) ||
                           this.allowed(this.getOrganizationalUnit2()) || this.allowed(this.getOrganizationalUnit3()))
                    || (this.isServerCertificate() && this.allowed(this.getCommonName()));
            }
        }
        return result;
    }

    private boolean allowed(String allow) {
        return WebServices.getInstance().getConfiguration().getAllowedClientCertificateTypes().contains(allow.toLowerCase());
    }

    @Override
    public Boolean isAuthenticated() {
        return ((null!=this.getIdentity()) && !this.getIdentity().fetchDeprecated().getValue() && (this.getIdentity().hasStatus(Identity.Status.approved)))
               && this.isValidated()
               && this.isRegistered();
    }

    @Override
    public Boolean isServerCertificate()
    {
        return this.serverCertificate;
    }

    @Override
    @SuppressWarnings("UnusedDeclaration")
    public ClientInfo getClientInfo() {
        return this.clientInfo;
    }

    @Override
    public String getCommonName()
    {
        return this.commonName;
    }

    @Override
    public String getFirstName() {
        return this.firstName;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getLastName() {
        return this.lastName;
    }

    @Override
    public String getMiddleName() {
        return this.middleName;
    }

    @Override
    @SuppressWarnings("UnusedDeclaration")
    public String getCountry() {
        return this.country;
    }

    public String getOrganization() {
        return this.organization;
    }

    @Override
    @SuppressWarnings("UnusedDeclaration")
    public String getOrganizationalUnit1() {
        return this.organizationalUnit1;
    }

    @Override
    public String getOrigin() {
        return this.origin;
    }

    @Override
    public String getReferrer() {
        return this.referrer;
    }

    @Override
    @SuppressWarnings("UnusedDeclaration")
    public String getOrganizationalUnit2() {
        return this.organizationalUnit2;
    }

    public String getOrganizationalUnit3() {
        return this.organizationalUnit3;
    }

    @Override
    @SuppressWarnings("UnusedDeclaration")
    public String getProvider() {
        return this.provider;
    }

    @Override
    public URI getPrincipalUri() {
        URI result = null;
        if(null!=this.getIdentity()) {
            try {
                BasePrincipal principal = this.getIdentity().getPrincipal();
                result = principal.getUri();
            }
            catch (Exception ex){
                Environment.getInstance().getReportHandler().severe(ex);
            }
        }
        return result;
    }

    @Override
    public JsonData toJson() {
        JsonData result = new JsonData();
        if(this.isServerCertificate()){
            result.put("commonName", this.getCommonName());
        }
        else{
            result.put("firstName", this.getFirstName());
            result.put("lastName", this.getLastName());
            result.put("middleName", this.getMiddleName());
        }
        result.put("identifier", this.getIdentifier());
        if(this.isRegistered() && (null!=this.getIdentity()))
        {
            result.put("status", this.getIdentity().fetchStatus().getValue());
        }
        result.put("authenticated", this.isAuthenticated());
        result.put("validated", this.isValidated());
        result.put("registered", this.isRegistered());
        result.put("principalUri", this.getPrincipalUri());
        return result;
    }

    @Override
    public UserActivity.OperationTypes getActivity()
    {
        return this.activity;
    }

    @Override
    public void setActivity(UserActivity.OperationTypes activity)
    {
        this.activity = activity;
    }

    @Override
    public BasePrincipal getPrincipal()
    {
        if((null!=this.getIdentity()) && (null==this.principal)){
            this.principal = this.getIdentity().getPrincipal();
        }
        if(null!=this.principal){
            this.principal.setCredentials(this);
        }
        return this.principal;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if(obj instanceof PKICredentials){
            PKICredentials other = (PKICredentials)obj;
            result = this.getIdentifier().equals(other.getIdentifier());
        }
        return result;
    }
}
