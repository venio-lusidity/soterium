;(function ($) {

    //Object Instance
    $.jEnclaves = function(el, options) {
        var state = el,
            methods = {};
        state.opts = $.extend({}, $.jEnclaves.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.current ={
            et_view: 'ditpr'
        };
        state.details ={
            asset: 'asset',
            count: 'count',
            enclave: 'enclave',
            iavm: 'iavm',
            ranked: 'ranked',
            risk: 'risk',
            top: 'top',
            vuln: 'vuln'
        };
        state._aUrl = '/pages/enclaves/assets/index.html';
        state._iUrl = '/iavms/compliance/iavm';

        // Store a reference to the environment object
        el.data("jEnclaves", state);

        // Private environment methods
        methods = {
            init: function() {
                state.opts.pnlNodeMiddle.css({overflow: 'hidden'});
                state.opts.nodeViewer.css({overflow: 'hidden'});
                if(state.opts.prsnify){
                    methods.prsnify.init();
                }
                methods.content.first();
                methods.favorites.init();
                methods.tree.init();
                methods.menu.init();
                methods.resize();
                lusidity.environment('onResize', function(){
                    methods.resize();
                });
                $(document).on('panel-collapse', function () {
                    methods.resize();
                });
            },
            exists: function (node) {
                 return (node && (node.length>0));
            },
            resize: function () {
                var h = $('.alert-zone').height();
                lusidity.resizePage((h*-1));
                var css = {height: 'inherit'};
                if(methods.exists(state.opts.nodeEdit)) {
                    state.opts.nodeEdit.css(css);
                }
                if(methods.exists(state.opts.addNode)) {
                    state.opts.addNode.css(css);
                }
            },
            content: {
                first: function () {
                    state.opts.nodeViewer.children().remove();

                    var c = dCrt('div');
                    var hd = dCrt('h4').html("Please select a favorite or a node in the Enclaves tree.").addClass('letterpress');
                    c.append(hd);

                    state.opts.nodeViewer.append(c);

                    var d = $.jCommon.element.getDimensions(c);
                    c.css({position: 'absolute', top: '50%', left: '50%', marginTop: ((d.h/2)*-1)+'px', marginLeft: ((d.w/2)*-1)+'px', textAlign: 'center'});
                },
                noData: function (node) {
                    node.children().remove();
                    var c = dCrt('div');
                    node.append(c);
                    var hd = dCrt('h4').html("No data found.").addClass('letterpress').css({margin: '20px 10px 0 10px'});
                    var t1 = dCrt('p').css({margin: '10px'})
                        .html('If you selected from favorites and your node is an Organization and the Enclaves selection is System Name try selecting Managed By or Owned By.');
                    var t2 = dCrt('p').css({margin: '10px'})
                        .html('If you selected from favorites and your node is a System Name/Enclave and the Enclaves selection Managed By or Owned By try selecting a System Name.');
                    var t3 = dCrt('p').css({margin: '10px'})
                        .html('There will be an update to fix this behavior.');
                    c.append(hd).append(t1).append(t2).append(t3);
                },
                ready: function (data) {
                    var r = true;$.each(state.details, function (key, value) {if(undefined===data._sd[value]){r = false;}return r;});return r;
                },
                init: function () {
                    if(!methods.exists(state.opts.nodeViewer)){
                        return false;
                    }
                    state.opts.nodeViewer.children().remove();
                    var nv = dCrt('div').css({height: 'inherit', width: 'inherit'});
                    state.opts.nodeViewer.append(nv);
                    //if et_view is ditprId, should return same data as ditpr
                    var cv = state.current.et_view;
                    if($.jCommon.string.equals(cv, 'ditprId', true)){
                        cv = "ditpr"
                    }
                    if(!state.current.item._dsId){
                        state.current.item._dsId = $.jCommon.getRandomId("dsId")
                    }
                    nv.pSummary({adjustHeight: 8, requests: state.requests, "export": true, data: state.current.item, dashboard: false, et_view: state.current.item.et_view ?  state.current.item.et_view : cv});
                },
                reset: function () {
                    methods.menu.add.init();
                    methods.menu.edit.init();
                    if(methods.exists(state.opts.nodeViewer)){
                        state.opts.nodeViewer.children().remove();
                        state.opts.nodeViewer.show();
                    }
                }
            },
            favorites: {
                init: function () {
                    if(!methods.exists(state.opts.nodeFav)){
                        return false;
                    }
                    state.opts.nodeFav.children().remove();
                    state.opts.treeFavNode = $.htmlEngine.panel(state.opts.nodeFav, 'glyphicons-star-empty', "Favorites", null, false);
                    state.opts.treeFavNode.on('treeNodeCreated', function (e) {
                        var t = $(e.node.find('.title-header')).first().first();
                        var tp;
                        if (e.item.et_view && t) {
                            switch (e.item.et_view) {
                                case 'ditpr':
                                    tp = 'System Name';
                                    break;
                                case 'ditprId':
                                    tp = 'System ID';
                                    break;
                                case 'managed':
                                    tp = 'Managed By';
                                    break;
                                case 'owned':
                                    tp = 'Owned By';
                                    break;
                                case 'location':
                                    tp = 'Location';
                                    break;
                            }
                            var ht = String.format('{0}: {1}{2}', tp, e.item.ditprId ? e.item.ditprId+' ':'', e.item.title);
                            t.html(ht);
                        }
                        var view = e.item.et_view;
                        if(tp ==='System ID'){
                            view = 'ditpr';
                        }
                        var sp = dCrt('span').css({margin: '0 5px 0 5px'});
                        var init = false;
                        var s = function (data) {
                            if(!data || !data.count || !data.count.inherited){
                                return false;
                            }
                            else if(data.count.inherited>0 || !init) {
                                e.item.node.show();
                                init = true;
                                if ($.jCommon.string.startsWith(e.item.et_view, "ditpr")) {
                                    t.prepend(sp.html(String.format('{0}:', data.count.inherited > data.count.exact ? data.count.inherited : data.count.exact)));
                                }
                                else {
                                    t.prepend(sp.html(String.format('{0} [{1}]:', data.count.inherited, data.count.exact)));
                                }
                            }
                        };
                        var f = function (j, t, e) {
                        };
                        $.htmlEngine.request(methods.url(state.details.count, e.item, view), s, f, null, 'get', true);
                    });
                    state.opts.treeFavNode.jEnclavesFavs({
                        rootSelected: !state.opts.favTreeInit,
                        onTreeNodeLeftClick: methods.tree.bind(state.opts.treeFavNode, true)
                    });
                    state.opts.favTreeInit = true;
                }
            },
            menu:{
                init: function () {
                    if(state.opts.nodeMenu) {
                        methods.menu.add.init();
                        methods.menu.edit.init();

                        var success = function(data){
                            if(data){
                                methods.favorites.init();
                            }
                        };

                        state.opts.nodeViewer.unbind('menu-bar-fav');
                        state.opts.nodeViewer.on('menu-bar-fav', function () {
                            var etv = state.current.item.et_view ? state.current.item.et_view : state.current.et_view;
                            $.htmlEngine.request('/rmk/favorites/infrastructure', success, success, {et_view: etv, id: state.current.item[state.KEY_ID], "default": false}, 'post');
                        });

                        state.opts.nodeViewer.unbind('menu-bar-default-fav');
                        state.opts.nodeViewer.on('menu-bar-default-fav', function () {
                            var etv = state.current.item.et_view ? state.current.item.et_view : state.current.et_view;
                            $.htmlEngine.request('/rmk/favorites/infrastructure', success, success, {et_view: etv, id: state.current.item[state.KEY_ID], "default": true}, 'post');
                        });
                        /*Menu item for tour - disabled at this time */
                        state.opts.nodeViewer.unbind('menu-bar-help');
                        state.opts.nodeViewer.on('menu-bar-help', function () {
                            methods.startTour();
                        });

                        var cvb = $('#content-view-btn');

                        var g1 ='glyphicons glyphicons-table';
                        var g2 ='glyphicons glyphicons-show-thumbnails';
                        function getView(type){
                            var t="tiles";
                            var c=g2;
                            if(type==='details'){
                                t='details';
                                c=g1;
                            }
                            return {t:t,c:c};
                        }
                        function alter(save){
                            var a,r;
                            state.current.view = (save) ? ((state.current.view ==='tiles') ? 'details' : 'tiles'):state.current.view;
                            a=getView((state.current.view ==='details') ? 'tiles' : 'details');
                            r=getView(state.current.view);
                            if(save) {
                                var onSuccess = function (data) {
                                };
                                var onFailed = function () {
                                };
                                $.htmlEngine.request("/rmk/favorites/infrastructure", onSuccess, onFailed, {enclaveView: state.current.view}, 'post');
                            }
                            return {a:a, r:r};
                        }
                        function change(save) {
                            var t = state.current.view;
                            var a = alter(save);
                            cvb.attr('title', a.a.t);
                            cvb.find('span').removeClass(a.r.c).addClass(a.a.c);
                            if(save){
                                methods.html.content.init(true);
                            }
                            else{
                                state.current.view = t;
                            }
                        }
                        change(false);

                        $(document).unbind('menu-bar-content-view');
                        $(document).on('menu-bar-content-view', function() {
                            change(true);
                        });

                        if (state.opts.addNode) {
                            $(document).unbind('menu-bar-add');
                            $(document).on('menu-bar-add', function () {
                                methods.html.form(state.opts.addNode, 'Add an Enclave', null, true);
                            });
                        }

                        if (state.opts.editNode) {
                            $(document).unbind('menu-bar-edit');
                            $(document).on('menu-bar-edit', function () {
                                var title = state.current.item.title;
                                if (!methods.isExcluded(title)) {
                                    methods.html.form(state.opts.editNode, 'Editing: ' + title, state.current.item, true);
                                }
                                else {
                                    lusidity.info.yellow('You cannot edit <strong>' + title + '</strong>.');
                                    lusidity.info.show(2);
                                }
                            });
                        }
                        if(state.current.item) {
                            var title = state.current.item.title;
                            if ($.jCommon.string.equals(title, 'enterprise', true)) {
                                state.opts.nodeMenu.menuBar('show', {action: 'add'});
                                state.opts.nodeMenu.menuBar('hide', {action: 'edit'});
                            }
                            else if ($.jCommon.string.equals(title, 'unmanaged', true)) {
                                state.opts.nodeMenu.menuBar('hide', {action: 'add'});
                                state.opts.nodeMenu.menuBar('hide', {action: 'edit'});
                            }
                            else {
                                state.opts.nodeMenu.menuBar('show', {action: 'add'});
                                state.opts.nodeMenu.menuBar('show', {action: 'edit'});
                            }
                        }
                    }
                },
                add:{
                    init: function () {
                        if(!methods.exists(state.opts.addNode)) {
                            return false;
                        }
                        state.opts.addNode.css({display: 'none'}).children().remove();
                    }
                },
                edit:{
                    init: function () {
                        if(!methods.exists(state.opts.nodeEdit)) {
                            return false;
                        }
                        state.opts.nodeEdit.css({display: 'none'}).children().remove();
                    }
                }
            },
            startTour: function () {
                $.tourHelper.tour.load("/pages/enclaves/tour.json");
                var s = function (data) {};
                $.htmlEngine.request('/personalization', s, s, {questions: {tour: {watched: true}}}, 'post');
            },
            prsnify: {
                init: function () {
                    if (state.opts.prsnify) {
                        var s = function (data) {
                            if (data) {
                                if (data.enclaveView) {
                                    state.current.view = data.enclaveView;
                                }
                                if(data.et_view){
                                    state.current.et_view = data.et_view;
                                }
                                if(state.current.et_view === 'enclave')
                                {
                                    state.current.et_view = 'ditpr';
                                }
                            }
                        };
                        var f = function () {};
                        $.htmlEngine.request("/rmk/favorites/infrastructure", s, f, null, 'get', false);
                    }
                }
            },
            url: function (detail, data, view) {
                var cv = state.current.et_view;
                //if et_view is ditprId, return same data as ditpr
                if($.jCommon.string.equals(cv, 'ditprId', true)){
                    cv = "ditpr"
                }
                return String.format('{0}/hierarchy/details?detail={1}&view={2}', (data ? data[state.KEY_ID] : state.current.item[state.KEY_ID]), detail, view ? view : cv);
            },
            tree: {
                bind: function (container, isFavorite) {
                    return function(e){
                        $.htmlEngine.busy(state.opts.pnlNodeLeft, {type: 'cube', cover: true, adjustWidth: 0, adjustHeight: 30});
                        try {
                            if (methods.exists(state.opts.nodeViewer)) {
                                state.opts.nodeViewer.children().remove();
                            }
                            if (state.opts.treeFavNode) {
                                state.opts.treeFavNode.find('.selected').removeClass('selected');
                            }
                            if (state.opts.treeNode) {
                                state.opts.treeNode.find('.selected').removeClass('selected');
                            }
                            //lusidity.environment('ajaxAbort');
                            var item = (e.item) ? e.item : e.node.data('item');
                            var items = $('li[data-id="' + item[state.KEY_ID] + '"]');
                            state.current.last = state.current.item;
                            state.current.item = item;
                            methods.content.reset();
                            var cId = state.current.item[state.KEY_ID];
                            var id = item[state.KEY_ID];

                            if (e.node.icon && e.node.icon.hasClass('glyphicon-plus')) {
                                //e.node.icon.click();
                            }
                            if (!isFavorite) {
                                state.current.treeNode = e.node;
                            }
                            state.current.isEnclave = $.jCommon.string.startsWith(item['vertexType'], '/electronic/system/enclave');
                            state.current.isLoc = $.jCommon.string.startsWith(item['vertexType'], '/location/location');
                            state.current.isOrg = $.jCommon.string.startsWith(item['vertexType'], '/organization/organization');

                            methods.content.init();
                            if (item._selectable) {
                                var t = e.node.find('div.title-header');
                                if (t) {
                                    $(t[0]).addClass('selected');
                                }
                            }
                        }
                        catch (e) {
                            console.log(e);
                        }
                        window.setTimeout(function () {
                            state.opts.pnlNodeLeft.loaders('hide');
                        }, 500);
                    }
                },
                dropdown: function (txt) {
                    var r = dCrt('div').addClass('dropdown').attr('id', "dd1").css({display: 'inline-block'});
                    var b1= dCrt('button').addClass('btn btn-default dropdown-toggle')
                        .attr('type', 'button').attr('data-toggle', "dropdown")
                        .attr('aria-haspopup', 'true').attr('aria-expanded', 'true').html(dCrt('span').html(txt));
                    var s1 = dCrt('span').addClass('caret').css({"float": 'right', marginTop: "8px"});
                    return r.append(b1.append(s1));
                },
                init: function () {
                    if(!methods.exists(state.opts.pnlNodeLeft)){
                        return false;
                    }
                    var menu = methods.tree.menu();
                    var t = dCrt('span').html('Enclaves');
                    state.opts.nodeTree.children().remove();
                    state.opts.treeNode = $.htmlEngine.panel(state.opts.nodeTree, "/assets/img/types/virtual_enclave.png", t, null, false, null, menu);
                    $.htmlEngine.busy(state.opts.nodeTree, {type: 'cube', cover: true, adjustWidth: 0, adjustHeight: 0});
                    state.opts.treeNode.on('treeViewDataLoaded', function () {
                        state.opts.nodeTree.loaders('hide');
                        state.opts.treeNode.unbind('treeViewDataLoaded');
                    });
                    var init = false;
                    state.opts.treeNode.on('treeNodeCreated', function (e) {
                        var ro = $.jCommon.string.contains(e.item.title, "Root Organizations", true);
                        var rt = ro || $.jCommon.string.equals(e.item.title, "Enterprise", true);
                        var n = {visibility: 'hidden', display: 'none'};
                        var d = {visibility: 'visible', display: ''};
                        e.item.node.css(n);
                        var t = $(e.item.node.find('.title-header')[0]);
                        if(e.item.ditprId){
                            t.first().html(String.format('{0} {1}', e.item.ditprId, e.item.title));
                        }
                        else{
                            t.first().html(e.item.title);
                        }
                        if (!state.current.item) {
                            state.current.item = e.item;
                        }
                        if (e.item._selectable && e.item.lid === state.current.item.lid && state.current.item.title!=='Root Organizations') {
                            t.addClass('selected');
                        }
                        var load = true;
                        if ($.jCommon.string.contains(e.item.title, "tag issues", true)) {
                            e.item.node.show();
                            if(state.current.et_view === 'managed' && $.jCommon.string.contains(e.item.title, "owned", true)) {
                                load = false;
                                e.item.node.hide();
                            }
                            if(state.current.et_view === 'owned' && $.jCommon.string.contains(e.item.title, "managed", true)) {
                                load = false;
                                e.item.node.hide();
                            }

                            e.node.on("treeNodeAfterSort", function () {
                                e.item.node.parent().prepend(e.item.node);
                            });
                        }
                        if(load) {
                            var sp = dCrt('span').css({margin: '0 5px 0 5px'});
                            var icon;
                            if(e.node.parent().is('ul')) {
                                var ul = e.node.parent();
                                icon = $(ul.parent().children()[0]);
                                if (icon && icon.is('span')) {
                                    icon.css(n);
                                }
                            }
                            var s = function (data) {
                                if (data.count.inherited > 0 || !init) {
                                    e.item.node.css(d);
                                    init = true;
                                    if(icon) {
                                        icon.css(d);
                                    }
                                }
                                var nd = e.node;
                                if(data.count.inherited === 0 || data.count.inherited === data.count.exact) {
                                    nd.jNodeReady({
                                        onReady: function () {
                                            if (nd.icon && data.count.inherited === 0) {
                                                window.setTimeout(function () {
                                                    nd.css({display: 'none', visibility: 'hidden'});
                                                }, 500);
                                            }
                                            else if (data.count.inherited === data.count.exact) {
                                                var t = 0;

                                                function hide() {
                                                    if (!nd.icon) {
                                                        if (t >= 30) {
                                                            return false;
                                                        }
                                                        t++;
                                                        window.setTimeout(function () {
                                                            hide();
                                                        }, 300);
                                                    }
                                                    else {
                                                        nd.icon.css({display: 'none', visibility: 'hidden'});
                                                    }
                                                }

                                                hide();
                                            }
                                        }
                                    });
                                }

                                if($.jCommon.string.startsWith(state.current.et_view, "ditpr")){
                                    t.prepend(sp.html(String.format('{0}:', data.count.inherited>data.count.exact ? data.count.inherited : data.count.exact)));
                                }
                                else {
                                    t.prepend(sp.html(String.format('{0} [{1}]:', data.count.inherited, data.count.exact)));
                                }
                            };
                            var f = function (j, t, e) {
                            };
                            if(e.item._counted){
                                s({count: {inherited: e.item._counted.inherited, exact: e.item._counted.exact}});
                            }
                            else if($.jCommon.is.numeric(e.item._count)){
                                s({count: {inherited: e.item._count, exact: rt ? 0 : e.item._count}});
                            }
                            else {
                                $.htmlEngine.request(methods.url(state.details.count, e.item), s, f, null, 'get', true);
                            }
                        }
                    });
                    state.opts.treeNode.jEnclavesTree({et_view: state.current.et_view,
                    onTreeNodeLeftClick: methods.tree.bind(state.opts.treeNode, false)});
                },
                menu: function () {
                    var options = [
                        {
                            label: "Location",
                            value: "location",
                            "default": $.jCommon.string.equals("location", state.current.et_view)
                        },
                        {
                            label: "Managed By",
                            value: "managed",
                            "default": $.jCommon.string.equals("managed", state.current.et_view)
                        },
                        {
                            label: "Owned By",
                            value: "owned",
                            "default": $.jCommon.string.equals("owned", state.current.et_view)
                        },
                        {
                            label: "System ID",
                            value: "ditprId",
                            "default": $.jCommon.string.equals("ditprId", state.current.et_view)
                        },
                        {
                            label: "System Name",
                            value: "ditpr",
                            "default": $.jCommon.string.equals("ditpr", state.current.et_view)
                        }
                    ];
                    var menu = methods.tree.dropdown('Select enclave view...').css({marginTop: "4px"});
                    var u = dCrt('ul').addClass('dropdown-menu').css({left: "0px"});
                    menu.append(u);
                    var dd = menu.find('.btn');
                    dd.css({minWidth: "160px", textAlign: "left"});
                    function ct(txt) {
                        if (dd) {
                            if (dd.children().length > 0) {
                                $(dd.children()[0]).remove();
                            }
                            dd.prepend(dCrt('span').html(txt))
                        }
                    }

                    $.each(options, function () {
                        var item = this;
                        var l = dCrt('li');
                        var a = dCrt('a').attr('href', '#').addClass('dropdown-toggle').html(item.label);
                        u.append(l.append(a));
                        l.on('click', function () {
                            var view = item.value;                             
                            ct(item.label);
                            var s = function (data) {
                                state.current.et_view = item.value;
                                methods.content.reset();
                                if(state.opts.treeFavNode){
                                    state.opts.treeFavNode.find('.selected').removeClass('selected');
                                }
                                methods.tree.init();
                            };
                            $.htmlEngine.request("/rmk/favorites/infrastructure", s, s, {et_view: view, et_label: item.label}, 'post');
                        });
                        if (item["default"]) {
                            ct(item.label);
                        }
                    });
                    return menu;
                }
            }
        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.jEnclaves.defaults = {};


    //Plugin Function
    $.fn.jEnclaves = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.jEnclaves($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $jEnclaves = $(this).data('jEnclaves');
            switch (method) {
                case 'exists': return (null!==$jEnclaves && undefined!==$jEnclaves && $jEnclaves.length>0);
                case 'state':
                default: return $jEnclaves;
            }
        }
    };

})(jQuery);
