;(function ($) {
    //Object Instance
    $.adHocSoftware = function (el, options) {
        var state = el,
            methods = {};
        state.container = $(el);
        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data};
        state.opts = $.extend({}, $.adHocSoftware.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.KEY_ID = '/vertex/uri';
        var _sub = {};
        var _subKey = 'lid';
        state.devOnly = false;
        state.linkStatus = {};

        // Store a reference to the environment object
        el.data('adHocSoftware', state);

        // Private environment methods
        methods = {
            init: function () {
                state.isAsset = $.jCommon.string.startsWith(state.opts.current['vertexType'], '/electronic/network/asset');
                state.isOrg = $.jCommon.string.contains(state.opts.current[state.KEY_ID], 'organization');
                state.isLoc = $.jCommon.string.contains(state.opts.current[state.KEY_ID], 'location');
                if(!state.opts.et_view){
                    state.opts.et_view = state.isOrg ? 'managed' : state.isLoc ? 'location' : 'ditpr';
                }
                var c = dCrt('span').css({position: 'relative', top: '-8px'});
                var h = dCrt('span').css({wordBreak: 'break-all'});
                var t =state.opts.current.title;
                var type = $.adhoc.getEtv(state.opts.current);

                var tl = dCrt('span').append(String.format("Report Builder for {0}: ", type));
                var hl = dCrt('span').append(dLink(t, state.opts.current[state.KEY_ID] + '?et_view=' + state.opts.current.et_view)).css({marginRight: '100px', wordBreak: 'break-all'});

                state.opts.hdrInfoNode = dCrt('span').append(String.format(' {0} Total Assets', $.jCommon.number.commas(state.opts.current.max)));
                h.append(tl).append(hl);
                c.append(state.opts.hdrInfoNode).append(dCrt('br').css({lineHeight: '2px'})).append(h);

                var actions = [{
                    glyph: "glyphicon-cog",
                    title: "",
                    items: [
                        {
                            glyph: "glyphicon-refresh",
                            title: "Coming Soon",
                            clicked: function (node, glyph, title, data) {
                            }
                        }
                    ]
                }];
                state.builderNode = $.htmlEngine.panel(state.worker.node, "glyphicons glyphicons-article", c, null, false);
                methods.html.init();
            },
            getValue: function (severity, item, callback, filters, hasF) {
                if(!hasF && $.jCommon.is.numeric(item[severity])){
                    console.log('numeric value retrieved');
                    if($.isFunction(callback)){
                        callback();
                    }
                }
                else {
                    var s = function (data) {
                        item[severity] = (data && data.et_exact) ? data.et_exact : 0;
                        if ($.isFunction(callback)) {
                            callback();
                        }
                    };

                    var url = String.format('{0}/severity/{1}', item.otherId ? item.otherId : item.relatedUri, severity);
                    $.htmlEngine.request(url, s, s, filters, hasF? "post" : "get");
                }
            },
            options: function (data) {
                var sorted =false;
                var init=false;
                var numCss = {minWidth: '70px', width: '70px', maxWidth: '70px', textAlign: 'right'};
                var w = $(window).width();
                function total(node, item, filters){
                    // This is the severity totals on the header line.
                    var r = dCrt('div');
                    var en = dCrt('span').attr('title', "Enumerated").html($.jCommon.number.commas(0));
                    var un = dCrt('span').attr('title', 'Unique').css({marginLeft: '2px'}).html(String.format('({0})', $.jCommon.number.commas(0)));
                    r.append(en).append(un);
                    node.append(r);
                }
                function hasFilters(filters) {
                    return ($.jCommon.json.getSortedKeyArray(filters.must).length>0) || ($.jCommon.json.getSortedKeyArray(filters.should).length>0);
                }
                function getValues(node, severity, item, filters){
                    var spin = $.htmlEngine.getSpinner();
                    node.append(spin);
                    function c(){
                        spin.remove();
                        var v = item[severity];
                        node.html(v);
                    }
                    methods.getValue(severity, item, c, filters, hasFilters(filters));
                }
                var r = {
                    colResizable: false,
                    data: data,
                    paging: {
                        enabled: true
                    },
                    offset: {
                        parent: 0,
                        table: -20
                    },
                    singleSort: true,
                    hovered: true,
                    keyId: 'lid',
                    getQuery: function(){
                        return jAssetFactory.assets(state.opts.data);
                    },
                    distinct: {
                        data: state.opts.data
                    },
                    filter: {
                        enabled: false,
                        nullable: true,
                        nullValue: "",
                        store: 'technology_security_vulnerabilities_vulnerability_details',
                        partition: 'technology_security_vulnerabilities_vulnerability_details',
                        properties: [{key: 'title', role: 'suggest'},
                            {key: 'compliant', role: 'suggest', replaceSpace: "_",
                                onResults: function(node, item, data){
                                    data.results = $.jCommon.array.sort(data.results, [{property: 'compliant', asc: true}]);
                                }, onTerm: function(item){
                                item.compliant = item.value;
                                var ct = $.htmlEngine.compliant(item);
                                item.label = ct.label;
                            }},{key: 'ditpr', role: 'suggest'}, {key: 'ditprAltId', role: 'filter', type: 'number'}, {key:'location', role: 'suggest'}, {key:'managed', role: 'suggest'}, {key:'owned', role: 'suggest'},
                            {key: 'catI', callback: function (node, item, filters) {
                                node.css({textAlign: 'right'});
                                total(node, item, filters);
                            }},{key: 'catII', callback: function (node, item, filters) {
                                node.css({textAlign: 'right'});total(node, item, filters);
                            }},{key: 'catIII', callback: function (node, item, filters) {
                                node.css({textAlign: 'right'});total(node, item, filters);
                            }},{key: 'critical', callback: function (node, item, filters) {
                                node.css({textAlign: 'right'});total(node, item, filters);
                            }},{key: 'high', callback: function (node, item, filters) {
                                node.css({textAlign: 'right'});total(node, item, filters);
                            }},{key: 'medium', callback: function (node, item, filters) {
                                node.css({textAlign: 'right'});total(node, item, filters);
                            }},{key: 'low', callback: function (node, item, filters) {
                                node.css({textAlign: 'right'});total(node, item, filters);
                            }},{key: 'unknown', callback: function (node, item, filters) {
                                node.css({textAlign: 'right'});total(node, item, filters);
                            }}]
                    },
                    search: {
                        enabled: false,
                        text: "What are you looking for?",
                        btn: "Add",
                        properties: ['title', 'ditpr', 'ditprAltId', 'location', 'managed', 'owned' ]
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
                    onFiltered: function (key, value) {

                    },
                    onBefore: function (items, node) {
                        var r = items;
                        $.each(r, function () {
                            var item = this;
                            var p = item.packedVulnerabilityMatrix;
                            item.exploit = parseInt(p===9999 ? 0 : p.toString().substring(0, 4));
                            item.findings = (item.catI + item.catII + item.catIII + item.critical + item.high + item.medium + item.low+ (item.unknown ? item.unknown : 0));
                            var ct = $.htmlEngine.compliant(this);
                            item.compliant = ct.label;
                            item._valid = true;
                        });
                        return r;
                    },
                    mapping: [
                        {header: {title: "#", css: {minWidth: '50px', width: '50px', maxWidth: '50px'}}, property: '#', type: 'integer', css: {minWidth: '50px', width: '50px', maxWidth: '50px'}},
                        {header: {title: "RS", tip: "Risk Score", css: {minWidth: '30px', width: '30px'}}, property: '', type: 'integer', callback: function (td, item, value, map, filters) {
                            if($.jCommon.json.hasProperty(item, 'metrics.html.cls')) {
                                td.addClass(item.metrics.html.cls).attr('title', item.metrics.html.label + ': ' + item.packedVulnerabilityMatrix);
                            }
                        }},
                        {header: { title: "Asset", property: 'title', sortable: false, sortType: 'string', sortTipMap: {asc: 'asc', desc: 'desc', 'none': 'Risk Score'}, css: {minWidth: '132px'}}, property: 'title', type: 'string', callback: function (td, item, value, map, filters) {
                            var a = dCrt('a').attr('href', item.otherId ? item.otherId : item.relatedUri).attr('target', "_blank").html(value);
                            td.append(a);
                        }},
                        {header: { title: 'HBC', tip: 'HBSS Baseline Compliance', property: 'compliant', css: {minWidth: '50px', width: '50px', maxWidth: '50px'}},
                            css: {minWidth: '50px', width: '50px', maxWidth: '50px'},
                            property: 'compliant', type: 'string', callback: function (td, item, value, map, filters) {
                            td.osIcon({
                                display: "tile",
                                fontSize: '16px',
                                hasTitle: false,
                                hasVersion: false,
                                linked: true,
                                data: {'/properties/technology/software/operatingSystem': {title: item.os, '/vertex/uri': item.osId}}
                            });
                            var ct = $.htmlEngine.compliant(item);
                            td.attr('title', ct.tip).addClass(ct.clr);
                        }},
                        {header: { title: "Location", property: 'location'}, property: 'location', type: 'string', callback: function (td, item, value, map, filters) {
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
                        {header: { title: "Managed By", property: 'managed'}, property: 'managed', type: 'string', callback: function (td, item, value, map, filters) {
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
                        {header: { title: "Owned By", property: 'owned'}, property: 'owned', type: 'string', callback: function (td, item, value, map, filters) {
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
                if(!state.isAsset){
                    r.mapping = $.jCommon.array.insertAt(r.mapping, {
                        header: {title: "", tip: "Click for more information.", css: {minWidth: '22px', width: '22px', maxWidth: '22px'}},
                        css: {textAlign: 'middle'},
                        property: '', type: 'integer', callback: function (td, item, value, map, filters, grid) {
                            var tot = item.catI+item.catII+item.catIII+item.critical+item.high+item.medium+item.low+(item.unknown ? item.unknown : 0);
                            if(tot<=0){
                                return false;
                            }
                            if(!td.glyph){
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
                                if(!trc) {
                                    td.glyph.removeClass('glyphicon-triangle-bottom').addClass('glyphicon-triangle-top');
                                    var r = td.parent();
                                    td.addClass('expanded').attr('init', 'init');
                                    var idx = r.index() + 1;
                                    trc = dCrt("div").addClass("tSubRow").attr('trc-grouped', grid.opts.grouped);
                                    _sub[item[_subKey]] = trc;
                                    var lnk = td.closest('.tRow');
                                    var lnkId = lnk.attr('data-index');
                                    trc.attr('data-index', lnkId).insertAfter(lnk);
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
                                    var pd = $.extend({}, state.opts.current);
                                    pd.title = item.other ? item.other : item.title;
                                    pd['/vertex/uri'] = item.relatedUri;

                                   // pd.prefixKey;
                                   // pd.prefixTree;

                                    var sb = c.pVulnerabilities({
                                        offset: {
                                            parent: 0,
                                            table: 0
                                        },
                                        minHeight: 0,
                                        height: 0,
                                        maxHeight: h,
                                        sub: true,
                                        grouped: false,
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
                        }}, 1);
                }
                if(state.isOrg || state.isLoc){
                    r.mapping = $.jCommon.array.insertAt(r.mapping,
                        {header: { title: "System Name", property: 'ditpr'}, property: 'ditpr', type: 'string', callback: function (td, item, value, map, filters) {

                            var d = dCrt('div').addClass('ellipse-it');
                            var v = state.linkStatus[item.ditprId];
                            if(!v){
                                state.linkStatus[item.ditprId] = "none";
                            }
                            d.pAuthorizedLink({lCache: state.linkStatus, view:"ditpr",vertexUri:item.ditprId,linkAttributes:[{id:"title",value:value},
                                {id:"href", value:item.ditprId + "?et_view=ditpr"},{id:"target", value: "_blank"}], linkHtml:value, schema:{}});
                            td.append(d);
                        }}
                        , 4);
                    r.mapping = $.jCommon.array.insertAt(r.mapping,
                        {header: { title: "DITPR ID", property: 'ditprAltId', css: {minWidth: '70px', width: '70px', maxWidth: '70px'}}, property: 'ditprAltId', type: 'string', callback: function (td, item, value, map, filters) {
                            if(item.ditprAltId) {
                                item.ditprAltId = item.ditprAltId.toString();
                                var v = state.linkStatus[item.ditprId];
                                if(!v){
                                    state.linkStatus[item.ditprId] = "none";
                                }
                                td.pAuthorizedLink({lCache: state.linkStatus, view:"ditpr",vertexUri:item.ditprId,linkAttributes:[{id:"title",value:value},
                                    {id:"href", value:item.ditprId},{id:"target", value: "_blank"}], linkHtml:value, schema:{}});
                            }
                        }}, 5);
                }
                var ext = [
                    {header: { title: "CI", tip: "Cat I", property: 'catI', css: numCss, sortable: false, defaultDir: 'desc'},
                        css: numCss,data: 'catI', property: 'catI', type: 'string', callback: function (td, item, value, map, filters) {
                        td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'catII', item, filters)));
                    }},
                    {header: { title: "CII", tip: "Cat II", property: 'catII', css: numCss,sortable: false, defaultDir: 'desc'}, css: numCss,data: 'catII', property: 'catII', type: 'string', callback: function (td, item, value, map, filters) { td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'catII', item, filters)));}},
                    {header: { title: "CIII", tip: "Cat III", property: 'catIII', css: numCss,sortable: false, defaultDir: 'desc'}, css: numCss,data: 'catIII', property: 'catIII', type: 'string', callback: function (td, item, value, map, filters) { td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'catIII', item, filters)));}},
                    {header: { title: "C", tip:"Critical", property: 'critical', css: numCss,sortable: false, defaultDir: 'desc'}, css: numCss,data: 'critical', property: 'critical', type: 'string', callback: function (td, item, value, map, filters) { td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'critical', item, filters)));}},
                    {header: { title: "H", tip: "High", property: 'high', css: numCss,sortable: false, defaultDir: 'desc'}, css: numCss,data: 'high', property: 'high', type: 'string', callback: function (td, item, value, map, filters) { td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'high', item, filters)));}},
                    {header: { title: "M", tip: "Medium", property: 'medium', css: numCss,sortable: false, defaultDir: 'desc'}, css: numCss,data: 'medium', property: 'medium', type: 'string', callback: function (td, item, value, map, filters) {td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'medium', item, filters)));}},
                    {header: { title: "L", tip: "Low", property: 'low', css: numCss,sortable: false, defaultDir: 'desc'}, css: numCss,data: 'low', property: 'low', type: 'string', callback: function (td, item, value, map, filters) { td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'low', item, filters)));}},
                    {header: { title: "I", tip: "Info", property: 'unknown', css: numCss,sortable: false, defaultDir: 'desc'}, css: numCss,data: 'unknown', property: 'unknown', type: 'string', callback: function (td, item, value, map, filters) { td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'info', item, filters)));}}
                ];
                $.each(ext, function () {
                    r.mapping.push(this);
                });

                return r;
            },
            html:{
                init: function () {
                    var node = dCrt('div');
                    state.builderNode.append(node);
                    var cnt = state.opts.data._count.avail;
                    state.opts.current.max = state.opts.current.et_exact ? cnt.exact : cnt.inherited;
                    state.opts.hdrInfoNode.append(" (" + $.jCommon.number.commas(state.opts.current.max) + " total)");
                    var chrt = dCrt('div').css({clear: 'both', marginTop: '0'});
                    node.append(chrt);
                    methods.html.chart(chrt, state.opts.data);
                    state.opts.tblNode = dCrt('div');
                    node.append(state.opts.tblNode);
                    state.opts.tblNode.pGridAdhoc(methods.options(state.opts.data));
                },
                filter: function (fltr, fltr2, mc, rc, item) {
                    var parent = state.opts.tblNode.parent();
                    state.opts.tblNode.remove();
                    state.opts.tblNode = dCrt('div');
                    parent.append(state.opts.tblNode);
                    if(rc) {
                        rc.children().remove();
                    }

                    var data;
                    if($.jCommon.string.equals(fltr, '***')){
                        data = state.opts.data;
                    }
                    else {
                        var ids = [];
                        $.each(state.opts.data._subs, function (k, v) {
                            var sub = v;
                            if ($.jCommon.string.equals(sub.title, fltr, true)) {
                                if (fltr2) {
                                    if ($.jCommon.string.equals(sub.version, fltr2, true)) {
                                        ids.push(k);
                                    }
                                }
                                else {
                                    ids.push(k);
                                }
                            }
                        });

                        var items = [];
                        $.each(state.opts.data.results, function () {
                            var item = $.extend({}, this, true);
                            $.each(ids, function () {
                                var id = this.toString();
                                if ($.jCommon.array.contains(item._subs, id)) {
                                    items.push(item);
                                    return false;
                                }
                            });
                        });
                        var len = items.length;
                        data = {
                            hits: len,
                            next: len,
                            start: 0,
                            _subs: $.extend({}, state.opts.data._subs, true),
                            results: items
                        };
                        if(rc) {
                            methods.html.subChart(fltr, mc, rc, data);
                        }
                    }
                    state.opts.tblNode.pGridAdhoc(methods.options(data));
                },
                chart: function (node, data) {
                    var software = {};
                    var hits = state.opts.current.max;
                    $.each(data._subs, function (k,v) {
                        var item = v;
                        var version = $.jCommon.json.getProperty(item, '/technology/software_version/version.version');
                        if(!version){
                            version = 'unknown';
                        }
                        item.version = version;
                        var lid = k.toString();
                        if(!item.version){
                            item.version = 'unknown';
                        }
                        var r = $.jCommon.colors.toHex(item._color.r);
                        var g = $.jCommon.colors.toHex(item._color.g);
                        var b = $.jCommon.colors.toHex(item._color.b);
                        item._color1 = '#'+r+g+b;
                        item._hvrColor1 = $.jCommon.colors.lighten(item._color1, 10);
                        var key = $.jCommon.string.makeKey(item.title);
                        var val = software[key];
                        if(!val){
                            val={
                                label: item.title,
                                folded: item.title.toLowerCase(),
                                value: 0,
                                color: item._color1,
                                hoverColor: item._hvrColor1,
                                legend: {
                                    onMake: function (mc, rc, item) {
                                        mc.css({overflowX: 'hidden', overflowY: 'auto'});
                                        var m = dCrt('div').addClass('media');
                                        var ml = dCrt('div').addClass('media-left');
                                        var sz = "12px";
                                        var clr = dCrt('div').css({
                                            width: sz,
                                            height: sz,
                                            minWidth: sz,
                                            minHeight: sz,
                                            maxWidth: sz,
                                            maxHeight: sz,
                                            backgroundColor: item.color,
                                            border: '1px solid ' + item.hoverColor});
                                        m.append(ml.append(clr));
                                        var mb = dCrt('div').addClass('media-body').css({fontSize: '12px'});
                                        var n = dCrt('div');
                                        var d = dCrt('span').addClass('active-link').attr('title', 'click once to apply filter click again to remove filter').html(String.format('{0} ({1}): {2}', item.perc, item.value, item.legend.data.title));

                                        d.on('click', function () {
                                            if(d.hasClass('active')){
                                                d.removeClass('active');
                                                methods.html.filter('***', null, mc, rc, item);
                                            }
                                            else {
                                                mc.find('.media-body .active').removeClass('active');
                                                d.addClass('active');
                                                state.opts.legend = item.legend.data.title;
                                                methods.html.filter(item.legend.data.title, null, mc, rc, item);
                                            }
                                        });

                                        mb.append(n.append(d));
                                        m.append(mb);
                                        mc.append(m);
                                    },
                                    onClick: function (node, data) {

                                    },
                                    data: item
                                }
                            };
                            software[key] = val;
                        }
                        $.each(data.results, function () {
                            $.each(this._subs, function () {
                                var a = this.toString();
                                if($.jCommon.string.equals(a, lid, true)){
                                    val.value+=1;
                                    val.perc = $.jCommon.number.percentage(state.opts.current.max, val.value, true);
                                    return false;
                                }
                            });
                        });
                    });

                    var options = [];
                    $.each(software, function (key, value) {
                        if(value.value>0) {
                            options.push(value);
                        }
                    });

                    options = $.jCommon.array.sort(options, [{property: 'folded', asc: true}]);
                    function onClick(e, chart, ap) {
                        var lbl = ap[0]._view.label;
                    }
                    var len = options.length;
                    var db = (len>6) ? 4 : 12;
                    var title = ($.jCommon.string.empty(state.opts.title) || $.jCommon.string.startsWith(state.opts.title, "auto save", true)) ? "Custom" : state.opts.title;
                    var d = dCrt('div').css({'position': 'relative', top: '10px'}).html(String.format("Software Report: {0} ({1}) Assets ({2}) Software", title, data.results.length, len));
                    node.jChartIt({max: state.opts.current.max, glyph: 'glyphicons-stats', title: d, dividedBy: db, width: 350, height: 175, onClick: onClick, data: options, type: 'bar', legend: 'Software Summation'});
                },
                subChart: function (fltr, mc, rc, data) {
                    var software = {};
                    var hits = data.results.length;
                    var subs = [];
                    $.each(data._subs, function (k,v) {
                        var item = v;
                        if(!$.jCommon.string.equals(fltr, item.title, true)){
                            return true;
                        }
                        subs.push(item);
                        var lid = k.toString();
                        item._color2 = $.jCommon.colors.getRandomColor();
                        item._hvrColor2 = $.jCommon.colors.lighten(item._color2, 10);
                        var key = $.jCommon.string.makeKey(item.version);
                        var val = software[key];
                        if(!val){
                            val={
                                label: item.version,
                                folded: item.version.toLowerCase(),
                                value: 0,
                                color: item._color2,
                                hoverColor: item._hvrColor2,
                                legend: {
                                    onMake: function (mc, rc, item) {
                                        mc.css({overflowX: 'hidden', overflowY: 'auto'});
                                        var m = dCrt('div').addClass('media');
                                        var ml = dCrt('div').addClass('media-left');
                                        var sz = "12px";
                                        var clr = dCrt('div').css({
                                            width: sz,
                                            height: sz,
                                            minWidth: sz,
                                            minHeight: sz,
                                            maxWidth: sz,
                                            maxHeight: sz,
                                            backgroundColor: item.color,
                                            border: '1px solid ' + item.hoverColor});
                                        m.append(ml.append(clr));
                                        var mb = dCrt('div').addClass('media-body').css({fontSize: '12px'});
                                        var n = dCrt('div');
                                        var d = dCrt('span').addClass('active-link').attr('title', 'click once to apply filter click again to remove filter').html(String.format('{0} ({1}): {2}', item.perc, item.value, item.legend.data.version));
                                        var glyph = dCrt('span').attr('title', 'open software in new tab').addClass("glyphicons glyphicons-new-window external-url");
                                        var link = dLink("", item.legend.data[state.KEY_ID]).append(glyph);

                                        d.on('click', function () {
                                            if(d.hasClass('active')){
                                                methods.html.filter(fltr, null, mc, rc, item);
                                            }
                                            else {
                                                mc.find('.media-body .active').removeClass('active');
                                                d.addClass('active');
                                                methods.html.filter(state.opts.legend, item.legend.data.version, mc, rc, item);
                                            }
                                        });

                                        mb.append(n.append(link).append(d));
                                        m.append(mb);
                                        mc.append(m);
                                    },
                                    onClick: function (node, data) {

                                    },
                                    data: item
                                }
                            };
                            software[key] = val;
                        }
                        $.each(data.results, function () {
                            $.each(this._subs, function () {
                                var a = this.toString();
                                if($.jCommon.string.equals(a, lid, true)){
                                    val.value+=1;
                                    val.perc = $.jCommon.number.percentage(hits, val.value, true);
                                    return false;
                                }
                            });
                        });
                    });

                    var options = [];
                    $.each(software, function (key, value) {
                        if(value.value>0) {
                            options.push(value);
                        }
                    });

                    options = $.jCommon.array.sort(options, [{property: 'folded', asc: true}]);
                    function onClick(e, chart, ap) {
                        var lbl = ap[0]._view.label;
                    }
                    rc.jChartIt({glyph: 'glyphicons-stats', title: null, height: 175, onClick: onClick, data: options, limit: 0, type: 'pie', maintainAspectRatio: false, chartAndLegend: true, legend: 'Versions', tip: String.format('Versions for {0}', fltr)});
                }
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
    $.adHocSoftware.defaults = {};


    //Plugin Function
    $.fn.adHocSoftware = function (method, options) {
        if (method === undefined) method = {};

        if (typeof method === 'object') {
            return new $.adHocSoftware($(this), method);
        } else {
            // Helper strings to quickly perform functions
            var $adHocSoftware = $(this).data('adHocSoftware');
            switch (method) {
                case 'exists':
                    return (null !== $adHocSoftware && undefined !== $adHocSoftware && $adHocSoftware.length > 0);
                case 'reset':
                    $adHocSoftware.reset(options);
                    break;
                case 'resize':
                    $adHocSoftware.resize(options);
                    break;
                case 'state':
                default:
                    return $adHocSoftware;
            }
        }
    };

    $.adHocSoftware.call = function (elem, options) {
        elem.adHocSoftware(options);
    };

    try {
        $.htmlEngine.plugins.register('adHocSoftware', $.adHocSoftware.call);
    }
    catch (e) {
        console.log(e);
    }

})(jQuery);