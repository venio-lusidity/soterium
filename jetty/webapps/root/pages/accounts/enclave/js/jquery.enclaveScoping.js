;(function ($) {

    //Object Instance
    $.enclaveScoping = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.enclaveScoping.defaults, options);
        state.current = {
            items: [],
            content: {}
        };
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.KEY_FROM = "/object/endpoint/endpointFrom";

        // Store a reference to the environment object
        el.data('enclaveScoping', state);
        var enclaves = ['/electronic/system/enclave/system_enclave', '/electronic/system/enclave/network_enclave', '/electronic/system/enclave/virtual_enclave', '/electronic/system/enclave/organization_enclave', '/electronic/system/enclave/location_enclave'];
        // Private environment methods
        methods = {
            init: function() {
                state.opts.pnlMiddleNode.css({overflow: 'hidden'});
                state.opts.pnlMiddleNode.jNodeReady({
                    onReady: function () {
                        var ul = dCrt('ul').addClass('nav nav-tabs');
                        state.opts.pnlMiddleNode.append(ul);
                        var li1 = dCrt('li').addClass('active');
                        state.opts.ta = dCrt('a').attr('href', '#tab-assigned').addClass('no-radius').attr('data-toggle', 'tab').attr('aria-expanded', true).html('Assigned');
                        li1.append(state.opts.ta);
                        var li2 = dCrt('li');
                        state.opts.tav = dCrt('a').attr('href', '#tab-available').addClass('no-radius').attr('data-toggle', 'tab').attr('aria-expanded', true).html('Available');
                        li2.append(state.opts.tav);
                        var li3 = dCrt('li');
                        state.opts.tad = dCrt('a').attr('href', '#tab-discover-acc').addClass('no-radius').attr('data-toggle', 'tab').attr('aria-expanded', true).html('Discover and Make Available');
                        li3.append(state.opts.tad);
                        ul.append(li1).append(li2).append(li3);

                        state.opts.c = dCrt('div').addClass('tab-content').css({position: 'relative'});
                        state.opts.pnlMiddleNode.append(state.opts.c);
                        state.opts.ca = dCrt('div').addClass('tab-pane active').attr('id', 'tab-assigned').css({position: 'relative'});
                        state.opts.c.append(state.opts.ca);
                        state.opts.cav = dCrt('div').addClass('tab-pane').attr('id', 'tab-available').css({position: 'relative'});
                        state.opts.c.append(state.opts.cav);
                        state.opts.cad = dCrt('div').addClass('tab-pane').attr('id', 'tab-discover-acc').css({position: 'relative'});
                        state.opts.c.append(state.opts.cad);

                        state.opts.tad.on('click', function () {
                            state.opts.ca.hide();
                            state.opts.cav.hide();
                            state.opts.cad.show();
                            methods.discover.init();
                        });
                        state.opts.ta.on('click', function () {
                            state.opts.ca.show();
                            state.opts.cav.hide();
                            state.opts.cad.hide();
                        });
                        state.opts.tav.on('click', function () {
                            state.opts.ca.hide();
                            state.opts.cav.show();
                            state.opts.cad.hide();
                        });

                        lusidity.environment('onResize', function () {
                            methods.resize();
                        });

                        window.setTimeout(function () {
                            methods.resize();
                            methods.pnlLeft.init();
                        }, 300);
                    }
                });
            },
            resize: function () {
                var h = state.opts.pnlMiddleNode.availHeight(50);
                var c = {overflow: 'hidden', maxHeight: h+'px', height: h+'px' };
                state.opts.pnlMiddleNode.css(c);
                state.opts.c.css(c);
                state.opts.ca.css(c);
                state.opts.cav.css(c);
                if(state.opts.discoverRsltNode && state.opts.discoverRsltNode.length>0) {
                    h -= 50;
                    dHeight(state.opts.discoverRsltNode, h, h, h);
                }
            },
            url: function (start, limit) {
                return '/query?pu=true&start=' + start + '&limit=' + limit;
            },
            clear: function (item, usr) {
                var defer = $.Deferred();
                var temp = [];
                methods.content.filterEnclaves();
                $.each(state.assigned, function () {
                    if(!$.jCommon.string.equals(item.lid, this.lid, true)){
                        temp.push(this);
                    }
                });
                state.assigned = temp;
                item.edge = null;
                state.enclaves.push(item);
                methods.content.make(state.opts.cav, "All Systems", state.enclaves, usr, false);
                defer.resolve();
            },
            deleteOne: function (item, usr, row, cb) {
                var s = function (data) {
                    if(data) {
                        if(cb && $.isFunction(cb)){
                            cb(item, data);
                        }
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
                    lusidity.info.red("Could not assign the request Enclave to the specified user.");
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
                $.htmlEngine.request(String.format('{0}/properties/acs/security/base_principal/authorized?pu=true', item[state.KEY_ID]), s, f, q, 'post');
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
                    var sorted = $.jCommon.array.sort(data.results, [{
                        property: 'lastName',
                        asc: true
                    }, {property: 'firstName', asc: true}]);

                    var items = [];
                    $.each(sorted, function () {
                        var usr = this;
                        var content = dCrt('div').addClass('list-group no-radius').attr('data-id', usr[state.KEY_ID]).css({margin: '0'});
                        container.append(content);

                        var item = {
                            title: String.format('{0}, {1}', usr.lastName, usr.firstName),
                            link: usr[state.KEY_ID],
                            data: usr,
                            groupedItems: [],
                            onExpand: function (item, leaf) {
                                if(item.content.children().length>0){
                                    return false;
                                }
                                var s = function (data) {
                                    if(data && data.results && data.results.length>0){
                                        var srt = $.jCommon.array.sort(data.results, [{property: 'title', asc: true}]);
                                        $.each(srt, function () {
                                            var ps = this;
                                            var node = dCrt('a').addClass('list-group-item no-radius').html(ps.title).attr('href', '#').attr('data-id', ps[state.KEY_ID]);
                                            content.append(node);
                                            node.on('click', function () {
                                                state.opts.ta.click();
                                                container.find('.active').removeClass('active');
                                                content.children().removeClass('active');
                                                node.addClass('active');
                                                state.current.ps = ps;
                                                methods.content.init(ps);
                                            });
                                        });
                                    }
                                    else{
                                        var node = dCrt('a').addClass('list-group-item no-radius').html("No positions found.");
                                        item.content.append(node);
                                    }
                                };
                                $.htmlEngine.request(String.format('{0}/acs/positions', usr[state.KEY_ID]), s, s, null, 'get');
                            },
                            content: content
                        };
                        items.push(item);
                    });
                    container.spring({leafs: items});
                }
            },
            discover: {
                init: function () {
                    state.opts.discoverRsltNode = dCrt('div').addClass('results-ht');
                    state.opts.cad.children().remove();
                    var discoverNode = dCrt('div').css({height: 'inherit', maxHeight: 'inherit'});
                    state.opts.cad.append(discoverNode);
                    var discoverContNode = dCrt('div').css({padding: '10px'});
                    discoverNode.append(discoverContNode);
                    var input = dCrt('input')
                        .attr('type', 'text')
                        .attr('placeholder', 'What system are you looking for (Title, DITPR or COAMS ID)?')
                        .attr('title', 'Organizations and people.')
                        .css({width: '100%', padding: '2px'});
                    discoverContNode.append(input);
                    var h = state.opts.pnlMiddleNode.availHeight(100);
                    dHeight(state.opts.discoverRsltNode, h, h, h);
                    discoverContNode.append(state.opts.discoverRsltNode);
                    var last = '';
                    var request;
                    input.on('keyup', function () {
                        var text = input.val();
                        state.opts.discoverRsltNode.children().remove();
                        if (!$.jCommon.string.equals(text, last)) {
                            if(request){
                                request.abort();
                            }
                            var url = '/accreditation/list?limit=100&phrase=' + encodeURI(text);
                            var s = function (data) {
                                methods.discover.create(data, state.opts.discoverRsltNode);
                            };
                            var f = function () {};
                            request = $.htmlEngine.request(url, s, f, null, 'get');
                            last = text;
                        }
                    });
                },
                create: function (data, container) {
                    if (data && data.results) {
                        state.adding = true;
                        state.done = false;
                        container.children().remove();
                        data.results = $.jCommon.array.sort(data.results, [{property: 'ditprId', asc: true}]);
                        $.each(data.results, function () {
                            var item = this;
                            var row = dCrt('div').addClass('result data-list').css({position: 'relative'}).attr('data-name', item.name);
                            var header = dCrt('div').addClass('panel-heading-title')
                                .css({position: 'relative', margin: '2px 60px 2p 2px'});
                            row.append(header);
                            var link = $(document.createElement('a')).attr('href', item.uri)
                                .attr('target', '_blank').css({margin: '5px 5px 0 5px'});
                            var s1 = dCrt('span').append(item[state.KEY_TITLE]);
                            header.append(s1);
                            if(item.ditprId) {
                                var s2 = dCrt('span').html(String.format('<strong>DITPR ID:</strong> {0}', item.ditprId.toString())).css({marginLeft: '5px'});
                                header.append(s2);
                            }
                            if(item.nameId) {
                                var s3 = dCrt('span').html(String.format('<strong>COAMS ID:</strong> {0}', item.nameId.toString())).css({marginLeft: '5px'});
                                header.append(s3);
                            }
                            header.append(link);

                            if(!item._found) {
                                var btn = dCrt('button').addClass('btn btn-success').css({position: 'absolute', right: '5px'}).html('Add');
                                row.append(btn);
                                var id = item[state.KEY_ID];
                                btn.on('click', function () {
                                    btn.attr('disabled', 'disabled');
                                    var s = function (data) {
                                        if (data && data.success) {
                                            row.remove();
                                            if(state.current.ps) {
                                                methods.content.init(state.current.ps);
                                            }
                                        }
                                        else{
                                            btn.removeAttr('disabled');
                                        }
                                        state.opts.cad.loaders('hide');
                                    };
                                    var url = '/accreditation/list';
                                    $.htmlEngine.request(url, s, s, {id: id}, 'post');
                                });
                            }

                            container.append(row);
                            return state.adding;
                        });
                        state.done = true;
                    }
                }
            },
            content: {
                init: function(usr){
                    if(state.opts.ca.is(":visible")) {
                        $.htmlEngine.busy(state.opts.ca, {type: 'cube', cover: true});
                    }
                    else if (state.opts.cav.is(":visible")) {
                        $.htmlEngine.busy(state.opts.cav, {type: 'cube', cover: true});
                    }
                    else{
                        $.htmlEngine.busy(state.opts.cad, {type: 'cube', cover: true});
                    }

                    state.authorized = [];
                    state.assigned = [];
                    state.enclaves = [];

                    state.opts.ca.children().remove();
                    state.opts.cav.children().remove();

                    state.opts.pnlRightNode.css({overflow: 'hidden'});
                    state.opts.pnlRightNode.children().remove();

                    methods.content.getEnclaves(usr);
                },
                filterEnclaves: function () {
                    var tmp = [];
                    $.each(state.enclaves, function () {
                        var d = this;
                        var f = false;
                        $.each(state.authorized, function () {
                            var a = this;
                            if($.jCommon.string.equals(d.lid, a[state.KEY_FROM].relatedId)){
                                d.edge = a;
                                d._count = a._count;
                                d._total = a._total;
                                state.assigned.push(d);
                                f=true;
                                return false;
                            }
                        });
                        if(!f){
                            tmp.push(d);
                        }
                    });
                    state.enclaves = tmp;
                },
                process: function (usr) {
                    state.assigned = [];
                    methods.content.filterEnclaves();
                    methods.content.make(state.opts.ca, "Assigned Enclaves", state.assigned, usr, true);
                    methods.content.make(state.opts.cav, "Available Enclaves", state.enclaves, usr, false);
                },
                getAuthorized: function (usr) {
                    state.authorized = [];
                    var s = function (data) {
                        if(data && data.results){
                            $.each(data.results, function () {
                                state.authorized.push(this);
                            });
                        }
                        methods.content.process(usr);
                        if(state.opts.cav.is(":visible")) {
                            state.opts.cav.loaders('hide');
                        }
                    };
                    $.htmlEngine.request("/admin/scoping/enclaves?id="+usr[state.KEY_ID], s, s, null, 'get', false);
                },
                getEnclaves: function (usr) {
                    state.enclaves = [];
                    var s = function (data) {
                        if(data && data.results){
                            $.each(data.results, function () {
                                state.enclaves.push(this);
                            });
                            methods.content.getAuthorized(usr);
                            if(state.opts.ca.is(":visible")) {
                                state.opts.ca.loaders('hide');
                            }
                        }
                    };
                    $.htmlEngine.request("/admin/enclaves", s, s, null, 'get');
                },
                make: function (container, title, items, usr, assigned) {
                    state.opts.cav.show();
                    items = $.jCommon.array.sort(items, [{property: 'title', asc: true}]);
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
                        sortOn: [ {property: 'title', asc: true}],
                        lists:{
                            groups:[],
                            filters:[]
                        },
                        actions: [],
                        details: {
                            adjust: 800,
                            sort: true,
                            search: true,
                            searchable: ['nameId'],
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
                                            var d = td.parent().find('#ditprId-'+item.lid);
                                            var f = 'enclave-customized';
                                            var g = "This System authorization has been modified and will not be affected by any update or delete automated processes.";
                                            t.addClass(f);
                                            t.attr('title', g);
                                            t.addClass(f);
                                            t.attr('title', g);
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
                                    var sp = $.htmlEngine.getSpinner();
                                    td.children().hide();
                                    td.append(sp);
                                    function cb(item, data) {
                                        var rows = td.parent().nextAll();
                                        $.each(rows, function () {
                                            var r = $(this);
                                            var t = $(r.children()[0]);
                                            var v = parseInt(t.html());
                                            v -= 1;
                                            t.html(v);
                                        });
                                        container.jFilterBar('remove', {item: item});
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
                                    var sp = $.htmlEngine.getSpinner();
                                    td.children().hide();
                                    td.append(sp);
                                    function cb(itm, data) {
                                        state.opts.ca.children().remove();
                                        itm.edge.customized = true;
                                        state.assigned.push(itm);
                                        methods.content.make(state.opts.ca, "Assigned Enclaves", state.assigned, usr, true);
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
                                {header: { title: " Assets Authorized", sortable: false, property: '_count'}, property: '_count', type: 'string', callback: function (td, item, value) {
                                    td.append(String.format("{0}/{1}", $.jCommon.number.commas(item._count), $.jCommon.number.commas(item._total)));
                                }},
                                {header: { title: "DITPR ID", sortable: true, property: 'ditprId'}, searchable: true, property: 'ditprId', type: 'string', callback: function (td, item, value) {
                                    if(item.edge && item.edge.customized){
                                        td.addClass('enclave-customized');
                                        td.attr('title', "This System authorization has been modified and will not be affected by any update or delete automated processes.");
                                    }
                                    td.append(value);
                                    td.attr('id', 'ditprId-'+item.lid);
                                }},
                                {header: { title: "COAMS ID", property: 'nameId'}, searchable: true, property: 'nameId', type: 'string'},
                                {header: { title: "Enclave", _default: true, sortable: true, property: 'title'}, searchable: true, property: 'title', type: 'string', callback: function (td, item, value) {
                                    if(item.edge && item.edge.customized){
                                        td.addClass('enclave-customized');
                                        td.attr('title', "This System authorization has been modified and will not be affected by any update or delete automated processes.");
                                    }
                                    td.attr('id', 'title-'+item.lid);
                                    td.append(dCrt('a').attr('href', item[state.KEY_ID]).attr('target', '_blank').html(item.title));
                                }}
                            ]
                        }
                    };
                    container.jFilterBar(options);
                    window.setTimeout(function () {
                        if(!state.opts.cav.hasClass('active')) {
                            state.opts.cav.hide();
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
    $.enclaveScoping.defaults = {
        limit: 1000
    };


    //Plugin Function
    $.fn.enclaveScoping = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === 'object') {
            return this.each(function() {
                new $.enclaveScoping($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $organization = $(this).data('enclaveScoping');
            switch (method) {
                case 'exists': return (null!==$organization && undefined!==$organization && $organization.length>0);
                case 'state':
                default: return $organization;
            }
        }
    };

})(jQuery);
