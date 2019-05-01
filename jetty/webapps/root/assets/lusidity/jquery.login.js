;(function ($) {
    //Object Instance
    $.login = function(el, options) {
        var state = el,methods = {};
        state.container = $(state[0]);
        state.opts = $.extend({}, $.environment.defaults, options);
        // Store a reference to the environment object
        el.data("login", state);
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.last = "/enclaves";
        state.register = false;
        var ttl = 10;

        var svc = lusidity.environment("host-primary")+"/files/docs/";
        svc = $.jCommon.string.replaceAll(svc, "/svc/", "/");
        var _mailTo = 'disa.meade.re.mbx.rmk@mail.mil';
        var _instruct = svc + "RMK Registration Specific Instructions.pdf";
        var _dd2875 = svc + "DD Form 2875_RMK.pdf";
        var _df787 =  svc + "DISA Form 787.pdf";

        // Private environment methods
        methods = {
            init: function() {
                var path = window.location.pathname.toLowerCase();
                if(!methods.isHome()) {
                    switch (path) {
                        case "/notification":
                        case  "/notification.html":
                            methods.notifications.display();
                            break;
                        case "/browser":
                        case "/browser.html":
                            break;
                        default:
                            methods.authenticate();
                            break;
                    }
                }

                if(state.opts.logout){
                    state.opts.logout.on('click', function () {
                        methods.logout();
                        window.location = "/";
                    });
                }

                if(methods.isHome()){
                    var e = jQuery.Event("homePage");
                    lusidity.trigger(e);
                }
            },
            getCName: function () {
                var u = $.jCommon.url.create(window.location.href);
                var c = $.jCommon.string.replaceAll(u.host, '\\.', '_');
                return c + '_loggedIn';
            },
            authenticate: function() {
                var s = function(data){
                    methods.navigate(data);
                };
                $.htmlEngine.request(state.opts.loginUrl, s, s, null, "get", false);
            },
            notifications: {
                navigate: function (data) {
                    window.location = "/notification?status=" + data.status;
                },
                display: function () {
                    var e = jQuery.Event("error");
                    lusidity.trigger(e);
                    var url = $.jCommon.url.create(window.location);
                    var status = parseInt(url.getParameter('status'));
                    if(status!==503) {
                        var s = function (data) {
                            if(!data.ping){
                               methods.notifications.navigate({status: 503});
                            }
                        };
                        var f = function () {};
                        $.htmlEngine.request("/ping", s, f, null, 'get', true);
                    }
                    var s = methods.notifications.status(parseInt(status));
                    $('.info-block').append(s.msg);
                },
                status: function (code, msg) {
                    var r;
                    var un = '</span><br/><br/><p style="color: #333 !important;"><strong>Unfortunately</strong> if you entered the wrong <strong>pin</strong> or <strong>cancelled</strong> the certificate selection, to login again you must close and reopen your browser.' +
                        '<br/>We apologize for this browsers limitation of CAC authentication session clearing.</p>';
                    switch (code) {
                        case 3:
                            r = {
                                msg: 'Your account has been disabled. To request that your account be reactivated click <a href="mailto:disa.meade.re.mbx.rmk@mail.mil?subject=Reactivate Account&body=Please fill out the below information.%0D%0A%0D%0AYour full name:%0D%0AEmail used to register your account:">here</a>.',
                                cls: 'red'
                            };
                            break;
                        case 1:
                            r = {
                                msg: 'Your request for an account has been submitted. Please ensure you send your <a href="'+ _dd2875 +'" target="_blank">DD Form 2875</a> and <a href="'+ _df787 +'" target="_blank">DISA Form 787</a> to the RMK Team using the RMK Group Email at <a href="mailto:'+ _mailTo + '">disa.meade.re.mbx.rmk@mail.mil</a>. Please put  <i>"RMK - New Account - Your Name" </i> in the Subject line of your email.' +
                                '<br/><br/>Once all documents have been provided, please allow one to three business days for review. Upon account approval, you will be required to access this site at least once every 35 days to keep your account active. If you fail to do so, your account will be disabled.',
                                cls: 'blue'
                            };
                            break;
                        case 2:
                            var url = $.jCommon.url.create(window.location.href);
                            var msg = 'Services are currently unavailable today due to maintenance.';
                            if(url.hasParam('msg')){
                                msg = atob(url.getParameter('msg'));
                            }
                            r = {
                                msg: msg,
                                cls: 'yellow'
                            };
                            var s = function (data) {};
                            $.htmlEngine.request("/log/msg", s, s, {message: "User redirected due to services being unavailable for maintenance."}, 'post');
                            break;
                        case 204:
                            r = {
                                msg: 'The page requested has no content.',
                                cls: 'red'
                            };
                            break;
                        case 401:
                            r = {
                                msg: '<strong>Unauthorized</strong>: You are not authorized to access this site.  If you have registered your account it may not have been approved yet.' +
                                '<br/><br/>You may have been redirected to this notification due to inactivity and have been logged out, to log back in please click <a href="/enclaves">here</a>' +
                                un,
                                cls: 'red'
                            };
                            break;
                        case 403:
                            var url = $.jCommon.url.create(window.location.href);
                            var msg = '<strong>Forbidden</strong>: You are not authorized to access this site.' + un;
                            if(url.hasParam('msg')){
                                msg = atob(url.getParameter('msg'));
                            }
                            r = {
                                msg: msg,
                                cls: 'red'
                            };
                            break;
                        case 503:
                            r = {
                                msg: 'Services have been interrupted, we apologize for the inconvenience, please <a href="/enclaves">try again</a> in a little while.' +
                                un,
                                cls: 'red'
                            };
                            break;
                        case 900:
                            r = {
                                msg: 'RMK is conducting a data audit and is currently unavailable.',
                                cls: 'yellow'
                            };
                            break;
                        case 500:
                        case 0:
                        default:
                            r = {
                                msg: 'Services have been interrupted.  We apologize for the inconvenience, please <a href="/enclaves">try again</a> in a little while.' +
                                un,
                                cls: 'red'
                            };
                            break;
                    }
                    return r;
                }
            },
            isHome: function () {
              var r = $.jCommon.url.create(window.location);
              return ($.jCommon.string.equals(r.relativePath, '/') || $.jCommon.string.equals(r.relativePath, 'index'));
            },
            isTest: function () {
                var r = $.jCommon.url.create(window.location);
                return $.jCommon.string.startsWith(r.relativePath, '/pages/test');
            },
            isReferrerHome: function () {
                var dr = document.referrer;
                var r = ($.jCommon.string.empty(dr)) ? null : $.jCommon.url.create(dr);
                return (undefined!==r && null!==r) && ($.jCommon.string.equals(r.relativePath, '/') || $.jCommon.string.equals(r.relativePath, 'index'))
            },
            login: function () {
                var s = function (data) {
                    if(data.authenticated){
                        $.jCommon.cookie.create(methods.getCName(), true, 10);
                        $.login.redirect();
                        var n = 0;
                        function c() {
                            n++;
                            var loggedIn = $.jCommon.cookie.read(methods.getCName());
                            if(!loggedIn) {
                                methods.notifications.navigate({status: 401});
                            }
                            else{
                                window.setTimeout(c, 10000);
                            }
                        }
                        c();
                    }
                    else{
                        methods.register(data);
                    }
                };
                var f = function () {
                    methods.notifications.navigate({status: 500});
                };
                $.htmlEngine.request(state.opts.loginUrl, s, f, null, "get", false);
            },
            logout: function(options) {
                $.jCommon.cookie.erase(methods.getCName());
                $.environment.close();
            },
            register: function (data) {
                if (data.validated && data.registered) {
                    if($.jCommon.string.equals(data.status, 'waiting')) {
                        methods.notifications.navigate({status: 1});
                    }
                    else if(!$.jCommon.string.equals(data.status, 'approved')){
                        methods.notifications.navigate({status: 3});
                    }
                    else{
                        methods.notifications.navigate({status: 500});
                    }
                }
                else if ((data.validated && !data.registered) || state.register) {
                    var s = function (data) {
                        if ((data.authenticated && data.online) || state.register) {
                            window.location = "/accounts/register";
                        }
                        else {
                            methods.notifications.navigate({status: data.online ? 403 : 503});
                        }
                    };
                    var f = function (jqXHR, textStatus, errorThrown) {
                        methods.notifications.navigate({status: 500});
                    };
                    $.htmlEngine.request('/ping', s, f, null, 'get', true);
                }
                else {
                    methods.notifications.navigate({status: 0});
                }
            },
            navigate: function(data, textStatus, errorThrown){
                var loggedIn = $.jCommon.cookie.read(methods.getCName());
                var dr = document.referrer;
                var r = ($.jCommon.string.empty(dr)) ? null : $.jCommon.url.create(dr);
                var c = $.jCommon.url.create(window.location);
                if(!$.jCommon.string.equals(loggedIn, 'true', true)){
                    if(!$.jCommon.string.contains(c.relativePath, ['notification','register','/','index'], true)){
                        methods.notifications.navigate({status: 401});
                    }
                }
                var path = window.location.pathname.toLowerCase();
                var e;

                function go() {
                    switch (path) {
                        case "/pages/notifications/notification.html":
                        case "/notification":
                        case "/notification.html":
                            methods.logout();
                            e = jQuery.Event("error");
                            break;
                        case "test":
                        case "/spring.html":
                            break;
                        case "/accounts/register":
                        case "/accounts/register.html":
                            if (!$.jCommon.is.object(data)) {
                                methods.notifications.navigate({status: 403});
                            }
                            else {
                                e = jQuery.Event("authorized");
                                e.auth = data;
                                if (data.authenticated && !state.register) {
                                    $.login.redirect();
                                }
                                else if ((data.validated && !data.registered) || state.register) {
                                    e = jQuery.Event("authorized");
                                    e.auth = data;
                                }
                                else {
                                    methods.notifications.navigate({status: 0});
                                }
                            }
                            break;
                        default:
                            if (!$.jCommon.is.object(data)) {
                                methods.notifications.navigate({status: 403});
                            }
                            else if (!data.connected) {
                                methods.notifications.navigate({status: 503});
                            }
                            else {
                                if (data.authenticated) {
                                    $.jCommon.cookie.create(methods.getCName(), true, 10);
                                    e = jQuery.Event("authenticated");
                                    state.user = data;
                                    if (state.settings) {
                                        state.settings.show();
                                    }
                                    if (state.opts.welcome) {
                                        state.opts.welcome.html(!$.jCommon.string.empty(state.getName()) ? state.getName() : 'Hello');
                                    }
                                    if (state.opts.me) {
                                        var ul = state.opts.me.closest('ul');
                                        ul.addClass('user-content');
                                        if (data.principalUri) {
                                            state.opts.me.attr('href', data.principalUri);
                                            var p = [
                                                {role: 'separator'},
                                                {label: "Accounts", url: "/accounts/acl", groups: ["admin", "account managers"]},
                                                {label: "Enclave Scoping", url: "/accounts/enclave", groups: ["account managers"]},
                                                {label: "Organization Scoping", url: "/accounts/organizations", groups: ["account managers"]},
                                                {label: "User Activity", url: "/pages/accounts/activity/index.html", groups: ["log managers"]},
                                                {role: 'separator'},
                                                {label: "Data", url: "/server/data", groups: ["admin"]},
                                                {label: "Indices", url: "/server/indices", groups: ["admin"]},
                                                {label: "Logs", url: "/server/logs", groups: ["admin"]},
                                                {role: 'separator'},
                                                {label: "Deduplication", sep: true, url: "/deduplicate", groups: ["admin"]},
                                                {label: "Jobs", url: "/server/workers", groups: ["admin"]},
                                                {label: "Importer", url: "/worker/rmk/importer", groups: ["admin"]},
                                                {label: "Workflow Templates", url: "/workflow/templates", groups: ["admin"]}
                                            ];
                                            $.each(p, function () {
                                                var li = $(document.createElement('li'));
                                                ul.append(li);
                                                if (this.role) {
                                                    li.attr('role', this.role).addClass('divider');
                                                }
                                                else {
                                                    var lk = $(document.createElement('a')).attr('href', this.url).addClass('nav-list-item');
                                                    var g = $(document.createElement('span'));
                                                    var t = $(document.createElement('span')).html(this.label);
                                                    li.append(lk.append(g).append(t));
                                                }
                                                if(this.groups){
                                                    li.hide();
                                                    $.login.authorized({"groups": this.groups, "r": false}, function (d) {
                                                        if(!d.auth){
                                                            if(li.prev().hasClass('divider') && (!li.next() || li.next().hasClass('divider'))){
                                                                li.prev().remove();
                                                            }
                                                            li.remove();
                                                        }
                                                        else{
                                                            li.show();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                }
                                else {
                                    methods.register(data);
                                }
                            }
                            break;
                    }

                    if(e){
                        if($.isFunction(state.opts.onAuthenticated)){
                            state.opts.onAuthenticated(e);
                        }
                        else {
                            lusidity.trigger(e);
                        }
                    }
                    data=null;
                }
                go();
            }
        };
        state.login = function(){
            methods.login();
        };
        state.logout = function(){
            methods.logout();
        };
        state.getName = function(){
            var result = state.user.firstName;
            if(!$.jCommon.string.empty(state.user.firstName) && !$.jCommon.string.empty(state.user.lastName)){
                result = state.user.firstName + ' ' + state.user.lastName;
            }
            return result;
        };
        state.getId = function(){
            return state.user.principalUri;
        };
        //environment: Initialize
        methods.init();
    };

    $.login.asi = function (c) {
        _chk(c);
    };

    $.login.unauth = function (data, msg) {
        var msg = btoa((msg ? msg : "<strong>Unauthorized:</strong> You do not have sufficient permissions to view the page requested."));
        window.location = "/notification?status="+data._response_code + '&msg='+msg;
    };

    $.login.authorized = function (options, c) {
        if(options.url){
            var s = function (data) {
              if(null===data || data._response_code){
                  if($.isFunction(options.failed)){
                      options.failed(data);
                  }
                  else{
                      $.login.unauth(data);
                  }
              }
              else if($.isFunction(options.success)){
                  options.success(data);
              }
              else{
                  console.log("options.success is required.");
              }
            };
            $.htmlEngine.request(options.url, s, s, null, 'get');
        }
        else {
            var s = function (data) {
                var a = (data && data.authorized);
                if (!c) {
                    c = options.c;
                }
                if (options.r && !a) {
                    window.location = "/notification?status=403";
                }
                if ($.isFunction(c)) {
                    c({auth: a, redirectUrl: "/notification?status=403"});
                }
            };
            var f = function (data) {
                var a = (data && data.authorized);
                if (options.r && !a) {
                    window.location = "/notification?status=403";
                }
                if ($.isFunction(c)) {
                    c({auth: a, redirectUrl: "/notification?status=403", error: true});
                }
            };
            $.htmlEngine.request('/authorization/authorized', s, f, options, "post", false);
        }
    };

    $.login.redirect = function(){
        var ref = document.referrer ? $.jCommon.url.create(document.referrer).relativePath : null;
        if(null!==ref && !$.jCommon.string.contains(ref, "register", true)
            && !$.jCommon.string.contains(ref, "notification", true)
            && !$.jCommon.string.empty(ref)
            && !$.jCommon.string.equals(ref, "/", true)
            && !$.jCommon.string.contains(ref, "index", true)) {
            last = ref;
            window.location = ref;
        }
        else {
            window.location = "/enclaves";
        }
    };

    //Default Settings
    $.login.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.login = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.login($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $login = $(this).data('login');
            if($login === undefined){
                return false;
            }
            switch (method) {
                case 'login': return $login.login();break;
                case 'getName': return $login.getName(); break;
                case 'getId': return $login.getId(options);break;
                case 'logout':
                default: $login.logout();break;
            }
        }
    }

})(jQuery);
