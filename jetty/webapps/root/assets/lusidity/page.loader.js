var pl_init=false;
var _ldr_init=false;
var grid_grp_init;
var pageLoader = {
    path: null,
    start: function (callback) {
        pageLoader._load((pageLoader.path + '/js/start.js'), 'js', callback)
    },
    _default: function () {
        document.addEventListener('jqueryReady', function () {
            pageLoader._wait(pageLoader._bRdy, 'pageLoaderReady');
        });
        pageLoader._wait(pageLoader._jRdy, 'jqueryReady');
    },
    _wait: function (caller, name) {
        if(caller()){
            var event = document.createEvent("HTMLEvents");
            event.initEvent(name, true, true);
            event.eventName = name;
            document.dispatchEvent(event);
        }
        else {
            window.setTimeout(function () {
                pageLoader._wait(caller, name);
            }, 100);
        }
    },
    _load: function (filename, filetype, callback) {
        try {
            var node;
            var head = document.getElementsByTagName('head')[0];
            if(undefined===filename){
                if($.isFunction(callback)){}
                callback();
                return false;
            }
            if (undefined !== filename && null !== filename) {
                filename += appVersion;
            }
            if (filetype === 'js') {
                node = document.createElement('script');
                node.setAttribute('type', 'text/javascript');
                if ($.isFunction(callback)) {
                    node.onload = function () {
                        callback(filename);
                    };
                    node.onerror = function (e) {
                        console.log(e);
                    };
                }
                node.setAttribute('src', filename);
            }
            else if (filetype === 'css') {
                node = document.createElement('link');
                node.setAttribute('rel', 'stylesheet');
                node.setAttribute('type', 'text/css');
                if ($.isFunction(callback)) {
                    node.onload = function () {
                        callback(filename);
                    };
                    node.onerror = function (e) {
                        console.log(e);
                    };
                }
                node.setAttribute('href', filename);

            }
            else if (filetype === 'meta') {
                node = document.createElement('meta');
                node.setAttribute('name', 'viewport');
                node.setAttribute('content', 'width=device-width, initial-scale=1.0');
            }
            else if (filetype === 'fav') {
                node = document.createElement('link');
                node.setAttribute('rel', 'shortcut icon');
                node.setAttribute('type', 'image/png');
                node.setAttribute('href', filename);
            }
            if (undefined !== node && null !== node) {
                head.appendChild(node);
            }
        }
        catch (e){
            console.log(e);
        }
    },
    _jRdy: function () {
        return (typeof jQuery !== 'undefined');
    },
    _bRdy: function () {
       return pageLoader._jRdy() && $.isFunction($.fn.modal) ;
    },
    init: function (scripts, styles, path) {
        if(_ldr_init){
         //   return false;
        }
        _ldr_init = true;
        pageLoader.path = path;
        var head = document.getElementsByTagName('head')[0];
        pageLoader._load(null, 'meta');
        pageLoader._load('/fav.png', 'fav');

        function process(items, filetype, callback) {
            if (items instanceof Array) {
                for (var i = 0; i < items.length; i++) {
                    pageLoader._load(items[i], filetype, callback);
                }
            }
        }

        function start() {
            var rdy = false;
            try {
                rdy = $.environment.isLoaded() &&  $.jCommon.isReady();
            } catch (e) {
            }
            if (rdy) {
                $(".page").css({display: 'block'});
                var js = ['/assets/js/common.js'];
                for (i = 0; i < js.length; i++) {
                    pageLoader._load(js[i], 'js');
                }
            }
            else {
                window.setTimeout(start, 100);
            }
        }

        function validate() {
            if (pageLoader._jRdy()) {
                $(head).append($(document.createElement("style")).html(
                    '#draggable, #draggable2, #draggable3 { border: solid 1px #000; width: 100px; height: 100px; padding: 0.5em; float: left; margin: 0 10px 10px 0; }'
                ));
                var css = [
                    '/assets/jquery/jquery-ui.min.css',
                    '/assets/bootstrap/css/bootstrap.min.css',
                    '/assets/fonts/sofiapro_regular_macroman/stylesheet.css',
                    '/assets/fonts/sofiapro_lightitalic_macroman/stylesheet.css',
                    '/assets/fonts/sofiapro_regular_macroman/stylesheet.css',
                    '/assets/fonts/sofiapro_lightitalic_macroman/stylesheet.css',
                    '/assets/css/style.css',
                    '/assets/css/color.css',
                    '/assets/icon8/os/css/styles.min.css',
                    '/assets/glyphicons/css/glyphicons.css',
                    '/assets/glyphicons/css/glyphicons-filetypes.css',
                    '/assets/glyphicons/css/glyphicons-halflings.css',
                    '/assets/glyphicons/css/glyphicons-social.css',
                    '/assets/css/tabs.css',
                    '/assets/css/elusidate.css',
                    '/assets/bootstrap/css/bootstrap-tour.min.css',
                    '/assets/js/form/css/formBuilder.css'
                ];
                if(styles){
                    for(var i=0;i<styles.length;i++){
                        css.push(styles[i]);
                    }
                }
                var scr = [
                    '/assets/js/jStorage.js',
                    '/assets/js/jBrowserInfo.js',
                    '/assets/js/jCommon.js',
                    '/assets/js/jAlert.js',
                    '/assets/js/objects/oStopWatch.js',
                    '/assets/lusidity/page.ui.js',
                    '/assets/lusidity/jquery.environment.js',
                    '/assets/lusidity/jquery.schemaEngine.js',
                    '/assets/lusidity/jquery.htmlEngine.js',
                    '/assets/js/factory/FnFactory.js',
                    '/assets/js/factory/QueryFactory.js',
                    '/assets/js/jNodeReady.js',
                    '/assets/lusidity/ObjectMap.js',
                    '/assets/lusidity/jquery.autoSuggest.js',
                    '/assets/lusidity/jquery.login.js',
                    '/assets/js/loaders/jquery.loaders.js',
                    '/assets/js/jquery.pageModal.js',
                    '/assets/js/jquery.panel.js',
                    '/assets/js/jActions.js',
                    '/assets/js/jJobStatus.js',
                    '/assets/js/form/jquery.menuBar.js',
                    '/assets/js/form/jquery.formBuilder.js',
                    '/assets/js/form/jquery.inputValidator.js',
                    '/assets/js/jquery.scrollHandler.js',
                    '/assets/js/jquery.sortElements.js',
                    '/assets/js/jFeedback.js',
                    '/assets/js/jquery.serverMessages.js',
                    '/assets/js/jquery.tourHelper.js',
                    '/assets/bootstrap/js/bootstrap-tour.min.js',
                    '/assets/lusidity/plugins/jquery.listViewModal.js'
                ];
                if(scripts){
                    for(var i=0;i<scripts.length;i++){
                        scr.push(scripts[i]);
                    }
                }
                var done=0;
                var len=(css.length + scr.length);
                process(css, 'css', function () {
                    done++;
                    if(done>=len){
                        start();
                    }
                });
                process(scr, 'js', function () {
                    done++;
                    if(done>=len){
                        start();
                    }
                });
            }
            else {
                window.setTimeout(validate, 100);
            }
        }
        validate();
    }
};
