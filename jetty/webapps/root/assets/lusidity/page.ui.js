

;(function ($) {

    //Object Instance
    $.lusidityUI = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.lusidityUI.defaults, options);

        // Store a reference to the environment object
        el.data("lusidityUI", state);

        // Private environment methods
        methods = {
            init: function() {
                methods.override();
                pageLoader.start(function () {
                    var ev = $.Event("lusidityReady");
                    ev.auth = state.opts;
                    state.trigger(ev);
                });
            },
            override: function () {
                var originalAddClassMethod = jQuery.fn.addClass;
                jQuery.fn.addClass = function () {
                    var result = originalAddClassMethod.apply(this, arguments);
                    var event = $.Event('addClassTrigger', arguments);
                    this.trigger(event);
                    var node = $(this);
                    methods.fillNode(node, arguments);
                    return result;
                };
                var originalRemoveClassMethod = jQuery.fn.removeClass;
                jQuery.fn.removeClass = function () {
                    var result = originalRemoveClassMethod.apply(this, arguments);
                    var event = $.Event('removeClassTrigger', arguments);
                    this.trigger(event);
                    return result;
                };

                var originalRemoveMethod = jQuery.fn.remove;
                jQuery.fn.remove = function () {
                    var result = originalRemoveMethod.apply(this, arguments);
                    var event = $.Event('removeNodeTrigger', arguments);
                    var node = $(this);
                    if(node.cover){
                        node.cover.remove();
                    }
                    this.trigger(event);
                    return result;
                };

                var originaltooltip = jQuery.fn.tooltip;
                jQuery.fn.tooltip = function () {
                    var result = originaltooltip.apply(this, arguments);
                    var event = $.Event('tooltipTrigger', arguments);
                    var node = $(this);
                    var c = node.css('cursor');
                    if(!c || $.jCommon.string.equals(c, 'pointer', true)){
                        node.css({cursor: 'help'});
                    }
                    this.trigger(event);
                    return result;
                };
            },
            getInt: function (v) {
                var r;
                try{
                    r = parseInt(v.replace('px'));
                }
                catch(e){r=0;}
                return r
            },
            fillNode: function (node, arguments) {
                function size(node, props){
                    var offset = node.offset();
                    var on = 0;
                    if(offset.top===0 && on<100){
                        window.setTimeout(function () {
                            on++;
                            size(node, props);
                        },10);
                    }
                    else {
                        window.setTimeout(function () {
                            var mt = node.css('margin-top');
                            var mb = node.css('margin-top');
                            mt = methods.getInt(mt);
                            mb = methods.getInt(mb);
                            var oh = node.attr('data-oh');
                            oh = methods.getInt(oh);
                            var h = (((($(window).height() - offset.top)-mt)-mb)-oh) + 'px';
                            var styles = {};
                            $.each(props, function () {
                                var p = this.toString();
                                switch(p) {
                                    case 'height':
                                    case 'minHeight':
                                    case 'maxHeight':
                                        styles[p] = h;
                                        break;
                                }
                            });
                            node.css(styles);
                            lusidity.environment('onResize', function () {
                                size(node, props);
                            });
                        }, 100);
                    }

                }
                if($.jCommon.string.contains(arguments[0], "full-min")){
                    size(node, ['minHeight', 'height']);
                }
                else if($.jCommon.string.contains(arguments[0], "full-max")){
                    node.css({overflow: 'auto'});
                    size(node, ['minHeight', 'height', 'maxHeight']);
                }
            }

        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.lusidityUI.defaults = {};


    //Plugin Function
    $.fn.lusidityUI = function(method, options) {
        if (method === undefined) method = {};
        var $lusidityUI = $(this).data('lusidityUI');
        if ($lusidityUI === undefined && typeof method === "object") {
            return this.each(function() {
                new $.lusidityUI($(this), method);
            });
        } else  if(typeof method !== "object") {
            // Helper strings to quickly perform functions
            switch (method) {
                case 'exists': return (null!==$lusidityUI && undefined!==$lusidityUI && $lusidityUI.length>0);
                case 'state':
                default: return $lusidityUI;
            }
        }
    };

})(jQuery);
