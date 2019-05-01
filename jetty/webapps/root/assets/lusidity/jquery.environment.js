;(function ($) {
    $.environment = function(el, options) {
        var state = el,
            methods = {};
        state.opts = $.extend({}, $.environment.defaults, options);
        state.loaded = false;
        state.resizeFunctions = [];
        state.url = $.jCommon.url.create(window.location.href);
        state.isMobile = $.jCommon.string.contains(navigator.userAgent.toLowerCase(), ['android','iphone', 'ipad'], true);
        state.isIPad = $.jCommon.string.contains(navigator.userAgent.toLowerCase(), ['ipad'], true);
        state.language = window.navigator.userLanguage || window.navigator.language;
        state.resizeStarted = false;
        el.data("environment", state);
        var _started = false;
        var ttl=10;

        methods = {
            init: function(options) {
                if(!_started) {
                    _started = true;
                    $(window).on('resize', function () {
                        methods.onResize(false);
                    });
                    methods.loadWidgets();
                    state[0].addEventListener('copy_clip', function (e) {
                        $.jCommon.copy.copyText(e.data.txt);
                    });
                    var e = jQuery.Event("environmentReady");
                    e.state = state;
                    state.trigger(e);
                }
            },
            loadWidgets: function(widgets){
                if(!widgets){
                    widgets = $('div[role="widget"]');
                }
                $.each(widgets, function(){
                    var widget = $(this);
                    var url = widget.attr("data-widget");
                    $.ajax({
                        url: url,
                        async: false,
                        success:  function(data){
                            widget.html(data);
                        }
                    });
                });
            },
            getCName: function () {
                var u = $.jCommon.url.create(window.location.href);
                var c = $.jCommon.string.replaceAll(u.host, '\\.', '_');
                return c + '_loggedIn';
            },
            onResize: function(delayed){
                if(state.resizeStarted){
                    return false;
                }
                state.resizeStarted = true;
                var rTime = new Date(1, 1, 2000, 12, 0, 0);
                var delta = 250;
                var on = 0;
                var s = true;
                var last = {
                    h: $(document).height(),
                    w: $(document).width()
                };
                function rs() {
                    $.each(state.resizeFunctions, function () {
                        this.caller();
                        on++
                    });
                }
                function check() {
                    if(!s){
                        return false;
                    }
                    var h = $(document).height();
                    var w = $(document).width();
                    if ((last.h !== h || last.w !== w)) {
                        last.h = h;
                        last.w = w;
                        rTime = new Date(1, 1, 2000, 12, 0, 0);
                        window.setTimeout(check, delta);
                    } else {
                        state.resizeStarted = false;
                        rs();
                    }
                }
                window.setTimeout(check, delta);
            }
        };
        state.isEmpty = function (obj, deep) {
            var result = false;
            try {
                result = ((obj === null || obj.length === 0 || obj === 'undefined' || obj === 'null'));
                if (deep && !result) {
                    result = state.isEmpty(obj.valueOf());
                }
            }
            catch (ignore) { result = false; }
            return result;
        };
        state.message = {
            add: function (options) {
                if (state.isEmpty(options) || state.isEmpty(options.msg)) {
                    return false;
                }

                if (options.debug && !state.opts.debug) {
                    return false;
                }
            }
        };
        state.openUri = function openNewBackgroundTab(options){
            var current = window;
            var win = window.open(options.uri, '_blank');
            if (!win) {
                alert('The pop was blocked, please allow popups for this site');
            }
            if(options.focus){
                win.focus();
            }
            else{
                current.focus();
            }
           /*
            var a = document.createElement("a");
            a.href = opt;
            var evt = document.createEvent("MouseEvents");
            //the tenth parameter of initMouseEvent sets ctrl key
            evt.initMouseEvent("click", true, true, window, 0, 0, 0, 0, 0,
                true, false, false, false, 0, null);
            a.dispatchEvent(evt);
            */
        };
        state.request = function(action){
            if(undefined === action.url){
                return false;
            }
            var url = action.url;
            if(!$.jCommon.string.startsWith(url, 'http', true)){
                url = encodeURI(state.opts.serviceHostUri +  action.url);
            }
            var xhrFields = action.xhrFields ? action.xhrFields : {};

            var async = $.jCommon.is.empty(action.async) ? true : action.async;
            if(async){
                xhrFields.withCredentials = true;
            }
            if(action.data && state.priviledged){
                action.data.priviledged = true;
                url += ($.jCommon.string.contains(url, "?") ? "&" : "?" ) + "priviledged=true";
            }
            var r = null;
            try {
                r = $.ajax({
                    url: url,
                    type: action.methodType,
                    async: async,
                    dataType: action.dataType ? action.dataType : 'json',
                    data: action.data ? action.data : null,
                    cache: false,
                    timeout: action.timeout ? action.timeout : 0,
                    xhrFields: xhrFields,
                    beforeSend: function (xhr) {
                        if (state.opts.actions === null) {
                            state.opts.actions = [];
                        }
                        action.id = state.opts.actions.length;
                        action.completed = false;
                        action.success = false;
                        state.opts.actions.push(action);

                        if (action.onbeforesend && typeof action.onbeforesend === "object") {
                            if (action.onbeforesend.message && action.onbeforesend.message.length > 0) {
                                state.message.add(action.onbeforesend.message);
                            }
                            if (action.onbeforesend.execute && $.isFunction(action.onbeforesend.execute)) {
                                action.onbeforesend.execute(xhr);
                            }
                        }
                    },
                    complete: function (jqXHR, textStatus) {
                        var x = state.opts.actions.length;
                        var stopAnimation = true;
                        for (var i = 0; i < x; i++) {
                            var act = state.opts.actions[i];
                            if (i === act.id) {
                                act.completed = true;
                            }
                            if (!act.completed) {
                                stopAnimation = false;
                            }
                        }
                        if (stopAnimation) {
                            state.opts.loaded = true;
                        }
                        if (action.oncompleted
                            && typeof action.oncompleted === 'object'
                            && action.oncompleted && $.isFunction(action.oncompleted)) {
                            action.oncompleted.execute(jqXHR, textStatus);
                        }
                    },
                    success: function (data) {
                        if (data) {
                            var loggedIn = $.jCommon.cookie.read(methods.getCName());
                            if (loggedIn) {
                                $.jCommon.cookie.create(methods.getCName(), true, ttl);
                            }
                            state.opts.actions[action.id].success = true;
                        }
                        else {
                            state.opts.actions[action.id].success = false;
                            state.message.add({msg: 'No data returned.', debug: true});
                        }

                        if (action.onsuccess && typeof action.onsuccess === 'object') {
                            if (action.onsuccess.message && action.onsuccess.message.length > 0) {
                                state.message.add(action.onsuccess.message);
                            }

                            if (action.onsuccess.execute && $.isFunction(action.onsuccess.execute)) {
                                action.onsuccess.execute(data);
                            }
                        }
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        state.opts.actions[action.id].success = false;
                        if (action.onfailure && typeof action.onfailure === 'object') {
                            if (action.onfailure.message && action.onfailure.message.length > 0) {
                                state.message.add(action.onfailure.message);
                            }
                            if (action.onfailure.execute && $.isFunction(action.onfailure.execute)) {
                                action.onfailure.execute(jqXHR, textStatus, errorThrown);
                            }
                        }
                    }
                });
            }
            catch (e){
                console.log(e);
            }
            return r;
        };
        state.columns = function (options) {
            var cw = $(options.container).innerWidth();
            var col = Math.floor(cw / options.minWidth);
            return ((col > options.maxItems) ? options.maxItems : ((col<1) ? 1: col));
        };
        state.storage =  {
            session: {
                save: function (name, json) {
                    sessionStorage[name] = JSON.stringify(json);
                },
                get: function (name) {
                    var session = sessionStorage[name];
                    if (!$.jCommon.is.empty(session)) {
                        session = JSON.parse(session);
                    }
                    return session;
                }
            },
            global: {
            }
        };
        state.info ={
            visible: false,
            outer: $('div.alert-block'),
            inner: $('div.alert-block').find('.alert-inner'),
            btn: $('div.alert-block').find('.glyphicon-remove'),
            callback: null,
            timer: null,
            autoHide: function(seconds){
                state.info.timer = window.setTimeout(function(){
                    state.info.hide();
                    state.info.timer = null;
                }, (seconds*1000));
            },
            clear: function(){
                if(state.info.timer){
                    clearTimeout(state.info.timer);
                }
                state.info.hide();
                state.info.inner.children().remove();
                this.outer.removeClass('alert-danger alert-success alert-info alert-warning');
            },
            yellow: function(text){
                state.info.say('alert-warning', text);
            },
            green: function(text){
                state.info.say('alert-success', text);
            },
            blue: function(text){
                state.info.say('alert-info', text);
            },
            red: function(text){
                state.info.say('alert-danger', text);
            },
            say: function (cls, text) {
                state.info.btn.unbind();
                state.info.btn.on('click', function () {
                    state.info.hide();
                });
                state.info.clear();
                state.info.outer.addClass(cls);
                state.info.inner.append(text);
            },
            height: function () {
                return state.info.outer.is(':visible') ? state.info.outer.outerHeight()+20 : 0;
            },
            show: function(timeout, callback){
                state.info.visible = true;
                state.info.callback = callback;
                state.info.outer.slideDown(300, function(){
                    if($.isFunction(callback)){
                        callback({show: true});
                    }
                    if(timeout && $.jCommon.is.numeric(timeout)){
                        state.info.autoHide(timeout, callback);
                    }
                });
            },
            hide: function(callback){
                state.info.visible = false;
                state.info.inner.html('');
                state.info.outer.slideUp(300, function(){
                    window.setTimeout(function () {
                        if($.isFunction(callback)){
                            callback({show: false});
                        }
                        else if($.isFunction(state.info.callback)){
                            state.info.callback({show: false});
                        }
                    }, 300);
                });
            },
            isVisible: function(){
                return state.info.visible;
            }
        };
        state.getImages = function(options){
            var action = {
                connector: null,
                async: options.async ? options.async : true,
                data: null,
                methodType: 'get',
                showProgress: false,
                onsuccess: {
                    message: { msg: null, debug: false },
                    execute: function (data) {
                        if(options.onSuccess && $.isFunction(options.onSuccess)){
                            options.onSuccess(data);
                        }
                    }
                },
                onfailure: {
                    message: { msg: null, debug: false },
                    execute: function (jqXHR, textStatus, errorThrown) {
                        if(options.onError && $.isFunction(options.onError)){
                            options.onError(jqXHR, textStatus, errorThrown);
                        }
                    }
                },
                url: options.url + '/images'
            };
            state.request(action);
        };
        state.getImage = function(options){
            var url = '/image?file=' + options.url;
            var action = {
                connector: null,
                async: true,
                data: null,
                methodType: 'get',
                showProgress: false,
                onsuccess: {
                    message: { msg: null, debug: false },
                    execute: function (data) {
                        options.element.hide('slide', {direction: 'right'},200).attr('src', data.dataUri);
                        if(options.onLoad && $.isFunction(options.onLoad)){
                            options.onLoad();
                        }
                        else{
                            options.element.show();
                        }
                    }
                },
                onfailure: {
                    message: { msg: null, debug: false },
                    execute: function (jqXHR, textStatus, errorThrown) {
                        if(options.onError && $.isFunction(options.onError)){
                            options.onError();
                        }
                    }
                },
                url: url
            };
            state.request(action);
        };
        state.getEntity= function (options) {
            var rPath = options.url;
            var action = {
                connector: null,
                async: options.async || undefined===options.async ? true : options.async,
                data: null,
                methodType: 'get',
                showProgress: false,
                onsuccess: {
                    message: { msg: null, debug: false },
                    execute: function (data) {
                        if($.jCommon.is.Function(options.onSuccess)){
                            options.onSuccess(data);
                        }
                    }
                },
                onfailure: {
                    message: { msg: null, debug: false },
                    execute: function (jqXHR, textStatus, errorThrown) {
                        if($.jCommon.is.Function(options.onFailure)){
                            options.onFailure(jqXHR, textStatus, errorThrown);
                        }
                    }
                },
                url: rPath
            };
            state.request(action)
        };
        state.getActions= function (options) {
            var rPath = options.url + '/actions';
            var action = {
                connector: null,
                async: true,
                data: null,
                methodType: 'get',
                showProgress: false,
                onsuccess: {
                    message: { msg: null, debug: false },
                    execute: function (data) {
                        if($.jCommon.is.Function(options.onSuccess)){
                            options.onSuccess(data);
                        }
                    }
                },
                onfailure: {
                    message: { msg: null, debug: false },
                    execute: function (jqXHR, textStatus, errorThrown) {
                        if($.jCommon.is.Function(options.onFailure)){
                            options.onFailure(jqXHR, textStatus, errorThrown);
                        }
                    }
                },
                url: rPath
            };
            state.request(action)
        };
        state.fb_script = function() {
            (function (d, s, id) {
                var js, fjs = d.getElementsByTagName(s)[0];
                if (d.getElementById(id)) return;
                js = d.createElement(s);
                js.id = id;
                js.src = "//connect.facebook.net/en_US/sdk.js#xfbml=1&appId=461927253879386&version=v2.0";
                fjs.parentNode.insertBefore(js, fjs);
            }(document, 'script', 'facebook-jssdk'));
        };
        state.tweet_script= function(){
            !function(d,s,id){
                var js,fjs=d.getElementsByTagName(s)[0];
                if(!d.getElementById(id))
                {
                    js=d.createElement(s);
                    js.id=id;
                    js.src="https://platform.twitter.com/widgets.js";
                    fjs.parentNode.insertBefore(js,fjs);
                }
            }(document,"script","twitter-wjs");
        };

        state.addResizeFunction = function(caller){
            if($.isFunction(caller)){
                try{state.resizeFunctions.push({ caller: caller});}catch (e){console.log(e)}
            }
        };
        state.listening = null;
        state.listen = function(options){
            state.listening = options.enabled;
            if(typeof(Storage) !== undefined){
                localStorage.setItem("lusidity.com.listening", state.listening)
            }
        };
        state.isListening = function(){
            if(null===state.listening){
                if(typeof(Storage) !== undefined){
                    try {
                        state.listening = localStorage.getItem("lusidity.com.listening", state.listening);
                    }
                    catch(e){}
                }
                if(null===state.listening||undefined===state.listening){
                    state.listening = true;
                }
            }
            return state.listening;
        };
        state.changeTitle = function(options){
            $('title').html(options.title);
            $('meta[property="og:title"]').attr('content', options.title);
            $('meta[property="og:url"]').attr('content', window.location.href);
        };
        state.changeDescription = function(options){
            $('meta[name="description"]').attr('content', options.text);
            $('meta[property="og:description"]').attr('content', options.text);
        };
        state.copy = function (options) {
            var e = new Event('copy_clip');
            e.data = options;
            state[0].dispatchEvent(e);
        };
        state.priviledged = function (options) {
            state.priviledged = true;
        };
        methods.init();
    };
    $.environment.makeUnselectable = function(node){
        node.addClass('no-select').attr("unselectable", "on");
        $.each(node.children(), function(){
            $.environment.makeUnselectable($(this));
        });
    };
    $.environment.isMobile = function(){
        return $.jCommon.string.contains(navigator.userAgent.toLowerCase(), ['android','iphone'], true);
    };
    $.environment.isIPad = function(){
        return $.jCommon.string.contains(navigator.userAgent.toLowerCase(), ['ipad'], true);
    };
    $.environment.getServiceUri = function(){
        var result= window.location.host.split(":");
        result = result[0];
        return result ? 'https://' + result + ':8443' : null;
    };
    $.environment.close = function () {
            var s = function (data) {
                if(navigator) {
                    try {
                        //https://www.w3.org/TR/clear-site-data/
                        //navigator.storage.clear();
                    }
                    catch(e){}
                }
            };
            $.htmlEngine.request('/pki/logout', s, s, null, 'get');
    };
    $.environment.defaults = {
        actions: null,
        server: {
            home: window.location.protocol + '//' + window.location.host
        },
        serviceHostUri: $.environment.getServiceUri(),
        debug: false,
        logging: false,
        mockData: false,
        view: null,
        navigating: false,
        delay: 300,
        message: {
            timer: 0,
            hovered: false,
            ttl: {
                factor: 5000,
                remaining: 0
            },
            items: null
        },
        onStarted: null
    };

    $.environment.isLoaded = function(){return true};

    $.environment.getRandomId = function(prefix){
        var rn = function(){return Math.floor(Math.random()*999999);};
        return prefix + '_' + rn() + '_' + rn();
    };

    $.fn.environment = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.environment($(this),method);
            });
        } else {
            var $environment = $(this).data('environment');
            switch (method) {
                case "priviledged": $environment.priviledged();break;
                case "info": return $environment.info;break;
                case "request": return $environment.request(options); break;
                case "onResize": return $environment.addResizeFunction(options); break;
                case "host-primary": return $environment.opts.server.primary; break;
                case "host-secondary": return $environment.opts.server.secondary; break;
                case "host-download": return $environment.opts.server.download; break;
                case "hosts": return $environment.opts.server.hosts; break;
                case "host-delete": return $environment.opts.server['delete']; break;
                case "home": return $environment.opts.server.home; break;
                case "columns": return $environment.columns(options);break;
                case "getActions": return $environment.getActions(options);
                case "getEntity": return $environment.getEntity(options);
                case "getImage": $environment.getImage(options);break;
                case "getImages": $environment.getImages(options);break;
                case 'changeTitle': $environment.changeTitle(options);break;
                case 'changeDescription': $environment.changeDescription(options);break;
                case 'isEmpty': return $environment.isEmpty(options);break;
                case 'fbScript': $environment.fb_script();break;
                case 'tweetScript': $environment.tweet_script();break;
                case 'isMobile': return $environment.isMobile;break;
                case 'isIPad': return $environment.isIPad;break;
                case 'listen': $environment.listen(options);break;
                case 'isListening': return $environment.isListening();break;
                case "language": return $environment.language;break;
                case "openUri": return $environment.openUri(options);break;
                case "copy": return $environment.copy(options);break;
                case 'state':
                default: return false;
            }
        }
    }

})(jQuery);
/*
  Below is an example of how to execute an ajax action.
    var action = {
        connector: 'The connector or array of connectors to use with this action.',
        async: true
        data: 'The data to send to server.',
        methodType: 'get or post',
        showProgress: true,
        onbeforesend: {
            message: { msg: 'Enter message here or null for no message.', debug: false },
            execute: function (xhr) {
                to access the client manager use window.parent.omanager
                to access the connector use action.connector
                to send a message use omanager.message.add('your message');
            }
        },
        oncompleted: {
            execute: function (jqXHR, textStatus) {
                to access the client manager use window.parent.omanager
                to access the connector use action.connector
                to send a message use omanager.message.add('your message');
            }
        },
        onsuccess: {
            message: { msg: 'Enter message here or null for no message.', debug: false },
            execute: function (data) {
                to access the client manager use window.parent.omanager
                to access the connector use action.connector
                to send a message use omanager.message.add('your message');
            }
        },
        onfailure: {
            message: { msg: 'Enter message here or null for no message.', debug: false },
            execute: function (jqXHR, textStatus, errorThrown) {
                to access the client manager use window.parent.omanager
                to access the connector use action.connector
                to send a message use omanager.message.add('your message');
            }
        },
        url: 'The url of the RESTful service.'
    };
    omanager.execute(action);
*/
