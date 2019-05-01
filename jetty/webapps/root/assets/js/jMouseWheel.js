

;(function ($) {

    //Object Instance
    $.mouseWheel = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.mouseWheel.defaults, options);

        // Store a reference to the environment object
        el.data("mouseWheel", state);

        // Private environment methods
        methods = {
            init: function() {
                var elem = state[0];
                function MouseWheelHandler(e) {
                    if(state.opts.onScrolled && $.isFunction(state.opts.onScrolled)){
                        var e = window.event || e; // old IE support
                        var delta = Math.max(-1, Math.min(1, (e.wheelDelta || -e.detail)));
                        e.up = delta>0;
                        state.opts.onScrolled(e);
                    }
                    else{
                        console.log("Set option onScrolled(e) as function.");
                    }
                }
                if (elem.addEventListener) {
                    // IE9, Chrome, Safari, Opera
                    elem.addEventListener("mousewheel", MouseWheelHandler, false);
                    // Firefox
                    elem.addEventListener("DOMMouseScroll", MouseWheelHandler, false);
                }
                else {
                    // IE 6/7/8
                    elem.attachEvent("onmousewheel", MouseWheelHandler);
                }
            }
        };
        //public methods
        state.destroy = function () {
            state.unbind('touchstart');
            state.unbind('touchmove');
            state.unbind('mouseenter mouseleave');
            state.unbind('DOMMouseScroll');
            state.unbind('mousewheel');
            state.unbind('onmousewheel');
        };
        // Initialize
        methods.init();
    };

    //Default Settings
    $.mouseWheel.defaults = {};


    //Plugin Function
    $.fn.mouseWheel = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.mouseWheel($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $mouseWheel = $(this).data('mouseWheel');
            switch (method) {
                case 'exists': return (null!==$mouseWheel && undefined!==$mouseWheel && $mouseWheel.length>0);
                case 'unbind': $mouseWheel.destroy();break;
                case 'state':
                default: return $mouseWheel;
            }
        }
    };

})(jQuery);
