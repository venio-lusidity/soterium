;(function ($) {
    /*
     triggers events: treeNodeLeftClick, treeNodeRightClick,treeNodeAfterSort,treeNodeCreated,treeViewDataLoaded,treeNodeDroppedFailed,treeNodeDroppedSuccess
     values passed: node, data
     */
    //Object Instance

    $.treeView = function (el, options) {
        var state = el,
            methods = {};
        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.treeView.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : 'treeView');
        if(!state.opts.name && options.schema.name) {
            state.opts.name = options.schema.name;
        }
        else if(!state.opts.name){
            state.opts.name = 'treeView';
        }
        state.started = false;
        state.current = {};

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        var level = {
            summary: "summary",
            complete: "complete"
        };

        // Store a reference to the environment object
        el.data("treeView", state);

        // Private environment methods
        methods = {
            init: function () {
                if (state.opts.name || state.opts.cssFiles) {
                    $.htmlEngine.loadFiles(state.worker.node, state.opts.name, state.opts.cssFiles);
                }
                state.worker.node.attr('data-valid', true).show();
                function load() {
                    var onSuccess = function (data) {
                        var f = data;
                        var event = jQuery.Event('treeViewInitDataLoaded', {item: f});
                        state.trigger(event);
                        if($.isFunction(state.opts.sort)){
                            f = state.opts.sort(f);
                        }
                        state.worker.node.addClass('tree');
                        methods.html.create(f, state.worker.node, true);
                    };
                    var onFailed = function () {
                        methods.loaders(state.worker.node, 'hide');
                        window.setTimeout(load, 300);
                    };
                    if(state.opts.get.request){
                        state.opts.get.request(null, onSuccess, onFailed);
                    }
                    else if (state.opts.get.rootQuery) {
                        $.htmlEngine.request(methods.url(0, state.opts.limit), onSuccess, onFailed, state.opts.get.rootQuery(), 'post', true);
                    }
                    else if (state.opts.get.rootUri) {
                        $.htmlEngine.request(state.opts.get.rootUri(), onSuccess, onFailed, null, 'get', true);
                    }
                }
                load();
            },
            url: function (start, limit) {
                return ((state.opts.get.url) ? state.opts.get.url : '/query') + '?start=' + start + '&limit=' + limit ;
            },
            getUri: function (data) {
                var result = null;
                if (null !== data && undefined !== data) {
                    result = (null !== data.otherId && undefined !== data.otherId) ? data.otherId : data[state.KEY_ID];
                }
                return result;
            },
            getId: function (data) {
                var id = null;
                if (null !== data && undefined !== data) {
                    id = methods.getUri(data);
                    if (id) {
                        id = $.jCommon.string.replaceAll(id, "/", "_");
                    }
                }
                return id;
            },
            loaders: function (node, arg) {
                if(node.loaders('exists')){
                    node.loaders(arg);
                }
                if(node.headers && node.headers.loaders('exists')){
                    node.headers.loaders(arg);
                }
            },
            html: {
                create: function (data, node, ir) {
                    if (data && data.results && data.results.length>0) {
                        var listNode = $(document.createElement('ul'));
                        node.append(listNode);
                        if(state.opts.disableContextMenu){
                            methods.disableContextMenu(listNode);
                        }
                        methods.html.addChildNodes(node, listNode, data, ir);
                    }
                    var event = jQuery.Event('treeViewLoaded', {node: node, item: data});
                    state.trigger(event);
                },
                cylon: function (node) {
                    $.htmlEngine.busy(node, {type: 'cylon', top: '3px', left: '6px', pre: 'true'});
                    window.setTimeout(function () {
                        node.loaders('hide');
                    }, 5000);
                },
                addChildNodes: function (parentNode, listNode, data, ir) {
                    if (data && data.results) {
                        $.each(data.results, function () {
                            var item = this;
                            item._root = ir;
                            var url = methods.getUri(item);
                            var exists = listNode.find('li[data-id="' + url + '"]');
                            if (exists.length <= 0 || state.opts.allowDuplicates) {
                                var m = function (node, data) {
                                    if (!node) {
                                        return true;
                                    }
                                    listNode.append(node);
                                    if (!state.started) {
                                        state.current.node = node;
                                        var icon = $(document.createElement('span')).addClass('glyphicon glyphicon-plus').addClass(state.opts.node.icon.css);
                                        icon.attr('data-loaded', 'false').hide();
                                        node.prepend(icon);
                                        node.icon = icon;
                                        state.started = true;
                                        node._root = true;
                                        if(state.opts.expandable) {
                                            methods.next(node, icon, item);
                                            methods.iconClick(icon, data, node);
                                        }
                                    }
                                    else if(state.opts.expandable) {
                                        methods.count(node, data);
                                    }
                                    item.node = node;
                                    var event = jQuery.Event('treeNodeCreated', {node: node, item: item, parentNode: parentNode, listNode: listNode});
                                    state.trigger(event);
                                };
                                methods.html.createNode(parentNode, item, m);
                            }
                        });
                        var dNode;
                        methods.sort(parentNode);
                        $.each(data.results, function () {
                            var item = this;
                            if(item && item.node) {
                                if(item.defaultFavorite){
                                    dNode = item.node;
                                }
                                var event = jQuery.Event('treeNodeAfterSort', {item: item});
                                item.node.trigger(event);
                            }
                        });
                        if(dNode){
                            dNode.parent().prepend(dNode);
                        }
                    }
                },
                createNode: function (parentNode, data, m) {
                    var s = function (item) {
                        try {
                            if(!item){
                                return false;
                            }
                            item._selectable = !item._root || state.opts.rootSelectable;
                            if(item._selectable && state.opts.exclusions){
                                $.each(state.opts.exclusions, function () {
                                    if($.jCommon.string.equals(item.title, this, true)){
                                        item._selectable = false;
                                    }
                                })
                            }
                            if(state.opts.mapper.id && item[state.opts.mapper.id]) {
                                var node = $(document.createElement('li')).css({position: 'relative'});
                                if(state.opts.mapper.id) {
                                    node.attr('data-id', item[state.opts.mapper.id]);
                                }
                                var txt = item[state.opts.mapper.label];
                                if(state.opts.devOnly && txt){
                                  txt = $.jCommon.string.toCodex(txt, 6);
                                }
                                var title = $(document.createElement('div')).css({display: 'block'}).addClass('title-header');
                                var inner = $(document.createElement('div')).addClass('title-header-item').html(txt).css({display: 'inline-block'});

                                title.append(inner);
                                title.txt = txt;
                                title.innerTxt = inner;
                                node.data('item', item);

                                if(state.opts.mapper.id) {
                                    title.attr('data-id', item[state.opts.mapper.id]);
                                }
                                title.attr('data-vertex_type', item['vertexType']);
                                if (state.opts.tooltip && item[state.opts.tooltip]) {
                                    title.attr('title', item[state.opts.tooltip]);
                                }
                                if (state.started || state.opts.rootSelectable) {
                                    title.addClass(state.opts.node.title.css);
                                }
                                node.append(title);
                                node.header = title;
                                if (state.opts.totals) {
                                    if ($.jCommon.is.numeric(item._count)) {
                                        state.updateSize({node: node, size: item._count, item: item});
                                    }
                                    else {
                                        var txt = $(node.children()[0]).attr('title');
                                        var query = $.isFunction(state.opts.get.countQuery) ? state.opts.get.countQuery(item) : state.opts.get.childQuery(item);
                                        state.updateSize({
                                            node: node, query: query, item: item
                                        });
                                    }
                                }
                                var del;
                                if (state.opts.onDelete && $.isFunction(state.opts.onDelete)) {
                                    del = $(document.createElement('div')).css({
                                        position: 'absolute',
                                        top: '0',
                                        'right': '0',
                                        cursor: 'pointer',
                                        height: '24px',
                                        width: '24px'
                                    }).hide();
                                    del.append($(document.createElement('span')).attr('title', "Remove").addClass('glyphicon glyphicon-remove shadowed').css({
                                        top: '8px',
                                        left: '8px',
                                        color: 'red'
                                    }));

                                    del.on("mouseover", function () {
                                        del.addClass('orange');
                                    });
                                    del.on("mouseleave", function () {
                                        del.removeClass('orange');
                                    });
                                    node.append(del);
                                    del.on('click', function () {
                                        state.opts.onDelete(node, data);
                                    });
                                }
                                if (item._selectable) {
                                    var dt = null;
                                    title.on("mouseup", function (e) {
                                        dt = null;
                                        if (!state.dragging) {
                                            var event, name,lft;
                                            if (e.which === 1) {
                                                state.worker.node.find('div.selected').removeClass('selected');
                                                title.addClass('selected');
                                                name = 'treeNodeLeftClick';
                                                lft = true;
                                            }
                                            else if (e.which === 3) {
                                                name = 'treeNodeRightClick';
                                            }
                                            if (name) {
                                                state.current.node = node;
                                                event = jQuery.Event(name, {node: node});
                                                state.trigger(event);
                                                if(lft && $.isFunction(state.opts.onTreeNodeLeftClick)){
                                                    state.opts.onTreeNodeLeftClick(event);
                                                }
                                            }
                                        }
                                    });
                                    title.on('mouseover', function () {
                                        if (del) {
                                            del.show();
                                        }
                                        if (state.dragging && node.icon) {
                                            dt = new Date();
                                            window.setTimeout(function () {
                                                if (dt) {
                                                    var ht = ((new Date()) - dt);
                                                    if (ht >= 1000) {
                                                        var hover = state.worker.node.find('div.drop-area-hover');
                                                        if (hover.length > 0) {
                                                            var icon = hover.parent().find('span.glyphicon');
                                                            if (icon.length > 0) {
                                                                icon.click();
                                                            }
                                                        }
                                                    }
                                                }
                                            }, 1000);
                                        }
                                    });
                                    title.on('mouseleave', function (e) {
                                        if (!$(e.relatedTarget).hasClass('glyphicon-remove')) {
                                            dt = null;
                                            if (del) {
                                                del.hide();
                                            }
                                        }
                                    });
                                }
                                if (state.opts.isDraggable && state.started) {
                                    methods.makeDraggable(node);
                                }
                                if (state.opts.isDroppable) {
                                    methods.makeDroppable(node);
                                }
                                node.on('tree-node-refresh', function (e) {
                                    e.stopPropagation();
                                    node.find('ul').remove();
                                    methods.next(node, node.icon, item);
                                    var txt = $(node.children()[0]).attr('title');
                                    var query = $.isFunction(state.opts.get.countQuery) ? state.opts.get.countQuery(item) : state.opts.get.childQuery(item);
                                    state.updateSize({
                                        node: node, query: query, item: item
                                    });
                                });
                                m(node, item);
                                if(item._selectable) {
                                    methods.html.selected(node, item);
                                }
                            }
                            else{
                                console.log("The mapping id is either missing or doesn't exists on the item.");
                            }
                        }
                        catch (e){
                            console.log(e);
                      }
                    };

                    if (!data.fromEndpointId) {
                        s(data);
                    }
                    else {
                        var pi = parentNode.data('item');
                        var f = data.fromEndpointId.replace('#', '');
                        var t = data.toEndpointId.replace('#', '');
                        var id = $.jCommon.string.equals(pi.lid, data.fromEndpointId) ? t : f;
                        var url = '/vertices/' + id;
                        $.htmlEngine.request(url, s, s, null, 'get');
                    }
                },
                selected: function(node, item){
                    if(!item._selectable){
                        return false;
                    }
                    if ((state.opts.defaultSelectable)
                        || (state.opts.rootSelectable && state.opts.rootSelected)) {
                        var p = !state.selected;
                        if((state.opts.defaultSelectable && !item.defaultFavorite)){
                            p=false;
                        }
                        var t;
                        if(p) {
                            state.selected = true;
                            t = node.find('div.title-header.title-header-item');
                            if (t) {
                                $(t[0]).addClass('selected');
                            }
                            var e1 = jQuery.Event('treeNodeLeftClick', {node: node, item: item, initial: true});
                            state.trigger(e1);
                            if($.isFunction(state.opts.onTreeNodeLeftClick)){
                                state.opts.onTreeNodeLeftClick(e1);
                            }
                            var e2 = jQuery.Event('treeNodeDefaultSelected', {node: node, item: item, initial: true});
                            state.trigger(e2);
                        }
                    }
                    if(item.defaultFavorite){
                        node.parent().prepend(node);
                        if(state.opts.defaultCss){
                            if(!t){
                                t = node.find('div.title-header');
                            }
                            t.css(state.opts.defaultCss);
                        }
                    }

                }
            },
            iconClick: function(icon, item, node){
                icon.on('click', function () {
                    var l = icon.attr('data-loaded');
                    l = (l && (l === 'true'));
                    if (!l) {
                        icon.attr('data-loaded', true);
                        methods.html.cylon(node);
                        methods.next(node, icon, item);
                    }
                    else {
                        methods.toggle(node, icon, false);
                    }
                });
            },
            sort: function (node) {
                var ul = node.find('ul');
                if (ul) {
                    ul.children().sortElements(function (a, b) {
                        var at = $(a).data('item')[state.opts.mapper.label];
                        var bt = $(b).data('item')[state.opts.mapper.label];
                        return $.jCommon.object.compare(at, bt);
                    });
                }
            },
            count: function (node, item, loaded) {
                if(state.opts.counted && item._counted){
                    item._count = item._counted.exact;
                    item._inherited = item._counted.inherited;
                }
                if(undefined!==node && undefined!==item) {
                    var s = function (data) {
                        var hits = (data && data.hits) ? parseInt(data.hits) : 0;
                        if (hits > 0 || (data.inherited && data.inherited>0)) {
                            if (!node._root) {
                                var icon = $(document.createElement('span')).addClass('glyphicon' + (loaded ? ' glyphicon-minus' : ' glyphicon-plus')).addClass(state.opts.node.icon.css);
                                icon.attr('data-loaded', loaded ? 'true' : 'false');
                                node.prepend(icon);
                                node.icon = icon;
                                item.icon = icon;
                                methods.iconClick(icon, item, node);
                                var event = jQuery.Event('treeNodeIconCreated', {node: node, item: item, icon: icon});
                                state.trigger(event);
                            }
                        }
                        if($.isFunction(state.opts.onTreeNodeCounted)){
                            state.opts.onTreeNodeCounted({node: node, item: item});
                        }
                    };
                    if($.jCommon.is.numeric(item._count)){
                        s({hits: item._count, inherited: item._inherited});
                    }
                    else if($.isFunction(state.opts.hasChildren) && state.opts.hasChildren(node, item)){
                        s({hits: 1});
                    }
                    else {
                        var d = state.opts.get.childQuery(item);
                        if ($.jCommon.is.string(d)) {
                            $.htmlEngine.request(d, s, s, null, 'get');
                        }
                        else {
                            d.apiKey = '_count';
                            $.htmlEngine.request(methods.url(0, state.opts.limit), s, s, d, 'post', true);
                        }
                    }
                }
            },
            next: function (node, icon, item) {
                function go() {
                    var success = function (data) {
                        //icon.css({visibility: 'hidden', display: 'none'});
                        if($.isFunction(state.opts.onBefore)){
                            data = state.opts.onBefore(data);
                        }
                        var size = $.jCommon.is.numeric(item._count) ? item._count : 0;
                        if (data && data.results) {
                            var len = data.results.length;
                            size = ((size > len) ? size : len);
                            state.updateSize({node: node, size: size, item: item});
                            methods.html.create(data, node);
                            icon.attr('data-loaded', 'true');
                            methods.toggle(node, icon, true);
                            if (!state.opts.expandRoot && node._root) {
                                methods.toggle(node, icon, true);
                            }
                            var event = jQuery.Event('treeViewDataLoaded', {node: node, icon: icon, item: item});
                            icon.trigger(event);
                            state.worker.node.trigger(event);
                            state.trigger(event);
                        }
                        else {
                            state.updateSize({node: node, size: size, item: item});
                            icon.trigger('treeViewDataLoaded', {error: true});
                            state.worker.node.trigger('treeViewDataLoaded', {error: true});
                        }
                        window.setTimeout(function () {
                            methods.loaders(node, 'hide');
                        }, 1000);
                    };
                    var failed = function () {
                        methods.loaders(node, 'hide');
                        if (state.started || state.opts.rootSelectable) {
                            icon.trigger('treeViewDataLoaded', {node: node, icon: icon, item: item});
                        } else {
                            icon.trigger('treeViewDataLoaded', {error: true});
                        }
                    };


                    if(state.opts.get.request){
                        state.opts.get.request(item, success, failed);
                    }
                    else {
                        var d = state.opts.get.childQuery(item);
                        if ($.jCommon.is.string(d)) {
                            $.htmlEngine.request(d, success, failed, null, 'get');
                        }
                        else {
                            $.htmlEngine.request(methods.url(0, state.opts.limit), success, failed, d, 'post');
                        }
                    }
                }
                go();
            },
            toggle: function (node, icon, fast) {
                var ul = node.find('ul').css({clear: 'both'});
                if (ul.length > 0) {
                    if (icon.hasClass('glyphicon-plus')) {
                        icon.removeClass('glyphicon-plus').addClass('glyphicon-minus');
                        if(fast){
                            ul.show();
                        }
                        else {
                            ul.slideDown('slow')
                        }
                    }
                    else if (icon.hasClass('glyphicon-minus')) {
                        icon.removeClass('glyphicon-minus').addClass('glyphicon-plus');
                        if(fast){
                            ul.hide();
                        }
                        else {
                            ul.slideUp('slow');
                        }
                    }
                }
            },
            disableContextMenu: function (elem) {
                $(elem).on('contextmenu', function (e) {
                    return false;
                });
                $.each($(elem).children(), function () {
                    methods.disableContextMenu(this);
                });
            },
            makeDraggable: function(node){
                node.header.draggable({
                    cursor: "move",
                    revert: true,
                    scroll: false,
                    start: function () {
                        state.dragging = true;
                        $(this).css({zIndex: 99999999}).addClass('dragging');
                    },
                    stop: function () {
                        $(this).css({zIndex: 999999}).removeClass('dragging');
                        state.dragging = false;
                    },
                    helper: function () {
                        return $(this).clone().appendTo('body').show();
                    }
                });
            },
            makeDroppable: function(node){
                node.header.droppable({
                    hoverClass: 'drop-area-hover',
                    drop: function (event, ui) {
                        event.stopPropagation();
                        event.preventDefault();
                        window.event.cancelBubble = true;
                        var target = $(event.target).parent();
                        var parentItem = target.data('item');
                        var source = $(ui.draggable.context).parent();
                        var sourceItem = source.data('item');
                        if(!$.jCommon.string.equals(sourceItem[state.KEY_ID], parentItem[state.KEY_ID], false)) {
                            var success = function (data) {
                                if(data && !data.error) {
                                    state.prev.treeNode = state.current.node;
                                    state.current.node = target;
                                    state.add(data.result, target, target.item);
                                    var event = jQuery.Event('treeNodeDroppedSuccess', {
                                        parentItem: parentItem,
                                        sourceItem: sourceItem
                                    });
                                    state.trigger(event);
                                }else{
                                    lusidity.info.red(data.error);
                                    lusidity.info.show(10);
                                }
                            };
                            var failed = function () {
                                var event = jQuery.Event('treeNodeDroppedFailed', {
                                    parentItem: parentItem,
                                    sourceItem: sourceItem
                                });
                                state.trigger(event);
                            };
                            // replace the vertexType of the source item with the parent so the right class gets constructed.
                            sourceItem.vertexType = parentItem.vertexType;
                            $.htmlEngine.request(state.opts.post.url(parentItem), success, failed, sourceItem, 'post');
                        }
                    }
                });
            }
        };
        state.add = function(data, parent, parentData){
            if(!parent){
                parent = state.current.node;
                parentData = state.current.node.data('item');
            }
            if(parent.length>0) {
                var node = parent.children('ul');
                if(node.length<=0){
                    node = $(document.createElement('ul'));
                    parent.append(node);
                }
                if (node.length === 1) {
                    if (!node.result) {
                        node.result = {};
                    }
                    if (!node.result.results) {
                        node.result.results = [];
                    }
                    node.result.results.push(data);
                    methods.html.addChildNodes(null, node);
                }
                methods.count(parent, parentData, true);
            }
        };

        state.expandTo = function (options) {
            var on = 0;
            var ul = state.worker.node.find('ul:first');

            function scan(nodes, path) {
                $.each(nodes, function () {
                    var node = $(this);
                    var item = node.data('item');
                    if (item && $.jCommon.string.equals(item[options.key], path, true)) {

                        function next(node) {
                            on++;
                            ul = node.find('ul:first');
                            if(ul.length>0) {
                                scan(ul.children(), options.paths[on]);
                            }
                        }

                        if((on+1)===options.paths.length){
                            if(options.select){
                                state.worker.node.find('.selected').removeClass('selected');
                                t = node.find('.title-header:first');
                                if (t) {
                                    t.find(".title-header-item:first").addClass('selected');
                                }
                                var event = jQuery.Event('treeNodeLeftClick', {node: node, item: item});
                                state.trigger(event);
                                if($.isFunction(state.opts.onTreeNodeLeftClick)){
                                    state.opts.onTreeNodeLeftClick(event);
                                }
                            }
                        }
                        else if (item.icon) {
                            if (item.icon.hasClass("glyphicon-plus")) {
                                var dl = item.icon.attr('data-loaded');
                                if(!dl || (dl==='false')) {
                                    function wt() {
                                        ul = node.find('ul:first');
                                        if(ul && ul.children().length>0) {
                                            next(node);
                                        }
                                        else{
                                            window.setTimeout(function () {
                                                wt();
                                            }, 500);
                                        }
                                    }
                                    wt();
                                }
                                item.icon.click();
                                if(dl && (dl==='true')) {
                                    check(node);
                                }
                            }
                            else {
                                next(node);
                            }
                        }
                        else {
                            next(node);
                        }
                    }
                });
            }

            scan(ul.children(), options.paths[on]);
        };

        state.currentNode = function(){
            return state.current.node;
        };
        //public methods
        state.select = function(options){
            function sync(node, item){
                $('.tree div.selected').removeClass('selected');
                node.addClass('selected');
                var event = jQuery.Event('treeNodeLeftClick', {node: node, item: item});
                state.trigger(event);
                if($.isFunction(state.opts.onTreeNodeLeftClick)){
                    state.opts.onTreeNodeLeftClick(event);
                }
            }
            var selected = state.worker.node.find('div[data-id="' + options.current[state.KEY_ID]  + '"]');
            var icon = selected.prev('span');
            var next = state.worker.node.find('div[data-id="' + options.next[state.KEY_ID]  + '"]');
            if($(next).length<=0){
                if($(next).length<=0){
                    console.log('The tree view is out of sync.');
                }
                else{
                    icon.on('treeViewDataLoaded', function(){
                        next = selected.find('div[data-id="' + options.next[state.KEY_ID]  + '"]');
                        if($(next).length>0){
                            sync(next, options.next);
                        }
                    });
                    icon.click();
                }
            }
            else{
                sync(next, options.next);
            }
        };
        state.remove = function(options) {
            function sync(node, item) {
                $('.tree div.selected').removeClass('selected');
                node.addClass('selected');
                var event = jQuery.Event('treeNodeLeftClick', {node: node, item: item});
                state.trigger(event);
                if($.isFunction(state.opts.onTreeNodeLeftClick)){
                    state.opts.onTreeNodeLeftClick(event);
                }
            }

            var selected = state.worker.node.find('div[data-id="' + options.current[state.KEY_ID] + '"]');
            if (selected) {
                selected.parent().remove();
            }
            var next = state.worker.node.find('div[data-id="' + options.next[state.KEY_ID] + '"]');
            if ($(next).length <= 0) {
                if ($(next).length <= 0) {
                    console.log('The tree view is out of sync.');
                }
                else {
                    if ($(next).length > 0) {
                        sync(next, options.next);
                    }
                }
            }
            else {
                sync(next, options.next);
            }
        };
        state.updateSize =  function (options) {
            if((state.opts.showCount || state.opts.totals) && (!options.item || !$.jCommon.is.numeric(options.item._count))) {
                var node = options.node;
                var query = options.query;

                function sizeChanged(hits) {
                    if (!node.header.innerTxt.badge) {
                        node.header.innerTxt.badge = $(document.createElement('span'));
                        node.header.innerTxt.prepend(node.header.innerTxt.badge);
                    }
                    if (state.opts.showCount || state.opts.totals) {
                        node.header.innerTxt.badge.html(hits>0 ? (hits + ": ") : '');
                    }
                }

                if ($.jCommon.is.numeric(options.size)) {
                    sizeChanged(options.size);
                }
                else {
                    var s = function (data) {
                        var hits = (data && data.hits) ? data.hits : 0;
                        sizeChanged(hits);
                    };
                    query.apiKey = '_count';
                    var url = ((state.opts.get.url) ? state.opts.get.url : '/query?start=0');
                    $.htmlEngine.request(url, s, s, query, 'post', true);
                }
            }
        };

        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.treeView.defaults = {
        counted: true,
        allowDuplicates: false,
        isDraggable: true,
        isDroppable: true,
        rootSelected: false,
        rootSelectable: true,
        defaultSelectable: false,
        expandRoot: true,
        expandable: true,
        disableContextMenu: false,
        paging: false,
        limit: 30,
        cls: 'circle',
        named: false,
        data: {
            list: {},
            selected: {}
        },
        node: {
            css: '',
            title: {
                css: ''
            },
            icon: {
                css: ''
            }
        },
        global: {
            css: 'root',
            title:{
                css: ''
            },
            icon:{
                cls: 'lusidity-font-color',
                css: {marginRight: '5px'}
            }
        }
    };


    //Plugin Function
    //noinspection JSUnusedLocalSymbols
    $.fn.treeView = function (method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function () {
                new $.treeView($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $treeView = $(this).data('treeView');
            switch (method) {
                case 'expandTo': $treeView.expandTo(options);break;
                case 'currentNode': return $treeView.currentNode();break;
                case 'add':
                    return $treeView.add(options);break;
                case 'select':
                    return $treeView.select(options);break;
                case 'remove':
                    return $treeView.remove(options);break;
                case 'updateSize':
                    return $treeView.updateSize(options);break;
                case 'state':
                default:
                    return $treeView;
            }
        }
    };

    $.treeView.call = function (elem, options) {
        elem.treeView(options);
    };

    try {
        $.htmlEngine.plugins.register("treeView", $.treeView.call);
    }
    catch (e) {
        console.log(e);
    }

})(jQuery);
