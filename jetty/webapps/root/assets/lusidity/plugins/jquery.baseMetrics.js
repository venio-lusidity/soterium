

;(function ($) {

    //Object Instance
    $.baseMetrics = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.baseMetrics.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("baseMetrics", state);

        // Private environment methods
        methods = {
            init: function() {
                state.worker.node.attr('data-valid', false).hide();
                var s = function(data){
                    if(data && data.results) {
                        methods.html.init(data);
                        state.worker.node.attr('data-valid', true).show();
                    }
                };
                var f = function(){
                };
                
                if(state.opts.query && state.opts.query.fn){
                    var fn = QueryFactory[state.opts.query.fn];
                    var q = fn(state.worker.data);
                    $.htmlEngine.request(methods.getQueryUrl(0, state.opts.limit), s, f, q, "post");
                }
                else {
                    var url = state.worker.data[state.KEY_ID] + "/properties" + state.opts.property;
                    if (state.opts.limit) {
                        url += "?limit=" + state.opts.limit;
                    }
                    var results = state.worker.data[state.opts.property];
                    if (results) {
                        var data = {results: results};
                        s(data);
                    }
                    else {
                        $.htmlEngine.request(url, s, f, null, 'get');
                    }
                }
            },
            getQueryUrl: function (start, limit) {
                if (undefined == start) {
                    start = 0;
                }
                if (undefined == limit) {
                    limit = 0;
                }
                return '/query?start=' + start + '&limit=' + limit;
            },
            resize: function () {
                var h = state.body.availHeight();
                state.body.css({height: h + 'px', maxHeight: h+'px', overflowY: 'auto', overflowX: 'hidden'});
            },
            html:{
                init: function(data){
                    state.body=$.htmlEngine.panel(
                        state.worker.node,state.opts.glyph, state.opts.title, state.opts.url /* url */, false /* borders */
                    );
                    var results = data.results;
                    $.each(results, function(){
                        var item = this;
                        var result = $(document.createElement('div')).addClass('data-list');
                        if(state.opts.properties){
                            var on = 0;
                            $.each(state.opts.properties, function(){
                                var value = $.jCommon.json.getProperty(item, this.property);
                                if(value) {
                                    if (on > 0) {
                                        if(state.opts.blocked){
                                            result.append("<br/>")
                                        }
                                        else {
                                            var splitter = $(document.createElement("span")).html("|").css({margin: '0 5px 0 5px'});
                                            result.append(splitter);
                                        }
                                    }
                                    if (this.label) {
                                        var lbl = $(document.createElement('div')).addClass('data-label').html(this.label);
                                        result.append(lbl);
                                    }
                                    var type = this.linked ? "a" : "div";
                                    var node = $(document.createElement(type)).addClass('data-value').html(value);
                                    if(this.linked){
                                        var key = this.hrefProperty ? this.hrefProperty : state.KEY_ID;
                                        var href = item[key];
                                        if(this.href){
                                            href = $.jCommon.string.replaceAll(this.href, "\\[href\\]", item[key]);
                                        }
                                        node.attr('href', href).attr('target', '_blank');
                                    }
                                    result.append(node);
                                    on++;
                                }
                            })
                        }
                        if(result.children().length>0) {
                            state.body.append(result);
                        }
                    });
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.dataList.defaults = {
        limit: 100
    };

    //Plugin Function
    $.fn.baseMetrics = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.baseMetrics($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $baseMetrics = $(this).data('baseMetrics');
            switch (method) {
                case 'state':
                default: return $baseMetrics;
            }
        }
    };

    $.baseMetrics.call= function(elem, options){
        elem.baseMetrics(options);
    };

    try {
        $.htmlEngine.plugins.register("baseMetrics", $.baseMetrics.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
