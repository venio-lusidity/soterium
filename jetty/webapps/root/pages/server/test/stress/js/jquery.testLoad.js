

;(function ($) {

    //Object Instance
    $.testLoad = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.testLoad.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        var query = {
            root: function () {
                return {
                    domain: "/electronic/system/enclave/virtual_enclave",
                    "native": {query: {filtered: {filter: {bool: {must: [{term: {"title.raw": "Enterprise"}}]}}}}}};
            },
            enclaves: function (data) {
                if (null != data) {
                    return {
                        domain: '/object/edge/infrastructure_edge',
                        type: data.vertexType,
                        lid: data.lid,
                        sort: {on: 'title', direction: 'asc'},
                        totals: false,
                        "native": {
                            query: {
                                filtered: {
                                    filter: {
                                        bool: {
                                            must: [
                                                {term: {'/object/endpoint/endpointFrom.relatedId.raw': data.lid}},
                                                {term: {'label.raw': '/electronic/base_infrastructure/infrastructures'}}
                                            ],
                                            should: [
                                                {'term': {'/object/endpoint/endpointTo.relatedType.raw': '/electronic/system/enclave/system_enclave'}},
                                                {'term': {'/object/endpoint/endpointTo.relatedType.raw': '/electronic/system/enclave/network_enclave'}},
                                                {'term': {'/object/endpoint/endpointTo.relatedType.raw': '/electronic/system/enclave/virtual_enclave'}}
                                            ]
                                        }
                                    }
                                }
                            }
                        }
                    };
                }
            }
        };

        function ProgressStatus() {
            this.calls = 0;
            this.errors = 0;
            this.success = 0;
            this.sw = new oStopWatch();
            return this;
        }

        function Cells(r) {
            r.children().remove();
            this.row = r;
            // title
            this.c0 = dCrt('td');
            r.append(this.c0);
            // calls
            this.c2 = dCrt('td').css({width: '100px', maxWidth: '100px'}).html('c: 0');
            r.append(this.c2);
            // success
            this.c3 = dCrt('td').css({width: '100px', maxWidth: '100px'}).html('s: 0');
            r.append(this.c3);
            // fail
            this.c4 = dCrt('td').css({width: '100px', maxWidth: '100px'}).html('f: 0');
            r.append(this.c4);
            // messages
            this.c1 = dCrt('td').css({whiteSpace: 'nowrap', overflow: 'hidden'});
            r.append(this.c1);
        }

        // Store a reference to the environment object
        el.data("testLoad", state);
        var servers = lusidity.environment('hosts');

        // Private environment methods
        methods = {
            init: function() {
                $.login.authorized({"groups": ["admin"], "r": true});

                state.body = $.htmlEngine.panel(state, "", "Stress Test", null, false, null, null);

                var tbl = dCrt('table').addClass('table table-stripped table-bordered');
                state.body.prepend(tbl);

                var form = dCrt('form').addClass('navbar-form navbar-left');
                state.ctrlClients = dCrt('input').attr('type', 'number').css({minWidth: '100px', width: '100px'}).addClass('form-control').attr('placeholder', "clients?");
                form.append(state.ctrlClients);
                state.ctrlTime = dCrt('input').attr('type', 'number').css({minWidth: '100px', width: '100px', marginLeft: '5px'}).addClass('form-control').attr('placeholder', "seconds?");
                form.append(state.ctrlTime);
                state.node = dCrt('div');
                state.barNode = dCrt('div').addClass('navbar no-radius blue');
                state.mbNode = dCrt('div').addClass('container-fluid');
                state.barNode.append(form).append(state.mbNode);
                state.body.append(state.barNode);
                state.body.append(state.node);
                methods.menu(tbl);
            },
            menu: function (tbl) {
                var startId = $.jCommon.getRandomId('start-test');
                var stopId = $.jCommon.getRandomId('stop-test');
                state.mbNode.menuBar({
                    target: state.node, separator: false, buttons: [
                        {
                            id: startId,
                            name: 'Start',
                            glyphicon: 'glyphicon-play',
                            tn: 'start',
                            title: 'Start',
                            cls: 'green'
                        },
                        {
                            id: stopId,
                            name: 'Stop',
                            glyphicon: 'glyphicon-stop',
                            tn: 'stop',
                            title: 'Stop',
                            cls: 'red'
                        }
                    ]
                });

                $(document).unbind('menu-bar-start');
                $(document).unbind('menu-bar-stop');

                var start = $('#'+startId);
                state.stop = $('#'+stopId);
                var img,glyph;
                state.stop.attr('disabled', 'disabled');

                var ps = new ProgressStatus();

                start.on('click', function () {
                    state.node.children().remove();
                    state.stop.removeAttr('disabled');
                    start.attr('disabled', 'disabled');
                    state.started = true;
                    glyph = start.find('.glyphicon').removeClass('glyphicon-play');
                    img = $.htmlEngine.getSpinner();
                    glyph.append(img);
                    var row = dCrt('tr');
                    tbl.prepend(row);
                    ps = new ProgressStatus();
                    ps.sw.start();
                    methods.test.init(tbl, row, ps);
                });

                state.stop.on('click', function () {
                    start.removeAttr('disabled');
                    state.stop.attr('disabled', 'disabled');
                    state.started = false;
                    if(img){
                        img.remove();
                    }
                    if(glyph) {
                        glyph.addClass('glyphicon-play');
                    }
                });
            },
            getQueryUrl: function (start, limit) {
                if (undefined == start) {
                    start = 0;
                }
                if (undefined == limit) {
                    limit = 0;
                }
                return state.opts.svr.url + '/query?start=' + start + '&limit=' + limit;
            },
            getInfrastructureUrl: function (data, start, limit) {
                if (undefined == start) {
                    start = 0;
                }
                if (undefined == limit) {
                    limit = 0;
                }
                return data[state.KEY_ID] + '/infrastructure?start=' + start + '&limit=' + limit;
            },
            test:{
                init: function (tbl, row, progressStatus) {
                    var clls = new Cells(row);
                    var tbl1 = dCrt('table').addClass('table table-stripped table-bordered');
                    state.node.append(tbl1);
                    var x = state.ctrlClients.val();
                    if (x) {
                        x = parseInt(x);
                    }
                    else {
                        x = state.opts.clients;
                    }
                    if(x<1){
                        x = state.opts.clients;
                    }
                    state.ctrlClients.val(x);

                    var time = state.ctrlTime.val();
                    if (time) {
                        time = parseInt(time);
                    }
                    else {
                        time = state.opts.time;
                    }
                    if(time<1){
                        time = state.opts.time;
                    }
                    state.ctrlTime.val(time);

                    function summary(cells, ps, n, t){
                        var elapsed = (ps.sw.getElapsed()/1000);
                        var per = (ps.calls/elapsed).toFixed(2);
                        cells.c0.css({width: '200px', maxWidth: '200px'});
                        cells.c0.html(String.format("{0} clients for {1} sec<br\>elapsed: {2}<br\>per second: {3}", n, t, elapsed.toFixed(2), per));
                        cells.c1.html(String.format("started: {0}", $.jCommon.dateTime.defaultFormat(ps.sw.getStartTime())));
                        cells.c2.html(String.format("c: {0}", ps.calls));
                        cells.c3.html(String.format("s: {0}", ps.success));
                        cells.c4.html(String.format("f: {0}", ps.errors));

                        if(state.started && elapsed>=t) {
                            state.stop.click();
                        }

                        if(state.started && elapsed<=t) {
                            window.setTimeout(function () {
                                summary(cells, ps, n, t);
                            }, 100);
                        }
                        else{
                            ps.sw.stop();
                            cells.c1.html(String.format("started: {0}<br/>stopped: {1}",
                                $.jCommon.dateTime.defaultFormat(ps.sw.getStartTime()),
                                $.jCommon.dateTime.defaultFormat(ps.sw.getStopTime())));
                            cells.c1.append(dCrt('br'));
                            var close = dCrt('div').css({cursor: 'pointer'});
                            var span = dCrt('span').addClass('glyphicon glyphicon-remove red').css({marginRight: '5px'});
                            close.append(span).append('Remove');
                            cells.c1.append(close);
                            close.on('click', function () {
                                cells.row.remove();
                            });
                        }
                    }
                    summary(clls, progressStatus, x, time);


                    function clients() {

                        for (var i = 0; i < x; i++) {
                            var r = dCrt('tr');
                            tbl1.append(r);
                            methods.test.client(String.format("client: {0}", i + 1), progressStatus, r);
                        }
                    }
                    clients();
                },
                client: function(name, ps, row){
                    var c = new ProgressStatus();
                    c.sw.start();
                    var clls = new Cells(row);
                    clls.c0.html(name);

                    function call(child, cells) {
                        if(state.started){
                            window.setTimeout(function () {
                                var s = function (data) {
                                    if(data) {
                                        child.success++;
                                        ps.success++;
                                        cells.c3.html(String.format("s: {0}", child.success));
                                        methods.test.enclaves(data, name, ps, child, row, cells, function () {
                                            call(child, cells);
                                        });
                                    }
                                    else{
                                        child.errors++;
                                        ps.errors++;
                                        cells.c4.html(String.format("f: {0}", child.errors));
                                        call(child, cells);
                                    }
                                };
                                var f = function () {
                                    child.errors++;
                                    ps.errors++;
                                    cells.c4.html(String.format("f: {0}", child.errors));
                                    call(child, cells);
                                };
                                var url = methods.getQueryUrl(0, 1000);
                                child.calls++;
                                ps.calls++;
                                cells.c1.html(String.format("Getting root, {0}", url));
                                cells.c2.html(String.format("c: {0}", child.calls));
                                $.htmlEngine.request(url, s, f, query.root(), 'post', true);
                            }, state.opts.delay);
                        }
                        else{
                            child.sw.stop();
                        }
                    }
                    call(c, clls);
                },
                enclaves: function(data, name, ps, child, row, cells, callback){
                    if(state.started){
                        var s = function (item) {
                            if(item) {
                                child.success++;
                                ps.success++;
                                cells.c3.html(String.format("s: {0}", child.success));
                            }
                            else{
                                child.errors++;
                                ps.errors++;
                                cells.c4.html(String.format("f: {0}", child.errors));
                            }
                            if($.isFunction(callback)){
                                callback();
                            }
                        };
                        var f = function () {
                            child.errors++;
                            ps.errors++;
                            cells.c4.html(String.format("f: {0}", child.errors));
                            if($.isFunction(callback)){
                                callback();
                            }
                        };
                        var url = methods.getQueryUrl(0, 1000);
                        child.calls++;
                        ps.calls++;
                        cells.c1.html(String.format("Getting children, {0}", url));
                        cells.c2.html(String.format("c: {0}", child.calls));
                        $.htmlEngine.request(url, s, f, query.enclaves(data), 'post', true);
                    }
                    else{
                        child.sw.stop();
                    }
                }
            }
        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.testLoad.defaults = {
        clients: 10,
        delay: 100,
        time: 30
    };


    //Plugin Function
    $.fn.testLoad = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.testLoad($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $testLoad = $(this).data('testLoad');
            switch (method) {
                case 'exists': return (null!=$testLoad && undefined!=$testLoad && $testLoad.length>0);
                case 'state':
                default: return $testLoad;
            }
        }
    };

})(jQuery);
