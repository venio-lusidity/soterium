;(function ($) {

    //Object Instance
    $.pluginName = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.pluginName.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("pluginName", state);

        // Private environment methods
        methods = {
            init: function() {
                lusidity.environment('onResize', function(){
                    methods.resize();
                });
            },
            exists: function (node) {
                return (node && (node.length>0));
            },
            resize: function () {}
        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.pluginName.defaults = {};


    //Plugin Function
    $.fn.pluginName = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.pluginName($(this), method);
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

})(jQuery);
