;(function ($) {

    //Object Instance
    $.domainTypeFilter = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.domainTypeFilter.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        // Store a reference to the environment object
        el.data("domainTypeFilter", state);

        // Private environment methods
        methods = {
            init: function() {
               // $.htmlEngine.loadFiles(state.worker.propertyNode, state.opts.name, state.opts.cssFiles);
                if(!state.worker.propertyNode){
                    state.worker.propertyNode = state.worker.valueNode.parent();
                }
                state.worker.valueNode.html('hello world');
                state.worker.propertyNode.attr('data-valid', true).show();
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.domainTypeFilter.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.domainTypeFilter = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.domainTypeFilter($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $domainTypeFilter = $(this).data('domainTypeFilter');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $domainTypeFilter;
            }
        }
    };

    $.domainTypeFilter.call= function(elem, options){
        elem.domainTypeFilter(options);
    };

    try {
        $.htmlEngine.plugins.register("domainTypeFilter", $.domainTypeFilter.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
