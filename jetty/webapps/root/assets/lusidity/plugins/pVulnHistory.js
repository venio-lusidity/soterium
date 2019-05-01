;(function ($) {
    //Object Instance
    $.pVulnHistory = function (el, options) {
        var state = el,
            methods = {};
        state.container = $(el);
        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data};
        state.opts = $.extend({}, $.pVulnHistory.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.KEY_ID = '/vertex/uri';

        // Store a reference to the environment object
        el.data('pVulnHistory', state);
        var musts = [];
        if(state.worker.data && !state.opts.data){
            state.opts.data = {totals: true, data: state.worker.data, '/vertex/uri': state.worker.data[state.KEY_ID]};
        }
        if(state.opts.data[state.KEY_ID]){
            musts.push({fKey: "relatedId", fValKey: 'value', value: state.worker.data.lid});
        }

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
                    var v = this.severity;
                    if($.jCommon.string.contains(v, 'cat', true)) {
                        v = methods.getSeverity(v)
                    }
                    else {
                        v = $.jCommon.string.toTitleCase(v);
                    }
                    this.severity = v;
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
                store: 'object_edge_vulnerability_edge',
                partition: 'electronic_network_asset',
                properties: [
                    {
                        key: 'severity', role: 'suggest', replaceSpace: "_",
                        onResults: function (node, item, data) {
                            data.results = $.jCommon.array.sort(data.results, [{property: 'severity', asc: true}]);
                        },
                        onTerm: function (item) {
                            item.label = methods.getSeverity(item.value);
                        }
                    },
                    {
                        key: 'vulnId', role: 'suggest',
                        onTerm: function (item) {
                            if(item.value) {
                                item.label = $.jCommon.string.replaceAll(item.value, '_rule', '');
                            }
                        }
                    },
                    {
                        key: 'resource', role: 'suggest', replaceSpace: "",
                        beforeSuggest: function (phrase, item) {
                            return phrase;
                        },
                        onResults: function (node, item, data, term) {
                            data.results = $.jCommon.array.sort(data.results, [{property: '_label', asc: true}]);
                        },
                        onTerm: function (item) {
                            item.label = FnFactory.toTitleCase(item.value);
                        }
                    },
                    {
                        key: 'result', role: 'suggest',
                        onResults: function (node, item, data) {
                            data.results = $.jCommon.array.sort(data.results, [{property: 'result', asc: true}]);
                        },
                        onTerm: function (item) {
                            if(item.value){
                                item.label = $.jCommon.string.replaceAll(item.value, "_", " ");
                            }
                        }
                    }]
            },
            getQuery: function () {
                return  {
                    domain: 'technology_security_vulnerabilities_vulnerability_entry',
                    type: 'technology_security_vulnerabilities_vulnerability_entry',
                    sort: [{property: 'vulnId.folded', asc: 'true'}, {property: 'scannedOn', asc: false}, {property: 'createdWhen', asc: false}],
                    "native": {
                        query: {
                            bool: {
                                must: []
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
                    header: {
                        title: 'Severity',
                        sortable: true,
                        property: 'severity',
                        sortProperty: 'ordinal',
                        css: {minWidth: '100px', width: '100px', maxWidth: '100px'}
                    },
                    property: 'severity'
                },
                {
                    header: {
                        title: 'Vuln Id',
                        property: 'vulnId',
                        css: {minWidth: '100px', width: '100px', maxWidth: '100px'}
                    },
                    property: 'vulnId', type: 'string', callback: function (td, item, value) {
                    if(value) {
                        value = $.jCommon.string.replaceAll(value, '_rule', '');
                        var url = item.otherId;
                        var a = dLink(value.toUpperCase(), url);
                        td.append(a);
                    }
                }
                },
                {
                    header: {
                        title: 'Title',
                        property: 'title'
                    },
                    property: 'title', type: 'string', callback: function (td, item, value) {
                    if(value) {
                        var d = dCrt('div').addClass('ellipse-it').attr('title', value);
                        var url = item.otherId;
                        var a = dLink(value, url);
                        td.append(d.append(a));
                    }
                }
                },
                {
                    header: {
                        title: 'Result',
                        property: 'result',
                        css: {minWidth: '50px', width: '150px'}
                    },
                    property: 'result', type: 'string', callback: function (td, item, value) {
                    var v = $.jCommon.string.replaceAll(value, "_", " ");
                    td.append(v);
                }
                },
                {
                    header: {title: 'Importer', property: 'resource', css: {minWidth: '10px', width: '150px'}},
                    property: 'resource', type: 'string', callback: function (td, item, value) {
                    var v = FnFactory.toTitleCase($.jCommon.string.getLast(value, '.'));
                    td.append(v);
                }
                },
                {
                    header: {
                        title: 'First Seen',
                        property: 'firstSeen',
                        css: {minWidth: '100px', width: '100px', maxWidth: '100px'}
                    },
                    property: 'firstSeen', type: 'datetime', callback: function (td, item, value) {
                    if(value) {
                        td.append($.jCommon.dateTime.dateOnly(value))
                    }
                }
                },
                {
                    header: {
                        title: 'Scanned On',
                        property: 'scannedOn',
                        css: {minWidth: '100px', width: '100px', maxWidth: '100px'}
                    },
                    property: 'scannedOn', type: 'datetime', callback: function (td, item, value) {
                    if(value) {
                        td.append($.jCommon.dateTime.dateOnly(value))
                    }
                }
                },
                {
                    header: {
                        title: 'In RMK As Of',
                        tip: 'Initial data started on March 2018',
                        property: 'createdWhen',
                        css: {minWidth: '100px', width: '100px', maxWidth: '100px'}
                    },
                    property: 'createdWhen', type: 'datetime', callback: function (td, item, value) {
                    if(value) {
                        td.append($.jCommon.dateTime.dateOnly(value))
                    }
                }
                }
            ]
        };

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
            },
            getSeverity: function (s) {
                var v = $.jCommon.string.toTitleCase(s);
                switch (s.toLowerCase()) {
                    case 'cat i':
                    case 'cat_i':
                        v = 'CAT I';
                        break;
                    case 'cat ii':
                    case 'cat_ii':
                        v = 'CAT II';
                        break;
                    case 'cat iii':
                    case 'cat_iii':
                        v = 'CAT III';
                        break;
                }
                return v;
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
    $.pVulnHistory.defaults = {};


    //Plugin Function
    $.fn.pVulnHistory = function (method, options) {
        if(method === undefined) method = {};

        if(typeof method === 'object') {
            return new $.pVulnHistory($(this), method);
        } else {
            // Helper strings to quickly perform functions
            var $pVulnHistory = $(this).data('pVulnHistory');
            switch (method) {
                case 'exists':
                    return (null !== $pVulnHistory && undefined !== $pVulnHistory && $pVulnHistory.length > 0);
                case 'reset':
                    $pVulnHistory.reset(options);
                    break;
                case 'resize':
                    $pVulnHistory.resize(options);
                    break;
                case 'state':
                default:
                    return $pVulnHistory;
            }
        }
    };

    $.pVulnHistory.call = function (elem, options) {
        elem.pVulnHistory(options);
    };

    try {
        $.htmlEngine.plugins.register('pVulnHistory', $.pVulnHistory.call);
    }
    catch (e) {
        console.log(e);
    }

})(jQuery);