

;(function ($) {

    //Object Instance
    $.schemaList = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.schemaList.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.on = 1;

        // Store a reference to the environment object
        el.data("schemaList", state);

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
                        state.start += data.results.length;
                        state.hits = $.jCommon.is.numeric(data.hits) ? data.hits : state.start;
                        if (state.opts.hasItemCounter) {
                            state.worker.node.panel('updateHeader', {
                                glyph: state.opts.glyph,
                                title: String.format("{0} {1}/{2}", state.opts.title, ((state.start > state.hits) ? state.hits : state.start), state.hits)
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
                        if (state.opts.query && state.opts.query.fn) {
                            var fn = QueryFactory[state.opts.query.fn];
                            var q = fn(state.worker.data);
                            if ($.jCommon.is.string(q)) {
                                $.htmlEngine.request(methods.getUrl(q, state.opts.start, state.opts.limit), s, f, null, 'get');
                            }
                            else {
                                $.htmlEngine.request(methods.getQueryUrl(state.start, state.opts.limit), s, f, q, 'post');
                            }

                        }
                        else if($.jCommon.json.hasProperty(state.worker.data, state.opts.property)){
                            var v = $.jCommon.json.getProperty(state.worker.data, state.opts.property);
                            if(!v.results){
                                if($.jCommon.is.array(v)){
                                    v = { results: v};
                                }
                                else{
                                    var t = {results: []};
                                    t.push(v);
                                    v=t;
                                }
                            }
                            v.hits = v.results.length;
                            v.next = v.hits;
                            s(v);
                        }
                        else {
                            var url = state.worker.data[state.KEY_ID] + '/properties' + state.opts.property;
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
                return url + '?start=' + start + '&limit=' + limit;
            },
            getQueryUrl: function (start, limit) {
               return methods.getUrl('/query', start, limit);
            },
            resize: function () {
                var h = state.body.availHeight();
                state.body.css({height: h + 'px', maxHeight: h+'px', overflowY: 'auto', overflowX: 'hidden'});
            },
            html:{
                init: function(data){
                    if(!state.opts.view){
                        return false;
                    }
                    var results = data.results ? data.results : data;
                    if(!$.jCommon.is.array(results)){
                        var t = [];
                        t.push(results);
                        results = t;
                    }
                    var sort = [];
                    if(state.opts.sort){
                        $.each(state.opts.sort, function (key, value) {
                            sort.push({property: key, asc: $.jCommon.string.equals(value, "asc")});
                        })
                    }
                    state.body.schemaEngine({isView: true, view: state.opts.view});
                    results = $.jCommon.array.sort(results, sort);
                    $.each(results, function () {
                        var schema = state.body.schemaEngine('state').opts.schema;
                        if(schema.cls){
                            state.body.addClass(schema.cls);
                        }
                        state.body.htmlEngine({ schema: schema, data: this, autoHide: true });
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

    // if you want the zone to adjust its height set adjustHeight to 0 or more.
    //Default Settings
    $.schemaList.defaults = {
        limit: 100,
        hasItemCounter: true,
        paging: true,
        hasHeader: true,
        missing: null
    };


    //Plugin Function
    $.fn.schemaList = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.schemaList($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $schemaList = $(this).data('schemaList');
            switch (method) {
                case 'state':
                default: return $schemaList;
            }
        }
    };

    $.schemaList.call= function(elem, options){
        elem.schemaList(options);
    };

    try {
        $.htmlEngine.plugins.register("schemaList", $.schemaList.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
