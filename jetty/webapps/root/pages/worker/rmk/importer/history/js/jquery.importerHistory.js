;(function ($) {
    //Object Instance
    $.importerHistory = function (el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.importerHistory.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        var results = [];
        var sources = [
            {key: "hbss", source: "399 EPO WHCA"},
            {key: "hbss", source: "399A EPO WHCA"},
            {key: "hbss", source: "399S EPO WHCA"},
            {key: "hbss", source: "DISA PROD CSD"},
            {key: "hbss", source: "DISA OOB"},
            {key: "hbss", source: "PKI-EPO-1 CSD"},
            {key: "hbss", source: "PKI-EPO-1 C3 PKI"},
            {key: "hbss", source: "PKI-EPO-2 CSD"},
            {key: "hbss", source: "PKI-EPO-2 C3 PKI"},
            {key: "hbss", source: "JITC EPO"},
            {key: "hbss", source: "EPAUS EPO"},
            {key: "sc", source: "SC Conus"},
            {key: "sc", source: "SC Conus 1"},
            {key: "sc", source: "SC Conus 2"},
            {key: "sc", source: "SC Conus 3"},
            {key: "sc", source: "SC Conus 4"},
            {key: "sc", source: "SC Conus 5"},
            {key: "sc", source: "SC EUR"},
            {key: "sc", source: "SC PAC"},
            {key: "sc", source: "SC JITC"}
        ];

        var classes = {
            hbss: [
                "com.lusidity.rmk.importer.hbss.HbssAssetsImporter",
                "com.lusidity.rmk.importer.hbss.HbssDeletedImporter",
                "com.lusidity.rmk.importer.hbss.HbssOpsImporter",
                "com.lusidity.rmk.importer.hbss.HbssStigImporter",
                "com.lusidity.rmk.importer.hbss.HbssSoftwareImporter"
            ],
            sc: [
                "com.lusidity.rmk.importer.acas.AcasApiImporter"
            ],
            sccm: [
                "com.lusidity.rmk.importer.msc.SccmImporter"
            ]
        };

        // Store a reference to the environment object
        el.data("importerHistory", state);

        // Private environment methods
        methods = {
            init: function () {
                var map = methods.map();
                var items = [];

                var l = map.length;
                var on = 0;

                function check(data) {
                    if (data) {
                        items.push(data);
                    }
                    on++;
                    if (on === l) {
                        methods.content(map, {results: items});
                    }
                }

                $.each(map, function () {
                    methods.get(0, 1, items, this, check);
                });
            },
            get: function (start, limit, items, item, cback) {
                state.next = 0;
                state.hits = 0;
                state.items = [];
                var s = function (d) {
                    var i;
                    if (d && d.results && d.results.length > 0) {
                        i = d.results[0];
                    }
                    cback(i);
                };
                var must = [
                    {match: {'deprecated': false}},
                    {match: {'root': false}},
                    {match: {'importer.folded': item.importer}},
                    {match: {'source.folded': item.source}}
                ];

                var q = {
                    domain: '/data/importer/importer_history',
                    type: '/data/importer/importer_history',
                    lid: null,
                    sort: {on: "createdWhen", direction: "desc"},
                    "native": {
                        query: {
                            filtered: {
                                filter: {
                                    bool: {
                                        must: must
                                    }
                                }
                            }
                        }
                    }
                };
                $.htmlEngine.request('/query?start=' + start + '&limit=' + limit, s, s, q, 'post', true);
            },
            contains: function (a1, a2) {
                var r = false;
                $.each(results, function () {
                    var x = this.source;
                    var im = this.importer;
                    r = ($.jCommon.string.equals(x, a1, true) && $.jCommon.string.equals(a2, im));
                    if (r) {
                        return false;
                    }
                });
                return r;
            },
            map: function (data) {
                var items = [];
                $.each(sources, function () {
                    var src = this;
                    var importers = classes[this.key];
                    $.each(importers, function () {
                        var item = {
                            source: src.source,
                            importer: this.toString(),
                            label: ((src.label) ? src.label : src.source)
                        };
                        items.push(item);
                    });
                });

                return $.jCommon.array.sort(items, [{property: 'importer', asc: true}]);
            },
            content: function (map, data) {
                $.each(map, function () {
                    var a1 = this.source;
                    var a2 = this.importer;
                    var lbl = this.label;
                    var f = false;
                    $.each(data.results, function () {
                        var b = this;
                        var b1 = b.source;
                        var b2 = b.importer;
                        var c = b.scanTypes;
                        var s = methods.scans(c);
                        var fs = methods.getFileSize(b.fileSize);
                        if ($.jCommon.string.equals(a1, b1, true) && $.jCommon.string.equals(a2, b2, true)) {
                            var u = {
                                label: lbl,
                                source: b1,
                                fileSize: fs,
                                scanTypes: s,
                                originFileNames: b.originFileNames,
                                importer: $.jCommon.string.getLast(b2, "."),
                                status: b.status,
                                createdWhen: b.createdWhen,
                                '/vertex/uri': b[state.KEY_ID]
                            };
                            u.combine = 'a' + u.source + u.importer;
                            results.push(u);
                            f = true;
                            return false;
                        }
                    });
                    if (!f) {
                        var u = {
                            label: lbl,
                            source: a1,
                            originFileNames: [],
                            importer: $.jCommon.string.getLast(a2, "."),
                            status: 'unknown',
                            createdWhen: 'unknown',
                            '/vertex/uri': $.jCommon.getRandomId("un")
                        };
                        u.combine = u.source + u.importer;
                        results.push(u);
                    }
                });
                methods.html.init();
            },
            scans: function (data) {
                var result;
                if (data.length > 0) {
                    var s = "";
                    $.each(data, function () {
                        s += $.jCommon.string.toTitleCase(this) + ",";
                    });
                    result = s.substr(0, s.length - 1);
                }
                return result;
            },
            getFileSize: function (data) {
                var x = parseInt(data);
                var round = Math.round;
                return round(x / 1024);
            },
            html: {
                init: function () {
                    results = $.jCommon.array.sort(results, [{property: 'source', asc: true}, {
                        property: 'importer',
                        asc: true
                    }]);
                    var v = {
                        title: "Latest Import History",
                        results: results,
                        hits: results.length,
                        next: results.length
                    };
                    var opts = {
                        groupsEnabled: true,
                        filter: true,
                        limit: results.length,
                        view: "details",
                        data: v,
                        realTime: true,
                        getUrl: function (data, start, limit) {
                            return null;
                        },
                        lists: {
                            groups: [
                                {label: 'Source', property: 'source'},
                                {label: 'Importer', property: 'importer', callback: function (value) {
                                    return FnFactory.toTitleCase($.jCommon.string.getLast(value, "."));
                                }}
                            ],
                            filters: []
                        },
                        actions: [],
                        details: {
                            data: v,
                            mapping: [
                                {
                                    header: {title: "#", css: {minWidth: '50px', width: '50px', maxWidth: '50px'}},
                                    property: '#',
                                    type: 'integer',
                                    css: {minWidth: '50px', width: '50px', maxWidth: '50px'}
                                },
                                {
                                    header: {title: "Source"},
                                    property: 'label',
                                    type: 'string',
                                    callback: function (td, item, value) {
                                        try {
                                            td.append(value);
                                        } catch (e) {
                                        }
                                    }
                                },
                                {
                                    header: {title: "Report Types"},
                                    property: 'scanTypes',
                                    type: 'string',
                                    callback: function (td, item, value) {
                                        try {
                                            td.append(value);
                                        } catch (e) {
                                        }
                                    }
                                },
                                {
                                    header: {title: "Importer"},
                                    property: 'importer',
                                    type: 'string',
                                    callback: function (td, item, value) {
                                        try {
                                            td.append(FnFactory.toTitleCase($.jCommon.string.getLast(value, ".")));
                                        } catch (e) {
                                        }
                                    }
                                },
                                {
                                    header: {title: "Imported On"},
                                    property: 'createdWhen',
                                    type: 'string',
                                    callback: function (td, item, value) {
                                        try {
                                            if (!$.jCommon.string.equals(value, "unknown")) {
                                                value = $.jCommon.dateTime.defaultFormat(value);
                                            }
                                            td.append(value);
                                        } catch (e) {
                                        }
                                    }
                                }
                            ]
                        }
                    };
                    state.opts.pnlMiddleNode.children().remove();
                    state.opts.pnlMiddleNode.jFilterBarPanel(opts);
                    $(state.find("table")).addClass('table-striped');
                    $('.page-content').css({overflow: 'hidden'});
                }
            }
        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.importerHistory.defaults = {};


    //Plugin Function
    $.fn.importerHistory = function (method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function () {
                new $.importerHistory($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $importerHistory = $(this).data('importerHistory');
            switch (method) {
                case 'exists':
                    return (null != $importerHistory && undefined != $importerHistory && $importerHistory.length > 0);
                case 'state':
                default:
                    return $importerHistory;
            }
        }
    };

})(jQuery);
