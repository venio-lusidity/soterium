

;(function ($) {

    //Object Instance
    $.jAthena = function (el, options) {
        var state = $(el),
            methods = {};
        state.container = $(el);
        state.opts = $.extend({}, $.jAthena.defaults, options);
        state.opts.host = $.jCommon.url.create(window.location.href);
        // Store a reference to the environment object
        el.data("athena", state);
        state.current = {};
        var servers = lusidity.environment('hosts');
        var tabs_init = false;

        // Private environment methods
        methods = {
            init: function () {
                state.opts.l = $('.panel-left');
                state.opts.m = $('.panel-middle');
                state.opts.r = $('.panel-right');

                $.each(servers, function () {
                    methods.create(this);
                });

                lusidity.environment("onResize", function (){
                    methods.resize(state.current.svr);
                });
            },
            create: function (svr) {
                svr.l = dCrt('div').addClass('tab-content no-border').hide();
                svr.m = dCrt('div').addClass('tab-content no-border').hide();
                svr.r = dCrt('div').addClass('tab-content no-border').hide();
                state.opts.l.append(svr.l);
                state.opts.m.append(svr.m);
                state.opts.r.append(svr.r);

                var id = $.jCommon.getRandomId('pane');
                var tab = $(document.createElement("div")).attr('tab-id', id).addClass('tab').html(svr.title);
                state.opts.pnlTabNodes.append(tab);
                svr.t = tab;

                var _init = false;
                tab.on('click', function () {
                    $('.tab').removeClass('selected');
                    tab.addClass('selected');
                    $('.tab-content').hide();
                    svr.l.show();
                    svr.m.show();
                    svr.r.show();
                    methods.resize(svr);
                    state.current.svr = svr;
                    if (!_init && svr.url) {
                        function get() {
                            var s = function (data) {
                                methods.html.info.init(svr, data);
                                methods.html.performance.init(svr, data);
                                methods.html.cache.init(svr, data);
                                if (state.opts.refresh) {
                                    window.setTimeout(function () {
                                       // get();
                                    }, state.opts.interval);
                                }
                                methods.html[state.opts.view].init(svr);
                            };
                            var f = function () {
                                if (state.opts.refresh) {
                                    window.setTimeout(function () {
                                       // get();
                                    }, state.opts.interval);
                                }
                            };
                            $.htmlEngine.request(svr.url + '/admin/sysinfo', s, f, "get");
                        }
                        get();
                    }
                });
                if (!tabs_init) {
                    tab.click();
                }
                tabs_init=true;
            },
            resize: function (svr) {
                lusidity.resizePage(-32);
                var h = state.availHeight(32);
                var lnh = h-187;
                if (svr && svr.logNode) {
                    dHeight(svr.logNode, lnh, lnh, lnh);
                }
                if(svr && svr.indexNode){
                    dHeight(svr.indexNode, lnh, lnh, lnh);
                }
                if(svr && svr.nodeDs){
                    dHeight(svr.nodeDs, lnh, lnh, lnh);
                }
                if(svr && svr.nodeJobs){
                    dHeight(svr.nodeJobs, lnh, lnh, lnh);
                }
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
                            value = dCrt('div').css({maxWidth: "50px", width: "50px"}).html(on);
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
                            value = dCrt('div').html(value);
                        }
                        td.append(value);
                        row.append(td);
                    });
                    tbody.append(row);
                });
                container.append(tbody);
            },
            getNode: function (item, style) {
                var node = dCrt('div');
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
                var result = dCrt('div');
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
                var node = dCrt('div').addClass('progress').css({position: 'relative'});
                var txt = dCrt('div').css({
                    position: 'absolute',
                    right: '5px',
                    color: color,
                    fontWeight: 'bold'
                }).html(perc + ' : ' + label);
                var bar = dCrt('div').addClass('progress-bar').addClass(cls).attr("role", "progressBar")
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
                            if(!svr.nodeCache) {
                                svr.nodeCache = dCrt('div').css({padding: '5px'});
                                svr.r.append(svr.nodeCache);
                            }
                            var nc = svr.nodeCache;
                            nc.children().remove();
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
                            nc.panel(opt);
                            var hits = methods.getProgress("Hits", cache.hit_rate, 0, 100, true);
                            nc.panel('add', hits);

                            var misses = methods.getProgress("Misses", cache.miss_rate, 0, 100);
                            nc.panel('add', misses);

                            nc.panel('add', methods.getNode(cache.hits).css({margin: "-10px 0 0 0"}));
                            nc.panel('add', methods.getNode(cache.misses));
                            nc.panel('add', methods.getNode(cache.attempts));
                            nc.panel('add', methods.getNode(cache.total_cached).css({margin: "0 0 0 0"}));
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

                        svr.nodeDs = dCrt('div').addClass('results');
                        $.each(data.buckets, function () {
                            var bucket = this;
                            var c = dCrt('div');
                            svr.nodeDs.append(c);
                            var body = methods.panel(c, 'glyphicons-table', bucket.title, null, true, null, null);
                            var content = $(document.createElement('table')).addClass('table table-hover');
                            methods.getTableHead(['#', 'Type', 'Total', 'Export'], content);
                            methods.getTableBody(['#', 'label', 'value', 'export'], bucket, bucket.types, content, state.opts.start);
                            body.append(content);
                            total += bucket.count;
                            tables += bucket.types.length;
                        });
                        var t = $.jCommon.number.commas(total);
                        var tbl = $.jCommon.number.commas(tables);
                        var body = methods.panel(svr.m, 'glyphicons-database', 'Soterium Datastore (Tables: ' + tbl + ' Vertices: ' + t + ')', null, false);
                        body.append(svr.nodeDs);
                        methods.resize(svr);
                    }
                },
                indices: {
                    cluster: {
                        init: function (svr, container) {
                            var s = function (data) {
                                methods.html.indices.cluster.create(svr, data, container);
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
                            if(!svr.indexInit) {
                                svr.indexInit = true;
                                var b = dCrt('div');
                                var cls;
                                var name = "Soterium Cluster: ";
                                var item = $.jCommon.array.sortKey(data);
                                item.buckets = [];
                                $.each(item, function (key, value) {
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
                                        item.buckets.push(bucket);
                                    }
                                });

                                var body = methods.panel(container, 'glyphicons-database ' + cls, name, null, false);
                                svr.indexNode = dCrt('div').addClass('results');
                                var ul = dCrt('ul').addClass('list-group');
                                body.append(svr.indexNode.append(ul));
                                methods.resize(svr);

                                $.each(item.buckets, function () {
                                    var c = dCrt('li').addClass('list-group-item');
                                    var l = dCrt('span').html(this.label + ":").css({marginRight: '5px'});
                                    var v = dCrt('span').html(this.value);
                                    c.append(l).append(v);
                                    ul.append(c);
                                });
                            }
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
                        if(!svr.leftInit) {
                            var content = dCrt('div').css({padding: '5px'});

                            if (data.os) {
                                content.append(methods.getNode(data.os.time));
                                content.append(methods.getNode(data.os.name));
                                content.append(methods.getNode(data.os.version));
                            }

                            if (data.extended) {
                                $.each(data.extended, function (key, value) {
                                    content.append(methods.getNode(value));
                                });
                            }
                        }

                        if (data.ds_stats) {
                            if(!svr.leftInit) {
                                svr.nodeIndexEngine = methods.panel(svr.l, "glyphicons-database", "Index CRUD and Query", null, false);
                                svr.nodeIndexEngine.css({padding: '5px'});
                            }
                            svr.nodeIndexEngine.children().remove();
                            $.each(data.ds_stats, function (key, value) {
                                svr.nodeIndexEngine.append(methods.getNode(value))
                            });
                        }

                        if(!svr.leftInit) {
                            svr.nodeSysInfo = methods.panel(svr.l, 'glyphicons-info-sign', "System Information", null, false);
                            svr.nodeSysInfo.append(content);
                        }
                        svr.leftInit=true;
                    }
                },
                logs: {
                    init: function (svr) {
                        var url = methods.getUrl(svr.url + '/admin/logs', state.opts.start, 200 /*state.opts.limit*/);
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
                                svr.logNode = dCrt('div').addClass('results');
                                var body = methods.panel(svr.m, 'glyphicons-blog', "Soterium Logs", null, false);
                                body.append(svr.logNode);
                            }
                            methods.resize(svr);
                            $.each(data, function () {
                                var r = dCrt('div').addClass('result');
                                var h = $(document.createElement('h4')).html(this.level + ': ' + this.time).css({marginBottom: '5px'});
                                var d = dCrt('div').addClass('description').html(this.message);
                                r.append(h).append(d);
                                svr.logNode.append(r);
                            });
                            state.opts.start += data.length;

                            svr.logNode.scrollHandler({
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
                                var body = methods.panel(svr.m, 'glyphicons-blog', "Soterium Logs", null, false);
                                body.append(svr.logNode);
                            }
                            $.each(data, function () {
                                var r = dCrt('div').addClass('result');
                                var h = $(document.createElement('h4')).html(this.level + ': ' + this.time).css({marginBottom: '5px'});
                                var d = dCrt('div').addClass('description').html(this.message);
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

                        var content = dCrt('div').css({padding: '5px'});

                        var cpu = methods.getProgress('CPU', data.processor.load, 0, 100);
                        content.append(cpu);

                        content.append(methods.getNode(data.processor.architecture).css({margin: "-10px 0 10px 0"}));

                        var memory = methods.getProgress("Memory", data.memory.load, 0, 100);
                        content.append(memory);

                        content.append(methods.getNode(data.memory.used).css({margin: "-10px 0 0 0"}));
                        content.append(methods.getNode(data.memory.max).css({margin: "0 0 0 0"}));

                        if(!svr.nodePerformance) {
                            svr.nodePerformance = methods.panel(svr.r, 'glyphicons-server', "Performance", null, false);
                        }
                        svr.nodePerformance.children().remove();
                        svr.nodePerformance.append(content);

                        if($.jCommon.json.hasProperty(data, 'stats.relationships')){
                            svr.nodePerformance.append(methods.getNode(data.stats.relationships).css({marginLeft: '5px'}));
                        }
                    }
                },
                workers: {
                    init: function (svr) {
                        if(!svr.jobInit) {
                            svr.r.append(svr.l.children());
                            svr.nodeJobs = dCrt('div').addClass('result');
                            svr.m.append(svr.nodeJobs);
                            svr.nodeJobs.jobs({url: svr.url, svr: svr, resize: methods.resize});
                            svr.jobInit = true;
                        }
                    }
                }
            }
        };

        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.jAthena.defaults = {
        refresh: true,
        interval: 10000,
        limit: 30,
        start: 0,
        view: 'logs'
    };


    //Plugin Function
    $.fn.jAthena = function (method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function () {
                new $.jAthena($(this), method);
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
