;(function ($) {

    //Object Instance
    $.pIavmAssets = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.pIavmAssets.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.isSys = $.jCommon.string.equals(state.worker.data.prefixKey, 'prefixDitpr');
        state.isLoc = $.jCommon.string.equals(state.worker.data.prefixKey, 'prefixLocation');
        state.isOrg = !(state.isSys && state.isLoc);
        state.linkStatus = {};
        var musts = [];
        if(state.opts.data.vulnId){
            musts.push({key: "vulnId", fKey: "vulnId", fValKey: "value", value: state.opts.data.vulnId});
        }
        else {
            musts.push({key: "relatedId", fKey: "relatedId", fValKey: "value", value: state.opts.data[state.KEY_ID]});
        }

        var opts = {
            minHeight: state.opts.minHeight,
            maxHeight: state.opts.maxHeight,
            height: state.opts.height,
            offset: state.opts.offset,
            sub: state.opts.sub,
            grouped: state.opts.grouped,
            hovered: true,
            keyId: 'lid',
            limit: state.opts.limit,
            musts: musts,
            showBusy: false,
            colResizable: false,
            singleSort: true,
            filter: {
                enabled: true,
                store: 'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
                partition: 'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
                properties: [{key: 'other', role: 'suggest'}, {key:'ditpr', role: 'suggest'}, {key: 'compliant', replaceSpace: "_", role: 'suggest', onResults: function(node, item, data){
                    data.results = $.jCommon.array.sort(data.results, [{property: 'compliant', asc: true}]);
                }, onTerm: function(item){
                    item.compliant = item.value;
                    var ct = $.htmlEngine.compliant(item);
                    item.label = ct.label;
                }}, {key:'ditprAltId', role: 'filter', type: 'number'},  {key:'location', role: 'suggest'}, {key:'managed', role: 'suggest'}, {key:'owned', role: 'suggest'}]
            },
            search: {
                enabled: false
            },
            onFiltered: function (key, value) {

            },
            onBefore: function (items, node) {
                var r = items;
                $.each(r, function () {
                    var ct = $.htmlEngine.compliant(this);
                    this.compliant = ct.label;
                });
                return r;
            },
            getQuery: function(){
                var data = state.opts.data;
                var q = {
                    asFilterable: true,
                    domain: 'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
                    type: 'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
                    "native": {
                        query : {
                            bool: {
                                must: [
                                    {"match":{"result.raw": "failed"}}
                                ]
                            }
                        }
                    },
                    sort: [
                        {property: 'packedVulnerabilityMatrix', asc: false},
                        {property: 'other.folded', asc: true}
                    ]
                };
                var must = q["native"].query.bool.must;
                if(!data.et_exact) {
                    var frmt = String.format('"{0}.raw":"{1}*"', data.prefixKey, data.prefixTree);
                    var match = '{"wildcard":{'+frmt+'}}';
                    must.push(JSON.parse(match));
                }
                else{
                    var frmt = String.format('"{0}.raw":"{1}"', data.prefixKey, data.prefixTree);
                    var match = '{"match":{'+frmt+'}}';
                    must.push(JSON.parse(match));
                }
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
                {header: {title: "#", css: {minWidth: '50px', width: '50px', maxWidth: '50px'}}, property: '#', type: 'integer', css: {minWidth: '50px', width: '50px', maxWidth: '50px'}},
                {header: {title: "RS", tip: "Risk Score", css: {minWidth: '30px', width: '30px'}}, property: '', type: 'integer', callback: function (td, item, value, map, filters) {
                    if($.jCommon.json.hasProperty(item, 'metrics.html.cls')) {
                        td.addClass(item.metrics.html.cls).attr('title', item.metrics.html.label + ': ' + item.packedVulnerabilityMatrix);
                    }
                }},
                {header: { title: "Asset", property: 'other', sortable: true, sortType: 'string', sortTipMap: {asc: 'asc', desc: 'desc', 'none': 'Risk Score'}, css: {minWidth: '132px'}}, property: 'other', type: 'string', callback: function (td, item, value) {
                    var a = dCrt('a').attr('href', item.relatedUri ? item.relatedUri : item.otherId).attr('target', "_blank").html(value);
                    td.append(a);
                }},
                {header: { title: 'HBC', tip: 'HBSS Baseline Compliance', property: 'compliant', css: {minWidth: '50px', width: '50px', maxWidth: '50px'}},
                    css: {minWidth: '50px', width: '50px', maxWidth: '50px'},
                    property: 'compliant', type: 'string', callback: function (td, item, value, map, filters) {
                    var ct = $.htmlEngine.compliant(item);
                    td.attr('title', ct.tip).addClass(ct.clr);
                    td.osIcon({
                        display: "tile",
                        fontSize: '16px',
                        hasTitle: false,
                        hasVersion: false,
                        linked: true,
                        data: {'/properties/technology/software/operatingSystem': {title: item.os, '/vertex/uri': item.osId}}
                    });
                }},
                {header: { title: "System Name", property: 'ditpr'}, property: 'ditpr', type: 'string', callback: function (td, item, value) {
                    var d = dCrt('div').addClass('ellipse-it');
                    var v = state.linkStatus[item.ditprId];
                    if(!v){
                        state.linkStatus[item.ditprId] = "none";
                    }
                    d.pAuthorizedLink({lCache: state.linkStatus, view:"ditpr",vertexUri:item.ditprId,linkAttributes:[{id:"title",value:value},
                        {id:"href", value:item.ditprId+ "?et_view=ditpr"},{id:"target", value: "_blank"}], linkHtml:value, schema:{}});
                    td.append(d);
                }},
                {header: { title: "Location", property: 'location'}, property: 'location', type: 'string', callback: function (td, item, value) {
                    if(item.location) {
                        var d = dCrt('div').addClass('ellipse-it');
                        var v = state.linkStatus[item.locationId];
                        if(!v){
                            state.linkStatus[item.locationId] = "none";
                        }
                        d.pAuthorizedLink({lCache: state.linkStatus, view:"loc",vertexUri:item.locationId,linkAttributes:[{id:"title",value:value},
                            {id:"href", value:item.locationId + "?et_view=loc"},{id:"target", value: "_blank"}], linkHtml:item.location, schema:{}});
                        td.append(d);
                    }
                }},
                {header: { title: "Managed By", property: 'managed'}, property: 'managed', type: 'string', callback: function (td, item, value) {
                    if(item.managed) {
                        var d = dCrt('div').addClass('ellipse-it');
                        var v = state.linkStatus[item.managedId];
                        if(!v){
                            state.linkStatus[item.managedId] = "none";
                        }
                        d.pAuthorizedLink({lCache: state.linkStatus, view:"managed",vertexUri:item.managedId,linkAttributes:[{id:"title",value:value},
                            {id:"href", value:item.managedId + "?et_view=managed"},{id:"target", value: "_blank"}], linkHtml:item.managed, schema:{}});
                        td.append(d);
                    }
                }},
                {header: { title: "Owned By", property: 'owned'}, property: 'owned', type: 'string', callback: function (td, item, value) {
                    if(item.owned) {
                        var d = dCrt('div').addClass('ellipse-it');
                        var v = state.linkStatus[item.ownedId];
                        if(!v){
                            state.linkStatus[item.ownedId] = "none";
                        }
                        d.pAuthorizedLink({lCache: state.linkStatus, view:"owned",vertexUri:item.ownedId,linkAttributes:[{id:"title",value:value},
                            {id:"href", value:item.ownedId + "?et_view=owned"},{id:"target", value: "_blank"}], linkHtml:item.owned, schema:{}});
                        td.append(d);
                    }
                }}
            ]
        };

        // Store a reference to the environment object
        el.data("pIavmAssets", state);

        // Private environment methods
        methods = {
            init: function() {
                if(state.isOrg || state.isLoc){
                    var item ={
                        header: { title: "DITPR ID", property: 'ditprAltId', css: {width: '80px'}}, property: 'ditprAltId', type: 'string', callback: function (td, item, value) {
                            if(item.ditprAltId) {
                                item.ditprAltId = item.ditprAltId.toString();
                                var v = state.linkStatus[item.ditprId];
                                if(!v){
                                    state.linkStatus[item.ditprId] = "none";
                                }
                                td.pAuthorizedLink({lCache: state.linkStatus, view:"ditpr",vertexUri:item.ditprId,linkAttributes:[{id:"title",value:value},
                                    {id:"href", value:item.ditprId},{id:"target", value: "_blank"}], linkHtml:value, schema:{}});
                            }
                        }};
                    var map = opts.mapping;
                    opts.mapping = $.jCommon.array.insertAt(map, item, 4);
                }

                state.worker.node.attr('data-valid', true).css({overflow: 'hidden'}).show();
                state.body = state.opts.title ? $.htmlEngine.panel(state.worker.node, state.opts.glyph, state.opts.title, null, false, null, null) : dCrt('div');
                if (!state.opts.title) {
                    state.worker.node.append(state.body);
                }
                if(state.opts.hInherit) {
                    state.body.css({height: 'inherit'});
                }
                else{
                    var h = state.body.availHeight();
                    var ph = state.worker.node.height();
                    h = ph > h ? h : ph;
                    if (state.opts.maxHeight > 0) {
                        h = state.opts.maxHeight;
                    }
                    dHeight(state.body, 0, 0, h);
                    opts.height = h;
                }

                opts.parentGrid = state.opts.parentGrid;
                state.body.css({overflow: 'hidden', fontSize: '12px', position: 'relative'});
                state.body.on('table-view-loaded', function (e) {
                    state.trigger(e);
                });
                state.body.addClass('sub-table');
                state.grid =state.body.pGrid(opts);
            }
        };
        methods.init();
        state.resize = function (opt) {
            state.grid.reset();
        };
        state.reset = function (opt) {
            state.grid.reset();
        };
        return state;
    };

    //Default Settings
    $.pIavmAssets.defaults = {
        limit: 90,
        maxHeight: 0,
        minHeight: 0,
        height: 0
    };


    //Plugin Function
    $.fn.pIavmAssets = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return new $.pIavmAssets($(this),method);
        } else {
            // Helper strings to quickly perform functions
            var $pIavmAssets = $(this).data('pIavmAssets');
            switch (method) {
                case 'exists': return (null!==$pIavmAssets && undefined!==$pIavmAssets && $pIavmAssets.length>0);
                case 'reset': $pIavmAssets.reset(options);break;
                case 'resize': $pIavmAssets.resize(options);break;
                case 'state':
                default: return $pIavmAssets;
            }
        }
    };

    $.pIavmAssets.call= function(elem, options){
        elem.pIavmAssets(options);
    };

    try {
        $.htmlEngine.plugins.register("pIavmAssets", $.pIavmAssets.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);