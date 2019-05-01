

;(function ($) {

    //Object Instance
    $.importerMatchedOn = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.dataList.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.on = 1;

        // Store a reference to the environment object
        el.data("importerMatchedOn", state);

        // Private environment methods
        methods = {
            init: function() {                     
                state.worker.node.attr('data-valid', false).hide();
                state.body = $.htmlEngine.panel(
                    state.worker.node, state.opts.glyph, state.opts.title, state.opts.url ? state.opts.url : null /* url */, false /* borders */
                );
                methods.adjustHeight();

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
                    var lid = state.worker.data.lid;
                    var u = methods.getQueryUrl(state.start, state.opts.limit);
                    var q = {
                        domain: '/object/edge/property_match_edge',
                        type: '/electronic/network/asset',
                        sort: {on: "createdWhen", direction: "desc"},
                        lid: lid,
                        "native": {
                            query: {
                                filtered: {
                                    filter: {
                                        bool: {
                                            must: [
                                                {term: {"/object/endpoint/endpointFrom.relatedId.raw": lid}}
                                            ]
                                        }
                                    }
                                }
                            }
                        }
                    };
                    $.htmlEngine.request(u, s, f, q, 'post');
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
                    var results = data.results;
                    $.each(results, function(){
                        var node = null;
                        var result = dCrt('div').addClass('data-list');
                        state.body.append(result);
                        var aged = $.jCommon.string.startsWith(this.title, "asset deprecated", true);
                        var t = this.title + (aged ? '' : ' matched on');
                        if(this.lastSeen){
                            node = dCrt('p').html(String.format('{0} <strong>{1}</strong>: {2} <strong>last seen on</strong>: {3} <strong>processed on</strong>: {4}', t, this.key, this.value,
                                $.jCommon.dateTime.defaultFormat(this.lastSeen), $.jCommon.dateTime.defaultFormat(this.createdWhen)));
                        }
                        else{
                            node = dCrt('p').html(String.format('{0} <strong>{1}</strong>: {2} <strong>processed on</strong> {3}', t, this.key, this.value, $.jCommon.dateTime.defaultFormat(this.createdWhen)));
                        }

                        result.append(node);
                        var items = this['/object/importer/disambiguated_item/disambiguatedItem'];
                        if(items){
                            var ul = dCrt('ul');
                            result.append(ul);
                            $.each(items, function () {
                                var item = this;
                                var li = dCrt('li');
                                ul.append(li);
                                li.html(String.format(('{0}{1}: {2}'), (aged ? '': 'disambiguated on '), item.key, item.value));
                            });
                        }
                    });
                }
            }
        };
        //public methods
        //environment: Initialize
        methods.init();
    };

    // if you want the zone to adjust its height set adjustHeight to 0 or more.
    //Default Settings
    $.importerMatchedOn.defaults = {
        limit: 100,
        hasItemCounter: true,
        paging: true,
        hasHeader: true
    };

    //Plugin Function
    $.fn.importerMatchedOn = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.importerMatchedOn($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $importerMatchedOn = $(this).data('importerMatchedOn');
            switch (method) {
                case 'state':
                default: return $importerMatchedOn;
            }
        }
    };

    $.importerMatchedOn.call= function(elem, options){
        elem.importerMatchedOn(options);
    };

    try {
        $.htmlEngine.plugins.register("importerMatchedOn", $.importerMatchedOn.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
