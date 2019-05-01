

;(function ($) {

    //Object Instance
    $.jData = function (el, options) {
        var state = $(el),
            methods = {};
        state.container = $(el);
        state.opts = $.extend({}, $.jData.defaults, options);
        state.opts.host = $.jCommon.url.create(window.location.href);
        // Store a reference to the environment object
        el.data("athena", state);
        state.current = {};
        var servers = lusidity.environment('hosts');

        // Private environment methods
        methods = {
            init: function () {
                $.each(servers, function () {
                    var svr = this;
                    methods.create(svr);
                });
                state.opts.pnlTabNodes.children().on('click', function (e) {
                    e.preventDefault();
                    state.opts.pnlTabNodes.children().removeClass('selected');
                    $(this).addClass('selected');
                    state.current.node.hide();
                    var node = $('#' + $(this).attr('tab-id'));
                    node.show();
                    var svr = node.data('svr');
                    methods.adjustSize(node, svr);
                });
            },
            create: function (svr) {
                var id = $.jCommon.getRandomId('pane');
                var tab = $(document.createElement("div")).attr('tab-id', id).addClass('tab').html(svr.title);
                var node = $(document.createElement('div'))
                    .attr('id', id)
                    .addClass('tab-panel').hide();
                if (!state.current.node) {
                    state.current.node = node;
                }
                if (state.opts.pnlTabNodes.isEmpty()) {
                    tab.addClass('selected');
                    node.show();
                }
                state.opts.pnlTabNodes.append(tab);
                state.opts.pnlTabContent.append(node);
                var row = $(document.createElement('div')).addClass('row content');
                var lc = $(document.createElement('div')).addClass('col-md-3');
                var mc = $(document.createElement('div')).addClass('col-md-6');
                var rc = $(document.createElement('div')).addClass('col-md-3');
                node.append(row.append(lc).append(mc).append(rc));
                svr.l = $(document.createElement('div')).addClass('page-panel panel-left');
                lc.append(svr.l);
                svr.m = $(document.createElement('div')).addClass('page-panel panel-middle');
                mc.append(svr.m);
                svr.r = $(document.createElement('div')).addClass('page-panel panel-right');
                rc.append(svr.r);
                svr.n = node;
                svr.t = tab;
                svr.row = row;
                svr.l.show();
                svr.m.show();
                svr.r.show();

                node.data('svr', svr);
                methods.adjustSize(node, svr);

                if (svr.url) {
                    function info() {
                        var s = function (data) {
                            methods.html.info.init(svr, data);
                        };
                        var f = function () {
                            window.setTimeout(function () {
                                info();
                            }, 100);
                        };
                        $.htmlEngine.request(svr.url + '/admin/sysinfo', s, f, "get");
                    }

                    info();
                    function performance() {
                        var s = function (data) {
                            methods.html.performance.init(svr, data);
                            methods.html.cache.init(svr, data);
                            if (state.opts.refresh) {
                                window.setTimeout(function () {
                                    performance();
                                }, state.opts.interval);
                            }
                        };
                        var f = function () {
                            if (state.opts.refresh) {
                                window.setTimeout(function () {
                                    performance();
                                }, state.opts.interval);
                            }
                        };
                        $.htmlEngine.request(svr.url + '/admin/sysinfo', s, f, "get");
                    }

                    performance();
                    if(!$.jCommon.string.equals(state.opts.view, 'none')) {
                        methods.html[state.opts.view].init(svr);
                    }
                }
            },
            adjustSize: function (node, svr) {
                var h = node.availHeight(10);
                var s2 = {maxHeight: h + 'px', height: h + 'px', overflowX: 'none', overflowY: 'auto'};
                var s1 = {maxHeight: h + 'px', height: h + 'px', overflow: 'hidden'};
                svr.n.css(s1);
                svr.row.css(s1);
                svr.l.parent().css(s1);
                svr.m.parent().css(s1);
                svr.r.parent().css(s1);
                svr.l.css(s2);
                svr.m.css(s2);
                svr.r.css(s2);
            },
            getUrl: function (relativePath, start, limit) {
                var url = relativePath;
                var params = '';
                if (start || start >= 0) {
                    params = 'start=' + start;
                }
                if (limit) {
                    params += ((params.length > 0) ? '&' : '') + 'limit=' + limit;
                }
                url += (params.length > 0) ? '?' + params : '';
                return url;
            },
            getTableHead: function (headers, container) {
                var thead = $(document.createElement('thead'));
                var row = $(document.createElement('tr'));
                $.each(headers, function () {
                    var th = $(document.createElement('th')).html(this);
                    if ($.jCommon.string.equals(this, "#")) {
                        th.width(50);
                    }
                    else if ($.jCommon.string.equals(this, "total", true)) {
                        th.width(150);
                    }
                    else if ($.jCommon.string.equals(this, "export", true)) {
                        th.width(42);
                    }
                    else if ($.jCommon.string.equals(this, "health", true)) {
                        th.width(36)
                    }
                    row.append(th);
                });
                thead.append(row);
                container.append(thead);
            },
            getTableBody: function (properties, parent, data, container, on, msg) {
                var tbody = container.find('tbody');
                if (tbody.length < 1) {
                    tbody = $(document.createElement('tbody'));
                }
                $.each(data, function () {
                    var item = this;
                    row = $(document.createElement('tr')).addClass('table-row');
                    $.each(properties, function () {
                        var key = this.toString();
                        var value;
                        var td = $(document.createElement('td')).addClass('css_' + this);
                        if ($.jCommon.string.equals(this, '#')) {
                            on++;
                            value = $(document.createElement('div')).css({maxWidth: "50px", width: "50px"}).html(on);
                            td.width(50);
                        }
                        else if ($.jCommon.string.equals(this, 'health')) {
                            value = $(document.createElement('span')).addClass('badge dark-' + item[key].value).html('&nbsp;');
                            td.width(36);
                        }
                        else if ($.jCommon.string.equals(this, 'export')) {
                            if (parent) {
                                var c1 = dCrt('div').css({fontSize: '24px', cursor: 'pointer', display: 'table-cell'});
                                var span1 = dCrt("span").addClass('filetypes filetypes-xlsx');
                                var img1 = dCrt("img").css({
                                    height: '16px',
                                    width: '16px'
                                }).attr('src', '/assets/img/loading.gif').hide();
                                c1.append(span1).append(img1);
                                td.append(c1);

                                var c2 = dCrt('div').css({
                                    fontSize: '24px',
                                    cursor: 'pointer',
                                    display: 'table-cell',
                                    marginLeft: '5px'
                                });
                                var span2 = dCrt("span").addClass('filetypes filetypes-json');
                                var img2 = dCrt("img").css({
                                    height: '16px',
                                    width: '16px'
                                }).attr('src', '/assets/img/loading.gif').hide();
                                c2.append(span2).append(img2);
                                td.append(c2);

                                var o = {
                                    store: parent.cls,
                                    partition: item.cls,
                                    sort: "title",
                                    max: item.value,
                                    batch: 100000
                                };

                                function handle(c, span, img, cmd, cb) {
                                    span.hide();
                                    img.show();
                                    c.css({cursor: 'wait'});
                                    r = true;
                                    o.command = cmd;
                                    o.callback = function (data) {
                                        img.hide();
                                        span.show();
                                        c.css({cursor: 'pointer'});
                                        cb();
                                    };
                                    if (!c.exportData('exists')) {
                                        c.exportData({schema: {plugin: o, name: 'exportData'}});
                                    }
                                    else {
                                        c.exportData('send', o);
                                    }
                                }

                                var cb1 = function () {
                                    r1 = false;
                                };
                                var r1 = false;
                                c1.on('click', function () {
                                    handle(c1, span1, img1, 'toExcel', cb1);
                                });
                                var cb2 = function () {
                                    r2 = false;
                                };
                                var r2 = false;
                                c2.on('click', function () {
                                    if (!r2) {
                                        r2 = true;
                                        handle(c2, span2, img2, 'toJson', cb2)
                                    }
                                });
                            }
                        }
                        else {
                            value = item[key].value ? item[key].value : item[key];
                            value = $(document.createElement('div')).html(value);
                        }
                        td.append(value);
                        row.append(td);
                    });
                    tbody.append(row);
                });
                container.append(tbody);
            },
            getNode: function (item, style) {
                var node = $(document.createElement('div'));
                if (item) {
                    if (style) {
                        node.css(style);
                    }
                    var v = $.jCommon.is.numeric(item.value) ? $.jCommon.number.commas(item.value) : item.value;
                    if (item.label) {
                        var l = $(document.createElement('span')).css({fontWeight: 'bold'}).append(item.label);
                        var sep = $(document.createElement('span')).append(':').css({marginRight: '5px'});
                        node.append(l).append(sep);
                        v = $(document.createElement('span')).append(v)
                    }
                }
                return node.append(v);
            },
            panel: function (container, glyph, title, url, borders, actions, menu) {
                var result = $(document.createElement('div'));
                var options = {
                    glyph: glyph,
                    title: title,
                    url: url,
                    borders: borders,
                    content: result,
                    body:{
                        css: {padding: 0}
                    },
                    actions: actions ? actions : [],
                    menu: menu
                };
                container.panel(options);
                return result;
            },
            getProgress: function (label, item, min, max, reverse) {
                var load = (item && item.value) ? Math.round(item.value) : 0;
                var perc = load + '%';
                var cls = reverse ? 'process-bar-danger' : 'process-bar-success';
                var color = "#000";
                if (reverse) {
                    if (load >= 41 && load <= 60) {
                        cls = 'process-bar-warning';
                    }
                    else if (load >= 60) {
                        cls = 'process-bar-success';
                    }
                }
                else {
                    if (load >= 75 && load <= 90) {
                        cls = 'process-bar-warning';
                    }
                    else if (load >= 90) {
                        cls = 'process-bar-danger';
                    }
                }
                var node = $(document.createElement('div')).addClass('progress').css({position: 'relative'});
                var txt = $(document.createElement('div')).css({
                    position: 'absolute',
                    right: '5px',
                    color: color,
                    fontWeight: 'bold'
                }).html(perc + ' : ' + label);
                var bar = $(document.createElement('div')).addClass('progress-bar').addClass(cls).attr("role", "progressBar")
                    .attr('aria-valuenow', load).attr('aria-valuemin', min).attr('aria-valuemin', max)
                    .css({width: perc});

                node.append(bar).append(txt);
                return node;
            },
            html: {
                cache: {
                    init: function (svr, data) {
                        var cache = data.cache;
                        if (cache) {
                            var content = $(document.createElement('div')).css({padding: '5px'});
                            svr.r.append(content);
                            var opt = {
                                glyph: 'glyphicons-equalizer',
                                title: "Cache",
                                url: "",
                                borders: false,
                                content: null,
                                actions: [
                                    {
                                        glyph: "glyphicon-cog",
                                        title: "",
                                        "items": [
                                            {
                                                glyph: "glyphicon-refresh",
                                                title: "Reset cache hits",
                                                clicked: function (node, glyph, title, data) {
                                                    methods.html.cache.reset(svr);
                                                }
                                            }
                                        ]
                                    }
                                ]
                            };
                            content.panel(opt);
                            var hits = methods.getProgress("Hits", cache.hit_rate, 0, 100, true);
                            content.panel('add', hits);

                            var misses = methods.getProgress("Misses", cache.miss_rate, 0, 100);
                            content.panel('add', misses);

                            content.panel('add', methods.getNode(cache.hits).css({margin: "-10px 0 0 0"}));
                            content.panel('add', methods.getNode(cache.misses));
                            content.panel('add', methods.getNode(cache.attempts));
                            content.panel('add', methods.getNode(cache.total_cached).css({margin: "0 0 0 0"}));
                        }
                    },
                    reset: function (svr) {
                        var s = function () {
                            lusidity.info.green("The cache was reset.");
                            lusidity.info.show(10);
                        };
                        var cmd = {command: 'cacheReset', params: {}};
                        $.htmlEngine.request(svr.url + '/admin/command', s, null, cmd, "post");
                    }
                },
                datastore: {
                    init: function (svr) {
                        var s = function (data) {
                            methods.html.datastore.create(svr, data);
                            if (state.refresh) {
                                window.setTimeout(function () {
                                    methods.html.datastore.init(svr);
                                }, state.opts.refresh);
                            }
                        };
                        var f = function () {
                            if (state.refresh) {
                                window.setTimeout(function () {
                                    methods.html.datastore.init(svr);
                                }, state.opts.refresh);
                            }
                        };
                        $.htmlEngine.request(svr.url + '/admin/status?role=data', s, f, "get");
                    },
                    create: function (svr, data) {
                        svr.m.children().remove();

                        var total = 0;
                        var tables = 0;

                        var b = $(document.createElement('div'));
                        $.each(data.buckets, function () {
                            var bucket = this;
                            var c = $(document.createElement('div'));
                            b.append(c);
                            var body = methods.panel(c, 'glyphicons glyphicons-table', bucket.title, null, true, null, null);
                            var content = $(document.createElement('table')).addClass('table table-hover');
                            methods.getTableHead(['#', 'Type', 'Total', 'Export'], content);
                            methods.getTableBody(['#', 'label', 'value', 'export'], bucket, bucket.types, content, state.opts.start);
                            body.append(content);
                            total += bucket.count;
                            tables += bucket.types.length;
                        });
                        var t = $.jCommon.number.commas(total);
                        var tbl = $.jCommon.number.commas(tables);
                        var body = methods.panel(svr.m, 'glyphicons glyphicons-datastore', 'Soterium Datastore (Tables: ' + tbl + ' Vertices: ' + t + ')', null, false);
                        body.append(b);
                    }
                },
                indices: {
                    cluster: {
                        init: function (svr, container) {
                            var s = function (data) {
                                try {
                                    methods.html.indices.cluster.create(svr, data, container);
                                } catch (e) {
                                }
                                if (state.refresh) {
                                    window.setTimeout(function () {
                                        methods.html.indices.cluster.init(svr);
                                    }, state.opts.refresh);
                                }
                            };
                            var f = function () {
                                if (state.refresh) {
                                    window.setTimeout(function () {
                                        methods.html.indices.cluster.init(svr);
                                    }, state.opts.refresh);
                                }
                            };
                            var options = {
                                relativePath: "/_cluster/health"
                            };
                            $.htmlEngine.request(svr.url + '/admin/report?role=data', s, f, options, "post");
                        },
                        create: function (svr, data, container) {
                            container.children().remove();
                            var total = 0;
                            var tables = 0;

                            var b = $(document.createElement('div'));

                            var cls;
                            var name = "Soterium Cluster: ";
                            var data = $.jCommon.array.sortKey(data);
                            data.buckets = [];
                            $.each(data, function (key, value) {

                                if ($.jCommon.string.equals(key, "cluster_name")) {
                                    name += value;
                                }
                                else if ($.jCommon.string.equals(key, "status")) {
                                    cls = value;
                                }
                                else {
                                    var label = $.jCommon.string.replaceAll(key, "_", " ");
                                    label = $.jCommon.string.toTitleCase(label);
                                    var bucket = {label: label, value: value};
                                    data.buckets.push(bucket);
                                }
                            });

                            var body = methods.panel(container, 'glyphicons glyphicons-datastore ' + cls, name, null, false);
                            var content = $(document.createElement('ul')).addClass('list-group');

                            $.each(data.buckets, function () {
                                var c = dCrt('li').addClass('list-group-item');
                                var l = dCrt('span').html(this.label + ":").css({marginRight: '5px'});
                                var v = dCrt('span').html(this.value);
                                c.append(l).append(v);
                                content.append(c);
                            });
                            body.append(content);
                        }
                    },
                    init: function (svr) {
                        svr.m.children().remove();
                        var cluster = dCrt('div');
                        svr.m.append(cluster);
                        methods.html.indices.cluster.init(svr, cluster);
                    }
                },
                info: {
                    init: function (svr, data) {
                        var content = $(document.createElement('div')).css({padding: '5px'});
                        content.append(methods.getNode(data.os.name));
                        content.append(methods.getNode(data.os.version));

                        $.each(data.extended, function (key, value) {
                            content.append(methods.getNode(value));
                        });

                        var body = methods.panel(svr.l, 'glyphicons-info-sign', "System Information", null, false);
                        body.append(content);

                        if (data.ds_stats) {
                            var b = methods.panel(svr.l, "glyphicons glyphicons-datastore", "Index Engine", null, false);
                            b.css({padding: '5px'});
                            $.each(data.ds_stats, function (key, value) {
                                b.append(methods.getNode(value))
                            });
                        }
                    }
                },
                logs: {
                    init: function (svr) {
                        var url = methods.getUrl(svr.url + '/admin/logs', state.opts.start, state.opts.limit);
                        var s = function (data) {
                            methods.html.logs.create(svr, data);
                        };
                        var f = function (jqXHR, textStatus, errorThrown) {
                        };
                        $.htmlEngine.request(url, s, f, "get");
                    },
                    create: function (svr, data) {
                        if (data) {
                            if (!svr.logNode) {
                                svr.logNode = $(document.createElement('results')).addClass('results');
                                var body = methods.panel(svr.m, 'glyphicons-blog', "Soterium Logs", null, false);
                                body.append(svr.logNode);
                            }
                            $.each(data, function () {
                                var r = $(document.createElement('div')).addClass('result');
                                var h = $(document.createElement('h4')).html(this.level + ': ' + this.time).css({marginBottom: '5px'});
                                var d = $(document.createElement('div')).addClass('description').html(this.message);
                                r.append(h).append(d);
                                svr.logNode.append(r);
                            });
                            state.opts.start += data.length;

                            svr.m.scrollHandler({
                                adjust: 100,
                                start: function () {
                                },
                                stop: function () {
                                },
                                top: function () {
                                },
                                bottom: function () {
                                    methods.html.logs.init(svr);
                                    svr.m.scrollHandler('start');
                                }
                            });
                        }
                    }
                },
                testLoad: {
                    init: function (svr) {
                        svr.m.children().remove();
                        var c = dCrt('div');
                        svr.m.append(c);
                        c.testLoad({svr: svr});
                    },
                    create: function (svr, data) {
                        if (data) {
                            if (!svr.logNode) {
                                svr.logNode = $(document.createElement('results')).addClass('results');
                                var body = methods.panel(svr.m, 'glyphicon-blogs', "Soterium Logs", null, false);
                                body.append(svr.logNode);
                            }
                            $.each(data, function () {
                                var r = $(document.createElement('div')).addClass('result');
                                var h = $(document.createElement('h4')).html(this.level + ': ' + this.time).css({marginBottom: '5px'});
                                var d = $(document.createElement('div')).addClass('description').html(this.message);
                                r.append(h).append(d);
                                svr.logNode.append(r);
                            });
                            state.opts.start += data.length;

                            svr.m.scrollHandler({
                                adjust: 100,
                                start: function () {
                                },
                                stop: function () {
                                },
                                top: function () {
                                },
                                bottom: function () {
                                    methods.html.logs.init(svr);
                                    svr.m.scrollHandler('start');
                                }
                            });
                        }
                    }
                },
                performance: {
                    init: function (svr, data) {
                        svr.r.children().remove();
                        var content = $(document.createElement('div')).css({padding: '5px'});
                        var cpu = methods.getProgress('CPU', data.processor.load, 0, 100);
                        content.append(cpu);

                        content.append(methods.getNode(data.processor.architecture).css({margin: "-10px 0 10px 0"}));

                        var memory = methods.getProgress("Memory", data.memory.load, 0, 100);
                        content.append(memory);

                        content.append(methods.getNode(data.memory.used).css({margin: "-10px 0 0 0"}));
                        content.append(methods.getNode(data.memory.max).css({margin: "0 0 0 0"}));

                        var body = methods.panel(svr.r, 'glyphicons-server', "Performance", null, false);
                        body.append(content);
                    }
                },
                workers: {
                    init: function (svr) {
                        svr.m.show();
                        svr.m.jobs({url: svr.url});
                    }
                }
            }
        };

        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.jData.defaults = {
        refresh: true,
        interval: 10000,
        limit: 30,
        start: 0,
        view: 'logs'
    };


    //Plugin Function
    $.fn.jData = function (method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function () {
                new $.jData($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $athena = $(this).data('athena');
            switch (method) {
                case 'state':
                default:
                    return $athena;
            }
        }
    };

})(jQuery);
