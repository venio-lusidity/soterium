(function($) {
    $.jobs = function(el, options) {
        var state = el,
            methods = {};
        state.opts = $.extend({}, $.jobs.defaults, options);
        state.opts.host = $.jCommon.url.create(window.location.href);
        state.KEY_ID = "/vertex/uri";
        state.KEY_TITLE = "title";
        state.servers = lusidity.environment('hosts');
        state.loaded = false;
        el.data("jobs", state);
        methods = {
            init: function() {
                state.current = {};
                methods.getData();
            },
            getData: function() {
                var success = function(data) {
                    if (!state.logged) {
                        state.logged = true;
                    }
                    state.current.data = data;
                    if (state.current.svr) {
                        methods.html.update();
                    } else {
                        methods.html.init(state.opts.svr);
                    }
                    if (state.opts.refresh) {
                        window.setTimeout(function() {
                            methods.getData();
                        }, 10000);
                    }
                };
                var failed = function(jqXHR, textStatus, errorThrown) {
                    window.setTimeout(function() {
                        methods.getData();
                        lusidity.info.red("Cannot update the jobs data at this time will retry in 30 seconds.");
                        lusidity.info.show(10);
                    }, 30000);
                };
                $.htmlEngine.request(state.opts.url + "/worker/jobs", success, failed, null, "get");
                lusidity.environment("onResize", function() {
                    methods.resize(state.current.svr);
                });
            },
            resize: function(svr) {
                if (svr.nodeBody) {
                    var h = svr.nodeBody.availHeight(8);
                    dHeight(svr.nodeBody, h, h, h);
                }
            },
            sortWorkers: function() {
                return $.jCommon.array.sort(state.current.data.workers, [{
                    property: "group",
                    asc: true
                }, {
                    property: "_ordinal",
                    asc: true
                }, {
                    property: "_title",
                    asc: true
                }]);
            },
            html: {
                init: function(svr, data) {
                    if (!svr.nodes) {
                        svr.nodes = {};
                    }
                    if (!svr.nodes.cat) {
                        var t = dCrt("span").html("Jobs Categories");
                        var body = $.htmlEngine.panel(svr.l, "glyphicons-clock", "Jobs Categories", null, false, null, null, true);
                        svr.nodes.cat = dCrt("ul").addClass("list-group");
                        body.append(dCrt("div").addClass("results").append(svr.nodes.cat));
                    }
                    methods.html.cat(svr);
                },
                cat: function(svr) {
                    var keys = [];
                    var workers = methods.sortWorkers();
                    if (!svr.catInit) {
                        $.each(workers, function() {
                            var item = this;
                            var key = $.jCommon.string.makeKey(String.format("{0}_{1}", state.opts.svr.title, item.group));
                            if ($.jCommon.array.contains(keys, key)) {
                                return true;
                            }
                            keys.push(key);
                            var sp = dCrt("span").css({
                                "float": "right",
                                marginRight: "5px"
                            });
                            var jobs = [];
                            $.each(workers, function() {
                                var i = this;
                                var k = $.jCommon.string.makeKey(String.format("{0}_{1}", state.opts.svr.title, i.group));
                                if ($.jCommon.string.equals(key, k)) {
                                    jobs.push(i.type);
                                }
                            });
                            var li = dCrt("li").addClass("list-group-item no-radius").append(dCrt("span").addClass("monitor").css({
                                marginRight: "5px"
                            })).append(dCrt("span").append(item.group)).append(sp).attr("title", "Status changes can take up to a minute.  Clicking the job after it has been selected will refresh the content.");
                            svr.nodes.cat.append(li);
                            li.on("click", function() {
                                svr.nodes.cat.children().removeClass("active");
                                li.addClass("active");
                                state.current.svr = svr;
                                state.current.group = item.group;
                                methods.html.list(workers);
                            });
                            if (svr.nodes.cat.children().length === 1) {
                                li.click();
                            }
                            li.jJobStatus({
                                view: "horizontal",
                                onDone: function(data, node, valid) {},
                                selector: ".monitor",
                                jobs: jobs,
                                excluded: []
                            });
                        });
                        var hli = dCrt("li").addClass("list-group-item no-radius").append(dCrt("span").append("History"));
                        hli.on("click", function() {
                            svr.nodes.cat.children().removeClass("active");
                            hli.addClass("active");
                            state.current.svr = svr;
                            state.current.group = "History";
                            svr.nodeJobs.panel("updateHeader", {
                                glyph: "glyphicons-blog",
                                title: "History"
                            });
                            methods.html.history.init();
                        });
                        svr.nodes.cat.append(hli);
                        svr.catInit = true;
                    }
                },
                update: function() {
                    if (state.current.group !== "History") {
                        var workers = methods.sortWorkers();
                        methods.html.list(workers);
                    }
                },
                list: function(items) {
                    var svr = state.current.svr;
                    var group = state.current.group;
                    var title = String.format("Job: {0}", group);
                    if (!svr.nodeBody) {
                        svr.nodeBody = $.htmlEngine.panel(svr.nodeJobs, "glyphicons-hourglass", title, null, false);
                        svr.nodeBody.addClass("results").css({
                            paddingBottom: "20px"
                        });
                    } else {
                        svr.nodeJobs.panel("updateHeader", {
                            glyph: "glyphicons-hourglass",
                            title: title
                        });
                    }
                    methods.resize(svr);
                    svr.nodeBody.children().remove();
                    if (group === "History" && !svr.historyInit) {
                        svr.historyInit = true;
                    } else {
                        $.each(items, function() {
                            var item = this;
                            if (!$.jCommon.string.equals(item.group, group)) {
                                return true;
                            }
                            methods.html.job(svr, item, svr.nodeBody);
                        });
                    }
                },
                job: function(svr, item, node) {
                    var key = $.jCommon.string.makeKey(String.format("{0}_{1}", state.opts.svr.title, item.group));
                    var pane = dCrt("div").attr("id", "#tab-" + key);
                    node.append(pane);
                    var c = dCrt("div").css({
                        position: "relative",
                        whiteSpace: "nowrap",
                        borderBottom: "1px solid #d4d4d4"
                    });
                    pane.append(c);
                    methods.html.info(c, item);
                    if (item.worker && item.worker.job) {
                        methods.html.time(c, item.worker.job);
                        methods.html.stats(c, item.worker.job);
                    }
                    methods.html.actions(svr, c, item, (item.worker && !item.worker.job));
                },
                getNode: function(item, key, parent) {
                    var tod = $.jCommon.string.equals(key, "timeOfDay");
                    var node = $(document.createElement("div")).css({
                        whiteSpace: "pre-wrap"
                    });
                    if (item && (item.value || (item.value === false))) {
                        var l = $(document.createElement("span")).append(dCrt("strong").append(item.label));
                        var sep = $(document.createElement("span")).append(":").css({
                            marginRight: "5px"
                        });
                        var v = $(document.createElement("span"));
                        if ($.jCommon.string.equals(item.label, "title", true)) {
                            node.css({});
                            node.addClass("blue");
                            v.append(dCrt("strong"));
                        }
                        if (tod) {
                            var last = item.value;

                            function tf(val, dft) {
                                return val ? ((val === 0) ? "00" : val.length === 1 ? "0" + val : val) : dft;
                            }
                            var hr = tf($.jCommon.string.getFirst(last, ":"), 23);
                            var min = tf($.jCommon.string.getLast(last, ":"), 59);
                            last = String.format("{0}:{1}", hr, min);
                            var input = dCrt("input").attr("type", "text").css({
                                width: "50px",
                                maxHeight: "26px"
                            }).attr("title", "In order to save your changes you must exit the input box by either tabbing out, hitting enter or clicking out of the box.");
                            input.val(last);
                            v.append(input);
                            try {
                                input.jTimePicker({
                                    readOnly: true,
                                    hasBtn: true,
                                    clicked: function(e) {
                                        state.opts.refresh = false;
                                    },
                                    focus: function(e) {
                                        state.opts.refresh = false;
                                    },
                                    close: function(e) {
                                        var val = input.val();
                                        if (!$.jCommon.string.equals(val, last)) {
                                            input.hide();
                                            var img = $.htmlEngine.getSpinner().css({
                                                position: "relative",
                                                top: "-2px"
                                            });
                                            var c = dCrt("span");
                                            v.append(c);
                                            c.append(img).append(dCrt("span").append(String.format("Updating to {0}", val)));
                                            var s = function(data) {
                                                state.opts.refresh = true;
                                            };
                                            $.htmlEngine.request(state.opts.url + "/worker/jobs", s, s, {
                                                type: parent.type,
                                                timeOfDay: val,
                                                stop: !parent.automated || !parent.automated.value,
                                                reset: parent.automated && parent.automated.value
                                            }, "post");
                                        } else {
                                            state.opts.refresh = true;
                                        }
                                    }
                                });
                            } catch (e) {
                                console.log(e);
                            }
                        } else {
                            v.append(item.value.toString());
                        }
                        node.append(l).append(sep).append(v);
                    }
                    return node.css({
                        paddingLeft: "5px"
                    });
                },
                make: function(node, keys, item, css) {
                    $.each(keys, function() {
                        try {
                            var v = $.jCommon.json.getProperty(item, this);
                            if (v || (v === false)) {
                                var n = methods.html.getNode(v, this, item);
                                if (css) {
                                    n.css(css);
                                }
                                node.append(n);
                            }
                        } catch (e) {}
                    });
                    node.css({
                        marginBottom: "5px",
                        padding: "5px 5px"
                    });
                },
                actions: function(svr, node, item, enabled) {
                    var mb = dCrt("div").css({
                        clear: "both",
                        position: "absolute",
                        top: "5px",
                        right: "5px"
                    });
                    node.append(mb);
                    var id = $.jCommon.getRandomId("run-btn");
                    var roId = $.jCommon.getRandomId("report-only-btn");
                    var sId = $.jCommon.getRandomId("stop-btn");
                    var kId = $.jCommon.getRandomId("kill-btn");
                    var btns = [{
                        id: id,
                        name: "Run",
                        title: item.executable ? "" : "Can not be executed from the web.",
                        glyphicon: "glyphicons-play-button",
                        tn: id,
                        cls: ""
                    }];
                    if (item.reportOnly) {
                        btns.push({
                            id: roId,
                            name: "Report Only",
                            title: item.executable ? "" : "Can not be executed from the web.",
                            glyphicon: "glyphicons-play",
                            tn: roId,
                            cls: ""
                        });
                    }
                    btns.push({
                        id: sId,
                        name: "Stop",
                        title: "This will attempt to stop the job.",
                        glyphicon: "glyphicons-stop",
                        tn: sId,
                        cls: "yellow"
                    }, {
                        id: kId,
                        name: "Reset",
                        title: "This will attempt to reset the job.",
                        glyphicon: "glyphicons-refresh",
                        tn: kId,
                        cls: "red"
                    });
                    mb.menuBar({
                        target: mb,
                        buttons: btns
                    });
                    var rBtn = $("#" + id);
                    var kBtn = $("#" + kId);
                    var sBtn = $("#" + sId);
                    var roBtn = $("#" + roId);
                    if (!enabled || !item.executable) {
                        rBtn.addClass("disabled");
                        if (item.executable) {
                            var btn = rBtn;
                            var glyph = "glyphicons-play-button";
                            if (state.opts.isReportOnly) {
                                btn = roBtn;
                                glyph = "glyphicons-play";
                            }
                            var glyph = btn.find(".glyphicons").removeClass(glyph);
                            var img = $.htmlEngine.getSpinner().css({
                                position: "relative",
                                top: "2px"
                            });
                            glyph.append(img);
                        }
                    }
                    mb.on("menu-bar-" + sId, function(e, d) {
                        sBtn.attr("disabled", "disabled");
                        var glyph = sBtn.find(".glyphicons").removeClass("glyphicons-stop");
                        var img = $.htmlEngine.getSpinner().css({
                            position: "relative",
                            top: "2px"
                        });
                        glyph.append(img);
                        var s = function(data) {
                            if (data && data.started) {
                                sBtn.hide();
                                lusidity.info.yellow('It may take a few minutes to stop the job "' + item.title.value + '".');
                                lusidity.info.show(5, function() {
                                    methods.resize(svr);
                                });
                            }
                            img.remove();
                            sBtn.addClass("glyphicons-stop");
                            sBtn.attr("disabled", "");
                        };
                        $.htmlEngine.request(state.opts.url + "/worker/jobs", s, s, {
                            type: item.type,
                            stop: true
                        }, "post");
                    });
                    mb.on("menu-bar-" + kId, function(e, d) {
                        kBtn.attr("disabled", "disabled");
                        var glyph = kBtn.find(".glyphicons").removeClass("glyphicons-refresh");
                        var img = $.htmlEngine.getSpinner().css({
                            position: "relative",
                            top: "2px"
                        });
                        glyph.append(img);
                        var s = function(data) {
                            if (data && data.started) {
                                kBtn.hide();
                                lusidity.info.yellow('It might take a few minutes to reset the job "' + item.title.value + '".');
                                lusidity.info.show(5);
                            }
                            img.remove();
                            kBtn.addClass("glyphicosn-refresh");
                            kBtn.attr("disabled", "");
                        };
                        $.htmlEngine.request(state.opts.url + "/worker/jobs", s, s, {
                            type: item.type,
                            reset: true
                        }, "post");
                    });

                    function post(a, b, pdata) {
                        a.addClass("disabled");
                        b.addClass("disabled");
                        var glyph = a.find(".glyphicons").removeClass("glyphicons-play-button");
                        var img = $.htmlEngine.getSpinner().css({
                            position: "relative",
                            top: "2px"
                        });
                        glyph.append(img);
                        var s = function(data) {
                            if (!data || !data.started) {
                                a.hide();
                                b.hide();
                                lusidity.info.red('Failed to start the job "' + item.title.value + '".');
                                lusidity.info.show(5);
                            }
                        };
                        $.htmlEngine.request(state.opts.url + "/worker/jobs", s, s, pdata, "post");
                    }
                    mb.on("menu-bar-" + roId, function(e, d) {
                        state.opts.isReportOnly = true;
                        post(roBtn, rBtn, {
                            type: item.type,
                            reportOnly: true
                        });
                    });
                    mb.on("menu-bar-" + id, function(e, d) {
                        state.opts.isReportOnly = false;
                        post(rBtn, roBtn, {
                            type: item.type,
                            reportOnly: false
                        });
                    });
                },
                info: function(node, item) {
                    var row = dCrt("div").css({
                        marginRight: state.opts.infoMarginLeft
                    });
                    node.append(row);
                    var keys = ["title", "description", "automated", "timeOfDay", "worker.started", "worker.job.started", "worker.job.message"];
                    methods.html.make(row, keys, item);
                },
                event: function(node, item) {
                    var row = dCrt("div");
                    node.append(row);
                    var keys = ["started", "finished", "elapsed"];
                    methods.html.make(row, keys, item);
                },
                time: function(node, item) {
                    if (item) {
                        var keys = ["elapsed", "perSecond", "estimated"];
                        var row = dCrt("div").addClass("table-cell").css({
                            whiteSpace: "nowrap"
                        });
                        node.append(row);
                        methods.html.make(row, keys, item, {
                            display: "table-cell",
                            padding: "0 5px 0 0"
                        });
                    }
                },
                stats: function(node, item, status) {
                    if (item) {
                        var keys = ["processed", "primary", "matches", "skipped", "created", "updated", "deleted", "queries", "total"];
                        var row = dCrt("div").css({
                            whiteSpace: "nowrap"
                        });
                        if ($.jCommon.string.equals(status, "processed", true)) {
                            node.addClass("green");
                        } else {
                            if ($.jCommon.string.equals(status, "failed", true)) {
                                node.addClass("red");
                            } else {
                                if ($.jCommon.string.equals(status, "processing", true)) {
                                    node.addClass("blue");
                                }
                            }
                        }
                        node.append(row);
                        methods.html.make(row, keys, item, {
                            display: "table-cell",
                            padding: "0 5px 0 0"
                        });
                    }
                },
                history: {
                    init: function() {
                        var svr = state.current.svr;
                        methods.resize(svr);
                        svr.nodeBody.children().remove();
                        state.loaded = true;
                        var limit = 100;
                        state.start = 0;
                        state.hits = 0;
                        var alias = getAlias(svr.url);

                        function page(scrolled) {
                            var s = function(d) {
                                if (d && d.hits) {
                                    state.start = d.next;
                                    state.hits = d.hits;
                                    if (scrolled) {
                                        svr.nodeBody.scrollHandler("start");
                                    }
                                    init(svr.nodeBody, d, scrolled);
                                }
                            };
                            $.htmlEngine.request(state.opts.url + "/jobs/history?start=" + state.start + "&limit=" + limit + "&server=" + alias, s, s, null, "get", false);
                        }
                        svr.nodeBody.scrollHandler({
                            adjust: 50,
                            start: function() {},
                            stop: function() {},
                            top: function() {},
                            bottom: function() {
                                if (state.start < state.hits) {
                                    page(true);
                                }
                            }
                        });

                        function make(node, item, scrolled, on) {
                            var id = item.lid;
                            var c = node.find('div[data-id="' + id + '"]');
                            if (c.length === 0) {
                                if (!scrolled) {
                                    state.start += 1;
                                }
                                if (item.value) {
                                    return true;
                                }
                                if (!item.stats) {
                                    item.stats = {
                                        message: {}
                                    };
                                }
                                try {
                                    item.stats.message.value = item.status;
                                } catch (e) {}
                                try {
                                    c = dCrt("div").attr("data-id", id).css({
                                        cursor: "pointer",
                                        backgroundColor: $.jCommon.number.isEven(on) ? "#f5f5f5" : "transparent"
                                    });
                                    node.append(c);
                                    if (item.started) {
                                        item.stats.started = {
                                            label: "Started",
                                            value: $.jCommon.dateTime.defaultFormat(item.started)
                                        };
                                    }
                                    methods.html.history.info(c, item);
                                } catch (e) {}
                            }
                        }

                        function init(node, d, scrolled) {
                            if (d && d.results) {
                                $.each(d.results, function() {
                                    make(node, this, scrolled);
                                });
                            }
                        }

                        function getAlias(svrUrl) {
                            var a;
                            $.each(state.servers, function() {
                                var server = this;
                                if ($.jCommon.string.equals(svrUrl, server.url, true)) {
                                    a = server.title ? server.title : server.alias;
                                    return false;
                                }
                            });
                            return a;
                        }
                        page(false);
                    },
                    info: function(container, item) {
                        container.append(methods.html.getNode(item.type)).css({
                            padding: "5px 5px"
                        });
                        if (item.title) {
                            container.append(methods.html.getNode({
                                label: "Title",
                                value: item.title
                            }));
                        }
                        if (item.jobType) {
                            container.append(methods.html.getNode({
                                label: "Class",
                                value: item.jobType
                            }));
                        }
                        if (item.status) {
                            var status = methods.html.getNode({
                                label: "Status",
                                value: item.status
                            });
                            var badge = dCrt("span").addClass("badge").html("&nbsp;");
                            container.append(status.append("&nbsp;").append(badge));
                            var clr = "green";
                            switch (item.status) {
                                case "processed":
                                    clr = "green";
                                    break;
                                case "idle":
                                    clr = "yellow";
                                    break;
                                case "failed":
                                    clr = "red";
                                    break;
                                case "partial":
                                    clr = "blue";
                                    break;
                                default:
                                    break;
                            }
                            badge.addClass(clr);
                        }
                        var t = dCrt("div");
                        container.append(t);
                        if (item.startedWhen) {
                            t.append(methods.html.getNode({
                                label: "Started",
                                value: $.jCommon.dateTime.defaultFormat(item.startedWhen)
                            }));
                        }
                        if (item.stoppedWhen) {
                            t.append(methods.html.getNode({
                                label: "Stopped",
                                value: $.jCommon.dateTime.defaultFormat(item.stoppedWhen)
                            }));
                        }
                        if (item.elapsed) {
                            t.append(methods.html.getNode({
                                label: "Elapsed",
                                value: item.elapsed
                            }));
                        }
                        t.children().css({
                            display: "inline-block"
                        });
                        var ps = item["/data/process_status/processStatus"];
                        if (ps) {
                            function getValue(prop) {
                                return ps[prop].count;
                            }
                            var v1 = dCrt("div");
                            container.append(v1);
                            v1.append(methods.html.getNode({
                                label: "Processed",
                                value: getValue("/system/primitives/synchronized_integer/processed")
                            }));
                            v1.append(methods.html.getNode({
                                label: "Total",
                                value: getValue("/system/primitives/synchronized_integer/total")
                            }));
                            v1.append(methods.html.getNode({
                                label: "Primary",
                                value: getValue("/system/primitives/synchronized_integer/primary")
                            }));
                            v1.append(methods.html.getNode({
                                label: "Inner",
                                value: getValue("/system/primitives/synchronized_integer/innerProcessed")
                            }));
                            v1.children().css({
                                display: "inline-block"
                            });
                            var v2 = dCrt("div");
                            container.append(v2);
                            v2.append(methods.html.getNode({
                                label: "Matches",
                                value: getValue("/system/primitives/synchronized_integer/matches")
                            }));
                            v2.append(methods.html.getNode({
                                label: "Skipped",
                                value: getValue("/system/primitives/synchronized_integer/skipped")
                            }));
                            v2.append(methods.html.getNode({
                                label: "Errors",
                                value: getValue("/system/primitives/synchronized_integer/errors")
                            }));
                            v2.children().css({
                                display: "inline-block"
                            });
                            var v3 = dCrt("div");
                            container.append(v3);
                            v3.append(methods.html.getNode({
                                label: "Created",
                                value: getValue("/system/primitives/synchronized_integer/created")
                            }));
                            v3.append(methods.html.getNode({
                                label: "Updated",
                                value: getValue("/system/primitives/synchronized_integer/updated")
                            }));
                            v3.append(methods.html.getNode({
                                label: "Deleted",
                                value: getValue("/system/primitives/synchronized_integer/deleted")
                            }));
                            v3.append(methods.html.getNode({
                                label: "Queries",
                                value: getValue("/system/primitives/synchronized_integer/queries")
                            }));
                            v3.children().css({
                                display: "inline-block"
                            });
                        }
                    }
                }
            }
        };
        methods.init();
    };
    $.jobs.defaults = {
        infoMarginLeft: "399px",
        refresh: true
    };
    $.fn.jobs = function(method, options) {
        if (method === undefined) {
            method = {};
        }
        if (typeof method === "object") {
            return this.each(function() {
                new $.jobs($(this), method);
            });
        } else {
            var $jobs = $(this).data("jobs");
            switch (method) {
                case "exists":
                    return (null != $jobs && undefined != $jobs && $jobs.length > 0);
                case "state":
                default:
                    return $jobs;
            }
        }
    };
})(jQuery);