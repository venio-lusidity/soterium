

;(function ($) {

    //Object Instance
    $.vertexResolver = function (el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data};
        state.opts = $.extend({}, $.vertexResolver.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.confirm = dCrt('div').css({height: '0', width: '0'});
        state.current = {node: null};
        state.initialData = {};
        state.owner = {};
        state.manager = {};
        // Store a reference to the environment object
        el.data("vertexResolver", state);

        // Private environment methods
        methods = {
            init: function () {
                state.worker.data = $.jCommon.array.sortKey(state.worker.data);
                var s = function (data) {
                    if (data && data.results && data.results.length>0) {
                        //needs this later to update header after cookie creation
                        state.initialData = data;
                        methods.html.init(data);
                        methods.updateHeader(data);
                        methods.updateOrgs(data);
                    }
                    else{
                        state.worker.node.append(dCrt('div').css({lineHeight: '20px', margin: '10px 10px'}).html("No similarities found."));
                    }
                };
                var f = function () {
                    lusidity.info.red('Something went wrong while finding matching assets.');
                    lusidity.info.show(5);
                };
                var url = methods.getUrl(state.worker.data, 0, state.opts.limit);
                $.htmlEngine.request(url, s, f, null, 'get');
            },
            updateOrgs: function (data) {
                if (data && data.origin) {
                    var manager = $.jCommon.json.getProperty(data.origin, 'managedBy', 'string', 0);
                    var owner = $.jCommon.json.getProperty(data.origin, 'ownedBy', 'string', 0);
                    state.manager = manager;
                    state.owner = owner;
                }
            },
            updateHeader: function (data) {
                var cookieCount = 0;
                var cookieJar = JSON.parse($.jCommon.cookie.read('dup_rec'));
                if (null !== cookieJar) {
                    var cookies = cookieJar["duplicates_rec"];
                    cookieCount = cookies.length;
                }
                state.start = data.results.length - cookieCount;
                state.hits = state.start;
                state.worker.node.panel('updateHeader', {
                    glyph: state.opts.glyph,
                    title: String.format("{0} {1}/{2}", state.opts.title, state.start, state.hits)
                });
            },
            onMerge: function (data) {
                var id = $.jCommon.json.getProperty(data, 'lid', 'string', 0);
                var uri = state.worker.data[state.KEY_ID];
                var s = function (data) {
                    if (!data.error) {

                    }
                    else {
                        lusidity.info.red(data.error);
                        lusidity.show(5);
                    }
                };
                var f = function (data) {
                    state.confirm.pageModal('hide');
                    state.pageModal('hide');
                };
                $.htmlEngine.request(uri + "/resolve", s, f, {id: id, merge: true}, 'delete');
            },
            setCookie: function (data) {
                //set for 1 year
                var minutes = 365 * 24 * 60;
                var id = $.jCommon.json.getProperty(data, 'lid', 'string', 0);
                var uri = state.worker.data[state.KEY_ID];
                var dup_rec = {
                    duplicates_rec: [
                        {a: uri, b: id}
                    ]
                };
                var cookie = $.jCommon.cookie.read('dup_rec');
                if (null === cookie) {
                    $.jCommon.cookie.create('dup_rec', JSON.stringify(dup_rec), minutes);
                }
                else {
                    var dups = [];
                    dups.push({a: uri, b: id});
                    var cookieJar = JSON.parse($.jCommon.cookie.read('dup_rec'));
                    var results = $.jCommon.array.sort(cookieJar, {property: "a", asc: true});
                    results = results["duplicates_rec"];
                    $.each(results, function () {
                        var item = this;
                        for (var i = 0; i < dups.length; i++) {
                            if (dups[i].b === item.b) {
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
            getUrl: function (data, start, limit) {
                return data[state.KEY_ID] + '/resolve?start=' + start + '&limit=' + limit;
            },
            createNode: function (type, css, cls) {
                return $(document.createElement(type));
            },
            header: function () {
                var header = dCrt('div');
                var hContent = dCrt('h4');
                hContent.append("Deduplication");
                header.append(hContent);
                return header;
            },
            body: {
                init: function (data) {
                    data = $.jCommon.array.sortKey(data);
                    var inclusions = ['/system/primitives/uri_value/identifiers.value', '/electronic/network/network_adapter/networkAdapters.ipAddress',
                        '/electronic/network/network_adapter/networkAdapters.macAddress', 'hostname', 'title', 'serialNumber', '/organization/organization/ownedBy'];
                    var filters = $.jCommon.json.matches(state.worker.data, data);
                    filters = $.jCommon.json.filter(filters, inclusions);
                    var container = dCrt('div').addClass('container-fluid');
                    var c = dCrt('div').addClass('row');
                    container.append(c);
                    var r = dCrt('div').css({marginTop: '5px'}).css({overflow: 'auto'});
                    var origin = dCrt('h4').html(state.worker.data.title);
                    r.append(origin);
                    var own1 = dCrt('h5').html("Owned By: " + state.owner);
                    r.append(own1);
                    var man1 = dCrt('h5').html("Managed By: " + state.manager);
                    r.append(man1);
                    var firstMap = $.jCommon.json.sortKeys(state.worker.data);
                    var pre1 = dCrt('pre').append($.jCommon.json.pretty(firstMap));
                    r.append(pre1);
                    var d = dCrt('div').css({marginTop: '5px'}).css({overflow: 'auto'});
                    var item = dCrt('h4').html(data.title);
                    d.append(item);
                    var own2 = dCrt('h5').html("Owned By: " + $.jCommon.json.getProperty(data, 'ownedBy', 'string', 0));
                    d.append(own2);
                    var man2 = dCrt('h5').html("Managed By: " + $.jCommon.json.getProperty(data, 'managedBy', 'string', 0));
                    d.append(man2);
                    var map = $.jCommon.json.sortKeys(data);
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
                        $.jCommon.json.hightlight(pre1, filters, 'yellow', 1);
                        $.jCommon.json.hightlight(pre2, filters, 'yellow', 2);
                    }, 500);
                    return container;
                }
            },
            footer: {
                init: function (data) {
                    var mFooter = dCrt('div');
                    var merge = dCrt('button').addClass('btn').html('Merge');
                    merge.on('click', function () {
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
                                var t1 = state.worker.data.title;
                                var t2 = data.title;
                                var msg = dCrt('div').css({
                                    verticalAlign: 'middle',
                                    height: '32px'
                                });
                                var confirmation = dCrt('h5').html('Click to merge FROM "<strong>' + t2 + '</strong>" TO "<strong>' + t1 + '</strong>".');
                                msg.append(confirmation);
                                body.append(msg);
                            },
                            footer: function () {
                                var lFooter = dCrt('div');
                                var btnBar = dCrt('div').addClass('btn-bar');
                                var mer = dCrt('button').attr('type', 'button')
                                    .addClass('btn btn-danger').html('Merge');
                                btnBar.append(mer);
                                mer.on('click', function () {
                                    lusidity.loaders('show');
                                    methods.onMerge(data);
                                    state.confirm.pageModal('hide');
                                    state.pageModal('hide');
                                    state.current.node.hide();
                                    //methods.init();
                                    window.location = window.location;
                                    lusidity.loaders('hide')

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
                    mFooter.append(merge);
                    var notDup = dCrt('button').addClass('btn').html('Not Duplicate');
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
                                var t1 = state.worker.data.title;
                                var t2 = data.title;
                                var msg = dCrt('div').css({
                                    verticalAlign: 'middle',
                                    height: '32px'
                                });
                                var confirmation = dCrt('h5').html('You have determined that asset "<strong>' + t2 +
                                    '</strong>" is not a match and should not be displayed. Is that correct?"');
                                msg.append(confirmation);
                                body.append(msg);
                            },
                            footer: function () {
                                var lFooter = dCrt('div');
                                var btnBar = dCrt('div').addClass('btn-bar');
                                var ok = dCrt('button').attr('type', 'button')
                                    .addClass('btn btn-danger').html('Ok');
                                btnBar.append(ok);
                                ok.on('click', function () {
                                    lusidity.loaders('show');
                                    methods.setCookie(data);
                                    lusidity.loaders('hide');
                                    state.current.node.hide();
                                    methods.updateHeader(state.initialData);
                                    state.confirm.pageModal('hide');
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
                    var fClose = dCrt('button').addClass('btn').html('Close');
                    mFooter.append(fClose);

                    fClose.on('click', function () {
                        state.pageModal('hide');
                    });

                    return mFooter;
                }
            },
            html: {
                init: function (data) {
                    state.body = $.htmlEngine.panel(
                        state.worker.node, state.opts.glyph, state.opts.title, null, false /* borders */
                    );
                    //get the cookies and don't show results for those items
                    var cookieJar = JSON.parse($.jCommon.cookie.read('dup_rec'));
                    var results = data.results;
                    results = $.jCommon.array.sort(results, [{property: "title", asc: true}]);
                    $.each(results, function () {
                        var item = this;
                        var id = $.jCommon.json.getProperty(item, 'lid', 'string', 0);
                        var hasCookie = false;
                        if (null !== cookieJar) {
                            var cookies = $.jCommon.array.sort(cookieJar, {property: "a", asc: true});
                            cookies = cookies["duplicates_rec"];
                            $.each(cookies, function () {
                                for (var i = 0; i < cookies.length; i++) {
                                    if (cookies[i].b === id) {
                                        hasCookie = true;
                                        break;
                                    }
                                }
                            });
                        }

                        if (!hasCookie) {
                            var node = $(document.createElement('div')).css({cursor: 'pointer'});
                            state.body.append(node);
                            var t = dCrt('div').css({display: 'table-cell'}).html(item.title);
                            var i = dCrt('div').css({display: 'table-cell', 'margin-left': '5px'});
                            var s = dCrt('span').addClass('glyphicons glyphicons-git-compare');
                            node.append(i.append(s)).append(t);
                            node.on('click', function () {
                                state.current.node = node;
                                var h = ($(window).height() - 20);
                                var w = ($(window).width() - 40);
                                state.pageModal();
                                state.pageModal('show', {
                                    glyph: 'glyphicons glyphicons-duplicate',
                                    width: w,
                                    height: h,
                                    header: methods.header(),
                                    footer: methods.footer.init(item),
                                    body: methods.body.init(item),
                                    hasClose: true
                                });
                            });
                        }

                    });
                }
            }

        };
        //public methods

        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.vertexResolver.defaults = {
        limit: 10
    };

    //Plugin Function
    $.fn.vertexResolver = function (method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function () {
                new $.vertexResolver($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $vertexResolver = $(this).data('vertexResolver');
            switch (method) {
                case 'exists':
                    return (null !== $vertexResolver && undefined !== $vertexResolver && $vertexResolver.length > 0);
                case 'state':
                default:
                    return $vertexResolver;
            }
        }
    };

    $.vertexResolver.call = function (elem, options) {
        elem.vertexResolver(options);
    };

    try {
        $.htmlEngine.plugins.register("vertexResolver", $.vertexResolver.call);
    }
    catch (e) {
        console.log(e);
    }

})(jQuery);
