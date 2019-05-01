;(function ($) {

    //Object Instance
    $.networkAdapters = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.networkAdapters.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_PRIMARY = "/electronic/network/network_adapter/networkAdapters";
        state.KEY_SECONDARY = "/electronic/network/invalid_net_adapter/invalidNetAdapters";
        state.boldRequired = false;
        // Store a reference to the environment object
        el.data("networkAdapters", state);

        // Private environment methods
        methods = {
            init: function() {
                state.worker.node.attr('data-valid', false).hide();
                state.body = $.htmlEngine.panel(
                    state.worker.node, state.opts.glyph, state.opts.title, state.opts.url ? state.opts.url : null /* url */, false /* borders */
                );
                methods.adjustHeight();
                if (!state.opts.hasHeader) {

                    state.worker.node.panel('hideHeader');
                }
                if (state.opts.body) {
                    if (state.opts.body.attr) {
                        $.htmlEngine.addAttributes(state.opts.body, state.body);
                    }
                    if (state.opts.body.css) {
                        $.htmlEngine.addStyling(state.opts.body, state.body);
                    }
                    if (state.opts.body.cls) {
                        state.body.addClass(state.opts.body.cls);
                    }
                }
                var s = function (data) {
                    state.worker.node.panel('notBusy');
                    if (data && data.results) {
                        state.start +=  data.results.length;
                        state.next = data.next;
                        state.hits = $.jCommon.is.numeric(data.hits) ? data.hits : state.start;
                        if (state.opts.hasItemCounter) {
                            state.worker.node.panel('updateHeader', {
                                glyph: state.opts.glyph,
                                title: String.format("{0} {1}/{2}", state.opts.title, ((state.start > state.hits) ? state.hits : state.start), state.hits),
                                tip:String.format("{0}", state.opts.tip)
                            });
                        }
                        else if (state.opts.tip) {
                            state.worker.node.panel('updateHeader', {
                                glyph: state.opts.glyph,
                                title: String.format("{0}", state.opts.title),
                                tip:String.format("{0}", state.opts.tip)
                            });
                        }
                        methods.html.init(data);
                        state.worker.node.attr('data-valid', true).show();
                    }
                    else{
                        if(state.opts.missing) {
                            state.worker.node.panel('updateHeader', {
                                glyph: state.opts.glyph,
                                title: String.format("{0}", state.opts.title)
                            });
                            state.worker.node.attr('data-valid', true).show();
                            methods.html.putMissing();
                        }
                    }
                    state.body.scrollHandler('start');
                };
                var f = function () {
                    state.worker.node.panel('notBusy');
                    state.body.scrollHandler('start');
                };

                function page() {
                    state.worker.node.panel('busy');
                    if (!state.start) {
                        state.start = 0;
                    }
                    if (!state.hits || state.start < state.hits) {
                        var url = state.worker.data[state.KEY_ID] + '/properties/';
                        if (state.opts.limit) {
                            url += '?start=' + state.start + '&limit=' + state.opts.limit;
                        }
                        var r = state.worker.data[state.opts.property];
                        if (r) {
                            var data = {results: []};
                            if (r.results) {
                                data.results = r.results
                            }
                            else if ($.jCommon.is.array(r)) {
                                data.results = r;
                            }
                            else {
                                data.results.push(r);
                            }
                            s(data);
                        }
                        else {
                            $.htmlEngine.request(url, s, f, null, 'get');
                        }

                    }
                    else {
                        state.worker.node.panel('notBusy');
                    }
                }
                state.body.scrollHandler({
                    adjust: 10,
                    start: function () {
                    },
                    stop: function () {
                    },
                    top: function () {
                    },
                    bottom: function () {
                        if (state.start < state.hits) {
                            page();
                        }
                    }
                });
                page();
            },
            adjustHeight: function () {
                if (state.opts.adjustHeight || state.opts.fill) {
                    $.htmlEngine.adjustHeight(state, state.opts.fill, state.opts.adjustHeight, false);
                }
            },
            getUrl: function (url, start, limit) {
                if (undefined === start) {
                    start = 0;
                }
                if (undefined === limit) {
                    limit = 0;
                }
                return url + ($.jCommon.string.contains(url, "?") ? '&' : '?') + 'start=' + start + '&limit=' + limit;
            },
            getQueryUrl: function (start, limit) {
                return methods.getUrl('/query', start, limit);
            },
            resize: function () {
                var h = state.body.availHeight();
                state.body.css({height: h + 'px', maxHeight: h+'px', overflowY: 'auto', overflowX: 'hidden'});
            },
            addDemoData: function (results,counter) {
            var clone1 = results.slice(0);
            for(var i = 0, l = counter; i < l; i++ ) results = $.merge($.merge([], results), clone1);
            return results;
            },
            html:{
                init: function(data){
                    var results = data.results;
                    var sort = [];
                    if(state.opts.sort){
                        $.each(state.opts.sort, function (key, value) {
                            sort.push({property: key, asc: $.jCommon.string.equals(value, "asc")});
                        })
                    }
                    else {
                        sort.push({property: "title", asc: true});
                    }
                    if(state.opts.boldFirst){
                        state.boldRequired = true;
                    }
                    var count = 0;
                    results = $.jCommon.array.sort(results, sort);
                    $.each(results, function(){
                        var item = this;
                        var result = $(document.createElement('div')).addClass('data-list');
                        if(state.opts.properties){
                            var on = 0;
                            $.each(state.opts.properties, function(){
                                var value = $.jCommon.json.getProperty(item, this.property);
                                if((null!==value) && this.dataType && $.jCommon.string.equals(this.dataType, "date")){
                                    value = $.jCommon.dateTime.defaultFormat(value);
                                }
                                if(value) {
                                    if (on > 0) {
                                        if (state.opts.blocked) {
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
                                    var type = "div";
                                    var node;
                                    if (state.boldRequired && count < 1) {
                                        node = $(document.createElement(type)).addClass('data-value').html(value).css({fontWeight: 'bold'});
                                    }
                                    else {
                                        node = $(document.createElement(type)).addClass('data-value').html(value);
                                    }
                                    result.append(node);
                                    on++;
                                }
                            });
                        }
                        if(result.children().length>0) {
                            state.body.append(result);
                        }
                        count ++;
                    });
                },
                putMissing: function(){
                    var missing = state.opts.missing;
                    var result = dCrt('div');
                    var p = dCrt('p').html(missing);
                    result.append(p);
                    state.body.append(result);
                }

            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.networkAdapters.defaults = {
        limit: 100,
        hasItemCounter: true,
        paging: true,
        hasHeader: true,
        missing: null
    };

    //Plugin Function
    $.fn.networkAdapters = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.networkAdapters($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $networkAdapters = $(this).data('networkAdapters');
            switch (method) {
                case 'state':
                default: return $networkAdapters;
            }
        }
    };

    $.networkAdapters.call= function(elem, options){
        elem.networkAdapters(options);
    };

    try {
        $.htmlEngine.plugins.register("networkAdapters", $.networkAdapters.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
