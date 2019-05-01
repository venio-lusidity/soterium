

;(function ($) {

    //Object Instance
    $.tableView2 = function(el, options) {
        var state = el,
            methods = {};
        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.tableView2.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : 'tableView2');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.started = false;
        state.on = 0;

        // Store a reference to the environment object
        el.data("tableView2", state);

        // Private environment methods
        methods = {
            init: function() {
                if (state.opts.paging) {
                    state.opts.paging = {start:0,hits:1};
                }
                $.htmlEngine.loadFiles(state.worker.propertyNode, state.opts.name, state.opts.cssFiles);
                if($.jCommon.is.object(state.opts.query)){
                    state.opts.query = {
                        domain: state.opts.query.edgeType,
                        type: state.opts.query.vertexType,
                        lid: state.opts.query.parentLid,
                        format: 'discovery',
                        direction: state.opts.query.direction,
                        totals: true,
                        'native': state.opts.query.query
                    };
                    methods.get();
                }
                else if(!$.jCommon.is.empty(state.opts.propertyUrl)){
                    methods.get();
                }
                else{
                    methods.make();
                }
            },
            paging: function () {
                if(state.opts.paging) {
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
            get: function(container) {
                if (!state.opts.paging || (state.opts.paging.start < state.opts.paging.hits)) {
                    if(container) {
                        $.htmlEngine.busy(container, {type: 'cube', cover: true, adjustWidth: 0, height: container.height()});
                    }

                    var s = function (data) {
                        if(container) {
                            container.loaders('hide');
                        }
                        if (data) {
                            state.opts.paging.hits = data.hits;
                            state.opts.paging.start = data.next===0 ? state.opts.paging.hits : data.next;
                            state.opts.data = data;
                            if (!state.started) {
                                state.started = true;
                                methods.make();
                                methods.paging();
                            }
                            else {
                                methods.getTableBody(data.results);
                            }
                            if(state.opts.loaded && $.isFunction(state.opts.loaded)){
                                state.opts.loaded(data, state.table);
                            }
                        }
                        else{
                            methods.make();
                        }
                        try {
                            state.node.scrollHandler('start');
                        }catch (e){}

                        if(container) {
                            container.loaders('hide');
                        }
                    };
                    var f = function () {
                        methods.make();
                        if(container) {
                            container.loaders('hide');
                        }
                    };
                    var n = (state.opts.paging) ? state.opts.paging.start : 0;
                    if(state.opts.query) {
                        var url = methods.getQueryUrl(n, state.opts.limit);
                        $.htmlEngine.request(url, s, f, state.opts.query, "post");
                    }
                    else if(state.opts.propertyUrl){
                        var url = methods.getPropertyUrl(n, state.opts.limit);
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
            make: function () {
                state.worker.node.attr('data-valid', true).show();
                if(state.opts.data) {
                    var data = (state.opts.data.results) ? state.opts.data.results : state.opts.data;
                    state.node = dCrt("div");
                    if(state.opts.paging){
                        var h = state.worker.node.height();
                        state.node.addClass("scrollable").css({height: h, maxHeight: h});
                    }
                    state.worker.node.append(state.node);
                    state.table = $(document.createElement('table')).css({width: '100%'}).addClass(state.opts.cls);
                    state.node.append(state.table);
                    methods.getTableHead();
                    methods.getTableBody(data);
                }
                else {
                    state.worker.node.append(dCrt('div').html('No results found.'));
                    state.worker.node.css({
                        height: '20px',
                        maxHeight: '',
                        minHeight: '',
                        overflow: 'hidden',
                        marginTop: '5px'
                    });
                }
                var event = jQuery.Event('tableView2DataLoaded');
                state.trigger(event);
            },
            getTableHead: function(){
                state.thead = $(document.createElement('thead'));
                if(state.opts.headerCls){
                    state.thead.addClass(state.opts.headerCls);
                }
                var row = $(document.createElement('tr'));
                $.each(state.opts.mapping, function(){
                    var header = this.header;
                    var th = $(document.createElement('th'));
                    row.append(th);
                    if(header.hidden){
                        th.hide();
                    }
                    if($.jCommon.string.equals(header.title, "#")){
                        th.css({minWidth: '20px', width: '20px' });
                    }
                    var c = dCrt('div').html(header.title);
                    th.append(c);
                    if($.jCommon.is.numeric(header.width)){
                        c.css({width: header.width+'px', maxWidth: header.width+'px' });
                    }
                    if(header.tip){
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
                    var item = this;
                    var row = $(document.createElement('tr'));
                    $.each(state.opts.mapping, function(){
                        var key = this.property;
                        var value;
                        var td = $(document.createElement('td')).addClass('css_' + key);
                        row.append(td);
                        if(this.hidden){
                            td.hide();
                        }
                        if($.jCommon.string.equals(key, '#')){
                            state.on++;
                            value = state.on;
                            td.css({width: '50px', maxWidth: '50px' });
                        }
                        else if(this.callback && $.isFunction(this.callback)){
                            this.callback(td, item, $.jCommon.json.getProperty(item, key));
                        }
                        else {
                            value = $.jCommon.json.getProperty(item, key);
                            value = $.htmlEngine.getString(value);
                            value = $(document.createElement('div')).html(value);
                        }
                        if($.jCommon.is.numeric(this.header.width)){
                            td.css({width: this.header.width+'px', maxWidth: this.header.width+'px' });
                        }
                        if(value) {
                            td.append(value);
                        }
                    });
                    state.tbody.append(row);
                });
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.tableView2.defaults = {
        adjust: 10,
        headerCls: 'blue',
        cls: 'table table-stripped table-bordered table-hover',
        paging: false,
        limit: 50
    };


    //Plugin Function
    $.fn.tableView2 = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.tableView2($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $tableView2 = $(this).data('tableView2');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $tableView2;
            }
        }
    };

    $.tableView2.call= function(elem, options){
        elem.tableView2(options);
    };

    try {
        $.htmlEngine.plugins.register("tableView2", $.tableView2.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
