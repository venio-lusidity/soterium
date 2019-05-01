;(function ($) {
    //Object Instance
    $.jVuln = function (el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.jVuln.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.devOnly = false;
        var _sub = {};
        var _subKey = 'lid';

        // Store a reference to the environment object
        el.data("jVuln", state);

        // Private environment methods
        methods = {
            init: function () {
                function resize(){
                    $('.page-content').css({padding: '60px 0 0 0'});
                    lusidity.resizePage(65);
                }
                resize();
                lusidity.environment('onResize', resize);
                state.css({overflow: 'hidden'});
                var href = window.location.href.toString();
                state.opts.url = $.jCommon.url.create(href);
                var psd = state.opts.url.getParameter('passed');
                state.opts.passed = $.jCommon.string.equals(psd, 'true');
                state.opts.type = state.opts.passed ? 'passed' : 'failed';
                state.opts.iavmUrl = state.opts.url.getParameter('id');

                function start() {
                    state.isAsset = $.jCommon.string.startsWith(state.opts.data['vertexType'], '/electronic/network/asset');
                    state.isOrg = $.jCommon.string.contains(state.opts.data[state.KEY_ID], 'organization');
                    state.isLoc = $.jCommon.string.contains(state.opts.data[state.KEY_ID], 'location');
                    methods.html.init();
                }

                var d = state.opts.url.getParameter('d');
                if (d) {
                    d = $.jCommon.string.getFirst(d, '#');
                    state.opts.data = $.jCommon.storage.getItem(d);
                    var s = function (data) {
                        if(data._response_code){
                            var msg = btoa("<strong>Unauthorized:</strong> You do not have sufficient permissions to view the page requested.");
                            window.location = "/notification?status="+data._response_code + '&msg='+msg;
                        }
                        if (data) {
                            state.current = {item: data};
                            start();
                        }
                    };
                    var f = function (data) {};
                    var url = String.format('{0}/hierarchy/details?detail={1}&view={2}&exact={3}', state.opts.data[state.KEY_ID], 'vuln', state.opts.data.et_view, state.opts.data.et_exact);
                    $.htmlEngine.request(url, s, f,  state.opts.data.filters, 'post', false);
                }
                else {
                    start();
                }
                state.modalInfo = dCrt('div').css({position: 'absolute', top: '200px', width: '0', height: '0'});
                lusidity.append(state.modalInfo);
            },
            getDownloadUrl: function (rPath) {
                var hd = lusidity.environment('host-download');
                if($.jCommon.string.endsWith(hd, "/")){
                    hd = $.jCommon.string.stripEnd(hd);
                }
                if($.jCommon.string.endsWith(hd, "/svc")){
                    hd = hd.substring(0, hd.length-4);
                }
                return hd+rPath;
            },
            getCls: function (item) {
                var r = item.key;
                switch (r) {
                    case "passed":
                        r = "info-blue";
                        break;
                    case "failed":
                        r = "high";
                        break;
                    case "unknown":
                        r = "purple-back";
                        break;
                }
                return r;
            },
            url: function (data) {
                return String.format('{0}/vulnerabilities?view={1}&severity={2}&exact={3}&start=0&limit=100000', data[state.KEY_ID], data.et_view, data.filter, data.et_exact);
            },
            options: function (item) {
                function filter(n, f) {
                    if (f === item.filter) {
                        n.addClass('highlight').css({fontWeight: 'bold'});
                    }
                }

                function total(node, item, filters){
                    node.children().remove();
                    state.opts.data.filters = null;
                    state.opts.data.groups = null;
                    var qry = jAssetVulnDetailFactory.totals(state.opts.data, item);
                    var fltrs = $.extend({}, filters);
                    if(!fltrs.must.vulnId && item.vulnId){
                        fltrs.must.vulnId = item.vulnId;
                    }
                    qry.filters = fltrs;
                    var s = function (data) {
                        if(data){
                            var r = dCrt('div');
                            var en = dCrt('span').attr('title', "Enumerated").html($.jCommon.number.commas(data.enumerated));
                            r.append(en);
                            if(!item.vulnId) {
                                var un = dCrt('span').attr('title', 'Unique').css({marginLeft: '2px'}).html(String.format('({0})', $.jCommon.number.commas(data.unique)));
                                r.append(un);
                            }
                            node.append(r);
                        }
                    };
                    $.htmlEngine.request(qry.url, s, s, qry, 'post');
                }

                var init = false;
                var numCss = {width: '80px', maxWidth: '80px', textAlign: 'right'};
                var w = $(window).width();
                var musts = [];

                if (state.opts.data.filter
                    && !$.jCommon.string.contains(state.opts.data.filter, "all", true)
                    && !$.jCommon.string.contains(state.opts.data.filter, "failed", true)) {
                    musts.push({key: "severity", fKey: "severity", fValKey: 'value', value: methods.getSeverity(state.opts.data.filter)});
                }
                var r = {
                    title: "Vulnerabilities",
                    titleHdr: state.titleHdr,
                    showFoundOnly: true,
                    group: {
                        enabled: false
                    },
                    actions: [
                        {
                            glyph: "glyphicon-cog",
                            title: "",
                            items: [
                                {
                                    img: "/assets/img/types/excel-2.png",
                                    title: "Export Details",
                                    onCreated: function (node, img, title, data) {
                                        node.attr("data-toggle", "tooltip").attr("data-placement", "left")
                                            .attr("title", 'Export data to a spreadsheet, do not navigate away from this page until the download has started.');
                                        node.tooltip();
                                    },
                                    mouseEnter: function (node, img, title, data) {
                                        ///node.find('.action-tooltip').show();
                                    },
                                    mouseLeave: function (node, img, title, data) {
                                        //node.find('.action-tooltip').hide();
                                    },
                                    clicked: function (data) {
                                        var key = state.opts.data[state.KEY_ID];
                                        var view = state.opts.data.et_view;
                                        var filter = state.opts.data.filter;
                                        var exact = state.opts.data.et_exact;
                                        var url = String.format('{0}{1}/hierarchy/details?view={2}&filter={3}&detail=csv&exact={4}&start=0&limit=1000000', lusidity.environment("host-download"), key, view, filter, exact);

                                        var s = function (nData) {
                                            if (nData) {
                                                var u = methods.getDownloadUrl(nData.url);
                                                window.location.replace(u, '_blank');
                                            }
                                        };
                                        var f = function () {

                                        };
                                        $.htmlEngine.request(url, s, f, state.opts.data.groups, 'post');
                                    }
                                }
                            ]
                        }
                    ],
                    musts: musts,
                    offset:{
                        parent: -56,
                        header: 0,
                        body: 0
                    },
                    grid: {
                        singleSort: true,
                        colResizable: false,
                        offset: {
                            parent: 0,
                            table: -20
                        },
                        paging: {
                            enabled: true
                        },
                        hovered: true,
                        keyId: 'lid',
                        getQuery: function () {
                            return jAssetVulnDetailFactory.details(state.opts.data, "vulnId", "vulnId");
                        },
                        distinct: {
                            data: state.opts.data
                        },
                        filter: {
                            store: 'technology_security_vulnerabilities_asset_vuln_detail',
                            partition: 'technology_security_vulnerabilities_asset_vuln_detail',
                            enabled: true,
                            properties: [{key: '_assets', callback: function (node, item, filters) {
                                node.css({textAlign: 'right'});
                                total(node, item, filters);
                            }},{key: 'severity', role: 'suggest'}, {key: 'vulnId', role: 'suggest', onTerm: function(item){
                                if(item.value){
                                    item.label = $.jCommon.string.replaceAll(item.value, '_rule', '');
                                }
                            }}]
                        },
                        search: {
                            enabled: false
                        },
                        onFiltered: function (key, value) {
                        }
                        ,
                        onAfter: function (items, header, content) {
                            var n = items.length;
                            state.pnl.jFilterBar('updateCount', {on: n, total: n});
                        },
                        onBefore: function (items){
                            return items;
                        },
                        onRowAdded: function(e){
                            if(e.isSub){
                                return false;
                            }
                            var trc = _sub[e.item[_subKey]];
                            if(trc && null!==trc){
                                var g1 = trc.attr('trc-grouped').toString();
                                var g2 = e.opts.grouped.toString();
                                if(g1!==g2){
                                    trc.remove();
                                    delete _sub[e.item[_subKey]];
                                    trc = null;
                                }
                            }
                            if(trc){
                                trc.unbind('mousenter');
                                trc.unbind('mouseleave');
                                trc.on('mouseenter', function () {
                                    var e = jQuery.Event("scroller-disabled");
                                    state.pnl.trigger(e);
                                });
                                trc.on('mouseleave', function () {
                                    var e = jQuery.Event("scroller-enabled");
                                    state.pnl.trigger(e);
                                });
                                var sp = e.row.find('.glyphicon');
                                sp.removeClass('glyphicon-triangle-bottom').addClass('glyphicon-triangle-top');
                                sp.parent().addClass('expanded');
                                trc.attr('data-index', e._idx);
                                e.rows.push(trc);
                            }
                        },
                        mapping: [
                            {
                                header: {title: "#", css: {minWidth: '50px', width: '50px', maxWidth: '50px'}},
                                property: '#',
                                type: 'integer',
                                css: {minWidth: '50px', width: '50px', maxWidth: '50px'}
                            }
                        ]
                    }
                };

                if (!state.isAsset) {
                    r.grid.mapping.push({
                        header: {
                            title: "",
                            tip: "Click for more information.",
                            css: {minWidth: '20px', width: '20px', maxWidth: '20px'}
                        },
                        css: {minWidth: '20px', width: '20px', maxWidth: '20px', textAlign: 'middle'},
                        property: '', type: 'integer', callback: function (td, item, value, map, filters, grid) {
                            if (!td.glyph) {
                                td.glyph = $.htmlEngine.glyph("glyphicon-triangle-bottom").css({cursor: 'pointer'});
                                td.append(td.glyph);
                            }
                            if(!grid.opts.grouped){
                                grid.opts.grouped = false;
                            }
                            td.on('click', function (e) {
                                var trc=_sub[item[_subKey]];
                                if(trc && null!==trc){
                                    var g1 = trc.attr('trc-grouped').toString();
                                    var g2 = grid.opts.grouped.toString();
                                    if(g1!==g2){
                                        trc.remove();
                                        delete _sub[item[_subKey]];
                                        trc = null;
                                    }
                                }
                                if(!trc){
                                    td.glyph.removeClass('glyphicon-triangle-bottom').addClass('glyphicon-triangle-top');
                                    var r = td.parent();
                                    td.addClass('expanded').attr('init', 'init');
                                    var idx = r.index()+1;
                                    trc = dCrt("div").addClass("tSubRow").attr('trc-grouped', grid.opts.grouped);
                                    _sub[item[_subKey]] = trc;
                                    trc.insertAfter(td.closest('.tRow'));
                                    var tdc = dCrt('div');
                                    trc.append(tdc);
                                    var c = dCrt('div');
                                    tdc.css({padding: '0 10px 0 30px'}).append(c);
                                    var h = 300;
                                    var w = c.availWidth();
                                    dWidth(c, w);
                                    var sp = $.htmlEngine.getSpinner();
                                    td.glyph.hide();
                                    sp.insertBefore(td.glyph);
                                    c.unbind('mousenter');
                                    c.unbind('mouseleave');
                                    trc.on('mouseenter', function () {
                                        var e = jQuery.Event("scroller-disabled");
                                        state.pnl.trigger(e);
                                    });
                                    trc.on('mouseleave', function () {
                                        var e = jQuery.Event("scroller-enabled");
                                        state.pnl.trigger(e);
                                    });
                                    c.on('table-view-loaded', function (e) {
                                        sp.remove();
                                        td.glyph.show();
                                    });
                                    var pd = $.extend({}, state.opts.data);
                                    pd.title = item.vuln;
                                    pd['/vertex/uri'] = item.relatedId;
                                    var grouped = $('.group-item').length>0;
                                    var sb = c.pVulnAssets({
                                        offset: {
                                            parent: 0,
                                            table: 0
                                        },
                                        minHeight: 0,
                                        height: 0,
                                        maxHeight: h,
                                        sub: true,
                                        grouped: grouped,
                                        data: pd,
                                        parentGrid: grid.node.parent()
                                    });
                                }
                                else if(td.hasClass('expanded')){
                                    td.removeClass('expanded');
                                    td.glyph.removeClass('glyphicon-triangle-top').addClass('glyphicon-triangle-bottom');
                                    trc.remove();
                                    delete _sub[item[_subKey]];
                                }
                                else{
                                    td.addClass('expanded');
                                    trc.insertAfter(td.closest('.tRow'));
                                    trc.show();
                                    td.glyph.removeClass('glyphicon-triangle-bottom').addClass('glyphicon-triangle-top');
                                }

                                if(grid.opts.grouped){
                                    var expanded = td.hasClass('expanded');
                                    state.pnl.jFilterBar("groupSubTableExpanded", {grid: grid, expanded: expanded, td: td});
                                }
                            });
                        }
                    });
                }

                var publishedKey;

                var m = [
                    {
                        header: {
                            title: "Vuln Id",
                            property: 'vulnId',
                            css: {minWidth: '150px', width: '150px', maxWidth: '150px'}
                        },
                        property: 'vulnId', type: 'string', callback: function(td, item, value){
                        if(value){
                            value = $.jCommon.string.replaceAll(value, '_rule', '');
                            td.append(value);
                        }
                    }
                    },
                    {
                        header: {title: "Title", property: 'vuln'},
                        property: 'vuln', type: 'string', callback: function (td, item, value, map, filters) {
                        td.append(dCrt('div').addClass('ellipse-it').append(dLink(value, item.relatedId)));
                    }
                    },
                    {
                        header: {
                            title: "Type",
                            property: 'vertexType',
                            css: {minWidth: '150px', width: '150px', maxWidth: '150px'}
                        },
                        property: 'vulnType', type: 'string', callback: function (td, item, value, map, filters) {
                        td.append(FnFactory.classTypeToName(value));
                    }
                    },
                    {
                        header: {title: "Published", defaultDir: 'desc', property: 'published', sortable: true, onBeforeSort: function (map) {
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
                ];

                if (!state.isAsset) {
                    r.grid.mapping.push(
                        {
                            header: {title: "Findings", property: '_assets', css: {width: '100px'}},
                            property: '_aggHits', type: 'num', callback: function (td, item, value, map, filters) {
                            if ($.jCommon.is.numeric(value)) {
                                td.css({textAlign: 'right'}).append($.jCommon.number.commas(value));
                            }
                        }});
                }
                if (state.isAll) {
                    r.grid.mapping.push({
                        header: {
                            title: "Severity",
                            property: 'severity',
                            css: {width: '100px'}
                        },
                        property: 'severity', type: 'string'
                    });
                }

                $.each(m, function () {
                    r.grid.mapping.push(this);
                });

                return r;
            },
            getSeverity: function(severity, root){
                var ft = severity;
                state.isAll = false;
                switch (severity) {
                    case 'passed':
                        ft = "Vulnerabilities Passed";
                        if(root) {
                            state.isAll = true;
                        }
                        break;
                    case 'failed':
                        ft = "Vulnerabilities Failed";
                        if(root) {
                            state.isAll = true;
                        }
                        break;
                    case 'iavm_passed':
                        ft = "IAVMs Passed";
                        if(root) {
                            state.isAll = true;
                        }
                        break;
                    case 'iavm_failed':
                        ft = "IAVMs Failed";
                        if(root) {
                            state.isAll = true;
                        }
                        break;
                    case 'catI':
                        ft = 'CAT I';
                        break;
                    case 'catII':
                        ft = 'CAT II';
                        break;
                    case 'catIII':
                        ft = 'CAT III';
                        break;
                    default:
                        ft = $.jCommon.string.toTitleCase(state.opts.data.filter);
                        break;
                }
                return ft;
            },
            html: {
                init: function () {
                    state.css({margin: '0 0', padding: '0 0'});
                    methods.html.header();
                    state.node = dCrt('div').css({height: 'inherit'});
                    state.append(state.node);
                    state.pnl = dCrt('div').css({height: 'inherit'});
                    state.node.append(state.pnl);
                    state.pnl.jFilterBar(methods.options(state.opts.data));
                },
                header: function () {
                    state.titleHdr = dCrt("div");
                    var et = state.opts.data.et_view;
                    var span = dCrt('span').html(String.format('Vulnerability Details {0}{1} ', (state.opts.data.et_exact ? 'Directly ':''), (et==='owned' ? 'Owned By' : et==='managed' ? 'Managed By' : et==='location' ? 'Located In' : 'for System')));
                    var link = dCrt('a').attr('target', '_blank').attr('href', String.format('{0}?et_view={1}&et_exact={2}', state.opts.data[state.KEY_ID], et, state.opts.data.et_exact)).html(String.format('{0}{1}', (state.opts.data.ditprId ? String.format('{0} ', state.opts.data.ditprId) : '') , state.opts.data.title));
                    var t = dCrt('div').append(span).append(link);
                    state.titleHdr.append(t);
                    var ft = methods.getSeverity(state.opts.data.filter, true);
                    if(state.opts.data.groups){
                        ft += " > ";
                        $.each(state.opts.data.groups, function () {
                            var grp = this;
                            ft+= String.format("{0}: {1} > ", grp.label, grp.extValue ? grp.extValue : grp.value);
                        });
                        ft = ft.substr(0, ft.length-3);
                    }
                    methods.html.getNode(state.titleHdr, "Filter Type", ft);
                    state.asOf = dCrt('div');
                    state.titleHdr.append(state.asOf);
                    var item = this;
                    if (state.current.item.asOf) {
                        i = true;
                        state.asOf.html(String.format("As of {0}", $.jCommon.dateTime.defaultFormat(state.current.item.asOf)));
                    }
                    document.title = String.format('{0} - Vulnerabilities', state.opts.data.title);
                },
                getNode: function (n, l, v) {
                    var c = dCrt('div');
                    n.append(c);
                    var lbl = dCrt('span').html(String.format('{0}:&nbsp;{1}', l, v));
                    c.append(lbl);
                },
                getAltLink: function (node, label, value, linked) {
                    var c = dCrt('div');
                    node.append(c);
                    var lbl = dCrt('span').html(String.format('{0}: &nbsp;', label));
                    c.append(lbl);
                    var v = $.jCommon.is.numeric(value) ? parseInt(value) : 0;
                    if (v > 0 && linked) {
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
    $.jVuln.defaults = {};


    //Plugin Function
    $.fn.jVuln = function (method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function () {
                new $.jVuln($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $jEnclavesSeverity = $(this).data('jVuln');
            switch (method) {
                case 'exists':
                    return (null !== $jEnclavesSeverity && undefined !== $jEnclavesSeverity && $jEnclavesSeverity.length > 0);
                case 'state':
                default:
                    return $jEnclavesSeverity;
            }
        }
    };

})(jQuery);
