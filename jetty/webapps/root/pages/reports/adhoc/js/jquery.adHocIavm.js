;(function ($) {

    //Object Instance
    $.adHocIavm = function(el, options) {
        var state = el,
            methods = {};

        var state = el,
            methods = {};
        state.container = $(el);
        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data};
        state.opts = $.extend({}, $.adHocSoftware.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.KEY_ID = '/vertex/uri';
        var _sub = {};
        var _subKey = 'relatedId';
        state.devOnly = false;
        state.linkStatus = {};

        // Store a reference to the environment object
        el.data("adHocIavm", state);

        // Private environment methods
        methods = {
            init: function () {
                if(methods.transform()) {
                    state.isAsset = $.jCommon.string.startsWith(state.opts.current['vertexType'], '/electronic/network/asset');
                    state.isOrg = $.jCommon.string.contains(state.opts.current[state.KEY_ID], 'organization');
                    state.isLoc = $.jCommon.string.contains(state.opts.current[state.KEY_ID], 'location');
                    if (!state.opts.et_view) {
                        state.opts.et_view = state.isOrg ? 'managed' : state.isLoc ? 'location' : 'ditpr';
                    }
                    var c = dCrt('span').css({position: 'relative', top: '-8px'});
                    var h = dCrt('span').css({wordBreak: 'break-all'});
                    var t = state.opts.current.title;
                    var type = $.adhoc.getEtv(state.opts.current);

                    var tl = dCrt('span').append(String.format("Report Builder for {0}: ", type));
                    var hl = dCrt('span').append(dLink(t, state.opts.current[state.KEY_ID] + '?et_view=' + state.opts.current.et_view)).css({
                        marginRight: '100px',
                        wordBreak: 'break-all'
                    });

                    state.opts.hdrInfoNode = dCrt('span').append(String.format(' {0} Total Assets', $.jCommon.number.commas(state.opts.current.max)));
                    h.append(tl).append(hl);
                    c.append(state.opts.hdrInfoNode).append(dCrt('br').css({lineHeight: '2px'})).append(h);

                    state.builderNode = $.htmlEngine.panel(state.worker.node, "glyphicons glyphicons-article", c, null, false);
                    methods.html.init();
                }
            },
            transform: function () {
                var d = state.opts.data;
                if($.jCommon.json.isEmpty(d, 'results')){
                    state.append(methods.html.msg("No results found."));
                    return false;
                }
                d.results = $.jCommon.array.sort(d.results, [{property: 'otherId', asc: true}]);

                var subs = {};
                var map = {};
                $.each(d.results, function () {
                    var id = this.otherId;
                    this.noticeId = $.jCommon.string.replaceAll(this.noticeId, "_rule", "").toUpperCase();
                    var a = map[id];
                    if(!a){
                        map[id] = a = this;
                        a._subs = [];
                    }

                    var rId = this.relatedId;
                    if(!$.jCommon.array.contains(a._subs, rId)){
                        a._subs.push(rId);
                    }
                    var v = subs[rId];
                    if(!v){
                        subs[rId] = this;
                    }
                });

                var f = [];
                $.each(map, function (k, v) {
                    f.push(this);
                });
                f = $.jCommon.array.sort(f, [{property: 'packedVulnerabilityMatrix', asc: false}]);
                d.results = f;
                d.hits = d.next = d.results.length;
                d._subs = subs;
                return true;
            },
            options: function (data) {
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
                            {
                                key: 'compliant', role: 'suggest', replaceSpace: "_",
                                onResults: function (node, item, data) {
                                    data.results = $.jCommon.array.sort(data.results, [{
                                        property: 'compliant',
                                        asc: true
                                    }]);
                                }, onTerm: function (item) {
                                item.compliant = item.value;
                                var ct = $.htmlEngine.compliant(item);
                                item.label = ct.label;
                            }
                            }, {key: 'ditpr', role: 'suggest'}, {
                                key: 'ditprAltId',
                                role: 'filter',
                                type: 'number'
                            }, {key: 'location', role: 'suggest'}, {key: 'managed', role: 'suggest'}, {
                                key: 'owned',
                                role: 'suggest'
                            }
                        ]
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
                        {header: { title: "Asset", property: 'other', sortable: false, sortType: 'string', sortTipMap: {asc: 'asc', desc: 'desc', 'none': 'Risk Score'}, css: {minWidth: '132px'}},
                            property: 'other', type: 'string', callback: function (td, item, value, map, filters) {
                            var a = dCrt('a').attr('href', item.otherId).attr('target', "_blank").html(value);
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
                msg: function (msg) {
                    var h = state.worker.node.availHeight(0);
                    var c = dCrt('div').addClass('centered');
                    var hd = dCrt('h5').html(msg).addClass('letterpress');
                    c.append(hd);
                    return c;
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
                        data = methods.html.applyFilter(fltr, fltr2);
                        if(rc) {
                            methods.html.subChart(fltr, mc, rc, data);
                        }
                    }
                    state.opts.tblNode.pGridAdhoc(methods.options(data));
                },
                applyFilter: function (fltr, fltr2, unique) {
                    var ids = [];
                    $.each(state.opts.data._subs, function (k, v) {
                        var sub = v;
                        if ($.jCommon.string.equals(sub.noticeId, fltr, true)) {
                            if (fltr2) {
                                if ($.jCommon.string.equals(sub.securityCenter, fltr2, true)) {
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
                                if(unique){
                                    return false;
                                }
                            }
                        });
                    });
                    var len = items.length;
                    return {
                        hits: len,
                        next: len,
                        start: 0,
                        _subs: $.extend({}, state.opts.data._subs, true),
                        results: items
                    };
                },
                chart: function (node, data) {
                    var working = {};
                    var hits = state.opts.current.max;
                    $.each(data._subs, function (k,v) {
                        var item = v;
                        var lid = k.toString();

                        var r = $.jCommon.colors.toHex(item._color_iavm.r);
                        var g = $.jCommon.colors.toHex(item._color_iavm.g);
                        var b = $.jCommon.colors.toHex(item._color_iavm.b);
                        item._color1 = '#'+r+g+b;
                        item._hvrColor1 = $.jCommon.colors.lighten(item._color1, 10);

                        var key = $.jCommon.string.makeKey(item.noticeId);
                        var val = working[key];
                        if(!val){
                            val={
                                label: item.noticeId,
                                folded: item.noticeId.toLowerCase(),
                                value: 0,
                                count: 0,
                                ordinal: item.ordinal,
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
                                        var d = dCrt('span').addClass('active-link').attr('title', 'click once to apply filter click again to remove filter')
                                            .html(String.format('{0} ({1}): {2}', item.perc, item.value, item.label))
                                            .attr('title', String.format('Each asset may have multiple {0} but only one per asset is represented.', item.label));

                                        d.on('click', function () {
                                            if(d.hasClass('active')){
                                                d.removeClass('active');
                                                methods.html.filter('***', null, mc, rc, item);
                                            }
                                            else {
                                                mc.find('.media-body .active').removeClass('active');
                                                d.addClass('active');
                                                state.opts.legend = item.legend.data.noticeId;
                                                methods.html.filter(item.legend.data.noticeId, null, mc, rc, item);
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
                            working[key] = val;
                            var data = methods.html.applyFilter(item.noticeId);
                            val.value = data.hits;
                            val.perc = $.jCommon.number.percentage(hits, data.hits, true);
                        }
                    });

                    var options = [];
                    $.each(working, function (key, value) {
                        if(value.value>0) {
                            options.push(value);
                        }
                    });

                    options = $.jCommon.array.sort(options, [{property: 'ordinal', asc: true}]);
                    console.log(options);
                    function onClick(e, chart, ap) {
                        var lbl = ap[0]._view.label;
                    }
                    var len = options.length;
                    var db = (len>6) ? 4 : 12;
                    var title = ($.jCommon.string.empty(state.opts.title) || $.jCommon.string.startsWith(state.opts.title, "auto save", true)) ? "Custom" : state.opts.title;
                    var d = dCrt('div').css({'position': 'relative', top: '10px'}).html(String.format("IAVM Report: {0} ({1}) Assets ({2}) IAVMs", title, data.results.length, len));
                    node.jChartIt({sorted: false, max: state.opts.current.max, glyph: 'glyphicons-stats', title: d, dividedBy: db, width: 350, height: 175, onClick: onClick, data: options, type: 'bar', legend: 'IAVMs Summation'});
                },
                subChart: function (fltr, mc, rc, data) {
                    var working = {};
                    var hits = data.results.length;
                    var subs = [];
                    $.each(data._subs, function (k,v) {
                        var item = v;
                        if(!$.jCommon.string.equals(fltr, item.noticeId, true)){
                            return true;
                        }
                        subs.push(item);
                        var lid = k.toString();

                        var r = $.jCommon.colors.toHex(item._color.r);
                        var g = $.jCommon.colors.toHex(item._color.g);
                        var b = $.jCommon.colors.toHex(item._color.b);
                        item._color2 = String.format('#{0}{1}{2}', r, g, b);
                        item._hvrColor2 = $.jCommon.colors.lighten(item._color2, 10);
                        var key = $.jCommon.string.makeKey(item.securityCenter);
                        var val = working[key];
                        if(!val){
                            val={
                                label: item.securityCenter,
                                folded: item.securityCenter.toLowerCase(),
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
                                        var d = dCrt('span').addClass('active-link').attr('title', 'click once to apply filter click again to remove filter').html(String.format('{0} ({1}): {2}', item.perc, item.value, item.legend.data.securityCenter)).attr('title', item.legend.data.noticeId);
                                        var glyph = dCrt('span').attr('title', 'open software in new tab').addClass("glyphicons glyphicons-new-window external-url");
                                        var link = dLink("", item.legend.data.relatedId).append(glyph);

                                        d.on('click', function () {
                                            if(d.hasClass('active')){
                                                methods.html.filter(fltr, null, mc, rc, item);
                                            }
                                            else {
                                                mc.find('.media-body .active').removeClass('active');
                                                d.addClass('active');
                                                methods.html.filter(state.opts.legend, item.legend.data.securityCenter, mc, rc, item);
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
                            working[key] = val;
                            $.each(data.results, function () {
                                $.each(this._subs, function () {
                                    var a = this.toString();
                                    if($.jCommon.string.equals(a, lid, true)){
                                        val.value+=1;
                                        return false;
                                    }
                                });
                            });
                        }

                    });

                    var options = [];
                    var t = 0;
                    $.each(working, function (key, value) {
                        if(value.value>0) {
                            t+=value.value;
                            options.push(value);
                        }
                    });

                    $.each(working, function (key, value) {
                        if(value.value>0) {
                            value.perc = $.jCommon.number.percentage(t, value.value, true);
                        }
                    });
                    options = $.jCommon.array.sort(options, [{property: 'folded', asc: true}]);
                    function onClick(e, chart, ap) {
                        var lbl = ap[0]._view.label;
                    }
                    var lgnd = dCrt('span');
                    var s1 = dCrt('span').html(String.format('Security Centers: {0}', fltr));
                    var s2 = dCrt('span').html(t).attr('title', 'Enumerated');
                    var s3 = dCrt('span').html(String.format('[{0}]', options.length)).attr('title', 'Unique');
                    lgnd.append(s1).append(s2).append(s3).children().css({paddingRight: '5px'});
                    rc.jChartIt({glyph: 'glyphicons-stats', title: null, height: 175, onClick: onClick, data: options, limit: 0, type: 'pie', maintainAspectRatio: false, chartAndLegend: true, legendOverride: true, legend: lgnd});
                }
            }
        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.adHocIavm.defaults = {};


    //Plugin Function
    $.fn.adHocIavm = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.adHocIavm($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $adHocIavm = $(this).data('adHocIavm');
            switch (method) {
                case 'exists': return (null!==$adHocIavm && undefined!==$adHocIavm && $adHocIavm.length>0);
                case 'state':
                default: return $adHocIavm;
            }
        }
    };

})(jQuery);
