

;(function ($) {

    //Object Instance
    $.assistant = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.assistant.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("assistant", state);

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
    $.assistant.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.assistant = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.assistant($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $assistant = $(this).data('assistant');
            switch (method) {
                case 'exists': return (null!=$assistant && undefined!=$assistant && $assistant.length>0);
                case 'state':
                default: return $assistant;
            }
        }
    };

    $.assistant.call= function(elem, options){
        elem.assistant(options);
    };

    try {
        $.htmlEngine.plugins.register("assistant", $.assistant.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
