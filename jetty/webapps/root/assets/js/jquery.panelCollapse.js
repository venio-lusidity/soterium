
;(function ($) {

    //Object Instance
    $.panelCollapse = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.panelCollapse.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.configs = [];
        var glyph = 'glyphicons glyphicons-chevron-';
        var col = 'col-md-';

        // Store a reference to the environment object
        el.data("panelCollapse", state);

        // Private environment methods
        methods = {
            init: function() {
                $.each(state.opts.panels, function () {
                   methods.handle(this); 
                });
            },
            handle: function (item) {
                if(!item.config) {
                    var config = methods.config(item.node);
                    item.config = config;
                }
                config.open = true;
                if(config && config.node){                     
                    if(item.collapsable){
                        config.node.css({position: 'relative'});
                        var w = config.node.parent().width();
                        var nw = config.node.width();
                        var mid = w/2;
                        var l = config.node.offset().left;
                        var p = l+nw;
                        config.isLeft = (p<mid);
                        config.left =(config.isLeft?(nw+15):-4 + 'px');
                        config.justify = (config.isLeft ? 'left' : 'right');
                        config.css = {};
                        config.css[config.isLeft ? 'right' : 'left'] = '-5px';
                        var cls = 'collapse-tab-' + config.justify;
                        var tab = $(document.createElement('div')).addClass(cls).css(config.css);
                        config.chevron = $(document.createElement('span')).addClass(glyph + config.justify);
                        tab.append(config.chevron);
                        config.node.append(tab);
                        tab.on('click', function () {
                            methods.collapse(this, item, config);
                        });
                    }else{
                        state.expandable = item;
                    }
                    state.configs.push(config);
                }    
            },
            collapse: function (node, item, config) {
                if(config.open) {
                    config.chevron.removeClass(glyph + (config.isLeft ? 'left' : 'right')).addClass(glyph + (config.isLeft ? 'right' : 'left'));
                    item.config.node.removeClass(col + item.config.cols).addClass(col+(item.config.cols-(item.config.cols-1)));
                    var r = (col + state.expandable.config.cols);
                    state.expandable.config.cols += (config.cols-1);
                    var a= (col + state.expandable.config.cols);
                    state.expandable.config.node.removeClass(r).addClass(a);
                    config.open=false;
                }
                else{
                    config.chevron.removeClass(glyph + (config.isLeft ? 'right' : 'left')).addClass(glyph + (config.isLeft ? 'left' : 'right'));
                    var r = (col + state.expandable.config.cols);
                    state.expandable.config.cols -= (config.cols-1);
                    var a= (col + state.expandable.config.cols);
                    state.expandable.config.node.removeClass(r).addClass(a);
                    item.config.node.addClass(col+item.config.cols).removeClass(col+(item.config.cols-(item.config.cols-1)));
                    config.open=true;
                }
                state.trigger('panel-collapse', {item: item, node: node});
            },
            config: function (node) {
                var r = {};
                var cls = node.attr("class");
                if(!$.jCommon.string.contains(cls, col, true)){
                    if(node.parent().length>0) {
                        r = methods.config(node.parent());
                    }
                }
                else{
                    var p = cls.split(" ");
                    $.each(p, function () {
                        if($.jCommon.string.contains(this, col, true)){
                            r.cls = this;
                            return false;
                        }
                    });
                    if(r.cls){
                        r.cols = parseInt(r.cls.replace("col-md-", ""));
                        r.node = node;
                    }
                }
                return r;
            }
        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.panelCollapse.defaults = {
        
    };


    //Plugin Function
    $.fn.panelCollapse = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.panelCollapse($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $panelCollapse = $(this).data('panelCollapse');
            switch (method) {
                case 'exists': return (null!==$panelCollapse && undefined!==$panelCollapse && $panelCollapse.length>0);
                case 'state':
                default: return $panelCollapse;
            }
        }
    };

})(jQuery);
