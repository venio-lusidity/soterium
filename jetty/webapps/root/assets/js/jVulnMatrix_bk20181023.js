;(function ($) {
    // triggers
    // document 'vuln-matrix-vulnerabilities-selected' (node, item, {checked: true or false}
    //Object Instance
    $.jVulnMatrix = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.jVulnMatrix.defaults, options);
        state.KEY_ID = '/vertex/uri';
        state.uri = state.opts.data[state.KEY_ID];
        state.editing = false;
        state.debug = false;
        state.opts.url = $.jCommon.url.create(window.location.href);
        state.ctrlId = $.jCommon.getRandomId("matrix");

        // Store a reference to the environment object
        el.data('jVulnMatrix', state);

        // Private environment methods
        methods = {
            init: function() {
                methods.html[state.opts.type].init();
            },
            query:{
                getCveById: function(vulnId){
                    return {
                        domains: ['/technology/security/vulnerabilities/cve/cve'],
                        filters:[
                            {operator: "must", propertyName: "vulnId", type: "raw", value: vulnId}
                        ]
                    };
                },
                getIavmById: function(iavmId){
                    return {
                        domains: ['/technology/security/iavm'],
                        filters:[
                            {operator: 'must', propertyName: 'noticeId', type: 'raw', value: vulnId}
                        ]
                    };
                }
            },
            get: function(url, onSuccess, onFail, async) {
                //noinspection JSUnusedLocalSymbols
                var action = {
                    connector: null,
                    async: async,
                    data: null,
                    methodType: 'get',
                    showProgress: false,
                    onbeforesend: {
                        message: {msg: null, debug: false},
                        execute: function () {
                        }
                    },
                    oncompleted: {
                        execute: function (jqXHR, textStatus) {}
                    },
                    onsuccess: {
                        message: {msg: null, debug: false},
                        execute: function (data) {
                            if (onSuccess && $.isFunction(onSuccess)) {
                                onSuccess(data);
                            }
                            else {
                                lusidity.info.green("Although response was successful, I don't know what to do with it.");
                                lusidity.info.show(10);
                            }
                        }
                    },
                    onfailure: {
                        message: {msg: null, debug: false},
                        execute: function (jqXHR, textStatus, errorThrown) {
                            if (onFail && $.isFunction(onFail)) {
                                onFail(jqXHR, textStatus, errorThrown);
                            }
                            else {
                                lusidity.info.red('Sorry, we cannot find the resource requested.&nbsp;&nbsp;'
                                    + 'You can try refreshing the&nbsp;<a href="' + window.location.href + '">page</a>.');
                                lusidity.info.show(10);
                            }
                        }
                    },
                    url: url
                };
                lusidity.environment('request', action);
            },
            getDate:function(stringDate){
                var result;
                    var d = new Date(Date.parse(stringDate));
                var options = {
                    year: "numeric", month: "short", day: "numeric"
                };
                result = d.toLocaleDateString("en-us", options);
                return result;
            },
            getUrl: function (url, start, limit) {
                if (undefined === start) {
                    start = 0;
                }
                if (undefined === limit) {
                    limit = 0;
                }
                return url + ($.jCommon.string.contains(url, "?") ? '&' : '?') + 'start=' + start + '&limit=' + limit;
            },
            getQueryUrl: function (start, limit) {
                return methods.getUrl('/query', start, limit);
            },
            getPanel: function(glyph, title, url, borders){
                var c = dCrt('div');
                state.body = $.htmlEngine.panel(c, glyph, title, url, borders, null, null);
                return c;
            },
            html:{
                card: {
                    init: function() {
                        methods.html.card.load();
                    },
                    load: function(){
                        if (!state.cardPanel) {
                            if(state.paneled) {
                                state.cardPanel = methods.getPanel(state.opts.glyph, state.opts.title, state.opts.href ? state.opts.href : null, state.opts.borders);
                            }
                            else{
                                state.cardPanel = dCrt('div');
                                state.body = dCrt('div');
                                state.cardPanel.append(state.body);
                                state.append(state.cardPanel);
                            }
                            state.append(state.cardPanel);
                        }
                        state.body.children().remove();
                        var header = dCrt('h5').attr('id', String.format('hd_{0}', state.ctrlId)).addClass('default-grey').css({padding: '10px', marginBottom: '10px'}).html("Risk Matrix");
                        state.body.append(header);

                        var dif = state.opts.offsetWidth;
                        var noHtml = state.width() <= 290;
                        var width = (state.body.innerWidth() - dif) / 6;
                        width = (width < 20) ? 20 : width;

                        var tbl = $(document.createElement('table')).addClass('jVulnMatrix').css({
                            width: (width * 6) + 'px',
                            maxWidth: (width * 6) + 'px',
                            height: width + 'px',
                            verticalAlign: 'top',
                            margin: '0 auto'
                        });
                        if (state.opts.css) {
                            tbl.css(state.opts.css);
                        }
                        state.body.css({marginBottom: '10px'});
                        state.body.append(tbl);

                        var row = $(document.createElement('tr'));

                        var td1 = $(document.createElement('td')).css({verticalAlign: 'top'});
                        var lhTxt = dCrt('div').html(noHtml ? 'EX' : 'Exploitability').addClass('rotate-left').css({position: 'relative', top: ((width*6)/2) + 'px'});
                        var lh = dCrt('div').addClass('head').css({
                            width: width + 'px',
                            height: (width*6) + 'px'
                        }).append(lhTxt).attr('title', 'Exploitability');
                        td1.append(lh);

                        var td2 = $(document.createElement('td')).css({verticalAlign: 'top'});
                        var mi = dCrt('div').addClass('head').css({
                            textAlign: 'center',
                            verticalAlign: 'middle',
                            lineHeight: width + 'px',
                            width: ((width * 5)) + 'px',
                            maxHeight: '104px'
                        }).html(noHtml ? 'MI' : 'Mission Impact').attr('title', 'Mission Impact');
                        var node = dCrt("div");
                        td2.append(mi).append(node);

                        row.append(td1).append(td2);
                        tbl.append(row);


                        var table = $(document.createElement('table')).addClass('risk-matrix').attr('id', state.ctrlId).css({position: 'relative'});
                        node.append(table);

                        var model = [
                            [{s: 11}, {s: 16}, {s: 20}, {s: 23}, {s: 25}],
                            [{s: 7}, {s: 13}, {s: 18}, {s: 21}, {s: 24}],
                            [{s: 4}, {s: 8}, {s: 15}, {s: 19}, {s: 22}],
                            [{s: 2}, {s: 5}, {s: 9}, {s: 14}, {s: 17}],
                            [{s: 1}, {s: 3}, {s: 6}, {s: 10}, {s: 12}]
                        ];

                        $.each(model, function () {
                            var rw = $(document.createElement('tr'));
                            var m = this;
                            $.each(m, function () {
                                var item = this;
                                var td = $(document.createElement('td')).attr('data-score', item.s).css({
                                    width: width + 'px',
                                    height: width + 'px'
                                });
                                rw.append(td);
                            });
                            table.append(rw);
                        });
                        methods.update({data: state.opts.data});
                    }
                },
                vulnerabilities: {
                    init: function () {
                        if (!state.started) {
                            state.started = true;
                            var c = dCrt('div').css({height: 'inherit'});
                            state.append(c);
                            if (state.opts.adjustHeight || state.opts.fill) {
                                $.htmlEngine.adjustHeight(state, true, state.opts.adjustHeight, false, function () {
                                    c.pVulnerabilities('reset');
                                });
                            }
                            c.jNodeReady({onReady: function () {
                                var opts = {
                                    initOnClick: state.opts.initOnClick,
                                    active: (state.opts.active===undefined) ? true : state.opts.active,
                                    hInherit: true,
                                    title: state.opts.title,
                                    glyph: state.opts.glyph,
                                    offset: state.opts.offset,
                                    data: {totals: true, data: state.opts.data, '/vertex/uri': state.opts.data[state.KEY_ID]}
                                };
                                c.pVulnerabilities(opts);
                            }});
                        }
                    }
                },
                findings: {
                    init: function () {
                        if(!state.initialized) {
                            state.initialized = true;
                            state.addClass('table');
                            if (!state.vulnPanel) {
                                state.vulnPanel = methods.getPanel(state.opts.glyph, state.opts.title, null, state.opts.borders);
                                state.append(state.vulnPanel);
                            }
                            state.body.children().remove();
                            var table = $(document.createElement('table')).css({
                                width: '100%',
                                'table-layout': 'fixed'
                            });
                            state.body.append(table);

                            var item = {
                                next: 0
                            };

                            function page() {
                                // $.htmlEngine.busy(state, {type: 'cube', cover: true});
                                var deviceId = state.uri;
                                var s = function (data) {
                                    try {
                                        item = data;
                                        methods.html.findings.rows(deviceId, data.results, table);
                                        if(item.hits){
                                            var header = state.vulnPanel.find('.panel-heading-title');
                                            if(header) {
                                                header.children().remove();
                                                var v = state.opts.title + ' ' + ((item.next>=item.hits)?item.hits:item.next) + '/' + item.hits;
                                                var t = $(document.createElement('div')).html(v);
                                                header.append(t);
                                            }
                                        }
                                    }
                                    catch (e) {
                                    }
                                    state.body.scrollHandler('start');
                                };
                                var f = function () {
                                    state.body.scrollHandler('start');
                                };
                                var uri;
                                if(state.opts.propertyKey){
                                    uri = state.opts.url.relativePath + '/properties' + state.opts.propertyKey;
                                }
                                else {
                                    uri = state.uri + '/vulnerabilities';
                                }
                                if(state.opts.top){
                                    uri = uri + '/aggregate'
                                }
                                uri+='?qt=vuln&passed=false&limit=' + state.opts.limit + '&start=' + item.next;
                                $.htmlEngine.request(uri, s, f, null, 'get');
                            }

                            state.body.scrollHandler({
                                adjust: 10,
                                start: function () {
                                },
                                stop: function () {
                                },
                                top: function () {
                                },
                                bottom: function () {
                                    if (item.next < item.hits) {
                                        page();
                                    }
                                }
                            });
                            page();
                        }
                    },
                    rows: function (deviceId, items, table) {
                        if (null !== items && undefined !== items) {
                            var hasHeader = false;
                            var row;
                            $.each(items, function () {
                                var item = this;
                                var vulnId = item['vulnId'];
                                if (null !== vulnId && undefined !== vulnId) {
                                    var htmlUrl = item[state.KEY_ID];
                                    var altUrl;
                                    if ($.common.string.startsWith(vulnId, 'cve', true)) {
                                        altUrl = 'https://web.nvd.nist.gov/view/vuln/detail?vulnId=' + vulnId;
                                    }
                                    var searchUrl = '/discover?q=' + vulnId;
                                    var summary = $.common.string.decode($.common.json.getProperty(item, '/system/primitives/raw_string/descriptions.results.value'));

                                    function create(key, row) {
                                        var td = $(document.createElement('td')).addClass('td-' + key).css({padding: '5px', verticalAlign: 'top'});
                                        var value = item[key];
                                        switch (key) {
                                            case 'vulnId':
                                                value = $.common.string.replaceAll(value, "_rule", "") + ((item.title && !$.common.string.equals(value, item.title, true)) ? ': ' + item.title: '');
                                                var link;
                                                if (!$.common.string.empty(htmlUrl)) {
                                                    link = $(document.createElement('a')).attr('href', htmlUrl).attr('target', '_blank').html(value);
                                                    td.append(link);
                                                }
                                                else {
                                                    td.append(value);
                                                }
                                                if (altUrl) {
                                                    link = $(document.createElement('a')).attr('href', altUrl).attr('target', '_blank')
                                                        .attr('title', 'National Vulnerability Database').addClass('fav-right').html('<span class="glyphicon glyphicon-share" aria-hidden="true"></span>');
                                                    td.append(link);
                                                }
                                                if (searchUrl) {
                                                    link = $(document.createElement('a')).attr('href', searchUrl).attr('target', '_blank')
                                                        .attr('title', 'Search').addClass('fav-right').html('<span class="glyphicon glyphicon-search" aria-hidden="true"></span>');
                                                    td.append(link);
                                                }
                                                break;
                                            case '/technology/security/vulnerabilities/cve/metric/base_metrics':
                                                td.html($.common.json.getProperty(item, key + '.base'));
                                                break;
                                            case 'score':
                                                var w = 20;
                                                var cbOuter = $(document.createElement('div')).css({display: 'inline-block'});
                                                var cb = $(document.createElement('input')).attr('type', 'checkbox')
                                                    .css({margin: '0 10px 0 0', position: 'relative', top: '2px'});
                                                cbOuter.append(cb);
                                                if(!state.editing) {
                                                    cbOuter.hide();
                                                }
                                                else{
                                                    cbOuter.show();
                                                }
                                                td.append(cbOuter);

                                            function changed() {
                                                var checked = cb.is(':checked');
                                                $(document).trigger('vuln-matrix-vulnerabilities-selected', [cb, item, {checked: checked}]);
                                                if (checked) {
                                                    row.addClass('selected');
                                                    row.next().addClass('selected');
                                                    row.next().removeClass('mouseover');
                                                }
                                                else {
                                                    row.removeClass('selected');
                                                    row.next().removeClass('selected');
                                                    row.next().addClass('mouseover')
                                                }
                                            }

                                                if (item.vuln_data) {
                                                    var div = $(document.createElement('div')).css({display: 'inline-block', verticalAlign: 'top'});
                                                    var span = $(document.createElement('span')).html('&nbsp;').addClass('vuln badge');
                                                    if (item.vuln_data.html && item.vuln_data.html.cls) {
                                                        div.attr('title', item.vuln_data.html.label);
                                                        span.addClass(item.vuln_data.html.cls);
                                                    }
                                                    div.append(span);
                                                    td.append(div);
                                                    w += 20;
                                                }
                                                td.attr('width', w);
                                                cb.on('change', function () {
                                                    if(state.editing) {
                                                        changed(cb);
                                                    }
                                                });

                                                $(document).on('poam-content-add', function (e) {
                                                    state.editing = true;
                                                    td.attr('width', w + 20);
                                                    cbOuter.show();
                                                    row.removeClass('selected');
                                                    row.next().removeClass('selected');
                                                    row.addClass('selectable');
                                                    row.on('mouseenter', function () {
                                                        row.next().addClass('mouseover');
                                                    });
                                                    row.on('mouseleave', function () {
                                                        row.next().removeClass('mouseover');
                                                    });
                                                    row.on('click', function () {
                                                        var checked = cb.is(':checked');
                                                        if (checked) {
                                                            cb.prop('checked', false);
                                                        }
                                                        else {
                                                            cb.prop('checked', true);
                                                        }
                                                        changed();
                                                    });
                                                });
                                                $(document).on('poam-content-form-close', function (e) {
                                                    state.editing = false;
                                                    td.attr('width', w - 20);
                                                    cb.prop('checked', false);
                                                    cbOuter.hide();
                                                    row.removeClass('selected');
                                                    row.next().removeClass('selected');
                                                    row.removeClass('selectable');
                                                    row.unbind('mouseenter');
                                                    row.unbind('mouseleave');
                                                    row.unbind('click');
                                                });
                                                break;
                                            default:
                                                td.html(value);
                                                break;
                                        }
                                        row.append(td);
                                    }

                                    if (hasHeader) {
                                        row = $(document.createElement('tr'));
                                        hasHeader = true;

                                        var headers = ['&nbsp;', 'Base Score', 'score'];
                                        $.each(headers, function () {
                                            var title = this.toString();
                                            var cls = 'cell-header';
                                            if (title === 'CVE Id') {
                                                cls = 'cell-header-title';
                                            }
                                            var td = $(document.createElement('td')).html(title).addClass(cls);
                                            row.append(td);
                                        });
                                        table.append(row).css({'border-bottom': 'solid 1px #c3c3c3'});
                                    }
                                    var keys = ['score', 'vulnId'];
                                    row = $(document.createElement('tr'));
                                    $.each(keys, function () {
                                        create(this.toString(), row);
                                    });
                                    row.attr('data-id', item[state.KEY_ID]);
                                    table.append(row);
                                    row = $(document.createElement('tr'));
                                    var td = $(document.createElement('td'))
                                        .attr('colspan', keys.length).html(summary)
                                        .css({'padding': '5px', 'border-bottom': 'solid 1px #c3c3c3'});
                                    row.append(td);
                                    table.append(row);
                                    if(state.editing){
                                        $(document).trigger('poam-content-add');
                                    }
                                }
                            });

                            $(document).on('poam-content-item-deleted', function(e, i){
                                var rows = table.find('tr');
                                rows.removeClass('selected');
                            });

                            $(document).on('poam-content-item-selected', function(e, i){
                                var rows = table.find('tr');
                                rows.removeClass('selected');
                                var on = 0;
                                $.each(i, function() {
                                    var selId = this[state.KEY_ID];
                                    $.each(rows, function () {
                                        if((on % 2)===0) {
                                            var row = $(this);
                                            var id = row.attr('data-id');
                                            if ($.common.string.equals(selId, id)) {
                                                row.addClass('selected');
                                                row.next().addClass('selected');
                                            }
                                            else if (!row.hasClass('selected')) {
                                                row.removeClass('selected');
                                                row.next().removeClass('selected');
                                            }
                                        }
                                        on++;
                                    });
                                });
                            });
                        }
                    }
                }
            },
            update: function (options) {
                if (options.data && options.data.metrics) {
                    var table = $('#' +state.ctrlId);
                    var node = $(table.parent());
                    if (table) {
                        var w = node.outerWidth();
                        var h = table.outerHeight();
                        function point(ia, ea, id, tooltip, clr) {
                            var dot = $('#'+ id);
                            if(dot.length<1){
                                dot = dCrt('div').attr('id', id).css({position: 'absolute', zIndex: '999999'});
                                var sp = dCrt('span').addClass('glyphicons glyphicons-riflescope').css({fontSize: '16px'});
                                node.css({position: 'relative'}).append(dot.append(sp));
                                var pvm = options.data.packedVulnerabilityMatrix;
                                if(pvm){
                                    dot.attr('data-packed', pvm);
                                }
                            }
                            var x = ((w/10)*ia);
                            var y = ((h/10)*ea);
                            var dw = dot.width();
                            var dh = dot.height();
                            var xd = (dw/2)+((ia===5) ? 3: 0);
                            var yd = (dh/2)+((ea===5) ? 2: 0);
                            x-=xd;
                            y-=yd;
                            x = Math.min(Math.max(x, ((dw/2)*-1)), w);
                            y = Math.min(Math.max(y, ((dh/2)*-1)), h);
                            dot.css({bottom: y+'px', left: x+'px', color: clr}).attr('title', String.format("{0} x: {1} y: {2}", tooltip, ia, ea));
                        }
                        if(!node.jNodeReady("state")){
                            node.jNodeReady({onReady: function(){
                                var m = options.data.metrics.matrix;
                                var ia = m.impactAxis;
                                var ga = options.data.metrics.gaussian;
                                var ea = ga ? ga : m.exploitabilityAxis;
                                point(ia, ea, String.format("{0}_gaussian", state.ctrlId), "", '#31708f');
                                $(String.format('#hd_{0}',  state.ctrlId))
                                    .append(dCrt('div').attr('title', 'Mission Impact').html(String.format("MI: {0}", ia)))
                                    .append(dCrt('div').attr('title', 'Exploitability').html(String.format("EX: {0}", ea)));
                            } });
                        }
                    }
                }
            }
        };

        //environment: Initialize
        methods.init();
    };

    $.jVulnMatrix.getCoordinates = function(options){
        var y = (options.metrics) ? Math.floor(Math.round(options.metrics.matrix.likelihoodAxis/2)) : 5;
        var x = (options.metrics) ? Math.floor(Math.round(options.metrics.matrix.impactAxis/2)) : 5;

        var conversion = [{y: 5, c: 2}, {y: 4, c: 3}, {y: 3, c: 4},{y: 2, c: 5}, {y: 1, c: 6}];

        $.each(conversion, function(){
            if(this.y===y){
                y=this.c;
                return false;
            }
        });

        y = (y<=0) ? 1 : y>5 ? 5:y;
        x = (x<=0) ? 1 : x>5 ? 5:x;

        return {x: x, y: y};
    };

    //Default Settings
    $.jVulnMatrix.defaults = {
        badged: true,
        offsetWidth: 0,
        limit: 5,
        paneled: true,
        metric: {
            impactAxis: 5,
            likelihoodAxis: 5,
            severity: 'critical',
            score: 25
        }
    };

    //Plugin Function
    $.fn.jVulnMatrix = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === 'object') {
            return this.each(function() {
                new $.jVulnMatrix($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $jVulnMatrix = $(this).data('jVulnMatrix');
            switch (method) {
                case 'state':
                default: return $jVulnMatrix;
            }
        }
    };

})(jQuery);