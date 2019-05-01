
;(function ($) {

    //Object Instance
    $.organization = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.organization.defaults, options);
        state.current = {
            items: [],
            content: {}
        };
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.KEY_ORG = '/organization/organization';
        state.KEY_ORG_E = '/object/edge/organization_edge';
        state.KEY_ORG_P = '/organization/organization/organizations';

        state.KEY_POS = '/organization/personnel_position';
        state.KEY_POS_KEY = 'organization_personnel_position';
        state.KEY_POS_E = '/object/edge';
        state.KEY_POS_TE = '/object/edge/term_edge';
        state.KEY_POS_P = '/organization/personnel_position/positions';
        state.KEY_POS_SE = '/object/edge/scoped_edge';
        state.KEY_POST_SP = '/organization/personnel_position/scopedPositions';
        state.KEY_PP = '/people/person';

        state.KEY_SGE = '/object/edge/principal_edge';
        state.KEY_SGS = '/acs/security/base_principal/principals';

        state.KEY_DESC = '/system/primitives/raw_string/descriptions';
        state.KEY_IDENTITY = '/acs/security/identity';
        state.adding = true;
        state.done = true;
        state.tabLast = 0;
        lusidity.environment('priviledged');

        state.pts = {
            org1: {
                title: 'Organizations',
                formTitle: 'Create Group',
                type: state.KEY_ORG,
                key: state.KEY_ORG_P,
                label: 'Position',
                desc: "An organized body of people with a particular purpose, especially a business, society, association, etc.",
                glyph: 'glyphicons glyphicons-bank',
                leftView: 'tree',
                hasMenu: true,
                limit: 1000,
                table: {
                    heads: ['Organization'],
                    properties: ['title'],
                    cls: 'table-hover'
                },
                qs: {
                    url: '/query/org',
                    root: function () {
                        return {
                            asFilterable: true,
                            domain: state.KEY_ORG,
                            "native": {query: {filtered: {filter: {bool: {should: [{term: {"title.folded": "root organizations"}},{term: {"title.folded": "defense information systems agency"}}]}}}}}
                        };
                    },
                    children: function(item){
                        return {
                            asFilterable: true,
                            domain: state.KEY_ORG_E,
                            type: state.KEY_ORG,
                            lid: item.lid,
                            "native": {
                                query: {
                                    filtered: {
                                        filter: {
                                            bool: {
                                                must: [
                                                    {term: {'/object/endpoint/endpointFrom.relatedId.raw': item.lid}},
                                                    {term: {'label.raw': state.KEY_ORG_P}}
                                                ]
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };

        state.pts.org2 = $.extend({}, state.pts.org1, {
            title: 'Branches',
            label: 'Branches',
            glyph: 'glyphicons glyphicons-tree-structure'
        });

        state.pts.pos = {
            title: 'Positions',
            formTitle: 'Create a Position',
            type: state.KEY_POS,
            edgeType: state.KEY_POS_TE,
            edgeKey: state.KEY_POS_P,
            key: state.KEY_POS_P,
            desc: "A Position is unique to an employee, and is used for budgeting and posting purposes. It is a more detailed summary of the duties specific to the individual employee.",
            label: 'Positions',
            glyph: 'glyphicons glyphicons-user-structure',
            init: function(node){
                node.on('menu-bar-add-position', function () {
                    methods.pnlMiddle.form(state.formNode, state.pts.pos, null, true);
                });
            },
            table:{
                cls: 'no-border',
                heads: [],
                properties: ['content-table'],
                getContent: function (item) {
                    var result = dCrt('div').css({width: '100%'});
                    var users = $.extend({}, state.pts.users);
                    users.title = item.title;
                    users.label = item.title;
                    methods.pnlMiddle.content.make(result, users, item);
                    return result;
                },
                onTable: function (node) {
                    if(state.adding){
                        var accept = ".position";
                        methods.droppable(state.current.item, state.pts.pos, node, state.KEY_POS_P, 'vertexType', state.KEY_POS, accept,true);
                    }
                },
                onRow: function (row) {
                    var item = row.data('item');
                    var accept = ".personnel-position";
                    methods.droppable(item, state.pts.pos, row, state.KEY_SGS, 'vertexType', state.KEY_PP, accept);
                    row.find('td').addClass('no-border');
                },
                onDrop: function () {
                    methods.pnlMiddle.content.init();
                }
            },
            menu:{
                opts: {
                    buttons: [
                        {
                            id: 'add-position',
                            glyphicon: 'glyphicons glyphicons-file-plus',
                            tn: 'add-position',
                            title: 'New Position',
                            cls: "default",
                            css: {maxWidth: '32px', maxHeight: '28px', padding: '2px 2px 2px 2px'}
                        }
                    ]
                }
            },
            leftView: 'tree',
            hasMenu: true,
            limit: 1000,
            qs: {
                children: function(item){
                    return {
                        domain: state.KEY_POS_TE,
                        type: state.KEY_ORG,
                        lid: item.lid,
                        sort: {on: 'title', direction: 'asc'},
                        "native": {
                            query: {
                                filtered: {
                                    filter: {
                                        bool: {
                                            must: [
                                                {term: {'/object/endpoint/endpointFrom.relatedId.raw': item.lid}},
                                                {term: {'label.raw': state.KEY_POS_P}}
                                            ]
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };

        state.pts.sc_pos = {
            title: 'Scoped Positions',
            formTitle: 'Create Group',
            type: state.KEY_POS,
            key: state.KEY_POST_SP,
            desc: "Scoped Position defines areas of responsibility.",
            label: 'Scoped Positions',
            glyph: 'glyphicon-screenshot',
            leftView: 'tree',
            limit: 1000,
            table:{
                cls: 'no-border',
                heads: [],
                properties: ['content-table'],
                getContent: function (item) {
                    var result = dCrt('div').css({width: '100%'});
                    var users = $.extend({}, state.pts.users);
                    users.title = item.title;
                    users.label = item.title;
                    methods.pnlMiddle.content.make(result, users, item);
                    function c() {
                        window.setTimeout(function () {
                            var b = result.find('.css_del');
                            var t = result.find('th').first();
                            if (b.length > 0 && t.length > 0) {
                                b.empty();
                                b.remove();
                                t.empty();
                                t.remove();
                            }
                            else {
                                c();
                            }
                        }, 100);
                    }

                    c();
                    return result;
                },
                onTable: function (node) {
                    var accept = ".scoped-position";
                    methods.droppable(state.current.item, state.pts.sc_pos, node, state.KEY_POST_SP, 'vertexType', state.KEY_POS, accept);
                },
                onRow: function (row) {
                    row.find('td').addClass('no-border');
                },
                onDrop: function () {
                    methods.pnlMiddle.content.init();
                }
            },
            qs: {
                children: function(item){
                    return {
                        domain: state.KEY_POS_SE,
                        type: state.KEY_ORG,
                        lid: item.lid,
                        sort: {on: 'title', direction: 'asc'},
                        "native": {
                            query: {
                                filtered: {
                                    filter: {
                                        bool: {
                                            must: [
                                                {term: {'/object/endpoint/endpointFrom.relatedId.raw': item.lid}},
                                                {term: {'label.raw': state.KEY_POST_SP}}
                                            ]
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
        state.pts.users = {
            title: 'Assigned',
            type: state.KEY_PP,
            key: state.KEY_SGS,
            desc: "",
            label: 'Assigned',
            glyph: 'glyphicons glyphicons-group',
            leftView: 'tree',
            hasMenu: true,
            limit: 1000,
            table:{
                heads: ['Remove', 'Title'],
                properties: ['del', 'title'],
                cls: 'table-hover',
                onDel: function (row, item,tbody) {
                    var s = function () {
                        row.remove();
                        var elmNode = tbody.children().get();
                        if(elmNode.length < 1){
                            var content = dCrt('div').html('No items found.').css({marginBottom: "10px"});
                            tbody.append(content);
                            var tHeadNode = tbody.prev();
                            if(tHeadNode){
                                tHeadNode.remove();
                            }
                        }
                        methods.pnlRight.positions();
                    };
                    var f = function () {
                        lusidity.info.red(msg);
                        lusidity.info.show(5);
                    };
                    var p = $.jCommon.element.getParent(row, ['table', 'tr']);
                    var pd = p.data('item');
                    var url = methods.link.url(state.KEY_SGS, item);
                    var d = methods.link.data(pd, item);
                    d['delete'] = true;
                    $.htmlEngine.request(url, s, f, d, 'post');
                }
            },
            qs: {
                children: function(item){
                    return {
                        domain: state.KEY_SGE,
                        type: state.KEY_SG,
                        lid: item.lid,
                        "native": {query: {filtered: {filter: {bool: {must: [
                            {term: {'/object/endpoint/endpointFrom.relatedId.raw': item.lid}},
                            {term: {'label.raw': state.KEY_SGS}}
                        ]}}}}}
                    }
                }
            }
        };
        state.confirm = dCrt('div').css({height: '0', width: '0'});
        // Store a reference to the environment object
        el.data('organization', state);

        // Private environment methods
        methods = {
            init: function() {
                methods.pnlMiddle.init();
                methods.pnlLeft.init();
                methods.pnlRight.init();
                methods.api.init();
                lusidity.environment('onResize', function () {
                    methods.pnlMiddle.resize();
                    methods.pnlRight.resize();
                });
                methods.pnlMiddle.resize();
                methods.pnlRight.resize();
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
            draggable: function (node, data) {
                if (state.contentNode.is(':visible')) {
                    node.data('item', data);
                    node.draggable({
                        cursor: "move",
                        revert: true,
                        scroll: false,
                        start: function () {
                            $(this).css({zIndex: 99999999});
                            var item = $(this).data('item');
                            if($.jCommon.string.startsWith(item.vertexType, state.KEY_PP)){
                                $(this).addClass('personnel-position');
                            }
                            else if($.jCommon.string.startsWith(item.vertexType, state.KEY_POS)){
                                $(this).addClass('scoped-position');
                                $(this).addClass('position');
                            }
                        },
                        stop: function () {
                            $(this).css({zIndex: 999999});
                        },
                        helper: function () {
                            return $(this).clone().appendTo('body').show();
                        }
                    });
                }
            },
            droppable: function (item, pt, dropArea, propertyKey, key, value, accept,hasRemovePriorItems) {
                if (dropArea) {
                    dropArea.droppable({
                        accept: accept,
                        hoverClass: 'drop-area-hover',
                        activeClass: 'drop-area-active-b',
                        drop: function (event, ui) {
                            event.stopPropagation();
                            window.event.cancelBubble = true;
                            var dragItem = ui.draggable.data('item');
                            if (ui.draggable.hasClass('result')
                                && ($.jCommon.string.empty(key) ||
                                    ($.jCommon.string.startsWith(dragItem[key], value, true) ||
                                        $.jCommon.string.equals(dragItem[key], value, true)))) {
                                dropArea.loaders('show');
                                var success = function (data) {
                                    if(hasRemovePriorItems){
                                        methods.clearPositions(item,propertyKey,dragItem);
                                    }
                                    methods.pnlRight.positions();
                                    dropArea.loaders('hide');
                                    if (!data.error) {
                                        ui.draggable.remove();
                                        if(pt.table && $.isFunction(pt.table.onDrop)){
                                            pt.table.onDrop(dropArea, data.from, data.to);
                                        }
                                    }
                                    else {
                                        lusidity.info.red(data.error);
                                        lusidity.info.show(5);
                                    }
                                };
                                var failed = function () {
                                    dropArea.loaders('hide');
                                };
                                ui.draggable.siblings().removeClass("active");
                                ui.draggable.addClass('active');
                                window.setTimeout(function () {
                                    var url = methods.link.url(propertyKey, dragItem);
                                    var data = methods.link.data(item, dragItem);
                                    $.htmlEngine.request(url, success, failed, data, 'post');
                                }, 100);
                            }
                        }
                    });
                }
            },
            clearPositions:function (item,propertyKey,dragItem) {
                var q = {
                    domain: '/object/edge/term_edge',
                    type: 'organization_organization',
                    "native": {
                        query: {
                            match_all: []
                        }
                    }
                };
                var success = function (data) {
                    if(data && data.results){
                        $.each(data.results, function () {
                            var linkOrganization = this;
                            if(linkOrganization.lid !== item.lid){
                                var orgPost = {
                                    domain: state.KEY_POS_TE,
                                    type: state.KEY_ORG,
                                    lid: linkOrganization.lid,
                                    "native": {
                                        query: {
                                            filtered: {
                                                filter: {
                                                    bool: {
                                                        must: [
                                                            {term: {'/object/endpoint/endpointFrom.relatedId.raw': linkOrganization.lid}},
                                                            {term: {'label.raw': state.KEY_POS_P}}
                                                        ]
                                                    }
                                                }
                                            }
                                        }
                                    }
                                };
                                var s = function (data) {
                                    if(data && data.results){
                                        $.each(data.results, function () {
                                            var linkPosition = this;
                                            var selectedObjectUri = dragItem.uri ? dragItem.uri : dragItem['/vertex/uri'];
                                            if(selectedObjectUri === linkPosition['/vertex/uri']){
                                                var url = methods.link.url(propertyKey, dragItem);
                                                var data = methods.link.data(linkOrganization, dragItem);
                                                data['delete'] = true;
                                                $.htmlEngine.request(url,null,null, data, 'post');
                                            }
                                        });
                                    }

                                };
                                $.htmlEngine.request('/query?limit=10000000', s, null, orgPost, 'post');
                            }
                        });
                    }
                };
                $.htmlEngine.request('/query?limit=10000000', success, null, q, 'post');
            },
            html: {
                formCancel: function () {
                    function cancel(node) {
                        if (node && node.formBuilder('exists')) {
                            node.formBuilder('cancel');
                        }
                    }
                    methods.html.tiles.setTileHeader();
                },
                table: function(container, pt, data,parentItems){
                    container.children().remove();
                    if(data && data.results && data.results.length>0) {
                        var tbl = dCrt('table').addClass('table ' + (pt.table.cls ? pt.table.cls : '')).css({marginBottom: "10px", width: '100%'});
                        if(!pt.table || pt.table.stylize){
                            tbl.addClass('table table-hover');
                        }
                        container.append(tbl);
                        methods.html.getTableHead(pt.table.heads , tbl);
                        if(data.results[0][state.opts.labelKey]) {
                            data.results = $.jCommon.array.sort(data.results, [{property: state.opts.labelKey, asc: true}]);
                        }
                        methods.html.getTableBody(pt, pt.table.properties, data.results, tbl, 0, parentItems);
                    }
                    else{
                        var content = dCrt('div').html('No items found.').css({marginBottom: "10px"});
                        if(pt.label==="Positions"){
                            content.css({marginLeft: '10px'});
                        }
                        container.append(content);
                    }
                    if(pt.table && $.isFunction(pt.table.onTable)){
                        pt.table.onTable(container);
                    }
                },
                getTableHead: function(headers, container){
                    if(headers.length>0) {
                        var thead = $(document.createElement('thead'));
                        var row = $(document.createElement('tr'));
                        $.each(headers, function () {
                            row.append($(document.createElement('th')).html(this));
                        });
                        thead.append(row);
                        container.append(thead);
                    }
                },
                getTableBody: function(pt, properties, items, container, on, parentItems){
                    var tbody = $(document.createElement('tbody'));
                    var results = [];
                    function load(){
                        if(results.length>0) {
                            var items = $.jCommon.array.sort(results, true, [{property: 'title', asc: true}]);
                            $.each(items, function () {
                                var item = this;
                                if(!item.error) {
                                    methods.html.makeRow(pt, tbody, properties, item, parentItems)
                                }
                            });
                        }
                    }
                    $.each(items, function() {
                        var d = this;
                        if (!d['/object/endpoint/endpointFrom.relatedId']) {
                            results.push(d);
                        }
                        else {
                            var v = $.jCommon.string.contains(state.current.item[state.KEY_ID],d.endpointFrom)?
                                d.endpointTo:
                                d.endpointFrom;
                            v = JSON.parse(v);
                            var s = function (item) {
                                results.push(item ? item : {error: true});
                            };
                            var url = '/domains/' + v.classKey + "/" + v.id;
                            $.htmlEngine.request(url, s, s, null, 'get');
                        }
                    });

                    var max = 150;
                    var on = 0;
                    function check(){
                        on+=100;
                        if(results.length===items.length || on>=max){
                            load();
                        }
                        else{
                            window.setTimeout(check, 100);
                        }
                    }
                    container.append(tbody);
                    check();
                },
                makeRow: function(pt, tbody, properties, item,parentItem) {
                    var row = $(document.createElement('tr')).addClass('table-row');
                    row.data('item', item);
                    tbody.append(row);
                    if (!item[state.opts.labelKey]) {
                        item[state.opts.labelKey] = "No label";
                    }
                    var pItemTitle = parentItem ? parentItem.title : state.current.item[state.opts.labelKey];
                    $.each(properties, function () {
                        var key = this.toString();
                        var value;
                        var td = $(document.createElement('td')).addClass('css_' + key);
                        if ($.jCommon.string.equals(key, 'del')) {
                            td.css({width: '50px', maxWidth: '50px'});
                            value = $(document.createElement('span')).attr('title', 'Select to de-link from ' + pItemTitle).addClass('glyphicon glyphicon-remove').css({
                                fontSize: '16px',
                                cursor: 'pointer',
                                color: 'red'
                            });
                            value.on('click', function () {
                                var spin = $.htmlEngine.indicators({height: 16, width: 16, type: 'spinner'});
                                spin.insertBefore(value);
                                value.hide();

                                if(pt.table && $.isFunction(pt.table.onDel)){
                                    pt.table.onDel(row, item,tbody);

                                }
                                else {
                                    var s = function () {
                                        row.remove();
                                        state.current.node.trigger('size-changed');
                                    };
                                    var f = function () {
                                        spin.remove();
                                        value.show();
                                        lusidity.info.red(msg);
                                        lusidity.info.show(5);
                                    };
                                    var url = methods.link.url(pt.key, item);
                                    var d = methods.link.data(state.current.item, item);
                                    d['delete'] = true;
                                    $.htmlEngine.request(url, s, f, d, 'post');

                                }
                            });
                            if (item.status && item.status === "processing") {
                                value.hide();
                            }
                        }
                        else if ($.jCommon.string.equals(key, 'tableLineNumber')) {
                            on++;
                            td.css({width: '50px', maxWidth: '50px'});
                            value = $(document.createElement('span')).html(on);
                        }
                        else if (key === "title") {
                            value = $(document.createElement('a')).html(item[key])
                                .attr('href', item[state.KEY_ID]).attr('target', '_blank');
                        }
                        else if (key === 'details') {
                            var details = item['/common/base_contact_detail/contactDetails'];
                            if (details) {
                                value = $(document.createElement('ul')).addClass('list-group');
                                $.each(details, function () {
                                    if (!$.jCommon.string.empty(this.value)) {
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
                        else if(key === 'content-table'){
                            if($.isFunction(pt.table.getContent)){
                                value = pt.table.getContent(item);
                                var h = value.find('.panel-heading');
                                if (h.length > 0) {
                                    h.parent().removeClass('panel-default');
                                    h.removeClass('panel-default').addClass('blue').css({padding: '0px'});
                                    var span = $(document.createElement('i'))
                                        .attr('title', 'Select to remove from ' + state.current.item[state.opts.labelKey])
                                        .addClass('glyphicon glyphicon-remove').css({
                                            cursor: 'pointer',
                                            color: 'red',
                                            display: 'inline-block',
                                            padding: '6px 12px'
                                        }).addClass('pull-right');
                                    span.on('click', function () {
                                        var spin = $.htmlEngine.indicators({height: 16, width: 16, type: 'spinner', css: {top: '3px', marginRight: '5px', "float": 'right'}});
                                        spin.insertBefore(span);
                                        span.hide();

                                        if(pt.table && $.isFunction(pt.table.onDel)){
                                            pt.table.onDel(row, item);
                                        }
                                        else {
                                            var s = function () {
                                                row.remove();
                                                var elmNode = tbody.children().get();
                                                if(elmNode.length < 1){
                                                    var content = dCrt('div').html('No items found.').css({marginBottom: "10px", marginLeft:"10px"});
                                                    tbody.append(content);
                                                }
                                                state.current.node.trigger('size-changed');
                                                methods.pnlRight.positions();
                                            };
                                            var f = function () {
                                                spin.remove();
                                                span.show();
                                                lusidity.info.red(msg);
                                                lusidity.info.show(5);
                                            };
                                            var url = methods.link.url(pt.key, item);
                                            var d = methods.link.data(state.current.item, item);
                                            d['delete'] = true;
                                            $.htmlEngine.request(url, s, f, d, 'post');
                                        }
                                    });
                                    h.append(span);
                                }
                            }
                        }
                        else {
                            value = $(document.createElement('span')).html(item[key]);
                        }
                        td.css({marginRight: '10px'}).append(value);
                        row.append(td);
                        if(pt.table && $.isFunction(pt.table.onRow)){
                            pt.table.onRow(row);
                        }
                    });
                }
            },
            link: {
                url: function (key, item) {
                    var id = item.uri ? item.uri : item[state.KEY_ID];
                    return (id + '/properties' + key);
                },
                deleteUrl: function (item) {
                    return item.uri ? item.uri : item[state.KEY_ID];
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
                    state.opts.pnlLeftNode.children().remove();
                    methods.pnlLeft.make(state.pts.org1, state.current.item);
                },
                'delete': function(item){
                    var btnBar = dCrt('div').addClass('btn-bar');
                    var delButton = $(document.createElement('button')).attr('type', 'button')
                        .addClass('btn btn-danger').html('Delete');
                    btnBar.append(delButton);
                    delButton.on('click', function () {
                        state.formNode.hide().children().remove();
                        state.contentNode.hide().children().remove();
                        var s = function () {
                            bLdr.loaders('hide');
                            methods.pnlLeft.init();
                        };
                        var f = function () {
                            bLdr.loaders('hide');
                            state.pageModal('hide');
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
                            var title = item[state.opts.labelKey];
                            var msg = dCrt('div').css({verticalAlign: 'middle', height: '32px'});
                            var question = dCrt('div').html('Click Delete to delete "<strong>' + title + '</strong>".');
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
                        $.htmlEngine.request('/query', s, f, query, 'post');
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
                    var container = dCrt('div');
                    state.opts.pnlLeftNode.append(container);
                    var pnlBody =$.htmlEngine.panel(container, pt.glyph, pt.title, null, false, null, null);
                    var clicked = function(node, item) {
                        state.current.pt = pt;
                        methods.pnlMiddle.content.compareRootOrganization(item);
                    };
                    methods.pnlLeft.process(pnlBody, clicked, pt.qs.root(), pt);
                },
                list: function(container, clicked, data, pt){
                    data.results = $.jCommon.array.sort(data.results, true, [{property: 'title', asc: true}]);
                    methods.pnlLeft[pt.leftView](
                        container, clicked, data, pt, null
                    );
                },
                scan: function (paths) {
                    state.opts.treeNode.treeView("expandTo", {paths: paths, key: 'title', select: true});
                },
                tree: function (container, clicked, data, pt) {
                    state.opts.treeNode = container;
                    state.opts.treeNode.treeView({
                        parentNode: container.parent(),
                        name: 'treeView',
                        mapper: {
                            id: state.KEY_ID,
                            uri: state.KEY_ID,
                            label: 'title'
                        },
                        onBefore: function (data) {
                            if(data && data.results){
                                $.each(data.results, function () {
                                    if(this._counted){
                                        delete this._counted;
                                    }
                                });
                            }
                            return data;
                        },
                        get: {
                            url: pt.qs.url,
                            rootQuery: function () {
                                return pt.qs.root();
                            },
                            childQuery: function (data) {
                                return pt.qs.children(data);
                            },
                            countQuery: function (data) {
                                return pt.qs.children(data);
                            }
                        },
                        sort: function (data) {
                            var r = data;
                            if(r && r.results){
                                r.results = $.jCommon.array.sort(r.results, [{property: 'title', asc: true}]);
                            }
                            return r;
                        },
                        post: {
                            url: function (target) {
                                var id = target[state.KEY_ID];
                                return (id ? id : target) + '/properties/organization/organization/organizations';
                            },
                            data: function (other) {
                                var id = other[state.KEY_ID];
                                var vertexType = other['vertexType'];
                                return {
                                    edgeDirection: "out",
                                    otherId: id ? id : other,
                                    vertexType: vertexType
                                }
                            }
                        },
                        limit: 1000,
                        rootSelectable: true,
                        rootSelected: false,
                        isDraggable: false,
                        isDroppable: false,
                        totals: true,
                        disableLoadingEffect:true,
                        expandable: true,
                        tooltip: 'title'
                    });
                    container.on('treeNodeCreated', function (e) {
                        if ($.jCommon.string.contains(e.item.title, "tag issues", true)) {
                            e.item.node.hide();
                            e.node.on("treeNodeAfterSort", function () {
                                e.item.node.parent().prepend(e.item.node);
                            });
                        }
                    });
                    container.on('treeNodeLeftClick', function (e) {
                        e.stopPropagation();
                        if(state.current.node){
                            state.current.node.unbind('size-changed');
                        }
                        var item = (e.item) ? e.item : e.node.data('item');
                        state.current.item = item;
                        state.current.node = e.node;
                        e.node.on('size-changed', function () {
                            if($.isFunction(pt.qs.getOrgs)){
                                var q = pt.qs.getOrgs(state.current.item);
                                container.treeView('updateSize', {node: state.current.node, query: q});
                            }
                        });
                        if(clicked && $.isFunction(clicked)){
                            clicked(e.node, item);
                        }
                    });
                },
                table: function (container, clicked, data, pt) {
                    $.each(data.results, function () {
                        var item = this;
                        var node = dCrt('div').addClass('result-group-item selectable')
                            .attr('data-name', item.name).css({cursor: 'pointer'});
                        var strong = $(document.createElement('strong')).html(item[state.opts.labelKey]);
                        var title = dCrt('div').css({marginLeft: '10px'}).append(strong);
                        node.append(title);
                        if (item[state.KEY_DESC]) {
                            try {
                                var d = item[state.KEY_DESC].results[0].value;
                                var desc = dCrt('div').html(d).css({
                                    marginLeft: '15px',
                                    marginRight: '15px'
                                });
                                node.append(desc);
                            } catch (e) {
                            }
                        }
                        container.append(node);

                        node.on('size-changed', function () {
                            sizeChanged();
                        });

                        function sizeChanged() {
                            var s = function (data) {
                                var hits = (data && data.hits) ? data.hits : 0;
                                title.children().remove();
                                strong = $(document.createElement('strong')).html(item[state.opts.labelKey]);
                                var badge = $(document.createElement('span')).addClass('badge blue').html(hits)
                                    .css({marginLeft: '5px'});
                                title.append(strong).append(badge);
                            };
                            var q = pt.qs.getOrgs(item);
                            $.htmlEngine.request('/query?limit=' + pt.limit, s, s, q, 'post');
                        }

                        sizeChanged();
                    });
                }
            },
            pnlMiddle: {
                init: function(){
                    state.formNode = dCrt('div');
                    state.formNode.hide();
                    state.opts.pnlMiddleNode.append(state.formNode);
                    state.contentNode = dCrt('div').css({height: 'inherit', overflow: 'hidden'});
                    state.opts.pnlMiddleNode.append(state.contentNode);
                },
                resize: function () {
                    var h = state.opts.pnlMiddleNode.availHeight(80);
                    state.formNode.css({minHeight: h+'px'});
                    if(state.opts.pNode){
                        var hh = (state.opts.hdrNode.height());
                        var pos = state.opts.pNode.find('.panel-sizable');
                        var spos = state.opts.scNode.find('.panel-sizable');
                        dMax(state.opts.pNode, h+(hh+5));
                        dMax(state.opts.scNode, h+hh+5);
                        dMax(pos, h-(hh-20));
                        dMax(spos, h-(hh-20));
                    }
                },
                form: function (container, pt, data) {
                    state.contentNode.hide();
                    state.formNode.show().children().remove();
                    var mode = !data ? 'add' : 'edit';
                    var defaultData = {
                        vertexType: pt.type,
                        edgeType: pt.edgeType,
                        edgeKey: pt.edgeKey,
                        edgeDirection: 'out',
                        fromId: state.current.item[state.KEY_ID]
                    };
                    container.formBuilder({
                        title: pt.formTitle,
                        borders: false,
                        css: {'margin-right': '0'},
                        panelCss: {margin: '10px'},
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
                            var msg = dCrt('div').css({verticalAlign: 'middle', height: '32px'});
                            var question = dCrt('div').html('Click Delete to delete "<strong>' + title + '</strong>".');
                            msg.append(question);
                            var statement = $(document.createElement('p')).html(
                                'Once Deleted, there is no way to recover "<strong>' + title + '</strong>".'
                            );
                            body.append(statement).append(msg);
                        },
                        onDelete: function () {
                            methods.pnlLeft.init();
                        },
                        close: function () {
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
                            container.loaders('hide');
                            if(data.item && data.item.result) {
                                methods.pnlMiddle.content.init();
                                methods.pnlRight.positions();
                                methods.pnlRight.resize();
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
                                    // todo: could change list title here.
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
                            return (null===data) ? '/domains/' + state.KEY_POS_KEY + '/new' : data[state.KEY_ID];
                        }
                    });
                },
                content: {
                    init: function(){
                        state.formNode.hide().children().remove();
                        state.contentNode.show().children().remove();

                        state.opts.hdrNode = dCrt('div');
                        var content = dCrt('div');
                        state.contentNode.append(state.opts.hdrNode).append(content);

                        methods.breadcrumb.init(state.current.item, state.opts.hdrNode);

                        state.opts.pNode = dCrt('div').addClass('col-md-6').css({overflow: 'hidden', padding: '0 0 0 0'});
                        state.opts.scNode = dCrt('div').addClass('col-md-6').css({overflow: 'hidden', padding: '0 0 0 0'});
                        content.append(state.opts.pNode).append(state.opts.scNode);

                        methods.pnlMiddle.content.make(state.opts.pNode, state.pts.pos, state.current.item, 'hdr-pos', true);
                        methods.pnlMiddle.content.make(state.opts.scNode, $.extend({}, state.pts.sc_pos), state.current.item, 'hdr-sc-pos', true);
                        methods.pnlMiddle.resize();
                    },
                    make: function(parentNode, pt, item, cls, isMain){
                        var container = dCrt('div');
                        parentNode.append(container);
                        if(pt.menu && pt.menu.opts){
                            pt.menu.opts.target = container;
                        }

                        var pnlBdy = $.htmlEngine.panel(container,pt.glyph,pt.title,null,false,null,(pt.menu ? pt.menu.opts : null));
                        if(isMain) {
                            pnlBdy.css({overflow: 'hidden auto'}).addClass('panel-sizable');
                        }
                        if(cls){
                            $(container.panel('getHeader')).addClass(cls);
                        }
                        if(pt.desc){
                            $(container.panel('getHeader')
                                .find('.pnl-title')).attr('title', pt.desc).addClass('header-text');

                        }
                        if(pt.init){
                            pt.init(container);
                        }
                        var s = function(data){
                            if(!data || !data.results){
                                data = {hits: 0, next: 0, results:[]};
                            }
                            methods.html.table(pnlBdy, pt, data, item);
                        };
                        $.htmlEngine.request('/query?limit=' + pt.limit, s, s, pt.qs.children(item), 'post');
                    },
                    rootOrganization:function () {
                        state.formNode.hide().children().remove();
                        state.contentNode.show().children().remove();
                        var div = dCrt('div');
                        var hd = dCrt('h4').html("Please select a sub-organization.").addClass('letterpress');
                        div.append(hd);
                        state.contentNode.append(div);
                        var d = $.jCommon.element.getDimensions(div);
                        div.css({
                            position: 'absolute',
                            top: '50%',
                            left: '50%',
                            marginTop: ((d.h/2)*-1)+'px',
                            marginLeft: ((d.w/2)*-1)+'px',
                            textAlign: 'center'});
                    },
                    compareRootOrganization:function (item) {
                        var toReturn;
                        var success = function (data) {
                            if(data && data.results){
                                $.each(data.results, function () {
                                    var org = this;
                                    if(org.lid === item.lid){
                                        methods.pnlMiddle.content.rootOrganization();
                                    }else{
                                        methods.pnlMiddle.content.init();
                                    }
                                });
                                return toReturn;
                            }};
                        var rootQuery = {
                            domain: state.KEY_ORG,
                            "native": {query: {filtered: {filter: {bool: {should: [
                                {term: {"title.folded": "root organizations"}}]
                            }}}}}
                        };
                        $.htmlEngine.request('/query?limit=10000000', success, success, rootQuery, 'post');
                    }
                }
            },
            pnlRight:{
                init: function () {
                    state.discoverContNode = dCrt('div').css({padding: '10px'});
                    state.opts.discoverNode.append(state.discoverContNode);
                    var input = $(document.createElement('input'))
                        .attr('type', 'text')
                        .attr('placeholder', 'What are you looking for?')
                        .attr('title', 'Organizations and people.')
                        .css({width: '100%', padding: '2px'});
                    state.discoverContNode.append(input);
                    state.discoverRsltNode = dCrt('div').css({overflowX: 'hidden', overflowY: 'auto'});
                    state.discoverContNode.append(state.discoverRsltNode);
                    var last = '';
                    input.on('keyup', function () {
                        var text = input.val();
                        state.discoverRsltNode.children().remove();
                        if (!$.jCommon.string.equals(text, last)) {
                            if(state.request){
                                state.request.abort();
                            }
                            var url = '/discover/suggest/principal?limit=100&phrase="' + encodeURI(text) + '"';
                            var s = function (data) {
                                methods.pnlRight.create(data, state.discoverRsltNode);
                            };
                            var f = function () {};
                            state.request = $.htmlEngine.request(url, s, f, null, 'get');
                            last = text;
                        }
                    });
                    methods.pnlRight.positions();
                    methods.pnlRight.resize();
                },
                nav: function (node, item) {
                    var ao = node.find('.org-assigned-positions');
                    var so = node.find('.org-scoped-positions');
                    function nav(c) {
                        c.css({position: 'relative'});
                        var nn = dCrt('span').css({color: '#337ab7', position: 'relative', top: '-4px', left: '-2px', padding: '0 0 0 0', height: '16px', cursor: 'pointer'})
                            .addClass('glyphicons glyphicons-circle-arrow-left').attr('title', 'Find this organization in the organization tree in the left panel.');
                        c.append(nn);
                        nn.on('click', function () {
                            var id = c.attr('data-organization-lid');
                            if(id){
                                id = '/domains/organization_organization/'+id;
                                var s = function (data) {
                                    if(data && data.results){
                                        var paths = [];
                                        $.each(data.results, function () {
                                            if(this.title === "Defense Information Systems Agency"){
                                                paths = [];
                                            }
                                            paths.push(this.title);
                                        });
                                        methods.pnlLeft.scan(paths);
                                    }
                                };
                                var f = function () {};
                                $.htmlEngine.request(id+'/breadcrumb',s,f,null);
                            }
                        });
                    }
                    $.each(ao, function () {
                        nav($(this));
                    });
                    $.each(so, function () {
                        nav($(this));
                    });
                },
                positions: function () {
                    state.opts.positionsNode.children().remove();
                    state.opts.positionsContNode  = dCrt('div').css({overflowX: 'hidden', overflowY: 'auto'});
                    state.opts.positionsNode.append(state.opts.positionsContNode);
                    var s = function (data) {
                        if(data && data.results){
                            var ps = $.jCommon.array.sort(data.results, [{property: 'title', asc: true}]);
                            $.each(ps, function () {
                                var item = this;
                                var p = dCrt('div').addClass('result data-list').attr('data-name', item.vertexLabel).css({cursor: 'move'});
                                var header = dCrt('div').addClass('panel-heading-title')
                                    .css({position: 'relative', marginBottom: '2px'});
                                p.append(header);
                                methods.pnlRight.createIcon(header,item);
                                var t = dLink(this.title, item[state.KEY_ID]);
                                t.css({fontWeight: 'bold'});
                                var link = $(document.createElement('a')).attr('href', item[state.KEY_ID])
                                    .attr('target', '_blank').html(item[state.opts.labelKey]).css({marginLeft: '30px',fontWeight: 'bold'});
                                header.append(link);

                                var success = function(cData){
                                    if(cData && cData.results){
                                        $.each(cData.results, function () {
                                            var infoItem = this;
                                            if(infoItem.description) {
                                                var r = dCrt('div').css({
                                                    margin: '0 0 5px 5px',
                                                    padding: '0'
                                                }).append(infoItem.description);
                                                methods.pnlRight.nav(r, item);
                                                methods.pnlRight.deleteScopeOrg(r);
                                                p.append(r);
                                            }
                                            else{
                                                var span = dCrt('span')
                                                    .attr('title', 'Select to delete from RMK')
                                                    .addClass('glyphicon glyphicon-remove pull-right').css({
                                                        fontSize: '16px',
                                                        cursor: 'pointer',
                                                        color: 'red',
                                                        position: 'relative',
                                                        padding: '4px',
                                                        top: '0'
                                                    });
                                                p.append(span);
                                                span.on('click', function () {
                                                    state.confirm.pageModal();
                                                    state.confirm.pageModal('show', {
                                                        glyph: 'glyphicon-warning-sign',
                                                        hasClose: true,
                                                        header: function () {
                                                            var header = dCrt('div');
                                                            var hContent = dCrt('h4').html("Delete Confirmation");
                                                            header.append(hContent);
                                                            return header;
                                                        },
                                                        body: function (body) {
                                                            body.children().remove();
                                                            var msg = dCrt('div').css({
                                                                verticalAlign: 'middle',
                                                                height: '32px'
                                                            });
                                                            var confirmation = dCrt('h5').html('Are you sure you want to delete <strong>' + item.title + '</strong>&nbsp;?');
                                                            msg.append(confirmation);
                                                            body.append(msg);
                                                        },
                                                        footer: function () {
                                                            var lFooter = dCrt('div');
                                                            var btnBar = dCrt('div').addClass('btn-bar');
                                                            var del = dCrt('button').attr('type', 'button')
                                                                .addClass('btn btn-danger').html('Delete');
                                                            btnBar.append(del);
                                                            del.on('click', function (e) {
                                                                e.preventDefault();
                                                                /*Run code to delete the position*/
                                                                var spin = $.htmlEngine.indicators({height: 16, width: 16, type: 'spinner'});
                                                                spin.insertBefore(span);
                                                                span.hide();
                                                                var s = function () {
                                                                    p.remove();
                                                                    if(state.current.item){
                                                                        if(state.contentNode){
                                                                            var contain = state.contentNode.find('span.pnl-title:contains('+ item.title +')');
                                                                            if(contain.length > 0){
                                                                                setTimeout(function() {
                                                                                    methods.pnlMiddle.content.init();
                                                                                }, 1000);
                                                                            }
                                                                        }
                                                                        state.confirm.pageModal('hide')
                                                                    }
                                                                };
                                                                var f = function () {
                                                                    spin.remove();
                                                                    value.show();
                                                                    lusidity.info.red(msg);
                                                                    lusidity.info.show(5);
                                                                };
                                                                var url = methods.link.deleteUrl(item);
                                                                $.htmlEngine.request(url, s, f, null, 'delete');
                                                            });
                                                            var cancel = dCrt('button').attr('type', 'button')
                                                                .addClass('btn btn-default btn-info').html('Cancel');
                                                            btnBar.append(cancel);
                                                            cancel.on('click', function () {
                                                                state.confirm.pageModal('hide');
                                                            });
                                                            lFooter.append(btnBar);
                                                            return lFooter;
                                                        }
                                                    });
                                                });
                                                if (item.status && item.status === "processing") {
                                                    span.hide();
                                                }
                                            }
                                        });
                                    }
                                };
                                state.opts.positionsContNode.append(p);
                                methods.draggable(p, item);
                                $.htmlEngine.request('/personnel/position?id='+ item.lid, success, success, null, 'get');
                            });
                        }
                    };
                    var q = {
                        domain: state.KEY_POS_KEY,
                        type: state.KEY_POS_KEY,
                        "native": {
                            query: {
                                match_all: []
                            }
                        }
                    };
                    $.htmlEngine.request('/query?limit=1000000', s, s, q, 'post');
                },
                resize: function () {
                    var tabSize = state.opts.tabContNode.availHeight(50);
                    if(state.opts.discoverNode){
                        dHeight(state.opts.discoverNode, tabSize,tabSize, tabSize);
                        if(state.discoverContNode){
                            dHeight(state.discoverContNode,tabSize,tabSize,tabSize);
                        }
                        if(state.discoverRsltNode){
                            dHeight(state.discoverRsltNode,tabSize,tabSize,tabSize);
                        }
                    }
                    tabSize = state.opts.tabContNode.availHeight(12);
                    if(state.opts.positionsNode){
                        dHeight(state.opts.positionsNode, tabSize, tabSize, tabSize);
                        dHeight(state.opts.positionsContNode,tabSize,tabSize,tabSize);
                    }
                },
                create: function (data, container) {
                    if (data && data.results) {
                        state.adding = true;
                        state.done = false;
                        container.children().remove();
                        $.each(data.results, function () {
                            var item = this;
                            if (!item[state.opts.labelKey]) {
                                item[state.opts.labelKey] = "No label";
                            }
                            if ($.jCommon.string.contains(item.vertexType, 'organization')
                                || $.jCommon.string.contains(item.vertexType, 'people')) {
                                var process = true;
                                if (state.tileNode) {
                                    var node = state.tileNode.find('div[data-id="' + item.uri + '"]');
                                    process = (node.length <= 0);
                                }
                                if (process) {
                                    var row = dCrt('div').addClass('result data-list').attr('data-name', item.name).css({cursor: 'move'});
                                    var header = dCrt('div').addClass('panel-heading-title')
                                        .css({position: 'relative', marginBottom: '2px'});
                                    row.append(header);
                                    var link = $(document.createElement('a')).attr('href', item.uri)
                                        .attr('target', '_blank').html(item[state.opts.labelKey]).css({marginLeft: '30px'});
                                    header.append(link);
                                    methods.pnlRight.createIcon(header,item);
                                    if(item.description) {
                                        var p = dCrt('div').css({
                                            margin: '0 0 5px 5px',
                                            padding: '0'
                                        }).append(item.description);
                                        methods.pnlRight.nav(p, item);
                                        methods.pnlRight.deleteScopeOrg(p);
                                        row.append(p);
                                    }

                                    if (item.externalUri) {
                                        var external = dCrt('div').addClass('external-link');
                                        var externalLink = $(document.createElement('a')).attr('href', item.externalUri)
                                            .attr('target', '_blank').html(item.externalUri);
                                        external.append(externalLink);
                                        row.append(externalLink)
                                    }
                                    container.append(row);
                                    methods.draggable(row, item);
                                }
                            }
                            return state.adding;
                        });
                        state.done = true;
                    }
                },
                deleteScopeOrg: function (node) {
                    var posLid;
                    var spanPos = node.find('span.pos-scoped-position');
                    if (spanPos.length > 0) {
                        posLid = spanPos.data('scope-position-lid');
                        if(!posLid){
                            posLid = spanPos.attr('scope-position-lid');
                        }
                        if (posLid) {
                            var qs = {
                                domain: state.KEY_SGE,
                                type: state.KEY_POS_KEY,
                                "native": {
                                    query: {
                                        filtered: {
                                            filter: {
                                                bool: {
                                                    must: [
                                                        {term: {'/object/endpoint/endpointFrom.relatedId.raw': posLid}}
                                                    ]
                                                }
                                            }
                                        }
                                    }
                                }
                            };
                            var success = function (data) {
                                if (data && data.results) {
                                    $.each(data.results, function () {
                                        var scopePositionItem = this;
                                        methods.pnlRight.insertDeleteSpan(scopePositionItem, node);
                                    });
                                }
                            };
                            $.htmlEngine.request('/query?limit=10000000', success, null, qs, 'post');
                        }
                    }
                },
                insertDeleteSpan:function (position,node) {
                    var spanOrg = node.find('span.org-scoped-positions');
                    if(!spanOrg){
                        return false;
                    }
                    if(!position){
                        var title = spanOrg.attr('data-pos-title');
                        var uri = spanOrg.attr('data-pos-uri');
                        if(title && uri){
                            position = {title: title, uri: uri};
                        }
                    }
                    if (position) {
                        spanOrg.each(function () {
                            var spanItemOrg = $(this);
                            var orgLid = spanItemOrg.data('organization-lid');
                            if (orgLid) {
                                var qo = {
                                    domain: state.KEY_ORG_E,
                                    type: state.KEY_ORG,
                                    "native": {
                                        query: {
                                            filtered: {
                                                filter: {
                                                    bool: {
                                                        must: [
                                                            {term: {'/object/endpoint/endpointFrom.relatedId.raw': orgLid}}
                                                        ]
                                                    }
                                                }
                                            }
                                        }
                                    }
                                };
                                var s = function (data) {
                                    if (data && data.results) {
                                        $.each(data.results, function () {
                                            var orgItem = this;
                                            if(orgItem && orgItem.lid === orgLid) {
                                                var span = $(document.createElement('span'))
                                                    .addClass('glyphicon glyphicon-remove')
                                                    .css({
                                                        cursor: 'pointer',
                                                        color: 'red',
                                                        display: 'inline-block',
                                                        padding: '3px'
                                                    });
                                                span.attr('title', 'Select to remove ' + position.title + ' from ' + orgItem.title);
                                                span.on('click', function () {
                                                    var spin = $.htmlEngine.indicators({
                                                        height: 16,
                                                        width: 16,
                                                        type: 'spinner'
                                                    });
                                                    spin.insertBefore(span);
                                                    span.hide();
                                                    var s = function () {
                                                        var locationForSpan = node.find("[data-organization-lid='" + orgLid + "']");
                                                        if (locationForSpan) {
                                                            node.find("[data-organization-lid='" + orgLid + "'] + br").remove();
                                                            node.find("[data-organization-lid='" + orgLid + "']").remove();
                                                        }
                                                        var exist = node.find('span.org-scoped-positions');
                                                        if (exist.length === 0) {
                                                            var spanScope = node.find('span.pos-scoped-position');
                                                            if (spanScope) {
                                                                node.find('span.pos-scoped-position').nextAll().remove();
                                                                node.find('span.pos-scoped-position').remove();
                                                            }
                                                        }
                                                        if (state.current.item && state.current.item.lid === orgLid) {
                                                            methods.pnlMiddle.content.init();
                                                        }
                                                        spin.remove();
                                                        span.remove();
                                                    };
                                                    var f = function () {
                                                        spin.remove();
                                                        span.show();
                                                        lusidity.info.red(msg);
                                                        lusidity.info.show(5);
                                                    };
                                                    var url = methods.link.url(state.KEY_POST_SP, position);
                                                    var d = methods.link.data(orgItem, position);
                                                    d['delete'] = true;
                                                    $.htmlEngine.request(url, s, f, d, 'post');

                                                });
                                                var locationForSpan = node.find("[data-organization-lid='" + orgLid + "']");
                                                if (locationForSpan) {
                                                    var fNode = node.find("[data-organization-lid='" + orgLid + "']");
                                                    if(fNode) {
                                                        fNode.removeAttr("style").before(span);
                                                    }
                                                }
                                            }
                                        });
                                    }
                                };
                                $.htmlEngine.request('/query?limit=10000000', s, null, qo, 'post');
                            }
                        });
                    }
                },
                get: function (text) {
                    var success = function (data) {
                        if(data) {
                            state.adding = false;
                            while (!state.done) {
                            }
                            methods.pnlRight.create(data);
                        }
                    };
                    var failed = function () {
                        lusidity.info.red("Sorry, that shouldn't have happened.");
                        lusidity.info.show(5);
                    };
                    var url = '/discover/suggest?phrase=' + encodeURI(text);
                    $.htmlEngine.request(url, success, failed, null, 'get');
                },
                make: function(parentNode, pt, item){
                    var container = dCrt('div');
                    parentNode.append(container);
                    if(pt.menu && pt.menu.opts){
                        pt.menu.opts.target = container;
                    }

                    var panel =$.htmlEngine.panel(container,pt.glyph,pt.title,null,false,null,(pt.menu ? pt.menu.opts : null));
                    $(container.panel('getHeader')).remove();
                    if(pt.init){
                        pt.init(container);
                    }

                    var s = function(data){
                        methods.html.table(panel, pt, data);
                    };
                    var f = function(){
                        lusidity.info.red("Sorry, something went wrong.");
                        lusidity.info.show(5);
                    };
                    $.htmlEngine.request('/query?limit=' + pt.limit, s, f, pt.qs.children(item), 'post');
                },
                createIcon:function (parentNode,item) {
                    var iconParent = dCrt('div').addClass('icon-parent').css({top: '0'});
                    var domainType = $.jCommon.string.getLast(item.vertexType, '/');
                    var url = 'url("/assets/img/types/' + domainType + '.png")';
                    var icon = dCrt('div').css({backgroundImage: url});
                    iconParent.append(icon);
                    parentNode.append(iconParent);
                }
            },
            breadcrumb:{
                init:function (data, node) {
                    if(!state.current.item){
                        return false;
                    }
                    if(!state.opts.panels.breadcrumb){
                        return false;
                    }
                    var s = function (data) {
                        if(data && data.results){
                            var ol = dCrt('ol').addClass('breadcrumb no-radius').css({margin: '0 0', fontSize: '12px'});
                            node.append(ol);
                            var on = 0;
                            $.each(data.results, function () {
                                if(on>0) {
                                    var item = this;
                                    var l = dCrt('li');
                                    var a = dCrt('a').attr("href", String.format('{0}?et_view={1}', item[state.KEY_ID], state.current.et_view)).attr('target', '_blank').html(item[state.KEY_TITLE]);
                                    ol.append(l.append(a));
                                }
                                on++;
                            });
                        }
                    };
                    $.htmlEngine.request(state.current.item[state.KEY_ID]+'/breadcrumb',s,s,null, 'get', false);
                }
            }
        };
        //public methods

        //Initialize
        methods.init();
    };

    //Default Settings
    $.organization.defaults = {
        "panels": {
            "breadcrumb": true
        },
        labelKey: 'title',
        get: {
            rootUrl: function(){
                return '/acs/security/principals';
            }
        }
    };


    //Plugin Function
    $.fn.organization = function(method) {
        if (method === undefined) method = {};

        if (typeof method === 'object') {
            return this.each(function() {
                new $.organization($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $organization = $(this).data('organization');
            switch (method) {
                case 'exists': return (null!==$organization && undefined!==$organization && $organization.length>0);
                case 'state':
                default: return $organization;
            }
        }
    };

})(jQuery);
