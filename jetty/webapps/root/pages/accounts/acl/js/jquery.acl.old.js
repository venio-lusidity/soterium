;(function ($) {

    //Object Instance
    $.acl = function(el, options) {
        var state = el,
            methods = {};
        state.opts = $.extend({}, $.acl.defaults, options);
        state.current = {
            deleteNodes:[],
            addNodes:[],
            items: []
        };
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.KEY_P = '/people/person';
        state.KEY_SG = '/acs/security/authorization/group';
        state.KEY_SGE = '/object/edge/principal_edge';
        state.KEY_SGS = '/acs/security/base_principal/principals';
        state.KEY_DESC = '/system/primitives/raw_string/descriptions';
        state.KEY_IDENTITY = '/acs/security/identity';
        state.adding = false;
        state.done = true;

        var pts = {
            people: {
                title: 'People',
                formTitle: 'Create Person',
                type: '/people/person',
                label: 'User',
                glyph: 'glyphicons glyphicons-user',
                leftView: 'table'
            },
            logs: {
                title: 'Logs',
                label: 'logs',
                glyph: 'glyphicons glyphicons-list',
                leftView: 'table',
                list: {
                    hits: 1,
                    limit: 20,
                    next: 20,
                    results: [
                        {'title': 'User Logs'}
                    ]
                },
                hasMenu: false
            },
            groups: {
                title: 'Groups',
                formTitle: 'Create Group',
                type: state.KEY_SG,
                key: state.KEY_SGS,
                label: 'Group',
                glyph: 'glyphicons glyphicons-group',
                leftView: 'tree',
                hasMenu: true,
                queries: {
                    root: function () {
                        return {
                            domain: state.KEY_SG,
                            type: state.KEY_SG,
                            'native': {query: {filtered: {filter: {bool: {must: [{term: {'title.raw': 'Root Security Group'}}]}}}}}}
                    },
                    children: function(item){
                        return {
                            domain: state.KEY_SGE,
                            type: state.KEY_SG,
                            lid: item.lid,
                            'native': {query: {filtered: {filter: {bool: {must: [
                                {term: {'/object/endpoint/endpointFrom.relatedId.raw': item.lid}},
                                {term: {'label.raw': state.KEY_SGS}}
                            ]}}}}}
                        }
                    }
                }
            },
            scoped: {
                title: 'Scoped',
                formTitle: 'Create Group',
                type: state.KEY_SG,
                key: state.KEY_SGS,
                glyph: 'glyphicons glyphicons-riflescope',
                leftView: 'tree',
                hasMenu: false,
                queries: {
                    root: function () {
                        return {
                            domain: state.KEY_IDENTITY,
                            type: state.KEY_IDENTITY,
                            sort: {on: 'title', direction: 'desc'},
                            'native': {query: {filtered: {filter: {bool: {must: [
                                {term: {'loginType.folded': 'pki'}}
                            ]}}}}}};
                    },
                    children: function(){
                        return null
                    }
                }
            },
            users: {
                title: 'User Identities',
                list: {
                    hits: 3,
                    limit: 20,
                    next: 20,
                    results: [
                        {'title': 'Active', status: 'approved', deprecated: false},
                        {'title': 'Deprecated', status: 'disapproved', deprecated: true},
                        {'title': 'Inactive', status: 'waiting', deprecated: false},
                        {'title': 'All', status: 'all', deprecated: false, children: true}
                    ]
                },
                type: state.KEY_SG,
                key: state.KEY_SGS,
                label: 'User Identities',
                glyph: 'glyphicons glyphicons-user',
                leftView: 'table',
                hasMenu: false,
                queries: {
                    root: function () {
                        return null;
                    },
                    children: function(item) {
                        var r;
                        if (item.status === 'all') {
                            r = {
                                domain: state.KEY_IDENTITY,
                                type: state.KEY_IDENTITY,
                                lid: item.lid,
                                sort: {on: 'lastLoggedIn', direction: 'desc'},
                                'native': {query: {filtered: {filter: {bool: {must: [
                                    {term: {'loginType.folded': 'pki'}}
                                ]}}}}}};
                        }
                        else {
                            r = {
                                domain: state.KEY_IDENTITY,
                                type: state.KEY_IDENTITY,
                                lid: item.lid,
                                sort: {on: 'lastLoggedIn', direction: 'desc'},
                                'native': {query: {filtered: {filter: {bool: {must: [
                                    {term: {'status.folded': item.status}},
                                    {term: {'deprecated': item.deprecated}},
                                    {term: {'loginType.folded': 'pki'}}
                                ]}}}}}};
                        }
                        return r;
                    }
                }
            },
            api: {
                title: 'API Keys',
                list: {
                    hits: 3,
                    limit: 20,
                    next: 20,
                    results: [
                        {'title': 'Active', status: 'approved', deprecated: false},
                        {'title': 'Deprecated', status: 'disapproved', deprecated: true},
                        {'title': 'Inactive', status: 'waiting', deprecated: false}
                    ]
                },
                type: state.KEY_SG,
                key: state.KEY_SGS,
                label: 'API Keys',
                glyph: 'glyphicons glyphicons-user-key',
                leftView: 'table',
                hasMenu: false,
                queries: {
                    root: function () {
                        return null;
                    },
                    children: function(item){
                        return {
                            domain: state.KEY_IDENTITY,
                            type: state.KEY_IDENTITY,
                            sort: {on: 'title', direction: 'asc'},
                            lid: item.lid,
                            'native': {query: {filtered: {filter: {bool: {must: [
                                {term: {'status.folded': item.status}},
                                {term: {'deprecated': item.deprecated}},
                                {term: {'loginType.folded': 'apikey'}}
                            ]}}}}}
                        }
                    }
                }
            }
        };

        // Store a reference to the environment object
        el.data('acl', state);

        // Private environment methods
        methods = {
            init: function() {
                methods.pnlMiddle.init();
                methods.pnlLeft.init();
                methods.pnlRight.init();
                methods.api.init();
            },
            api:{
                init: function () {
                    var btn = $('#api_key_btn');
                    btn.on('click', function () {
                        var v = $('#api_key_url').val();
                        if(v){
                            var s = function (data) {
                                var d = $('#api_key_result');
                                d.children().remove().html();
                                if(data && data.key) {
                                    d.val(data.key);
                                    d.select();
                                }
                            };
                            var f = function () {};
                            $.htmlEngine.request('/admin/api_key?provider=x509&identity=' + v, s, f, null, 'get');
                        }
                    });
                }
            },
            clear: function(){
                state.current.item = null;
                state.current.node = null;
                state.tbody = null;
            },
            draggable: function (node, data) {
                if (state.contentNode.is(':visible')) {
                    node.data('item', data);
                    node.draggable({
                        cursor: 'move',
                        revert: true,
                        scroll: false,
                        start: function () {
                            $(this).addClass('drop-area-active').css({zIndex: 99999999});
                        },
                        stop: function () {
                            $(this).removeClass('drop-area-active').css({zIndex: 999999});
                        },
                        helper: function () {
                            return $(this).clone().appendTo('body').show();
                        }
                    });
                }
            },
            droppable: function (dropArea) {
                if (dropArea) {
                    dropArea.droppable({
                        hoverClass: 'drop-area-hover',
                        activeClass: 'drop-area-active',
                        drop: function (event, ui) {
                            event.stopPropagation();
                            window.event.cancelBubble = true;
                            if (ui.draggable.hasClass('result')) {
                                $.htmlEngine.busy(dropArea);
                                var success = function (data) {
                                    if (!data.error) {
                                        methods.html.makeRow(['del', 'title'], data.to);
                                        state.current.node.trigger('size-changed');
                                        ui.draggable.remove();
                                    }
                                    else {
                                        lusidity.info.red(data.error);
                                        lusidity.info.show(5);
                                    }
                                    dropArea.loaders('hide');
                                };
                                var failed = function (jqXHR, textStatus, errorThrow) {
                                    dropArea.loaders('hide');
                                };
                                ui.draggable.siblings().removeClass('active');
                                ui.draggable.addClass('active');
                                window.setTimeout(function () {
                                    var url = methods.link.url(ui.draggable.data('item'));
                                    var data = methods.link.data(state.current.item, ui.draggable.data('item'));
                                    $.htmlEngine.request(url, success, failed, data, 'post');
                                }, 100);
                            }
                        }
                    });
                }
            },
            html: {
                panel: function(container, glyph, title, url, borders, actions, menu){
                    var result = $(document.createElement('div'));
                    var options = {
                        glyph: glyph,
                        title: title,
                        url: url,
                        borders: borders,
                        content: result,
                        actions: actions ? actions : [],
                        menu: menu
                    };
                    container.panel(options);
                    return result;
                },
                formCancel: function () {
                    function cancel(node) {
                        if (node && node.formBuilder('exists')) {
                            node.formBuilder('cancel');
                        }
                    }
                    methods.html.tiles.setTileHeader();
                },
                table: function(container, data){
                    container.children().remove();
                    var content = $(document.createElement('table')).addClass('table table-hover');
                    container.append(content);

                    if(data && data.results) {
                        var heads = ['Remove', 'User'];
                        var properties = ['del', 'title'];
                        if ($.jCommon.json.hasProperty(data.results[0], 'status')) {
                            heads = ['Status', 'Deprecated', 'User', 'Last Login', 'Contact Details'];
                            properties = ['status', 'deprecated', 'title', 'lastLoggedIn', 'details'];
                        }
                        methods.html.getTableHead(heads, content);
                        var items = $.jCommon.array.sort(data.results,[{property: 'lastLoggedIn', asc: false}]);
                        methods.html.getTableBody(properties, items, content, 0);
                    }
                },
                getTableHead: function(headers, container){
                    var thead = $(document.createElement('thead'));
                    var row = $(document.createElement('tr'));
                    $.each(headers, function(){
                        row.append($(document.createElement('th')).html(this));
                    });
                    thead.append(row);
                    container.append(thead);
                },
                getTableBody: function(properties, items, container, on, msg){
                    if(!state.tbody || state.tbody.length<1){
                        state.tbody = $(document.createElement('tbody'));
                    }
                    function load(){
                        results = $.jCommon.array.sort(results, true, ['title']);
                        $.each(results, function(){
                            methods.html.makeRow(properties, this);
                        });
                    }
                    var results = [];
                    $.each(items, function() {
                        if (!$.jCommon.json.hasProperty(this,'/object/endpoint/endpointFrom.relatedId')) {
                            results.push(this);
                        }
                        else {
                            var f =this['/object/endpoint/endpointFrom'].relatedId.replace('#', '');
                            var t =this['/object/endpoint/endpointTo'].relatedId.replace('#', '');
                            var id = $.jCommon.string.contains(state.current.item[state.KEY_ID],f)?t:f;
                            var s = function (item) {
                                results.push(item);
                            };
                            var url = '/vertices/' + id;
                            $.htmlEngine.request(url, s, null, null, 'get');
                        }
                    });

                    function check(){
                        if(results.length===items.length){
                            load();
                        }
                        else{
                            window.setTimeout(check, 100);
                        }
                    }
                    container.append(state.tbody);
                    check();
                },
                makeRow: function(properties, item){
                    var row = $(document.createElement('tr')).addClass('table-row');
                    state.tbody.append(row);

                    $.each(properties, function(){
                        var key = this.toString();
                        var value;
                        var td = $(document.createElement('td')).addClass('css_' + key);
                        if($.jCommon.string.equals(key, 'del')){
                            td.css({width: '50px', maxWidth: '50px'});
                            value = $(document.createElement('span')).attr('title', 'Select to remove from ' + state.current.item.title).addClass('glyphicon glyphicon-remove').css({fontSize: '16px', cursor: 'pointer', color: 'red'});
                            value.on('click', function(){
                                var s = function(data){
                                    row.remove();
                                    state.current.node.trigger('size-changed');
                                };
                                var f = function(){
                                    lusidity.info.red(msg);
                                    lusidity.info.show(5);
                                };
                                var url = methods.link.url(item);
                                var d = methods.link.data(state.current.item, item);
                                d["delete"] = true;
                                $.htmlEngine.request(url, s, f, d, 'post');
                            });
                            if(item.status && item.status === 'processing'){
                                value.hide();
                            }
                        }
                        else if($.jCommon.string.equals(key, 'tableLineNumber')){
                            on++;
                            td.css({width: '50px', maxWidth: '50px'});
                            value = $(document.createElement('span')).html(on);
                        }
                        else if($.jCommon.string.equals(key, 'lastLoggedIn')){
                            value = $(document.createElement('a')).html($.jCommon.dateTime.defaultFormat(item[key]));
                        }
                        else if(key==='deprecated'){
                            td.css({width: '100px', maxWidth: '100px'});
                            var cb = $(document.createElement('input')).attr('type', 'checkbox');
                            if(item[key]){
                                cb.attr('checked', 'checked');
                            }
                            value = $(document.createElement('span')).append(cb);
                            cb.on('click', function () {
                                var checked = cb.is(':checked');
                                item.deprecated = checked;
                                if(checked) {
                                    item.status = 'disapproved';
                                }
                                var s = function (data) {
                                    row.remove();
                                    methods.pnlLeft.init();
                                };
                                $.htmlEngine.request(item[state.KEY_ID], s, null, item, 'post', true);
                            });
                        }
                        else if(key==='status'){
                            td.css({width: '150px', maxWidth: '150px'});
                            var onClick = function (node, data) {
                                data.item.status = data.value;
                                var s = function (data) {
                                    row.remove();
                                    methods.pnlLeft.init();
                                };
                                $.htmlEngine.request(item[state.KEY_ID], s, null, data.item, 'post', true);
                            };
                            value = $.htmlEngine.dropDown([
                                    {label: 'Approved', value: 'approved', onClick: onClick, selected: $.jCommon.string.equals(item.status, 'approved'), item: item},
                                    {label: 'Disapproved', value: 'disapproved', onClick: onClick, selected: $.jCommon.string.equals(item.status, 'disapproved'), item: item},
                                    {label: 'Waiting', value: 'waiting', onClick: onClick, selected: $.jCommon.string.equals(item.status, 'waiting'), item: item}
                                ]);
                        }
                        else if(key==='title') {
                            var link = (item.principalUri ? item.principalUri : item[state.KEY_ID]);
                            value = $(document.createElement('a')).html(item[key])
                                .attr('href', link).attr('target', '_blank');
                        }
                        else if(key==='details'){
                            var details = item['/common/base_contact_detail/contactDetails'];
                            if(details){
                                value = $(document.createElement('ul')).addClass('list-group');
                                $.each(details, function () {
                                    if(!$.jCommon.string.empty(this.value)) {
                                        var li = $(document.createElement('l')).addClass('list-group-item');
                                        value.append(li);
                                        var l = $(document.createElement('span')).html(this.vertexLabel);
                                        li.append(l);
                                        var c;
                                        if (this.vertexType === '/common/email') {
                                            c = $(document.createElement('a'))
                                                .attr('href', 'mailto:' + this.value).html(this.value);
                                        }
                                        else if (this.vertexType === '/common/phone_number') {
                                            c = $(document.createElement('span'))
                                                .html($.jCommon.string.toPhoneNumber(this.value));
                                        }
                                        else {
                                            c = $(document.createElement('span')).html(this.value);
                                        }
                                        li.append(c.css({marginLeft: '5px'}));
                                    }
                                });
                            }
                        }
                        else{
                            value = $(document.createElement('span')).html(item[key]);
                        }
                        td.css({marginRight: '10px'}).append(value);
                        row.append(td);
                    });
                }
            },
            link: {
                url: function (item) {
                    var id = item.uri ? item.uri : item[state.KEY_ID];
                    return (id + '/properties' + state.current.pt.key);
                },
                data: function (from, to) {
                    return {
                        from: from,
                        to: to
                    };
                }
            },
            pnlLeft: {
                init: function () {
                    state.current = {
                        deleteNodes:[],
                        addNodes:[],
                        items: []
                    };
                    state.opts.pnlLeftNode.children().remove();
                    methods.pnlLeft.make(pts.groups);
                    methods.pnlLeft.make(pts.users);
                    methods.pnlLeft.make(pts.api);
                    methods.pnlLeft.logs(pts.logs);
                },
                logs: function (pt) {
                    var container = $(document.createElement('div'));
                    state.opts.pnlLeftNode.append(container);
                    var options = !pt.hasMenu ? null : {
                        target: container,
                        buttons: []
                    };
                    var pnl = methods.html.panel(container, pt.glyph, pt.title, null, false, null, options);
                    var clicked = function(node, item) {
                        var selected = state.opts.pnlLeftNode.find('.selected');
                        if (selected.length > 0) {
                            selected.removeClass('selected');
                        }
                        state.current.item = item;
                        state.current.node = node;
                        state.tbody = null;
                        state.current.pt = pt;
                        methods.pnlMiddle.logs.init(pt);
                    };
                    methods.pnlLeft.list(pnl, clicked, pt.list, pt);
                },
                remove: function(item, pt){
                    var btnBar = $(document.createElement('div')).addClass('btn-bar');
                    var delButton = $(document.createElement('button')).attr('type', 'button').addClass('btn btn-danger').html('Delete');
                    btnBar.append(delButton);
                    delButton.on('click', function () {
                        pageCover.busy(true);
                        state.formNode.hide().children().remove();
                        state.contentNode.hide().children().remove();
                        var s = function (data) {
                            methods.pnlLeft.init();
                            pageCover.busy(false);
                        };
                        var f = function (j, t, e) {
                            state.pageModal('hide');
                            pageCover.busy(false);
                        };
                        state.pageModal('hide');
                        $.htmlEngine.request(item[state.KEY_ID], s, f, null, 'delete');
                    });

                    var cancel = $(document.createElement('button')).attr('type', 'button')
                        .addClass('btn btn-default btn-info').html('Cancel');
                    btnBar.append(cancel);

                    cancel.on('click', function(){
                        state.pageModal('hide');
                    });

                    if(!state.pageModal('exists')) {
                        state.pageModal();
                    }

                    state.pageModal('show', {
                        glyph: 'glyphicon-warning-sign yellow',
                        header: 'Delete confirmation required.',
                        body: function(body){
                            var title = item.title;
                            var msg = $(document.createElement('div')).css({verticalAlign: 'middle', height: '32px'});
                            var question = $(document.createElement('div')).html('Click Delete to delete "<strong>' + title + '</strong>".');
                            msg.append(question);
                            var statement = $(document.createElement('p')).html(
                                'Once Deleted, there is no way to recover "<strong>' + title + '</strong>".'
                            );
                            body.append(statement).append(msg);
                        },
                        footer: btnBar,
                        hasClose: true
                    });
                },
                process: function(pnl, clicked, query, pt) {
                    if (null !== query) {
                        var s = function (data) {
                            if (data) {
                                methods.pnlLeft.list(pnl, clicked, data, pt);
                            }
                            else {
                                lusidity.info.red('Sorry, we could not find anything.');
                                lusidity.info.show(5);
                            }
                        };
                        var f = function () {
                        };
                        $.htmlEngine.request('/query?start=0&limit=10000', s, f, query, 'post');
                    }
                    else{
                        methods.pnlLeft.list(pnl, clicked, pt.list, pt);
                    }
                },
                selected: function(node, data){
                    state.current.item = data;
                    if(state.current.node){
                        state.current.node.removeClass('selected');
                    }
                    node.addClass('selected');
                    state.current.node = node;
                    methods.html.formCancel();
                },
                make: function(pt){
                    var container = $(document.createElement('div'));
                    state.opts.pnlLeftNode.append(container);
                    var name =  pt.title.toLowerCase();

                    var options = !pt.hasMenu ? null : {
                        target: container,
                        buttons: [
                            {id: 'add-'+name, glyphicon: 'glyphicons glyphicons-file-plus', tn: 'add-' + name,
                                title: 'New '+pt.label, cls: 'green', css: {maxWidth: '40px', maxHeight: '34px', padding: '3px 4px'}},
                            {id: 'delete-'+name, glyphicon: 'glyphicons glyphicons-file-minus', tn: 'delete-'  + name,
                                title: 'Delete '+pt.label, cls: 'red', css: {maxWidth: '40px', maxHeight: '34px', padding: '3px 4px'}, disabled: true}
                        ]
                    };
                    var groupNode = methods.html.panel(container, pt.glyph, pt.title, null, false, null, options);
                    var nodeAdd,nodeDelete;
                    var clicked = function(node, item) {
                        var selected = state.opts.pnlLeftNode.find('.selected');
                        if (selected.length > 0) {
                            selected.removeClass('selected');
                        }
                        state.current.item = item;
                        state.current.node = node;
                        state.tbody = null;
                        state.current.pt = pt;
                        if (pt.hasMenu) {
                            var id = nodeDelete.attr('id');
                            $.each(state.current.deleteNodes, function () {
                                if ($(this).attr('id') === id) {
                                    $(this).removeAttr('disabled');
                                }
                                else {
                                    $(this).attr('disabled', 'disabled');
                                }
                            });
                        }
                        methods.pnlMiddle.content.init(pt);
                    };
                    methods.pnlLeft.process(groupNode, clicked, pt.queries.root(), pt);

                    nodeAdd = $('#add-'+name);
                    nodeDelete = $('#delete-'+name);
                    nodeDelete.attr('disabled', 'disabled');
                    state.current.addNodes.push(nodeAdd);
                    state.current.deleteNodes.push(nodeDelete);

                    container.on('menu-bar-delete-'+name, function(){
                        methods.pnlLeft.remove(state.current.item, pt);
                    });
                    container.on('menu-bar-add-'+name, function () {
                        $.each(state.current.deleteNodes, function(){
                            $(this).attr('disabled', 'disabled');
                        });
                        methods.pnlMiddle.form(state.formNode, pt, null, true);
                    });
                },
                list: function(container, clicked, data, pt){
                    data.results = $.jCommon.array.sort(data.results, [{property: 'title', asc: true}]);
                    methods.pnlLeft[pt.leftView](
                        container, clicked, data, pt, null
                    );
                },
                tree: function (container, clicked, data, pt, item) {
                    var treeView = $.htmlEngine.plugins.get('treeView');
                    if (treeView) {
                        container.on('treeNodeLeftClick', function (e) {
                            if(state.current.node){
                                state.current.node.unbind('size-changed');
                            }
                            var item = (e.item) ? e.item : e.node.data('item');
                            state.current.item = item;
                            state.current.node = e.node;
                            e.node.on('size-changed', function () {
                                var q = pt.queries.children(state.current.item);
                                container.treeView('updateSize', {node: state.current.node, query: q});
                            });
                            if(clicked && $.isFunction(clicked)){
                                clicked(e.node, item);
                            }
                            e.node.header.addClass('selected');
                        });
                        treeView(container, {
                            node: container,
                            parentNode: container.parent(),
                            schema: {
                                name: 'treeView',
                                plugin: {
                                    mapper: {
                                        id: state.KEY_ID,
                                        uri: state.KEY_ID,
                                        label: 'title'
                                    },
                                    get: {
                                        rootQuery: function (data) {
                                            return pt.queries.root();
                                        },
                                        childQuery: function (data) {
                                            return pt.queries.children(data);
                                        }
                                    },
                                    post: {
                                        url: function (target) {
                                            var id = target[state.KEY_ID];
                                            return (id ? id : target) + '/properties/electronic/base_infrastructure/infrastructures';
                                        },
                                        data: function (other) {
                                            var id = other[state.KEY_ID];
                                            var vertexType = other['vertexType'];
                                            return {
                                                edgeDirection: 'out',
                                                otherId: id ? id : other,
                                                vertexType: vertexType
                                            }
                                        }
                                    },
                                    limit: 1000,
                                    rootSelectable: true,
                                    rootSelected: true,
                                    isDraggable: false,
                                    isDroppable: false,
                                    totals: true,
                                    tooltip: 'title'
                                }
                            }
                        });
                    }
                },
                table: function (container, clicked, data, pt) {
                    $.each(data.results, function () {
                        var item = this;
                        var node = $(document.createElement('div')).addClass('result-group-item selectable')
                            .attr('data-name', item.name).css({cursor: 'pointer'});
                        var strong = $(document.createElement('strong')).html(item.title);
                        var title = $(document.createElement('div')).css({marginLeft: '10px'}).append(strong);
                        node.append(title);
                        if (item[state.KEY_DESC]) {
                            try {
                                var d = item[state.KEY_DESC].results[0].value;
                                var desc = $(document.createElement('div')).html(d).css({
                                    marginLeft: '15px',
                                    marginRight: '15px'
                                });
                                node.append(desc);
                            } catch (e) {
                            }
                        }
                        container.append(node);
                        node.on('click', function () {
                            state.current.item = item;
                            container.children().removeClass('selected');
                            node.addClass('selected');
                            if (clicked && $.isFunction(clicked)) {
                                clicked(node, item);
                            }
                            node.addClass('selected');
                        });

                        node.on('size-changed', function () {
                            sizeChanged();
                        });

                        function sizeChanged() {
                            try {
                                var s = function (data) {
                                    var hits = (data && data.hits) ? data.hits : 0;
                                    title.children().remove();
                                    strong = $(document.createElement('strong')).html(item.title);
                                    var badge = $(document.createElement('span')).addClass('badge blue').html(hits)
                                        .css({marginLeft: '5px'});
                                    title.append(strong).append(badge);
                                };
                                var q = pt.queries.children(item);
                                $.htmlEngine.request('/query?start=0&limit=10000', s, s, q, 'post');
                            }
                            catch (e){}
                        }
                        sizeChanged();
                    });
                }
            },
            pnlMiddle: {
                init: function(){
                    var h = state.opts.pnlMiddleNode.innerHeight();
                    state.formNode = $(document.createElement('div')).css({minHeight: h+'px'});
                    state.formNode.hide();
                    state.opts.pnlMiddleNode.append(state.formNode);
                    state.contentNode = $(document.createElement('div')).css({minHeight: h+'px'});
                    state.opts.pnlMiddleNode.append(state.contentNode);
                    methods.droppable(state.contentNode);
                },
                content: {
                    init: function(pt){
                        state.formNode.hide().children().remove();
                        state.contentNode.show().children().remove();
                        methods.pnlMiddle.content.make(pt);
                    },
                    make: function(pt){
                        var container = $(document.createElement('div'));
                        state.contentNode.append(container);
                        var options = {
                            target: container,
                            buttons: [
                                {id: 'add-user', glyphicon: 'glyphicons glyphicons-user-add', tn: 'add-user',
                                    title: 'Add User', cls: 'green', css: {maxWidth: '40px', maxHeight: '34px', padding: '3px 4px'}}
                            ]
                        };
                        state.contentPanel = methods.html.panel(container, pt.glyph,
                            pt.title + ': ' + state.current.item.title, null, false, null, options);

                        var addNode = $('#add-user');
                        container.on('menu-bar-add-user', function(){
                            if(!state.pageModal('exists')) {
                                state.pageModal();
                            }
                            state.pageModal('show', {
                                glyph: 'glyphicons-info-sign',
                                header: 'Add User',
                                body: function(body){
                                    var node = $(document.createElement('div'))
                                        .html('Use the search box in the Discovery tab on the right to find users groups.' +
                                            ' Then drag a discovery and drop into the middle panel to add.');
                                    body.append(node);

                                },
                                footer: null,
                                hasClose: true
                            });
                        });

                        var s = function(data){
                            if(data ){
                                methods.html.table(state.contentPanel, data);
                            }
                            else{
                                lusidity.info.yellow('No items found.');
                                lusidity.info.show(5);
                            }
                        };
                        var f = function(){
                            lusidity.info.red('Sorry, something went wrong.');
                            lusidity.info.show(5);
                        };
                        var q = pt.queries.children(state.current.item);
                        $.htmlEngine.request('/query?start=0&limit=10000', s, f, q, 'post');
                    }
                },
                logs: {
                    init: function () {
                        state.formNode.hide().children().remove();
                        state.contentNode.show().children().remove();
                        var container = dCrt('div').addClass('user-logs').css({overflow: 'hidden'});
                        state.contentNode.append(container);
                        var opts  = {
                            filter: true,
                            view: 'details',
                            data: state.current.item,
                            realTime: true,
                            body:{
                                attr: {'data-oh': 10},
                                cls: 'full-max'
                            },
                            paging:true,
                            getUrl: function (data, start, limit) {
                                return '/user/activity/logs?start=' + start + '&limit=' + limit;
                            },
                            lists:{
                                groups:[
                                    {label: 'Operation Type', property: 'operationType'},
                                    {label: 'User', property: 'title'}
                                ],
                                filters:[
                                    {label: 'User', property: 'title'}
                                ]
                            },
                            actions: [],
                            details: {
                                mapping: [
                                    {header: {title: '#'}, property: '#', type: 'integer'},
                                    {header: {title: 'Operation'}, property: 'operationType', type: 'string'},
                                    {header: {title: 'When'}, property: 'createdWhen', type: 'datetime'},
                                    {header: { title: 'User'}, property: 'title', type: 'string' },
                                    {header: { title: 'Server'}, property: 'server', type: 'string' },
                                    {header: { title: 'Success'}, property: 'success', type: 'boolean' },
                                    {header: { title: 'Comment'}, property: 'comment', type: 'string', callback: function (td, item, value) {
                                        if(value) {
                                            var v = $.jCommon.string.replaceAll(value, '\r\n', '<br\>');
                                            td.append(dCrt('p').append(v));
                                        }
                                    } }
                                ]
                            }
                        };
                        container.jFilterBar(opts);
                    }
                },
                form: function (container, pt, data, show) {
                    state.contentNode.hide();
                    state.formNode.show().children().remove();
                    var mode = !data ? 'add' : 'edit';
                    var defaultData = {
                        vertexType: pt.type,
                        edgeType: state.KEY_SGE,
                        edgeKey: state.KEY_SGS,
                        edgeDirection: 'out',
                        fromId: state.current.item[state.KEY_ID]
                    };
                    container.formBuilder({
                        title: pt.formTitle,
                        borders: false,
                        css: {'margin-right': '0'},
                        glyph: 'glyphicons glyphicons-file',
                        url: null,
                        actions: [],
                        show: false,
                        data: data,
                        defaultData: defaultData,
                        mode: mode,
                        before: function () {
                        },
                        isDeletable: function () {
                            return (null!==data);
                        },
                        deleteMessage: function (body, data) {
                            var title = methods.getTitle(data);
                            var msg = $(document.createElement('div')).css({verticalAlign: 'middle', height: '32px'});
                            var question = $(document.createElement('div')).html('Click Delete to delete "<strong>' + title + '</strong>".');
                            msg.append(question);
                            var statement = $(document.createElement('p')).html(
                                'Once Deleted, there is no way to recover "<strong>' + title + '</strong>".'
                            );
                            body.append(statement).append(msg);
                        },
                        onDelete: function (item) {
                            methods.pnlLeft.init();
                        },
                        close: function (node) {
                            state.formNode.hide();
                            if(undefined!== state.opts.previousVisible && null!==state.opts.previousVisible){
                                $.each(state.opts.previousVisible, function(){
                                    $(this).show();
                                });
                            }
                            state.opts.previousVisible = null;
                            state.contentNode.show();
                        },
                        display: function (node) {
                        },
                        formError: function (msg) {
                            lusidity.info.red(msg);
                            lusidity.info.show(5);
                        },
                        onSuccess: function (data) {
                            if(data.item && data.item.result) {
                                state.current.node.trigger('tree-node-refresh', {node: state.current.node});
                            }
                            state.contentNode.show();
                        },
                        onFailed: function () {
                            lusidity.info.red('Sorry, something went wrong.');
                            lusidity.info.show(5);
                        },
                        nodes: [
                            {
                                node: 'input',
                                type: 'text',
                                required: true,
                                id: 'title',
                                label: 'Title',
                                placeholder: 'Enter a friendly name.',
                                onChanged: function(node){
                                    var text = node.val();
                                }
                            },
                            {
                                node: 'textarea',
                                required: false,
                                id: '/system/primitives/raw_string/descriptions',
                                map: {
                                    direction: 'out',
                                    key: 'value',
                                    vertexType: '/system/primitives/raw_string'
                                },
                                label: 'Description',
                                css: {width: '100%', height: '100px'}
                            }
                        ],
                        getUrl: function () {
                            return (null===data) ? '/domains/domain/new' : data[state.KEY_ID];
                        }
                    });
                }
            },
            pnlRight:{
                init: function () {
                    var container = $(document.createElement('div')).css({padding: '10px'});
                    state.opts.discoverNode.append(container);
                    var input = $(document.createElement('input'))
                        .attr('type', 'text')
                        .attr('placeholder', 'Who are you looking for?')
                        .css({width: '100%', padding: '2px'});
                    container.append(input);
                    var resultsNode = $(document.createElement('div'));
                    container.append(resultsNode);

                    var last = '';
                    input.on('keyup', function (e) {
                        var text = input.val();
                        resultsNode.children().remove();
                        if (!$.jCommon.string.equals(text, last)) {
                            var url = '/discover/suggest/principal?limit=100&phrase="' + encodeURI(text) + '"';
                            var success = function (data) {
                                methods.pnlRight.create(data, resultsNode);
                            };
                            $.htmlEngine.request(url, success, null, null, 'get');
                            last = text;
                        }
                    });
                },
                create: function (data, container) {
                    if (data && data.results) {
                        state.adding = true;
                        state.done = false;
                        container.children().remove();
                        $.each(data.results, function () {
                            var item = this;
                            var process = true;
                            if (state.tileNode) {
                                var node = state.tileNode.find('div[data-id="' + item.uri + '"]');
                                process = (node.length <= 0);
                            }
                            if (process) {
                                var row = $(document.createElement('div')).addClass('result').attr('data-name', item.name).css({cursor: 'move'});
                                var header = $(document.createElement('div')).addClass('panel-heading-title')
                                    .css({position: 'relative', marginBottom: '2px'});
                                row.append(header);
                                var link = dLink(item.title, item.uri).css({marginLeft: '30px'});
                                header.append(link);

                                var iconParent = $(document.createElement('div')).addClass('icon-parent').css({top: '0'});
                                var domainType = $.jCommon.string.getLast(item.vertexType, '/');
                                var url = 'url("/assets/img/types/' + domainType + '.png")';
                                var icon = $(document.createElement('div')).css({backgroundImage: url});
                                iconParent.append(icon);
                                header.append(iconParent);

                                if (item.externalUri) {
                                    var external = $(document.createElement('div')).addClass('external-link');
                                    var externalLink = $(document.createElement('a')).attr('href', item.externalUri)
                                        .attr('target', '_blank').html(item.externalUri);
                                    external.append(externalLink);
                                    row.append(externalLink)
                                }
                                container.append(row);
                                methods.draggable(row, item);
                            }
                            return state.adding;
                        });
                        state.done = true;
                    }
                },
                get: function (text) {
                    var success = function (data) {
                        state.adding = false;
                        function  check() {
                            if(state.done){
                                methods.pnlRight.create(data);
                            }
                            else{
                                window.set(check, 100);
                            }
                        }
                        check();
                    };
                    var failed = function () {
                        lusidity.info.red('Sorry, that shouldn\'t have happened.');
                        lusidity.info.show(5);
                    };
                    var url = '/discover/device?phrase=' + encodeURI(text);
                    $.htmlEngine.request(url, success, failed, null, 'get');
                }
            }
        };
        //public methods

        //Initialize
        methods.init();
    };

    //Default Settings
    $.acl.defaults = {
        get: {
            rootUrl: function(){
                return '/acs/security/principals';
            }
        }
    };

    //Plugin Function
    $.fn.acl = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === 'object') {
            return this.each(function() {
                new $.acl($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $acl = $(this).data('acl');
            switch (method) {
                case 'exists': return (null!==$acl && undefined!==$acl && $acl.length>0);break;
                case 'state':
                default: return $acl;
            }
        }
    };
})(jQuery);
