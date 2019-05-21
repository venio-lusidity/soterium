;(function ($) {
    //Object Instance
    $.pVulnerabilities = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);
        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.pVulnerabilities.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.KEY_ID = '/vertex/uri';
        // Store a reference to the environment object
        el.data('pVulnerabilities', state);
        var musts = [];
        if(state.worker.data && !state.opts.data){
            state.opts.data = {totals: true, data: state.worker.data, '/vertex/uri': state.worker.data[state.KEY_ID]};
        }

        if(state.opts.data[state.KEY_ID]){
            musts.push({fKey: "otherId", fValKey: 'value', value: state.opts.data[state.KEY_ID]});
        }
        var publishedKey;
        var opts = {
            singleSort: true,
            hovered: true,
            keyId: 'lid',
            showBusy: false,
            musts: musts,
            colResizable: false,
            filter: {
                enabled: true,
                store: 'technology_security_vulnerabilities_asset_vuln_detail',
                partition: 'technology_security_vulnerabilities_asset_vuln_detail',
                properties: [{key: 'severity', role: 'suggest'}, {key: 'vulnId', role: 'suggest', onTerm: function(item){
                    if(item.value){
                        item.label = $.jCommon.string.replaceAll(item.value, '_rule', '');
                    }
                }}]
            },
            search: {
                enabled: false
            },
            onFiltered: function (key, value) {

            },
            onBefore: function (items) {
                $.each(items, function () {
                    var v = this.severity;
                    if($.jCommon.string.contains(v, 'cat', true)){
                        switch(v.toLowerCase()){
                            case 'high':
                            case 'cat i':
                            case 'cat_i':
                                v = 'CAT I';
                                break;
                            case 'cat ii':
                            case 'cat_ii':
                            case 'medium':
                                v = 'CAT II';
                                break;
                            case 'cat iii':
                            case 'cat_iii':
                            case 'low':
                                v = 'CAT III';
                                break;
                        }
                    }
                    else{
                        v = $.jCommon.string.toTitleCase(v);
                    }
                    this.severity = v;
                    if(this.vulnId){
                        v = $.jCommon.string.replaceAll(this.vulnId, "_rule", '');
                        this.vulnId = v.toUpperCase();
                    }
                });
                return items;
            },
            getQuery: function(){
                var q = {
                    domain: 'technology_security_vulnerabilities_asset_vuln_detail',
                    type:'technology_security_vulnerabilities_asset_vuln_detail',
                    sort: [{property: 'ordinal', asc: 'true'},{property: 'published', asc: false}, {property: 'vuln', asc: true}],
                    "native": {
                        query : {
                            bool: {
                                must: []
                            }
                        }
                    }
                };
                var must = q["native"].query.bool.must;
                var data = state.opts.data;
                if(data && data.data && ($.jCommon.string.endsWith(data.data.vertexType, "/asset") || $.jCommon.string.endsWith(data.data.vertexType, "/acas_invalid_asset"))){
                    var frmt = String.format('"{0}.raw":"{1}"', "otherId", data[state.KEY_ID]);
                    var match = '{"wildcard":{'+frmt+'}}';
                    must.push(JSON.parse(match));
                }
                else if(!data.exact) {
                    var frmt = String.format('"{0}.raw":"{1}*"', data.prefixKey, data.prefixTree);
                    var match = '{"wildcard":{'+frmt+'}}';
                    must.push(JSON.parse(match));
                }
                else{
                    var frmt = String.format('"{0}.raw":"{1}"', data.prefixKey, data.prefixTree);
                    var match = '{"match":{'+frmt+'}}';
                    must.push(JSON.parse(match));
                }
                var frmt = String.format('"{0}.raw":"{1}"', "otherId", data[state.KEY_ID]);
                var match = '{"match":{'+frmt+'}}';
                must.push(JSON.parse(match));

                if(data.filters && data.filters.length>0){
                    $.each(data.filters, function () {
                        var frmt = String.format('"{0}.folded":"{1}"', this.fKey, this.value.toLowerCase());
                        var match = '{"match":{'+frmt+'}}';
                        must.push(JSON.parse(match));
                    });
                }
                return q;
            },
            distinct: {
                data: state.opts.data
            },
            mapping: [
                {
                    header: {title: '#', css: {minWidth: '50px', width: '50px', maxWidth: '50px'}},
                    property: '#',
                    type: 'integer'
                },
                {
                    header: {title: 'Severity', property: 'severity', css: {minWidth: '100px', width: '100px', maxWidth: '100px'}},
                    property: 'severity', type: 'string'
                },
                {
                    header: {title: 'Vuln Id', property: 'vulnId', css: {minWidth: '75px', width: '75px', maxWidth: '75px'}},
                    property: 'vulnId', type: 'string', callback: function(td, item, value){
                    if(value){
                        value = $.jCommon.string.replaceAll(value, '_rule', '');
                        td.append(value);
                    }
                }
                },
                {
                    header: {title: 'Title', property: 'title'},
                    property: 'vuln', type: 'string', callback: function (td, item, value) {
                    var d = dCrt('div').addClass('ellipse-it');
                    var a = dLink(value, item.relatedId, value).addClass('ellipse-it');
                    td.append(d.append(a));
                }
                },
                {
                    header: {title: 'Type', property: 'vertexType',  css: {minWidth: '150px', width: '150px', maxWidth: '150px'}},
                    property: 'vertexType', type: 'string', callback: function (td, item, value) {
                    td.append(FnFactory.classTypeToName(value));
                }
                },
                {
                    header: {title: "Published", property: 'published', sortable: true, onBeforeSort: function (map) {
                        map.property = (publishedKey ? publishedKey : map.property)+":"+"datetime";
                    },
                        css: {minWidth: '120px', width: '120px', maxWidth: '120px'}},
                    property: 'published', type: 'datetime', callback: function (td, item, value, map, filters) {
                    var v = item.publishedOn ? item.publishedOn : item.published;
                    if(item.publishedOn){
                        publishedKey = 'publishedOn'
                    }
                    if(item.published){
                        publishedKey = 'published';
                    }
                    if (v) {
                        var df = $.jCommon.dateTime.defaultFormat(v);
                        if (df) {
                            df = df.split(",");
                            td.append(String.format('{0}, {1}', df[0], df[1])).attr('title', df);
                        }
                    }
                }
                }
            ]
        };

        // Private environment methods
        methods = {
            init: function () {
                var m = $.extend({}, state.opts, opts, true);
                state.pGridViewer(m);
            }
        };
        methods.init();
        state.resize = function (opts) {
            state.pGridViewer('resize', opts);
        };
        state.reset = function (opts) {
            state.pGridViewer('reset', opts);
        };
    };

    //Default Settings
    $.pVulnerabilities.defaults = {
    };


    //Plugin Function
    $.fn.pVulnerabilities = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === 'object') {
            return new $.pVulnerabilities($(this),method);
        } else {
            // Helper strings to quickly perform functions
            var $pVulnerabilities = $(this).data('pVulnerabilities');
            switch (method) {
                case 'exists': return (null!==$pVulnerabilities && undefined!==$pVulnerabilities && $pVulnerabilities.length>0);
                case 'reset': return false;/*$pVulnerabilities.reset(options);*/break;
                case 'resize': $pVulnerabilities.resize(options);break;
                case 'state':
                default: return $pVulnerabilities;
            }
        }
    };

    $.pVulnerabilities.call= function(elem, options){
        elem.pVulnerabilities(options);
    };

    try {
        $.htmlEngine.plugins.register('pVulnerabilities', $.pVulnerabilities.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);