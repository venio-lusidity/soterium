;(function ($) {

    // pass default groups
    // state.opts.data.groups = ["owned","managed"];

    //Object Instance
    $.jFilterBar = function(el, options) {
        var state = el,
            methods = {};
        state.opts = $.extend({}, $.jFilterBar.defaults, options);
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        state.bodyId = $.jCommon.getRandomId("fb-body-id");
        var _groups = [];
        var _grouped = 0;
        var _nodes = [];
        var _isGrouped = false;
        var _stateId;
        var _grids = [];
        var _grpData = {};
        var _on = 0;
        var _total = 0;
        var _loaded = false;
        var _selected = 0;
        var _started = false;
        var _resizing = false;
        var stopWatch = new oStopWatch();

        // Store a reference to the environment object
        el.data("jFilterBar", state);

        // Private environment methods
        methods = {
            init: function() {
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
                    methods.resize.init();
                    if(state.opts.actions && state.nodeHdr){
                        state.nodeHdr.jActions({actions: state.opts.actions});
                    }
                    if(_groups.length>0) {
                        _isGrouped = true;
                        $.each(_groups, function () {
                            // todo: seems to create an extra tree node.
                            var grp = $('li[data-sel="' + $.jCommon.string.makeKey(this.label) + '"]');
                            grp.click();
                        });
                    }
                    methods.html[state.opts.view]();
                    lusidity.environment('onResize', function () {
                        methods.resize.init();
                    });
                }, true);
            },
            changeView: function () {
                methods.html.init(state.paging);
            },
            resize: {
                init: function () {
                    var h=0;
                    if(_isGrouped) {
                        h = state.panel.height();
                        var hdr = state.nodeHdr.height();
                        h -= (hdr + 25);
                        dHeight(state.body, 0, h, h);
                    }
                    dHeight(state.body, h, h, h);
                }
            },
            enable: function (node, enabled) {
                  if(enabled){
                      node.removeAttr('disabled').removeClass('disabled');
                  }
                  else{
                      node.attr('disabled', 'disabled').addClass('disabled');
                  }
            },
            checkGroups: function(id){
                var ul = state.groupBtn.find('ul');
                var len = 0;
                $.each(ul.children(), function () {
                   if(!$(this).hasClass('selected')){
                       len++;
                   }
                });
                if(len>0){
                    methods.enable(state.groupBtn.find('button'), false);
                }
                else {
                    methods.enable(state.groupBtn.find('button'), true);
                }
                if(state.opts.treed && id) {
                    var btn = $('#' + id);
                    if (_selected > 1) {
                        btn.find('span').hide();
                    }
                    else {
                        btn.find('span').show();
                    }
                }
            },
            refine:{
                add: function (node, item, del, id) {
                    var r = dCrt('div').attr('id', id).addClass('filter-sel blue font-dark').css({position: 'relative', cursor: 'pointer'});
                    var b1= dCrt('div').append(item.label);
                    var b2= dCrt('span').addClass('glyphicons glyphicons-remove font-red-med').css({position: 'absolute', top: '4px', right: '0', fontSize: '16px'});
                    node.append(r.append(b1).append(b2));
                    r.on('click', function () {
                        if($.isFunction(del)){
                            r.remove();
                            del(node, item);
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
                    var url = String.format('/refine/{0}/{1}/properties/distinct/dynamic', str, prt);
                    map.distinct.data = $.extend({}, state.opts.data, true);
                    map.distinct.data.allowDuplicates = true;
                    map.distinct.data.extendData = true;
                    map.distinct.data.et_exact = state.opts.data.et_exact;
                    $.htmlEngine.request(url, s, s, map, 'post', true);
                },
                group: function () {
                    if(state.opts.treed){
                        methods.grouped.treeRefine();
                    }
                    else{
                        methods.grouped.gridRefine();
                    }
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
                init: function (data) {
                    _nodes =[];
                    if(state.opts.treed) {
                        methods.html.tree(data);
                    }
                    else{
                        methods.grouped.byGrid();
                    }
                },
                treeRefine: function () {
                    var w = dCrt('div').css({marginBottom: '5px'});
                    var r = dCrt('div');
                    state.groupBtn = methods.refine.btn('Group By');
                    var u = dCrt('ul').addClass('dropdown-menu');
                    state.groupBar.append(w.append(r).append(state.groupBtn.append(u)));
                    state.opts.group.groups = $.jCommon.array.sort(state.opts.group.groups, [{property: "label", asc: true}]);
                    var first=null;
                    $.each(state.opts.group.groups, function () {
                        if (this.hidden) {
                            return true;
                        }
                        var item = this;
                        if(state.opts.group.exclusions){
                            var skip = false;
                            $.each(state.opts.group.exclusions, function () {
                                if(this.key === item.key){
                                    skip = true;
                                    return false;
                                }
                            });
                            if(skip){
                                return true;
                            }
                        }
                        /*
                        label: 'Location',
                            key: 'location',
                            fKey: 'locationId',
                            fValKey: 'url',
                            urlKey: 'locationId'
                         */
                        var l = dCrt('li').attr('data-sel', $.jCommon.string.makeKey(item.label));
                        var a = dCrt('a').attr('href', '#').addClass('dropdown-toggle').html(item.label);
                        u.append(l.append(a));
                        l.on('click', function () {
                            l.hide();
                            window.setTimeout(function () {
                                var id = $.jCommon.getRandomId('sel');
                                if (null === first) {
                                    first = id;
                                }
                                var _groupings = [];

                                function callback(data) {
                                    var del = function (node, data) {
                                        var tmp = [];
                                        $.each(_groups, function () {
                                            if (this.label !== data.label) {
                                                tmp.push(this);
                                            }
                                        });
                                        _groups = tmp;
                                        _grouped = _groups.length;
                                        _selected--;
                                        if (_grouped > 0) {
                                            methods.grouped.init(state.opts.current.data);
                                        }
                                        else {
                                            state.opts.current = {};
                                            _groups = [];
                                            _isGrouped = false;
                                            first = null;
                                            methods.html[state.opts.view]();
                                        }
                                        l.removeClass('selected').show();
                                        methods.checkGroups(first);
                                    };
                                    l.addClass('selected');
                                    methods.refine.add(r, item, del, id);
                                    _selected++;
                                    methods.checkGroups(first);
                                    if (data.results && data.results[0]) {
                                        state.opts.current = {data: data.results[0].results};
                                        methods.grouped.init(state.opts.current.data);
                                    }
                                }

                                _grouped++;
                                if (_groups.length === 0) {
                                    _groupings.push(item);
                                    var d = $.extend({}, state.opts.distinct, true);
                                    _groups.push(item);
                                    if (state.opts.group.filters) {
                                        d.must = {};
                                        $.each(state.opts.group.filters, function () {
                                            d.must[this.fKey] = {term: this.value, type: this.type};
                                        });
                                    }
                                    methods.refine.getDistinct(state.opts.group.store, state.opts.group.partition, {
                                        properties: _groupings,
                                        distinct: d
                                    }, callback);
                                }
                                else {
                                    callback(item);
                                    state.treeNode.pSimpleTreeView('showExpand', {idx: _groups.length - 1});
                                    _groups.push(item);
                                }
                            }, 100);
                        });
                    });
                },
                gridRefine: function () {
                    var w = dCrt('div').css({marginBottom: '5px'});
                    var r = dCrt('div');
                    state.groupBtn = methods.refine.btn('Group By').addClass('disabled');
                    var u = dCrt('ul').addClass('dropdown-menu');
                    state.groupBar.append(w.append(r).append(state.groupBtn.append(u)));
                    if(state.opts.disableGrpAt>0 && _total>state.opts.disableGrpAt) {
                        methods.enable(state.groupBtn, false);
                        return true;
                    }
                    function callback(data) {
                        _groupings = data;
                        if(state.opts.grpUseGo) {
                            state.groupIt = dCrt('button').addClass('btn btn-success green disabled').html('Go').css({marginLeft: '5px'});
                            state.groupIt.on('click', function () {
                                state.groupIt.addClass('disabled');
                                methods.grouped.init();
                            });
                            w.append(state.groupIt);
                        }
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

                                $.each(state.opts.group.groups, function () {
                                    if(this.key === item.key){
                                        return false;
                                    }
                                });
                                function check() {
                                    if(state.opts.maxGroupings>0 && _groups.length>=state.opts.maxGroupings){
                                        state.groupBtn.find('button').attr('disabled', 'disabled').addClass('disabled');
                                        if(state.groupIt && state.groupIt.length>0) {
                                            methods.enable(state.groupIt, true);
                                        }
                                    }
                                    else{
                                        methods.checkGroups();
                                    }
                                    if(state.groupIt && state.groupIt.length>0) {
                                        if (_groups.length > 0) {
                                            methods.enable(state.groupIt, true);
                                        }
                                        else {
                                            methods.enable(state.groupIt, false);
                                        }
                                    }
                                }
                                function make() {
                                    if(!state.opts.grpUseGo)
                                    {
                                        if(_isGrouped){
                                            methods.grouped.init();
                                        }
                                        else {
                                            methods.html.grid();
                                        }
                                    }
                                }
                                var del = function (node, data) {
                                    var tmp = [];
                                    $.each(_groups, function () {
                                        if(this.label !== data.label){
                                            tmp.push(this);
                                        }
                                    });
                                    _groups = tmp;
                                    _isGrouped = _groups.length>0;
                                    if(state.groupIt && state.groupIt.length>0) {
                                        methods.enable(state.groupIt, true);
                                    }
                                    l.show();
                                    check();
                                    make();
                                };
                                check();
                                methods.refine.add(r, item, del);
                                _isGrouped = true;
                                make();
                            });
                        });
                        if (state.opts.disableGrpAt>0 && _total > state.opts.disableGrpAt) {
                            methods.enable(state.groupBtn.find('button'), false);
                        }
                        else {
                            methods.enable(state.groupBtn.find('button'), true);
                        }
                    }
                    if(state.opts[state.opts.view].distinct){
                        state.opts.distinct = $.extend({}, state.opts.distinct, state.opts[state.opts.view].distinct);
                    }
                    _groupings = $.jCommon.array.clone(state.opts.group.groups);
                    methods.refine.getDistinct(state.opts.group.store, state.opts.group.partition, {properties: _groupings, distinct: state.opts.distinct}, callback);
                },
                byGrid: function () {
                    if(_groups.length>0){
                        stopWatch.start(true);
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
                                $.each(child.results, function () {
                                    var item = $.extend({}, this, true);
                                    var n = node.clone();
                                    n.append(dCrt('span').css({padding: '0 5px 0 5px'}).html('>'));
                                    var v = getGroupValue(item);
                                    n._title = v;
                                    n.append(dCrt('span').attr('data-group-key', item.key).html(item.extValue ? item.extValue : item.value));
                                    n._musts = $.jCommon.array.clone(node._musts);
                                    item.term = v;
                                    n._musts.push(item);
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
                            var item = $.extend({}, this, true);
                            item.groups = [];
                            var node = dCrt('div').addClass('group-node blue font-dark');
                            var v = getGroupValue(item);
                            node._title = v;
                            node.append(dCrt('span').attr('data-group-key', item.key).html(item.extValue ? item.extValue : item.value));
                            node._musts = [];
                            item.term = v;
                            node._musts.push(item);
                            var r = grp(node, 1);
                            if(!r){
                                _nodes.push(node);
                            }
                        });
                    }
                    _isGrouped = _nodes.length>0;
                    methods.html.grid();
                }
            },
            html: {
                init: function(){
                    state.panel = dCrt('div').addClass(state.opts.noHead ? '': 'panel panel-default').css({marginBottom: '0', height: 'inherit', width: 'inherit', padding: '0 0 0 0' });
                    state.append(state.panel);
                    if(!state.opts.noHead) {
                        methods.header();
                        state.panel.append(state.nodeHdr);
                    }
                    state.body = dCrt('div').attr('fb-body-id', state.bodyId).addClass('filter-bar-panel panel-body-content' + (state.opts.noHead ? '': ' panel-body')).css({padding: '2px 0 2px 0'});
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
                                var h = (th.height() + ts.height() + tth);
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
                                h+=28;
                                console.log(e.opts.parentId + ': ' + h);
                                dHeight(e.node.parent(), null, null, h);
                            }
                        }
                    }
                },
                tree: function (data) {
                    state.body.children().remove();
                    var node = dCrt('div').addClass('row').css({fontSize: '14px', position: 'relative', marginLeft: '-10px'});
                    state.body.append(node);
                    var lPanel = dCrt('div').addClass('col-md-4').css({height: 'inherit'});
                    state.treeNode = dCrt('div').css({height: 'inherit', paddingLeft: '2px', overflow: 'auto'}).addClass('pg-panel').show();
                    lPanel.append(state.treeNode);
                    var rPanel = dCrt('div').addClass('col-md-8').css({height: 'inherit'});
                    var rc = dCrt('div').css({height: 'inherit', overflowX: 'hidden', overflowY: 'auto'}).addClass('pg-panel').show();
                    rPanel.append(rc);
                    node.append(lPanel).append(rPanel);

                    function sz() {
                        var h = state.body.availHeight(10);
                        state.body.height(h);
                        node.height(h);
                        if(rc.pSummary('exists')){
                            rc.pSummary('resize')
                        }
                    }
                    node.jNodeReady({onResized: function () {
                        sz();
                    }});
                    sz();
                    state.treeNode.pSimpleTreeView({
                        data: data,
                        parentData: state.opts.data,
                        key: "results",
                        grouped: _grouped,
                        showSelected: true,
                        onExpand: function (e) {
                            var item = e.item.item;
                            var filters = [];
                            var on=0;
                            $.each(_groups, function () {
                                filters.push(this);
                                if(this.key === item.key){
                                    return false;
                                }
                                on++;
                            });
                            var _groupings = [];
                            _groupings.push(_groups[on+1]);
                            var d = $.extend({}, state.opts.distinct, true);
                            if(!d.must){
                                d.must={};
                            }
                            $.each(filters, function () {
                                var filter = this;
                                $.each(e.item.groups, function () {
                                    var grp = this;
                                    if(grp.key ===filter.key){
                                        var k = filter.fKey ? filter.fKey : filter.key;
                                        d.must[k] = {
                                            term: grp[filter.fValKey ? filter.fValKey : k],
                                            type: filter.type ? filter.type : 'string'
                                        };
                                    }
                                });
                            });
                            if(state.opts.group.filters){
                                $.each(state.opts.group.filters, function () {
                                   d.must[this.fKey] = {term: this.value, type: this.type};
                                });
                            }
                            function callback(data) {
                                if(data.results && data.results[0]){
                                    var d = data.results[0];
                                    state.treeNode.pSimpleTreeView('update', {item: e.item, data: d, grouped: _grouped});
                                }
                            }
                            var qry = {
                                properties: _groupings,
                                distinct: d
                            };
                            methods.refine.getDistinct(state.opts.group.store, state.opts.group.partition, qry, callback);
                        },
                        onTreeNodeClicked: function (e) {
                            if($.isFunction(state.opts.onTreeNodeClicked)){
                                state.opts.onTreeNodeClicked({item: e.item, node: rc});
                            }
                        },
                        nodeCount: function (e) {
                            if($.isFunction(state.opts.nodeCount)){
                                state.opts.nodeCount(e);
                            }
                        }
                    });
                },
                grid: function () {
                    _grids = [];
                    state.body.children().remove();
                    methods.resize.init();
                    var container = dCrt('div').css({height: 'inherit', maxHeight: 'inherit'});
                    state.body.append(container);
                    function bind(gc, gh, gi) {
                        gc.bind('table-view-row-added', function (e) {
                            state.trigger(e);
                        });
                        _grids.push(gc);
                        gc.bind('table-view-rows-loaded', function (e) {
                            _on = e.paging.start > e.paging.hits ? e.paging.hits : e.paging.start;
                            var total = e.paging.hits;
                            if(!_isGrouped){
                                _total = e.paging.hits;
                            }
                            if(_isGrouped && state.opts.expandGrpAt>0 && _total > state.opts.expandGrpAt){
                                return true;
                            }
                            var title;
                            if(e.isSub){
                                return false;
                            }
                            else if(gi){
                                if(e.paging.hits<=0){
                                    gi.remove();
                                }
                                else {
                                    var s = gh.find('div.grouped-node-count');
                                    if (s.length === 0) {
                                        s = dCrt('div').addClass('grouped-node-count');
                                        gh.append(dCrt('br')).append(s);
                                    }
                                    title = String.format("Found: {0}", $.jCommon.number.commas(total));
                                    s.html(title);
                                }
                            }
                            else {
                                if(state.opts.showFoundOnly){
                                    title = String.format("{0}: Found {1}", state.opts.title, $.jCommon.number.commas(total));
                                }
                                else{
                                    title = String.format("{0}: {1} of {2}", state.opts.title, $.jCommon.number.commas(_on), $.jCommon.number.commas(total));
                                }
                                methods.header(title);
                            }
                            if(state.groupBtn) {
                                var btn = state.groupBtn.find('button');
                                if(!btn || btn.length===0){
                                    return false;
                                }
                                if (state.opts.disableGrpAt>0 && total > state.opts.disableGrpAt) {
                                    methods.enable(btn, false);
                                }
                                else {
                                    methods.enable(btn, true);
                                }
                            }
                        });
                        gc.bind('table-view-loaded', function (e) {
                            e.stopPropagation();
                            if(gi && e.paging.hits<=0){
                                gi.remove();
                            }
                            else if(_isGrouped){
                                methods.html.resize(e, !e.opts.groupedInit);
                                e.opts.groupedInit = true;
                            }
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
                            var id = $.jCommon.getRandomId('frame');
                            var collapsed = (state.opts.expandGrpAt>0 && _total > state.opts.expandGrpAt);
                            var leafs = [];
                            state.opts.grouped = true;
                            if(state.opts.data && state.opts.data.count) {
                                _total = $.jCommon.number.parse(state.opts.data.count);
                            }
                            if(state.opts.expandGrpAt>0 && _total > state.opts.expandGrpAt){
                                state.groupBtn.addClass('disabled');
                                //methods.html.spring(container, bind);
                            }
                            else {
                                var on = 0;
                                $.each(_nodes, function () {
                                    on++;
                                    var opts = $.extend({}, state.opts.grid);
                                    opts.paneled = true;
                                    var gh = this;
                                    methods.html.expanded(container, gh, opts, bind, id, on, true);
                                });
                            }
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
                },
                content: function (gi, gh, opts, bind, id) {
                    var gc = dCrt('div').addClass("group-node-content");
                    var dh = 500;
                    dHeight(gc, 0, 0, dh);
                    gi.append(gc);
                    var musts = gh._musts ? gh._musts : [];
                    if (state.opts.musts) {
                        musts.push.apply(musts, state.opts.musts);
                    }
                    opts.musts = musts;
                    opts.grouped = true;
                    opts.maxHeight = dh;
                    opts.minHeight = 0;
                    opts.height = 0;
                    opts.parentId = "prnt_" + id;
                    opts.stateId = _stateId;
                    opts.onResized = function (e) {
                        methods.html.resize(e, true);
                    };
                    if($.isFunction(bind)) {
                        bind(gc, gh, gi);
                    }
                    _grpData[opts.parentId] = opts;
                    gc.attr('id', opts.parentId);
                    return gc;
                },
                expanded: function (container, gh, opts, bind, id, on) {
                    var gi = dCrt('div').addClass("group-item");
                    container.append(gi);
                    gi.append(gh);
                    var gc = methods.html.content(gi, gh, opts, bind, id);
                    var useIf = false;
                    opts.iframeId = useIf ? id : opts.parentId;
                    if (useIf) {
                        var src = String.format('/pages/grid/index.html?pId={0}&sId={1}', opts.parentId, _stateId);
                        var frm = dCrt('iframe').attr('id', id).addClass('grouped-node-iframe').attr('src', src).attr('width', '100%').attr('scrolling', 'no');
                        gc.append(frm);
                    }
                    else {
                        gc.pGrid(opts);
                    }
                },
                spring: function (container, bind, expanded) {
                    var leafs = [];
                    var node = dCrt('div').css({height: 'inherit', width: 'inherit'});
                    container.append(node);
                    $.each(_nodes, function () {
                        var gh = this;
                        gh.removeClass();
                        var gi = dCrt('div').addClass("group-item");
                        var opts = $.extend({}, state.opts.grid);
                        opts.paneled = true;
                        var leaf = {
                            title: gh,
                            link: false,
                            groupedItems: [],
                            hdrCss: {fontSize: '14px'},
                            content: gi,
                            groups: gh._musts,
                            parentData: state.opts.data,
                            onExpand: function (item, data) {
                                var node = $(item.content);
                                var exp = node.attr('exp');
                                if(!exp) {
                                    var id = $.jCommon.getRandomId('frame');
                                    opts.iframeId = id;
                                    opts.spring = true;
                                    var gc = methods.html.content(gi, gh, opts, bind, id);
                                    gc.pGrid(opts);
                                    node.attr('exp', true);
                                }

                            }
                        };
                        leafs.push(leaf);
                    });
                    stopWatch.stop(true);
                    stopWatch.getFinalTime(true);
                    node.spring({collapsed: !expanded, leafs: leafs, scroller: container, onVisible: state.opts.onVisible, nodeCount: state.opts.nodeCount, collapseAll: false});
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
            // this use to calculate the parent table size and should be removed from use once verified that it is no longer required.
            var e = opts.grid;
            var h = e.node.height();
            var tNode = e.node.find('.tTable');
            var nh = e.node.attr('o-size');
            var fh = tNode.attr('o-size');
            var cnt = e.node.attr('o-cnt');
            cnt = cnt ? parseInt(cnt) : 0;
            if(opts.expanded){
                cnt++;
                if(h<500) {
                    if(!nh) {
                        e.node.attr('o-size', h);
                        tNode.attr('o-size', tNode.height());
                    }
                    var mh = 500;
                    var th = e.node.find('.tHeader');
                    var ts = e.node.find('.tSearch');
                    fh= mh-(th.height()+ts.height());
                    e.opts.maxHeight = mh;
                    dHeight(e.node, 0, mh, mh);
                    dHeight(tNode, 0, fh, fh);
                }
            }
            else if(nh) {
                cnt--;
                if(cnt===0) {
                    nh = parseInt(nh);
                    fh = parseInt(fh);
                    e.opts.maxHeight = nh;
                    dHeight(e.node, 0, nh, nh);
                    dHeight(tNode, 0, fh, fh);
                }
            }
            e.node.attr('o-cnt', cnt>=0 ? cnt : 0);
        };
        state.getGroupData = function (options) {
            return _grpData[options.parentId];
        };
        state.reload = function (options) {
            if(_isGrouped){
                methods.grouped.init();
            }
            else {
                methods.html.grid();
            }
        };
    };

    //Default Settings
    $.jFilterBar.defaults = {
        paging: true,
        view: "grid",
        data: {
            et_exact: false
        },
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
        treed: false,
        maxGroupings: 2,
        disableGrpAt: 0,
        expandGrpAt: 0,
        grpUseGo: false,
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
                    case 'reload':
                        $jFilterBar.reload(options);break;
                    case 'state':
                    default:
                        return $jFilterBar;
                }
            }
            else{
                console.log('Cannot find jFilterBar.');
            }
        }
    };

})(jQuery);
