/*
 * Copyright (c) 2008-2012, Venio, Inc.
 * All Rights Reserved Worldwide.
 *
 * This computer software is protected by copyright law and international treaties.
 * It may not be duplicated, reproduced, distributed, compiled, executed,
 * reverse-engineered, or used in any other way, in whole or in part, without the
 * express written consent of Venio, Inc.
 *
 * Portions of this computer software also embody trade secrets, patents, and other
 * protected intellectual property of Venio, Inc. and third parties and are subject to
 * applicable laws, regulations, treaties, agreements, and other legal mechanisms.
 */

package com.lusidity.services.server;

import com.lusidity.Environment;
import com.lusidity.cache.rest.ResponseExpiredCache;
import com.lusidity.framework.annotations.AtClassExclude;
import com.lusidity.framework.annotations.AtWebResource;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.security.ObfuscateX;
import com.lusidity.framework.text.StringX;
import com.lusidity.server.IServer;
import com.lusidity.services.security.Blocker;
import com.lusidity.services.security.Tracer;
import com.lusidity.services.security.authentication.pki.PKIAuthenticator;
import com.lusidity.services.server.resources.BaseServerResource;
import org.restlet.*;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.service.LogService;
import org.restlet.util.Series;

import java.time.LocalTime;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

@AtClassExclude
public
class WebServices
    extends Application
    implements IServer {
// ------------------------------ FIELDS ------------------------------

    private static final String DESCRIPTION = "EmailTemplate Services";
    private static final String OWNER = "EmailTemplate";
    private static final String AUTHOR = "EmailTemplate";
    private static final int DEFAULT_PORT = 8443;


    private static final Protocol DEFAULT_PROTOCOL = Protocol.HTTPS;
    private static WebServices instance = null;
    private WebServerConfig config = null;
    private boolean logging = true;
    private boolean opened = false;
    private Component component = null;
    private ResponseExpiredCache responseExpiredCache = null;

    // --------------------------- CONSTRUCTORS ---------------------------

    public WebServices() {
        super();
        this.setName(this.getClass().getName());
        this.setDescription(WebServices.DESCRIPTION);
        this.setOwner(WebServices.OWNER);
        this.setAuthor(WebServices.AUTHOR);
        WebServices.instance = this;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * Get singleton instance of the WebService.
     *
     * @return Environment.
     */
    public static WebServices getInstance() {
        return WebServices.instance;
    }


    @Override
    public boolean restart() {
        return false;
    }

    @Override
    public
    void setOffline(boolean offline)
    {}

    @Override
    public
    boolean isOffline()
    {
        return false;
    }

    @Override
    public int getPort() {
        return (null != this.config) ? this.config.getPort() : -1;
    }


// -------------------------- OTHER METHODS --------------------------

    @SuppressWarnings("RefusedBequest")
    @Override
    public Restlet createInboundRoot() {
        //
        //  Set-up media types
        //
        this.setupMediaTypes();

        //
        //  Set-up blocking by inbound host
        //
        Blocker blocker = this.setAuthorizedLocations(this.getContext());

        Environment.getInstance().getReportHandler().say("Creating endpoints.");

        //
        //  Set-up routing and attach server resources
        //
        Router router = new Router(this.getContext());

        Collection<Class<? extends BaseServerResource>> resources = Environment.getInstance().getReflections().getSubTypesOf(BaseServerResource.class);

        int total = 0;
        if (null != resources) {
            for (Class<? extends BaseServerResource> resource : resources) {
                AtWebResource wra = resource.getAnnotation(AtWebResource.class);
                if (null == wra) {
                    Environment.getInstance().getReportHandler().severe("Missing WebResourceAnnotation, %s.", resource.getName());
                } else {
                    if (wra.matchingMode() > 6) {
                        Environment.getInstance().getReportHandler().severe("Matching mode will not be set, " +
                                                                            "expect 1 (WebResourceAnnotation.MODE_BEST_MATCH) or " +
                                                                            "2 (WebResourceAnnotation.MODE_FIRST_MATCH).");
                    }
                    String path = wra.pathTemplate();
                    if (!StringX.isBlank(path)) {
                        if (!StringX.startsWithIgnoreCase(path, "/svc/")) {
                            path = StringX.removeStart(path, "/");
                            path = String.format("/svc/%s", path);
                        }
                        if ((wra.matchingMode()>0) && (wra.matchingMode()<7)) {
                            router.attach(path, resource, wra.matchingMode());
                        } else {
                            router.attach(path, resource);
                        }
                        Environment.getInstance().getReportHandler().say("REST Service: %s is online.", path);
                        total++;
                    }
                }
            }
        }

        Environment.getInstance().getReportHandler().say("Created %d endpoints.", total);

        if (!StringX.isBlank(this.getConfiguration().getWebPath())) {
            Directory directory = new Directory(this.getContext(), this.getConfiguration().getWebPath());
            directory.setDeeplyAccessible(true);
            directory.setListingAllowed(true);
            router.attach("/", directory);
            //this.getContext().setClientDispatcher(new Client(this.getContext(), Protocol.FILE));

            Environment.getInstance().getReportHandler().info("Services web directory is set.");
        } else {
            Environment.getInstance().getReportHandler().warning("Services web directory is not set.");
        }

        blocker.setNext(router);

        return blocker;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);
    }

    /**
     * Set-up media types.
     */
    private void setupMediaTypes() {
        this.getMetadataService().addExtension("form", MediaType.APPLICATION_WWW_FORM);
    }

    /**
     * Blocker that contains a list of location that are authorized access.
     *
     * @param context The Restlet context.
     */
    private Blocker setAuthorizedLocations(Context context) {
        //Note: Could implement an API Key infrastructure here.
        Blocker blocker = new Blocker(context);
        if (!this.config.getAllowedURIs().isEmpty()) {
            blocker.setAllowableURIs(this.config.getAllowedURIs());
        }
        // Gets the requesting location
        blocker.setNext(new Tracer(context));

        return blocker;
    }

    /**
     * Launch services.
     *
     * @throws ApplicationException
     */
    @Override
    public boolean start(Object... params)
	    throws Exception
    {
	    try {
            if (params[0] instanceof WebServerConfig) {
                this.config = (WebServerConfig) params[0];
                Engine.setLogLevel(this.config.getLogLevel());
                Engine.setRestletLogLevel(this.config.getRestletLogLevel());

                LocalTime expiresAt = LocalTime.MIDNIGHT;
                String lt =this.config.getCacheExpiresAt();
                if(!StringX.isBlank(lt))
                {
                    expiresAt=LocalTime.parse(lt);
                }
                this.responseExpiredCache = new ResponseExpiredCache(expiresAt);
                this.responseExpiredCache.setEnabled(!StringX.isBlank(lt));

                this.component = new Component();

                LogService logService = new LogService(this.logging);

                this.component.setLogService(logService);

                Server server = this.component.getServers().add(this.config.getProtocol(), this.config.getPort());
                Series<Parameter> parameters = server.getContext().getParameters();
                this.component.getClients().add(Protocol.FILE);

                if (this.config.getProtocol().equals(Protocol.HTTPS)) {
                    String kp =  this.config.getKeystorePwd();
                    String tp = this.config.getTrustedPwd();
                    String seed = Environment.getInstance().getConfig().getSetting("encryption_key");
                    kp = ObfuscateX.decrypt(kp, seed, null);
                    tp = ObfuscateX.decrypt(tp, seed, null);
                    parameters.add("keystorePath", this.config.getKeystore().getCanonicalPath());
                    parameters.add("keystorePassword", kp);
                    parameters.add("keyPassword", kp);
                    parameters.add("keystoreType", (StringX.endsWithIgnoreCase(this.config.getKeystore().getCanonicalPath(), ".jks") ? "JKS" : "pkcs12"));
                    if ((null!=this.config.getTrusted()) && this.config.getTrusted().isFile()) {
                        parameters.add("truststorePath", this.config.getTrusted().getCanonicalPath());
                        parameters.add("truststorePassword", tp);
                    }
                    // Added for Client Certificates : causes a connection refused
                    if (this.config.getAuthMode() == WebServerConfig.AuthMode.x509) {
                        parameters.add("sslContextFactory", "org.restlet.engine.ssl.DefaultSslContextFactory");
                        parameters.add("needClientAuthentication", "true");
                        parameters.add("wantClientAuthentication", "true");
                    }
                }



                if (this.config.getAuthMode() == WebServerConfig.AuthMode.x509) {
                    // Added for Client Certificates
                    PKIAuthenticator guard = new PKIAuthenticator(server.getContext());
                    guard.setNext(this);

                    this.component.getDefaultHost().attachDefault(guard);
                } else {
                    this.component.getDefaultHost().attachDefault(this);
                }
                this.component.start();
                this.opened = true;
                Environment.getInstance().getReportHandler().say("REST Services started on port: %d.", this.config.getPort());
            }
        } catch (Exception ex) {
            throw new ApplicationException("%s %s", ex,
                "If the port is currently in use try this command, ss -l -p -n | grep \"8443\", to find and stop the process.");
        }

        return this.isOpened();
    }

    @Override
    public void close() {
        if (this.component != null) {
            try {
                this.component.stop();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public boolean isOpened() {
        return (null != this.component) && this.opened;
    }

    @Override
    public boolean credentialsRequired()
    {
        return (null==this.config.getAuthMode()) || (this.config.getAuthMode()!=WebServerConfig.AuthMode.none);
    }

    public ResponseExpiredCache getResponseExpiredCache()
    {
        return this.responseExpiredCache;
    }

    public void setLogging(boolean logging) {
        Level level = logging ? Level.WARNING : Level.OFF;
        Engine.setLogLevel(level);
        Engine.setRestletLogLevel(level);

        Logger logger = this.getLogger();
        logger.setLevel(level);

        this.logging = logging;
    }

    public Protocol getProtocol() {
        return this.config.getProtocol();
    }

    public WebServerConfig getConfiguration() {
        return this.config;
    }

    public static JsonData getEndPoints(){
        JsonData results = JsonData.createArray();
        Collection<Class<? extends BaseServerResource>> resources = Environment.getInstance().getReflections().getSubTypesOf(BaseServerResource.class);
        if (null != resources) {
            for (Class<? extends BaseServerResource> resource : resources) {
                AtWebResource wra = resource.getAnnotation(AtWebResource.class);
                if(null!=wra){
                    JsonData result = JsonData.createObject();
                    result.put("path", wra.pathTemplate());
                    result.put("matchMode", wra.matchingMode());
                    result.put("name", resource.getSimpleName());
                }
            }
        }

        return results;
    }
}