
;(function ($) {

    //Object Instance
    $.assetMatches = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.assetMatches.defaults, options);
        state.url = $.jCommon.url.create(window.location.href);
        state.current={
            start: 0,
            limit: 20,
            id: null,
            selected: null,
            node: null,
            subSelected: null,
            artifactUri: {}
        };
        state.KEY_ID = '/vertex/uri';
        state.confirm = dCrt('div').css({height: '0', width: '0'});
        var leftNode = $('.panel-left');
        var contentNode = $('.panel-middle');
        var menuBar = $('.menu-bar');
        var _mos = ['bios_guid','serial_number','hbss_record_id','fqdn','mac_address','mcafee_guid'];
        // Store a reference to the environment object
        el.data("assetMatches", state);

        // Private environment methods
        methods = {
            init: function() {
                $.htmlEngine.busy(state, {type: 'cube', cover: true});
                var s = function (data) {
                    if(data && data.results && data.results.length>0){
                        state.current.data = data;
                        methods.content.msg(state.opts.pnlMiddleNode, "Select an asset on the left.");
                        methods.content.init(data);
                    }
                    else{
                        methods.content.msg(state.opts.pnlMiddleNode, "No results found.");
                    }
                    state.loaders('hide');
                };
                var f= function () {
                    state.loaders('hide');
                };
               $.htmlEngine.request('/asset/matches?start=0&limit=100', s, f, null, 'get');
            },
            updatePanel: function (data) {
                state.panel = dCrt('div');
                var count = 0;
                state.append(state.panel);
                if(data.results.length > 0){
                    $.each(data.results, function () {
                       var item = this;
                        if(!item.read){
                            count++;
                        }
                    });
                }
                state.body = $.htmlEngine.panel(state.panel, 'glyphicons glyphicons-duplicate', String.format('Matches {0} of {1}', data.results.length, data.hits), null,false,[]);
            },
            header: function () {
                var header = dCrt('div');
                var hContent = dCrt('h4').css({position: 'relative', top: '-8px'});
                hContent.append("Deduplication");
                header.append(hContent);
                return header;
            },
            //cookies not used at this time
            setCookie: function (data) {
                //set for 1 year
                var minutes = 120;
                var id = $.jCommon.json.getProperty(data, 'lid', 'string', 0);
                var dup_rec = {
                    duplicates_rec: [
                        {lid:id}
                    ]
                };
                var cookie = $.jCommon.cookie.read('dup_rec');
                if (null === cookie) {
                    $.jCommon.cookie.create('dup_rec', JSON.stringify(dup_rec), minutes);
                }
                else {
                    var dups = [];
                    dups.push({lid: id});
                    var cookieJar = JSON.parse($.jCommon.cookie.read('dup_rec'));
                    var results = $.jCommon.array.sort(cookieJar, [{property: "lid", asc: true}]);
                    results = results["duplicates_rec"];
                    $.each(results, function () {
                        var item = this;
                        for (var i = 0; i < dups.length; i++) {
                            if (dups[i].lid === item.lid) {
                                return;
                            }
                        }
                        dups.push(item);
                    });
                    $.jCommon.cookie.erase('dup_rec');
                    var newCookie = {duplicates_rec: dups};
                    $.jCommon.cookie.create('dup_rec', JSON.stringify(newCookie), minutes);
                }
                state.pageModal('hide');
            },
            getValues: function (data) {
                var id = $.jCommon.json.getProperty(data, 'lid', 'string', 0);
                var duprec = {
                    duplicates_rec: [
                        {lid: id}
                    ]
                };
                return c + '_loggedIn';
            },
            body: {
                init: function (left, right) {
                    if(left && right){
                        var inclusions = ['/system/primitives/uri_value/identifiers.value', '/electronic/network/network_adapter/networkAdapters.ipAddress',
                            '/electronic/network/network_adapter/networkAdapters.macAddress','/system/primitives/uri_value/volatileIdentifiers.value', 'hostname', 'title', 'serialNumber', '/organization/organization/ownedBy'];
                        var filters = $.jCommon.json.matches(left, right);
                        filters = $.jCommon.json.filter(filters, inclusions);
                        var container = dCrt('div').addClass('container-fluid');
                        var c = dCrt('div').addClass('row');
                        container.append(c);
                        var r = dCrt('div').css({marginTop: '5px'}).css({overflow: 'auto'});
                        var origin = dCrt('h4').html('Left Asset:&nbsp;').css({display: 'inline-block'});
                        var oLink = dLink(left.title, '/domains/electronic_network_asset/' + left.lid).css({color: '#3b3b3b'});
                        var oWrap = dCrt('h4').html(oLink).css({display: 'inline-block'});
                        r.append(origin).append(oWrap);
                        var firstMap = $.jCommon.json.sortKeys(left);
                        var pre1 = dCrt('pre').append($.jCommon.json.pretty(firstMap));
                        r.append(pre1);
                        var d = dCrt('div').css({marginTop: '5px'}).css({overflow: 'auto'});
                        var compareTo = dCrt('h4').html('Right Asset:&nbsp;').css({display: 'inline-block'});
                        var cLink = dLink(right.title, '/domains/electronic_network_asset/' + right.lid).css({color: '#3b3b3b'});
                        var cWrap = dCrt('h4').html(cLink).css({display: 'inline-block'});
                        d.append(compareTo).append(cWrap);
                        var map = $.jCommon.json.sortKeys(right);
                        var pre2 = dCrt('pre').append($.jCommon.json.pretty(map));
                        d.append(pre2);

                        var p1 = dCrt('div').css({overflow: 'hidden'}).addClass('col-md-6 panel-border').append(r);
                        var p2 = dCrt('div').css({overflow: 'hidden'}).addClass('col-md-6 panel-border').append(d);
                        c.append(p1).append(p2);
                        window.setTimeout(function () {
                            var h = ($(window).height() - 20);
                            h -= (89 + 65) + 65;
                            p1.css({maxHeight: h + 'px', height: h + 'px'});
                            p2.css({maxHeight: h + 'px', height: h + 'px'});
                            r.css({maxHeight: (h - 5) + 'px', height: (h - 5) + 'px'});
                            d.css({maxHeight: (h - 5) + 'px', height: (h - 5) + 'px'});
                            var exclusions = ['lid://hbss_assets_importer','lid://acas_api_importer','lid://sccm_importer'];
                            $.jCommon.json.hightlight(pre1, filters, 'highlight', 1, exclusions);
                            $.jCommon.json.hightlight(pre2, filters, 'highlight', 2, exclusions);
                        }, 500);
                        return container;
                    }

                }
            },
            footer: {
                init: function (left, right) {
                    if(left && right){
                        var mFooter = dCrt('div');

                        var mrgl = dCrt('button').addClass('btn').html('Merge Left');
                        mrgl.on('click', function () {
                            state.confirm.pageModal();
                            state.confirm.pageModal('show', {
                                glyph: 'glyphicon-warning-sign',
                                hasClose: true,
                                header: function () {
                                    var header = dCrt('div');
                                    var hContent = dCrt('h4').html("Merge confirmation required.");
                                    header.append(hContent);
                                    return header;
                                },
                                body: function (body) {
                                    body.children().remove();
                                    var msg = dCrt('div').css({
                                        verticalAlign: 'middle',
                                        height: '32px'
                                    });
                                    var confirmation = dCrt('h5').html('Click to merge "<strong>' + left.title + '&nbsp;(Left Asset)</strong>" with "<strong>' + right.title + '&nbsp;(Right Asset)</strong>".<br /><br /> <strong>Note:</strong> The left Asset always wins.');
                                    msg.append(confirmation);
                                    body.append(msg);
                                },
                                footer: function () {
                                    var lFooter = dCrt('div');
                                    var btnBar = dCrt('div').addClass('btn-bar');
                                    var mer = dCrt('button').attr('type', 'button')
                                        .addClass('btn btn-danger').html('Merge');
                                    btnBar.append(mer);
                                    mer.on('click', function (e) {
                                        e.preventDefault();
                                        right.ignored = true;
                                        methods.onMerge(left, right, true);
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
                        mFooter.append(mrgl);

                        var mrgr = dCrt('button').addClass('btn').html('Merge Right');
                        mrgr.on('click', function () {
                            state.confirm.pageModal();
                            state.confirm.pageModal('show', {
                                glyph: 'glyphicon-warning-sign',
                                hasClose: true,
                                header: function () {
                                    var header = dCrt('div');
                                    var hContent = dCrt('h4').html("Merge confirmation required.");
                                    header.append(hContent);
                                    return header;
                                },
                                body: function (body) {
                                    body.children().remove();
                                    var msg = dCrt('div').css({
                                        verticalAlign: 'middle',
                                        height: '32px'
                                    });
                                    var confirmation = dCrt('h5').html('Click to merge "<strong>' + right.title + '&nbsp;(Right Asset)</strong>" with "<strong>' + left.title + '&nbsp;(Left Asset)</strong>".<br /><br /> <strong>Note:</strong> The right Asset always wins.');
                                    msg.append(confirmation);
                                    body.append(msg);
                                },
                                footer: function () {
                                    var lFooter = dCrt('div');
                                    var btnBar = dCrt('div').addClass('btn-bar');
                                    var mer = dCrt('button').attr('type', 'button')
                                        .addClass('btn btn-danger').html('Merge');
                                    btnBar.append(mer);
                                    mer.on('click', function (e) {
                                        e.preventDefault();
                                        right.ignored = true;
                                        methods.onMerge(left, right, false);
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
                        mFooter.append(mrgr);


                        var notDup = dCrt('button').addClass('btn').html('Ignore');
                        mFooter.append(notDup);
                        notDup.on('click', function () {
                            state.confirm.pageModal();
                            state.confirm.pageModal('show', {
                                glyph: 'glyphicon-warning-sign',
                                hasClose: true,
                                header: function () {
                                    var header = dCrt('div');
                                    var hContent = dCrt('h4').html("Not A Duplicate Asset.");
                                    header.append(hContent);
                                    return header;
                                },
                                body: function (body) {
                                    body.children().remove();
                                    var t1 = left.title;
                                    var t2 = right.title;
                                    var msg = dCrt('div').css({
                                        verticalAlign: 'middle',
                                        height: '32px'
                                    });
                                    var confirmation = dCrt('h5').html('You have determined that asset "<strong>' + t2 +
                                        '&nbsp;(Right Asset)</strong>" is not a match and should not be displayed. Is that correct?"');
                                    msg.append(confirmation);
                                    body.append(msg);
                                },
                                footer: function () {
                                    var lFooter = dCrt('div');
                                    var btnBar = dCrt('div').addClass('btn-bar');
                                    var ok = dCrt('button').attr('type', 'button')
                                        .addClass('btn btn-danger').html('Ok');
                                    btnBar.append(ok);
                                    ok.on('click', function (e) {
                                        e.preventDefault();
                                        right.ignored = true;
                                        methods.onIgnore(left, right);
                                        state.current.subSelected.hide();
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
                        var fClose = dCrt('button').addClass('btn').html('Cancel');
                        mFooter.append(fClose);

                        fClose.on('click', function () {
                            state.pageModal('hide');
                        });
                        var space = dCrt('span').html('&nbsp;&nbsp;&nbsp;&nbsp;');
                        mFooter.append(space);

                        var db = dCrt('button').addClass('btn').html('Delete Both').css({color: 'red'});
                        db.on('click', function () {
                            state.confirm.pageModal();
                            state.confirm.pageModal('show', {
                                glyph: 'glyphicon-warning-sign',
                                hasClose: true,
                                header: function () {
                                    var header = dCrt('div');
                                    var hContent = dCrt('h4').html("Delete confirmation required.");
                                    header.append(hContent);
                                    return header;
                                },
                                body: function (body) {
                                    body.children().remove();
                                    var t1 = left.title;
                                    var msg = dCrt('div').css({
                                        verticalAlign: 'middle',
                                        height: '32px'
                                    });
                                    var confirmation = dCrt('h5').html('Click to permanently DELETE <strong>' + left.title + '&nbsp;(Left Asset)</strong> and <strong>' + right.title + '&nbsp;(Right Asset)</strong>.');
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
                                        left.ignored = true;
                                        methods.remove.right(left, right, true);
                                        right.deleted = true;
                                        methods.remove.left(left, right, true);
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
                        mFooter.prepend(db);

                        var dr = dCrt('button').addClass('btn').html('Delete').css({color: 'red'});
                        dr.on('click', function () {
                            state.confirm.pageModal();
                            state.confirm.pageModal('show', {
                                glyph: 'glyphicon-warning-sign',
                                hasClose: true,
                                header: function () {
                                    var header = dCrt('div');
                                    var hContent = dCrt('h4').html("Delete confirmation required.");
                                    header.append(hContent);
                                    return header;
                                },
                                body: function (body) {
                                    body.children().remove();
                                    var t1 = left.title;
                                    var t2 = right.title;
                                    var msg = dCrt('div').css({
                                        verticalAlign: 'middle',
                                        height: '32px'
                                    });
                                    var confirmation = dCrt('h5').html('Click to permanently DELETE "<strong>' + t2 + '&nbsp;(Right Asset)</strong>."');
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
                                        right.ignored = true;
                                        methods.remove.right(left, right, true);
                                        state.current.subSelected.hide();
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
                        mFooter.append(dr);

                        var dl = dCrt('button').addClass('btn').html('Delete').css({color: 'red', "float": 'left'});
                        dl.on('click', function () {
                            state.confirm.pageModal();
                            state.confirm.pageModal('show', {
                                glyph: 'glyphicon-warning-sign',
                                hasClose: true,
                                header: function () {
                                    var header = dCrt('div');
                                    var hContent = dCrt('h4').html("Delete confirmation required.");
                                    header.append(hContent);
                                    return header;
                                },
                                body: function (body) {
                                    body.children().remove();
                                    var t1 = left.title;
                                    var msg = dCrt('div').css({
                                        verticalAlign: 'middle',
                                        height: '32px'
                                    });
                                    var confirmation = dCrt('h5').html('Click to permanently DELETE "<strong>' + t1 + '&nbsp;(Left Asset)</strong>."');
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
                                        left.ignored = true;
                                        methods.remove.left(left, right, true);
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
                        mFooter.append(dl);

                        return mFooter;
                    }
                }
            },
            onMerge: function (left, right, isParent) {
                var s = function (data) {
                    methods.onIgnore(left, right);
                    if(isParent){
                        var l = $('li[data-uri="' + left[state.KEY_ID] + '"]');
                        if(l){
                            l.remove();
                        }
                    }
                    methods.hide();
                };
                var f = function (data) {
                    methods.hide();
                };
                var l = left[state.KEY_ID];
                var r = right[state.KEY_ID];
                var url = String.format('{0}/merge{1}', (isParent ? l : r), (isParent ? r: l));
                $.htmlEngine.request(url, s, f, null, 'get');
            },
            hide: function () {
                try {
                    state.confirm.pageModal('hide');
                }catch(e){}
                try {
                    state.pageModal('hide');
                }catch (e){}
            },
            remove: {
                del :function (uri, success, failed) {
                    var s = function (data) {
                        if($.isFunction(success)){
                            success(uri, data);
                        }
                        methods.hide();
                    };
                    var f = function () {
                        if($.isFunction(failed)){
                            failed(uri);
                        }
                        methods.hide();
                    };
                    $.htmlEngine.request(uri , s, f, null, 'delete');
                },
                left: function(left, right, delAsset, callback) {
                    var s = function (uri, data) {
                        var temp = [];
                        state.current.data.hits-=1;
                        $.each(state.current.data.results, function () {
                            var c = this[state.KEY_ID];
                            if(!$.jCommon.string.equals(c, uri)){
                                temp.push(this);
                            }
                        });
                        state.current.data.results = temp;
                        state.opts.pnlMiddleNode.children().remove();
                        state.leftBody.children().remove();
                        methods.content.msg(state.opts.pnlMiddleNode, "Select an asset on the left.");
                        methods.content.make();
                        if($.isFunction(callback)){
                            callback(left, right, delAsset);
                        }
                    };
                    var f = function (uri) {};
                    // delete the question.
                    methods.remove.del(state.current.item[state.KEY_ID], s, f);
                    if(delAsset) {
                        // delete the asset.
                        methods.remove.del(left[state.KEY_ID], function () {
                        }, function () {
                        });
                    }
                },
                right: function (left, right, delAsset, callback) {
                    var s = function (uri, data) {
                        state.current.item.ext_matches-=1;
                        var l = $('li[data-uri="' + uri + '"]');
                        if(l){
                            l.remove();
                        }
                        if($.isFunction(callback)){
                            callback(left, right, delAsset);
                        }
                        if (state.current.node && state.current.node.mLink) {
                            state.current.node.mLink.html(String.format('{0}: {1} possible matches', state.current.item.title, state.current.item.ext_matches));
                        }
                        if (state.current.item.ext_matches <= 0) {
                            right.deleted = true;
                            methods.remove.left(left, right, false);
                        }
                        else {
                            methods.content.details.init(state.current.node, state.current.item);
                        }
                    };
                    var f = function (uri) {};
                    // delete the link between the question and the match.
                    methods.remove.del(right.ext_qam[state.KEY_ID], s, f);
                    if(delAsset) {
                        /// delete the asset
                        methods.remove.del(right[state.KEY_ID], function () {
                        }, function () {
                        });
                    }
                }
            },
            onIgnore: function (left, right) {
                methods.remove.right(left, right, false);
            },
            filterContains: function (items) {
                var r = false;
                $.each(_mos, function () {
                   r = $.jCommon.array.contains(items, this.toString());
                   if(r){
                       return false;
                   }
                });
                return r;
            },
            invalid: function (item) {
                methods.content.msg(state.opts.pnlMiddleNode, "The asset selected is no longer valid for deduplication and will be ignored and removed from the list.");
                $.htmlEngine.busy(state, {type: 'cube', cover: true});
                window.setTimeout(function () {
                    methods.remove.left(item, {}, false);
                    state.loaders('hide');
                }, 3000);
            },
            content: {
                make: function () {
                    methods.content.list(state.leftBody, state.current.data, 'clickable', function (node, item) {
                        node.parent().children().removeClass('selected');
                        node.addClass('selected');
                        state.current.item = item;
                        state.current.node = node;
                        methods.content.details.init(node, item);
                    });
                },
                list: function (node, data, cls, onClick) {
                    var grp = dCrt('div').addClass('list-group no-radius');
                    node.append(grp);
                    var ttl = 0;
                    var lft = true;
                    if(!data || !data.results){
                        return false;
                    }
                    data.results = $.jCommon.array.sort(data.results, [{property: 'title', asc: true}]);
                    $.each(data.results, function () {
                        var i = this;
                        if(i.ext_matches){
                            if(i.ext_matches<=0 || !i.matchedOns || i.matchedOns.length<=0) {
                                return true;
                            }
                            if(!methods.filterContains(i.matchedOns)){
                                return true;
                            }
                        }

                        if(i.ext_qam && i.ext_qam.matchedOns && !methods.filterContains(i.ext_qam.matchedOns)) {
                            return true;
                        }

                        var l = dCrt('li').addClass('list-group-item no-radius').css({cursor: 'pointer'});
                        grp.append(l);
                        if(cls){
                            l.addClass(cls);
                        }

                        var t = dCrt('div');
                        if(i.os) {
                            var d = dCrt('div').css({display: 'inline-block', width: '20px', height: '20px', marginRight: '5px'});
                            t.append(d);
                         d.osIcon({
                                display: "inline",
                                fontSize: '16px',
                                hasTitle: false,
                                hasVersion: false,
                                linked: true,
                                data: {
                                    '/properties/technology/software/operatingSystem': {
                                        title: i.os,
                                        '/vertex/uri': i.osId
                                    }
                                }
                            });
                        }
                        if(i.ext_matches){
                            l.attr("data-uri", i.artifactUri);
                            l.mLink = dLink(String.format('{0}: {1} possible {2}', i.title, i.ext_matches, (i.ext_matches>1 ? 'matches' : 'match')), i.artifactUri).css({color: "#3b3b3b"});
                            var d1 = dCrt('div').css({display: 'inline-block'});
                            t.append(d1).append(l.mLink);
                        }
                        else{
                            l.attr("data-uri", i[state.KEY_ID]);
                            var vLink = dLink(i.title, i[state.KEY_ID]).css({color: "#3b3b3b"});
                            t.append(dCrt('div').css({display: 'inline-block'})).append(vLink);
                        }
                        if(i.ext_qam && i.ext_qam.matchedOns){
                            lft = false;
                            var sp = dCrt('div').addClass('separator').css({display: 'inline-block'});
                            t.append(dCrt('span').css({marginLeft: '5px'}).append(dCrt("strong").html("simple matches:")).append(sp));
                            $.each(i.ext_qam.matchedOns, function () {
                                var v = $.jCommon.string.replaceAll(this, "_", " ");
                                sp.append(dCrt('span').css({marginLeft: '5px'}).html(v));
                            })
                        }
                        l.append(t.css({'display': 'inline'}));
                        if($.isFunction(onClick)) {
                            l.on('click', function () {
                                state.current.subSelected = l;
                                onClick(l, i);
                            });
                        }
                        ttl++;
                    });
                    if(lft){
                        var sp = dCrt('span').html(String.format('Assets found {0} out of {1}', ttl, data.hits)).css({position:'relative', top: '12px'});
                        state.opts.pnlLeftNode.panel("updateHeader", {title: sp});
                    }
                    else{
                        var sp = dCrt('span').html(String.format('Matches found {0} out of {1}', ttl, data.hits)).css({position:'relative', top: '12px'});
                        state.opts.pnlMiddleNode.panel("updateHeader", {title: sp});
                    }
                },
                init: function (data) {
                    var sp = dCrt('span').html(String.format('Assets {0} of {1}', data.results.length, data.hits)).css({position:'relative', top: '12px'});
                    function tgl(glyph, mo) {
                        if(glyph.hasClass("glyphicon-ok")){
                            glyph.removeClass("glyphicon-ok").addClass("glyphicon-ban-circle");
                            _mos = $.jCommon.array.removeValue(_mos, mo);
                        }
                        else{
                            glyph.removeClass("glyphicon-ban-circle").addClass("glyphicon-ok");
                            _mos.push(mo);
                        }
                        state.opts.pnlMiddleNode.children().remove();
                        state.leftBody.children().remove();
                        methods.content.msg(state.opts.pnlMiddleNode, "Select an asset on the left.");
                        methods.content.make();
                    }
                    var actions = [
                        {
                            glyph: "glyphicon-filter",
                            title: "Filter",
                            stayOpen: true,
                            "items": [
                                {
                                    glyph: "glyphicon-ok",
                                    title: "BIOS Guid",
                                    clicked: function (node, glyph, title, data) {
                                       tgl(glyph, "bios_guid");
                                    }
                                },
                                {
                                    glyph: "glyphicon-ok",
                                    title: "FQDN",
                                    clicked: function (node, glyph, title, data) {
                                        tgl(glyph, "fqdn");
                                    }
                                },
                                {
                                    glyph: "glyphicon-ok",
                                    title: "HBSS Record Id",
                                    clicked: function (node, glyph, title, data) {
                                        tgl(glyph, "hbss_record_id");
                                    }
                                },
                                {
                                    glyph: "glyphicon-ok",
                                    title: "MAC Address",
                                    clicked: function (node, glyph, title, data) {
                                        tgl(glyph, "mac_address");
                                    }
                                },
                                {
                                    glyph: "glyphicon-ok",
                                    title: "McAfee GUID",
                                    clicked: function (node, glyph, title, data) {
                                        tgl(glyph, "mcafee_guid");
                                    }
                                },
                                {
                                    glyph: "glyphicon-ok",
                                    title: "Serial Number",
                                    clicked: function (node, glyph, title, data) {
                                        tgl(glyph, "serial_number");
                                    }
                                }
                            ]
                        }
                    ];
                    state.leftBody = $.htmlEngine.panel(state.opts.pnlLeftNode, 'glyphicons glyphicons-list', sp, null,false, actions);
                    methods.content.make();
                },
                details: {
                    init: function (node, item) {
                        state.opts.pnlMiddleNode.children().remove();
                        $.htmlEngine.busy(state.opts.pnlMiddleNode, {type: 'cube', cover: true});
                        var s = function (data) {
                            if (data && data.results && data.results.length > 0) {
                                methods.content.details.make(node, item, data);
                            }
                            else {
                                methods.invalid(item);
                            }
                            state.opts.pnlMiddleNode.loaders('hide');
                        };
                        var f = function () {
                            state.opts.pnlMiddleNode.loaders('hide');
                        };
                        $.htmlEngine.request(String.format('{0}/matches/details', item[state.KEY_ID]), s, f, null, 'get');
                    },
                    make: function (node, item, data) {
                        state.opts.pnlMiddleNode.children().remove();
                        var sp = dCrt('span').html(String.format('Matches {0} of {1}', data.results.length, data.hits)).css({position:'relative', top: '12px'});
                        var body = $.htmlEngine.panel(state.opts.pnlMiddleNode, 'glyphicons glyphicons-duplicate', sp, null,false,[]);
                        methods.content.list(body, data, null, function (node, item) {
                            methods.content.details.onClick(node, item);
                        });
                    },
                    onClick: function (node, item) {
                        node.parent().children().removeClass('selected');
                        node.addClass('selected');
                        if(state.current.item.artifactUri){
                            state.current.artifactUri = state.current.item.artifactUri;
                            var s = function(pData) {
                                if(pData) {
                                    state.current.match = item;
                                    state.current.matchNode = node;
                                    var h = ($(window).height() - 20);
                                    var w = ($(window).width() - 40);
                                    state.pageModal();
                                    state.pageModal('show', {
                                        glyph: 'glyphicons glyphicons-duplicate',
                                        width: w,
                                        height: h,
                                        header: methods.header(),
                                        body: methods.body.init(pData, item),
                                        footer: methods.footer.init(pData, item),
                                        hasClose: true
                                    });
                                }
                                else{
                                    methods.invalid(item);
                                }
                            };
                            var f = function(){};
                            $.htmlEngine.request(state.current.item.artifactUri, s, f, null, 'get');
                        }
                    }
                },
                msg: function (node, message) {
                    node.children().remove();
                    var c = dCrt('div');
                    node.append(c);
                    var hd = dCrt('h4').html(message).addClass('letterpress').css({margin: '20px 10px 0 10px'});
                    c.append(hd);
                }
            }
        };
        //public methods
        // Initialize
        methods.init();
    };

    //Default Settings
    $.assetMatches.defaults = {
        css: {minHeight: '50px', height: '50px'},
        delay: 60000
    };


    //Plugin Function
    $.fn.assetMatches = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.assetMatches($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $assetMatches = $(this).data('assetMatches');
            switch (method) {
                case 'exists': return (null!==$assetMatches && undefined!==$assetMatches && $assetMatches.length>0);
                case 'state':
                default: return $assetMatches;
            }
        }
    };

})(jQuery);
