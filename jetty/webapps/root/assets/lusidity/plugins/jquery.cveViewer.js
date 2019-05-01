;(function ($) {

    //Object Instance
    $.cveViewer = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.cveViewer.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        // Store a reference to the environment object
        el.data("cveViewer", state);

        // Private environment methods
        methods = {
            init: function() {
               // $.htmlEngine.loadFiles(state.worker.propertyNode, state.opts.name, state.opts.cssFiles);

                var id = state.worker.data.iavmNoticeNumber;
                if(id){
                    var onSuccess = function(data){
                        methods.html.create(data);
                    };
                    var onFail = function(){

                    };
                    var url = '/rmk/iavms/' + id + '/cve';
                    methods.get(url, onSuccess, onFail);
                }
                state.worker.propertyNode.attr('data-valid', true).show();
            },

            get: function(url, onSuccess, onFail) {
                //noinspection JSUnusedLocalSymbols
                var action = {
                    connector: null,
                    async: true,
                    data: null,
                    methodType: 'get',
                    showProgress: false,
                    onbeforesend: {
                        message: {msg: null, debug: false},
                        execute: function () {
                        }
                    },
                    oncompleted: {
                        execute: function (jqXHR, textStatus) {
                        }
                    },
                    onsuccess: {
                        message: {msg: null, debug: false},
                        execute: function (data) {
                            if (onSuccess && $.isFunction(onSuccess)) {
                                onSuccess(data);
                            }
                            else {
                                lusidity.info.green("Although response was successful, I don't know what to do with it.");
                                lusidity.info.show(10);
                            }
                        }
                    },
                    onfailure: {
                        message: {msg: null, debug: false},
                        execute: function (jqXHR, textStatus, errorThrown) {
                            if (onFail && $.isFunction(onFail)) {
                                onFail(jqXHR, textStatus, errorThrown);
                            }
                            else {
                                lusidity.info.red('Sorry, we cannot find the resource requested.&nbsp;&nbsp;'
                                + 'You can try refreshing the&nbsp;<a href="' + window.location.href + '">page</a>.');
                                lusidity.info.show(10);
                            }
                        }
                    },
                    url: url
                };
                lusidity.environment('request', action);
            },
            html:{
                create: function(data){
                    $.each(data, function(){
                        var item = this;
                        var content = $(document.createElement('div')).addClass('bs-callout bs-red');
                        var head = $(document.createElement('h5')).addClass('cve-title').html($.jCommon.json.getProperty(item, 'title'));
                        var meta = $(document.createElement('div')).html($.jCommon.json.getProperty(item, '/technology/security/vulnerabilities/cve/cve/summary.value'));
                        content.append(head).append(meta);
                        state.worker.valueNode.append(content);
                    });
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.cveViewer.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.cveViewer = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.cveViewer($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $cveViewer = $(this).data('cveViewer');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $cveViewer;
            }
        }
    };

    $.cveViewer.call= function(elem, options){
        elem.cveViewer(options);
    };

    try {
        $.htmlEngine.plugins.register("cveViewer", $.cveViewer.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
