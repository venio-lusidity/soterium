;(function ($) {

    //Object Instance
    $.loaders = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.loaders.defaults, options);
        state.root = '/assets/js/loaders/css/';
        state.loader = null;
        state.cover = null;

        // Store a reference to the environment object
        el.data("loaders", state);

        // Private environment methods
        methods = {
            init: function() {
                state.css({position: 'relative'});
                $.htmlEngine.loadFiles(state, null, [
                    state.root + 'loader.css?nocache=' + $.jCommon.getRandomId('nc'),
                    state.root + state.opts.type + '.css?nocache=' + $.jCommon.getRandomId('nc')
                ]);
                var on = 0;
                state.on('cssFileLoaded', function () {
                    if (on === 1) {
                        state.unbind('cssFileLoaded');
                        methods.html.init();
                    }
                    on++;
                });
            },
            clear: function(){
                if(null!==state.cover){
                    state.removeAttr('cover-id');
                    state.cover.remove();
                    state.cover = null;
                }
                if(null!==state.loader){
                    state.loader.remove();
                    state.loader = null;
                }
            },
            cover: function () {
                var sw;
                function  cover() {
                    if(null===state.cover){
                        var id = $.jCommon.getRandomId("cover");
                        state.cover = dCrt("div").addClass('page-cover').attr('id', id);
                        var h = state.height();
                        state.attr('cover-id', id);
                        $("body").append(state.cover);
                    }
                    if(state.height()>0) {
                        var offset = state.offset();
                        var y = offset.top;
                        var x = offset.left;
                        y = (y <= 0) ? 0 : y + 'px';
                        x = (x <= 0) ? 0 : x + 'px';
                        var h = (state.opts.height ? state.opts.height : state.availHeight(0) + state.opts.adjustHeight) + 'px';
                        var w = (state.opts.width ? state.opts.width : state.width() + state.opts.adjustWidth) + 'px';
                        state.cover.css({
                            minHeight: h,
                            height: h,
                            maxHeight: h,
                            minWidth: w,
                            maxWidth: w,
                            width: w,
                            position: "absolute",
                            top: y,
                            left: x
                        });
                    }
                    else{
                        sw = new oStopWatch();
                        sw.wait(100, cover);
                    }
                }
                cover();
            },
            getSize: function(elem, size){
                var h = elem.height()>0 ? elem.height() :elem.innerHeight();
                var w = elem.height()>0 ? elem.width() : elem.innerWidth();

                return (h>0 && w>0) ? {w: w, h:h} : false;
            },
            resize: function(){
                if(!state.loader){
                    return false;
                }
                var s2 = methods.getSize(state.loader);
                if(s2) {
                    var s1 = methods.getSize(state, s2);
                    if(!s1){
                        state.css({minHeight: (s2.h + 10) +'px'});
                        s1 = methods.getSize(state, s2);
                    }

                    if (s1) {
                        //  state.blind.width(s1.w).height(s1.h);
                        var left = (s1.w / 2) - (s2.w / 2);
                        var top = (s1.h / 2) - (s2.h / 2);
                        state.loader.css({top: state.opts.top ? state.opts.top : top + 'px', left: state.opts.left ? state.opts.left : left + 'px'});
                    }
                    else {
                        attempt();
                    }
                }
                else{
                    attempt();
                }

                function attempt(){
                    window.setTimeout(function () {
                        methods.resize()
                    }, 100);
                }
            },
            html:{
                init: function () {
                    methods.clear();
                    if(state.opts.cover){
                        methods.cover();
                        state.on("remove", function () {
                            if(state.cover){
                                state.cover.remove();
                            }
                        });
                    }
                    methods.html.create();
                },
                create: function(){
                    var node = state.opts.cover ? state.cover : state;
                    state.loader = dCrt('div').addClass('ls-' + state.opts.type);
                    if(state.opts.cls){
                        state.loader.addClass(state.opts.cls);
                    }
                    if(state.opts.css) {
                        state.loader.css(state.opts.css);
                    }
                    if(state.opts.pre){
                        node.prepend(state.loader);
                    }
                    else {
                        node.append(state.loader);
                    }
                    methods[state.opts.type].create();
                    methods.resize();
                }
            },
            cube: {
                create: function () {
                    for(var i=1;i<10;i++){
                        var cube = dCrt('div').addClass('ls-cube' + i);
                        state.loader.append(cube);
                    }
                }
            },
            cylon: {
                create: function(){
                    var node = dCrt('div').addClass('ls-cylon-loader');
                    state.loader.append(node);
                }
            },
            dots: {
                create: function () {
                    var node = dCrt('div').addClass("spinner");
                    if(state.opts.dots && state.opts.dots.squared){
                        var sq = state.opts.dots.squared;
                        node.css({width: sq+'em', height: sq+'em'});
                    }
                    for (var i = 1; i < 8; i++) {
                        var n = dCrt('div').addClass("dot-holder");
                        var dot = dCrt('div').addClass("dot");
                        node.append(n.append(dot));
                    }
                    state.append(node);
                }
            },
            spinner: {
                create: function(){
                    var node = dCrt('div').addClass('cube');
                    state.append(node);
                }
            }

        };
        //public methods
        state.display = function(options){
            methods.clear();
            methods.html.init();
            if($.isFunction(options)) {
                options();
            }
        };
        state.hidden = function(options){
            state.css({minHeight: ''});
            window.setTimeout(function () {
                if ($.isFunction(options)) {
                    options();
                }
                methods.clear();
            }, (options && options.delay ? options.delay : state.opts.delay));
        };
        state.fadeTo = function(elem){
            state.loader.fadeOut('slow', function(){
                elem.fadeIn('slow');
            });
        };

        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.loaders.defaults = {
        pre: false,
        css: {},
        delay: 300,
        adjustWidth: 0,
        adjustHeight: 0
    };


    //Plugin Function
    $.fn.loaders = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.loaders($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $loaders = $(this).data('loaders');
            if($loaders) {
                switch (method) {
                    case 'show':
                        $loaders.display(options);
                        break;
                    case 'hide':
                        $loaders.hidden(options);
                        break;
                    case 'fadeTo':
                        $loaders.fadeTo(options);
                        break;
                    case 'exists': return true; break;
                    case 'state':
                    default:
                        return $loaders;
                }
            }else{
                return false;
            }
        }
    };

})(jQuery);
