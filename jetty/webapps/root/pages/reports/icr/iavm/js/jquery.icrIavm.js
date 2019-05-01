

;(function ($) {
    //Object Instance
    $.icrIavm = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.icrIavm.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.asOf = dCrt('div');

        // Store a reference to the environment object
        el.data("icrIavm", state);

        // Private environment methods
        methods = {
            init: function() {
                state.css({overflow: 'hidden'});
                var href = window.location.href.toString();
                state.opts.url = $.jCommon.url.create(href);

                if(state.opts.url.hasParam('passed')) {
                    methods.iavm();
                }
                else{
                    methods.assets();
                }
                function resize() {
                    lusidity.resizePage(60);
                }
                resize();
                lusidity.environment('onResize', resize);
            },
            assets: function () {
                var d = state.opts.url.getParameter('d');
                //parsing string from url
                d = $.jCommon.string.getFirst(d, '#');
                state.opts.data = JSON.parse(atob(d));
                methods.html.init();
            },
            iavm: function () {
                state.isIavm = true;
                var psd = state.opts.url.getParameter('passed');
                state.opts.passed = $.jCommon.string.equals(psd, 'true');
                state.opts.type = state.opts.passed ? 'passed' : 'failed';
                state.opts.iavmUrl = state.opts.url.getParameter('id');

                $('.page-content').css({padding: '60px 0 0 0'});
                var s = function (data) {
                    state.opts.data = data;
                    methods.html.init();
                };
                var f = function () {
                    lusidity.info.red('Failed to retrieve the IAVM.');
                    lusidity.info.show();
                };
                $.htmlEngine.request(state.opts.iavmUrl, s, f, null, 'get');
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
            options: function (item) {
                var init = false;
                var w = $(window).width();
                var ellipsis = w<1460 ? 30 : 50;
                var opts = {
                    disabled: false,
                    fill: true,
                    filter: true,
                    view: "details",
                    title: String.format('{0} Assets', state.opts.passed ? 'Passed' : 'Failed'),
                    realTime: true,
                    settings: null,
                    paging: false,
                    item: item,
                    onData: function (data) {
                        if(data.asOf){
                            if(!init && data.asOf && state.asOf){
                                init=true;state.asOf.html(String.format("As of {0}", $.jCommon.dateTime.defaultFormat(data.asOf)));
                            }
                        }
                    },
                    getUrl: function (data, start, limit) {
                        var r;
                        if(state.isIavm) {
                            r = String.format('{0}/report/compliance?passed={1}&start={2}&limit={3}', data[state.KEY_ID], state.opts.passed, start, limit);
                        }
                        else{
                            r = String.format('{0}/hierarchy/details?detail={1}&view={2}&filter={3}&start={4}&limit={5}',  data[state.KEY_ID], data.detail, data.et_view, data.filter, start, limit);
                        }
                        return r;
                    },
                    sortable: false,
                    sortOn: [],
                    lists: {
                        groups: [
                            {
                                label: 'System Name',
                                property: 'ditpr'
                            },
                            {
                                label: 'Location',
                                property: 'location'
                            },
                            {
                                label: 'Managed By',
                                property: 'managed'
                            },
                            {
                                label: 'Owned By',
                                property: 'owned'
                            }
                        ]
                    },
                    actions: [
                        {
                            glyph: "glyphicon-cog",
                            title: "",
                            items:[
                                {
                                    img: "/assets/img/types/excel-2.png",
                                    title: "Export To Excel",
                                    onCreated: function (node, glyph, title, data) {
                                        node.attr("data-toggle", "tooltip").attr("data-placement", "left")
                                            .attr("title", 'Export data to an Excel spreadsheet, do not navigate away from this page until the download has started.');
                                        node.tooltip();
                                    },
                                    mouseEnter: function (node, glyph, title, data) {
                                        ///node.find('.action-tooltip').show();
                                    },
                                    mouseLeave: function (node, glyph, title, data) {
                                        //node.find('.action-tooltip').hide();
                                    },
                                    clicked: function(data){
                                        var key = state.opts.data[state.KEY_ID];
                                        var detail =  "csv";
                                        var view = state.opts.data.et_view;
                                        var filter = state.opts.data.filter;
                                        var exact = false;
                                        var url = key +"/hierarchy/details?detail="+detail +"&view="+view+"&filter="+filter+"&exact="+exact;
                                        var t = url;
                                        var s = function (nData) {
                                            if (nData) {
                                                var u = nData.url;
                                                // could open in new tab look it up
                                                var w = window.open(u);
                                                window.setTimeout(function(){
                                                    w.close();
                                                }, 10000);

                                            }
                                        };
                                        var f = function () {

                                        };
                                        $.htmlEngine.request(t, s, f, null, 'get');
                                    }
                                }
                            ]
                        }
                    ],
                    details: {
                        fill: true,
                        offset: {
                            parent: 0,
                            table: 0
                        },
                        all: false,
                        hovered: true,
                        keyId: 'lid',
                        limit: state.opts.passed ? state.opts.data.passed : state.opts.data.failed,
                        filter: {
                            enabled: true,
                            properties: [{key: 'other'}, {key: 'ditpr'}, {key: 'ditprAltId'}, {key: 'location'}, {key: 'managed'}, {key: 'owned'}]
                        },
                        search: {
                            enabled: true,
                            text: "Build a list",
                            btn: "Add",
                            properties: ['other', 'ditpr', 'ditprAltId', 'location', 'managed', 'owned' ]
                        },
                        onFiltered: function (key, value) {

                        },
                        onAfter: function (items, header, content) {
                            var n = items.length;
                            state.pnl.jFilterBar('updateCount', {on: n, total: n });
                        },
                        onBefore: function (items) {
                            $.each(items, function () {
                                this.other = this.other ? this.other.toLowerCase() : this.otherId ? $.jCommon.string.getLast(this.otherId, "/") : "unknown";
                                this.location = (this.location ? this.location.toUpperCase() : null);
                                this.owned = (this.owned ? this.owned.toUpperCase() : null);
                                this.managed = (this.managed ? this.managed.toUpperCase(): null);
                            });
                            var r;
                            if(state.isIavm){
                                r = $.jCommon.array.sort(items, [{property: 'other', asc: true}]);
                            }
                            else{
                                r = $.jCommon.array.sort(items, [{property: 'noticeYear', asc: false}, {property: 'noticeNum', asc: false}, {property: 'noticeType', asc: true}]);
                            }
                            return r;
                        },
                        mapping: [
                            {header: {title: "#", css: {width: '20px'}}, property: '#', type: 'integer', css: {width: '20px'}},
                            {header: { title: "Asset", property: 'other', css: {minWidth: '132px'}}, property: 'other', type: 'string', callback: function (td, item, value) {
                                var a = dCrt('a').attr('href', item.otherId).attr('target', "_blank").html(value);
                                td.append(a);
                            }},
                            {header: { title: "System Name", property: 'ditpr'}, property: 'ditpr', type: 'string', callback: function (td, item, value) {
                                if(item.asOf){
                                    if(!init && item.asOf && state.asOf){
                                        init=true;state.asOf.html(String.format("As of {0}", $.jCommon.dateTime.defaultFormat(item.asOf)));
                                    }
                                }
                                var t = (value) ? $.jCommon.string.ellipsis(value, ellipsis, false) : value;
                                var a = dCrt('a').attr('title', txt).attr('href', item.ditprId).attr('target', "_blank").html(t);
                                td.append(a);
                            }},
                            {header: { title: "DITPR ID", property: 'ditprAltId', css: {width: '80px'}}, property: 'ditprAltId', type: 'string', callback: function (td, item, value) {
                                if(item.ditprAltId) {
                                    item.ditprAltId = item.ditprAltId.toString();
                                    var a = dCrt('a').attr('href', item.ditprId).attr('target', "_blank").html(value);
                                    td.append(a);
                                }
                            }},
                            {header: { title: "Location", property: 'location'}, property: 'location', type: 'string', callback: function (td, item, value) {
                                if(item.location) {
                                    var t = (value) ? $.jCommon.string.ellipsis(value, ellipsis, false) : value;
                                    var a = dCrt('a').attr('title', value).attr('href', item.locationId).attr('target', "_blank").html(t);
                                    td.append(a);
                                }
                            }},
                            {header: { title: "Managed By", property: 'managed'}, property: 'managed', type: 'string', callback: function (td, item, value) {
                                if(item.managed) {
                                    var t = (value) ? $.jCommon.string.ellipsis(value, ellipsis, false) : value;
                                    var a = dCrt('a').attr('title', value).attr('href', item.managedId).attr('target', "_blank").html(t);
                                    td.append(a);
                                }
                            }},
                            {header: { title: "Owned By", property: 'owned'}, property: 'owned', type: 'string', callback: function (td, item, value) {
                                if(item.owned) {
                                    var t = (value) ? $.jCommon.string.ellipsis(value, ellipsis, false) : value;
                                    var a = dCrt('a').attr('title', value).attr('href', item.ownedId).attr('target', "_blank").html(t);
                                    td.append(a);
                                }
                            }}
                        ]
                    }
                };

                if(!state.isIavm){
                    var tmp = $.jCommon.array.insertAt(opts.details.mapping, {header: { title: "Notice Id", property: 'noticeId', css: {minWidth: '132px'},callback: function (th, map) {
                        th.append(map.title);
                    }}, property: 'noticeId', type: 'string', callback: function (td, item, value) {
                        var a = dCrt('a').attr('href', item.relatedId).attr('target', "_blank").html(value);
                        td.append(a);
                    }}, 2);
                    opts.details.mapping = tmp;
                    opts.details.search.properties.push('noticeId');
                    opts.details.filter.properties.push({key: 'noticeId'});
                }

                return opts;
            },
            html:{
                init: function () {
                    methods.html.header();
                    state.node = dCrt('div');
                    state.append(state.node);
                    var hd = dCrt('div').addClass('row content');
                    state.node.append(hd);
                    state.pnl = dCrt('div').css({margin: "0 5px 0 5px"});
                    state.node.append(state.pnl);
                    state.pnl.jFilterBar(methods.options(state.opts.data));
                },
                header: function () {
                    var h = dCrt("div").addClass('jumbotron no-radius').css({
                        margin: "-10px 5px 10px 5px",
                        padding: "10px 20px"
                    });
                    state.append(h);
                    if(state.isIavm) {
                        var span = dCrt('span').html('IAVM Compliance for&nbsp;');
                        var link = dCrt('a').attr('target', '_blank').attr('href', state.opts.data.relatedId).html(String.format('{0} - {1}', state.opts.data.title.toUpperCase(), state.opts.data.noticeId));
                        var t = dCrt('h5').append(span).append(link);
                        h.append(t);
                        h.append(state.asOf);
                        methods.html.getAltLink(h, 'passed', state.opts.data.passed, !state.opts.passed);
                        methods.html.getAltLink(h, 'failed', state.opts.data.failed, state.opts.passed);
                    }
                    else{
                        var span = dCrt('span').html('IAVM Details for&nbsp;');
                        var link = dCrt('a').attr('target', '_blank').attr('href', state.opts.data[state.KEY_ID]).html(String.format('{0}', state.opts.data.title));
                        var t = dCrt('h5').append(span).append(link);
                        h.append(t);
                        var ft;
                        switch (state.opts.data.filter){
                            case 'iavm_passed':
                                ft = "IAVMs Passed";
                                break;
                            case 'iavm_failed':
                                ft = "IAVMs Failed";
                                break;

                        }
                        methods.html.getNode(h, "Filter Type", String.format('{0}: {1}', ft, $.jCommon.number.commas(state.opts.data.count)));
                        h.append(state.asOf);
                        document.title = String.format('{0} - Assets', state.opts.data.title);
                    }
                    if(state.isIavm) {
                        document.title = String.format('{0} - IAVM Compliance', state.opts.data.noticeId);
                    }
                    else{
                        document.title = String.format('{0} - IAVM Compliance', state.opts.data.title);
                    }
                },
                getNode: function (n, l, v) {
                    var c = dCrt('div');
                    n.append(c);
                    var lbl = dCrt('span').html(String.format('{0}: &nbsp;{1}', l, v));
                    c.append(lbl);
                },
                getAltLink: function (node, label, value, linked) {
                    var c = dCrt('div');
                    node.append(c);
                    var lbl = dCrt('span').html(String.format('{0}: &nbsp;', label));
                    c.append(lbl);
                    var v = $.jCommon.is.numeric(value) ? parseInt(value) : 0;
                    if(v>0 && linked){
                        v = dCrt('a').attr('target', '_blank').attr('href', String.format('/iavms/compliance/iavm?id={0}&passed={1}', state.opts.iavmUrl, !state.opts.passed)).html(v);
                    }
                    c.append(v);
                }
            }
        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.icrIavm.defaults = {};


    //Plugin Function
    $.fn.icrIavm = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.icrIavm($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $icrIavm = $(this).data('icrIavm');
            switch (method) {
                case 'exists': return (null!==$icrIavm && undefined!==$icrIavm && $icrIavm.length>0);
                case 'state':
                default: return $icrIavm;
            }
        }
    };

})(jQuery);
