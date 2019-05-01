;(function ($) {

    //Object Instance
    $.pluginName = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.pluginName.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("pluginName", state);

        // Private environment methods
        methods = {
            init: function() {
               // $.htmlEngine.loadFiles(state.worker.propertyNode, state.opts.name, state.opts.cssFiles);
                state.worker.node.html('hello world');
                state.worker.node.attr('data-valid', true).show();
            },
            createNode: function(type, css, cls) {
                return $(document.createElement(type));
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.pluginName.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.pluginName = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.pluginName($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $pluginName = $(this).data('pluginName');
            switch (method) {
                case 'exists': return (null!==$pluginName && undefined!==$pluginName && $pluginName.length>0);
                case 'state':
                default: return $pluginName;
            }
        }
    };

    $.pluginName.call= function(elem, options){
        elem.pluginName(options);
    };

    try {
        $.htmlEngine.plugins.register("pluginName", $.pluginName.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
