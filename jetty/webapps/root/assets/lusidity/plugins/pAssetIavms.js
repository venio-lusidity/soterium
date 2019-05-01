;(function ($) {

    //Object Instance
    $.pAssetIavms = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.pAssetIavms.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.isSys = $.jCommon.string.equals(state.worker.data.prefixKey, 'prefixDitpr');
        state.isLoc = $.jCommon.string.equals(state.worker.data.prefixKey, 'prefixLocation');
        state.isOrg = !(state.isSys && state.isLoc);
        var musts = [];
        musts.push({key: "otherId", fKey: "otherId", fValKey: "value", value: state.opts.data[state.KEY_ID]});

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
            filter: {
                enabled: true,
                store: 'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
                partition: 'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
                properties: [{key: 'noticeId', role: 'suggest'}]
            },
            search: {
                enabled: false,
                properties:['noticeId']
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
                    domain: 'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
                    type: 'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
                    sort: [{property: 'noticeYear', asc: false},{property: 'noticeType', asc: true},{property: 'noticeNum', asc: false}],
                    "native": {
                        query : {
                            bool: {
                                must: [
                                    {"match":{"result.raw": "failed"}}
                                ]
                            }
                        }
                    }
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
                {header: { title: "IAVM Notice Id", property: 'noticeId', css: {minWidth: '132px'},callback: function (th, map) {
                    th.append(map.title);
                }}, property: 'noticeId', type: 'string', callback: function (td, item, value) {
                    var a = dCrt('a').attr('href', item.relatedId).attr('target', "_blank").html(value);
                    td.append(a);
                }},
                {header: { title: "Title", property: 'title'}, property: 'title', type: 'string', callback: function (td, item, value) {
                    var a = dCrt('a').attr('href', item.relatedId).attr('target', "_blank").html(value);
                    td.append(a);
                }},
                {header: { title: "POA&M Date", property: 'due', css: {minWidth: '132px'}}, property: 'due', type: 'date', callback: function (td, item, value) {
                    if(value) {
                        var df = $.jCommon.dateTime.dateOnly(value);
                        td.append(df);
                    }
                }}
            ]
        };
        // Store a reference to the environment object
        el.data("pAssetIavms", state);

        // Private environment methods
        methods = {
            init: function() {
                // $.htmlEngine.loadFiles(state.worker.propertyNode, state.opts.name, state.opts.cssFiles);
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
    $.pAssetIavms.defaults = {
        limit: 90,
        maxHeight: 0,
        minHeight: 0,
        height: 0
    };


    //Plugin Function
    $.fn.pAssetIavms = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return new $.pAssetIavms($(this),method);
        } else {
            // Helper strings to quickly perform functions
            var $pAssetIavms = $(this).data('pAssetIavms');
            switch (method) {
                case 'exists': return (null!==$pAssetIavms && undefined!==$pAssetIavms && $pAssetIavms.length>0);
                case 'reset': $pAssetIavms.reset(options);break;
                case 'resize': $pAssetIavms.resize(options);break;
                case 'state':
                default: return $pAssetIavms;
            }
        }
    };

    $.pAssetIavms.call= function(elem, options){
        elem.pAssetIavms(options);
    };

    try {
        $.htmlEngine.plugins.register("pAssetIavms", $.pAssetIavms.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);