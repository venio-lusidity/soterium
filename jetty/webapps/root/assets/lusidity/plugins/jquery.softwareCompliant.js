

;(function ($) {

    //Object Instance
    $.softwareCompliant = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.softwareCompliant.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.KEY_APPS = '/technology/software/applications';
        state.KEY_VER = '/technology/software_version/version.version';
        state.green = "#49b26f";
        state.red = "#d9534f";
        state.gray = "#a9a9a9";
        state.start = 0;
        state.isInvalid = $.jCommon.string.contains(state.worker.data.vertexType, 'acas_invalid_asset');

        // Store a reference to the environment object
        el.data("softwareCompliant", state);

        // Private environment methods
        methods = {
            init: function() {
                state.worker.node.attr('data-valid', false).hide();
                var s = function(data){
                    if(data && data.results) {
                        state.start += data.results.length;
                        state.hits = data.hits;
                    }
                    methods.html.init(data);
                    state.worker.node.attr('data-valid', true).show();
                    $('.page-content').scrollHandler('start');
                };

                var f = function(){
                    $('.page-content').scrollHandler('start');
                };

                function page() {
                    if(!state.hits || state.start<state.hits) {
                        if (state.opts.query && state.opts.query.fn) {
                            var fn = QueryFactory[state.opts.query.fn];
                            var q = fn(state.worker.data);
                            $.htmlEngine.request(methods.getQueryUrl(state.start, state.opts.limit), s, f, q, 'post');
                        }
                        else {
                            var url = state.worker.data[state.KEY_ID] + '/properties' + state.opts.property;
                            if (state.opts.limit) {
                                url += '?start=' + state.start + '&limit=' + state.opts.limit;
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
            resize: function () {
                var h = state.body.availHeight();
                state.body.css({height: h + 'px', maxHeight: h+'px', overflowY: 'auto', overflowX: 'hidden'});
            },
            html:{
                init: function(data){
                    state.body=$.htmlEngine.panel(
                        state.worker.node,state.opts.glyph, state.opts.title, null, false /* borders */
                    );
                    var c = dCrt('div');
                    state.body.append(c);
                    state.compliant = dCrt('div');
                    state.body.append(state.compliant);
                    if(!data || (data.results && data.results.length<=0)){
                        methods.html.policies(c);
                        return false;
                    }
                    var os = data.results[0];
                    var s = function (d) {
                        methods.html.policies(c, d, os);
                    };
                    var u = os[state.KEY_ID] + '/properties/technology/software/complianceSoftware';
                    $.htmlEngine.request(u, s, s, null, 'get');
                },
                policies: function(node, data, os){
                    if(!data || !os){
                        var st = $.htmlEngine.compliant(state.worker.data);
                        state.worker.node.panel('updateHeader',
                            { glyph: String.format('{0} {1}', st.glyph, st.clr), tip: st.tip, title: st.tip, url: null }
                        );
                        return false;
                    }
                    var results = data.results;
                    if(!(results && results.length>0)){
                        return false;
                    }
                    results = $.jCommon.array.sort(results, [{property: "title", asc: true}]);
                    function splitter(node){
                        if (node.children().length>0) {
                            var splitter = $(document.createElement("span")).html("|").css({margin: '0 5px 0 5px'});
                            node.append(splitter);
                        }
                    }
                    var cache = {};
                    $.each(results, function(){
                        var item = this;
                        var ver = $.jCommon.json.getProperty(item, state.KEY_VER);
                        var k = $.jCommon.string.makeKey(item.title);
                        var c = cache[k];

                        if(c){
                            var f = ver;
                            try{
                                var a = parseInt(c.ver.replaceAll('.', ''));
                                var b = parseInt(ver.replaceAll('.', ''));
                                ver = (a>b) ? c.ver : ver;
                            }catch(e){}
                            c.v.html(ver);
                        }
                        else {
                            var n = dCrt('div').addClass('data-list');
                            node.append(n);
                            var t = dCrt('a').addClass('data-value').attr('href', item[state.KEY_ID]).attr('target', '_blank').html(item.title);
                            n.append(t);
                            splitter(n);
                            var l = dCrt('div').addClass('data-label').html('expected version:');
                            n.append(l);
                            var v = dCrt('div').addClass('data-value').html(ver);
                            n.append(v);
                            splitter(n);
                            l = dCrt('div').addClass('data-label').html('actual version:');
                            n.append(l);
                            var p = dCrt('div').addClass('data-value');
                            n.append(p);
                            cache[k] = {p: p, v: v, ver: ver};
                        }
                    });
                    var f = function () {

                    };
                    var s = function (data) {
                        if (data) {
                            methods.html.compliant(data, cache);
                        }
                    };
                    var u = state.worker.data[state.KEY_ID] + "/software/compliance";
                    $.htmlEngine.request(u, s, f, null, 'get');
                },
                hasAlias: function (actual, policy) {
                    var alias = [
                        {title: "mcafee virusscan enterprise"},
                        {title: "mcafee virusscan enterprise for linux", alias: "mcafee virusscan enterprise"},
                        {title: "mcafee agent"},
                        {title: "mcafee data loss prevention"},
                        {title: "mcafee dlp endpoint", alias: "mcafee data loss prevention"},
                        {title: "mcafee host intrusion prevention"},
                        {title: "mcafee policy auditor agent", alias: "mcafee policy auditor agent"},
                        {title: "mcafee usaf accm"},
                        {title: "mcafee accm", alias: "mcafee usaf accm"},
                        {title: "mcafee rogue system detection", "disabled": true},
                        {title: "mcafee rsd", alias: "mcafee rogue system detection", "disabled": true},
                        {title: "rsd", alias: "mcafee rogue system detection", "disabled": true},
                        {title: "mcafee application control","disabled": true}
                    ];
                    var r = false;
                    $.each(alias, function () {
                       var item = this;
                       var t = $.jCommon.string.makeKey(item.title);
                       var a = item.alias ? $.jCommon.string.makeKey(item.alias) : t;
                       r = ((actual===t && policy===a) || actual===a && policy===t);
                       if(r){
                           return false;
                       }
                    });
                    return r;
                },
                compliant: function (data, cache) {
                    var results = data[state.KEY_APPS];
                    if(!(results && results.length>0)){
                        return false;
                    }
                    var cpl = data.compliant;
                    var st = $.htmlEngine.compliant(data);
                    state.worker.node.panel('updateHeader',
                        { glyph: String.format('{0} {1}', st.glyph, st.clr), tip: st.tip, title: st.tip, url: null }
                    );
                    var matches = [];
                    $.each(cache, function (key, value) {
                        var policy=value;
                        $.each(results, function () {
                            var item = this;
                            var k = $.jCommon.string.makeKey(item.title);
                            if($.jCommon.string.equals(k, key, true) || methods.html.hasAlias(k, key)){
                                policy.item = item;
                                policy.disabled = $.jCommon.json.getProperty(item, 'disabled');
                                matches.push({software: item, softwareKey: k, policyKey: key, policy: policy});
                                return false;
                            }
                        });
                        if(policy.disabled){
                            policy.p.html("disabled").css({"color": state.gray});
                        }
                        else if(!policy.item){
                            policy.p.html("missing").css({"color": state.red});
                        }
                        else{
                            policy.p.html($.jCommon.json.getProperty(policy.item, state.KEY_VER));
                            cpl = $.jCommon.json.getProperty(policy.item, 'compliant');
                            var clr = $.jCommon.string.equals(cpl, 'yes', true) ? state.green : state.red;
                            policy.p.css({color: clr});
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
    $.softwareCompliant.defaults = {};


    //Plugin Function
    $.fn.softwareCompliant = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.softwareCompliant($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $softwareCompliant = $(this).data('softwareCompliant');
            switch (method) {
                case 'state':
                default: return $softwareCompliant;
            }
        }
    };

    $.softwareCompliant.call= function(elem, options){
        elem.softwareCompliant(options);
    };

    try {
        $.htmlEngine.plugins.register("softwareCompliant", $.softwareCompliant.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
