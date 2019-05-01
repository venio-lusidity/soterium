

;(function ($) {

    //Object Instance
    $.jFilterBarPanel = function(el, options) {
        var state = el,
            methods = {};
        state.opts = $.extend({}, $.jFilterBarPanel.defaults, options);
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.bodyId = $.jCommon.getRandomId("fb-body-id");

        // Store a reference to the environment object
        el.data("jFilterBarPanel", state);

        // Private environment methods
        methods = {
            init: function() {
                state.started = true;
                if(state.body) {
                    var id = state.body.attr('cover-id');
                    if (id) {
                        var cover = $('#' + id);
                        if (cover) {
                            cover.remove();
                        }
                    }
                }
                state.addClass('filter-bar-panel');
                methods.reset();
                state.opts.glyph = state.opts.glyphs[state.opts.view];
                if(state.opts.data.title) {
                    state.opts.title = state.opts.data.title;
                }
                methods.make();
                if(state.opts.actions && state.header && state.opts.filter){
                    state.header.jActions({actions: state.opts.actions});
                }
                function set() {
                    var h = state.parent().availHeight();
                    state.css({
                        minHeight: h + 'px',
                        height: h + 'px',
                        maxHeight: h + 'px',
                        padding: '0 0 0 0',
                        overflow: 'hidden'
                    });
                    h = state.body.availHeight(15);
                    state.body.css({
                        minHeight: h + 'px',
                        height: h + 'px',
                        maxHeight: h + 'px',
                        padding: '10px 0 10px 0',
                        overflowX: 'hidden',
                        overflowY: 'auto'
                    });

                    if(state.opts.fill){
                        methods.resize();
                    }
                    methods.adjustTable();
                }
                set();
                methods.html.bind();
                methods.data.setLimit();
                // load init data
                methods.get(0);
                lusidity.environment('onResize', function () {
                    set();
                });
                $.each(state.groups, function () {
                    var grp = $('li[data-sel="' + $.jCommon.string.makeKey(this.label) + '"]');
                    grp.click();
                });
                state.started = false;
            },
            changeView: function () {
                methods.html.init(state.paging);
            },
            reset: function () {
                state.paging = {
                    excluded: 0,
                    start: 0,
                    hits: 0,
                    count: 0,
                    results: []
                };
                state.groups = (state.opts.settings && state.opts.settings.groups) ? state.opts.settings.groups : [];
                state.refine = (state.opts.settings && state.opts.settings.refine) ? state.opts.settings.refine : [];
                state.children().remove();
                state.header = null;
                state.panel = null;
                state.body = null;
            },
            adjustTable: function () {
                var tbl = state.body.find('table');
                if(tbl) {
                    var ac = tbl.width();
                    var ex = state.body.width();
                    if (ac > ex) {
                        state.body.css({overflowX: 'auto'});
                    }
                    else {
                        state.body.css({overflowX: 'hidden'})
                    }
                }
            },
            data: {
                group: function (data) {
                    if(state.groups && state.groups.length>0){
                        function getResults(expected, items, map, results){
                            var unknowns = [];
                            $.each(items, function () {
                                var value = $.jCommon.json.getProperty(this, map.property);
                                if($.jCommon.is.array(value)){
                                    console.log('filter bar cannot group arrays yet');
                                }
                                else{
                                    var v2 = $.jCommon.is.object(value) ? (value.value) ? value.value : value.title : value;
                                    if($.jCommon.string.empty(v2)){
                                        v2 = "Unknown";
                                    }
                                    if($.jCommon.string.equals(expected, v2, true)){
                                        results.push(this);
                                    }
                                }
                            });
                        }

                        function find(grouped, item, items, map) {
                            var v = $.jCommon.json.getProperty(item, map.property);
                            if($.jCommon.string.empty(v)){
                                v = 'Unknown';
                            }
                            var key = $.jCommon.string.makeKey(v);
                            var altLabel;
                            if($.isFunction(map.callback)){
                                altLabel = map.callback(v);
                            }
                            if(!grouped[key]){
                                grouped[key] ={
                                    label: altLabel ? altLabel : v,
                                    grouped: {},
                                    results: []
                                };
                                getResults(v, items, map, grouped[key].results);
                            }
                            if(!grouped[key]){
                                grouped[key] ={
                                    label: altLabel ? altLabel : v,
                                    grouped: {},
                                    results: []
                                };
                                getResults(v, items, map, grouped[key].results);
                            }
                        }

                        function group(result, items, idx) {
                            var on = 0;
                            if(idx===0) {
                                result.grouped ={};
                            }
                            var len = state.groups.length;
                            $.each(state.groups, function () {
                                if(on===idx) {
                                    result.grouped.name = this.label;
                                    result.grouped.property = this.property;
                                    var map = this;
                                    // first group all known items.
                                    $.each(items, function () {
                                        find(result.grouped, this, items, map);
                                    });
                                    // group all unknowns.
                                    if(len>(idx+1)){
                                        $.each(result.grouped, function (key, value) {
                                            if($.jCommon.is.object(value)) {
                                                value.grouped = {};
                                                group(value, value.results, (idx + 1));
                                            }
                                        });
                                    }
                                    return false;
                                }
                                on++;
                            });
                        }

                        var result = {};
                        group(result, state.paging.results, 0);
                        return result;
                    }
                    return data;
                },
                preprocess: function (data) {
                    if(data && data.results &&  data.results.length > 0) {
                        // Keep all the original data as it comes in
                        state.paging.hits = data.hits;
                        state.paging.start = data.next;
                        state.paging.excluded += data.excluded ? data.excluded : 0;
                        $.each(data.results, function () {
                            var a = true;
                            var item = this;
                            // ensure no duplicates
                            $.each(state.paging.results, function () {
                                a = !$.jCommon.string.equals(this[state.KEY_ID], item[state.KEY_ID]);
                                return a;
                            });
                            if (a) {
                                state.paging.results.push(this);
                            }
                        });
                        state.paging.count = state.paging.results.length;
                    }
                    return methods.data.group(state.paging);
                },
                setLimit: function () {
                    var av = state.body.availHeight(0);
                    // ensure scrolling is possible for paging by adding one extra row;
                    if($.jCommon.string.equals(state.opts.view, 'tiles')) {
                        var c = 100;
                        var y = (Math.floor(Math.round(av / c))) + 1;
                        y = (y < 2) ? 3 : y;
                        var w = state.body.width();
                        var x = Math.floor(Math.round(w / c));
                        state.opts.limit = y * x;
                    }
                    else{
                        var l = Math.floor(Math.round(av/40));
                        state.opts.limit = l<50?50:l;
                    }
                }
            },
            refine:{
                add: function (node, item, del) {
                    var r = dCrt('div').addClass('filter-sel blue').css({cursor: 'pointer'});
                    var b1= dCrt('div').append(item.label);
                    var b2= dCrt('span').addClass('glyphicon glyphicon-remove font-red-med');
                    node.append(r.append(b1).append(b2));
                    r.on('click', function () {
                        if($.isFunction(del)){
                            r.remove();
                            del(node, item);
                        }
                    });
                },
                init: function () {
                    if(state.opts.filter) {
                        if (state.filterBar) {
                            state.filterBar.remove();
                        }
                        state.filterBar = dCrt('div').addClass('filter-bar-w');
                        state.header.append(state.filterBar);
                        if (state.opts.lists.groups && state.opts.lists.groups.length > 0) {
                            methods.refine.group();
                        }
                        methods.refine.refine();
                    }
                },
                btn: function (txt) {
                    var r = dCrt('div').addClass('dropdown').css({display: 'inline-block'});
                    var b1= dCrt('button').addClass('btn btn-default dropdown-toggle')
                        .attr('type', 'button').attr('data-toggle', "dropdown")
                        .attr('aria-haspopup', 'true').attr('aria-expanded', 'true').html(txt + '&nbsp;');
                    var s1 = dCrt('span').addClass('caret');
                    //var s2 = dCrt('span').addClass('sr-only').html('Toggle Down');

                    return r.append(b1.append(s1)); //r.append(b1).append(b2.append(s1).append(s2));
                },
                group: function () {
                    var w = dCrt('div').css({marginBottom: '5px'});
                    var r = dCrt('div');
                    state.groupBtn = methods.refine.btn('Group By');
                    var u = dCrt('ul').addClass('dropdown-menu');
                    state.filterBar.append(w.append(r).append(state.groupBtn.append(u)));
                    $.each(state.opts.lists.groups, function () {
                        if(this.hidden) {
                            return true;
                        }
                        var item = this;
                        var l = dCrt('li').attr('data-sel', $.jCommon.string.makeKey(this.label));
                        var a = dCrt('a').attr('href', '#').addClass('dropdown-toggle').html(item.label);
                        u.append(l.append(a));
                        l.on('click', function () {
                            l.hide();
                            if(!state.started) {
                                state.groups.push(item);
                            }
                            var del = function (node, data) {
                                var tmp = [];
                                $.each(state.groups, function () {
                                    if(this.label !== data.label){
                                        tmp.push(this);
                                    }
                                });
                                state.groups = tmp;
                                methods.html.init(state.paging);
                                l.show();
                            };
                            methods.refine.add(r, item, del);
                            if(!state.started) {
                                methods.html.init(state.paging);
                            }
                        });

                    });
                },
                refine: function () {
                    if (state.opts.filters) {
                        state.refine = [];
                        var w = dCrt('div').css({marginBottom: '5px'});
                        var r = dCrt('div');
                        var c = methods.refine.btn('Refine');

                        var u = dCrt('ul').addClass('dropdown-menu');
                        state.filterBar.append(w.append(r).append(c.append(u)));
                        $.each(state.opts.lists.groups, function () {
                            if(this.hidden) {
                                return true;
                            }
                            var item = this;
                            var l = dCrt('li');
                            var a = dCrt('a').attr('href', '#').addClass('dropdown-toggle').html(item.label);
                            u.append(l.append(a));
                            l.on('click', function (e) {
                                e.stopPropagation();
                                var init = false;
                                var values = [];
                                $.each(state.paging.results, function () {
                                    var v = $.jCommon.json.getProperty(this, item.property);
                                    if (item.getValue && $.isFunction(item.getValue)) {
                                        v = item.getValue(item, value);
                                    }
                                    if (v && !$.jCommon.array.contains(values, v)) {
                                        values.push(v);
                                    }
                                });
                                values.sort();
                                var sub = dCrt('ul');
                                l.append(sub);
                                $.each(values, function () {
                                    var value = {original: item.label, label: item.label + ":" + this};
                                    var li = dCrt('li');
                                    var ai = dCrt('div').html(value.label);
                                    ui.append(li.append(ai));
                                    li.on('click', function () {
                                        state.refine.push(value);
                                        var del = function (node, data) {
                                            var tmp = [];
                                            $.each(state.refine, function () {
                                                if (this.original !== data.original) {
                                                    tmp.push(this);
                                                }
                                            });
                                            state.refine = tmp;
                                        };
                                        methods.refine.add(r, item, del);
                                    })
                                });
                            });
                        });
                    }
                }
            },
            header: function() {
                function make() {
                    if(!state.header.head){
                        state.header.head = dCrt('div').addClass('panel-title').css({
                            position: 'relative',
                            top: '-4px',
                            marginLeft: '35px'
                        });
                    }
                    state.header.head.children().remove();
                    state.asOf = dCrt('span');
                    var c = dCrt('span').html(state.paging.count  + " of " + state.paging.hits +
                        ((state.paging.count===state.paging.hits) ? '' :
                            (((state.paging.count+state.paging.excluded)>=state.paging.hits) ? ' (' + state.paging.excluded + ' excluded)' : '')));
                    var count = dCrt('div').append(c).css({fontSize: '10px'});
                    if(!state.opts.realTime && state.asOf) {
                        c.append('as of ').append(state.asOf);
                    }
                    var title = dCrt('div').html(state.opts.data.title);
                    return state.header.head.append(count).append(title);
                }
                if (!state.header) {
                    state.header = $(document.createElement('div')).addClass('panel-heading');
                    var c = dCrt('div').css({marginBottom: '5px'});
                    var icon = dCrt('div').addClass(state.opts.glyph).css({
                        position: 'absolute',
                        top: '13px',
                        fontSize: '24px'
                    });
                    state.header.append(c.append(make()).append(icon));
                    methods.refine.init();
                }
                else{
                    make();
                }
            },
            make: function(){
                state.panel = $(document.createElement('div')).addClass(state.opts.noHead ? '': 'panel panel-default').css({marginBottom: '0'});
                state.append(state.panel);
                if(!state.opts.noHead) {
                    methods.header();
                }
                state.body = $(document.createElement('div')).attr('fb-body-id', state.bodyId).addClass('panel-body-content' + (state.opts.noHead ? '': ' panel-body'));
                if(state.opts.content) {
                    state.body.append(state.opts.content);
                }
                if(state.opts.body && state.opts.body.attr){
                    $.htmlEngine.addAttributes(state.opts.body, state.body);
                }
                if(state.opts.body && state.opts.body.css){
                    $.htmlEngine.addStyling(state.opts.body, state.body);
                }
                if(state.opts.body && state.opts.body.cls){
                    state.body.addClass(state.opts.body.cls);
                }
                state.panel.append(state.header).append(state.body);
                if(!state.opts.borders && !state.opts.noHead){
                    state.panel.addClass('no-border');
                    state.body.addClass('no-border');
                }
            },
            resize: function(){
                var h = state.innerHeight();
                state.panel.css({minHeight: h+ 'px', height: h+ 'px'});
                if(state.header){
                    var hOffset = state.header.outerHeight();
                    h -= hOffset;
                }
                state.body.css({minHeight: h+ 'px', height: h+ 'px'});
            },
            get: function (scrlTop) {
                if(state.paging.start<state.paging.hits || state.paging.hits===0) {
                    var url = state.opts.getUrl(state.opts.data, state.paging.start, state.opts.limit);
                    if (null !== url) {
                        $.htmlEngine.busy(state.body, {type: 'cube', cover: true, adjustWidth: 0, adjustHeight: 0});
                        var s = function (data) {
                            if (data && data.results && data.results.length > 0) {
                                methods.html.init(data, scrlTop);
                            }
                            else {
                                state.body.loaders('hide');
                            }
                        };
                        var f = function () {
                            state.body.loaders('hide');
                        };

                        if ($.jCommon.is.string(url)) {
                            $.htmlEngine.request(url, s, f, null, 'get', true);
                        }
                        else {
                            $.htmlEngine.request(url.url, s, f, url.data, 'post', true);
                        }
                    }
                    else{
                        methods.html.init(state.opts.data, 0);
                    }
                }
            },
            html: {
                init: function (data, scrlTop) {
                    if (!$.jCommon.is.numeric(scrlTop)) {
                        scrlTop = 0;
                    }
                    try {
                        var processed = methods.data.preprocess(data);
                        methods.header();
                        if (state.asOf && data.createdWhen) {
                            var dt = $.jCommon.dateTime.fromString(data.createdWhen);
                            state.asOf.html($.jCommon.dateTime.defaultFormat(dt));
                        }
                        if (state.opts.groupsEnabled && state.groups && state.groups.length > 0) {
                            methods.html.groups(processed, state.body);
                        }
                        else {
                            methods.html[state.opts.view].init(processed, state.body, scrlTop);
                        }
                    }
                    catch (e) {
                        console.log(e);
                    }

                    window.setTimeout(function () {
                        state.body.scrollTop(scrlTop);
                        state.body.loaders('hide');
                    }, 300);
                },
                groups: function (data, node) {
                    try {
                        node.children().remove();
                        var values = [];
                        function make(item, txt) {
                            var t = txt;
                            var p = false;
                            if (item && item.grouped) {
                                var keys = $.jCommon.json.getSortedKeyArray(item.grouped);
                                $.each(keys, function () {
                                    var key = this.key;
                                    var value = item.grouped[key];
                                    p = true;
                                    if (!$.jCommon.string.equals(key, ['label', 'property', 'results', 'name'])) {
                                        var v = ((t.length > 0) ? t + ' > ' : '') + value.label;
                                        make(value, v);
                                    }
                                });
                            }
                            if (!p) {
                                values.push({label: t, data: item});
                            }
                        }

                        make(data, '');
                        $.each(values, function () {
                            var c = dCrt('div').addClass('group-node blue').html(this.label + ' (' + this.data.results.length + ')');
                            var w = dCrt('div');
                            node.append(c).append(w);
                            methods.html[state.opts.view].init(this.data, w);
                        });
                    }
                    catch (e){
                        console.log(e);
                    }
                },
                bind: function () {
                    if(state.opts.realTime) {
                        var adjust = state.body.height() / 2;
                        adjust = adjust > 50 ? 50 : adjust;
                        state.body.scrollHandler({
                            adjust: 0,
                            delay: 10,
                            start: function () {
                            },
                            stop: function (scrollTop, isBottom) {
                            },
                            top: function () {
                            },
                            bottom: function () {
                                methods.get(state.body.scrollTop());
                            }
                        });
                    }
                },
                tiles:{
                    init: function (data, container, scrlTop) {
                        if(state.groupBtn) {
                            state.groupBtn.find('.btn').attr('disabled', 'disabled');
                        }
                        var opt = state.opts.tiles;
                        opt.data = $.extend(true, {}, data);
                        if(scrlTop<=0) {
                            container.children().remove();
                            if(state.content){
                                state.content.remove();
                            }
                            state.content = dCrt('div').css({marginBottom: "50px"});
                            container.append(state.content);
                            state.content.tiles(opt);
                        }
                        else{
                            state.content.tiles('addAll', opt);
                            state.body.scrollHandler('start');
                        }
                    }
                },
                details:{
                    init: function (data, container, scrlTop) {
                        try {
                            if (state.groupBtn) {
                                state.groupBtn.find('.btn').removeAttr('disabled');
                            }
                            container.children().remove();
                            var opt = state.opts.details;
                            opt.data = data;
                            var c = dCrt('div');
                            container.append(c);
                            c.on("tableViewDataLoaded", function () {
                                methods.adjustTable();
                            });
                            c.pTableView(opt);
                            window.setTimeout(function () {
                                if (scrlTop > 0) {
                                    state.body.scrollTop(scrlTop);
                                }
                            }, 500);
                            state.body.scrollHandler('start');
                        }
                        catch(e){
                            console.log(e);
                        }
                    }
                },
                panel: function(container, glyph, title, url, borders, actions, menu){
                    var result = dCrt('div');
                    var options = {
                        glyph: glyph,
                        title: title,
                        url: url,
                        borders: borders,
                        content: result,
                        body:{
                            css: {padding: 0}
                        },
                        actions: actions ? actions : [],
                        menu: menu
                    };
                    container.panel(options);
                    return result;
                }
            },
            style: function (actual, expected, styleIt) {
                if(styleIt) {
                    var cs = window.getComputedStyle(actual[0], null);
                    if (cs) {
                        // add css styles
                    }
                }
            }
        };
        //public methods
        state.update = function (content){
            state.body.children().remove();
            state.body.append(content)
        };
        state.updateHeader = function (options){
            state.opts = $.extend({}, state.opts, options);
            methods.header();
        };
        state.getHeader = function () {
          return state.header;
        };
        state.getBody = function () {
            return state.body;
        };
        state.add = function (content){
            state.body.append(content);
        };
        state.changeView = function (options) {
            state.opts = $.extend({}, $.jFilterBarPanel.defaults, options);
            methods.changeView();
        };
        state.load = function (options) {
            state.opts = $.extend({}, $.jFilterBarPanel.defaults, options);
            methods.init();
        };
        state.settings = function () {
          return {groups: ((!state.groups) ? [] : state.groups), refine: ((!state.refine) ? [] : state.refine)};
        };
        state.simple = function (opt) {
            var t = dCrt('table');
            var tbl = opt.table;
            methods.style(tbl, t, opt.styleIt);
            var th = tbl.find('thead');
            if(th.length>0) {
                var rTh = dCrt('thead');
                t.append(rTh);
                methods.style(th, rTh, opt.styleIt);
                $.each(th.find('tr'), function () {
                    var rR = dCrt('tr');
                    rTh.append(rR);
                    methods.style($(this), rR, opt.styleIt);
                    $.each($(this).find('th'), function () {
                       var rRTH = dCrt('th').html(this.innerHTML);
                        methods.style($(this), rRTH, opt.styleIt);
                        rR.append(rRTH);
                    });
                });
            }
            var tb = tbl.find('tbody');
            if(tb.length>0) {
                var rTb = dCrt('tbody');
                t.append(rTb);
                methods.style(tb, rTb, opt.styleIt);
                $.each(tb.find('tr'), function () {
                    var rR = dCrt('tr');
                    methods.style($(this), rR, opt.styleIt);
                    rTb.append(rR);
                    $.each($(this).find('td'), function () {
                        var rTd = dCrt('td').html(this.innerHTML);
                        methods.style($(this), rTb, opt.styleIt);
                        rR.append(rTd);
                    });
                });
            }
            return t;
        };
        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.jFilterBarPanel.defaults = {
        groupsEnabled: true,
        limit: 100,
        glyphs:{
            details: 'glyphicons glyphicons-table',
            tiles: 'glyphicons glyphicons-show-thumbnails'
        },
        borders: false,
        content: null,
        sortOn: []
    };


    //Plugin Function
    $.fn.jFilterBarPanel = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.jFilterBarPanel($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $jFilterBarPanel = $(this).data('jFilterBarPanel');
            if($jFilterBarPanel) {
                switch (method) {
                    case "settings":
                        return $jFilterBarPanel.settings(options);
                        break;
                    case "simple":
                        return $jFilterBarPanel.simple(options);
                        break;
                    case 'updateHeader':
                        $jFilterBarPanel.updateHeader(options);
                        break;
                    case 'getHeader':
                        return $jFilterBarPanel.getHeader();
                        break;
                    case 'getBody':
                        return $jFilterBarPanel.getBody();
                        break;
                    case 'update':
                        $jFilterBarPanel.update(options);
                        break;
                    case 'add':
                        $jFilterBarPanel.add(options);
                        break;
                    case 'exists':
                        return (null !== $jFilterBarPanel && undefined !== $jFilterBarPanel && $jFilterBarPanel.length > 0);
                    case 'load':
                        $jFilterBarPanel.load(options);break;
                    case 'changeView':
                        $jFilterBarPanel.changeView(options);break;
                    case 'state':
                    default:
                        return $jFilterBarPanel;
                }
            }
            else{
                console.log('Cannot find jFilterBarPanel.');
            }
        }
    };

})(jQuery);
