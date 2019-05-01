;(function ($) {
    //Object Instance
    $.pSoftware = function (el, options) {
        var state = el,
            methods = {};
        state.container = $(el);
        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data};
        state.opts = $.extend({}, $.pSoftware.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.KEY_ID = '/vertex/uri';

        // Store a reference to the environment object
        el.data('pSoftware', state);
        var musts = [];
        if(state.worker.data && !state.opts.data){
            state.opts.data = {totals: true, data: state.worker.data, '/vertex/uri': state.worker.data[state.KEY_ID]};
        }
        if(state.opts.data[state.KEY_ID]){
            musts.push({fKey: "/object/endpoint/endpointFrom.relatedId", fValKey: 'value', value: state.worker.data.lid});
            musts.push({fKey: "label", fValKey: 'value', value: '/technology/software/applications'});
        }

        var sd = true;
        var opts = {
            hovered: true,
            keyId: 'lid',
            limit: state.opts.limit,
            showBusy: false,
            musts: musts,
            colResizable: false,
            search: {
                enabled: false
            },
            onFiltered: function (key, value) {
            },
            onBefore: function (items) {
                $.each(items, function () {
                   if(!this.source){
                       this.source = "com.lusidity.rmk.importer.hbss.HbssSoftwareImporter";
                   }
                   this.source = FnFactory.toTitleCase($.jCommon.string.getLast(this.source, "."));
                   if(this._edge){
                       this.sourceDate = this._edge.createdWhen;
                   }
                });
                return items;
            },
            distinct: {
                data: state.opts.data
            },
            filter: {
                enabled: true,
                nullable: true,
                nullValue: "",
                store: 'object_edge_software_edge',
                partition: 'electronic_network_asset',
                properties: [
                    {
                        key: '/object/endpoint/endpointTo.label', returnKey: 'title', role: 'suggest',
                        onResults: function (node, item, data) {
                            data.results = $.jCommon.array.sort(data.results, [{property: 'title', asc: true}]);
                        },
                        onTerm: function (item) {
                            item.label = $.jCommon.string.replaceAll(item.value, '_rule', '');
                        }
                    }
                ]
            },
            getQuery: function () {
                return {
                    domain: 'object_edge_software_edge',
                    type: 'electronic_network_asset',
                    lid: state.worker.data.lid,
                    asFilterable: false,
                    include_edge: true,
                    sort: [{property: '/object/endpoint/endpointTo.label.folded', asc: true}],
                    "native": {
                        query: {
                            bool: {
                                must: [
                                    {match: {'deprecated': false}}
                                ]
                            }
                        }
                    }
                };
            },
            mapping: [
                {
                    header: {title: '#', css: {minWidth: '50px', width: '50px', maxWidth: '50px'}},
                    property: '#',
                    type: 'integer'
                },
                {
                    header: {title: 'Application', property: '/object/endpoint/endpointTo.label'},
                    property: '/object/endpoint/endpointTo.label', type: 'string', callback: function (td, item, value) {
                    var lnk = dLink(item.title, item[state.KEY_ID]);
                    td.append(lnk);
                }
                },
                {
                    header: {title: 'Version', property: '/technology/software_version', css: {width: '150px'}},
                    property: '/technology/software_version/version', type: 'string', callback: function (td, item, value) {
                        if(value && value.length>0){
                            td.append(value[0].version);
                        }
                }
                },
                {
                    header: {title: 'Source', property: 'source', css: {width: '150px'}},
                    property: 'source', type: 'string'
                }
            ]
        };

        if(sd){
            opts.mapping.push(
                {
                    header: {title: 'Source Date', property: 'sourceDate', css: {width: '150px'}},
                    property: 'sourceDate', type: 'date', callback: function (td, item, value) {
                    if(value){
                        td.append( $.jCommon.dateTime.defaultFormat(value));
                    }
                }
                });
        }

        // Private environment methods
        methods = {
            init: function () {
                var m = $.extend({}, state.opts, opts, true);
                if(state.opts.hasTitle){
                    $.jCommon.array.insertAt(m.mapping,
                        {
                            header: {
                                title: 'Title',
                                property: 'title'
                            },
                            property: 'title', type: 'string'
                        }
                        ,3);
                }
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
    $.pSoftware.defaults = {};


    //Plugin Function
    $.fn.pSoftware = function (method, options) {
        if (method === undefined) method = {};

        if (typeof method === 'object') {
            return new $.pSoftware($(this), method);
        } else {
            // Helper strings to quickly perform functions
            var $pSoftware = $(this).data('pSoftware');
            switch (method) {
                case 'exists':
                    return (null !== $pSoftware && undefined !== $pSoftware && $pSoftware.length > 0);
                case 'reset':
                    $pSoftware.reset(options);
                    break;
                case 'resize':
                    $pSoftware.resize(options);
                    break;
                case 'state':
                default:
                    return $pSoftware;
            }
        }
    };

    $.pSoftware.call = function (elem, options) {
        elem.pSoftware(options);
    };

    try {
        $.htmlEngine.plugins.register('pSoftware', $.pSoftware.call);
    }
    catch (e) {
        console.log(e);
    }

})(jQuery);