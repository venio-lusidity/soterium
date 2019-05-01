;(function ($) {
    //Object Instance
    $.icrIavm = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.icrIavm.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.asOf = dCrt('div');
        state.linkStatus = {};
        // Store a reference to the environment object
        el.data("icrIavm", state);
        var _iavmUrl = '/pages/iavms/iavm/index.html';
        var _sub = {};
        var _subKey = 'lid';

        // Private environment methods
        methods = {
            init: function() {
                function resize(){
                    $('.page-content').css({padding: '60px 0 0 0', overflow: 'hidden'});
                    lusidity.resizePage(65);
                }
                resize();
                lusidity.environment('onResize', resize);
                state.css({overflow: 'hidden'});
                var href = window.location.href.toString();
                state.opts.url = $.jCommon.url.create(href);

                if(state.opts.url.hasParam('passed')) {
                    methods.iavm();
                }
                else{
                    methods.assets();
                }
            },
            assets: function () {
                var d = state.opts.url.getParameter('d');
                //parsing string from url
                d = $.jCommon.string.getFirst(d, '#');
                state.opts.data = $.jCommon.storage.getItem(d);
                $.login.authorized({"url": state.opts.data[state.KEY_ID], success: function (data) {
                    methods.html.init();
                }, failed: function (data) {
                    $.login.unauth(data);
                }});
            },
            iavm: function () {
                state.isIavm = true;
                var psd = state.opts.url.getParameter('passed');
                state.opts.passed = $.jCommon.string.equals(psd, 'true');
                state.opts.type = state.opts.passed ? 'passed' : 'failed';
                state.opts.iavmUrl = state.opts.url.getParameter('id');

                $('.page-content').css({padding: '60px 0 0 0'});
                var s = function (data) {
                    if(data._response_code){
                        var msg = btoa("<strong>Unauthorized:</strong> You do not have sufficient permissions to view the page requested.");
                        window.location = "/notification?status="+data._response_code + '&msg='+msg;
                    }
                    state.opts.data = data;
                    methods.html.init();
                };
                var f = function () {
                    lusidity.info.red('Failed to retrieve the IAVM.');
                    lusidity.info.show();
                };
                $.htmlEngine.request(state.opts.iavmUrl, s, f, null, 'get');
            },
            getDownloadUrl: function (rPath) {
                var r = rPath;
                if(!$.jCommon.string.startsWith(rPath, 'http')){
                    var hd = lusidity.environment('host-download');
                    if($.jCommon.string.endsWith(hd, "/")){
                        hd = $.jCommon.string.stripEnd(hd);
                    }
                    if($.jCommon.string.endsWith(hd, "/svc")){
                        hd = hd.substring(0, hd.length-4);
                    }
                    r = hd+rPath;
                }
                return r;
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
                state.opts.isAsset = $.jCommon.string.contains(state.opts.data[state.KEY_ID], "asset");

                function total(node, item, filters){
                    node.children().remove();
                    state.opts.data.filters = null;
                    state.opts.data.groups = null;
                    var qry = jIavmFactory.totals(state.opts.data, item);
                    var fltrs = $.extend({}, filters);
                    qry.filters = fltrs;
                    var s = function (data) {
                        if(data){
                            var r = dCrt('div');
                            var en = dCrt('span').attr('title', "Enumerated").html($.jCommon.number.commas(data.enumerated));
                            r.append(en);
                            node.append(r);
                        }
                    };
                    if(!state.opts.isAsset){
                        $.htmlEngine.request(qry.url, s, s, qry, 'post');
                    }
                }


                var opts = {
                    title: 'Unique IAVMs',
                    paging: true,
                    titleHdr: state.titleHdr,
                    showFoundOnly: true,
                    onData: function (data) {
                        if(data.asOf){
                            if(!init && data.asOf && state.asOf){
                                init=true;state.asOf.html(String.format("As of {0}", $.jCommon.dateTime.defaultFormat(data.asOf)));
                            }
                        }
                    },
                    group: {
                        enabled: false,
                        store: 'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
                        partition: 'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
                        groups: []
                    },
                    actions: [
                        {
                            glyph: "glyphicon-cog",
                            title: "",
                            items:[
                                {
                                    img: "/assets/img/types/excel-2.png",
                                    title: "Export Details",
                                    onCreated: function (node, glyph, title, data) {
                                        node.attr("data-toggle", "tooltip").attr("data-placement", "left")
                                            .attr("title", 'Export data to a spreadsheet, do not navigate away from this page until the download has started.');
                                        node.tooltip();
                                    },
                                    mouseEnter: function (node, glyph, title, data) {
                                        ///node.find('.action-tooltip').show();
                                    },
                                    mouseLeave: function (node, glyph, title, data) {
                                        //node.find('.action-tooltip').hide();
                                    },
                                    clicked: function(data){
                                        var success = function (nData) {
                                            if (nData) {
                                                if (nData) {
                                                    var u = methods.getDownloadUrl(nData.url);
                                                    window.location.replace(u, '_blank');
                                                }
                                            }
                                            if (ldr) {
                                                ldr.hide();
                                                glyph.show();
                                            }
                                        };
                                        if(state.isIavm) {
                                            var schema = [
                                                {
                                                    idx: 0,
                                                    key: 'other',
                                                    label: 'Asset'
                                                },
                                                {
                                                    idx: 1,
                                                    key: 'asOf',
                                                    label: 'As Of'
                                                },
                                                {
                                                    idx: 2,
                                                    key: 'noticeId',
                                                    label: 'Notice Id'
                                                },
                                                {
                                                    idx: 3,
                                                    key: 'ditpr',
                                                    label: 'DITPR'
                                                },
                                                {
                                                    idx: 4,
                                                    key: 'ditprAltId',
                                                    label: 'DITPR ID'
                                                },
                                                {
                                                    idx: 5,
                                                    key: 'location',
                                                    label: 'Location'
                                                },
                                                {
                                                    idx: 6,
                                                    key: 'managed',
                                                    label: 'Managed By'
                                                },
                                                {
                                                    idx: 7,
                                                    key: 'owned',
                                                    label: 'Owned By'
                                                }
                                            ];
                                            var q = jIavmFactory.iavms(state.opts);
                                            q.schema = schema;
                                            q.fileNamePrefix = state.opts.data.title + '_' + state.opts.data.noticeId + '_' + state.opts.type;
                                            var url = '/query/csv?limit=0';
                                            $.htmlEngine.request(url, success, success, q, 'post');
                                        }
                                        else{
                                            var key = state.opts.data[state.KEY_ID];
                                            var detail =  "csv";
                                            var view = state.opts.data.et_view;
                                            //var filter = state.opts.data.filter;
                                            var filter = "iavm_unique_failed";
                                            var exact = state.opts.data.et_exact;
                                            var altUrl = lusidity.environment('host-download') + key +"/hierarchy/details?detail="+detail +"&view="+view+"&filter="+filter+"&exact="+exact+"&start=0&limit=0";
                                            var s = function (nData) {
                                                if (nData) {
                                                    var u = methods.getDownloadUrl(nData.url);
                                                    window.location.replace(u, '_blank');
                                                }
                                            };
                                            var f = function () {};
                                            var groups = state.opts.data.groups ? $.jCommon.array.clone(state.opts.data.groups) : [];
                                            if(state.opts.groups){
                                                $.jCommon.array.addAll(groups, $.jCommon.array.clone(state.opts.groups));
                                            }
                                            $.htmlEngine.request(altUrl, s, f, groups, 'post');
                                        }
                                    }
                                }
                            ]
                        }
                    ],
                    offset:{
                        parent: 0,
                        header: 0,
                        body: 0
                    },
                    grid: {
                        colResizable: false,
                        offset: {
                            parent: 0,
                            table: -20
                        },
                        limit: 30000,//state.isIavm ? 80 : 1000,
                        hovered: true,
                        keyId: 'lid',
                        getQuery: function () {
                            var q;
                            if(state.isIavm) {
                                q = jIavmFactory.iavms(state.opts)
                            }
                            else{
                                // remove build a list
                                q = jIavmFactory.details(state.opts.data, 'noticeId');
                            }
                            return q;
                        },
                        distinct: {
                            data: state.opts.data
                        },
                        filter: {
                            enabled: true,
                            store: 'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
                            partition: 'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
                            properties: [{key: '_assets', callback: function (node, item, filters) {
                                node.css({textAlign: 'right'});total(node, item, filters);
                            }},{key: 'other', role: 'suggest'}, {key: 'ditpr', role: 'suggest'}, {key: 'ditprAltId', role: 'filter', 'type': 'number'}, {key: 'location', role: 'suggest'}, {key: 'managed', role: 'suggest'}, {key: 'owned', role: 'suggest'}]
                        },
                        search: {
                            enabled: false,
                            text: "Build a list",
                            btn: "Add",
                            properties: ['other', 'ditpr', 'ditprAltId', 'location', 'managed', 'owned' ]
                        },
                        onFiltered: function (key, value) {
                        },
                        onAfter: function (items, header, content) {
                            var n = items.length;
                            state.pnl.jFilterBar('updateCount', {on: n, total: n});
                        },
                        onBefore: function (items) {
                            var r = items;
                            if(!state.isIavm){
                                r = $.jCommon.array.sort(r, [{property: 'noticeYear', asc: false}, {property: 'noticeType', asc: true},{property: 'noticeNum', asc: false}])
                            }
                            return r;
                        },
                        mapping: [
                            {header: {title: "#", css: {width: '50px'}}, property: '#', type: 'integer', css: {width: '20px'}, callback: function(td, item, value){
                                if(item.asOf){
                                    if(!init && item.asOf && state.asOf){
                                        init=true;state.asOf.html(String.format("As of {0}", $.jCommon.dateTime.defaultFormat(item.asOf)));
                                    }
                                    td.append(value);
                                }
                            }}
                        ]
                    }
                };

                if(!state.isIavm){
                    opts.grid.onRowAdded = function(e){
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
                            trc.show();
                            trc.attr('data-index', e._idx);
                            e.rows.push(trc);
                        }
                    };
                    opts.grid.mapping.push({
                        header: {
                            title: "",
                            tip: "Click for more information.",
                            css: {minWidth: '20px', width: '20px', maxWidth: '20px'}
                        },
                        css: {minWidth: '20px', width: '20px', maxWidth: '20px', textAlign: 'middle'},
                        property: '', type: 'integer', callback: function (td, item, value, map, filters, grid) {
                            if(state.opts.isAsset){
                                return false;
                            }
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
                                    tdc.css({padding: '0 0px 0 30px'}).append(c);
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
                                    pd[state.KEY_ID] = item.relatedId;
                                    var grouped = $('.group-item').length>0;
                                    var sb = c.pIavmAssets({
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
                    opts.grid.mapping.push({
                        header: {title: "Findings", property: '_assets', css: {width: '100px'}},
                        property: '_aggHits', type: 'num', callback: function (td, item, value, map, filters) {
                            if ($.jCommon.is.numeric(value)) {
                                td.css({textAlign: 'right'}).append($.jCommon.number.commas(value));
                            }
                        }
                    });
                    opts.grid.mapping.push({header: { title: "IAVM Notice Id", property: 'noticeId', css: {minWidth: '132px'},callback: function (th, map) {
                        th.append(map.title);
                    }}, property: 'noticeId', type: 'string', callback: function (td, item, value) {
                        var a = dCrt('a').attr('href', item.relatedId).attr('target', "_blank").html(value);
                        td.append(a);
                    }});
                    opts.grid.mapping.push({header: { title: "Title", property: 'title'}, property: 'title', type: 'string', callback: function (td, item, value) {
                        var a = dCrt('a').attr('href', item.relatedId).attr('target', "_blank").html(value);
                        td.append(a);
                    }});
                    opts.grid.mapping.push({header: { title: "POA&M Date", property: 'due', css: {minWidth: '132px'}}, property: 'due', type: 'date', callback: function (td, item, value) {
                        if(value) {
                            var df = $.jCommon.dateTime.dateOnly(value);
                            td.append(df);
                        }
                    }});
                    opts.grid.search.properties.push('noticeId');
                    opts.grid.filter.properties.push({key: 'noticeId', role: 'suggest'});
                }
                else{
                    opts.grid.mapping.push({header: { title: "Asset", property: 'other', css: {minWidth: '132px'}}, property: 'other', type: 'string', callback: function (td, item, value) {
                        var a = dCrt('a').attr('href', item.otherId).attr('target', "_blank").html(value);
                        td.append(a);
                    }});
                    opts.grid.mapping.push({header: { title: "System Name", property: 'ditpr'}, property: 'ditpr', type: 'string', callback: function (td, item, value) {
                        var t = (value) ? $.jCommon.string.ellipsis(value, ellipsis, false) : value;
                        var v = state.linkStatus[item.ditprId];
                        if(!v){
                            state.linkStatus[item.ditprId] = "none";
                        }
                        td.pAuthorizedLink({lCache: state.linkStatus, view:"ditpr",vertexUri:item.ditprId,linkAttributes:[{id:"title",value:t},
                            {id:"href", value:item.ditprId},{id:"target", value: "_blank"}], linkHtml:t, schema:{}});
                    }});
                    opts.grid.mapping.push({header: { title: "DITPR ID", property: 'ditprAltId', css: {width: '80px'}}, property: 'ditprAltId', type: 'string', callback: function (td, item, value) {
                        if(item.ditprAltId) {
                            item.ditprAltId = item.ditprAltId.toString();
                            var v = state.linkStatus[item.ditprId];
                            if(!v){
                                state.linkStatus[item.ditprId] = "none";
                            }
                            td.pAuthorizedLink({lCache: state.linkStatus, view:"ditpr",vertexUri:item.ditprId,linkAttributes:[{id:"title",value:value},
                                {id:"href", value:item.ditprId},{id:"target", value: "_blank"}], linkHtml:value, schema:{}});
                        }
                    }});
                    opts.grid.mapping.push({header: { title: "Location", property: 'location'}, property: 'location', type: 'string', callback: function (td, item, value) {
                        if(item.location) {
                            var t = (value) ? $.jCommon.string.ellipsis(value, ellipsis, false) : value;
                            var v = state.linkStatus[item.locationId];
                            if(!v){
                                state.linkStatus[item.locationId] = "none";
                            }
                            td.pAuthorizedLink({lCache: state.linkStatus, view:"loc",vertexUri:item.locationId,linkAttributes:[{id:"title",value:t},
                                {id:"href", value:item.locationId},{id:"target", value: "_blank"}], linkHtml:t, schema:{}});
                        }
                    }});
                    opts.grid.mapping.push({header: { title: "Managed By", property: 'managed'}, property: 'managed', type: 'string', callback: function (td, item, value) {
                        if(item.managed) {
                            var t = (value) ? $.jCommon.string.ellipsis(value, ellipsis, false) : value;
                            var v = state.linkStatus[item.managedId];
                            if(!v){
                                state.linkStatus[item.managedId] = "none";
                            }
                            td.pAuthorizedLink({lCache: state.linkStatus, view:"managed",vertexUri:item.managedId,linkAttributes:[{id:"title",value:t},
                                {id:"href", value:item.managedId},{id:"target", value: "_blank"}], linkHtml:t, schema:{}});
                        }
                    }});
                    opts.grid.mapping.push({header: { title: "Owned By", property: 'owned'}, property: 'owned', type: 'string', callback: function (td, item, value) {
                        if(item.owned) {
                            var t = (value) ? $.jCommon.string.ellipsis(value, ellipsis, false) : value;
                            var v = state.linkStatus[item.ownedId];
                            if(!v){
                                state.linkStatus[item.ownedId] = "none";
                            }
                            td.pAuthorizedLink({lCache: state.linkStatus, view:"owned",vertexUri:item.ownedId,linkAttributes:[{id:"title",value:t},
                                {id:"href", value:item.ownedId},{id:"target", value: "_blank"}], linkHtml:t, schema:{}});
                        }
                    }});
                }

                return opts;
            },
            html:{
                init: function () {
                    state.isOrg = $.jCommon.string.contains(state.opts.data[state.KEY_ID], 'organization');
                    state.isLoc = $.jCommon.string.contains(state.opts.data[state.KEY_ID], 'location');
                    if(!state.opts.et_view){
                        state.opts.et_view = state.isOrg ? 'managed' : state.isLoc ? 'location' : 'ditpr';
                    }
                    methods.html.header();
                    state.node = dCrt('div').css({height: 'inherit'});
                    state.append(state.node);
                    state.pnl = dCrt('div').css({height: 'inherit', margin: "0 5px 0 5px"});
                    state.node.append(state.pnl);
                    state.pnl.jFilterBar(methods.options(state.opts.data));
                },
                header: function () {
                    state.titleHdr = dCrt("div");
                    if(state.isIavm) {
                        var span = dCrt('span').html('IAVM Compliance for&nbsp;');
                        var link = dCrt('a').attr('target', '_blank').attr('href', state.opts.data.relatedId).html(String.format('{0} - {1}', state.opts.data.title.toUpperCase(), state.opts.data.noticeId));
                        var t = dCrt('div').append(span).append(link);
                        state.titleHdr.append(t);
                        state.titleHdr.append(state.asOf);
                        methods.html.getAltLink(state.titleHdr, 'passed', state.opts.data.passed, !state.opts.passed);
                        methods.html.getAltLink(state.titleHdr, 'failed', state.opts.data.failed, state.opts.passed);
                    }
                    else{
                        var et = state.opts.data.et_view;
                        var span = dCrt('span').html(String.format('IAVM Details {0}{1} ', (state.opts.data.et_exact ? 'Directly ':''), (et==='owned' ? 'Owned By' : et==='managed' ? 'Managed By' : et==='location' ? 'Located In' : 'for System')));
                        var link = dCrt('a').attr('target', '_blank').attr('href', String.format('{0}?et_view={1}&et_exact={2}', state.opts.data[state.KEY_ID], et, state.opts.data.et_exact)).html(String.format('{0}{1}', (state.opts.data.ditprId ? String.format('{0} ', state.opts.data.ditprId) : '') , state.opts.data.title));
                        var t = dCrt('div').append(span).append(link);
                        state.titleHdr.append(t);
                        var ft;
                        switch (state.opts.data.filter){
                            case 'iavm_passed':
                                ft = "IAVMs Passed";
                                break;
                            default:
                                ft = state.isIavm ? "IAVMs Failed" : "All";
                                break;

                        }
                        if(state.opts.data.groups){
                            ft += " > ";
                            $.each(state.opts.data.groups, function () {
                                var grp = this;
                                ft+= String.format("{0}: {1} > ", grp.label, grp.extValue ? grp.extValue : grp.value);
                            });
                            ft = ft.substr(0, ft.length-3);
                        }
                        methods.html.getNode(state.titleHdr, "Filter Type", ft);
                        state.titleHdr.append(state.asOf);
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
                        v = dCrt('a').attr('target', '_blank').attr('href', String.format(_iavmUrl + '?id={0}&passed={1}', state.opts.iavmUrl, !state.opts.passed)).html(v);
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
