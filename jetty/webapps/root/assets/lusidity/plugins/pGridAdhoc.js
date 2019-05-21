

;(function ($) {

    //Object Instance
    $.pGridAdhoc = function (el, options) {
        var state = el,
            methods = {};
        state.container = $(el);
        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data};
        state.opts = $.extend({}, $.pGridAdhoc.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.grid = {
            height: state.opts.height,
            minHeight: state.opts.minHeight,
            maxHeight: state.opts.maxHeight
        };

        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.page = {
            idx: 0,
            up: false,
            next: 0,
            suggest: [],
            terms: [],
            data: [],
            on: 0,
            start: 0,
            hits: 0,
            top: 0,
            gap: 0,
            bfr: state.opts.bfr,
            st: 0,
            isSub: (state.opts.sub ? true : false),
            callback: {
                header: []
            }
        };
        state.bfrdRows = [];
        state._disabled = false;
        state.sort = ((state.opts.sort && state.opts.sort.properties) ? state.opts.sort.properties : []);
        state.musts = {};
        state.shoulds = {};
        state.rh = state.opts.rowHeight ? state.opts.rowHeight : state.opts.schema.content.table.row.height;
        state.opts.schema.content.table.row.height = state.rh;
        state.scrollDrag = false;
        state.requestOn = 0;
        state.colSizing = false;
        state.scrollerDisabled = false;
        state.resizeDisabled = false;
        state.resizing = false;
        state.loading = true;
        state.vars = {st: 0, idx: 0, bfrIdx:0, diff: 0, tsr: 0};
        state._init = true;
        // Store a reference to the environment object
        el.data("pGridAdhoc", state);

        // Private environment methods
        methods = {
            init: function () {
                if(state.opts.data && state.opts.data.results){
                    state.page.data = state.opts.data.results;
                }
                state.opts.limit = !state.opts.paging.enabled ? state.opts.limit : state.page.bfr*3;
                state.page.next = state.opts.limit;
                window.setTimeout(function () {
                    methods.start();
                }, 500);
                lusidity.environment('onResize', function () {
                    methods.reset(true);
                    methods.start();
                });
            },
            start: function () {
                state.opts.init = false;
                state.loading = true;
                var len = state.opts.mapping.length;
                var w = state.width();
                state.opts.cellMaxWidth = w / len;
                state.opts.offset.parent = state.opts.offset.parent ? state.opts.offset.parent : 0;
                state.opts.offset.table = state.opts.offset.table ? state.opts.offset.table : 0;

                if (!state.node) {
                    state.node = dCrt('div').addClass('tNode').css({position: 'relative', clear: 'both'});
                    if (state.page.isSub) {
                        state.addClass('grid-sub');
                        state.node.addClass('grid-sub-view');
                    }
                }
                state.append(state.node);
                if (state.opts.showBusy) {
                    $.htmlEngine.busy(state.node, {
                        type: 'cube',
                        cover: true,
                        adjustWidth: 0,
                        adjustHeight: 0
                    });
                }

                state.contentNode = dCrt('div').addClass('tContent');

                state.headerNode = dCrt('div').addClass('tHeader').css({
                    paddingRight: '18px',
                    position: 'relative',
                    clear: 'both'
                });
                state.searchNode = dCrt('div').addClass('tSearch').css({position: 'relative', clear: 'both'});
                state.tableNode = dCrt('div').addClass('tTable').css({
                    position: 'relative',
                    clear: 'both'
                }).append(state.contentNode);

                if (state.opts.grouped) {
                    state.node.css({paddingRight: '18px'});
                }

                state.node.append(state.searchNode).append(state.headerNode).append(state.tableNode);

                if (state.opts.item && state.opts.getUrl && $.isFunction(state.opts.getUrl)) {
                    this.get(false);
                }

                methods.resize.all(false);
                methods.header.init();
                methods.filter.init();
                methods.search.init();

                methods.content.init();
                methods.resize.cells();
                methods.resize.all(true);
                var sh = state.page.top;
                if(sh>0){
                    state.tableNode.scrollTop(sh);
                }
                state._init=false;
            },
            ext: function (musts, shoulds) {
                function ext(items, result) {
                    $.each(items, function () {
                        var item = this;
                        var add = true;
                        $.each(result, function () {
                            if ($.jCommon.string.equals(this.key, item.fKey) && $.jCommon.string.equals(this.value, item.value, true)) {
                                add = false;
                                return false;
                            }
                        });
                        if (add) {
                            var v = item.fValKey ? item[item.fValKey] : item.value;
                            var k = item.fKey ? item.fKey : item.key;
                            result[item.fKey] = {term: v.toLowerCase()}
                        }
                    });
                }
                var msts = $.extend({}, state.musts);
                var shlds = $.extend({}, state.shoulds);
                if (state.opts.musts) {
                    ext(state.opts.musts, msts);
                }
                if (state.opts.shoulds) {
                    ext(state.opts.shoulds, shlds);
                }
                return {musts: msts, shoulds: shlds};
            },
            inject: function (idx) {
            },
            getUrl: function (start, limit, url) {
                return (url ? url : '/query') + '?pu=true&start=' + start + '&limit=' + limit;
            },
            getQuery: function () {
                var qry = $.extend({}, state.opts.getQuery());
                function cmn() {
                    if (!qry["native"].query) {
                        qry["native"].query = {}
                    }
                    if (!qry["native"].query.bool) {
                        qry["native"].query.bool = {};
                    }
                }
                var fltrd = false;
                if (($.jCommon.json.getLength(state.musts) > 0 || state.opts.musts.length > 0) && qry['native']) {
                    fltrd = true;
                    cmn();
                    if (!qry["native"].query.bool.must) {
                        qry["native"].query.bool.must = [];
                    }
                    var must = qry["native"].query.bool.must;

                    if (state.opts.musts) {
                        $.each(state.opts.musts, function () {
                            var item = this;
                            var add = true;
                            $.each(state.musts, function () {
                                if ($.jCommon.string.equals(this.key, item.property) && $.jCommon.string.equals(this.value, item.value, true)) {
                                    add = false;
                                    return false;
                                }
                            });
                            if (add) {
                                var v = item.fValKey ? item[item.fValKey] : item.value;
                                var k = item.fKey ? item.fKey : item.key;
                                if($.jCommon.string.equals(v, "null value", true) || !v){
                                    if (!qry["native"].query.bool.must_not) {
                                        qry["native"].query.bool.must_not = [];
                                    }
                                    var missing = qry["native"].query.bool.must_not;
                                    missing.push({exists: {field: k}});
                                }
                                else {
                                    var frmt = String.format('"{0}.folded":"{1}"', k, v.toLowerCase());
                                    var match = '{"match":{' + frmt + '}}';
                                    must.push(JSON.parse(match));
                                }
                            }
                        });
                    }
                    $.each(state.musts, function (key, value) {
                        if($.jCommon.string.equals(value.term, "null value", true)){
                            if (!qry["native"].query.bool.must_not) {
                                qry["native"].query.bool.must_not = [];
                            }
                            var missing = qry["native"].query.bool.must_not;
                            missing.push({exists: {field: key}});
                        }
                        else {
                            var v = methods.getValueFromType(value.term, value.type);
                            v = $.jCommon.is.string(v) ? '"' + v.toLowerCase() + '"' : v;
                            var p = (!value.type || value.type === 'string') ? '.folded' : '';
                            var frmt = String.format('"{0}{1}":{2}', key, p, v);
                            var match = '{"match":{' + frmt + '}}';
                            must.push(JSON.parse(match));
                        }
                    });
                }

                if ($.jCommon.json.getLength(state.page.suggest)>0 && state.opts.search && state.opts.search.properties &&$.jCommon.json.getLength(state.opts.search.properties)>0) {
                    fltrd=true;
                    var shld = qry["native"].query.bool.should;
                    if (!shld) {
                        shld = [];
                        qry["native"].query.bool.should = shld;
                    }
                    $.each(state.opts.search.properties, function () {
                        var key = this;
                        $.each(state.page.suggest, function () {
                            var frmt;var match;
                            if (this.suggested) {
                                frmt = String.format('"{0}.folded":"{1}"', key, this.name.toLowerCase());
                                match = '{"term":{' + frmt + '}}';
                                shld.push(JSON.parse(match));
                            }
                            else if(this.search) {
                                frmt = String.format('"{0}.folded":"*{1}*"', key, this.name.toLowerCase());
                                match = '{"wildcard":{' + frmt + '}}';
                                shld.push(JSON.parse(match));
                            }
                            else{
                                frmt = String.format('"{0}.folded":"{1}*"', key, this.name.toLowerCase());
                                match = '{"wildcard":{' + frmt + '}}';
                                shld.push(JSON.parse(match));
                            }
                        });
                    });
                }

                if(!fltrd && !qry['native'].query.match_all){
                    qry['native'].query.match_all = {};
                }
                else if(qry['native'].query.match_all){
                    delete qry['native'].query.match_all;
                }
                if ($.jCommon.json.getLength(state.sort) > 0) {
                    qry.sort = state.sort;
                }
                return qry;
            },
            getValueFromType: function (value, type) {
                var v;
                type = type ? type : 'string';
                switch (type) {
                    case 'number':
                        v = parseInt(value);
                        break;
                    case "bool":
                        v = $.jCommon.equals(value, 'true', true);
                        break;
                    case 'string':
                    default:
                        v = value;
                        break;
                }
                return v;
            },
            reset: function (rs) {
                if(!state.resizing) {
                    methods.content.hideSubTables();
                    if(!rs) {
                        state.page = {
                            suggest: state.page.suggest,
                            terms: [],
                            data: [],
                            on: 0,
                            start: 0,
                            hits: 0,
                            top: 0,
                            gap: 0,
                            bfr: 30,
                            st: 0,
                            idx: 0,
                            up: false,
                            next: state.opts.limit,
                            isSub: (state.opts.sub ? true : false),
                            callback: {
                                header: []
                            }
                        };
                        state.vars = {st: 0, idx: 0, bfrIdx:0, diff: 0, tsr: 0};
                    }
                    else{
                        state.page.up = false;
                    }
                }
                state.page.bfrPrev=0;
                state.page.bfrNext=0;
                state.opts.init = false;
                if(state.node) {
                    state.node.children().remove();
                }
                state.contentNode = null;
                state.headerNode = null;
                state.searchNode = null;
                state.tableNode = null;
                state.table = null;
                state.opts.height = state.grid.height;
                state.opts.minHeight = state.grid.minHeight;
                state.opts.maxHeight = state.grid.maxHeight;
            },
            get: function (callback, requestOn, async) {
                if (async === undefined) {
                    async = true;
                }
                var qry = methods.getQuery();
                if ((state.page.start < state.page.hits || state.page.hits === 0)) {
                    var url = methods.getUrl(state.page.start, state.opts.limit, qry.url);
                    var s = function (data) {
                        if (data && data.results && data.results) {
                            if ($.isFunction(state.opts.onBefore)) {
                                data.results = state.opts.onBefore(data.results, state);
                            }
                            state.page.hits = data.isAggregated ? data.unique : data.hits;
                            state.page.start = data.next;
                            if ($.isFunction(state.opts.onData)) {
                                state.opts.onData(data);
                            }
                        }
                        if ($.isFunction(callback)) {
                            callback(data, requestOn);
                        }
                        if (state.opts.showBusy) {
                            state.node.loaders('hide');
                        }
                    };
                    var f = function () {
                        if (state.opts.showBusy) {
                            state.node.loaders('hide');
                        }
                    };
                    var mt = (qry._method ? qry._method : 'post');
                    var ct = $.jCommon.string.equals(mt, 'post', true) ? qry : null;
                    _request = $.htmlEngine.request(url, s, f, ct, mt, async);
                }
            },
            header: {
                init: function () {
                    state.hTable = methods.table.init(state.headerNode, state.opts.schema.header.table);
                    state.hTable.css({marginBottom: '0'});
                    state.headerNode.append(state.hTable);
                    var row = methods.table.init(null, state.opts.schema.header.table.row);
                    state.hTable.append(row);
                    state.opts.cells = {
                        count: state.opts.mapping.length
                    };
                    methods.resize.getCellSizes();
                    var on = 0;
                    $.each(state.opts.mapping, function () {
                        var map = this.header;
                        var cell = methods.table.init(row, state.opts.schema.header.table.row.cell, map);
                        cell.addClass(String.format('cell_{0}_w{1}', on, state.page.isSub ? '_sub' : ''));
                        cell.attr('data-header', map.property);
                        methods.resize.cell(cell, map, on);

                        var node = dCrt('div').addClass('tCell-in');
                        cell.append(node);

                        if (map.callback && $.isFunction(map.callback)) {
                            var r = methods.ext();
                            map.callback(node, map, {must: r.musts, should: r.shoulds}, state.opts);
                        }
                        else if (map.title) {
                            node.append(map.title);
                        }
                        if (map.tip) {
                            cell.attr('title', map.tip);
                        }

                        function setTip(nc, tip) {
                            if(map.sortTipMap){
                                var alt = map.sortTipMap[tip];
                                if(alt){
                                    tip=alt;
                                }
                            }
                            nc.attr('title', tip);
                        }
                        if (map.sortable) {
                            var d = 'glyphicon glyphicon-triangle-bottom';
                            var u = 'glyphicon glyphicon-triangle-top';
                            var cs = {fontSize: '16px', color: '#5f5f5f'};
                            var n = dCrt('div').css({
                                'float': 'right',
                                textAlign: 'right',
                                margin: '0px 5px 0px 5px',
                                width: '32px',
                                cursor: 'pointer',
                                fontSize: '16px',
                                color: '#c3c3c3'
                            }).addClass('col-sort').attr('id', $.jCommon.getRandomId("chev"));
                            node.append(n);
                            var a = $.htmlEngine.glyph(u);
                            var b = $.htmlEngine.glyph(d);
                            n.append(a).append(b);
                            map.dir = "none";
                            $.each(state.sort, function () {
                                if ($.jCommon.string.equals($.jCommon.string.getFirst(this.property, "."), map.property)) {
                                    map.dir = this.asc ? "asc" : "desc";
                                    return false;
                                }
                            });

                            function set() {
                                if (map.dir === 'none') {
                                    n.css({color: '#c3c3c3'});
                                    map.dir = 'none';
                                    a.show();
                                    b.show();
                                }
                                else if (map.dir === 'asc') {
                                    n.css(cs);
                                    map.dir = 'asc';
                                    a.show();
                                    b.hide();
                                }
                                else {
                                    n.css(cs);
                                    map.dir = 'desc';
                                    a.hide();
                                    b.show();
                                }
                                setTip(n, map.dir);
                            }

                            set();

                            function toggle(chev) {
                                if (map.dir === 'none') {
                                    chev.css(cs);
                                    if(map.defaultDir && map.defaultDir==='desc'){
                                        map.dir = 'desc';
                                        a.hide();
                                        b.show();
                                    }
                                    else {
                                        map.dir = 'asc';
                                        a.show();
                                        b.hide();
                                    }
                                }
                                else if (map.dir === 'asc') {
                                    if(map.defaultDir && map.defaultDir==='desc'){
                                        chev.css({color: '#c3c3c3'});
                                        map.dir = 'none';
                                        a.show();
                                        b.show();
                                    }
                                    else {
                                        chev.css(cs);
                                        map.dir = 'desc';
                                        a.hide();
                                        b.show();
                                    }
                                }
                                else {
                                    if(map.defaultDir && map.defaultDir==='desc'){
                                        map.dir = 'asc';
                                        a.show();
                                        b.hide();
                                    }
                                    else {
                                        chev.css({color: '#c3c3c3'});
                                        map.dir = 'none';
                                        a.show();
                                        b.show();
                                    }
                                }
                                var tip = map.dir;
                                if(map.sortTipMap){
                                    var alt = map[tip];
                                    if(alt){
                                        tip=alt;
                                    }
                                }
                                setTip(chev, map.dir);
                            }

                            n.on('click', function () {
                                toggle($(this));
                                methods.sort(map);
                            });
                        }
                        on++;
                    });
                    methods.resize.cells();

                    if (methods.filter.enabled()) {
                        row = methods.table.init(null, state.opts.schema.header.table.row);
                        state.hTable.append(row);
                        on = 0;
                        $.each(state.opts.mapping, function () {
                            var map = this.header;
                            var cell = methods.table.init(row, state.opts.schema.header.table.row.cell, map);
                            var cls = String.format('cell_{0}_w{1}', on, state.page.isSub ? '_sub' : '');
                            cell.addClass(cls);
                            var node = dCrt('div').addClass('tCell-in');
                            cell.append(node);
                            node.attr('data-filter-input', map.property);
                            on++;
                        });
                    }

                    if (state.opts.colResizable) {
                        state.hTable.on('mouseover', function () {
                            state.colSizing = true;
                            if (state.page.isSub) {
                                state.opts.parentGrid.pGrid('subResizing', true);
                            }
                        });
                        state.hTable.on('mouseleave', function () {
                            state.colSizing = false;
                            if (state.page.isSub) {
                                state.opts.parentGrid.pGrid('subResizing', false);
                            }
                        });
                        state.hTable.find('.tCell').resizable({
                            handles: 'e', resize: function (event, ui) {
                                event.preventDefault();
                                var w = ui.size.width;
                                if (w > 19) {
                                    var idx = ui.element.index();
                                    state.opts.cells.widths[idx].w = ui.size.width;
                                    methods.resize.cells();
                                }
                            }
                        });
                    }
                }
            },
            content: {
                init: function () {
                    var nd = true;
                    if($.isFunction(state.opts.onBefore)) {
                        state.page.data = state.opts.onBefore(state.page.data, state);
                    }
                    state.page.next = state.page.data.length;
                    state.page.hits = state.page.next;
                    methods.content.make();
                },
                noData: function (node) {
                    state.headerNode.hide();
                    state.searchNode.hide();
                    dHeight(state.contentNode, 50, 50, 50);
                    node.children().remove();
                    var c = dCrt('div');
                    node.append(c);
                    var hd = dCrt('h5').html("No results found.").addClass('letterpress').css({margin: '20px 10px 0 10px'});
                    c.append(hd);
                },
                getRow: function (item, on) {
                    var row = methods.table.init(null, state.opts.schema.content.table.row);
                    row.attr('data-index', on);
                    var cells = $(state.hTable.find('tr')[0]).children();
                    var idx = 0;
                    $.each(state.opts.mapping, function (idx, map) {
                        var cell = methods.table.init(row, state.opts.schema.content.table.row.cell, map);
                        var node = dCrt('div').addClass('tCell-in');
                        cell.append(node);
                        cell.addClass(String.format('cell_{0}_w{1}', idx, state.page.isSub ? '_sub' : ''));
                        if (!item) {
                            return true;
                        }
                        var value;
                        if ($.jCommon.string.equals(map.property, "#")) {
                            value = (on + 1);
                        }
                        else {
                            value = item[map.property];
                        }
                        if (map.callback && $.isFunction(map.callback)) {
                            var r = methods.ext();
                            value = map.callback(node, item, value, map, {
                                must: r.musts,
                                should: r.shoulds
                            }, {node: state.node, opts: state.opts});
                        }

                        if (value) {
                            node.append(value);
                        }
                        idx++;
                    });
                    methods.resize.cells();
                    return row;
                },
                getRows: function (idx, up, next, st) {
                    methods.content.hideSubTables();
                    state.table.find('.tRow').remove();
                    state.page.idx = idx;
                    state.page.up = up;
                    state.page.next = next;
                    var i = idx < 0 ? 0 : idx;
                    var maxRows = next;
                    if (!state.opts.paging.enabled) {
                        var mx = state.page.bfr*3;
                        maxRows = state.page.data.length>mx ? mx : state.page.data.length;
                    }
                    var rows = [];
                    var on = 0;
                    for (i; i < maxRows; i++) {
                        var item = state.page.data[i];
                        if (!item || !item._valid) {
                            break;
                        }
                        item = item.item ? item.item : item;
                        var row = methods.content.getRow(item, i);
                        row.attr('data-row-parent', !state.page.isSub);
                        dMax(row, state.rh);
                        rows.push(row);
                        if ($.isFunction(state.opts.onRowAdded)) {
                            var e = {};
                            e._idx = i;
                            e.rows = rows;
                            e.body = state.body;
                            e.row = row;
                            e.item = item;
                            e.opts = state.opts;
                            e.paging = state.page;
                            e.isSub = state.page.isSub;
                            state.opts.onRowAdded(e);
                        }
                        state.page.on = state.page.on + ((up) ? -1 : 1);
                        on++;
                    }
                    if (rows.length > 0) {
                        var tsRows = [];
                        $.each(rows, function () {
                            state.body.append(this);
                            if (this.hasClass('tSubRow')) {
                                if (!state.page.isSub && state.resizing && $.isFunction(this.onResize)) {
                                    this.onResize();
                                }
                                var sd = this.find('.sub-table.grid-sub');
                                if (sd && sd.pGrid('exists')) {
                                    sd.pGrid('enable');
                                }
                            }
                        });
                        methods.resize.cells();
                    }
                    st = methods.content.adjust(up);
                    methods.content.setContentHeight();
                    state.table.css({top: st + 'px'});
                    if (state.opts.paging.enabled) {
                    }
                    var e2 = jQuery.Event("table-view-rows-loaded");
                    e2.node = state.node;
                    e2.item = state.page.data;
                    e2.opts = state.opts;
                    e2.paging = state.page;
                    e2.isSub = state.page.isSub;
                    state.trigger(e2);
                    state.loading = false;
                },
                setContentHeight: function () {
                    var ht = (state.rh * state.page.hits);
                    var th = methods.getContentHeight(((state.opts.maxHeight > 0) ? state.opts.maxHeight : state.node.availHeight()) - 5);
                    var h = ((ht < th) && !state.page.isSub && !state.opts.grouped) ? th : ht;
                    dHeight(state.contentNode, h, h);
                },
                hideSubTables: function () {
                    if (state.page.isSub || !state.table) {
                        return false;
                    }
                    if (!state.hidden) {
                        state.hidden = dCrt('div').css({'visibility': 'hidden'});
                        lusidity.append(state.hidden);
                    }
                    var sbr = state.table.find('.tSubRow');
                    if (sbr) {
                        $.each(sbr, function () {
                            var grd = $(this).find('.sub-table.grid-sub');
                            if (grd && grd.pGrid('exists')) {
                                grd.pGrid('disable');
                            }
                            else {
                                console.log('Could not find pGrid.');
                            }
                        });
                    }
                    state.hidden.append(sbr);
                },
                make: function () {
                    state.page.size = state.page.data.length;
                    if(state.page.size>0) {
                        if (state.table) {
                            state.table.remove();
                        }
                        state.table = methods.table.init(state.contentNode).addClass('tGrid');
                        state.body = methods.table.init(state.table, state.opts.schema.content.table);
                        methods.content.getRows(state.page.idx, state.page.up, state.opts.limit, state.page.top);
                        methods.content.scroll.init();
                    }

                    var e = jQuery.Event("table-view-loaded");
                    e.node = state.node;
                    e.item = state.page.data;
                    e.opts = state.opts;
                    e.paging = state.page;
                    e.isSub = state.page.isSub;
                    state.trigger(e);
                    if ($.isFunction(state.opts.onAfter)) {
                        state.opts.onAfter(state.page.data, state.headerNode, state.tableNode);
                    }
                },
                next: function (up) {
                    var st = state.tableNode.scrollTop();
                    state.page.gap = st - state.page.top;
                    state.page.top = st;
                    state.vars = {st: 0, idx: 0, bfrIdx: 0, diff: 0, tsr: 0};

                    methods.content.getIndex(up);

                    var process = true;
                    var n = (state.vars.idx - state.vars.bfrIdx);
                    if (!methods.content.fixTop() && (n >= state.page.bfr)) {
                        var multi = Math.floor(state.vars.idx / state.page.bfr);
                        var prev = state.page.bfr * multi;
                        if ((prev === state.page.bfrPrev)) {
                            process = false;
                        }
                    }
                    var len = state.opts.limit;
                    var next = state.vars.bfrIdx + len;
                    var caching = false;
                    var bfrIdx = state.vars.bfrIdx;
                    if(up){
                        bfrIdx -= state.page.bfr;
                        bfrIdx = (bfrIdx<0) ? 0 : bfrIdx;
                    }
                    for (var j = bfrIdx; j < next; j++) {
                        var item = state.page.data[j];
                        if ((j < state.page.hits) && (undefined === item || !item._valid)) {
                            caching = true;
                            state.page.start = j;
                            $.htmlEngine.busy(state.node, {
                                type: 'cube',
                                cover: true,
                                adjustWidth: 0,
                                adjustHeight: 0
                            });
                            state.requestOn += 1;
                            methods.get(function (data, requestedOn) {
                                if (data && data.results) {
                                    var on = j;
                                    $.each(data.results, function () {
                                        var d = state.page.data[on];
                                        if(d) {
                                            d._valid = true;
                                            d.item = this;
                                            on++;
                                        }
                                    });
                                    if (process && (requestedOn === state.requestOn)) {
                                        methods.content.getRows(state.vars.bfrIdx, up, next, state.vars.st);
                                    }
                                }
                                state.node.loaders('hide');
                            }, state.requestOn, true);
                            break;
                        }
                    }
                    if (process && !caching) {
                        methods.content.getRows(state.vars.bfrIdx, up, next, state.vars.st);
                    }
                },
                getBfrd: function (idx) {
                    var r;
                    $.each(state.bfrdRows, function () {
                        if (this.idx === idx) {
                            r = this;
                            return false;
                        }
                    });
                    return r;
                },
                fixTop: function () {
                    var r = false;
                    var chk = state.tableNode.scrollTop();
                    if (chk <= state.rh) {
                        state.vars = {st: 0, idx: 0, bfrIdx: 0, diff: 0, tsr: 0};
                        state.page.bfrPrev=0;
                        state.page.bfrNext=0;
                        state.tableNode.scrollHandler('disabled', {
                            disabled: true, onChange: function () {
                                state.page.top = 0;
                                state.page.gap = 0;
                                state.tableNode.scrollHandler('setHeight', 0);
                                state.tableNode.scrollTop(0);
                                state.tableNode.scrollHandler('disabled', false);
                            }
                        });
                        r = true;
                    }
                    return r;
                },
                adjust: function (up) {
                    if (methods.content.fixTop()) {
                        state.vars.st = 0;
                    }
                    else {
                        state.vars.diff = 0;
                        state.vars.hasSub = false;
                        $.each(state.body.children(), function () {
                            var row = $(this);
                            if (row.hasClass('tSubRow')) {
                                state.vars.hasSub = true;
                                var idx = parseInt(row.attr('data-index'));
                                if (idx < state.vars.idx) {
                                    state.vars.tsr++;
                                    state.vars.diff += row.height();
                                }
                                else {
                                    return false;
                                }
                            }
                        });
                        var n = (state.vars.idx - state.vars.bfrIdx);
                        if (n >= state.page.bfr) {
                            var multi = Math.floor(state.vars.idx / state.page.bfr);

                            var prev = state.page.bfr * multi;
                            var next = prev + state.page.bfr;

                            if (!state.page.bfrPrev || (prev !== state.page.bfrPrev || next !== state.page.bfrNext)) {
                                state.vars.st = (state.page.top - ((n * state.rh) + state.vars.diff));
                                state.page.bfrPrev = prev;
                                state.page.bfrNext = next;
                            }
                        }
                        else {
                            state.page.bfrPrev = null;
                            state.page.bfrNext = null;
                            var rnd = Math.round((state.vars.bfrIdx * state.rh));
                            state.vars.st = rnd;
                        }
                    }
                    state.tableNode.scrollHandler('disabled', false);
                    return state.vars.st;
                },
                getIndex: function (up) {
                    var idx = -1;

                    var sch = state.tableNode[0].scrollHeight;
                    var th = state.tableNode.height();
                    var max = (state.page.top + th + state.rh);
                    if (max >= sch) {
                        idx = (Math.round((max - th) / state.rh));
                    }
                    else if (state.body.isInView(false, state.tableNode)) {
                        $.each(state.body.children(), function () {
                            var row = $(this);
                            function add(rw) {
                                state.vars.diff += rw.height();
                            }
                            if (row.hasClass('tSubRow')) {
                                state.vars.hasSub = true;
                                if (!row.isInView(false, state.tableNode)) {
                                    add(row);
                                }
                            }
                            else if (row.isInView(true, state.tableNode)) {
                                idx = parseInt(row.attr('data-index'));
                                return false;
                            }
                        });
                    }
                    var sh = state.page.top;// - ((state.page.top > state.vars.diff) ? state.vars.diff : 0);
                    state.vars.idx = idx > -1 ? idx : Math.round((sh / state.rh));
                    state.vars.idx = state.vars.idx < 0 ? 0 : state.vars.idx;
                    state.vars.idx = ((state.vars.idx + 1) > state.page.hits) ? state.page.hits : state.vars.idx;
                    state.vars.bfrIdx = state.vars.idx < 0 ? 0 : state.vars.idx;
                    if ((state.vars.bfrIdx - state.page.bfr) > 0) {
                        state.vars.bfrIdx -= state.page.bfr;
                    }
                    else {
                        state.vars.bfrIdx = 0;
                    }
                },
                scroll: {
                    init: function () {
                        if (state.opts.paging.enabled && !state._disabled) {
                            state.last = 0;
                            if (state.opts.limit >= state.page.hits) {
                                state.tableNode.on('scroll', function () {
                                    var st = state.tableNode.scrollTop();
                                    state.page.gap = st - state.page.top;
                                    state.page.top = st;
                                });
                                return false;
                            }
                            state.page.init = false;
                            var btm = false;
                            state.tableNode.scrollHandler({
                                isStopped: function (down, dif, bottom) {
                                    var tp;
                                    if (bottom && btm) {
                                        return false;
                                    }
                                    btm = false;
                                    if (state.resizing && !state._disabled) {
                                        state.tableNode.scrollTop(state.page.top);
                                        methods.content.next(state.page.up);
                                        state.resizing = false;
                                    }
                                    else if (!state._disabled) {
                                        methods.content.next(!down);
                                    }
                                    if (bottom) {
                                        state.tableNode.scrollHandler('disabled', true);
                                        var sh = state.tableNode[0].scrollHeight;
                                        state.tableNode.scrollTop(sh);
                                        state.tableNode.scrollHandler('setHeight', sh);
                                        state.tableNode.scrollHandler('disabled', false);
                                        btm = true;
                                    }
                                }
                            });
                        }
                    }
                }
            },
            filter: {
                getDistinct: function (str, prt, map, callback) {
                    if (state.resizing && state.filters.length > 0) {
                        if ($.isFunction(callback)) {
                            callback();
                            return false;
                        }
                    }
                    var s = function (data) {
                        if ($.isFunction(callback)) {
                            callback(data);
                        }
                    };
                    var url = String.format('/refine/{0}/{1}/properties/distinct', str, prt);
                    var cnt = map;
                    if (!map.distinct) {
                        cnt = {distinct: map};
                    }
                    $.htmlEngine.request(url, s, s, cnt, 'post', true);
                },
                isFilterable: function (key) {
                    return methods.filter.enabled() && $.jCommon.array.contains(state.opts.filter.properties, key)
                },
                enabled: function () {
                    return state.opts.filter.enabled && state.opts.filter.properties && state.opts.filter.properties.length > 0;
                },
                init: function () {
                    if (!methods.filter.enabled()) {
                        return false;
                    }

                    function callback(data) {
                        if (data) {
                            state.filters = ((state.opts.filter && state.opts.filter.properties) ? state.opts.filter.properties : []);
                            $.each(data.results, function () {
                                var item = this;
                                $.each(state.filters, function () {
                                    if (this.key === item.key && item.role==='filter') {
                                        this.results = item.results ? item.results : [];
                                        return false;
                                    }
                                });
                            });
                        }
                        $.each(state.filters, function () {
                            var c = state.headerNode.find('div[data-filter-input="' + this.key + '"]');
                            if (c) {
                                c.children().remove();
                                if (this.role === 'filter') {
                                    methods.filter.make(c, this);
                                }
                                else if(this.role === 'suggest'){
                                    methods.filter.suggest(c, this);
                                }
                                if ($.isFunction(this.callback)) {
                                    var r = methods.ext();
                                    this.callback(c, this, {must: r.musts, should: r.shoulds}, state.opts);
                                    state.page.callback.header.push({callback: this.callback, node: c, key: this.key});
                                }
                            }
                        });
                    }

                    var d = [];
                    $.each(state.opts.filter.properties, function () {
                        if (this.role === 'filter') {
                            d.push(this);
                        }
                    });
                    var r = methods.ext();
                    var dist = $.extend({}, state.opts.distinct);
                    dist.must = r.musts;
                    dist.should = r.shoulds;
                    methods.filter.getDistinct(state.opts.filter.store, state.opts.filter.partition, {
                        properties: d,
                        distinct: dist
                    }, callback);
                },
                make: function (node, item) {
                    node.children().remove();
                    var key = item.key;
                    node.css({position: 'relative', padding: '0'}).children().remove();
                    var ac = dCrt('input').css({width: '100%', paddingRight: '14px'});
                    node.append(ac);
                    var terms = [];
                    var sel = state.musts[key];
                    $.each(item.results, function () {
                        if ($.isFunction(item.onTerm)) {
                            item.onTerm(this);
                        }
                        if (!this.label) {
                            this.label = this.value;
                        }
                        var term = this.value.toString();
                        var found = false;
                        $.each(terms, function () {
                            var item = this;
                            if ($.jCommon.string.equals(term, item.value.toString(), true)) {
                                found = true;
                                return false;
                            }
                        });
                        if (term && !found) {
                            terms.push(this);
                        }
                        if (sel && sel.term && (sel.term === term || $.jCommon.string.equals(sel.term, term, true))) {
                            ac.val(this.label);
                            mb();
                        }
                    });
                    var btn;
                    function mb() {
                        btn = dCrt('div').attr('title', 'Remove filter').attr('type', 'button').attr('data-key', key).css({
                            position: 'absolute',
                            top: '4px',
                            right: '5px',
                            cursor: 'pointer'
                        });
                        node.append(btn);

                        var sp = dCrt('span').addClass('glyphicon glyphicon-remove').css({
                            marginLeft: '5px',
                            color: '#a94442'
                        });
                        btn.append(sp);

                        btn.on('click', function () {
                            methods.filter.remove(btn, ac);
                        });
                    }

                    ac.autocomplete({
                        source: terms, optionName: "terms.label", select: function (event, ui) {
                            var term = ui.item.label.toString();
                            ac.val(term);

                            var kp = state.musts[key];
                            if (kp && kp.btn) {
                                kp.btn.remove();
                            }
                            state.musts[key] = {term: ui.item.value, type: item.type ? item.type : 'string', altKey: item.altKey};

                            ac.on('blur', function () {
                                var term = ac.val();
                                if (!term || !$.jCommon.array.contains(terms, term)) {
                                    remove();
                                }
                            });
                            methods.reset();
                            methods.start();
                        }
                    });
                },
                suggest: function (node, item) {
                    node.children().remove();
                    var key = item.altKey ? item.altKey : item.key;
                    var rKey = item.returnKey ? item.returnKey : key;
                    node.css({position: 'relative', padding: '0'}).children().remove();
                    var ac = dCrt('input').css({width: '100%', paddingRight: '14px'});
                    node.append(ac);
                    var terms = [];
                    var sel = state.musts[key];
                    function mk() {
                        if (item.results) {
                            $.each(item.results, function () {
                                if ($.isFunction(item.onTerm)) {
                                    item.onTerm(this);
                                }
                                if (!this.label) {
                                    this.label = this.value;
                                }
                                var term = this.value.toString();
                                var found = false;
                                $.each(terms, function () {
                                    var item = this;
                                    if ($.jCommon.string.equals(term, item.value.toString(), true)) {
                                        found = true;
                                        return false;
                                    }
                                });
                                if (term && !found) {
                                    this.altKey = item.altKey;
                                    terms.push(this);
                                }
                                if (sel && sel.term && (sel.term === term || $.jCommon.string.equals(sel.term, term, true))) {
                                    ac.val(this.label);
                                    mb();
                                }
                            });
                        }
                    }
                    mk();
                    var btn;

                    function mb() {
                        btn = dCrt('div').attr('title', 'Remove filter').attr('type', 'button').attr('data-key', key).css({
                            position: 'absolute',
                            top: '4px',
                            right: '5px',
                            cursor: 'pointer'
                        });
                        node.append(btn);
                        var sp = dCrt('span').addClass('glyphicon glyphicon-remove').css({
                            marginLeft: '5px',
                            color: '#a94442'
                        });
                        btn.append(sp);

                        btn.on('click', function () {
                            methods.filter.remove(btn, ac);
                        });
                    }

                    ac.autocomplete({
                        source: function(request, response){
                            terms = [];
                            item.results = [];
                            var q = methods.getQuery();
                            var term = request.term.toLowerCase();
                            if(item.replaceSpace || item.replaceSpace===""){
                                term = $.jCommon.string.replaceAll(term, " ",item.replaceSpace);
                            }
                            if($.isFunction(item.beforeSuggest)){
                                term = item.beforeSuggest(term, item);
                            }
                            if(item.type==='number'){
                                term = parseInt(term);
                            }
                            else if(item.type==='bool'){
                                if(!(term==='true' || term==='false')){
                                    return false;
                                }
                                term = (term==='true');
                            }
                            else{
                                term = String.format('"*{0}*"', term);
                            }

                            var cp = (!item.type || item.type==='string') ? '.folded' : '';
                            q["native"].aggs = {
                                agg_result: {
                                    terms: {
                                        size: 0,
                                        field: (key + cp)
                                    },
                                    aggs: {
                                        only_one_post: {top_hits: {size: "1"}}
                                    }
                                }
                            };

                            var must = q["native"].query.bool.must;
                            var frmt = String.format('"{0}{1}":{2}', key, cp, term);
                            var match = '{"wildcard":{'+frmt+'}}';
                            must.push(JSON.parse(match));
                            var s = function (data) {
                                if(data && data.results){
                                    if($.isFunction(item.onResults)){
                                        item.onResults(node, item, data, term);
                                    }
                                    $.each(data.results, function () {
                                        var term = $.jCommon.json.getProperty(this, rKey);
                                        if(!term){
                                            return true;
                                        }
                                        term = term.toString();
                                        var found = false;
                                        $.each(item.results, function () {
                                            var item = this;
                                            if ($.jCommon.string.equals(term, item.value.toString(), true)) {
                                                found = true;
                                                return false;
                                            }
                                        });
                                        if(!found) {
                                            item.results.push({value: term});
                                        }
                                    });
                                    mk();
                                    response(terms);
                                }
                            };
                            $.htmlEngine.request('/query?limit=25', s, s, q, 'post');
                        },
                        select: function (event, ui) {
                            var term = ui.item.label.toString();
                            ac.val(term);
                            var kp = state.musts[key];
                            if (kp && kp.btn) {
                                kp.btn.remove();
                            }
                            state.musts[key] = {term: ui.item.value, type: item.type ? item.type : 'string', altKey: item.altKey};

                            ac.on('blur', function () {
                                var term = ac.val();
                                if (!term || !$.jCommon.array.contains(terms, term)) {
                                    remove();
                                }
                            });
                            methods.reset();
                            methods.start();
                        }
                    });
                },
                remove: function(btn, input) {
                    var key = btn.attr('data-key');
                    var tmp = {};
                    $.each(state.musts, function (k, e) {
                        if (!$.jCommon.string.equals(k, key)) {
                            tmp[k] = e;
                        }
                    });
                    state.musts = tmp;
                    methods.reset();
                    methods.start();
                    input.val('');
                    btn.remove();
                }
            },
            onResize: function (callback) {
                if (state.resizeStarted) {
                    return false;
                }
                state.resizeStarted = true;
                var rTime = new Date(1, 1, 2000, 12, 0, 0);
                var delta = 100;
                var on = 0;
                var s = true;
                var last = {
                    h: $(document).height(),
                    w: $(document).width()
                };

                function rs() {
                    callback();
                }

                function check() {
                    if (!s) {
                        return false;
                    }
                    var h = $(document).height();
                    var w = $(document).width();
                    if ((last.h !== h || last.w !== w)) {
                        rTime = new Date(1, 1, 2000, 12, 0, 0);
                        last.h = h;
                        last.w = w;
                        window.setTimeout(check, delta);
                    } else {
                        state.resizeStarted = false;
                        rs();
                    }
                }

                window.setTimeout(check, delta);
            },
            resize: {
                all: function (useCaller, afterResize) {
                    methods.resize.node(useCaller, afterResize);
                    methods.resize.cells();
                },
                node: function (useCaller, afterResize) {
                    dWidth(state.node, '', '', '');
                    dHeight(state.node, '', '', '');
                    var ofs = (state.page.isSub ? 40 : ((state.opts.grouped) ? 10 : 0));
                    var w = state.node.availWidth(ofs);
                    var pw = state.node.parent().width();
                    if(w>pw){
                        w = pw;
                    }
                    dWidth(state.node, 0, 0, w);
                    // state.opts.height, state.opts.minHeight
                    dHeight(state.node, 0, 0, state.opts.maxHeight);
                    function rs() {
                        var ah = state.node.parent().css('max-height');
                        if (ah === 'none') {
                            ah = state.node.availHeight();
                        }
                        else {
                            ah = parseInt(ah);
                        }

                        var mh = state.opts.maxHeight;
                        if (mh > 0 && ah > mh) {
                            ah = (mh);
                        }
                        if (state.page.isSub && mh > ah) {
                            ah = mh;
                        }


                        var th = methods.getContentHeight(ah);
                        if (state.page.isSub) {
                            // vertical scrollbar
                            th -= 14;
                        }
                        else if(state._init){
                            th -= 5;
                        }
                        dHeight(state.tableNode, null, null, th);
                        if (useCaller) {
                            methods.resize.call();
                        }
                        if($.isFunction(afterResize)){
                            afterResize();
                        }
                        state._init = false;
                    }
                    function check(i) {
                        if(state.headerNode && state.headerNode.height()>0){
                            state._init = i;
                            rs();
                        }
                        else {
                            window.setTimeout(function () {
                                check(i);
                            }, 100);
                        }
                    }
                    check(state._init);
                },
                call: function () {
                    if($.isFunction(state.opts.onResized)){
                        window.setTimeout(function () {
                            state.opts.onResized({
                                node: state.node,
                                item: state.page.data,
                                opts: state.opts,
                                paging: state.page,
                                isSub: state.page.isSub
                            });
                        },100);
                    }
                },
                getCellSizes: function () {
                    if (!state.hTable) {
                        return false;
                    }
                    if (!state.opts.cells.widths) {
                        var nw = state.node.width();
                        var dif = state.opts.spring ? 10 : 20;
                        state.opts.cells.pw = nw>0 ? (nw-dif) : state.node.availWidth(dif);
                        state.opts.cells.widths = [];
                        var w = state.opts.cells.pw / state.opts.cells.count;
                        state.opts.cells.width = w;
                        for (var i = 0; i < state.opts.cells.count; i++) {
                            state.opts.cells.widths.push({w: w, mod: false});
                        }
                    }
                },
                cell: function (cell, map, on) {
                    var w = state.opts.cells.widths[on];
                    var cw;
                    if (map.css && (map.css.width || map.css.minWidth || map.css.maxWidth)) {
                        cw = map.css.maxWidth ? map.css.maxWidth : (map.css.width ? map.css.width : map.css.minWidth);
                        cw = parseInt($.jCommon.string.replaceAll(cw, 'px'));
                        cw = {w: cw, mod: true};
                    }
                    if (cw && $.jCommon.is.numeric(cw.w)) {
                        state.opts.cells.widths[on] = cw;
                        var n = 0;
                        for (var i = 0; i < state.opts.cells.count; i++) {
                            var c = state.opts.cells.widths[i];
                            if (i !== on && !c.mod) {
                                n++;
                            }
                        }
                        var dif = (w.w - cw.w) / n;
                        for (var i = 0; i < state.opts.cells.count; i++) {
                            var c = state.opts.cells.widths[i];
                            if (i !== on && !c.mod) {
                                c.w += dif;
                            }
                        }
                    }
                },
                cells: function () {
                    if(state.opts.resetCellSizes){
                        state.opts.cells.widths=false;
                        methods.resize.getCellSizes();
                        state.opts.resetCellSizes=false;
                    }
                    if (state.opts.cells && state.opts.cells.widths) {
                        var left = 0;
                        for (var i = 0; i < state.opts.cells.count; i++) {
                            var w = state.opts.cells.widths[i].w;
                            var cls = String.format('.cell_{0}_w{1}', i, state.page.isSub ? '_sub' : '');
                            var cells = state.node.find(cls);
                            dWidth(cells, 0, w, 0);
                            cells.css({left: left + 'px'});
                            left += w;
                        }
                        if (state.headerNode) {
                            state.tableNode.width((left - 18));
                        }
                        if (state.table) {
                            state.tableNode.width(left + 18);
                        }
                    }
                }
            },
            getContentHeight: function (h) {
                if(state.headerNode) {
                    var hdr = state.headerNode.height();
                    hdr = (hdr < state.rh) ? state.rh : hdr;
                    var srch = state.searchNode.height();
                    if (methods.search.enabled()) {
                        srch = (srch <= 0) ? state.rh : srch;
                        srch += 12;
                    }
                    h -= (hdr + srch);
                    h += state.opts.offset.table;
                }
                return h;
            },
            search: {
                isFilterable: function (key) {
                    return methods.search.enabled() && $.jCommon.array.contains(state.opts.search.properties, key)
                },
                enabled: function () {
                    return state.opts.search.enabled && state.opts.search.properties && state.opts.search.properties.length > 0;
                },
                init: function () {
                    if (!methods.search.enabled()) {
                        return false;
                    }
                    state.searchNode.children().remove();
                    var node = dCrt('div');
                    state.searchNode.append(node);
                    var grp = dCrt('div').addClass('ui-widget input-group').css({marginBottom: '5px'});
                    node.append(grp);
                    var box = dCrt('input').addClass('form-control').attr('type', 'text').attr('placeholder', state.opts.search.text).css({
                        height: '34px',
                        minWidth: '230px'
                    });
                    grp.append(box);
                    var sp = dCrt('span').addClass('input-group-btn');
                    grp.append(sp);
                    var btn = dCrt('btn').addClass('btn btn-default').attr('title', 'Using this button will allow you to do a partial match.').attr('type', 'button').html(state.opts.search.btn);
                    sp.append(btn);

                    var searchFilterNode = dCrt('div');
                    state.searchNode.append(searchFilterNode);

                    var selected = false;

                    function split(val) {
                        return val.split(/,\s*/);
                    }

                    function extractLast(term) {
                        return split(term).pop();
                    }

                    function mb(term) {
                        if (!$.jCommon.string.empty(term.name)) {
                            var btn = dCrt('btn').addClass('btn btn-default blue').attr('title', 'Remove filter').attr('type', 'button');
                            btn.append(dCrt('span').html(term.name)).css({margin: '0 10px 10px 0'});
                            searchFilterNode.append(btn);
                            var sp = dCrt('span').addClass('glyphicon glyphicon-remove').css({
                                marginLeft: '5px',
                                color: '#a94442'
                            });
                            btn.append(sp);
                            btn.on('click', function () {
                                var temp = [];
                                $.each(state.page.suggest, function () {
                                    if (!$.jCommon.string.equals(this.name, term.name)) {
                                        temp.push(this);
                                    }
                                });
                                state.page.suggest = temp;
                                btn.remove();
                                if (term.suggested) {
                                    state.page.suggest.push(term.name.toString());
                                }
                                methods.reset();
                                methods.start();
                            });
                        }
                    }

                    btn.on('click', function (e) {
                        var term = box.val();
                        if (!$.jCommon.string.empty(term)) {
                            term = term.trim();
                            var found = false;
                            $.each(state.page.suggest, function () {
                                if ($.jCommon.string.equals(this.name, term, true)) {
                                    found = true;
                                    return false;
                                }
                            });
                            if (!found) {
                                var v = {name: term, suggest: false};
                                state.page.suggest.push(v);
                                methods.reset();
                                methods.start();
                            }
                        }
                    });
                    $.each(state.page.data, function () {
                        var item = this;
                        $.each(state.opts.search.properties, function () {
                            var key = this.toString();
                            var term = item[key];
                            if (term && !$.jCommon.array.contains(state.page.suggest, term)) {
                                state.page.suggest.push(term);
                            }
                        });
                    });

                    $.each(state.page.suggest, function (term) {
                        mb(this);
                    });

                    box.autocomplete({
                        minLength: 0,
                        source: function (request, response) {
                            state.page.suggest.sort();
                            // delegate back to autocomplete, but extract the last term
                            response($.ui.autocomplete.filter(
                                state.page.suggest, extractLast(request.term)));
                        },
                        focus: function () {
                            // prevent value inserted on focus
                            return false;
                        },
                        select: function (event, ui) {
                            selected = true;
                            // remove the current input
                            state.page.suggest.pop();
                            // add the selected item
                            var term = ui.item.value;
                            if (!$.jCommon.string.empty(term)) {
                                term = term.trim();
                                state.page.suggest.push({name: term, suggested: true});
                            }
                            this.value = "";
                            methods.reset();
                            methods.start();
                        }
                    });
                }
            },
            sort: function (header) {
                if (!$.jCommon.is.array(state.sort)) {
                    console.log('wrong object type.');
                }
                if(state.opts.singleSort){
                    state.sort=[];
                }
                if (header.dir === 'none') {
                    var tmp = [];
                    $.each(state.sort, function () {
                        var a = header.property;
                        var b = this.property;
                        if (!$.jCommon.string.equals(a, b)) {
                            tmp.push(this);
                        }
                    });
                    state.sort = tmp;
                }
                else {
                    var f = false;
                    $.each(state.sort, function () {
                        var a = header.property;
                        var b = this.property;
                        if ($.jCommon.string.equals(a, b)) {
                            this.asc = (header.dir === 'asc');
                            f = true;
                            return false;
                        }
                    });
                    if (!f) {
                        state.sort.push({property: header.property + ((header.sortType) ? '.folded' : ''), asc: (header.dir === 'asc')})
                    }
                }
                methods.reset();
                methods.start();
            },
            table: {
                set: function (node, schema) {
                    if (!schema) {
                        return false;
                    }
                    if (schema.cls && schema.cls.length > 0) {
                        node.addClass(schema.cls);
                    }
                    if ($.jCommon.json.getLength(schema.css) > 0) {
                        node.css(schema.css);
                    }
                    if ($.jCommon.json.getLength(schema.attr) > 0) {
                        $.each(schema.attr(function (k, v) {
                            node.append(k, v);
                        }));
                    }
                },
                make: function (node, type, schema, map) {
                    var r = dCrt(type);
                    methods.table.set(r, schema);
                    methods.table.set(r, map);
                    if ((undefined !== node) && (null !== node)) {
                        node.append(r);
                    }
                    return r;
                },
                init: function (node, schema, map) {
                    return methods.table.make(node, 'div', schema, map);
                }
            }
        };

        state.rebindRemove = function () {
            var i = state.headerNode.find('.tCell-in .glyphicon-remove');
            $.each(i, function () {
                var rmv = this;
                var btn = rmv.parent();
                var input = btn.prev();
                btn.unbind('click');
                btn.on('click', function () {
                    methods.filter.remove(btn, input);
                });
            });
        };
        state.resize = function (opts) {
            state.resizeDisabled = true;
            if (opts) {
                state.opts = $.extend({}, state.opts, opts);
            }
            methods.resize.all(false);
            state.resizeDisabled = false;
        };
        state.reset = function (opts) {
            if (opts) {
                state.opts = $.extend({}, state.opts, opts);
            }
            methods.reset(state.opts.resizing);
            methods.start();
            state.opts.resizing = false;
        };
        state.scroller = function (opts) {
            if (opts.enabled) {
                // state.tableNode.removeClass('scroller-disabled');
                state.scrollerDisabled = false;
                state.resizeDisabled = false;
            }
            else {
                state.scrollerDisabled = true;
                state.resizeDisabled = true;
                // state.tableNode.addClass('scroller-disabled');
            }
        };
        //environment: Initialize
        methods.init();

        state._enable = function () {
            if(state.page.top>0) {
                state.tableNode.scrollTop(state.page.top);
            }
            state._disabled = false;
        };
        state.setContentHeight = function (opts) {
            methods.content.setContentHeight();
        };

        state.subResizing = function (opts) {
            state.resizeDisabled = opts;
        };
        state._disable = function () {
            state._disabled = true;
        };
        return state;
    };
    $.pGridAdhoc.classes = {
        table: 'table',
        table_striped: 'table-striped',
        table_bordered: 'table-bordered',
        table_hover: 'table-hover',
        table_condensed: 'table-condensed'

    };

    //Default Settings
    $.pGridAdhoc.defaults = {
        colResizable: true,
        paneled: false,
        distinct: {},
        limit: 75,
        bfr: 25,
        minHeight: 0,
        maxHeight: 0,
        height: 0,
        musts: [],
        sub: false,
        grouped: false,
        offset: {
            parent: 0,
            table: 0
        },
        showBusy: false,
        paging: {
            enabled: true
        },
        schema: {
            header: {
                table: {
                    type: 'div',
                    cls: 'tHead',
                    css: {},
                    attr: {},
                    row: {
                        type: 'div',
                        cls: 'tRow',
                        css: {},
                        attr: {},
                        cell: {
                            type: 'div',
                            cls: 'tCell',
                            css: {textAlign: 'center', verticalAlign: 'middle'},
                            attr: {}
                        }
                    }
                }
            },
            content: {
                table: {
                    type: 'div',
                    cls: 'tBody ',
                    attr: {},
                    row: {
                        type: 'div',
                        cls: 'tRow',
                        css: {},
                        attr: {},
                        height: 28,
                        cell: {
                            type: 'div',
                            cls: 'tCell',
                            css: {whiteSpace: 'nowrap', overflow: 'hidden'},
                            attr: {}
                        }
                    }
                }
            }
        },
        onBefore: function (items, node) {
            return items;
        },
        onAfter: function (items, node) {
            return items;
        },
        onFiltered: function (e) {
            // data will be at e.item
        },
        singleSort: false,
        filter: {
            enabled: true,
            properties: []
        },
        group: {
            enabled: false,
            properties: []
        },
        sort: {
            enabled: false,
            properties: []
        },
        search: {
            enabled: false,
            text: "Build a list",
            btn: "Add",
            properties: [/*'title', 'name'*/]
        },
        mapping: [
            {
                header: {
                    title: 'H1',
                    property: 'title',
                    cls: "some-class",
                    css: {display: 'block'},
                    attr: {id: '12345'},
                    callback: function (node, map) {
                    }
                },
                property: 'title',
                cls: "some-class",
                css: {display: 'block'},
                attr: {id: '12345'},
                callback: function (node, item, value, map) {
                }
            }
        ],
        queries: {
            get: function (data, start, limit) {
                // data original data
                // start paging
                // limit max returned
            },
            filter: function (filters, searchTerms, start, lmit) {
                // filters an array of key value pairs
                // terms an array of string values
                // start paging
                // limit max returned
                $.each(filters, function () {
                    var key = this.key;
                    var value = this.value
                });
            }
        }
    };

    //Plugin Function
    $.fn.pGridAdhoc = function (method, options) {
        if (method === undefined) method = {};
        if (typeof method === "object") {
            return this.each(function() {
                new $.pGridAdhoc($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $pGridAdhoc = $(this).data('pGridAdhoc');
            switch (method) {
                case 'enable':
                    $pGridAdhoc._enable();
                    break;
                case 'disable':
                    $pGridAdhoc._disable(options);
                    break;
                case 'exists':
                    return (null !== $pGridAdhoc && undefined !== $pGridAdhoc && $pGridAdhoc.length > 0);
                    break;
                case 'resize':
                    $pGridAdhoc.resize(options);
                    break;
                case 'reset':
                    $pGridAdhoc.reset(options);
                    break;
                case 'subResizing':
                    $pGridAdhoc.subResizing(options);
                    break;
                case 'rebindRemove':
                    $pGridAdhoc.rebindRemove(options);
                    break;
                case 'scroller':
                    $pGridAdhoc.scroller(options);
                    break;
                case 'setContentHeight':
                    $pGridAdhoc.setContentHeight(options);
                    break;
                case 'state':
                default:
                    return $pGridAdhoc;
                    break;
            }
        }
    };

    $.pGridAdhoc.call = function (elem, options) {
        elem.pGridAdhoc(options);
    };

    try {
        $.htmlEngine.plugins.register("pGridAdhoc", $.pGridAdhoc.call);
    }
    catch (e) {
        console.log(e);
    }

})(jQuery);