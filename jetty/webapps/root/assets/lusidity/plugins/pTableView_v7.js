

;(function ($) {

    //Object Instance
    $.pTableView = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);
        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.pTableView.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);

        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.suggest = [];
        state.terms = [];
        state.paging = {
            max: 50,
            start: 0,
            hits: 0
        };
        state.callback = {
            header: []
        };
        state.filters = {};
        var _rh =state.opts.schema.content.table.row.height;
        // Store a reference to the environment object
        el.data("pTableView", state);

        // Private environment methods
        methods = {
            init: function() {
                var len = state.opts.mapping.length;
                var w = state.width();
                state.opts.cellMaxWidth = w/len;
                state.opts.offset.parent = state.opts.offset.parent ? state.opts.offset.parent : 0;
                state.opts.offset.table = state.opts.offset.table ? state.opts.offset.table : 0;

                state.node = dCrt('div').addClass('tNode').css({position: 'relative', clear: 'both'});
                state.append(state.node);

                state.contentNode = dCrt('div').addClass('tContent').css({position: 'relative'});
                state.sliderNode = dCrt('div').addClass('tSlider').hide();

                state.headerNode = dCrt('div').addClass('tHeader').css({overflow: 'hidden', paddingRight: '18px', position: 'relative', clear: 'both'});
                state.searchNode = dCrt('div').addClass('tSearch').css({position: 'relative', clear: 'both'});
                state.tableNode = dCrt('div').addClass('tTable').css({position: 'relative', clear: 'both', marginRight: '18px'}).append(state.contentNode).append(state.sliderNode);

                state.node.append(state.searchNode).append(state.headerNode).append(state.tableNode);

                if(state.opts.item && state.opts.getUrl && $.isFunction(state.opts.getUrl)){
                    this.get(false);
                }
                else if(state.opts.data && state.opts.data.results){
                    state.paging.hits = state.opts.data.results.length;
                }
                methods.resize.all();
                methods.header.init();
                methods.content.init();
                methods.search.init();
                methods.filter.init();
                window.setTimeout(function () {
                   methods.resize.cells();
                }, 300);
                lusidity.environment('onResize', function () {
                    methods.resize.all();
                });
            },
            inject: function (idx) {
            },
            get: function (async, callback) {
                if(undefined===async){
                    async=true;
                }
                if((state.paging.start<state.paging.hits || state.paging.hits===0) && $.isFunction(state.opts.getUrl)) {
                    var url = state.opts.getUrl(state.opts.item, state.paging.start, state.opts.limit);
                    if (null !== url) {
                        $.htmlEngine.busy(state.tableNode, {type: 'cube', cover: true, adjustWidth: 0, adjustHeight: state.opts.offset.parent});
                        var s = function (data) {
                            if (data && data.results && data.results) {
                                if (state.opts.data && state.opts.data.results) {
                                    $.each(data.results, function () {
                                        state.opts.data.results.push(this);
                                    });
                                }
                                else {
                                    state.opts.data = data;
                                }
                                state.paging.hits = data.hits;
                                state.paging.start = data.next;
                                if ($.isFunction(state.opts.onData)) {
                                    state.opts.onData(data);
                                }
                            }
                            if($.isFunction(callback)){
                                callback();
                            }
                            state.tableNode.loaders('hide');
                        };
                        var f = function () {
                            state.tableNode.loaders('hide');
                        };

                        $.htmlEngine.request(url, s, f, null, 'get', async);
                    }
                    else if(state.opts.data && state.opts.data.results){
                        methods.html.init(state.opts.data, 0);
                    }
                }
            },
            header:{
                init: function () {
                    state.hTable = methods.table.init(state.headerNode, state.opts.schema.header.table);
                    state.hTable.css({marginBottom: '0'});
                    state.headerNode.append(state.hTable);
                    var row = methods.table.row(null, state.opts.schema.header.table.row);
                    state.hTable.append(row);
                    $.each(state.opts.mapping, function () {
                        var map = this.header;
                        var cell = methods.table.cell(row, state.opts.schema.header.table.row.cell, map);
                        cell.attr('data-header', map.property);
                        if(map.callback && $.isFunction(map.callback)){
                            map.callback(cell, map);
                        }
                        else if(map.title){
                            cell.append(map.title);
                        }
                        if(map.tip){
                            cell.attr('title', map.tip);
                        }
                        if (map.sortable) {
                            cell.css({position: 'relative'});
                            var d = 'glyphicon glyphicon-triangle-bottom';
                            var u = 'glyphicon glyphicon-triangle-top';
                            var cs = {fontSize: '16px', color: '#5f5f5f'};
                            var n = dCrt('div').css({
                                "float": 'right',
                                textAlign: 'right',
                                margin: '0px 5px 0px 5px',
                                width: '32px',
                                cursor: 'pointer',
                                fontSize: '16px',
                                color: '#c3c3c3'
                            }).addClass('col-sort');
                            cell.append(n);
                            var a = $.htmlEngine.glyph(u);
                            var b = $.htmlEngine.glyph(d);
                            n.append(a).append(b);
                            function toggle() {
                                if (map.dir === 'none') {
                                    n.css(cs);
                                    map.dir = 'asc';
                                    a.show();
                                    b.hide();
                                }
                                else if (map.dir === 'asc') {
                                    n.css(cs);
                                    map.dir = 'desc';
                                    a.hide();
                                    b.show();
                                }
                                else {
                                    n.css({color: '#c3c3c3'});
                                    map.dir = 'none';
                                    a.show();
                                    b.show();
                                }
                            }

                            n.on('click', function () {
                                toggle();
                                methods.sort(map);
                            });
                            toggle();
                        }
                    });

                    if(methods.filter.enabled()) {
                        row = methods.table.row(null, state.opts.schema.header.table.row);
                        state.hTable.append(row);
                        $.each(state.opts.mapping, function () {
                            var map = this.header;
                            var cell = methods.table.cell(row, state.opts.schema.header.table.row.cell, map);
                            cell.attr('data-filter-input', map.property);
                        });
                    }

                    if(state.opts.colResizable) {
                        state.hTable.find('td').resizable({
                            handles: 'e', resize: function (event, ui) {
                                methods.resize.cells();
                            }
                        });
                    }
                }
            },
            content: {
                init: function () {
                    if(state.opts.data){
                        if($.jCommon.is.array(state.opts.data)){
                            state.opts.data = { results: $.jCommon.array.clone(state.opts.data)};
                        }
                        state.working = state.opts.data.results;
                        methods.content.make();
                        return true;
                    }
                },
                getRow: function (item, on) {
                    var row = methods.table.row(null, state.opts.schema.content.table.row);
                    row.attr('data-index', on);
                    if(state.opts.hovered){
                        row.on('mouseenter', function () {
                            row.addClass('hovered');
                        });
                        row.on('mouseleave', function () {
                            row.removeClass('hovered');
                        });
                    }
                    var cells = $(state.hTable.find('tr')[0]).children();
                    $.each(state.opts.mapping, function (idx, map) {
                        var cell = methods.table.cell(row, state.opts.schema.content.table.row.cell, map);
                        if(!item){
                            return true;
                        }
                        var value;
                        if ($.jCommon.string.equals(map.property, "#")) {
                            value = (on+1);
                        }
                        else {
                            value = item[map.property];
                        }
                        if (map.callback && $.isFunction(map.callback)) {
                            value = map.callback(cell, item, value, map);
                        }

                        if (value) {
                            cell.append(value);
                        }
                    });
                    return row;
                },
                getRows: function (idx, up) {
                    var on = 0;
                    var i = idx<0 ? 0 : idx;
                    var len = (i+state.paging.max);
                    var rows = [];
                    for(i;i<len;i++){
                        var item = state.working[i];
                        if(!item || (state.opts.all && (on>=state.paging.hits))){
                            break;
                        }
                        var row = methods.content.getRow(item, i);
                        rows.push(row);
                        dMax(row, _rh);
                    }
                    if(rows.length>0){
                        state.body.children().remove();
                        state.body.append(rows);
                        methods.resize.cells();
                    }
                },
                make: function () {
                    if ($.isFunction(state.opts.onBefore)) {
                        state.working = state.opts.onBefore(state.working, state);
                    }
                    state.paging.size = state.working.length;

                    if (state.table) {
                        state.table.remove();
                    }
                    state.table = methods.table.init(state.contentNode).addClass('tGrid');
                    state.body = methods.table.tbody(state.table, state.opts.schema.content.table);
                    methods.content.max();
                    methods.resize.slider();
                    methods.content.getRows(0, true);

                    var e = jQuery.Event("table-view-loaded");
                    e.item = state.working;
                    state.trigger(e);
                    if ($.isFunction(state.opts.onAfter)) {
                        state.opts.onAfter(state.working, state.headerNode, state.tableNode);
                    }
                },
                max: function () {
                    if (state.opts.all) {
                        return state.paging.hits;
                    }
                    var h = methods.getContentHeight();
                    var c = Math.floor((h / _rh));
                    c = (c>state.paging.hits && state.paging.hits>0) ? state.paging.hits : c;
                    state.paging.max=c;
                    return c;
                }
            },
            filter: {
                isFilterable: function (key) {
                    return methods.filter.enabled() && $.jCommon.array.contains(state.opts.filter.properties, key)
                },
                enabled: function () {
                    return state.opts.filter.enabled && state.opts.filter.properties && state.opts.filter.properties.length>0;
                },
                init: function () {
                    if(!methods.filter.enabled()){
                        return false;
                    }
                    $.each(state.opts.filter.properties, function () {
                        var c = state.headerNode.find('td[data-filter-input="' +  this.key  + '"]');
                        if(c){
                            if($.isFunction(this.callback)) {
                                this.callback(c, this.key, state.working);
                                state.callback.header.push({callback: this.callback, node: c, key: this.key});
                            }
                            else{
                                methods.filter.make(c, this.key);
                            }
                        }
                    });
                },
                make: function (node, key) {
                    node.css({position: 'relative', padding: '0'}).children().remove();
                    var ac = dCrt('input').css({width: '100%', paddingRight: '14px'});
                    node.append(ac);
                    var terms = [];
                    $.each(state.working, function () {
                        var term = this[key];
                        if (term && !$.jCommon.array.contains(terms, term)) {
                            terms.push(term);
                        }
                    });
                    ac.autocomplete({source: terms, select: function (event, ui) {
                        var term = ui.item.value;
                        ac.val(term);

                        var kp = state.filters[key];
                        if(kp && kp.btn){
                            kp.btn.remove();
                        }
                        state.filters[key] = { term: term };

                        var btn = dCrt('div').attr('title', 'Remove filter').attr('type', 'button').css({position: 'absolute', top: '6px', right: '5px', cursor: 'pointer'});
                        node.append(btn);

                        var sp = dCrt('span').addClass('glyphicon glyphicon-remove').css({marginLeft: '5px', color: '#a94442'});
                        btn.append(sp);

                        ac.on('blur', function () {
                            var term = ac.val();
                            if(!term || !$.jCommon.array.contains(terms, term)){
                                remove();
                            }
                        });

                        btn.on('click', function () {
                            remove();
                        });

                        function remove() {
                            var tmp = {};
                            $.each(state.filters, function (k, e) {
                                if(!$.jCommon.string.equals(k, key)){
                                    tmp[k]=e;
                                }
                            });
                            state.filters= tmp;
                            filter();
                            ac.val('');
                            btn.remove();
                        }
                        function filter() {
                            var temp = [];
                            if($.jCommon.json.getLength(state.filters)>0) {
                                $.each(state.opts.data.results, function () {
                                    var item = this;
                                    var f = true;
                                    $.each(state.filters, function (k, e) {
                                        var a = item[k];
                                        if (!$.jCommon.string.equals(e.term, a, true)) {
                                            f = false;
                                        }
                                        return f;
                                    });
                                    if (f) {
                                        temp.push(item);
                                    }
                                });
                                state.working = temp;
                            }
                            else{
                                state.working = state.opts.data.results;
                            }
                            methods.search.init();
                            var e = jQuery.Event("table-view-filtered");
                            e.item = state.working;
                            state.trigger(e);
                            $.each(state.callback.header, function () {
                                this.callback(this.node, this.key, {results: state.working});
                            });
                            methods.content.make();
                            methods.resize.all();
                        }
                        filter();
                    }});
                }
            },
            resize: {
                all: function(){
                    methods.resize.node();
                    methods.resize.cells();
                },
                node: function () {
                    var h = state.opts.height ? state.opts.height : state.node.availHeight();
                    h -= state.opts.offset.parent ? state.opts.offset.parent : 0;
                    dMax(state.node, h);
                },
                getCellSizes: function () {
                    if(!state.hTable){
                        return false;
                    }
                    state.sizes = [];
                    var rows = state.hTable.find('tr');
                    $.each(rows, function () {
                        var len =  $(this).children();
                        $.each($(this).children(), function (idx, cell) {
                            var s = state.sizes[idx];
                            var w = $(this).innerWidth()+(((idx===0) || ((idx+1)===len))?0:2);
                            if(w>0) {
                                state.sizes.push(w);
                            }
                        });
                        return false;
                    });
                    return (state.sizes.length > 0);
                },
                cells: function(){
                    if(state.table && methods.resize.getCellSizes()) {
                        var rows = state.body.children();
                        var on = 0;
                        var len = rows.length;
                        for(var i=0;i<len;i++){
                            var row = $(rows[i]);
                            $.each(row.children(), function (idx, cell) {
                                dMax($(this), _rh, state.sizes[idx]);
                            });
                        }
                    }
                },
                slider: function () {
                    var max = methods.content.max();
                    var h = (max * _rh);
                    methods.slider.init(h);
                }
            },
            getContentHeight: function(){
                var hdr = state.headerNode.height();
                hdr = (hdr<_rh) ? _rh : hdr;
                var srch = state.searchNode.height();
                if (methods.search.enabled()) {
                    srch = (srch<=0) ? _rh : srch;
                    srch += 12;
                }
                var h = state.opts.maxHeight>0 ? state.opts.maxHeight : state.node.height();
                h -= (hdr + srch + state.opts.offset.table);
                h = (state.opts.minHeight>h ? state.opts.minHeight : h);
                return h;
            },
            search: {
                isFilterable: function (key) {
                    return methods.search.enabled() && $.jCommon.array.contains(state.opts.search.properties, key)
                },
                enabled: function () {
                    return state.opts.search.enabled && state.opts.search.properties && state.opts.search.properties.length>0;
                },
                init: function () {
                    if(!methods.search.enabled()){
                        return false;
                    }
                    state.searchNode.children().remove();
                    var node = dCrt('div');
                    state.searchNode.append(node);
                    var grp = dCrt('div').addClass('ui-widget input-group').css({marginBottom: '5px'});
                    node.append(grp);
                    var box = dCrt('input').addClass('form-control').attr('type', 'text').attr('placeholder', state.opts.search.text).css({height: '34px', minWidth: '230px'});
                    grp.append(box);
                    var sp = dCrt('span').addClass('input-group-btn');
                    grp.append(sp);
                    var btn = dCrt('btn').addClass('btn btn-default').attr('title', 'Using this button will allow you to do a partial match.').attr('type', 'button').html(state.opts.search.btn);
                    sp.append(btn);

                    var selected = false;
                    function split(val) {
                        return val.split(/,\s*/);
                    }

                    function extractLast(term) {
                        return split(term).pop();
                    }

                    btn.on('click', function (e) {
                        var term = box.val();
                        if(!$.jCommon.string.empty(term)) {
                            state.terms.push({name: term, suggested: false});
                            methods.search.filter();
                        }
                    });

                    state.suggest = [];
                    state.terms = [];
                    $.each(state.working, function () {
                        var item = this;
                        $.each(state.opts.search.properties, function () {
                            var key = this.toString();
                            var term = item[key];
                            if (term && !$.jCommon.array.contains(state.suggest, term)) {
                                state.suggest.push(term);
                            }
                        });
                    });

                    box.autocomplete({
                        minLength: 0,
                        source: function (request, response) {
                            state.suggest.sort();
                            // delegate back to autocomplete, but extract the last term
                            response($.ui.autocomplete.filter(
                                state.suggest, extractLast(request.term)));
                        },
                        focus: function () {
                            // prevent value inserted on focus
                            return false;
                        },
                        select: function (event, ui) {
                            selected = true;
                            // remove the current input
                            state.terms.pop();
                            // add the selected item
                            var term = ui.item.value;
                            if(!$.jCommon.string.empty(term)) {
                                state.terms.push({name: term, suggested: true});
                            }
                            // add placeholder to get the comma-and-space at the end
                            state.terms.push("");
                            this.value = "";
                            methods.search.filter();
                            return false;
                        }
                    });
                },
                filter: function () {
                    if(!state.searchFilterNode){
                        state.searchFilterNode = dCrt('div');
                        state.searchNode.append(state.searchFilterNode);
                    }
                    state.searchFilterNode.children().remove();
                    if (state.terms && state.terms.length>0) {
                        $.each(state.terms, function () {
                            var term = this;
                            if(!$.jCommon.string.empty(term.name)) {
                                var btn = dCrt('btn').addClass('btn btn-default blue').attr('title', 'Remove filter').attr('type', 'button');
                                btn.append(dCrt('span').html(term.name)).css({margin: '0 10px 10px 0'});
                                state.searchFilterNode.append(btn);
                                var sp = dCrt('span').addClass('glyphicon glyphicon-remove').css({marginLeft: '5px', color: '#a94442'});
                                btn.append(sp);
                                btn.on('click', function () {
                                    var temp = [];
                                    $.each(state.terms, function () {
                                        if(!$.jCommon.string.equals(this.name, term.name)){
                                            temp.push(this);
                                        }
                                    });
                                    state.terms = temp;
                                    btn.remove();
                                    if(term.suggested) {
                                        state.suggest.push(term.name.toString());
                                    }
                                    methods.search.filter();
                                });
                            }
                        });
                        var temp = [];
                        $.each(state.suggest, function () {
                            var s = this.toString();
                            var found = false;
                            $.each(state.terms, function () {
                                if($.jCommon.string.equals(s, this.name, false)){
                                    found = true;
                                    return false;
                                }
                            });
                            if(!found){
                                temp.push(s);
                            }
                        });
                        state.suggest = temp;
                        var rows = state.table.find('tbody').find('tr');
                        if(state.terms<=0){
                            state.working = state.opts.data.results;
                        }
                        else {
                            state.working = [];
                            $.each(state.opts.data.results, function () {
                                var found = false;
                                var item = this;
                                $.each(state.opts.search.properties, function () {
                                    var a = item[this];
                                    if(!a){
                                        return true;
                                    }
                                    $.each(state.terms, function () {
                                        var term = this;
                                        found = $.jCommon.string.contains(a, term.name, true);
                                        if (found) {
                                            return false;
                                        }
                                    });
                                    if (found) {
                                        return false;
                                    }
                                });
                                if(found) {
                                    state.working.push(item);
                                }
                            });
                        }
                    }
                    else{
                        state.working = state.opts.data.results;
                    }
                    if(state.opts.reset && state.terms.length===0 && state.opts.sort.properties.length === 0){
                        state.working = state.opts.reset(state.working, state);
                    }
                    var e = jQuery.Event("table-view-filtered");
                    e.item = state.working;
                    state.trigger(e);
                    $.each(state.callback.header, function () {
                        this.callback(this.node, this.key, {results: state.working});
                    });
                    methods.content.make();
                    methods.resize.all();
                }
            },
            slider: {
                init: function (h) {
                    state.sliderNode.children().remove();
                    if(state.paging.max>=state.paging.hits){
                        if(!state.opts.sub) {
                            dMax(state.contentNode, h + 5);
                            state.contentNode.css({overflowY: 'auto', overflowX: 'hidden'});
                            var h = state.contentNode.availHeight();
                            dMax(state.contentNode, h);
                        }
                        return false;
                    }
                    if(!state.opts.sub) {
                        state.contentNode.css({overflowY: '', overflowX: ''});
                        dMax(state.contentNode, '');
                    }
                    state.sliderNode.css({
                        position: 'absolute',
                        top: '0',
                        right: '-28px',
                        zIndex: 99999999
                    }).show();

                    if(!state.wheeled) {
                        methods.slider.wheel();
                    }
                    dMax(state.sliderNode, h);
                    var c = dCrt('div').css({width: '28px'});
                    dMax(c, h);
                    state.sliderNode.append(c);
                    state.slide = dCrt('div');
                    dMax(state.slide, h);
                    c.append(state.slide);
                    var idx = state.paging.hits;
                    var min = ((state.paging.max<state.paging.hits) ? state.paging.max : state.paging.hits);
                    var max = Math.ceil(state.paging.hits / min);
                    state.slide.slider({
                        orientation: "vertical",
                        range: "max",
                        min: 1,
                        max: state.paging.hits,
                        value: state.paging.hits,
                        step: 1,
                        slide: function( event, ui ) {
                            var up = (ui.value>idx);
                            idx = ui.value;
                            methods.slider.increment(up, idx);
                        }
                    });
                },
                increment: function(up, idx){
                    var next = (state.paging.hits - idx);
                    if($.isFunction(state.opts.getUrl) && !up && ((state.paging.max-idx)>=(state.paging.start-30))){
                        methods.get(false);
                    }
                    methods.content.getRows(next, up);
                },
                wheel: function () {
                    if(state.tableNode.mouseWheel('exists')){
                        state.tableNode.mouseWheel('unbind');
                    }
                    state.tableNode.mouseWheel({onScrolled : function (e) {
                        e.preventDefault();
                        e.stopPropagation();
                        var idx = state.slide.slider("option", "value");
                        idx = (e.up) ? (idx + state.opts.increment.wheel) : (idx - state.opts.increment.wheel);
                        if (idx > 0 && idx <= (state.paging.hits)){
                            state.slide.slider("option", "value", idx);
                            methods.slider.increment(e.up, idx);
                        }
                    }});
                    state.wheeled = true;
                }
            },
            sort: function (header) {
                if(!$.jCommon.is.array(state.opts.sort.properties)){
                    console.log('wrong object type.');
                }
                if(header.dir === 'none'){
                    var tmp = [];
                    $.each(state.opts.sort.properties, function () {
                        var a = header.property;
                        var b = this.property;
                        if(!$.jCommon.string.equals(a, b)){
                            tmp.push(this);
                        }
                    });
                    state.opts.sort.properties = tmp;
                }
                else{
                    var f = false;
                    $.each(state.opts.sort.properties, function () {
                        var a = header.property;
                        var b = this.property;
                        if($.jCommon.string.equals(a, b)){
                            this.asc = (header.dir==='asc');
                            f = true;
                            return false;
                        }
                    });
                    if(!f){
                        state.opts.sort.properties.push({property: header.property, asc: (header.dir==='asc')})
                    }
                }
                state.on=0;
                state.working = $.jCommon.array.sort(state.working, state.opts.sort.properties);
                methods.search.filter();
            },
            table: {
                set: function(node, schema){
                    if(!schema){
                        return false;
                    }
                    if(schema.cls && schema.cls.length>0){
                        node.addClass(schema.cls);
                    }
                    if($.jCommon.json.getLength(schema.css)>0){
                        node.css(schema.css);
                    }
                    if($.jCommon.json.getLength(schema.attr)>0){
                        $.each(schema.attr(function (k, v) {
                            node.append(k, v);
                        }));
                    }
                },
                make: function (node, type, schema, map) {
                    var r = dCrt(type);
                    methods.table.set(r, schema);
                    methods.table.set(r, map);
                    if((undefined!==node) && (null!==node)) {
                        node.append(r);
                    }
                    return r;
                },
                init: function (node, schema, map) {
                    return methods.table.make(node, 'table', schema, map);
                },
                tHead: function (node, schema, map) {
                    return methods.table.make(node, 'thead', schema, map);
                },
                tbody: function (node, schema, map) {
                    return methods.table.make(node, 'tbody', schema, map);
                },
                hCell: function (row, schema, map) {
                    return methods.table.make(row, 'th', schema, map);
                },
                row: function (tbl, schema, map) {
                    return methods.table.make(tbl, 'tr', schema, map);
                },
                cell: function (row, schema, map) {
                    return methods.table.make(row, 'td', schema, map);
                }
            }
        };

        state.resize = function () {
            methods.resize.all();
        };
        //environment: Initialize
        methods.init();
    };
    $.pTableView.classes = {
        table: 'table',
        table_striped: 'table-striped',
        table_bordered: 'table-bordered',
        table_hover: 'table-hover',
        table_condensed: 'table-condensed'

    };

    //Default Settings
    $.pTableView.defaults = {
        all: true,
        colResizable: true,
        limit: 50,
        increment:{
          wheel: 1
        },
        minHeight: 0,
        maxHeight: 0,
        offset: {
            parent: 0,
            table: 0
        },
        schema: {
            header: {
                table: {
                    cls: 'table ' + $.pTableView.classes.table_bordered,
                    css: {},
                    attr: {},
                    row: {
                        cls: '',
                        css: {},
                        attr: {},
                        cell: {
                            cls: '',
                            css: {textAlign: 'center', verticalAlign: 'middle'},
                            attr: {}
                        }
                    }
                }
            },
            content: {
                table: {
                    cls: 'table ' + $.pTableView.classes.table_bordered,
                    attr: {},
                    row: {
                        cls: '',
                        css: {},
                        attr: {},
                        height: 28,
                        cell: {
                            cls: '',
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
        },
        onFiltered: function (e) {
            // data will be at e.item
        },
        filter: {
            enabled: true,
            properties: []
        },
        group:{
            enabled: false,
            properties:[]
        },
        sort: {
            enabled: false,
            properties: []
        },
        search: {
            enabled: false,
            text: "Build a list",
            btn: "Add",
            properties: []
        },
        mapping:[
            {
                header:{title: 'H1', property: 'title', cls: "some-class", css:{display: 'block'}, attr: {id: '12345'}, callback: function (node, map) {}},
                property: 'title', cls: "some-class", css:{display: 'block'}, attr: {id: '12345'}, callback: function (node, item, value, map) {}
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
    $.fn.pTableView = function(method, options) {
        if (method === undefined) method = {};
        if (typeof method === "object") {
            return new $.pTableView($(this),method);
        } else {
            // Helper strings to quickly perform functions
            var $pTableView = $(this).data('pTableView');
            switch (method) {
                case 'exists': return (null!==$pTableView && undefined!==$pTableView && $pTableView.length>0);break;
                case 'resize': $pTableView.resize();break;
                case 'state':
                default: return $pTableView;break;
            }
        }
    };

    $.pTableView.call= function(elem, options){
        elem.pTableView(options);
    };

    try {
        $.htmlEngine.plugins.register("pTableView", $.pTableView.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
