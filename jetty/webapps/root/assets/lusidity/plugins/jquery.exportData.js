;(function ($) {

    //Object Instance
    $.exportData = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.exportData.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.sUrl = lusidity.environment('host-primary');

        // Store a reference to the environment object
        el.data("exportData", state);

        // Private environment methods
        methods = {
            init: function() {
                // exportData -store "com.lusidity.domains.electronic.network.Asset" -sort "hostname" -max 1000 -batch 500
                if($.jCommon.is.empty(state.opts.partition)){
                    state.opts.partition = state.opts.store;
                }
                if($.jCommon.is.empty(state.opts.sort)){
                    state.opts.sort = 'title';
                }
                if(!state.opts.max || state.opts.max<0){
                    state.opts.max = 1000;
                }
                var cmd = {
                    command: state.opts.command,
                    params: {
                        store: state.opts.store,
                        partition: state.opts.partition,
                        sort: state.opts.sort,
                        max: state.opts.max,
                        batch: state.opts.batch,
                        user: state.opts.user
                    }
                };
                var s = function (data) {
                    if($.isFunction(state.opts.callback) && data){
                        state.opts.callback(data);
                        var u = data.download;
                        window.location.assign(u);
                    }
                };
                $.htmlEngine.request(state.sUrl + '/admin/command', s, s, cmd, "post");
            },
            createNode: function(type, css, cls) {
                return $(document.createElement(type));
            }
        };
        //public methods
        state.send = function (opts) {
            state.opts =  $.extend({}, state.opts, opts);
            methods.init();
        };


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.exportData.defaults = {
        batch: 100000,
        max: 1000,
        sort: 'title',
        user: false
    };


    //Plugin Function
    $.fn.exportData = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.exportData($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $exportData = $(this).data('exportData');
            switch (method) {
                case 'exists': return (null!=$exportData && undefined!=$exportData && $exportData.length>0);
                case 'send': $exportData.send(o);
                case 'state':
                default: return $exportData;
            }
        }
    };

    $.exportData.call= function(elem, options){
        elem.exportData(options);
    };

    try {
        $.htmlEngine.plugins.register("exportData", $.exportData.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
