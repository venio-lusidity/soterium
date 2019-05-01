

;(function ($) {

    //Object Instance
    $.ditprAcl = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.ditprAcl.defaults, options);
        state.current = {
            items: [],
            content: {}
        };
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.KEY_FROM = "/object/endpoint/endpointFrom";

        // Store a reference to the environment object
        el.data('ditprAcl', state);
        var enclaves = ['/electronic/system/enclave/system_enclave', '/electronic/system/enclave/network_enclave', '/electronic/system/enclave/virtual_enclave'];

        // Private environment methods
        methods = {
            init: function() {
                state.opts.pnlMiddleNode.css({overflow: 'hidden'});
                state.opts.pnlMiddleNode.jNodeReady({onReady: function () {
                    var ul = dCrt('ul').addClass('nav nav-tabs');
                    state.opts.pnlMiddleNode.append(ul);
                    var li1 = dCrt('li').addClass('active');
                    var a1 = dCrt('a').attr('href', '#tab-assigned').attr('data-toggle', 'tab').attr('aria-expanded', true).html('Assigned');
                    li1.append(a1);
                    var li2 = dCrt('li');
                    var a2 = dCrt('a').attr('href', '#tab-available').attr('data-toggle', 'tab').attr('aria-expanded', true).html('Available');
                    li2.append(a2);
                    ul.append(li1).append(li2);

                    state.opts.tabContent = dCrt('div').addClass('tab-content');
                    state.opts.pnlMiddleNode.append(state.opts.tabContent);
                    state.opts.tabAssigned = dCrt('div').addClass('tab-pane active').attr('id', 'tab-assigned');
                    state.opts.tabContent.append(state.opts.tabAssigned);
                    state.opts.tabAvailable = dCrt('div').addClass('tab-pane active').attr('id', 'tab-available');
                    state.opts.tabContent.append(state.opts.tabAvailable);

                    window.setTimeout(function () {
                        var h = state.opts.pnlMiddleNode.availHeight(50);
                        var c = {overflow: 'hidden', maxHeight: h+'px', height: h+'px' };
                        state.opts.pnlMiddleNode.css(c);
                        state.opts.tabContent.css(c);
                        state.opts.tabAssigned.css(c);
                        state.opts.tabAssigned.children().remove();

                        state.opts.tabAvailable.css(c);
                        state.opts.tabAvailable.children().remove();
                        methods.pnlLeft.init();
                    }, 300);
                }});
            },
            draggable: function (node, data) {
                if (state.contentNode.is(':visible')) {
                    node.data('item', data);
                    node.draggable({
                        cursor: "move",
                        revert: true,
                        scroll: false,
                        start: function (event, ui) {
                            $(this).css({zIndex: 99999999});
                            var item = $(this).data('item');
                            if($.jCommon.string.startsWith(item.vertexType, state.KEY_PP)){
                                $(this).addClass('personnel-position');
                            }
                            else if($.jCommon.string.startsWith(item.vertexType, state.KEY_POS)){
                                $(this).addClass('scoped-position');
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
            droppable: function (item, pt, dropArea, propertyKey, key, value, accept) {
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
                                var failed = function (jqXHR, textStatus, errorThrow) {
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
            url: function (start, limit) {
                return '/query?start=' + start + '&limit=' + limit ;
            },
            clear: function (item, usr) {
                var defer = $.Deferred();
                var temp = [];
                methods.content.filterDitprs();
                $.each(state.assigned, function () {
                    if(!$.jCommon.string.equals(item.lid, this.lid, true)){
                        temp.push(this);
                    }
                });
                state.assigned = temp;
                item.edge = null;
                state.ditprs.push(item);
                methods.content.make(state.opts.tabAvailable, "All System Names", state.ditprs, usr, false);
                defer.resolve();
            },
            deleteOne: function (item, usr, row, cp) {
                var s = function (data) {
                    if(data) {
                        row.remove();
                        methods.clear(item, usr);
                    }
                };
                var f = function () {
                    row.show();
                };
                if(item.edge) {
                    $.htmlEngine.request(item.edge[state.KEY_ID], s, f, null, 'delete');
                }
            },
            link: function (item, usr, cb) {
                var s = function (data) {
                    if(data && data.edge) {
                        item.edge = data.edge;
                        state.authorized.push(data.edge);
                        cb(item, data);
                        data.edge.customized = true;
                        $.htmlEngine.request(data.edge[state.KEY_ID], s, f, data.edge, 'post');
                    }
                };
                var f = function () {
                    lusidity.info.red("Could not assign the requested System Name to the specified user.");
                    lusidity.info.show(10);
                };

                var q = {
                    from:{
                        vertexType: item.vertexType,
                        uri: item[state.KEY_ID]
                    },
                    to:{
                        vertexType: usr.vertexType,
                        uri: usr[state.KEY_ID]
                    }
                };
                $.htmlEngine.request(String.format('{0}/properties/acs/security/base_principal/authorized', item[state.KEY_ID]), s, f, q, 'post');
            },
            pnlLeft: {
                init: function () {
                    state.opts.pnlLeftNode.children().remove();
                    methods.pnlLeft.make();
                },
                make: function(){
                    var container = $(document.createElement('div'));
                    state.opts.pnlLeftNode.append(container);
                    var pnlBody =$.htmlEngine.panel(container, 'glyphicons glyphicons-group', 'Personnel', null, false, null, null);

                    var s = function (data) {
                        if(data && data.results){
                            methods.pnlLeft.list(data, pnlBody);
                        }
                    };
                    $.htmlEngine.request(methods.url(0, state.opts.limit), s, s, QueryFactory.matchAll('/people/person'), 'post');
                },
                list: function (data, container) {
                    var grp = dCrt('div').addClass('list-group no-radius');
                    container.append(grp);

                    var sorted = $.jCommon.array.sort(data.results, [{
                        property: 'lastName',
                        asc: true
                    }, {property: 'firstName', asc: true}]);

                    $.each(sorted, function () {
                        var usr = this;
                        var node = dCrt('a').addClass('list-group-item').html(String.format('{0}, {1}', usr.lastName, usr.firstName)).attr('href', '#').attr('title', usr[state.KEY_ID]);
                        grp.append(node);
                        node.on('click', function () {
                            grp.children().removeClass('active');
                            node.addClass('active');
                            methods.content.init(usr);
                        });
                    });
                }
            },
            content: {
                init: function(usr){
                    state.authorized = [];
                    state.assigned = [];
                    state.ditprs = [];

                    state.opts.tabAssigned.children().remove();
                    state.opts.tabAvailable.children().remove();

                    state.opts.pnlRightNode.css({overflow: 'hidden'});
                    state.opts.pnlRightNode.children().remove();
                    methods.content.getDitprs(usr);
                },
                filterDitprs: function () {
                    var tmp = [];
                    $.each(state.ditprs, function () {
                        var d = this;
                        var f = false;
                        $.each(state.authorized, function () {
                            var a = this;
                            if($.jCommon.string.equals(d.lid, a[state.KEY_FROM].relatedId)){
                                d.edge = a;
                                state.assigned.push(d);
                                f=true;
                                return false;
                            }
                        });
                        if(!f){
                            tmp.push(d);
                        }
                    });
                    state.ditprs = tmp;
                },
                process: function (usr) {
                    state.assigned = [];
                    methods.content.filterDitprs();

                    methods.content.make(state.opts.tabAssigned, "Assigned System Names", state.assigned, usr, true);

                    methods.content.make(state.opts.tabAvailable, "Available System Names", state.ditprs, usr, false);
                },
                getAuthorized: function (usr) {
                    state.authorized = [];
                    var on = 0;
                    function check() {
                        if(on===3){
                            methods.content.process(usr);
                        }
                        else{
                            window.setTimeout(check, 100);
                        }
                    }
                    var s = function (data) {
                        if(data && data.results){
                            $.each(data.results, function () {
                                state.authorized.push(this);
                            });
                        }
                        on++;
                    };
                    $.each(enclaves, function () {
                        var q = {
                            domain: '/object/edge/authorized_edge',
                            type: this.toString(),
                            return_edge: true,
                            "native": {query: {filtered: {filter: {bool: {must: [{term: {'/object/endpoint/endpointTo.relatedId.raw': usr.lid}}]}}}}}
                        };
                        $.htmlEngine.request(methods.url(0, state.opts.limit), s, s, q, 'post', false);
                    });
                    check();
                },
                getDitprs: function (usr) {
                    state.ditprs = [];
                    var on = 0;
                    function check() {
                        if(on===3){
                            methods.content.getAuthorized(usr);
                        }
                        else{
                            window.setTimeout(check, 100);
                        }
                    }
                    var s = function (data) {
                        on++;
                        if(data && data.results){
                            $.each(data.results, function () {
                                if(undefined!==this.ditprId) {
                                    state.ditprs.push(this);
                                }
                            });
                        }
                    };
                    $.each(enclaves, function () {
                        var q = QueryFactory.matchAll(this.toString());
                        $.htmlEngine.request(methods.url(0, state.opts.limit), s, s, q, 'post');
                    });
                    check();
                },
                make: function (container, title, items, usr, assigned) {
                    state.opts.tabAvailable.show();
                    items = $.jCommon.array.sort(items, [{property: 'ditprId', asc: true}]);
                    var options = {
                        fill: true,
                        groupsEnabled: false,
                        filter: null,
                        view: "details",
                        data: {results: items},
                        title: title,
                        realTime: true,
                        settings: null,
                        getUrl: function (data, start, limit) {
                            return null;
                        },
                        sortable: true,
                        sortOn: [ {property: 'ditprId', asc: true}],
                        lists:{
                            groups:[],
                            filters:[]
                        },
                        actions: [],
                        details: {
                            adjust: 800,
                            sort: true,
                            search: true,
                            mapping: [
                                {header: {title: "#", callback: function (th, node) {
                                    th.css({width: '20px'});
                                }}, property: '#', type: 'integer', callback: function (td, item, value) {
                                    td.css({width: '20px'});
                                }},
                                {
                                    header: {title: "Allow", callback: function (th, node) {
                                        if(!assigned){
                                            th.hide();
                                            return false;
                                        }
                                    th.css({width: '20px'});
                                }},
                                    property: 'cbox', type: 'input', callback: function (td, item, value) {
                                    if(!assigned){
                                        td.hide();
                                        return false;
                                    }
                                    td.find('input').prop('checked', !item.edge.denied);
                                }, action: function (td, input, item) {
                                    var allow = input.is(':checked');
                                    item.edge.denied = !allow;
                                    item.edge.customized = true;
                                    var s = function (data) {
                                        if(data) {
                                            var t = td.parent().find('#title-'+item.lid);
                                            t.addClass('enclave-customized');
                                            t.attr('title', "This System Name authorization has been modified and will not be affected by any update or delete automated processes.");
                                        }
                                    };
                                    $.htmlEngine.request(item.edge[state.KEY_ID], s, s, item.edge, 'post');
                                }},
                                {
                                    header: {title: "", callback: function (th, node) {
                                        if(!assigned){
                                            th.hide();
                                            return false;
                                        }
                                        th.css({width: '20px'});
                                    }},
                                    property: 'btn', type: 'button',label: 'Remove', callback: function (td, item, value) {
                                    if(!assigned){
                                        td.hide();
                                        return false;
                                    }
                                }, action: function (td, input, item) {
                                    function cb(item, data) {
                                        td.parent().remove();
                                    }
                                    methods.deleteOne(item, usr, td.parent(), cb);
                                }},
                                {
                                    header: {title: "", callback: function (th, node) {
                                        if(assigned){
                                            th.hide();
                                            return false;
                                        }
                                        th.css({width: '20px'});
                                    }},
                                    property: 'btn', type: 'button',label: 'Add', callback: function (td, item, value) {
                                    if(assigned){
                                        td.hide();
                                        return false;
                                    }
                                }, action: function (td, input, item) {
                                    function cb(itm, data) {
                                        state.opts.tabAssigned.children().remove();
                                        itm.edge.customized = true;
                                        state.assigned.push(itm);
                                        methods.content.make(state.opts.tabAssigned, "Assigned System Names", state.assigned, usr, true);
                                        var rows = td.parent().nextAll();
                                        $.each(rows, function () {
                                            var r = $(this);
                                            var t = $(r.children()[0]);
                                            var v = parseInt(t.html());
                                            v -= 1;
                                            t.html(v);
                                        });
                                        td.parent().remove();
                                    }
                                    methods.link(item, usr, cb);
                                }},
                                {header: { title: "System Name", sortable: true, property: 'ditprId'}, searchable: true, property: 'ditprId', type: 'string', callback: function (td, item, value) {
                                    td.append(value);
                                    td.attr('id', 'title-'+item.lid);
                                    if(item.edge && item.edge.customized){
                                        td.addClass('enclave-customized');
                                        td.attr('title', "This System Name authorization has been modified and will not be affected by any update or delete automated processes.");
                                    }
                                }},
                                {header: { title: "Enclave", sortable: true, property: 'title'}, searchable: true, property: 'title', type: 'string', callback: function (td, item, value) {
                                    td.append(dCrt('a').attr('href', item[state.KEY_ID]).attr('target', '_blank').html(item.title));
                                }}
                            ]
                        }
                    };
                    container.jFilterBar(options);
                    window.setTimeout(function () {
                        if(!state.opts.tabAvailable.hasClass('active')) {
                            state.opts.tabAvailable.hide();
                        }
                    }, 300);
                }
            }
        };
        //public methods

        //Initialize
        methods.init();
    };

    //Default Settings
    $.ditprAcl.defaults = {
        limit: 1000
    };


    //Plugin Function
    $.fn.ditprAcl = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === 'object') {
            return this.each(function() {
                new $.ditprAcl($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $organization = $(this).data('ditprAcl');
            switch (method) {
                case 'exists': return (null!==$organization && undefined!==$organization && $organization.length>0);
                case 'state':
                default: return $organization;
            }
        }
    };

})(jQuery);
