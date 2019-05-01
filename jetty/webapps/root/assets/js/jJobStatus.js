(function($) {
    $.jJobStatus = function(el, options) {
        var state = el,
            methods = {};
        state.opts = $.extend({}, $.jJobStatus.defaults, options);
        state.KEY_ID = "/vertex/uri";
        state.KEY_TITLE = "title";
        el.data("jJobStatus", state);
        methods = {
            init: function() {
                methods.get();
                lusidity.environment("onResize", function() {
                    methods.resize();
                });
            },
            get: function() {
                state.sUrl = lusidity.environment("host-secondary");
                var url = state.sUrl + "/jobs";
                var s = function(data) {
                    if (data && data.results && state.opts.view) {
                        data.results = $.jCommon.array.sort(data.results, [{
                            property: "title",
                            asc: true
                        }]);
                        methods.views[state.opts.view](data);
                    }
                };
                var f = function() {};
                $.htmlEngine.request(url, s, f, null, "get");
            },
            isIncluded: function(data) {
                var included = false;
                if (state.opts.jobs && state.opts.jobs.length > 0) {
                    for (var i = 0; i < state.opts.jobs.length; i++) {
                        if ($.jCommon.string.equals(state.opts.jobs[i].toString(), data)) {
                            included = true;
                            break;
                        }
                    }
                } else {
                    included = true;
                }
                if (state.opts.excluded && state.opts.excluded.length > 0) {
                    for (var t = 0; t < state.opts.excluded.length; t++) {
                        if ($.jCommon.string.equals(state.opts.excluded[t].toString(), data)) {
                            included = false;
                            break;
                        }
                    }
                }
                return included;
            },
            getTotalRunning: function(data) {
                var running = 0;
                $.each(data, function() {
                    var result = this;
                    if (methods.isIncluded(result["class"])) {
                        if (result.running) {
                            running++;
                        }
                    }
                });
                return running;
            },
            exists: function(node) {
                return (node && (node.length > 0));
            },
            resize: function() {},
            sleep: function() {
                window.setTimeout(function() {
                    methods.get();
                }, state.opts.interval);
            },
            views: {
                menu: function(data) {
                    if (state.is("ul")) {
                        state.children().remove();
                        var count = methods.getTotalRunning(data.results);
                        var n = dCrt("span").html(count);
                        state.append(n);
                        if (count > 0) {
                            var g = dCrt("span").css({
                                marginTop: "8px",
                                marginLeft: "5px"
                            });
                            var spin = $.htmlEngine.getSpinner();
                            g.append(spin);
                            n.append(g);
                        }
                        var c = 0;
                        var valid = false;
                        $.each(data.results, function() {
                            var job = this;
                            debugger;
                            if (methods.isIncluded(job["class"])) {
                                var li = dCrt("li");
                                if (c === 0) {
                                    li.css({
                                        marginTop: "10px"
                                    });
                                }
                                var running = job.running;
                                var lk = dCrt("a").attr("href", "#").addClass("nav-list-item");
                                var s = dCrt("span").html(job.title);
                                li.append(lk.append(s)).attr("data-toggle", "tooltip").attr("data-placement", "left").attr("title", job.desc);
                                li.tooltip();
                                if (running) {
                                    var itemSpin = $.htmlEngine.getSpinner();
                                    var k = dCrt("span").append(itemSpin).css({
                                        marginTop: "8px",
                                        marginLeft: "5px"
                                    });
                                    s.append(k);
                                }
                                state.append(li);
                                var d = dCrt("li").addClass("divider");
                                state.append(d);
                                c++;
                                if (running) {
                                    valid = true;
                                }
                            }
                        });
                        if ($.isFunction(state.opts.onDone)) {
                            state.opts.onDone(data, state, valid);
                        }
                    }
                    methods.sleep();
                },
                bar: function(data) {
                    state.children().remove();
                    var valid = false;
                    $.each(data.results, function() {
                        var job = this;
                        if (methods.isIncluded(job["class"]) && job.running) {
                            debugger;
                            valid = true;
                            return false;
                        }
                    });
                    if ($.isFunction(state.opts.onDone)) {
                        debugger;
                        state.opts.onDone(data, state, valid);
                    }
                    methods.sleep();
                },
                badge: function(data) {
                    var bdg = state.find(".badge");
                    if (bdg.length === 0) {
                        bdg = dCrt("span").addClass("badge blue");
                        state.append(bdg);
                    }
                    var n = 0;
                    $.each(data.results, function() {
                        var job = this;
                        if (methods.isIncluded(job["class"]) && (job.running || job.started)) {
                            n++;
                        }
                    });
                    bdg.html(n);
                    methods.sleep();
                },
                horizontal: function(data) {
                    var node = state.find(state.opts.selector);
                    if (node.length > 0) {
                        node.children().remove();
                        var s = 0;
                        var r = 0;
                        $.each(data.results, function() {
                            var job = this;
                            if (methods.isIncluded(job["class"])) {
                                s += (job.started ? 1 : 0);
                                r += (job.running ? 1 : 0);
                            }
                        });

                        function mk(n, t) {
                            return dCrt("span").addClass("badge-default").append(n).css({
                                padding: "2px 4px",
                                "marginRight": "2px"
                            }).attr("title", t);
                        }
                        var sn = mk(s, "started (Either started manually or runs on a timer or interval)");
                        var rn = mk(r, "running (Currently processing)");
                        node.append(sn);
                        node.append(rn);
                        if (s > 0) {
                            sn.removeClass("badge-default").addClass("badge-green");
                        }
                        if (r > 0) {
                            rn.removeClass("badge-default").addClass("badge-green");
                        }
                    }
                    methods.sleep();
                }
            }
        };
        methods.init();
    };
    $.jJobStatus.defaults = {
        interval: 10000,
        childType: "li",
        select: [],
        excluded: []
    };
    $.fn.jJobStatus = function(method, options) {
        if (method === undefined) {
            method = {};
        }
        if (typeof method === "object") {
            return this.each(function() {
                new $.jJobStatus($(this), method);
            });
        } else {
            var $jJobStatus = $(this).data("jJobStatus");
            switch (method) {
                case "exists":
                    return (null !== $jJobStatus && undefined !== $jJobStatus && $jJobStatus.length > 0);
                case "state":
                default:
                    return $pluginName;
            }
        }
    };
})(jQuery);