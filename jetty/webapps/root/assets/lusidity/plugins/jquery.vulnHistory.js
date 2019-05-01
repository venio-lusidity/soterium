;(function ($) {

    //Object Instance
    $.vulnHistory = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.vulnHistory.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.on = 1;

        // Store a reference to the environment object
        el.data("vulnHistory", state);

        // Private environment methods
        methods = {
            init: function() {
                //http://webdesign.tutsplus.com/tutorials/building-a-vertical-timeline-with-css-and-a-touch-of-javascript--cms-26528
                // http://bashooka.com/coding/javascript-timeline-libraries/
                // use wk 2014 as the example.
                state.worker.node.attr('data-valid', false).hide();

                state.body=$.htmlEngine.panel(
                    state.worker.node, state.opts.glyph, state.opts.title, state.opts.url /* url */, false /* borders */
                );

                $.htmlEngine.adjustHeight(state.body, true, state.opts.adjustHeight, false);

                if(state.opts.body) {
                    if(state.opts.body.attr) {
                        $.htmlEngine.addAttributes(state.opts.body, state.body);
                    }
                    if(state.opts.body.css) {
                        $.htmlEngine.addStyling(state.opts.body, state.body);
                    }
                    if(state.opts.body.cls) {
                        state.body.addClass(state.opts.body.cls);
                    }
                }

                var s = function(data){
                    if(data && data.results) {
                        state.start += data.results.length;
                        state.hits = data.hits;
                        state.worker.node.panel('updateHeader', {glyph: state.opts.glyph, title: String.format("{0} {1}/{2}", state.opts.title, state.start, state.hits)});
                        methods.html.init(data);
                        state.worker.node.attr('data-valid', true).show();
                    }
                    state.body.scrollHandler('start');
                };

                var f = function(){
                    state.body.scrollHandler('start');
                };

                function page() {
                    if(!state.start){
                        state.start = 0;
                    }
                    if(!state.hits || state.start<state.hits) {
                        var url = state.worker.data[state.KEY_ID] + "/vulnerability/history?start="+state.start;
                        if (state.opts.limit) {
                            url += "&limit=" + state.opts.limit;
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
            getQueryUrl: function (start, limit) {
                if (undefined === start) {
                    start = 0;
                }
                if (undefined === limit) {
                    limit = 0;
                }
                return '/query?start=' + start + '&limit=' + limit;
            },
            html:{
                init: function(data){
                    var results = data.results;
                    results = $.jCommon.array.sort(results, [{property: "title", asc: true}]);
                    $.each(results, function(){
                        var item = this;
                        var result = $(document.createElement('div')).addClass('data-list');
                        var sp = "&nbsp;|&nbsp;";
                        var key = '/technology/security/vulnerabilities/vulnerability_entry/history';
                        var v = item[key];
                        var sv;
                        try{
                            sv = $.jCommon.string.replaceAll(item.severity, '_', ' ');
                        }catch(e){}
                        if(v && v.results) {
                            $.each(v.results, function () {
                                var subItem = this;
                                if (!init) {
                                    init = true;
                                    var init = false;
                                    var lbl = dCrt('div').addClass('data-label').html(String.format("{0}. Vulnerability:", state.on));
                                    var node = dCrt('a').attr('href', subItem.vulnId).html(subItem[state.KEY_TITLE]);
                                    result.append(lbl).append(node);
                                    if(sv){
                                        result.append(dCrt('span').html('&nbsp;(' + sv + ')'));
                                    }
                                    result.append(dCrt('br'));
                                }
                                var t = subItem.resource;
                                t = FnFactory.toTitleCase(t);
                                var sb = dCrt('span').html(t);
                                var dt = dCrt('span').html($.jCommon.dateTime.defaultFormat(subItem.scannedOn));
                                var ps = dCrt('span').html(subItem.result);
                                result.append(sb).append(dCrt('span').html(sp)).append(ps).append(dCrt('span').html(sp)).append(dt).append(dCrt('br'));
                            });
                        }

                        state.on++;
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
    $.vulnHistory.defaults = {
        limit: 100
    };


    //Plugin Function
    $.fn.vulnHistory = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.vulnHistory($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $vulnHistory = $(this).data('vulnHistory');
            switch (method) {
                case 'state':
                default: return $vulnHistory;
            }
        }
    };

    $.vulnHistory.call= function(elem, options){
        elem.vulnHistory(options);
    };

    try {
        $.htmlEngine.plugins.register("vulnHistory", $.vulnHistory.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
