;(function ($) {
    //Object Instance
    $.jAssets = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.jAssets.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.devOnly = false;
        var _sub = {};
        var _subKey = 'lid';
        state.linkStatus = {};

        // Store a reference to the environment object
        el.data("jAssets", state);
        // Private environment methods
        methods = {
            init: function() {
                state.opts.treed=false;
                function resize(){
                    $('.page-content').css({padding: '60px 0 0 0', overflow: 'hidden'});
                    lusidity.resizePage(65);
                }
                resize();
                lusidity.environment('onResize', resize);
                state.css({overflow: 'hidden'});
                var href = window.location.href.toString();
                state.opts.url = $.jCommon.url.create(href);
                if(state.opts.url.hasParam('treed')){
                    state.opts.treed = true;
                }
                var psd = state.opts.url.getParameter('passed');
                state.opts.passed = $.jCommon.string.equals(psd, 'true');
                state.opts.type = state.opts.passed ? 'passed' : 'failed';
                state.opts.iavmUrl = state.opts.url.getParameter('id');

                function start() {
                    state.isAsset = $.jCommon.string.startsWith(state.opts.data['vertexType'], '/electronic/network/asset');
                    state.isOrg = $.jCommon.string.contains(state.opts.data[state.KEY_ID], 'organization');
                    state.isLoc = $.jCommon.string.contains(state.opts.data[state.KEY_ID], 'location');
                    if(!state.opts.et_view){
                        state.opts.et_view = state.isOrg ? 'managed' : state.isLoc ? 'location' : 'ditpr';
                    }
                    methods.html.init();
                }
                var d = state.opts.url.getParameter('d');
                if(d) {
                    //parsing string from url
                    d = $.jCommon.string.getFirst(d, '#');
                    state.opts.data = $.jCommon.storage.getItem(d);
                    if(!state.opts.treed && (state.opts.data && state.opts.data.treed)){
                        state.opts.treed = true;
                    }
                    state.opts.grouped = (state.opts.data && undefined!==state.opts.data.grp);
                    var s = function (data) {
                        if(data._response_code){
                            var msg = btoa("<strong>Unauthorized:</strong> You do not have sufficient permissions to view the page requested.");
                            window.location = "/notification?status="+data._response_code + '&msg='+msg;
                        }
                        if(data){
                            state.current ={ item: data};
                            start();
                        }
                    };
                    var f = function (data) {};
                    var url = String.format('{0}/hierarchy/details?detail={1}&view={2}&exact={3}', state.opts.data[state.KEY_ID], 'vuln', state.opts.data.et_view, state.opts.data.et_exact);
                    $.htmlEngine.request(url, s, f, state.opts.data.filters, 'post', false);
                }
                else{ start();}
                state.modalInfo = dCrt('div').css({position: 'absolute', top: '200px', width: '0', height: '0'});
                lusidity.append(state.modalInfo);
            },
            linkClick: function(item, view){
                var u = methods.authUrl(item, view);
                var s = function (data) {
                    if (!data || !data.authorized) {
                        console.log("unauthorized");
                        return false;
                    }
                };
                $.htmlEngine.request(u, s, s, null, "get", true);
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
            url: function (data, start, limit) {
                return String.format('{0}/hierarchy/details?detail={1}&view={2}&filter={3}&exact={4}&start={5}&limit={6}',  data[state.KEY_ID], data.detail, data.et_view, data.filter, data.et_exact, start, limit);
            },
            authUrl: function (data, view) {
                return String.format('{0}/hierarchy/details?detail={1}&view={2}&exact={3}&_nocache={4}', data, 'auth', view, false, $.jCommon.getRandomId('c'));
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
            options: function (item) {
                function filter(n, f){
                    if(f === item.filter){
                        n.addClass('highlight').css({fontWeight: 'bold'});
                    }
                }
                var sorted =false;
                var init=false;
                var numCss = {minWidth: '70px', width: '70px', maxWidth: '70px', textAlign: 'right'};
                var w = $(window).width();
                function total(node, item, filters){
                    state.opts.data.filters = null;
                    state.opts.data.groups = null;
                    var qry = jAssetFactory.totals(state.opts.data, item);
                    qry.filters = filters;
                    if(hasFilters(qry.filters)) {
                        $.each(qry.filters.must, function (k, v) {
                            if (k === 'title') {
                                qry.filters.must.other = v;
                                delete qry.filters.must.title;
                                return false;
                            }
                        });
                    }
                    var s = function (data) {
                        node.children().remove();
                        if(data){
                            // changed
                            var r = dCrt('div').css({position: 'relative', top: "-2px"});
                            var enmr = $.jCommon.number.commas(data.enumerated);
                            var en = dCrt('div').attr('title', "Enumerated").html(enmr).attr('title', 'Enumerated: ' + enmr).css({overflow: 'hidden', whiteSpace: 'nowrap'});
                            var uniq = $.jCommon.number.commas(data.unique);
                            var un = dCrt('div').attr('title', 'Unique').css({overflow: 'hidden', whiteSpace: 'nowrap', position: 'relative', top: "-2px"}).html(String.format('({0})', uniq)).attr('title', 'Unique: ' + uniq);
                            r.append(en).append(un);
                            node.append(r);
                        }
                    };
                    $.htmlEngine.request(qry.url, s, s, qry, 'post');
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
                    data: state.opts.data,
                    title: "Assets",
                    titleHdr: state.titleHdr,
                    treed: state.opts.treed,
                    disableGrpAt: 1500,
                    expandGrpAt: 1500,
                    maxGroups: 2,
                    onTreeNodeClicked: function (e) {
                        e.node.children().remove();
                        var nd = dCrt('div');
                        e.node.append(nd);
                        $.each(e.item.groups, function () {
                            var t;
                            switch (this.key) {
                                case 'enclave':
                                case 'ditpr':
                                    t = "System Name";
                                    break;
                                case 'managed':
                                    t = "Managed By";
                                    break;
                                case 'location':
                                    t = 'Location';
                                    break;
                                case 'os':
                                    t = 'Operating System';
                                    break;
                                case 'compliant':
                                    t = 'Compliant';
                                    break;
                                case 'owned':
                                    t = "Owned By";
                                    break;
                                case 'asset':
                                    t = "Asset";
                                    break;
                                default:
                                    t = "Unknown";
                                    break;
                            }
                            this.label=t;
                        });
                        nd.pSummary({card: {offsetWidth: 10}, "export": true, filters: state.opts.data.filters, groups: e.item.groups, data: state.opts.data, dashboard: false, et_view: state.opts.data.et_view, treed: state.opts.treed});
                    },
                    onVisible: function (e) {
                        // {groups: grps, item: item, li: li, hdr: hdr}
                        var li = e.node ? e.node :  $('#'+e.item.id);
                        var item = e.item;
                        var counted = li.attr('counted');
                        if(counted){
                            return true;
                        }
                        function set(n) {
                            if(n<=0){
                                if(e.leaf){
                                    e.leaf.remove();
                                }
                                else {
                                    li.hide();
                                }
                            }
                            else {
                                var hdr = e.node ? e.node : item.hdr;
                                if(e.spring){
                                    hdr.append(dCrt('div').html("Found: " + $.jCommon.number.commas(n)));
                                }
                                else {
                                    hdr.prepend(dCrt('span').html(n + ':').css({marginRight: '5px'}));
                                }
                            }
                        }
                        if(item.item && $.jCommon.is.numeric(item.item.count)){
                            set(item.item.count);
                        }
                        else {
                            var pd = item.parentData;
                            var url = String.format('{0}/hierarchy/details?detail={1}&view={2}&exact={3}&_nocache={4}', pd[state.KEY_ID], "count", pd.et_view, pd.et_exact, $.jCommon.getRandomId('c'));
                            var s = function (data) {
                                if (data && data.count) {
                                    var n = data.count.exact < data.count.inherited ? data.count.inherited : data.count.exact;
                                    set(n);
                                }
                                else {
                                    li.hide();
                                }
                            };
                            var d = e.item.groups ? e.item.groups : state.opts.groups;
                            $.htmlEngine.request(url, s, s, d, 'post', true, 60000);
                        }
                    },
                    showFoundOnly: true,
                    group: {
                        limit: 0,
                        treed: false,
                        enabled: true,
                        store: 'technology_security_vulnerabilities_vulnerability_details',
                        partition: 'technology_security_vulnerabilities_vulnerability_details',
                        exclusions: state.opts.data.groups,
                        filters: state.opts.data.filters,
                        groups: [
                            {
                                label: 'HBSS Compliancy',
                                key: 'compliant',
                                fKey: 'compliant',
                                fValKey: 'value',
                                onGroup: function (item, value) {
                                    var c = $.htmlEngine.compliant({compliant: item.value});
                                    // label, clr, tip
                                    return c.label;
                                }
                            },
                            {
                                label: 'Location',
                                key: 'location',
                                fKey: 'locationId',
                                fValKey: 'url',
                                urlKey: 'locationId'
                            },
                            {
                                label: 'Managed By',
                                key: 'managed',
                                fKey: 'managedId',
                                fValKey: 'url',
                                urlKey: 'managedId'
                            },
                            {
                                label: 'Operating System',
                                key: 'os',
                                fKey: 'osId',
                                fValKey: 'url',
                                urlKey: 'osId'
                            },
                            {
                                label: 'Owned By',
                                key: 'owned',
                                fKey: 'ownedId',
                                fValKey: 'url',
                                urlKey: 'ownedId'
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
                                    title: "Export Summary",
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
                                    clicked: function(data){
                                        var key = state.opts.data[state.KEY_ID];
                                        var detail =  "csv";
                                        var view = state.opts.data.et_view;
                                        var filter = "all";
                                        var exact = state.opts.data.et_exact;
                                        var url = lusidity.environment('host-download') + key +"/hierarchy/details?detail="+detail +"&view="+view+"&filter="+filter+"&exact="+exact+"&start=0&limit=0";
                                        var s = function (nData) {
                                            if (nData) {
                                                var u = methods.getDownloadUrl(nData.url);
                                                window.location.replace(u, '_blank');
                                            }
                                        };
                                        var f = function () {

                                        };
                                        var groups = state.opts.data.groups ? $.jCommon.array.clone(state.opts.data.groups) : [];
                                        if(state.opts.groups){
                                            $.jCommon.array.addAll(groups, $.jCommon.array.clone(state.opts.groups));
                                        }
                                        $.htmlEngine.request(url, s, f, groups, 'post');
                                    }
                                },
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
                                    clicked: function(data){
                                        var key = state.opts.data[state.KEY_ID];
                                        var detail =  "csv";
                                        var view = state.opts.data.et_view;
                                        var filter = "all_details";
                                        var exact = state.opts.data.et_exact;
                                        var url = lusidity.environment('host-download') + key +"/hierarchy/details?detail="+detail +"&view="+view+"&filter="+filter+"&exact="+exact+"&start=0&limit=0";
                                        var s = function (nData) {
                                            if (nData) {
                                                var u = methods.getDownloadUrl(nData.url);
                                                window.location.replace(u, '_blank');
                                            }
                                        };
                                        var f = function () {

                                        };
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
                    offset:{
                        parent: 0,
                        header: 0,
                        body: 0
                    },
                    grid: {
                        colResizable: false,
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
                            enabled: true,
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
                                var sp = e.row.find('.glyphicon');
                                sp.removeClass('glyphicon-triangle-bottom').addClass('glyphicon-triangle-top');
                                sp.parent().addClass('expanded');
                                trc.show();
                                trc.attr('data-index', e._idx);
                                // changed commented out and added
                                //e.rows.push(trc);
                                e.body.append(trc);
                                var st = trc.find('.sub-table.grid-sub');
                                if(st.pGrid('exists')) {
                                    st.pGrid('resize');
                                }
                            }
                        },
                        onFiltered: function (key, value) {

                        },
                        onBefore: function (items, node) {
                            var r = items;
                            $.each(r, function () {
                                var p = this.packedVulnerabilityMatrix;
                                this.exploit = parseInt(p===9999 ? 0 : p.toString().substring(0, 4));
                                this.findings = (this.catI + this.catII + this.catIII + this.critical + this.high + this.medium + this.low+ (this.unknown ? this.unknown : 0));
                                var ct = $.htmlEngine.compliant(this);
                                this.compliant = ct.label;
                            });
                            return r;
                        },
                        onAfter: function (items, header, content) {
                            var n = items.length;
                            state.pnl.jFilterBar('updateCount', {on: n, total: n });
                        },
                        mapping: [
                            {header: {title: "#", css: {minWidth: '50px', width: '50px', maxWidth: '50px'}}, property: '#', type: 'integer', css: {minWidth: '50px', width: '50px', maxWidth: '50px'}},
                            {header: {title: "RS", tip: "Risk Score", css: {minWidth: '30px', width: '30px'}}, property: '', type: 'integer', callback: function (td, item, value, map, filters) {
                                if($.jCommon.json.hasProperty(item, 'metrics.html.cls')) {
                                    td.addClass(item.metrics.html.cls).attr('title', item.metrics.html.label + ': ' + item.packedVulnerabilityMatrix);
                                }
                            }},
                            {header: { title: "Asset", property: 'title', sortable: true, sortType: 'string', sortTipMap: {asc: 'asc', desc: 'desc', 'none': 'Risk Score'}, css: {minWidth: '132px'}}, property: 'title', type: 'string', callback: function (td, item, value, map, filters) {
                                if(!init && item.asOf) {
                                    init=true;
                                    state.asOf.html(String.format("As of {0}", $.jCommon.dateTime.defaultFormat(item.asOf)));
                                }
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
                    }
                };
                if(!state.isAsset){
                    r.grid.mapping = $.jCommon.array.insertAt(r.grid.mapping, {
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

                                    // changed from td.parent() to td.parent().parent();
                                    var r = td.parent().parent();

                                    td.addClass('expanded').attr('init', 'init');
                                    var idx = r.index() + 1;

                                    // changed from div to tr;
                                    trc = dCrt("tr").addClass("tSubRow").attr('trc-grouped', grid.opts.grouped);
                                    _sub[item[_subKey]] = trc;
                                    var lnk = td.closest('.tRow');
                                    var lnkId = lnk.attr('data-index');
                                    trc.attr('data-index', lnkId).insertAfter(lnk);

                                    // changed from div to td;
                                    var tdc = dCrt('td').attr('colspan', r.children().length);

                                    trc.append(tdc);
                                    var c = dCrt('div');

                                    // changed removed .css({padding: '0 10px 0 30px'});
                                    tdc.append(c);

                                    var h = 300;

                                    // changed commented out
                                    //var w = c.availWidth();
                                    //dWidth(c, w);

                                    var sp = $.htmlEngine.getSpinner();
                                    td.glyph.hide();
                                    sp.insertBefore(td.glyph);
                                    c.on('table-view-loaded', function (e) {
                                        // changed
                                        if(!e.opts.sub_resized) {
                                            window.setTimeout(function () {
                                                sp.remove();
                                                td.glyph.show();
                                                // changed added
                                                var th = e.table.height();
                                                var ch = e.table.find('table').children().height();
                                                if (ch < th) {
                                                    e.table.height(ch + 5);
                                                }
                                                e.opts.sub_resized = true;
                                            }, 300);
                                        }
                                    });
                                    var pd = $.extend({}, state.opts.data);
                                    pd.title = item.other ? item.other : item.title;
                                    pd['/vertex/uri'] = item.relatedUri;
                                    var grouped = $('.group-item').length > 0;
                                    var sb = c.pVulnerabilities({
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
                                    // changed;
                                    trc.insertAfter(r);
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
                    r.group.groups.push({
                        label: 'System Name',
                        key: 'ditpr',
                        fKey: 'ditprId',
                        fValKey: 'url',
                        urlKey: 'ditprId'
                    });
                    r.grid.mapping = $.jCommon.array.insertAt(r.grid.mapping,
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
                    r.grid.mapping = $.jCommon.array.insertAt(r.grid.mapping,
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
                    {header: { title: "CI", tip: "Cat I", property: 'catI', css: numCss, sortable: true, defaultDir: 'desc'},
                        css: numCss,data: 'catI', property: 'catI', type: 'string', callback: function (td, item, value, map, filters) {
                        td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'catII', item, filters)));
                    }},
                    {header: { title: "CII", tip: "Cat II", property: 'catII', css: numCss,sortable: true, defaultDir: 'desc'}, css: numCss,data: 'catII', property: 'catII', type: 'string', callback: function (td, item, value, map, filters) { td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'catII', item, filters)));}},
                    {header: { title: "CIII", tip: "Cat III", property: 'catIII', css: numCss,sortable: true, defaultDir: 'desc'}, css: numCss,data: 'catIII', property: 'catIII', type: 'string', callback: function (td, item, value, map, filters) { td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'catIII', item, filters)));}},
                    {header: { title: "C", tip:"Critical", property: 'critical', css: numCss,sortable: true, defaultDir: 'desc'}, css: numCss,data: 'critical', property: 'critical', type: 'string', callback: function (td, item, value, map, filters) { td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'critical', item, filters)));}},
                    {header: { title: "H", tip: "High", property: 'high', css: numCss,sortable: true, defaultDir: 'desc'}, css: numCss,data: 'high', property: 'high', type: 'string', callback: function (td, item, value, map, filters) { td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'high', item, filters)));}},
                    {header: { title: "M", tip: "Medium", property: 'medium', css: numCss,sortable: true, defaultDir: 'desc'}, css: numCss,data: 'medium', property: 'medium', type: 'string', callback: function (td, item, value, map, filters) {td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'medium', item, filters)));}},
                    {header: { title: "L", tip: "Low", property: 'low', css: numCss,sortable: true, defaultDir: 'desc'}, css: numCss,data: 'low', property: 'low', type: 'string', callback: function (td, item, value, map, filters) { td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'low', item, filters)));}},
                    {header: { title: "I", tip: "Info", property: 'unknown', css: numCss,sortable: true, defaultDir: 'desc'}, css: numCss,data: 'unknown', property: 'unknown', type: 'string', callback: function (td, item, value, map, filters) { td.html(($.jCommon.is.numeric(value) ? value : getValues(td, 'info', item, filters)));}}
                ];
                var svrts=['catI','catII','catIII','critical','high','medium','low','unknown'];
                $.each(ext, function () {
                    if(!$.jCommon.string.contains(item.filter, svrts)){
                        r.grid.mapping.push(this);
                    }
                    else if($.jCommon.string.equals(item.filter, this.property)){
                        r.grid.mapping.push(this);
                    }
                });

                return r;
            },
            formCancel: function () {
                function cancel(node) {
                    if (node && node.formBuilder('exists')) {
                        node.formBuilder('cancel');
                    }
                }
                cancel(state.opts.addNode);
                cancel(state.opts.editNode);
            },
            form: function (container, title, data, show) {
                container.children().remove();
                var mode = !data ? 'add' : 'edit';
                var defaultData = {
                    edgeKey: '/electronic/base_infrastructure/infrastructures',
                    edgeDirection: 'out'
                };
                container.formBuilder({
                    title: title,
                    borders: false,
                    css: {'margin-right': '0'},
                    panelCss: {margin: '10px'},
                    glyph: 'glyphicons glyphicons-file',
                    url: null,
                    actions: [],
                    show: false,
                    data: data,
                    mode: mode,
                    isDeletable: function () {
                        if (mode !== "add") {
                            return !methods.isExcluded(state.current.item.title);
                        }
                        return false;
                    },
                    deleteMessage: function (body, data) {
                        var title = data.title;
                        var msg = dCrt('div').css({verticalAlign: 'middle', height: '32px'});
                        var question = dCrt('div').html('Click Delete to delete "<strong>' + title + '</strong>" and all of its linked Enclaves.');
                        msg.append(question);
                        var statement = dCrt('p').html(
                            'Once Deleted, there is no way to recover "<strong>' + title + '</strong>".  Furthermore, any enclave linked to this one will be deleted if they are not linked to any other Enclave or Network Device.'
                        );
                        body.append(statement).append(msg);
                    },
                    onDelete: function (item) {
                        var s = function (data) {
                            if (!data.error) {
                                state.current.item = state.current.last;
                                state.opts.treeNode.treeView('remove', {current: item, next: state.current.item});
                            }
                            else {
                                lusidity.info.red(data.error);
                                lusidity.show(5);
                            }
                        };
                        var f = function (data) {};
                        $.htmlEngine.request(item[state.KEY_ID], s, f, null, 'delete');
                    },
                    close: function (node) {
                        try {
                            state.opts.pnlMiddleNode.scrollTop(0)
                        } catch (e) {
                        }
                        state.opts.pnlMiddleNode.css({overflowY: 'hidden'});
                        if (state.opts.editNode) {
                            state.opts.editNode.hide();
                        }
                        if (state.opts.addNode) {
                            state.opts.addNode.hide();
                        }
                        if (state.opts.viewerNode) {
                            state.opts.viewerNode.show();
                        }
                    },
                    display: function (node) {
                        state.opts.pnlMiddleNode.css({overflowY: 'auto'});
                        if (state.opts.editNode) {
                            state.opts.editNode.hide();
                        }
                        if (state.opts.addNode) {
                            state.opts.addNode.hide();
                        }
                        if (state.opts.viewerNode) {
                            state.opts.viewerNode.hide();
                        }
                        node.show();
                    },
                    defaultData: defaultData,
                    before: function () {},
                    formError: function (msg) {
                        lusidity.info.red(msg);
                        lusidity.info.show(5);
                    },
                    onSuccess: function (data) {
                        container.show();
                        if (data.item.failed) {
                            lusidity.info.red(data.item.error ? data.item.error : 'Sorry, something went wrong, failed to ' + data.editing ? 'modify' : 'create' + ' the enclave.');
                            lusidity.info.show(5);
                        }
                        else {
                            var item = methods.getData(data.item);
                            methods.html.formCancel();
                            if (data.editing) {
                                if (data.item && data.item.result) {
                                    state.current.last = state.current.item;
                                    state.current.item = item;
                                    var title = item.title;
                                    if (state.current.edgeNode && state.current.edgeNode.root) {
                                        state.current.edgeNode.root.el.name = title;
                                        state.current.edgeNode.root.el.title.html(title);
                                    }
                                    if (methods.getTreeNode()) {
                                        methods.getTreeNode().title.html(title);
                                        methods.getTreeNode().data('item', item);
                                    }
                                    methods.html.content.init(false);
                                    methods.html.summary.init();
                                    methods.html.menu();
                                }
                            }
                            else {
                                if (state.opts.treeNode) {
                                    methods.html.treeView.add(item);
                                }
                                if (!state.current.edgeNode) {
                                    state.current.edgeNode = {};
                                }
                                if (!state.current.edgeNode.devices) {
                                    state.current.edgeNode.devices = {};
                                }
                                if (!state.current.edgeNode.devices.results) {
                                    state.current.edgeNode.devices.results = [];
                                }
                                state.current.edgeNode.devices.results.push(item);
                                methods.html.content.init(false);
                            }
                        }
                    },
                    onFailed: function () {
                        container.show();
                        lusidity.info.red('Sorry, something went wrong, failed to create the enclave.');
                        lusidity.info.show(5);
                    },
                    nodes: [
                        {
                            node: 'input',
                            type: 'text',
                            'required': true,
                            readOnly: false,
                            id: 'title',
                            label: "Title",
                            placeholder: 'Enter a friendly name.'
                        },
                        {
                            node: 'input',
                            type: 'text',
                            readOnly: true,
                            'required': true,
                            id: 'ditprId',
                            label: "DITPR ID",
                            placeholder: 'Enter the DITPR Identity.',
                            onAvailable: function (node) {
                                var v = node.val();
                                if(!v){
                                    v = state.current.item.ditprId;
                                    if(!v){
                                        v='rmk';
                                    }
                                    v=$.jCommon.getRandomId(v);
                                    node.val(v);
                                }
                            }
                        },
                        {
                            node: 'input',
                            type: 'number',
                            'required': true,
                            id: 'terrainScore',
                            label: "Cyber Terrain Score",
                            focussed: true,
                            "default": 1,
                            max: 10,
                            min: 1,
                            authorized: ['admin']
                        },
                        {
                            node: 'textarea',
                            'required': false,
                            id: '/system/primitives/raw_string/descriptions',
                            map: {
                                direction: 'out',
                                key: 'value',
                                vertexType: '/system/primitives/raw_string'
                            },
                            label: "Description",
                            css: {width: '100%', height: '100px'}
                        }
                    ],
                    getUrl: function () {
                        return methods.getUri(state.current.item) + '/infrastructure?mode=' + mode;
                    }
                });
                if($.jCommon.string.equals(mode, 'add')){
                    var ditpr = container.find('#ditprId');
                    var rn = $.jCommon.getRandomNumber();
                    defaultData.ditprId = state.current.item && state.current.item.ditprId ?
                        state.current.item.ditprId + rn: rn;
                    ditpr.val(defaultData.ditprId);
                }
            },
            html:{
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
                    var span = dCrt('span').html(String.format('Asset Details {0}{1} ', (state.opts.data.et_exact ? 'Directly ':''), (et==='owned' ? 'Owned By' : et==='managed' ? 'Managed By' : et==='location' ? 'Located In' : 'for System')));
                    var link = dCrt('a').attr('target', '_blank').attr('href', String.format('{0}?et_view={1}&et_exact={2}', state.opts.data[state.KEY_ID], et, state.opts.data.et_exact)).html(String.format('{0}{1}', (state.opts.data.ditprId ? String.format('{0} ', state.opts.data.ditprId) : '') , state.opts.data.title));
                    var t = dCrt('div').append(span).append(link);
                    state.titleHdr.append(t);
                    var ft;
                    switch (state.opts.data.filter){
                        case 'passed':
                            ft = "Vulnerabilities Passed";
                            break;
                        case 'failed':
                            ft = "Vulnerabilities Failed";
                            break;
                        case 'iavm_passed':
                            ft = "IAVMs Passed";
                            break;
                        case 'iavm_failed':
                            ft = "IAVMs Failed";
                            break;
                        case 'catI':
                            ft='CAT I';
                            break;
                        case 'catII':
                            ft='CAT II';
                            break;
                        case 'catIII':
                            ft='CAT III';
                            break;
                        default:
                            ft = $.jCommon.string.replaceAll(state.opts.data.filter, "_", " ");
                            ft = $.jCommon.string.toTitleCase(ft);
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
                    state.asOf = dCrt('div');
                    state.titleHdr.append(state.asOf);
                    if(state.opts.data.cai){
                        var node = dCrt('div');
                        state.titleHdr.append(node);
                        if(state.opts.data.cai>state.opts.data.ci){
                            node.append(dCrt('div').html(String.format("Working with {0} Scoped Assets of {1} found", $.jCommon.number.commas(state.opts.data.ci), $.jCommon.number.commas(state.opts.data.cai))));
                        }
                        else{
                            node.append(dCrt('div').html(String.format("{0} Assets found", $.jCommon.number.commas(state.opts.data.cai))));
                        }
                    }
                    document.title = String.format('{0} - Assets', state.opts.data.title);
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
    $.jAssets.defaults = {};


    //Plugin Function
    $.fn.jAssets = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.jAssets($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $jEnclavesSeverity = $(this).data('jAssets');
            switch (method) {
                case 'exists': return (null!==$jEnclavesSeverity && undefined!==$jEnclavesSeverity && $jEnclavesSeverity.length>0);
                case 'state':
                default: return $jEnclavesSeverity;
            }
        }
    };

})(jQuery);
