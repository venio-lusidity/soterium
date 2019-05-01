

;(function ($) {

    //Object Instance
    $.pTableView = function(el, options) {
        var state = el,
            methods = {};
        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.pTableView.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : 'pTableView');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.started = false;
        state.on = 0;
        state.suggest = [];
        state.terms = [];
        state.initialized = false;

        // Store a reference to the environment object
        el.data("pTableView", state);

        // Private environment methods
        methods = {
            init: function () {
                if (state.opts.paging) {
                    state.opts.paging = {start: 0, hits: 1};
                }
                $.htmlEngine.loadFiles(state.worker.propertyNode, state.opts.name, state.opts.cssFiles);
                if ($.jCommon.is.object(state.opts.query)) {
                    state.opts.query = {
                        domain: state.opts.query.edgeType,
                        type: state.opts.query.vertexType,
                        lid: state.opts.query.parentLid,
                        format: 'discovery',
                        direction: state.opts.query.direction,
                        totals: true,
                        "native": state.opts.query.query
                    };
                    methods.get();
                }
                else if (!$.jCommon.is.empty(state.opts.propertyUrl)) {
                    methods.get();
                }
                else {
                    methods.make();
                }
            },
            paging: function () {
                if (state.opts.paging) {
                    state.node.scrollHandler({
                        adjust: state.opts.adjust,
                        start: function () {
                        },
                        stop: function (scrollTop, isBottom) {
                        },
                        top: function () {
                        },
                        bottom: function () {
                            methods.get(state.node);
                        }
                    });
                }
            },
            get: function (container) {
                if (!state.opts.paging || (state.opts.paging.start < state.opts.paging.hits)) {
                    if (container) {
                        $.htmlEngine.busy(container, {
                            type: 'cube',
                            cover: true,
                            adjustWidth: 0,
                            height: container.height()
                        });
                    }
                    var s = function (data) {
                        if (container) {
                            container.loaders('hide');
                        }
                        if (data) {
                            state.opts.paging.hits = data.hits;
                            state.opts.paging.start = data.next === 0 ? state.opts.paging.hits : data.next;
                            state.opts.data = data;
                            if (!state.started) {
                                methods.make();
                                methods.paging();
                            }
                            else {
                                methods.getTableBody(data.results);
                            }
                            if (state.opts.loaded && $.isFunction(state.opts.loaded)) {
                                state.opts.loaded(data, state.table, true);
                            }
                        }
                        else {
                            methods.make();
                        }
                        try {
                            state.node.scrollHandler('start');
                        } catch (e) {
                        }

                        if (container) {
                            container.loaders('hide');
                        }
                    };
                    var f = function () {
                        methods.make();
                        if (container) {
                            container.loaders('hide');
                        }
                    };
                    var n = (state.opts.paging) ? state.opts.paging.start : 0;
                    var url;
                    if (state.opts.query) {
                        url = methods.getQueryUrl(n, state.opts.limit);
                        $.htmlEngine.request(url, s, f, state.opts.query, "post");
                    }
                    else if (state.opts.propertyUrl) {
                        url = methods.getPropertyUrl(n, state.opts.limit);
                        $.htmlEngine.request(url, s, f, null, 'get');
                    }
                }
            },
            getQueryUrl: function (start, limit) {
                if (undefined === start) {
                    start = 0;
                }
                if (undefined === limit) {
                    limit = 0;
                }
                return '/query?start=' + start + '&limit=' + limit;
            },
            getPropertyUrl: function (start, limit) {
                if (undefined === start) {
                    start = 0;
                }
                if (undefined === limit) {
                    limit = 0;
                }
                return state.opts.propertyUrl + '?start=' + start + '&limit=' + limit;
            },
            search: function () {
                var grp = dCrt('div').addClass('ui-widget input-group').css({marginBottom: '5px'});
                state.search.append(grp);
                var box = dCrt('input').addClass('form-control').attr('type', 'text').attr('placeholder', (state.opts.searchText ? state.opts.searchText : 'What are you looking for...')).css({height: '34px', minWidth: '230px'});
                grp.append(box);
                var sp = dCrt('span').addClass('input-group-btn');
                grp.append(sp);
                var btn = dCrt('btn').addClass('btn btn-default').attr('title', 'Using this button will allow you to do a partial match.').attr('type', 'button').html((state.opts.buttonText ? state.opts.buttonText: "Search"));
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
                        methods.filter();
                    }
                });

                box.autocomplete({
                    minLength: 0,
                    source: function (request, response) {
                        state.suggest.sort();
                        var sgst = [];
                        var a = request.term.toString();
                        $.each(state.suggest, function () {
                            var b = this.toString();
                           if(((a===b) || $.jCommon.string.contains(b, a, true)) && !$.jCommon.array.contains(sgst, b)){
                             sgst.push(b);
                           }
                        });
                        // delegate back to autocomplete, but extract the last term
                        response(sgst);
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
                        methods.filter();
                        return false;
                    }
                });
            },
            searchable: function (that, item, key) {
                if (state.opts.search && that.searchable) {
                    var v = $.jCommon.json.getProperty(item, key);
                    if(that.callbackSearchValue && $.isFunction(that.callbackSearchValue)){
                        v = that.callbackSearchValue(item, v);
                    }
                    if(!$.jCommon.string.empty(v) && !$.jCommon.array.contains(state.suggest, v)){
                        state.suggest.push(v);
                    }
                }
            },
            filter: function () {
                state.filters.children().remove();
                if (state.terms) {
                    $.each(state.terms, function () {
                        var term = this;
                        if(!$.jCommon.string.empty(term.name)) {
                            var btn = dCrt('btn').addClass('btn btn-default blue').attr('title', 'Remove filter').attr('type', 'button');
                            btn.append(dCrt('span').html(term.name)).css({margin: '0 10px 10px 0'});
                            state.filters.append(btn);
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
                                    state.suggest.push(term.name);
                                }
                                methods.filter();
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
                        rows.css({display: ''});
                    }
                    else {
                        rows.css({display: 'none'});
                        $.each(rows, function () {
                            var found = false;
                            var row = $(this);
                            $.each(row.children(), function () {
                                var html = $(this).html();
                                $.each(state.terms, function () {
                                    var term = this;
                                    found = $.jCommon.string.contains(html, term.name, true);
                                    if (found) {
                                        return false;
                                    }
                                });
                                if (found) {
                                    row.css({display: ''});
                                    return false;
                                }
                            });
                        });
                    }

                    state.trigger('table-view-filtered');
                }
            },
            make: function () {
                if(!state.started && state.opts.before && $.isFunction(state.opts.before)){
                    state.opts.data = state.opts.before(state.opts.data);
                }
                state.worker.node.attr('data-valid', true).show();
                if(state.opts.data) {
                    if(state.opts.search){
                        var row = dCrt('div').addClass('row').css({marginBottom: '10px'});
                        state.search = dCrt('div').addClass('col-lg-12');
                        state.worker.node.append(row.append(state.search));
                        state.filters = dCrt('div').addClass('col-lg-12');
                        row.append(state.filters);
                        methods.search();
                    }
                    state.data = (state.opts.data.results) ? state.opts.data.results : state.opts.data;
                    if(!state.initialized) {
                        state.node = dCrt("div");
                        if (state.opts.paging) {
                            var h = state.worker.node.height();
                            state.node.addClass("scrollable").css({height: h, maxHeight: h});
                        }
                        state.worker.node.append(state.node);
                        state.table = $(document.createElement('table')).css({width: '100%'}).addClass(state.opts.cls);
                        state.node.append(state.table);
                        methods.getTableHead();
                        methods.getTableBody(state.data);
                        state.trigger('table-view-created');
                        state.initialized=true;
                    }
                    else{
                        methods.getTableBody(state.data);
                    }
                }
                else {
                    if (state.opts.loaded && $.isFunction(state.opts.loaded)) {
                        state.opts.loaded(data, state.worker.node, false);
                    }
                    state.worker.node.append(dCrt('div').html('No results found.'));
                    state.worker.node.css({
                        height: '20px',
                        maxHeight: '',
                        minHeight: '',
                        overflow: 'hidden',
                        marginTop: '5px'
                    });
                }
                var event = jQuery.Event('pTableViewDataLoaded');
                state.trigger(event);
                state.started = true;
            },
            getTableHead: function(){
                state.thead = $(document.createElement('thead'));
                if(state.opts.headerCls){
                    state.thead.addClass(state.opts.headerCls);
                }
                var row = $(document.createElement('tr'));
                $.each(state.opts.mapping, function(){
                    var header = this.header;
                    var th = $(document.createElement('th')).css({overflow: 'hidden'});
                    row.append(th);
                    if(header.hidden){
                        th.hide();
                    }
                    if($.jCommon.string.equals(header.title, "#")){
                        th.css({minWidth: '20px', width: '20px' });
                    }
                    var c = dCrt('div').css({position: 'relative'}).html(header.title);
                    c.jNodeReady({onReady: function (node) {
                        if(header.sortable || header.autoSize) {
                            var a = $(th).width();
                            var e = (header.title.width()+ (header.sortable ? 40 : 0));
                            if (a < e || header.autoSize) {
                                var w = (e + 40) + 'px';
                                $(th).css({minWidth: w, width: w});
                            }
                        }
                    }});
                    th.append(c);
                    if(state.opts.sort) {
                        th.css({'position': 'relative', textAlign: 'left'});
                        if(th.width){
                            th.width+=40;
                        }
                    }
                    if(state.opts.sort && header.sortable){
                        var d = 'glyphicon glyphicon-triangle-bottom';
                        var u = 'glyphicon glyphicon-triangle-top';
                        var cs = {fontSize: '16px', color: '#5f5f5f'};
                        var id = $.jCommon.getRandomId("hd");
                        var n = dCrt('div').attr('id', id).addClass("cell-sortable").css({position: 'absolute', textAlign: 'right', right: '5px', width: '32px', bottom: '5px', cursor: 'pointer', fontSize: '16px', color: '#c3c3c3'});
                        th.append(n);
                        var a = $.htmlEngine.glyph(u);
                        var b = $.htmlEngine.glyph(d);
                        n.append(a).append(b);
                        n.data('_header', header);
                        function toggle() {
                            var srts = row.find('.cell-sortable');
                            if(srts) {
                                $.each(srts, function () {
                                    var srt = $(this);
                                    var act = srt.attr('id');
                                    if (!act || id !== act) {
                                        srt.css({color: '#c3c3c3'});
                                        srt.data('_header').dir = 'none';
                                        srt.children().show();
                                    }
                                });
                            }
                            if(header.dir === 'none'){
                                n.css(cs);
                                header.dir = 'asc';
                                a.show();
                                b.hide();
                            }
                            else if(header.dir === 'asc'){
                                n.css(cs);
                                header.dir = 'desc';
                                a.hide();
                                b.show();
                            }
                            else{
                                n.css({color: '#c3c3c3'});
                                header.dir = 'none';
                                a.show();
                                b.show();
                            }

                            n.attr('title', header.dir);
                        }
                        n.on('click', function () {
                            toggle();
                            methods.sort(header);
                        });
                        toggle();
                    }
                    if($.jCommon.is.numeric(header.width)){
                        c.css({width: header.width+'px', maxWidth: header.width+'px' });
                    }
                    if(header.tip){
                        th.css({overflow: ''});
                        c.attr("data-toggle","tooltip").attr("data-placement", "bottom").attr("title", header.tip);
                        c.tooltip();
                    }
                    if($.isFunction(header.callback)){
                        header.callback(th, c);
                    }
                });
                state.thead.append(row);
                state.table.append(state.thead);
            },
            getTableBody: function(data){
                state.tbody = state.table.find('tbody');
                if(state.tbody.length<1) {
                    state.tbody = $(document.createElement('tbody'));
                    state.table.append(state.tbody);
                }

                $.each(data, function(){
                    try {
                        var item = this;
                        var row = $(document.createElement('tr'));
                        $.each(state.opts.mapping, function () {
                            var key = this.property;
                            var td = $(document.createElement('td')).addClass('css_' + key).attr('data-key', key);
                            row.append(td);
                            try {
                                //var key = this.property;
                                var value;
                                if (this.hidden) {
                                    td.hide();
                                }
                                if ($.jCommon.string.equals(key, '#')) {
                                    state.on++;
                                    value = state.on;
                                    td.css({width: '50px', maxWidth: '50px'});
                                }
                                else if ($.jCommon.string.equals(key, 'cbox')) {
                                    var node = dCrt('input').attr('type', 'checkbox');
                                    td.append(node);
                                    td.css({width: '20px', maxWidth: '20px', position: 'relative', overflow: 'hidden'});
                                    if (this.callback && $.isFunction(this.callback)) {
                                        this.callback(td, item, node.checked);
                                    }
                                    var action = this.action;
                                    var s = '50px';
                                    var fill = dCrt('div').css({
                                        position: 'absolute',
                                        cursor: 'pointer',
                                        minWidth: s,
                                        width: s,
                                        height: s,
                                        minHeight: s,
                                        top: 0,
                                        left: 0
                                    });
                                    td.append(fill);
                                    fill.on('click', function (e) {
                                        if (node.is(':checked')) {
                                            node.prop('checked', false);
                                        }
                                        else {
                                            node.prop('checked', true);
                                        }
                                        if (action && $.isFunction(action)) {
                                            action(td, node, item);
                                        }
                                    });
                                }
                                else if ($.jCommon.string.equals(key, 'btn')) {
                                    var node = dCrt('button').addClass('btn btn-default').attr('role', 'button');
                                    if (this.label) {
                                        node.html(this.label);
                                    }
                                    td.append(node);
                                    if (this.callback && $.isFunction(this.callback)) {
                                        this.callback(td, item, node.checked);
                                    }
                                    var action = this.action;
                                    node.on('click', function (e) {
                                        if (action && $.isFunction(action)) {
                                            action(td, node, item);
                                        }
                                    });
                                }
                                else if (this.callback && $.isFunction(this.callback)) {
                                    methods.searchable(this, item, key);
                                    this.callback(td, item, $.jCommon.json.getProperty(item, key));
                                }
                                else {
                                    value = $.jCommon.json.getProperty(item, key);
                                    value = $.htmlEngine.getString(value);
                                    methods.searchable(this, item, key);
                                    value = $(document.createElement('div')).html(value);
                                }
                                if ($.jCommon.is.numeric(this.header.width)) {
                                    td.css({width: this.header.width + 'px', maxWidth: this.header.width + 'px'});
                                }
                                if (value) {
                                    td.append(value);
                                }
                            }
                            catch (e){}
                        });
                        state.tbody.append(row);
                        row.item =item;
                    }
                    catch (e){
                        console.log(e);
                    }
                });
            },
            sort: function (header) {
                state.opts.sortOn = [];
                state.on=0;
                var data = $.jCommon.array.clone(state.data);
                if(header.dir !== 'none'){
                    state.on++;
                    state.opts.sortOn.push({property: header.property, asc: ('asc'===header.dir)});
                    data = $.jCommon.array.sort(data, state.opts.sortOn);
                }
                state.tbody.remove();
                state.tbody=null;
                methods.getTableBody(data);
                if(state.opts.search) {
                    methods.filter();
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.pTableView.defaults = {
        adjust: 10,
        headerCls: 'blue',
        cls: 'table table-stripped table-bordered table-hover',
        paging: false,
        limit: 50,
        sortOn: []
    };


    //Plugin Function
    $.fn.pTableView = function(method, options) {
        if (method === undefined) method = {};
        if (typeof method === "object") {
            return this.each(function() {
                new $.pTableView($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $pTableView = $(this).data('pTableView');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $pTableView;
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
