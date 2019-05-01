;(function ($) {
    // Expects an outer element as the scroll container and an inner container containing all the items to scroll.
    /*
     <div style="max-height: 20px" class="scroll-container">
     <div>
     items.....
     </div>
     <div>
     */
    $.scrollHandler = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);
        state.opts = $.extend({}, $.scrollHandler.defaults, options);

        // Store a reference to the environment object
        el.data("scrollHandler", state);

        var btmLast = 0;
        var _started = false;
        var _scrollDrag = false;
        var _enabled = true;

        // Private environment methods
        methods = {
            init: function() {
                methods.scrollDrag();
                methods.start();
            },
            isBottom: function () {
                var h = state.height();
                var s = state.scrollTop()+state.opts.adjust;
                var t = 0;
                $.each(state.children(), function(){
                    t += $(this).height();
                });
                return ((h+s)>=t);
            },
            scrollDrag: function(){
                if($.isFunction(state.opts.onDragStop)){
                    var md = false;
                    _scrollDrag = false;
                    state.on('scroll', function () {
                        if(md) {
                            _scrollDrag = true;
                            if($.isFunction(state.opts.onDragStart)){
                                state.opts.onDragStart();
                            }
                        }
                    });
                    state.on('mousedown', function () {
                        md = true;
                    });
                    state.on('mouseup', function () {
                        md = false;
                        if(_scrollDrag){
                            state.opts.onDragStop();
                        }
                        _scrollDrag = false;
                    });
                }
            },
            start: function(){
                if(_started || !_enabled){
                    return false;
                }
                _started=true;
                state.top = state.scrollTop();
                var _init = false;
                var down = true;
                var dif = 0;
                var tmr;
                function wait(){
                    tmr = window.setTimeout(function(){
                        scrollEnd();
                    },  state.opts.delay);
                }
                function scrollEnd(){
                    window.clearTimeout(tmr);
                    if(_enabled) {
                        var st = state.scrollTop();
                        if(!_init && st!==state.top){
                            _init=true;
                            dif=st;
                        }
                        if(_init) {
                            if(st!==state.top){
                                down = (st>state.top);
                            }
                            if ((st === state.top)) {
                                methods.stopped(down, down ? st-dif : dif-st);
                                methods.top();
                                methods.bottom();
                                _init = false;
                            }
                            else {
                                state.top = st;
                            }
                        }
                    }
                    wait();
                }
                wait();

                if(state.opts.start && $.isFunction(state.opts.start)){
                    state.opts.start();
                }
            },
            stop: function(){
                _enabled = false;
            },
            stopped: function(down, dif){
                if(_enabled && $.isFunction(state.opts.isStopped)) {
                   // _enabled = false;
                    if(state.opts.loaders) {
                        if(state.loaders('exists')){
                            state.loaders('show');
                        }
                        else {
                            state.loaders({type: 'cube', cover: 'true'});
                        }
                    }
                    var p = true;
                    if(state.opts.disableStopOnDrag){
                        p = !_scrollDrag;
                    }
                    if(p) {
                        state.opts.isStopped(down, dif, methods.isBottom());
                    }
                }
            },
            top: function(){
                if(_enabled && state.opts.enable.top) {
                    var process = state.scrollTop() === 0;
                    if (process) {
                        if (state.opts.top && $.isFunction(state.opts.top)) {
                            state.opts.top();
                        }
                    }
                }
            },
            bottom: function(){
                if(_enabled && state.opts.enable.bottom) {
                    var st = state.scrollTop();
                    if (methods.isBottom()) {
                        if (state.opts.bottom && $.isFunction(state.opts.bottom)) {
                            methods.stop();
                            state.opts.bottom();
                        }
                    }
                }
            }
        };
        state.start = function (opt) {
            if(state.opts.loaders && state.loaders('exists')){
                state.loaders('hide');
            }
            _enabled = true;
            if(opt && opt.top){
                state.top = opt.top;
            }
            if(!_started) {
                methods.start();
            }
        };
        state.setHeight = function (options) {
            state.top = options;
        };
        //public methods
        state.isBottom = function () {
            methods.isBottom();
        };
        state.disabled = function (a) {
            if($.isFunction(a.onChange)){
                _enabled = !a.disabled;
                a.onChange();
            }
            else {
                _enabled = !a;
            }
        };

        state.stop = function (a) {
            _enabled = false;
        };

        state.isInViewport = function(el){
            //special bonus for those using jQuery
            if (typeof jQuery === "function" && el instanceof jQuery) {
                el = el[0];
            }

            var rect = el.getBoundingClientRect();

            return (
                rect.top >= 0 &&
                rect.left >= 0 &&
                rect.bottom <= (state.height() || window.innerHeight || document.documentElement.clientHeight) && /*or $(window).height() */
                rect.right <= (state.width() || window.innerWidth || document.documentElement.clientWidth) /*or $(window).width() */
            );
        };

        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.scrollHandler.defaults = {
        enable:{
            bottom: true,
            top: true
        },
        stopped: true,
        delay: 50,
        init: 1000,
        adjust: 1
    };


    //Plugin Function
    $.fn.scrollHandler = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.scrollHandler($(this),method);
            });
        } else {
            try {
                // Helper strings to quickly perform functions
                var $scrollHandler = $(this).data('scrollHandler');
                switch (method) {
                    case 'exists':
                        return (null !== $scrollHandler && undefined !== $scrollHandler && $scrollHandler.length > 0);
                        break;
                    case 'isInViewport':
                        return $scrollHandler.isInViewport(options);
                        break;
                    case 'isBottom':
                        return $scrollHandler.isBottom();
                        break;
                    case 'disabled':
                        $scrollHandler.disabled(options);
                        break;
                    case 'stop':
                        $scrollHandler.stop(options);
                        break;
                    case 'start':
                        $scrollHandler.start(options);
                        break;
                    case 'setHeight':
                        $scrollHandler.setHeight(options);
                    case 'state':
                    default:
                        return $scrollHandler;
                }
            }
            catch (e){
                return false;
            }
        }
    };

})(jQuery);
