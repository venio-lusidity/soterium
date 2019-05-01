;(function ($) {

    //Object Instance
    $.metrics = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);
        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.metrics.defaults, options.schema.plugin);
        state.opts.data = options.data;
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';

        // Store a reference to the environment object
        el.data("metrics", state);

        // Private environment methods
        methods = {
            init: function() {
                state.worker.node.children().remove();
                state.opts.href = null;
                state.worker.node.jVulnMatrix(state.opts);
                state.worker.node.attr('data-valid', true).show();
            },
            getUri: function (data) {
                var result = null;
                if (null !== data && undefined !== data) {
                    result = (null !== data.otherId && undefined !== data.otherId) ? data.otherId : data[state.KEY_ID];
                }
                return result;
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.metrics.defaults = {};


    //Plugin Function
    $.fn.metrics = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.metrics($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $metrics = $(this).data('metrics');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $metrics;
            }
        }
    };

    $.metrics.call= function(elem, options){
        elem.metrics(options);
    };

    try {
        $.htmlEngine.plugins.register("metrics", $.metrics.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
