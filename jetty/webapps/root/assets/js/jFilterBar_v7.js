;(function ($) {

    //Object Instance
    $.jFilterBar = function(el, options) {
        var state = el,
            methods = {};
        state.opts = $.extend({}, $.jFilterBar.defaults, options);
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        state.bodyId = $.jCommon.getRandomId("fb-body-id");
        var _groups = [];
        var _nodes = [];
        var _groupings;
        var _isGrouped = false;
        var _started;
        var _stateId;
        var _grids = [];
        var _grpData = {};
        var _on = 0;
        var _total = 0;
        var _loaded = false;

        // Store a reference to the environment object
        el.data("jFilterBar", state);

        // Private environment methods
        methods = {
            init: function() {
                _started = true;
                $.jCommon.load.script('/assets/js/jFilterBarGrp.js', function () {
                    _stateId = $.jCommon.getRandomId('filter_bar');
                    state.attr('id', _stateId);
                    state.on('scroller-enabled', function () {
                        state.scroller({enabled: true});
                    });
                    state.on('scroller-disabled', function () {
                        state.scroller({enabled: false});
                    });
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
                    state.opts.glyph = state.opts.glyphs[state.opts.view];
                    methods.html.init();
                    function start() {
                        methods.resize.init();
                        methods.html[state.opts.view]();
                        if(state.opts.actions && state.nodeHdr){
                            state.nodeHdr.jActions({actions: state.opts.actions});
                        }
                        $.each(_groups, function () {
                            var grp = $('li[data-sel="' + $.jCommon.string.makeKey(this.label) + '"]');
                            grp.click();
                        });
                        _started = false;
                        lusidity.environment('onResize', function () {
                            methods.resize.init();
                        });
                    }
                    window.setTimeout(function () {
                        start();
                    }, 500);
                }, true);
            },
            changeView: function () {
                methods.html.init(state.paging);
            },
            resize: {
                init: function () {
                    var h = state.innerHeight();
                    state.panel.css({
                        padding: '0 0 0 0'
                    });
                    if(!_loaded){
                        h-=20;
                    }
                    if(state.nodeHdr){
                        h -= state.nodeHdr.outerHeight();
                    }
                    dHeight(state.body, h, h, h);
                    state.body.css({
                        padding: '2px 0 2px 0'
                    });
                }
            },
            refine:{
                add: function (node, item, del) {
                    var r = dCrt('div').addClass('filter-sel blue font-dark').css({position: 'relative', cursor: 'pointer'});
                    var b1= dCrt('div').append(item.label);
                    var b2= dCrt('span').addClass('glyphicons glyphicons-remove font-red-med').css({position: 'absolute', top: '4px', right: '0', fontSize: '16px'});
                    node.append(r.append(b1).append(b2));
                    r.on('click', function () {
                        if($.isFunction(del)){
                            r.remove();
                            del(node, item);
                            if(_groups.length>=2){
                                state.groupBtn.find('button').attr('disabled', 'disabled').addClass('disabled');
                            }
                            else {
                                state.groupBtn.find('button').removeAttr('disabled').removeClass('disabled');
                            }
                        }
                    });
                },
                init: function () {
                    if(state.opts.group && state.opts.group.enabled) {
                        if (state.groupBar) {
                            state.groupBar.remove();
                        }
                        state.groupBar = dCrt('div').addClass('filter-bar-w');
                        state.nodeHdr.append(state.groupBar);
                        if (state.opts.group.groups && state.opts.group.groups.length > 0) {
                            methods.refine.group();
                        }
                    }
                },
                btn: function (txt) {
                    var r = dCrt('div').addClass('dropdown').css({display: 'inline-block'});
                    var b1= dCrt('button').addClass('btn btn-default dropdown-toggle')
                        .attr('type', 'button').attr('data-toggle', "dropdown")
                        .attr('aria-haspopup', 'true').attr('aria-expanded', 'true').html(txt + '&nbsp;');
                    var s1 = dCrt('span').addClass('caret');
                    return r.append(b1.append(s1));
                },
                getDistinct: function(str, prt, map, callback){
                    var s = function (data) {
                        if($.isFunction(callback)) {
                            callback(data);
                        }
                    };
                    var url = String.format('/refine/{0}/{1}/properties/distinct', str, prt);
                    $.htmlEngine.request(url, s, s, map, 'post', true);
                },
                group: function () {
                    function callback(data) {
                        _groupings = data;
                        var w = dCrt('div').css({marginBottom: '5px'});
                        var r = dCrt('div');
                        state.groupBtn = methods.refine.btn('Group By');
                        var u = dCrt('ul').addClass('dropdown-menu');
                        state.groupBar.append(w.append(r).append(state.groupBtn.append(u)));
                        _groupings.results = $.jCommon.array.sort(_groupings.results, [{property: "label", asc: true}]);
                        $.each(_groupings.results, function () {
                            if(this.hidden) {
                                return true;
                            }
                            var item = this;
                            var l = dCrt('li').attr('data-sel', $.jCommon.string.makeKey(item.label));
                            var a = dCrt('a').attr('href', '#').addClass('dropdown-toggle').html(item.label);

                            u.append(l.append(a));
                            l.on('click', function () {
                                l.hide();
                                if(!_started) {
                                    _groups.push(item);
                                }
                                if(_groups.length>=2){
                                    state.groupBtn.find('button').attr('disabled', 'disabled').addClass('disabled');
                                }
                                else {
                                    state.groupBtn.find('button').removeAttr('disabled').removeClass('disabled');
                                }
                                $.each(state.opts.group.groups, function () {
                                   if(this.key === item.key){
                                       return false;
                                   }
                                });
                                var del = function (node, data) {
                                    var tmp = [];
                                    $.each(_groups, function () {
                                        if(this.label !== data.label){
                                            tmp.push(this);
                                        }
                                    });
                                    _groups = tmp;
                                    methods.grouped.init();
                                    l.show();
                                };
                                methods.refine.add(r, item, del);
                                if(!_started) {
                                    methods.grouped.init();
                                }
                            });

                        });
                        if (_total > state.opts.disableGrpAt) {
                            state.groupBtn.find('button').attr('disabled', 'disabled').addClass('disabled');
                        }
                        else {
                            state.groupBtn.find('button').removeAttr('disabled').removeClass('disabled');
                        }
                    }
                    if(state.opts[state.opts.view].distinct){
                        state.opts.distinct = $.extend({}, state.opts.distinct, state.opts[state.opts.view].distinct);
                    }
                    _groupings = $.jCommon.array.clone(state.opts.group.groups);
                    methods.refine.getDistinct(state.opts.group.store, state.opts.group.partition, {properties: _groupings, distinct: state.opts.distinct}, callback);
                }
            },
            header: function(title, glyph) {
                if(!state.nodeHdr) {
                    state.nodeHdr = dCrt('div').addClass('filter-bar-header');
                    state.nodeHdrContent = dCrt('div').addClass('filter-bar-header-content');
                    state.nodeHdr.append(state.nodeHdrContent);
                    methods.refine.init();
                }
                state.nodeHdrContent.children().remove();
                if(state.opts.titleHdr){
                    state.nodeHdrContent.append(state.opts.titleHdr);
                }
                if (state.opts.title || title) {
                    var lbl = dCrt(state.opts.url ? 'a' : 'span').addClass('filter-bar-header-title');
                    if (state.opts.url) {
                        dLink(lbl, state.opts.url);
                    }
                    lbl.append(title? title : state.opts.title);
                    state.nodeHdrContent.append(lbl)
                }
                if (state.opts.glyph || glyph) {
                    var icon = dCrt('span').addClass(glyph ? glyph : state.opts.glyph).addClass('filter-bar-header-glyph');
                    icon.css({position: "absolute", "left": "-45px"});
                    state.nodeHdrContent.append(icon);
                }
            },
            grouped: {
                init: function () {
                    _nodes =[];
                    if(state.opts.group.treed){
                        methods.grouped.byTree();
                    }
                    else{
                        methods.grouped.byHtml();
                    }
                },
                byTree: function () {
                    if(_groups.length>0){
                        function getGroupValue(item){
                            var v = item.value;
                            $.each(state.opts.group.groups, function () {
                                var map = this;
                                if(map.key===item.key && $.isFunction(map.onGroup)){
                                    v = map.onGroup(item);
                                    return false;
                                }
                            });
                            return v;
                        }
                        function grp(node, idx) {
                            var child = _groups[idx];
                            if(child) {
                                var fltrs = node._musts;
                                $.each(child.results, function () {
                                    var item = this;
                                    var _musts = $.extend([], node._musts, true);
                                    var n = {item: item, _musts: $.jCommon.array.clone(fltrs), results: []};
                                    n._musts.push({property: item.key, value: item.value, url: item.url});
                                    r = grp(n, (idx + 1));
                                    node.results.push(n);
                                });
                                return true;
                            }
                            return false;
                        }
                        var root = _groups[0];
                        $.each(root.results, function () {
                            var item = this;
                            var node = {item: item, _musts: [], results: []};
                            node._musts.push({property: item.key, value: item.value, url: item.url});
                            var r = grp(node, 1);
                            _nodes.push(node);
                        });
                    }
                    _isGrouped = _nodes.length>0;
                    methods.html[state.opts.view]();
                },
                byHtml: function () {
                    if(_groups.length>0){
                        function getGroupValue(item){
                            var v = item.value;
                            $.each(state.opts.group.groups, function () {
                                var map = this;
                                if(map.key===item.key && $.isFunction(map.onGroup)){
                                    v = map.onGroup(item);
                                    return false;
                                }
                            });
                            return v;
                        }
                        function grp(node, idx) {
                            var child = _groups[idx];
                            if(child) {
                                var fltrs = node._musts;
                                $.each(child.results, function () {
                                    var item = this;
                                    var n = node.clone();
                                    n.append(dCrt('span').css({padding: '0 5px 0 5px'}).html('>'));
                                    var v = getGroupValue(item);
                                    n.append(dCrt('span').attr('data-group-key', item.key).html(v));
                                    if(!n._musts){
                                        n._musts = $.jCommon.array.clone(fltrs);
                                    }
                                    n._musts.push({property: item.key, value: item.value});
                                    r = grp(n, (idx + 1));
                                    if(!r){
                                        _nodes.push(n);
                                    }
                                });
                                return true;
                            }
                            return false;
                        }
                        var root = _groups[0];
                        $.each(root.results, function () {
                            var item = this;
                            item.groups = [];
                            var node = dCrt('div').addClass('group-node blue font-dark');
                            var v = getGroupValue(item);
                            node.append(dCrt('span').attr('data-group-key', item.key).html(v));
                            node._musts = [];
                            node._musts.push({property: item.key, value: item.value, label: v});
                            var r = grp(node, 1);
                            if(!r){
                                _nodes.push(node);
                            }
                        });
                    }
                    _isGrouped = _nodes.length>0;
                    methods.html[state.opts.view]();
                }
            },
            html: {
                init: function(){
                    state.panel = dCrt('div').addClass(state.opts.noHead ? '': 'panel panel-default').css({marginBottom: '0'});
                    state.append(state.panel);
                    if(!state.opts.noHead) {
                        methods.header();
                        state.panel.append(state.nodeHdr);
                    }
                    state.body = dCrt('div').attr('fb-body-id', state.bodyId).addClass('filter-bar-panel panel-body-content' + (state.opts.noHead ? '': ' panel-body'));
                    state.panel.append(state.body);
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
                    if(!state.opts.borders && !state.opts.noHead){
                        state.panel.addClass('no-border');
                        state.body.addClass('no-border');
                    }
                },
                resize: function (e, rc) {
                    if(e.opts.grouped && rc){
                        function r() {
                            var th = e.node.find('.tHeader');
                            var ts = e.node.find('.tSearch');
                            var tt = e.node.find('.tTable');
                            var tb = e.node.find('.tBody');
                            if (th && ts && tb) {
                                var tth = tt.height();
                                var tbh = tb.height();
                                tth = tbh < tth ? tbh : tth;
                                if(tth<=0){
                                    window.setTimeout(r, 500);
                                }
                                else {
                                    var h = (th.height() + ts.height() + tth) + 16;
                                    rc = false;
                                    if (tth > 500) {
                                        h = 500;
                                        rc = true;
                                    }
                                    var opts = {
                                        maxHeight: h,
                                        minHeight: 0,
                                        height: 0
                                    };
                                    dHeight(e.node.parent(), null, null, h);
                                    dHeight($('#' + e.opts.parentId), null, h, h);
                                    dHeight($('#' + e.opts.iframeId), null, h, h);
                                }
                            }
                        }
                        r();
                    }
                },
                tree: function () {
                    state.body.children().remove();
                    var node = dCrt('div');
                    state.body.append(node);
                    node.pSimpleTreeView({
                        data: _nodes,
                        key: "results"
                    });
                },
                grid: function () {
                    _grids = [];
                    state.body.children().remove();
                    var container = dCrt('div').css({height: 'inherit'});
                    state.body.append(container);
                    function bind(gc, gh, gi) {
                        gc.bind('table-view-row-added', function (e) {
                            state.trigger(e);
                        });
                        _grids.push(gc);
                        gc.bind('table-view-rows-loaded', function (e) {
                            _on = e.paging.start > e.paging.hits ? e.paging.hits : e.paging.start;
                            _total = e.paging.hits;
                            if(e.isSub){
                                return false;
                            }
                            else if(gi){
                                if(_on<=0){
                                    gi.hide();
                                }
                                else {
                                    var s = gh.find('div.grouped-node-count');
                                    if (s.length === 0) {
                                        s = dCrt('div').addClass('grouped-node-count');
                                        gh.append(dCrt('br')).append(s);
                                    }
                                    var title = String.format("Results: {0} of {1}", _on, _total);
                                    s.html(title);
                                }
                                methods.header(state.opts.title + ': Grouped');
                            }
                            else if(!_isGrouped && state.opts.paging) {
                                var title;
                                if(state.opts.showFoundOnly){
                                    title = String.format("{0}: Found {1}", state.opts.title, _total);
                                }
                                else{
                                    title = String.format("{0}: {1} of {2}", state.opts.title, _on, _total);
                                }
                                methods.header(title);
                            }
                            if(state.groupBtn) {
                                var btn = state.groupBtn.find('button');
                                if(!btn || btn.length===0){
                                    return false;
                                }
                                if (_total > state.opts.disableGrpAt) {
                                    btn.attr('disabled', 'disabled').addClass('disabled');
                                }
                                else {
                                    btn.find('btn').removeAttr('disabled').removeClass('disabled');
                                }
                            }
                        });
                        gc.bind('table-view-loaded', function (e) {
                            e.stopPropagation();
                            var e2 = jQuery.Event("filter-table-view-loaded");
                            e2.node = state.node;
                            e2.item = e.item;
                            e2.opts = state.opts;
                            e2.paging = e.paging;
                            e2.grouped = e.opts.grouped;
                            e2.init = e.opts.init;
                            state.trigger(e2);
                        });
                    }

                    if(_isGrouped){
                        if(state.opts.group.treed){
                            methods.html.tree();
                            return true;
                        }
                        container.removeClass('filter-bar-ofh').css({overflowY: 'auto', overflowX: 'hidden'});
                        function group() {
                            var on = 0;
                            $.each(_nodes, function () {
                                on++;
                                state.opts.grouped = true;
                                var opts = $.extend({}, state.opts.grid);
                                opts.paneled = true;
                                var gi = dCrt('div').addClass("group-item");
                                container.append(gi);
                                var gh = this;
                                gi.append(gh);
                                var gc = dCrt('div').addClass("group-node-content");
                                dHeight(gc, null, 500, 500);
                                gi.append(gc);
                                var musts = gh._musts ? gh._musts : [];
                                if (state.opts.musts) {
                                    musts.push.apply(musts, state.opts.musts);
                                }
                                var id = $.jCommon.getRandomId('frame');
                                var useIf = false;
                                opts.musts = musts;
                                opts.grouped = true;
                                opts.maxHeight = 500;
                                opts.minHeight = 0;
                                opts.height = 0;
                                opts.parentId = "prnt_" + id;
                                opts.iframeId = useIf ? id : opts.parentId;
                                opts.stateId = _stateId;
                                opts.onResized = function (e) {
                                    methods.html.resize(e, true);
                                };
                                bind(gc, gh, gi);
                                _grpData[opts.parentId] = opts;
                                gc.attr('id', opts.parentId);
                                if (useIf) {
                                    var src = String.format('/pages/grid/index.html?pId={0}&sId={1}', opts.parentId, _stateId);
                                    var frm = dCrt('iframe').attr('id', id).addClass('grouped-node-iframe').attr('src', src).attr('width', '100%').attr('scrolling', 'no');
                                    gc.append(frm);
                                }
                                else {
                                    gc.pGrid(opts);
                                }
                            });
                        }
                        group();
                    }
                    else {
                        container.addClass('filter-bar-ofh');
                        state.opts.grouped = false;
                        var opts = $.extend({}, state.opts.grid);
                        opts.paneled = true;
                        var musts = [];
                        if(state.opts.musts){
                            musts.push.apply(musts, state.opts.musts);
                        }
                        opts.musts = musts;
                        opts.grouped = false;
                        opts.onResized = function (e) {
                            methods.html.resize(e, true);
                        };
                        container.css({overflow: 'hidden'});
                        bind(container);
                        container.pGrid(opts);
                    }
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
        //environment: Initialize
        methods.init();

        state.scroller = function (opts) {
            var st = state.body.scrollTop();
            if(opts.enabled){
               // state.body.removeClass('scroller-disabled');
            }
            else{
               // state.body.addClass('scroller-disabled');
            }
            if(!state.opts.grouped) {
                $.each(_grids, function () {
                    this.pGrid('scroller', opts);
                });
            }
        };

        state.groupSubTableExpanded = function (opts, expanded) {
            var e = opts.grid;
            if(opts.expanded){
                var h = e.node.height();
                if(h<500) {
                    var gs = opts.td.attr('grp-sized');
                    if(!gs) {
                        opts.td.attr('grp-sized', h);
                    }
                    var mh = h + (500-h);
                    var th = e.node.find('.tHeader');
                    var ts = e.node.find('.tSearch');
                    mh -= th.height()-ts.height()-20;
                    e.opts.maxHeight = mh;
                    dHeight(e.node.parent(), null, null, mh);
                    dHeight($('#' + e.opts.iframeId), null, mh, mh);
                    var tt = e.node.find('.tTable');
                    tt.css({overflow: ''});
                    e.node.css({maxHeight: mh});
                    tt.css({maxHeight: mh});
                }
            }
            else{
                var gs = opts.td.attr('grp-sized');
                if(gs){
                    e.opts.maxHeight = parseInt(gs);
                    opts.td.removeAttr('grp-sized');
                    methods.html.resize(e, true);
                }
            }
        };

        state.getGroupData = function (options) {
            return _grpData[options.parentId];
        }
    };

    //Default Settings
    $.jFilterBar.defaults = {
        paging: true,
        view: "grid",
        title: 'Set a title using the "title" option can be text or an element',
        distinct: {
            filters:{
                musts: [],
                shoulds: []
            }
        },
        offset:{
            parent: -56,
            header: 0,
            body: -60
        },
        limit: 60,
        glyphs:{
            grid: 'glyphicons glyphicons-table',
            details: 'glyphicons glyphicons-table',
            tiles: 'glyphicons glyphicons-show-thumbnails'
        },
        disableGrpAt: 1500,
        borders: false,
        content: null,
        showFoundOnly: false
    };
    //Plugin Function
    $.fn.jFilterBar = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.jFilterBar($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $jFilterBar = $(this).data('jFilterBar');
            if($jFilterBar) {
                switch (method) {
                    case 'groupSubTableExpanded':
                        $jFilterBar.groupSubTableExpanded(options);
                        break;
                    case 'getGroupData':
                        return $jFilterBar.getGroupData(options);
                    case 'exists':
                        return (null !== $jFilterBar && undefined !== $jFilterBar && $jFilterBar.length > 0);
                    case 'scroller':
                        $jFilterBar.scroller(options);break;
                    case 'state':
                    default:
                        return $jFilterBar;
                }
            }
        }
    };

    try {
        $.htmlEngine.plugins.register('jFilterBar', $.pVulnerabilities.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
