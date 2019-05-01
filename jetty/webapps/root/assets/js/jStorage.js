;(function ($) {

    //Object Instance
    $.jStorage = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.jStorage.defaults, options);

        // Store a reference to the environment object
        el.data("jStorage", state);

        // Private environment methods
        methods = {
            init: function() {
            },
            exists: function (node) {
                return (node && (node.length>0));
            },
            resize: function () {}
        };
        //public methods
        state.getData = function (options) {
            var r = state.opts;
            if(options){
                r = $.extend({}, state.opts, options, true);
            }
            return r;
        };

        // Initialize
        methods.init();
    };

    //Default Settings
    $.jStorage.defaults = {};


    //Plugin Function
    $.fn.jStorage = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.jStorage($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $jStorage = $(this).data('jStorage');
            switch (method) {
                case 'exists': return (null!==$jStorage && undefined!==$jStorage && $jStorage.length>0);
                case 'data': return $jStorage.getData(options);break;
                case 'state':
                default: return $jStorage;
            }
        }
    };

})(jQuery);
