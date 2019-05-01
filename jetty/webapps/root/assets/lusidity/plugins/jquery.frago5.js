

;(function ($) {

    //Object Instance
    $.frago5 = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.frago5.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.KEY_COMPLIANT = '/technology/software_compliance_policy/compliance';

        // Store a reference to the environment object
        el.data("frago5", state);

        // Private environment methods
        methods = {
            init: function() {
                state.worker.node.attr('data-valid', false).hide();
                var s = function(data){
                    if(data && data.results) {
                        state.next += data.results.length;
                        state.hits = data.hits;
                        methods.html.init(data);
                        state.worker.node.attr('data-valid', true).show();
                    }
                    $('.page-content').scrollHandler('start');
                };

                var f = function(){
                    $('.page-content').scrollHandler('start');
                };
                
                function page() {
                    if(!state.next){
                        state.next = 0;
                    }
                    if(!state.hits || state.next<state.hits) {
                        if (state.opts.query && state.opts.query.fn) {
                            var fn = QueryFactory[state.opts.query.fn];
                            var q = fn(state.worker.data);
                            $.htmlEngine.request(methods.getQueryUrl(state.next, state.opts.limit), s, f, q, 'post');
                        }
                        else {
                            var url = state.worker.data[state.KEY_ID] + '/properties' + state.opts.property;
                            if (state.opts.limit) {
                                url += '?start=' + state.next + '&limit=' + state.opts.limit;
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
                }

                $('.page-content').scrollHandler({
                    adjust: 140,
                    start: function () {
                    },
                    stop: function () {
                    },
                    top: function () {
                    },
                    bottom: function () {
                        if (state.next < state.hits) {
                            page();
                        }
                    }
                });
                page();
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
                    results = $.jCommon.array.sort(results, [{property: "title", asc: true}]);
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
                            var c = dCrt('div');
                            state.body.append(c);
                            c.dataList({data:item, node:c, schema:{ name:"Policy Required Software", plugin:{
                                "glyph": "glyphicons glyphicons-info-sign",
                                "title": "Required Software",
                                "property": "/technology/software/complianceSoftware",
                                "properties":[
                                    {
                                        "property": "title",
                                        "linked": true
                                    },
                                    {
                                        "property": "/technology/software_version/version.version",
                                        "maxItems": 1
                                    }
                                ]
                            }}});
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
    $.frago5.defaults = {
        limit: 100,
        paging: true
    };


    //Plugin Function
    $.fn.frago5 = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.frago5($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var frago5 = $(this).data('frago5');
            switch (method) {
                case 'state':
                default: return frago5;
            }
        }
    };

    $.frago5.call= function(elem, options){
        elem.frago5(options);
    };

    try {
        $.htmlEngine.plugins.register("frago5", $.frago5.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
