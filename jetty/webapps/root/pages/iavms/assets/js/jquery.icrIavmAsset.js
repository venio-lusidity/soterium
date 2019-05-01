;(function ($) {
    //Object Instance
    $.icrIavmAsset = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.icrIavmAsset.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.asOf = dCrt('div');
        state.linkStatus = {};
        // Store a reference to the environment object
        el.data("icrIavmAsset", state);
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
                methods.assets();
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
                function hasFilters(filters) {
                    return ($.jCommon.json.getSortedKeyArray(filters.must).length>0) || ($.jCommon.json.getSortedKeyArray(filters.should).length>0);
                }
                state.opts.isAsset = $.jCommon.string.contains(state.opts.data[state.KEY_ID], "asset");

                function total(node, item, filters){
                    node.children().remove();
                    if(state.opts.isAsset){
                       return false;
                    }
                    state.opts.data.filters = null;
                    state.opts.data.groups = null;
                    var qry = jIavmAssetFactory.totals(state.opts.data, item);
                    var fltrs = $.extend({}, filters);
                    if(hasFilters(fltrs)) {
                        $.each(fltrs.must, function (k, v) {
                            if (k === 'title') {
                                fltrs.must['other'] = v;
                                delete fltrs.must.title;
                                return false;
                            }
                        });
                    }
                    qry.filters = fltrs;
                    var s = function (data) {
                        if(data){
                            var r = dCrt('div');
                            var en = dCrt('span').attr('title', "Enumerated").html($.jCommon.number.commas(data.enumerated));
                            r.append(en);
                            node.append(r);
                        }
                    };
                    $.htmlEngine.request(qry.url, s, s, qry, 'post');
                }
                var opts = {
                    title: 'Unique Assets',
                    paging: true,
                    titleHdr: state.titleHdr,
                    showFoundOnly: true,
                    onData: function (data) {
                        if (data.asOf) {
                            if (!init && data.asOf && state.asOf) {
                                init = true;
                                state.asOf.html(String.format("As of {0}", $.jCommon.dateTime.defaultFormat(data.asOf)));
                            }
                        }
                    },
                    group: {
                        enabled: false,
                        store: 'technology_security_vulnerabilities_iavms_iavm_asset_unique_details',
                        partition: 'technology_security_vulnerabilities_iavms_iavm_asset_unique_details',
                        groups: []
                    },
                    actions: [
                        {
                            glyph: "glyphicon-cog",
                            title: "",
                            items: [
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
                                    clicked: function (data) {
                                        var key = state.opts.data[state.KEY_ID];
                                        var detail =  "csv";
                                        var view = state.opts.data.et_view;
                                        var filter = "iavm_unique_asset_failed";
                                        var exact = state.opts.data.et_exact;
                                        var url = lusidity.environment('host-download') + key +"/hierarchy/details?detail="+detail +"&view="+view+"&filter="+filter+"&exact="+exact+"&start=0&limit=0";
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
                                        $.htmlEngine.request(url, s, f, groups, 'post');
                                    }
                                }
                            ]
                        }
                    ],
                    offset: {
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
                        limit: 80,
                        hovered: true,
                        keyId: 'lid',
                        singleSort: true,
                        getQuery: function () {
                            return jIavmAssetFactory.details(state.opts.data, 'otherId');
                        },
                        distinct: {
                            data: state.opts.data
                        },
                        filter: {
                            enabled: true,
                            store: 'technology_security_vulnerabilities_iavms_iavm_asset_unique_details',
                            partition: 'technology_security_vulnerabilities_iavms_iavm_asset_unique_details',
                            properties: [
                                {
                                    key: 'findings', callback: function (node, item, filters) {
                                    node.css({textAlign: 'right'});
                                    total(node, item, filters);
                                }
                                },
                                {key: 'title', role: 'suggest'},
                                {
                                    key: 'compliant', role: 'suggest', replaceSpace: "_", onTerm: function (item) {
                                    item.compliant = item.value;
                                    var ct = $.htmlEngine.compliant(item);
                                    item.label = ct.label;
                                }},
                                {key: 'ditpr', role: 'suggest'},
                                {key: 'ditprAltId', role: 'filter', 'type': 'number'},
                                {key: 'location', role: 'suggest'}, {key: 'managed', role: 'suggest'},
                                {key: 'owned', role: 'suggest'}
                            ]
                        },
                        search: {
                            enabled: false,
                            text: "Build a list",
                            btn: "Add",
                            properties: ['other', 'ditpr', 'ditprAltId', 'location', 'managed', 'owned']
                        },
                        onFiltered: function (key, value) {
                        },
                        onAfter: function (items, header, content) {
                            var n = items.length;
                            state.pnl.jFilterBar('updateCount', {on: n, total: n});
                        },
                        onBefore: function (items) {
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
                                trc.show();
                                trc.attr('data-index', e._idx);
                                e.rows.push(trc);
                            }
                        },
                        mapping: [
                            {
                                header: {title: "#", css: {width: '50px'}},
                                property: '#',
                                type: 'integer',
                                css: {width: '20px'},
                                callback: function (td, item, value) {
                                    if (item.asOf) {
                                        if (!init && item.asOf && state.asOf) {
                                            init = true;
                                            state.asOf.html(String.format("As of {0}", $.jCommon.dateTime.defaultFormat(item.asOf)));
                                        }
                                        td.append(value);
                                    }
                                }
                            },
                            {
                                header: {
                                    title: "",
                                    tip: "Click for more information.",
                                    css: {minWidth: '20px', width: '20px', maxWidth: '20px'}
                                },
                                css: {minWidth: '20px', width: '20px', maxWidth: '20px', textAlign: 'middle'},
                                property: '',
                                type: 'integer',
                                callback: function (td, item, value, map, filters, grid) {
                                    if(state.opts.isAsset){
                                        return false;
                                    }
                                    if (!td.glyph) {
                                        td.glyph = $.htmlEngine.glyph("glyphicon-triangle-bottom").css({cursor: 'pointer'});
                                        td.append(td.glyph);
                                    }
                                    if (!grid.opts.grouped) {
                                        grid.opts.grouped = false;
                                    }
                                    td.on('click', function (e) {
                                        var trc = _sub[item[_subKey]];
                                        if (trc && null !== trc) {
                                            var g1 = trc.attr('trc-grouped').toString();
                                            var g2 = grid.opts.grouped.toString();
                                            if (g1 !== g2) {
                                                trc.remove();
                                                delete _sub[item[_subKey]];
                                                trc = null;
                                            }
                                        }
                                        if (!trc) {
                                            td.glyph.removeClass('glyphicon-triangle-bottom').addClass('glyphicon-triangle-top');
                                            var r = td.parent();
                                            td.addClass('expanded').attr('init', 'init');
                                            var idx = r.index() + 1;
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
                                            pd[state.KEY_ID] = item.relatedId;
                                            var grouped = $('.group-item').length > 0;
                                            var sb = c.pAssetIavms({
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
                            },
                            {header: { title: "IAVMs", property: 'findings', css:{maxWidth: '75px'}}, property: 'findings', type: 'string', callback: function (td, item, value, filters) {
                                if($.jCommon.is.numeric(value)) {
                                    td.append(dCrt('div').css({textAlign: 'right'})).html(value);
                                }
                            }},
                            {header: {title: "RS", tip: "Risk Score", css: {minWidth: '30px', width: '30px', maxWidth: '20px'}}, property: '', type: 'integer', callback: function (td, item, value, map, filters) {
                                if($.jCommon.json.hasProperty(item, 'metrics.html.cls')) {
                                    td.addClass(item.metrics.html.cls).attr('title', item.metrics.html.label + ': ' + item.packedVulnerabilityMatrix);
                                }
                            }},
                            {header: { title: "Asset", property: 'title', sortable: true, sortType: 'string', sortTipMap: {asc: 'asc', desc: 'desc', 'none': 'Risk Score'}, css: {minWidth: '132px'}}, property: 'title', type: 'string', callback: function (td, item, value) {
                                var a = dCrt('a').attr('href', item.relatedId).attr('target', "_blank").html(value);
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
                    }
                };
                if(state.isOrg || state.isLoc) {
                    opts.group.groups.push({
                        label: 'System Name',
                        key: 'ditpr',
                        fKey: 'ditprId',
                        fValKey: 'url',
                        urlKey: 'ditprId'
                    });
                    opts.grid.mapping = $.jCommon.array.insertAt(opts.grid.mapping,
                        {
                            header: {
                                title: "DITPR ID",
                                property: 'ditprAltId',
                                css: {minWidth: '70px', width: '70px', maxWidth: '70px'}
                            },
                            property: 'ditprAltId',
                            type: 'string',
                            callback: function (td, item, value, map, filters) {
                                if (item.ditprAltId) {
                                    item.ditprAltId = item.ditprAltId.toString();
                                    var v = state.linkStatus[item.ditprId];
                                    if(!v){
                                        state.linkStatus[item.ditprId] = "none";
                                    }
                                    td.pAuthorizedLink({lCache: state.linkStatus, view:"ditpr",vertexUri:item.ditprId,linkAttributes:[{id:"title",value:value},
                                        {id:"href", value:item.ditprId},{id:"target", value: "_blank"}], linkHtml:value, schema:{}});
                                }
                            }
                        }, 6);
                    opts.grid.mapping = $.jCommon.array.insertAt(opts.grid.mapping,
                        {
                            header: {title: "System Name", property: 'ditpr'},
                            property: 'ditpr',
                            type: 'string',
                            callback: function (td, item, value, map, filters) {
                                var d = dCrt('div').addClass('ellipse-it');
                                var v = state.linkStatus[item.ditprId];
                                if(!v){
                                    state.linkStatus[item.ditprId] = "none";
                                }
                                d.pAuthorizedLink({lCache: state.linkStatus, view:"ditpr",vertexUri:item.ditprId,linkAttributes:[{id:"title",value:value},
                                    {id:"href", value:item.ditprId + "?et_view=ditpr"},{id:"target", value: "_blank"}], linkHtml:value, schema:{}});
                                td.append(d);
                            }
                        }, 7);
                }

                return opts;
            },
            html:{
                init: function () {
                    state.isOrg = $.jCommon.string.contains(state.opts.data[state.KEY_ID], 'organization');
                    state.isLoc = $.jCommon.string.contains(state.opts.data[state.KEY_ID], 'location');
                    if(!state.opts.data.et_view){
                        state.opts.data.et_view = state.isOrg ? 'managed' : state.isLoc ? 'location' : 'ditpr';
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
                    var et = state.opts.data.et_view;
                    var span = dCrt('span').html(String.format('IAVM Details {0}{1} ', (state.opts.data.et_exact ? 'Directly ':''), (et==='owned' ? 'Owned By' : et==='managed' ? 'Managed By' : et==='location' ? 'Located In' : 'for System')));
                    var link = dCrt('a').attr('target', '_blank').attr('href', String.format('{0}?et_view={1}&et_exact={2}', state.opts.data[state.KEY_ID], et, state.opts.data.et_exact)).html(String.format('{0}{1}', (state.opts.data.ditprId ? String.format('{0} ', state.opts.data.ditprId) : '') , state.opts.data.title));
                    var t = dCrt('div').append(span).append(link);
                    state.titleHdr.append(t);
                    var ft = "All";
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
    $.icrIavmAsset.defaults = {};


    //Plugin Function
    $.fn.icrIavmAsset = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.icrIavmAsset($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $icrIavmAsset = $(this).data('icrIavmAsset');
            switch (method) {
                case 'exists': return (null!==$icrIavmAsset && undefined!==$icrIavmAsset && $icrIavmAsset.length>0);
                case 'state':
                default: return $icrIavmAsset;
            }
        }
    };

})(jQuery);
