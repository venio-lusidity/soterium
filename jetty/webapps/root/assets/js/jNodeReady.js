;(function ($) {

    //Object Instance
    $.jNodeReady = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.jNodeReady.defaults, options);

        // Store a reference to the environment object
        el.data("jNodeReady", state);

        // Private environment methods
        methods = {
            init: function() {
                if(state.opts.opts && state.opts.opts.rcs) {
                }
                methods.validate();
            },
            validate: function(){
                function test(){
                    var h = state.height();
                    var w = state.width();
                    if(state.opts.onVisible && $.isFunction(state.opts.onVisible)){
                        if(state.is(':visible') && w>0){
                            state.opts.h = h;
                            state.opts.w = w;
                            state.opts.onVisible({h: h, w: w, vars: state.opts.vars});
                        }
                        else{
                            wait();
                        }
                    }
                    else if(state.opts.onWidth && $.isFunction(state.opts.onWidth)){
                        if(w>0){
                            state.opts.h = h;
                            state.opts.w = w;
                            state.opts.onWidth({h: h, w: w, vars: state.opts.vars});
                        }
                        else{
                            wait();
                        }
                    }
                    else if(h>0 && w>0){
                        if(state.opts.onReady && $.isFunction(state.opts.onReady)){
                            state.opts.onReady(state);
                        }

                        if(state.opts.onResized && $.isFunction(state.opts.onResized)){
                            if(h!==state.opts.h || w!==state.opts.w){
                                state.opts.h = h;
                                state.opts.w = w;
                                state.opts.onResized({h: h, w: w, vars: state.opts.vars});
                            }
                            wait();
                        }
                    }
                    else{
                        if(state.opts.elapsed<state.opts.timeOut) {
                            wait();
                        }
                    }
                }
                function wait(){
                    window.setTimeout(function(){
                        state.opts.elapsed += state.opts.interval;
                        test();
                    }, state.opts.interval);
                }
                if(state.opts.immediate){
                    test();
                }
                else {
                    wait();
                }
            }
        };
        //public methods
        state.start = function (options) {
            if(options) {
                state.opts = $.extend({}, $.jNodeReady.defaults, options);
            }
            methods.validate();
        };

        //environment: Initialize
        methods.init();
    };

    //Default Settings
    /*
    onReady: function(e){
    },
    onVisible: function (e) {
    }
    */
    $.jNodeReady.defaults = {
        immediate: true,
        elapsed: 0,
        interval: 200,
        timeOut: 60000
    };
    //Plugin Function
    $.fn.jNodeReady = function(method, options) {
        if (method === undefined) method = {};
        var $jNodeReady = $(this).data('jNodeReady');
        if ($jNodeReady===undefined && typeof method === "object") {
            return this.each(function() {
                new $.jNodeReady($(this),method);
            });
        } else if(typeof method !== "object") {
            // Helper strings to quickly perform functions
            switch (method) {
                case 'start':
                    $jNodeReady.start(options);
                    break;
                case 'exists': return (null!==$jNodeReady && undefined!==$jNodeReady && $jNodeReady.length>0);break;
                case 'state':
                default:return $jNodeReady;
            }
        }
        else{
            $jNodeReady.start(method);
        }
    };

})(jQuery);
