;(function ($) {

    //Object Instance
    $.discovery = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.discovery.defaults, options);
        var _info = 'This search is not a free word search rather a semantic search and selecting one of the suggestions will return a better result.';
        var _started =false;
        var _hits = 0;
        var _next = 0;
        var _no = 0;
        var _total=0;
        var _url = $.jCommon.url.create(window.location.href);
        state.showApprox = false;
        state.KEY_ID = '/vertex/uri';

        // Store a reference to the environment object
        el.data("discovery", state);

        // Private environment methods
        methods = {
            init: function() {
                state.opts.url = $.jCommon.url.create(window.location.href);
                if(state.opts.url.isUrl){
                   pageCover.busy(true);
                    state.css({overflow: 'hidden'});
                    state.opts.phrase = state.opts.url.getParameter("q");
                    state.isVertex = $.jCommon.string.startsWith(state.opts.phrase, "/domains/");
                    state.vertexUrl = state.isVertex ? state.opts.phrase : null;
                    state.node = $.htmlEngine.panel(state, state.opts.glyph, methods.getHeader(), state.vertexUrl, false /* borders */);

                    state.node.addClass('discovery');
                    methods.setLimit();
                    methods.get(state.opts.phrase);
                }
            },
            getHeader: function (data) {
                if(data && (data.groups.length<_next) &&(_hits===_next)){
                    return String.format('Searched on: "{0}"{1}', state.opts.phrase, (state.showApprox ? String.format(": found {0}", _total) : ""));
                }
                return String.format('Searched on: "{0}"{1}', state.opts.phrase, (state.showApprox ? String.format(" : {0} (approximate hits)", hits) : ""));
            },
            setLimit: function () {
                var av = state.availHeight(0);
                var l = Math.floor(Math.round(av/40));
                state.opts.limit = l<50?50:l;
            },
            getNext: function (content, data) {
              if(data._on>=state.opts.limit) {
                  if (!data._next && data.results && data.results.length > 0) {
                      data._next = state.opts.limit;
                  }
                  else {
                      data._next += state.opts.limit;
                  }
                  pageCover.busy(true);
                  var url = String.format('/discover?phrase={0}&start={1}&limit={2}&groupIt=true', state.opts.phrase, data._next, state.opts.limit);
                  var s = function (response) {
                      pageCover.busy(false);
                      if (response && response.groups && response.groups.length > 0) {
                          var item = response.groups[0];
                          item._on = data._on;
                          item._next = data._next;
                          methods.html.rows(content, item);
                      }
                      else{
                          methods.msg(content, "End of results", "end-results");
                      }
                  };
                  var f = data.results[0];
                  var d = [];
                  d.push({domain: f.vertexType});
                  $.htmlEngine.request(url, s, s, d, "post", true);
              }
            },
            msg: function (node, message, cls) {
                if(cls){
                    if(node.hasClass(cls)){
                        return false;
                    }
                    node.addClass(cls);
                }
                var c = dCrt('div');
                node.append(c);
                var hd = dCrt('h5').html(message).addClass('letterpress').css({margin: '20px 10px 0 10px'});
                c.append(hd);
            },
            get: function(phrase) {
                var url = String.format('/discover?phrase={0}&start={1}&limit={2}&groupIt=true', phrase, _next, state.opts.limit);
                pageCover.busy(true);
                var s = function(data){
                    pageCover.busy(false);
                    if(_url.hasParam('json')){
                        $('div.page').remove();
                        var map = _url.hasParam('json') ? $.jCommon.json.sortKeys(data)
                            : $.schemaEngine.createEntityMap(data);
                        var div = $(document.createElement("pre")).append($.jCommon.json.pretty(map));
                        $('body').append(div);
                    }
                    else
                    {
                        if(data.hits){
                            _hits = data.hits;
                        }
                        _next += state.opts.limit;
                        _next = _next>_hits ? _hits : _next;

                        if(data.groups.length>0) {
                            methods.html.init(state.node, data);
                        }
                        else if(_next>0){
                            _hits = _next = (_no);
                        }
                        else {
                            lusidity.info.yellow('No results found for <strong>"' + data.phrase + '"</strong>.<br /><br />This search is not really a free word search rather a symantic search and selecting one of the suggestions will yield better results.');
                            state.hide();
                            lusidity.info.show();
                        }
                        if(state.isVertex){
                            _hits=_next=1;
                        }
                        state.panel('updateHeader', {
                            glyph: state.opts.glyph,
                            title: methods.getHeader(data)
                        });
                    }
                };
                var f = function(jqXHR, textStatus, errorThrown){
                    pageCover.busy(false);
                };
                $.htmlEngine.request(url, s, f, null, "get");
            },
            getQueryUrl: function (start, limit) {
                if (undefined === start) {
                    start = 0;
                }
                if (undefined === limit) {
                    limit = 0;
                }
                return '/query?start=' + start + '&limit=' + limit;
            },
            getQuery: function (item, pItem) {
                return {
                    file_name: $.jCommon.string.replaceAll(String.format("{0}_{1}", pItem.name, item.name), " ", "_"),
                        domain: item.edgeType,
                    type: item.vertexType,
                    lid: item.parentLid,
                    format: 'discovery',
                    direction: item.direction,
                    hits: item.hits,
                    "native": item.query
                };
            },
            tabs:{
                getTitle: function (item) {
                    var n = item.name.toLowerCase();
                    switch (n){
                        case 'stig rule':
                            n = 'STIG Rules';
                            break;
                        case 'acas invalid asset':
                            n = 'Unmatched ACAS Only Asset';
                            break;
                        default:
                            n = item.name;
                            break;
                    }
                    item.name = n;
                },
                add: function(tabs, content, item, selected) {
                    if(!state.names){
                        state.names = [];
                    }

                    if (item.name && !$.jCommon.array.contains(state.names, item.name)) {
                        state.names.push(item.name);
                        var tab = dCrt('div')
                            .addClass('tab link').attr('data-name', item.name).attr('data-total', 1).html(item.name);
                        var found = $(document.createElement('span'));
                        //found.html(' (~' + item.hits + ')');
                       // tab.append(found);
                        tabs.append(tab);

                        var bdy = dCrt('div').css({overflowX: 'hidden', overflowY: 'auto', height: 'inherit'});
                        var h =content.availHeight(4);
                        dHeight(bdy, h, h, h);
                        content.append(bdy);

                        if(selected){
                            tab.addClass('selected');
                            bdy.show();
                        }
                        methods.html.rows(bdy, item);
                        tab.on('click', function () {                             
                            if (!$(this).hasClass('selected')) {
                                tabs.children().removeClass('selected');
                                $(this).addClass('selected');
                                content.children().hide();
                                bdy.show();
                            }
                        });
                    }
                },
                addRelatedTab: function(tabs, item, pItem){
                    if(!tabs.items){
                        tabs.items = [];
                        tabs.results = dCrt('div').addClass('results');
                        tabs.parent().append(tabs.results);
                    }
                    if(item.name){
                        if(!methods.tabs.contains(tabs, item.name)){
                            var tab = dCrt('div')
                                .addClass('tab link').attr('data-name', item.name).attr('data-total', 1).html(item.name);
                            var found =  $(document.createElement('span'));
                            found.html(' (found: ' + item.hits + ')');
                            tab.append(found);
                            tabs.append(tab);
                            tabs.items.push(item.name);
                            if($.jCommon.string.contains(item.name, "asset", true)){
                                $.login.authorized({"groups": ["power user"], "r": false}, function (data) {
                                    if(!data.auth){
                                        return false;
                                    }
                                    var glyph = dCrt('span').addClass('glyphicon glyphicon-download-alt').css({
                                        marginLeft: '0',
                                        fontSize: '12px',
                                        position: 'relative',
                                        top: '1px',
                                        color: '#337ab7',
                                        cursor: 'pointer'
                                    }).attr("data-toggle","tooltip").attr("data-placement", "top").attr("data-container", "body").attr("title", 'Export data to an Excel spreadsheet, do not navigate away from this page until the download has started.');
                                    tabs.append(glyph);
                                    glyph.on('click', function () {
                                        var hst = lusidity.environment('host-delete');
                                        var qry = methods.getQuery(item, pItem);
                                        var s = function (data) {
                                            if (data && data.url) {
                                                var u = hst + data.url;
                                                u = $.jCommon.string.replaceAll(u, "/svc/", "/");
                                                window.location.assign(u);
                                            }
                                        };
                                        $.htmlEngine.request(hst + "/query/export", s, s, qry, 'post');
                                    });
                                });
                            }
                            tab.on('click', function(){
                                if(!$(this).hasClass('selected')) {
                                    pageCover.busy(true);
                                    tabs.children().removeClass('selected');
                                    $(this).addClass('selected');
                                    tabs.results.children().remove();
                                    tabs.results.css({maxHeight: '300px', height: '300px', overflow: 'hidden', marginTop: '5px'});
                                    // item.query.sort = [{"title":{"missing":"_last","ignore_unmapped":true,"order":"asc"}}];
                                    var excluded = 0;
                                    var qry = methods.getQuery(item, pItem);
                                    tabs.results.pGrid(TableFactory.get(item, {qry: qry, maxHeight: 300}));
                                }
                            });
                        }
                    }
                },
                contains: function(tabs, name){
                    var found  = false;
                    $.each(tabs.items, function(){
                        found = $.jCommon.string.equals(this, name, true);
                        return !found;
                    });
                    return found;
                }
            },
            html:{
                init: function (node, data) {
                    var tabs = dCrt('div');
                    var content = dCrt('div');
                    function size() {
                        var h = state.node.availHeight();
                        state.node.css({overflow: 'hidden'});
                        dHeight(state.node, h, h, h);
                        if(content.children().length>0) {
                            h = content.availHeight(4);
                            dHeight(content.children(), h, h, h);
                        }
                    }
                    size();
                    $(window).on('resize', function () {
                       size();
                    });
                    state.node.append(tabs).append(content);
                    methods.html.create(tabs, content, data);
                },
                create: function(tabs, content, data){
                    if(data.groups && data.groups.length>0) {
                        var on = 0;
                        $.each(data.groups, function(){
                            var item = this;
                            if(item.hits>0 && item.name) {
                                methods.tabs.getTitle(item);
                            }
                        });
                        var grps = $.jCommon.array.sort(data.groups, [{property: 'name', asc: true}]);
                        $.each(grps, function(){
                            var item = this;
                            if(item.hits>0 && item.name) {
                                methods.tabs.add(tabs, content, item, on === 0);
                                on++;
                            }
                        });
                    }
                    else{
                        lusidity.info.yellow('No results found for <strong>"' + data.phrase + _info);
                        state.hide();
                        lusidity.info.show();
                    }
                },
                rows: function (content, data) {
                    function make(r) {
                        if (r && r.results) {
                            var leafs = [];
                            if(!data._on){
                                data._on = 0;
                            }
                            $.each(r.results, function () {
                                var item = this;
                                _total++;
                                var row = dCrt('div').attr('data-name', item.name).css({padding: '5px'});
                                var title = dCrt('div').css({display: 'inline-block'}).append(dCrt('span').append(String.format('{0}. ', (data._on+1))));
                                if(item.vertexType){
                                    var ic = dCrt('span').semanticType({data: item, imageOnly: true, image: true, 'float': '', display: 'inline', padding: '0px 5px 0px 0px'});
                                    title.append(ic);
                                }
                                var t = item.title;
                                var sb;
                                if($.jCommon.string.contains(t, ":[")){
                                    t = $.jCommon.string.stripEnd($.jCommon.string.getLast(t, ":["));
                                    sb = $.jCommon.string.getFirst(": [");
                                }
                                t = FnFactory.toTitleCase(t);
                                title.append(dCrt('span').html(t));

                                if(state.opts.collapsable) {
                                    var leaf = {
                                        title: title,
                                        link: item.uri,
                                        content: row
                                    };
                                    leafs.push(leaf);
                                }
                                else{
                                    var header = $(document.createElement('h5'));
                                    row.append(header);
                                    var link = $(document.createElement('a')).attr('href', item.uri).attr('target', '_blank').html(title);
                                    header.append(link);
                                    row.append(header);
                                    content.append(row);
                                }

                                var vt = (item.name) ? item.name : ((item.vertexType) ? FnFactory.process("fn::propertyToName::vertexType", item) : null);
                                if(vt){
                                    if($.jCommon.string.equals(vt, 'acas invalid asset', true)){
                                        vt = "Unmatched ACAS Only Asset";
                                    }
                                    row.append(dCrt('div').html(vt));
                                }

                                if (item.externalUri) {
                                    var span = dCrt('span').addClass('glyphicons glyphicons-new-window');
                                    var div = dCrt('div').html(item.externalUri).css({marginTop: '5px', display: 'inline-block'});
                                    var ext = dCrt('a').attr('href', item.externalUri).attr('target', "_blank").append(div).append(span);
                                    row.append(ext)
                                }

                                if (!$.jCommon.string.empty(item.description)) {
                                    var desc = dCrt('div').addClass('description');
                                    var p = $(document.createElement('p')).html(item.description);
                                    desc.append(p);
                                    row.append(desc);
                                }

                                if (!$.jCommon.string.empty(item.keyMatchedOn) && !$.jCommon.string.empty(item.valueMatchedOn)) {
                                    var mo = dCrt('div').addClass('description');
                                    var kmo =dCrt('span').append('Matched On: ' + item.keyMatchedOn+':');
                                    var vhmo = $.jCommon.json.hightlightText(item.valueMatchedOn, item.phrase, 'highlight');
                                    var vmo =dCrt('span').css({marginLeft: '5px'}).append(vhmo);
                                    row.append(mo.append(kmo).append(vmo));
                                }

                                if(item.action) {
                                    DiscoveryFactory[item.action.fn](item, row, state.opts.collapsable ? leaf : title, _no);
                                }

                                if(item.related){
                                    var t = dCrt('div').addClass('tabs');
                                    row.append(t);
                                    item.related = $.jCommon.array.sort(item.related, [{property: 'name', asc: true}]);
                                    $.each(item.related, function () {
                                        var related = this;
                                        related.related = true;
                                        related.parentLid = item.uri;
                                        methods.tabs.addRelatedTab(t, related, item);
                                    });
                                }
                                _no++;
                                data._on++;
                            });
                            if(state.opts.collapsable) {
                                content.spring({leafs: leafs});
                            }
                            else{
                                content.scrollHandler({
                                    adjust: 10,
                                    start: function () {
                                    },
                                    stop: function () {
                                    },
                                    top: function () {
                                    },
                                    bottom: function () {
                                        methods.getNext(content, data);
                                    }
                                });
                            }

                            if(r.results.length<state.opts.limit){
                                methods.msg(content, "End of results", "end-results");
                            }
                        }
                    }
                    if (data && data.results) {
                        make(data, false);
                    }
                    else if(data.query) {
                        get(0, state.opts.limit);
                    }

                    function get(start, limit) {
                       pageCover.busy(true);
                        var s = function (response) {
                            pageCover.busy(false);
                            make(response);
                        };
                        var f = function () {
                            pageCover.busy(false);
                        };
                        var q = {
                            domain: data.edgeType,
                            type: data.vertexType,
                            lid: data.parentLid,
                            format: 'discovery',
                            direction: data.direction,
                            totals: true,
                            "native": data.query
                        };
                        var url = methods.getQueryUrl((start ? start: 0), (limit ? limit : 50));
                        $.htmlEngine.request(url, s, f, q, "post");
                    }
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.discovery.defaults = {
        glyph: 'glyphicons glyphicons-database-search',
        limit: 50
    };


    //Plugin Function
    $.fn.discovery = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.discovery($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $discovery = $(this).data('discovery');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $discovery;
            }
        }
    };

})(jQuery);
