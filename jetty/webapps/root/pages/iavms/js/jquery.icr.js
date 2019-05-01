

;(function ($) {

    //Object Instance
    $.icr = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.icr.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.node = dCrt('div');
        state.asOf = dCrt('div');
        state.append(state.node);
        var hostFileUrl = String.format("{0}/files", ($.jCommon.string.contains(window.location.href, "rmk.disa.mil") ?
            'https://svc-2.rmk.disa.mil'
            : $.jCommon.string.replaceAll(lusidity.environment('sUri'), '/svc', '')));

        // Store a reference to the environment object
        el.data("icr", state);
        var _iavmUrl = '/pages/iavms/iavm/index.html';

        // Private environment methods
        methods = {
            init: function() {
                $('.page-content').css({padding: '60px 0 0 0', overflow: 'hidden'});
                state.css({'overflow': 'hidden'});
                function resize() {
                    lusidity.resizePage(60);
                }
                resize();
                lusidity.environment('onResize', resize);
                state.node.children().remove();
                methods.html.init();
            },
            getCls: function (item) {
                var r = item.key;
                switch(r){
                    case "passed":
                        r = "info-blue";
                        break;
                    case "failed":
                        r = "high";
                        break;
                    case "unknown":
                        r="purple-back";
                        break;
                }
                return r;
            },
            options: function (data) {
                var init = false;
                return {
                    groupsEnabled: false,
                    filter: state.opts.filter,
                    view: "details",
                    title: 'IAVM Compliance',
                    realTime: true,
                    settings: null,
                    noHead: true,
                    paging: false,
                    getUrl: function (data, start, limit) {
                        return '/iavm/report/compliance?limit=10000';
                    },
                    sortable: true,
                    sortOn: [ {property: 'packedVulnerabilityMatrix', asc: false}],
                    lists:{
                        groups:[],
                        filters:[]
                    },
                    actions: [
                        {
                            glyph: "glyphicon-cog",
                            title: "",
                            items:[
                                {
                                    glyph: "glyphicons glyphicons-download",
                                    title: "Download CSV file",
                                    onCreated: function (node, glyph, title, data) {
                                        node.attr("data-toggle", "tooltip").attr("data-placement", "left")
                                            .attr("title", 'The CSV file can open directly in excel or be used to import into most any tool.');
                                        node.tooltip();

                                    },
                                    mouseEnter: function (node, glyph, title, data) {
                                        ///node.find('.action-tooltip').show();
                                    },
                                    mouseLeave: function (node, glyph, title, data) {
                                        //node.find('.action-tooltip').hide();
                                    },
                                    clicked: function(node, glyph, title, data){
                                        if(state.opts.fileName){
                                            var url = String.format("{0}/{1}", hostFileUrl, state.opts.fileName);
                                            window.open(url, '_blank');
                                        }
                                        else{
                                            lusidity.info.yellow("The file is not available.");
                                            lusidity.info.show(5);
                                        }
                                    }
                                }
                            ]
                        }
                    ],
                    details: {
                        search: true,
                        searchText: 'Build your IAVM list...',
                        buttonText: 'Add',
                        sort: true,
                        before: function (data) {
                            if(!data || !data.results){
                                return data;
                            }
                            var r = data.results ? data : {};
                            $.each(data.results, function () {
                                if(this.noticeId){
                                    var v = this.noticeId.toUpperCase();
                                    this.noticeId = v;
                                    var p = v.split('-');
                                    if(p.length === 3) {
                                        this.s1 = p[0];
                                        this.s2 = p[1];
                                        this.s3 = p[2];
                                    }
                                }
                            });
                            r.results = $.jCommon.array.sort(data.results, [{property: "s1", asc: false}, {property: "s2", asc: true}, {property: "s3", asc: false}]);
                            if(!r.hits && r.results) {
                                r.hits = r.results.length;
                            }
                            return r;
                        },
                        mapping: [
                            {header: { title: "IAVM Notice ID", sortable: false, property: 'noticeId'}, searchable: true, property: 'noticeId', type: 'string', callback: function (td, item, value) {
                                if(!state.opts.fileName && item.fileName){
                                    state.opts.fileName = item.fileName;
                                }
                                if(!item.relatedId){
                                    item.relatedId="/";
                                }
                                if(!item.title){
                                    item.title = "not updated yet";
                                }
                                var a = dCrt('a').attr('href', item.relatedId).attr('target', "_blank").html(value.toUpperCase());
                                td.append(a);
                            }},
                            {
                                header: {title: 'Title'},
                                property: 'title', type: 'string', callback: function (td, item, value) {
                                td.html(value);

                                if($.jCommon.string.equals(value, 'undefined iavm', true)){
                                    td.attr('title', "This title is a placeholder and will be updated when a new IAVM list is imported and this IAVM notice id is matched.");
                                }
                            }
                            },
                            {header: {title: "POA&M Date", tip: "POA&M Mitigation Date", autoSize: true}, property: 'due', callback: function (td, item, value) {
                                if(item.asOf){
                                    if(!init && item.asOf && state.asOf){
                                        init=true;state.asOf.html(String.format("As of {0}", $.jCommon.dateTime.defaultFormat(item.asOf)));
                                    }
                                }
                                if(value) {
                                    var dt = $.jCommon.dateTime.defaultFormat(value);
                                    var p = dt.split(',');
                                    if(p.length === 3){
                                        dt = String.format("{0}, {1}", p[0], p[1]);
                                    }
                                    td.html(dt);
                                }
                            }},
                            {header: {title: "Passed", tip: "Scanned assets that have Passed", autoSize: true}, property: 'passed', type: 'integer', callback: function (td, item, value) {
                                var v = $.jCommon.is.numeric(value) ? parseInt(value) : 0;
                                td.attr('data-value', v);
                                if(v>0){
                                    v = dCrt('a').attr('target', '_blank').attr('href', String.format(_iavmUrl + '?id={0}&passed=true', item[state.KEY_ID])).html($.jCommon.number.commas(v));
                                }
                                td.css({width: '20px', textAlign: 'right'}).append(v);
                            }},
                            {header: {title: "Failed", tip: "Scanned assets that have Failed", autoSize: true}, property: 'failed', type: 'integer', callback: function (td, item, value) {
                                var v = $.jCommon.is.numeric(value) ? parseInt(value) : 0;
                                td.attr('data-value', v);
                                if(v>0){
                                    v = dCrt('a').attr('target', '_blank').attr('href', String.format(_iavmUrl + '?id={0}&passed=false', item[state.KEY_ID])).html($.jCommon.number.commas(v));
                                }
                                td.css({width: '20px', textAlign: 'right'}).append(v);
                            }},
                            {header: {title: "Total Scanned", tip: "Total assets scanned.", autoSize: true}, property: 'total', type: 'integer', callback: function (td, item, value) {
                                if($.jCommon.is.numeric(value)) {
                                    td.attr('data-value', value);
                                    td.css({width: '20px', textAlign: 'right'}).append($.jCommon.number.commas(value));
                                }
                            }},
                            {header: {title: "% Compliant", tip: "Percent Compliant.", autoSize: true}, property: 'total', type: 'integer', callback: function (td, item, value) {
                                if($.jCommon.is.numeric(item.total) && $.jCommon.is.numeric(item.passed)) {
                                    var d = (item.passed/item.total).toFixed(2);
                                    d = (d*100).toFixed(0);
                                    td.css({width: '20px', textAlign: 'right'}).append(d+'%');
                                }
                            }}
                        ]
                    }
                };
            },
            html:{
                init: function () {
                    methods.html.header();
                    var hd = dCrt('div').addClass('row content');
                    state.node.append(hd);
                    state.pnl = dCrt('div');
                    state.node.append(state.pnl);
                    state.pnl.jFilterBar(methods.options());
                    state.pnl.on('table-view-filtered', function () {
                        methods.html.total();
                    });
                    state.pnl.on('table-view-created', function () {
                        methods.html.total();
                    });
                },
                header: function () {
                    if(!state.header) {
                        state.header = dCrt("div").addClass('jumbotron no-radius').css({
                            margin: "-10px 0 10px 0",
                            padding: "10px 20px",
                            minHeight: '160px'
                        });
                        state.node.append(state.header);
                    }
                },
                total: function () {
                    state.header.children().remove();
                    var tbls = state.pnl.find('table');
                    var tbl = $(tbls[0]);

                    var tb = tbl.find('tbody');
                    var all = tb.find('tr');
                    var rows = [];
                    $.each(all, function () {
                        if ($(this).is(':visible')) {
                            rows.push($((this)));
                        }
                    });

                    var p = 0;
                    var f = 0;
                    var t = 0;
                    var rt = 0;

                    function getNum(node) {
                        var v = node.attr('data-value');
                        return $.jCommon.is.numeric(v) ? parseInt(v) : 0;
                    }

                    $.each(rows, function () {
                        var c = $(this).children();
                        var cp = getNum($(c[3]));
                        var cf = getNum($(c[4]));
                        var ct = getNum($(c[5]));
                        p += parseInt(cp);
                        f += parseInt(cf);
                        t += parseInt(ct);
                        rt++;
                    });
                    var d = (p/t).toFixed(2);
                    d = (d*100).toFixed(0);
                    var tbl = dCrt('table');
                    state.header.append(tbl);
                    var tr = dCrt('tr');
                    var td1 = dCrt('td');
                    var td2 = dCrt('td').attr('width', '100px').css({width: "140px", maxWidth: "140px", position: 'relative'});
                    tbl.append(tr.append(td1).append(td2));
                    var h = dCrt('h3').append(String.format("IAVM Compliance Summation for {0} IAVMs", $.jCommon.number.commas(rt)));
                    var sum = dCrt('div').append(String.format("Passed: {0}<br/>Failed: {1}<br/>Total Scanned: {2}<br/>Compliant: {3}%", $.jCommon.number.commas(p), $.jCommon.number.commas(f), $.jCommon.number.commas(t), d));
                    td1.append(h).append(state.asOf).append(sum);
                    methods.html.chart([p, f], td2);
                },
                chart: function (data, node) {
                    if (data) {
                        var container = dCrt('div').css({
                            height: "100px",
                            maxHeight: "100px",
                            width: "140px",
                            maxWidth: "140px",
                            margin: "-20px 40px 0"
                        });
                        node.append(container);
                        var ctx = dCrt('canvas').attr('id', "iavm_chart").attr('height', '100px').attr('width', '100px');
                        container.append(ctx);
                        var p = data.p;
                        var f = data.f;
                        var item = {
                            labels: [
                                "Passed",
                                "Failed"
                            ],
                            datasets: [
                                {
                                    data: data,
                                    backgroundColor: [
                                        "#49b26f",
                                        "#d9534f"
                                    ],
                                    hoverBackgroundColor: [
                                        "#49b26f",
                                        "#d9534f"
                                    ]
                                }]
                        };

                        var myPieChart = new Chart(ctx, {
                            type: 'pie',
                            data: item,
                            options: {
                                animateScale: true,
                                legend: {
                                    display: false
                                }
                            }
                        });
                    }
                }
            }
        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.icr.defaults = {};


    //Plugin Function
    $.fn.icr = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.icr($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $icr = $(this).data('icr');
            switch (method) {
                case 'exists': return (null!==$icr && undefined!==$icr && $icr.length>0);
                case 'state':
                default: return $icr;
            }
        }
    };

})(jQuery);
