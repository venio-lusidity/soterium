

;(function ($) {

    //Object Instance
    $.poam = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.poam.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.GROUPS = ['ISSM'];
        state.KEY_STORE = 'technology_security_vulnerabilities_forms_poam';
        state.KEY_POAM = '/technology/security/vulnerabilities/forms/poam';
        state.KEY_VULN = '/technology/security/vulnerabilities/base_vulnerability/vulnerabilities';
        state.KEY_TRGT = '/ta/data_vertex/targets';
        state.KEY_WF = '/process/workflow/workflow';
        state.opts.url = $.jCommon.url.create(window.location.href);
        state.authorized = {groups:["Information System Security Manager"]};

        state.poams = {
            queries: {
                get: function (item) {
                    return {
                        domain: '/object/edge',
                        type: state.KEY_POAM,
                        sort: {on: 'createdWhen', direction: 'desc'},
                        "native": {
                            query: {
                                filtered: {
                                    filter: {
                                        bool: {
                                            must: [
                                                {term: {'label.raw': state.KEY_TRGT}},
                                                {term: {'/object/endpoint/endpointTo.relatedId.raw': item.lid}}
                                            ]
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                workflows: function(){
                    return {
                        domain: state.KEY_WF,
                        type: state.KEY_WF,
                        sort: {on: 'title', direction: 'asc'},
                        "native": {query:{"match_all":{}}}
                    }
                }
            }
        };


        // Store a reference to the environment object
        el.data("poam", state);

        // Private environment methods
        methods = {
            init: function() {
                state.pageItem = null;
                var s = function(data){
                    state.pageItem = data;
                    methods.content.init();
                };
                $.htmlEngine.request(state.opts.url.relativePath, s, null, null, 'get', true);

                $(document).on('vuln-matrix-vulnerabilities-selected', function (e, node, item, checked) {
                    if (checked.checked) {
                        state.vulnSelected.push(item);
                    }
                    else {
                        var temp = [];
                        $.each(state.vulnSelected, function () {
                            var vuln = this;
                            if (item[state.KEY_ID] !== vuln[state.KEY_ID]) {
                                temp.push(vuln);
                            }
                        });
                        state.vulnSelected = temp;
                    }
                });
            },
            html: {
                panel: function (container, glyph, title, url, borders, actions, menu) {
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
                    var content = $(document.createElement('table')).addClass('table table-hover');
                    container.append(content);
                    methods.html.getTableHead(['Identifier', 'Submitted'], content);
                    methods.html.getTableBody(['title', 'dt::createdWhen'], $.jCommon.array.sort(data.results, 'asc', ['title']), content, 0);
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
                getTableBody: function(properties, data, container){
                    var tbody = $(document.createElement('tbody'));
                    container.append(tbody);
                    if(data) {
                        $.each(data, function () {
                            methods.html.makeRow(tbody, properties, this)
                        });
                    }
                },
                makeRow: function(tbody, properties, item){
                    var row = $(document.createElement('tr')).addClass('table-row');
                    tbody.append(row);

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
                                };
                                var f = function(){
                                    lusidity.info.red(msg);
                                    lusidity.info.show(5);
                                };
                                var url = methods.link.url(item);
                                var d = methods.link.data(item, state.current.item);
                                d['delete']= true;
                                $.htmlEngine.request(url, s, f, d, 'post');
                            });
                            if(item.status && item.status === "processing"){
                                value.hide();
                            }
                        }
                        else if($.jCommon.string.equals(key, 'tableLineNumber')){
                            on++;
                            td.css({width: '50px', maxWidth: '50px'});
                            value = $(document.createElement('span')).html(on);
                        }
                        else if(key==='deprecated'){
                            td.css({width: '100px', maxWidth: '100px'});
                            var cb = $(document.createElement('input')).attr('type', 'checkbox');
                            if(item[key]==='true'){
                                cb.attr('checked', 'checked');
                            }
                            value = $(document.createElement('span')).append(cb);
                        }
                        else if(key==="title"){
                            value = $(document.createElement('a')).html(item[key])
                                .attr('href', item[state.KEY_ID]).attr('target', '_blank');
                        }
                        else if($.jCommon.string.startsWith(key, 'dt::')){
                            var last = $.jCommon.string.getLast(key, '::');
                            var dt = new Date(item[last]);
                            var d = $.jCommon.dateTime.format(dt, 'j\\ M Y\\');
                            value = $(document.createElement('span')).html(d);
                        }
                        else{
                            value = $(document.createElement('span')).html(item[key]);
                        }
                        td.css({marginRight: '10px'}).append(value);
                        row.append(td);
                    });

                    row.addClass('selectable');

                    row.on('click', function(){
                        row.siblings().removeClass('selected');
                        row.addClass('selected');
                        if(state.poams.delNode){
                            state.poams.delNode.removeAttr('disabled');
                        }
                        state.poams.selected = {
                            item: item,
                            node: row
                        };
                        var s = function(data){
                            if(data && data.results){
                                $(document).trigger('poam-content-item-selected', [data.results]);
                            }
                        };
                        var f = function(){};
                        var url = item[state.KEY_ID] + '/properties/technology/security/vulnerabilities/base_vulnerability/vulnerabilities';
                        $.htmlEngine.request(url, s, f, null, 'get');
                    });
                }
            },
            content: {
                init: function() {
                    state.worker.node.children().remove();
                    var container = $(document.createElement('div'));
                    state.worker.node.append(container);
                    var options = {
                        target: container
                    };
                    var c = function (d) {
                        if (d.auth) {
                            options.buttons = [
                                {
                                    id: 'add-poam',
                                    glyphicon: 'glyphicons glyphicons-file-plus',
                                    tn: 'add-poam',
                                    title: 'New POA&M',
                                    cls: 'green',
                                    css: {maxWidth: '40px', maxHeight: '34px', padding: '3px 4px'}
                                },
                                {
                                    id: 'delete-poam',
                                    glyphicon: 'glyphicons glyphicons-file-minus',
                                    tn: 'delete-poam',
                                    title: 'Cancel POA&M',
                                    cls: 'red',
                                    css: {maxWidth: '40px', maxHeight: '34px', padding: '3px 4px'},
                                    disabled: true
                                }
                            ]
                        }
                    };
                    $.login.authorized(state.authorized, c);
                    var content = methods.html.panel(container, state.opts.glyph, state.opts.title, null, false, null, options);

                    container.on('menu-bar-add-poam', function () {
                        methods.form("New POA&M");
                        state.vulnSelected = [];
                        $(document).trigger('poam-content-add');
                    });
                    container.on('menu-bar-delete-poam', function () {
                        if (null === state.poams.selected) {
                            lusidity.info.yellow("Please select a POA&M to delete.");
                            lusidity.show(5);
                            state.poams.delNode.attr('disabled', 'disabled');
                        }
                        else {
                            methods.content.deleteContent();
                        }
                    });
                    state.poams.delNode = $('#delete-poam');
                    state.poams.addNode = $('#add-poam');
                    var s = function(data){
                        function callback(other) {
                            if(other && other.results) {
                                methods.html.table(container, other);
                            }
                            else{
                                lusidity.info.yellow("No items found.");
                                lusidity.info.show(5);
                            }
                        }
                    };
                    var f = function(){
                        lusidity.info.red("Sorry, something went wrong.");
                        lusidity.info.show(5);
                    };
                    $.htmlEngine.request('/query?limit=1000', s, f, state.poams.queries.get(state.pageItem), 'post');
                },
                deleteContent: function(){
                    var btnBar = $(document.createElement('div')).addClass('btn-bar');
                    var delButton = $(document.createElement('button')).attr('type', 'button')
                        .addClass('btn btn-danger').html('Delete');
                    btnBar.append(delButton);
                    delButton.on('click', function () {
                       pageCover.busy(true);
                        var s = function (data) {
                            $(document).trigger('poam-content-item-deleted');
                            if (undefined===data || null===data || data.error) {
                                lusidity.info.red("Something when wrong while trying to delete.");
                                lusidity.show(5);
                            }
                            methods.content.init();
                            state.pageModal('hide');
                            pageCover.busy(false);
                        };
                        $.htmlEngine.request(state.poams.selected.item[state.KEY_ID], s, s, null, 'delete');
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
                            var title = state.poams.selected.item.title;
                            if(!title){
                                title = state.poams.selected.item[state.KEY_ID];
                            }
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
                }
            },
            link: {
                url: function (item, key) {
                    var id = item.uri ? item.uri : item[state.KEY_ID];
                    return id + '/properties' + key;
                },
                data: function (from, to) {
                    return {
                        from: from,
                        to: to
                    };
                }
            },
            makeLink: function(from, to, key, success, failed){
                var s = function(data){
                   if(success && $.isFunction(success)){
                       success(data);
                   }
                };
                var f = function(){
                    if(failed && $.isFunction(failed)){
                        failed();
                    }
                };
                var url = methods.link.url(from, key);
                var data = methods.link.data(from, to);
                $.htmlEngine.request(url, s, f, data, 'post', false);
            },
            form: function (title, data, show) {
                state.worker.node.children().remove();
                var container = $(document.createElement('div'));
                state.worker.node.append(container);
                var mode = !data ? 'add' : 'edit';
                var defaultData = {
                    vertexType: state.KEY_POAM,
                    edgeType: '/object/edge',
                    edgeKey: state.KEY_TRGT,
                    edgeDirection: 'in',
                    toId: state.pageItem[state.KEY_ID]
                };
                container.formBuilder({
                    title: title,
                    borders: false,
                    css: {'margin-right': '0'},
                    panelCss: {padding: '5px'},
                    glyph: 'glyphicons glyphicons-file',
                    url: null,
                    actions: [],
                    show: false,
                    data: data,
                    defaultData:defaultData,
                    mode: mode,
                    before: function () {
                        var save = true;
                        if(state.vulnSelected.length<=0){
                            save = false;
                            lusidity.info.red('You must select at least one vulnerability.');
                            lusidity.info.show(5);
                        }
                        return {save: save, cancel: false};
                    },
                    isDeletable: function () {
                        return false;
                    },
                    deleteMessage: function (body, data) {
                    },
                    onDelete: function (item) {
                    },
                    close: function (node) {
                        $(document).trigger('poam-content-form-close');
                        methods.content.init();
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
                            var linked;
                            var item = data.item.result;
                            var success = function(data){
                                $.each(state.vulnSelected, function () {
                                    try {
                                        var s2 = function () {
                                        };
                                        var f2 = function () {
                                            lusidity.info.red('Failed to make link for ' + this.vulnId + '.');
                                            lusidity.info.show(5);
                                        };
                                        methods.makeLink(item, this, state.KEY_VULN, s2, f2);
                                    }
                                    catch(e){}
                                });

                                $(document).trigger('poam-content-form-close');
                                methods.content.init();
                            };
                            var failed = function(){
                                lusidity.info.red('Failed to make link to ' + data.title + '.');
                                lusidity.info.show(5);

                                $(document).trigger('poam-content-form-close');
                                methods.content.init();
                                return false;
                            };
                            linked = methods.makeLink(item, state.pageItem, state.KEY_TRGT, success, failed);
                        }
                    },
                    onFailed: function () {
                        lusidity.info.red('Sorry, something went wrong.');
                        lusidity.info.show(5);
                    },
                    nodes: [
                        {
                            node: 'input',
                            readOnly: true,
                            type: 'text',
                            required: true,
                            width: '100%',
                            id: 'title',
                            label: 'POAM Identifier',
                            placeholder: '',
                            onChanged: function(node){
                            },
                            onAvailable: function(node){
                                node.focus();
                                var d = new Date();
                                var val = 'POAM-' + d.getJulian() + '-' + d.getHours() + d.getMinutes() + d.getSeconds();
                                node.attr('actual', val);
                                node.val(val);
                                node.blur();
                            }
                        },
                        {
                            node: 'input',
                            readOnly: true,
                            type: 'text',
                            required: true,
                            width: '100%',
                            id: '/people/person/initiators',
                            label: 'ISSM initiator',
                            placeholder: '',
                            onChanged: function(node){
                            },
                            onAvailable: function(node){
                                node.focus();
                                var name = $(document).login('getName');
                                var actual = $(document).login('getId');
                                node.attr('actual', actual);
                                node.val(name);
                                node.blur();
                            }
                        },
                        {
                            focussed: false,
                            node: 'modal',
                            type: 'text',
                            width: '100%',
                            placeholder: 'Click to select a workflow.',
                            modal: {
                                name: "listViewModal",
                                plugin: {
                                    title: 'Select a Workflow',
                                    glyph: 'glyphicons glyphicons-flowchart',
                                    get: {
                                        query: function (data) {
                                            return state.poams.queries.workflows();
                                        }
                                    },
                                    mapper: {
                                        value: state.KEY_ID,
                                        displayed: 'title',
                                        description: 'description'
                                    },
                                    limit: 10000,
                                    fade: false
                                }
                            },
                            linked: true,
                            id: '/process/workflow/workflow/workflows',
                            label: 'Workflow',
                            readOnly: true,
                            required: true,
                            onChanged: function(node, item){
                            }
                        },
                        {
                            node: 'datetime',
                            type: 'text',
                            required: true,
                            width: '100%',
                            id: 'expiresOn',
                            label: 'Get well date',
                            title: "Get well date needs to be 10 days or greater.",
                            min: 10,
                            max: 365,
                            placeholder: 'Select a date within 365 days.',
                            onChanged: function(node){
                                var val = node.val();
                                if(val && val==='10'){
                                    node.val('');
                                }
                            },
                            onAvailable: function(node){
                            }
                        },
                        {
                            node: 'dropdown',
                            'required': true,
                            id: 'reason',
                            width: '100%',
                            label: "Reason",
                            placeholder: 'Select a reason for the delay...',
                            options: [
                                {displayed: 'Funding', value: 'Funding'},
                                {displayed: 'Testing ', value: 'Testing'},
                                {displayed: 'Pending Software Update', value: 'Pending Software Update'},
                                {displayed: 'Patch Unavailable', value: 'Patch Unavailable'}]
                        },
                        {
                            node: 'textarea',
                            required: true,
                            id: '/system/primitives/raw_string/descriptions',
                            map: {
                                direction: 'out',
                                key: 'value',
                                vertexType: '/system/primitives/raw_string'
                            },
                            label: 'Mission Impact',
                            css: {width: '100%', height: '100px'}
                        }
                    ],
                    getUrl: function () {
                        return (null===data) ?  '/domains/' + state.KEY_STORE + '/new' : data[state.KEY_ID];
                    }
                });
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.poam.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.poam = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.poam($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $poam = $(this).data('poam');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $poam;
            }
        }
    };

    $.poam.call= function(elem, options){
        elem.poam(options);
    };

    try {
        $.htmlEngine.plugins.register("poam", $.poam.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
