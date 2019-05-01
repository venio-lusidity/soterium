;(function ($) {
    //Object Instance
    $.pSummary = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.pSummary.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.current ={
            et_view: 'ditpr'
        };
        state.details ={
            asset: 'asset',
            count: 'count',
            enclave: 'enclave',
            iavm: 'iavm',
            ranked: 'ranked',
            risk: 'risk',
            top: 'top',
            vuln: 'vuln'
        };
        state.callbacks = [];

        var _aUrl = '/pages/enclaves/assets/index.html';
        var _vUrl = '/pages/enclaves/vuln/index.html';
        var _iUrl =  '/pages/iavms/iavm/index.html';
        var _iaUrl =  '/pages/iavms/assets/index.html';
        
        // Store a reference to the environment object
        el.data("pSummary", state);

        // Private environment methods
        methods = {
            init: function() {
                if(state.opts.et_view && state.opts.et_view==='ditprId'){
                    state.opts.et_view = 'ditpr';
                }
                state.worker.node.attr('data-valid', true).show();

                state.current.item = $.extend({}, state.worker.data, true);
                state.current.isAsset = $.jCommon.string.contains(state.current.item.vertexType, "network/asset");
                state.current.isOrg = $.jCommon.string.contains(state.current.item.vertexType, "organization");
                state.current.isLoc = $.jCommon.string.contains(state.current.item.vertexType, "location");
                state.current.isSys = $.jCommon.string.contains(state.current.item.vertexType, "enclave");

                var url = $.jCommon.url.create(window.location.href);
                if(!state.opts.data){
                    state.opts.data = {};
                }
                if (url.hasParam("et_view")) {
                    state.opts.et_view = url.getParameter("et_view");
                }
                if(state.opts.et_view==="loc" || state.current.isLoc){
                    state.opts.et_view="location";
                }

                if (url.hasParam("et_exact")) {
                    state.opts.data.et_exact = $.jCommon.string.equals(url.getParameter("et_exact"), "true", true);
                }
                if(!state.opts.data.et_exact){
                    state.opts.data.et_exact=false;
                }
                state.current.et_exact = $.jCommon.string.equals(state.opts.data.et_exact.toString(), "true", true);
                state.current.et_view = state.opts.et_view;
                if(!state.opts.et_view) {
                    if (state.current.isOrg) {
                        state.opts.et_view = 'managed';
                    }
                    else {
                        state.opts.et_view = 'ditpr';
                    }
                }
                state.current.et_view = state.opts.et_view;

                if(!state.current.et_exact && state.current.isAsset){
                    state.current.et_exact = true;
                    state.opts.data.et_exact = true;
                }

                methods.resize.init();

                methods.content.init();

                lusidity.environment('onResize', function () {
                    methods.resize.init();
                });
            },
            resize: {
                init: function () {
                    var h = state.worker.node.availHeight(state.opts.adjustHeight);
                    dHeight(state.worker.node, h, h, h);
                    state.worker.node.css({overflowX: 'hidden', overflowY: 'auto'});
                    $.each(state.callbacks, function () {
                        if($.isFunction(this)){
                          this();
                        }
                    });
                },
                register: function (callback) {
                    state.callbacks.push(callback);
                }
            },
            getFilters: function (d, data) {
                try {
                    d.prefixTree = data.prefixTree ? data.prefixTree : state.current.et_view === 'ditpr' ? data.prefixDitpr : data.prefixTree;
                    d.prefixKey = data.prefixKey ? data.prefixKey : methods.getPrefixKey();
                    d.filters = state.opts.data.filters ? $.jCommon.array.clone(state.opts.data.filters) : [];
                    d.groups = state.opts.data.groups ? $.jCommon.array.clone(state.opts.data.groups) : [];
                    if (state.opts.groups) {
                        $.each(state.opts.groups, function () {
                            if (!$.jCommon.array.contains(d.filters, this, 'fKey')) {
                                d.groups.push(this);
                                var val = this.fValKey ? this[this.fValKey] : this.value;
                                d.filters.push({
                                    fKey: (!this.fKey ? this.key : this.fKey),
                                    value: val,
                                    type: this.type
                                });
                            }
                        });
                    }
                }
                catch (e){}
            },
            getDownloadUrl: function (rPath) {
                var hd = lusidity.environment('host-download');
                if($.jCommon.string.endsWith(hd, "/")){
                    hd = $.jCommon.string.stripEnd(hd);
                }
                if($.jCommon.string.endsWith(hd, "/svc")){
                    hd = hd.substring(0, hd.length-4);
                }
                return hd+rPath;
            },
            getDownloadGlyph:function(callback){
                var r = dCrt("span");
                var ldr = dCrt("img").css({
                    height: '16px',
                    width: '16px'
                }).attr('src', '/assets/img/loading.gif').hide();
                var glyph = dCrt('span').addClass('glyphicon glyphicon-download-alt').css({
                    marginLeft: '5px',
                    fontSize: '12px',
                    position: 'relative',
                    top: '1px',
                    color: '#337ab7'
                }).attr("data-toggle","tooltip").attr("data-placement", "top").attr("data-container", "body").attr("title", 'Export data to an Excel spreadsheet, do not navigate away from this page until the download has started.');
                glyph.tooltip();
                glyph.css({cursor: 'pointer'});
                glyph.on('click', function () {
                    if($.isFunction(callback)){
                        ldr.show();
                        glyph.hide();
                        callback(glyph, ldr);
                    }
                });
                return r.append(glyph).append(ldr);
            },
            exists: function (node) {
                return (node && (node.length>0));
            },
            getEtv: function(){
                var t;
                switch (state.current.et_view) {
                    case 'ditpr':
                        t = "System Name";
                        break;
                    case 'managed':
                        t = "Managed By";
                        break;
                    case 'owned':
                        t = "Owned By";
                        break;
                    case 'location':
                        t = "Located In";
                        break;
                    case 'asset':
                        t = "Asset";
                        break;
                    default:
                        t = "Unknown";
                        break;
                }

                if(!state.current.isAsset && state.current.et_exact){
                    t = String.format('Directly {0}', t);
                }

                return t;
            },
            getPrefixKey: function () {
                var r;
                switch (state.current.et_view){
                    case 'owned':
                        r='prefixOwned';
                        break;
                    case 'managed':
                        r='prefixManaged';
                        break;
                    case 'location':
                        r='prefixLocation';
                        break;
                    default:
                        r='prefixDitpr';
                        break;
                }
                return r;
            },
            content: {
                noAuth: function () {
                    state.worker.node.children().remove();
                    var c = dCrt('div');
                    var hd = dCrt('h4').html("Sorry, you are not authorized to view..").addClass('letterpress');
                    var t = dCrt('h4').html(state.current.item.title).addClass('letterpress');
                    c.append(hd).append(t);
                    state.worker.node.append(c);
                    var d = $.jCommon.element.getDimensions(c);
                    c.css({position: 'absolute', top: '50%', left: '50%', marginTop: ((d.h/2)*-1)+'px', marginLeft: ((d.w/2)*-1)+'px', textAlign: 'center'});
                },
                soon: function () {
                    state.worker.node.children().remove();
                    var h = state.worker.node.availHeight(0);
                    state.worker.node.height(h-50);
                    var c = dCrt('div');
                    var hd = dCrt('h4').html("Coming soon..").addClass('letterpress');
                    c.append(hd);
                    state.worker.node.append(c);
                    var d = $.jCommon.element.getDimensions(c);
                    c.css({position: 'absolute', top: '50%', left: '50%', marginTop: ((d.h/2)*-1)+'px', marginLeft: ((d.w/2)*-1)+'px', textAlign: 'center'});
                },
                noData: function (node) {
                    node.children().remove();
                    var c = dCrt('div');
                    node.append(c);
                    var hd = dCrt('h4').html("No findings.").addClass('letterpress').css({margin: '20px 10px 0 10px'});
                    c.append(hd);
                },
                ready: function (data) {
                    var r = true;$.each(state.details, function (key, value) {if(undefined===data._sd[value]){r = false;}return r;});return r;
                },
                init: function () {
                    if (!methods.exists(state.worker.node)) {
                        return false;
                    }
                    state.worker.node.children().remove();
                    var s = function (data) {
                        if (!data || !data.authorized) {
                           methods.content.noAuth();
                        }
                        else {
                           methods.content.load();
                        }
                    };
                    var url = methods.content.smry.url('auth', state.current.item);
                    $.htmlEngine.request(url, s, s, null, "get", true);
                },
                load: function() {
                    if (!state.current.item._sd) {
                        state.current.item._sd = {};
                    }

                    var pnls = state.opts.panels;
                    var all = pnls.findings && pnls.topFindings && pnls.risk;

                    if (all) {
                        var b = dCrt('div').addClass('blue').css({margin: '0 0'});
                        state.worker.node.append(b);


                        if (pnls.breadcrumb) {
                            methods.content.smry.b(state.current.item, b);
                        }

                        if (pnls.title) {
                            var hContent = dCrt('div').addClass('blue').css({padding: '10px 10px', margin: '0 0', position: 'relative'});
                            state.worker.node.append(hContent);

                            var h = dCrt('h4').css({marginRight: "100px", position: 'relative'});
                            hContent.append(h);

                            var hl = dCrt('a').attr('href', state.current.item[state.KEY_ID] + '?et_view=' + state.current.et_view).attr('target', '_blank').html(state.current.item.title);
                            var type = methods.getEtv();

                            var tl = dCrt('span').html(type + ': ');
                            h.append(tl).append(hl);

                            if(!state.current.isAsset){
                                $.login.authorized({"groups": ["testers"], "r": false},function (data) {
                                    if (!data.auth) {
                                        return false;
                                    }
                                    var div1 = dCrt('div')
                                        .attr('title', 'Create a custom report.')
                                        .css({overflow: 'hidden', position: "absolute", top: '11px', right: '30px', cursor: 'pointer', fontSize: "14px", color: "#000000"});
                                    state.opts.adhocLnk = dLink('', '', state.current.item[state.KEY_ID]).css({color: 'inherit'});
                                    var img1 = dCrt('span').addClass('glyphicons glyphicons-file-import').css({marginRight: '5px', fontSize: '24px'});
                                    state.opts.adhocLnk.append(img1);
                                    div1.append(state.opts.adhocLnk);
                                    hContent.append(div1);
                                });

                                var div2 = dCrt('div')
                                    .attr('title', 'Click to download a summary of this data as a PDF')
                                    .css({overflow: 'hidden', position: "absolute", top: '16px', right: '5px', cursor: 'pointer', fontSize: "14px", color: "#000000"});
                                var lnk2 = dCrt('span');
                                var img2 = dCrt('span').addClass('filetypes filetypes-pdf').css({marginRight: '5px', fontSize: '24px'});
                                lnk2.append(img2);
                                div2.append(lnk2);
                                hContent.append(div2);

                                lnk2.on('click', function () {
                                    var ldr = dCrt('img').css({
                                        height: '16px',
                                        width: '16px'
                                    }).attr('src', '/assets/img/loading.gif');
                                    lnk2.hide();
                                    div2.append(ldr);
                                    var url = String.format('{0}{1}/pdf/executive?view={2}&exact={3}&_nocache={4}', lusidity.environment('host-primary'), state.current.item[state.KEY_ID], state.current.et_view, state.current.et_exact, $.jCommon.getRandomId('c'));

                                    var s = function (nData) {
                                        ldr.remove();
                                        if (nData) {
                                            var u = String.format('{0}{1}', lusidity.environment('host-primary'), nData.url);
                                            var org = u;
                                            u = $.jCommon.string.replaceAll(u, "/svc/", "/") + "?nocache=" +  $.jCommon.getRandomId('c');

                                            var req = new XMLHttpRequest();
                                            req.open("GET", u);
                                            req.responseType = "blob";
                                            req.onload = function (event) {
                                                var blob = req.response;
                                                var link=document.createElement('a');
                                                link.href=window.URL.createObjectURL(blob);
                                                link.download = $.jCommon.string.getLast(org, "/");
                                                link.click();
                                            };
                                            req.send();
                                        }
                                        lnk2.show();
                                    };
                                    $.htmlEngine.request(url, s, s, null, 'get', true, 0);
                                });
                            }

                            if (state.opts.groups && state.opts.groups.length > 0) {
                                var grp = dCrt('div').css({position: 'relative'});
                                hContent.append(grp.append(dCrt('span').html("Groupings:&nbsp;")));
                                var lst;
                                $.each(state.opts.groups, function () {
                                    grp.append(dCrt('div').css({display: 'inline-block'}).append(dCrt('span').attr('data-url', this.url).html(this.extValue ? this.extValue : this.value).attr('title', this.label)));
                                    lst = dCrt('div').css({
                                        position: 'relative',
                                        display: 'inline-block',
                                        fontWeight: 'bold',
                                        margin: '0 2px 0 2px',
                                        top: '-1px'
                                    }).html('>');
                                    grp.append(lst);
                                });
                                lst.remove();
                            }
                        }
                    }

                    var c1;
                    var c2;
                    var c3;

                    if (all) {
                        var r1 = dCrt('div').addClass('row');
                        state.worker.node.append(r1);

                        // Problem could be that the location doesn't exist yet.

                        c1 = dCrt('div').addClass('col-md-4');
                        c2 = state.opts.c2 ? state.opts.c2 : dCrt('div').addClass('col-md-5').css({
                            position: 'relative',
                            minHeight: '200px'
                        });
                        c3 = state.opts.c3 ? state.opts.c3 : dCrt('div').addClass('col-md-3');
                        r1.append(c1).append(c2).append(c3);
                    }
                    else {
                        c1 = state.worker.node;
                        c2 = c1;
                        c3 = c1;
                        if(!state.opts.calcHeights){
                            c1.css({height: '', minHeight:'',maxHeight:''});
                        }
                    }

                    $.htmlEngine.busy(state.worker.node, {type: 'cube', cover: true, adjustWidth: 0, adjustHeight: 0});

                    state.current.item.ranked = {};
                    state.current.item._sd.top = {};

                    methods.content.stats(c1, state.current.item, function () {
                        state.worker.node.loaders('hide');

                        window.setTimeout(function () {
                            if (pnls.topFindings) {
                                $.htmlEngine.busy(c2, {
                                    type: 'cube',
                                    cover: false,
                                    adjustWidth: 0,
                                    adjustHeight: 0
                                });

                                methods.content.smry.detail(state.current.item, state.details.ranked, function () {
                                    methods.content.smry.bar2(c2);
                                    c2.loaders('hide');
                                }, true);
                            }
                            if (pnls.risk) {
                                $.htmlEngine.busy(c3, {
                                    type: 'cube',
                                    cover: false,
                                    adjustWidth: 0,
                                    adjustHeight: 0
                                });
                                methods.content.smry.detail(state.current.item, state.details.risk, function () {
                                    methods.content.smry.r(state.current.item, c3);
                                    c3.loaders('hide');
                                }, true);

                                var rc2 = dCrt('div').addClass('row');
                                state.worker.node.append(rc2);

                                if (state.opts.cascade && !state.current.et_exact) {
                                    var rc3 = dCrt('div').addClass('row');
                                    state.worker.node.append(rc3);
                                    methods.content.subs(rc3, state.current.item);
                                }
                            }
                        }, 300);
                    });
                },
                links: function(node){
                    var d = dCrt('div').css({lineHeight: '20px', margin: '5px 8px'});
                    var s = dCrt('span').addClass('glyphicon glyphicon-new-window').html("&nbsp;");
                    var url = state.current.item[state.KEY_ID]
                        + '/export/excel'
                        + '?category=expired_scans'
                        + '&domain=' + state.current.item.vertexType;
                    var link = dCrt('div').addClass('link').append(s).append("Export Asset Expired Scans Report");
                    link.css({cursor: 'pointer', textDecoration: 'underline'});
                    d.append(link);
                    link.on('click', function(){
                        var s = function (data) {
                            lusidity.info.yellow('Your file request has been submitted and you will receive an email notification' +
                                ' and a dashboard notification when the file is ready.  Your document will be processed in the order received.');
                            lusidity.info.show(20);
                        };
                        var f = function () {
                            lusidity.info.red('We are sorry, something went wrong while trying to submit your request' +
                                ' for a document, please try again.  If you get this error again please use the' +
                                ' blue feedback button to report the error.');
                            lusidity.info.show(20);
                        };
                        $.htmlEngine.request(url, s, f, null);
                    });
                    node.append(d);
                },
                stats: function (node, data, callback) {
                    if(state.opts.panels.findings) {
                        if (!data._sd) {
                            data._sd = {}
                        }

                        if (!data._sd.asset) {
                            data._sd.asset = {};
                        }

                        var ul = dCrt('ul').addClass('list-group').css({margin: '5px 5px'});
                        node.append(ul);

                        data._sd.enclave = {count: 0};
                        var ul2 = dCrt('li').addClass('list-group-item list-sum-item no-radius');
                        ul.append(ul2);
                        var ul3 = dCrt('li').addClass('list-group-item list-sum-item no-radius');
                        ul.append(ul3);
                        var ul4 = dCrt('li').addClass('list-group-item list-sum-item no-radius');
                        ul.append(ul4);

                        methods.content.smry.detail(data, state.details.count, function () {
                            methods.content.smry.a(data, ul2);
                        }, false);
                        methods.content.smry.detail(data, state.details.vuln, function () {
                            methods.content.smry.v(data, ul3);
                        }, true);
                        methods.content.smry.detail(data, state.details.iavm, function () {
                            methods.content.smry.i(data, ul4);
                        }, true);
                    }
                    if($.isFunction(callback)){
                        callback();
                    }
                },
                subs: function (node, data) {
                    $.htmlEngine.busy(node, {type: 'cube', cover: true, adjustWidth: 0, adjustHeight: 0});

                    var c1 = dCrt('div').addClass('col-md-6');
                    var c2 = dCrt('div').addClass('col-md-6');
                    node.append(c1).append(c2);

                    function make(item, on) {
                        var c = (on===0) ? c1 : c2;
                        var pnl = dCrt('div').css({marginBottom: '5px'});
                        c.append(pnl);
                        var body = $.htmlEngine.panel(
                            pnl, 'glyphicons glyphicons-git-branch', item.title, String.format('{0}?et_view={1}', item[state.KEY_ID], state.current.et_view), true /* borders */
                        );
                        var r = false;
                        methods.content.stats(body, item);
                        if($.jCommon.json.hasProperty(item._sd, "count.count.inherited")) {
                            r = $.jCommon.is.numeric(item._sd.count.count.inherited) && item._sd.count.count.inherited > 0;
                        }
                        if(!r){
                            pnl.remove();
                        }
                        return r;
                    }

                    var q;
                    if(state.current.et_view === 'location'){
                        q = QueryFactory.childLocations(data);
                    }
                    else if(state.current.et_view === 'ditpr'){
                        q =QueryFactory.ditpr.children(data);
                    }
                    else{
                        q = QueryFactory.childOrganizations(data);
                    }

                    var s = function (data) {
                        if(data && data.results){
                            var on=0;
                            $.each(data.results, function () {
                                if(make(this, on)) {
                                    on++;
                                    if (on > 1) {
                                        on = 0;
                                    }
                                }
                            });
                        }
                        node.loaders('hide');
                    };
                    var f= function () {
                        node.loaders('hide');
                    };
                    $.htmlEngine.request('/query?limit=1000', s, f, q, 'post');
                },
                make: function (node, data) {
                    var c1 = dCrt('div').addClass('col-md-12');
                    c1.css({minHeight: '600px', height: '600px'});
                    node.append(c1);
                    c1.jEnclavesTable({item: data, et_view: state.current.et_view});
                },
                reset: function () {
                    methods.menu.add.init();
                    methods.menu.edit.init();
                    if(methods.exists(state.worker.node)){
                        state.worker.node.children().remove();
                        state.worker.node.show();
                    }
                },
                color: function (severity) {
                    var cl;
                    var cd;
                    switch (severity) {
                        case 1:
                        case 2:
                            cl = '#d9534f';
                            cd = '#C5524E';
                            break;
                        case 3:
                            cl = '#ff7e2e';
                            cd = '#EB782E';
                            break;
                        case 4:
                        case 5:
                            cl = '#f0ab36';
                            cd = '#DC9735';
                            break;
                        case 6:
                        case 7:
                            cl = '#ecf028';
                            cd = '#E2E627';
                            break;
                        default:
                            cl = '#45a9e4';
                            cd = '#249AE4';
                            break;
                    }

                    return {cl: cl, cd: cd};
                },
                smry:{
                    url: function (detail, data) {
                        return String.format('{0}/hierarchy/details?detail={1}&view={2}&exact={3}&_nocache={4}', (data ? data[state.KEY_ID] : state.current.item[state.KEY_ID]), detail, state.current.et_view, state.current.et_exact, $.jCommon.getRandomId('c'));;
                    },
                    chart: function (node) {
                        if (state.current.item._sd.vuln) {
                            var item = state.current.item._sd.vuln;

                            var container = dCrt('div');
                            node.append(container);
                            var ctx = dCrt('canvas').attr('id', "severity_chart");
                            container.append(ctx);

                            var c = item.catI+item.critical;
                            var h = item.catII+item.high;
                            var m = item.catIII+item.medium;
                            var l = item.low;

                            var data = {
                                labels: [
                                    "CAT I/Critical",
                                    "CAT II/High",
                                    "CAT III/Medium",
                                    "Low"
                                ],
                                datasets: [
                                    {
                                        data: [c, h, m, l],
                                        backgroundColor: [
                                            "#d9534f",
                                            "#f0ab36",
                                            "#ecf028",
                                            "#49b26f"
                                        ],
                                        hoverBackgroundColor: [
                                            "#d9534f",
                                            "#f0ab36",
                                            "#ecf028",
                                            "#49b26f"
                                        ]
                                    }]
                            };

                            var myPieChart = new Chart(ctx, {
                                type: 'pie',
                                data: data,
                                options: {
                                    animateScale: true
                                }
                            });
                        }
                    },
                    legend: function (node, popover) {
                        var c = dCrt('div').css({width: '60%'});
                        var ul = dCrt('ul').addClass('list-group').css({margin: '5px 5px', fontSize: '12px'});
                        c.append(dCrt('div').append(ul));

                        var key =[{l: 'CAT I/Critical', w: 1}, {l: 'High', w: 3}, {l: 'CAT II/Medium', w: 5}, {l: 'CAT III/Low', w: 7}, {l: 'Info', w: 8}];
                        $.each(key, function () {
                            var clr = methods.content.color(this.w);
                            var li = dCrt('li').addClass('list-group-item list-sum-item no-radius').css({padding: '2px 2px'});
                            var sp = dCrt('span').addClass('badge').css({marginLeft: '10px', backgroundColor: clr.cl, fontSize: '10px' }).html('&nbsp;');
                            li.append(sp).append(this.l);
                            ul.append(li);
                        });

                        if(popover) {
                            var glyph = $.htmlEngine.glyph('glyphicons glyphicons-asterisk').css({
                                postion: 'relative',
                                top: '0',
                                fontSize: '10px'
                            });
                            var leg = dCrt('div').css({
                                position: 'relative',
                                display: 'inline-block',
                                top: '4px',
                                marginLeft: '-4px',
                                cursor: 'pointer'
                            }).html("Legend...");
                            node.css({marginTop: '10px'}).append(glyph).append(leg);
                            leg.popover({
                                placement: 'top',
                                trigger: 'hover',
                                animated: true,
                                html: true,
                                content: function(){
                                    return c.html()
                                },
                                title: 'Legend'
                            });
                        }
                        else{
                            var header = dCrt('h5').addClass('default-grey').css({padding: '10px', marginBottom: '10px'}).html('Legend');
                            node.append(header).append(c);
                        }
                    },
                    bar2: function (node) {
                        if(node.loaders('exists')){
                            node.loaders('hide');
                        }
                        if(!state.modalInfo) {
                            state.modalInfo = dCrt('div').css({
                                position: 'absolute',
                                top: '200px',
                                width: '0',
                                height: '0'
                            });
                            lusidity.append(state.modalInfo);
                        }
                        if (state.current.item._sd.ranked && state.current.item._sd.ranked.results) {
                            var values = state.current.item._sd.ranked.results;
                            node.attr('id', 'top_findings');
                            var header = dCrt('h5').addClass('default-grey').css({padding: '10px', marginBottom: '10px'}).html("Top Findings");
                            node.append(header);

                            var container = dCrt('div').attr('id', "bar_vuln_stat_chart");
                            node.append(container);

                            function make() {
                                container.children().remove();

                                var legend = dCrt('div');
                                var chart = dCrt('div').css({fontSize: '12px'});
                                container.append(chart).append(legend);
                                methods.content.smry.legend(legend, false);
                                var on = 0;
                                var sorted = $.jCommon.array.sort(values, [{property: "weight", asc: false}]);
                                on = 0;
                                var max = state.current.isAsset ? 1 : state.current.item._sd.count.count.inherited;
                                $.each(sorted, function () {
                                    var item = this;
                                    var id = (item.vuln.relatedId ? item.vuln.relatedId : item.vuln[state.KEY_ID]);
                                    var t = $.jCommon.string.replaceAll(this.vuln.vulnId.toUpperCase(), '_RULE', '');
                                    if ($.jCommon.string.contains(id, "security_center")) {
                                        t = "SCP-" + t;
                                    }
                                    var sv = dLink(t, id).css({color: 'white'});
                                    sv.on('click', function (e) {
                                        e.stopPropagation();
                                    });
                                    var o = dCrt('div').css({
                                        margin: '2px 0 5px 0',
                                        width: '100%',
                                        cursor: state.current.isAsset ? "help" : "pointer"
                                    }).addClass('med-grey box-shadow');
                                    var bk = methods.content.color(item.severity);
                                    var i = dCrt('div').css({
                                        width: '0',
                                        whiteSpace: 'nowrap',
                                        margin: '2px 0 2px 0',
                                        padding: '5px 5px 5px 5px',
                                        textAlign: 'left',
                                        minHeight: '32px',
                                        height: '32px',
                                        borderColor: bk.cd,
                                        backgroundColor: bk.cl
                                    });

                                    var s1 = dCrt('div').css({
                                        position: 'relative',
                                        top: '2px',
                                        fontSize: '12px',
                                        display: 'inline-block'
                                    }).append(sv);
                                    var s2 = dCrt('div').css({
                                        position: 'relative',
                                        top: '1px',
                                        display: 'inline-block'
                                    });
                                    var s3 = dCrt('div').css({
                                        position: 'absolute',
                                        top: '8px',
                                        fontSize: '12px',
                                        right: '5px'
                                    }).attr('title', 'This represents the number of assets that has this finding.').html($.jCommon.number.commas(item.rank));

                                    var ot = t;
                                    if (item.vuln.severity) {
                                        ot = String.format("{0}: {1}", ot, FnFactory.getSeverityLabel(item.vuln.severity, item.vuln.vertexType));
                                    }
                                    o.css({position: 'relative', color: 'white'}).attr('title', ot);
                                    i.append(s3).append(s1).append(s2);
                                    chart.append(o.append(i));

                                    function move() {
                                        var p = ($.jCommon.number.percentage(max, item.rank) / 100);
                                        var tw = i.parent().width();
                                        var iw = tw * p;
                                        i.animate({width: state.current.isAsset ? '100%' : iw + 'px'}, function () {
                                            var sw = (s1.width() + s2.width() + 5);
                                            var dif = 5;
                                            var w = ((sw > iw) ? (sw + 5) : iw) + dif;
                                            if ((w + s3.width() + 10) >= tw) {
                                                s3.css({left: '', right: '5px'})
                                            }
                                            else {
                                                var p = i.parent().width();
                                                s3.css({right: '', left: w + 'px'});
                                            }
                                        });
                                    }

                                    window.setTimeout(move, 500);
                                    var s = function (data) {
                                        var ctx = dCrt('div').css({minWidth: '400px'});
                                        ctx.append(dCrt('div').html('Click the bar for more information.'));
                                        ctx.append(dCrt('div').html(String.format('Weight: {0}<br/><br/>', item.weight)));
                                        if (data && data.results && data.results.length > 0) {
                                            var node = s2;
                                            if (data.results.length > 1) {
                                                s2.append(dCrt('span').html('( IAVMs... )')).css({marginLeft: '5px'});
                                                node = ctx;
                                            }
                                            node.append(dCrt('span').html('(')).css({marginLeft: '5px'});
                                            var on = 0;
                                            $.each(data.results, function () {
                                                var a = dCrt('a').attr('href', this[state.KEY_ID]).attr('target', '_blank').html(this.iavmNoticeNumber).css({
                                                    fontSize: '12px',
                                                    color: data.results.length > 1 ? '#333':'white'
                                                });
                                                node.append(a);
                                                a.on('click', function (e) {
                                                    e.stopPropagation();
                                                });
                                                if (on > 0) {
                                                    a.css({marginLeft: '5px'});
                                                }
                                                on++;
                                            });
                                            node.append(dCrt('span').html(')')).css({
                                                marginLeft: '5px',
                                                fontSize: '12px'
                                            });
                                            window.setTimeout(move, 100);
                                        }
                                        var d = function (data) {
                                            if (data) {
                                                var desc = $.jCommon.json.getProperty(data, 'results.value');
                                                var p = dCrt('p').html($.jCommon.string.ellipsis(desc, 256));
                                                ctx.append(p);
                                            }
                                            var pos = o.offset();
                                            var ht = $(window).height();
                                            var av = ht-pos.top;
                                            var plc = (!state.current.isAsset && av)>=300 ? "bottom" : "top";

                                            o.popover({
                                                html: true,
                                                placement: 'left',
                                                animated: true,
                                                container: 'body',
                                                content: function () {
                                                    return ctx.html();
                                                }
                                            });
                                            o.on('mouseenter', function () {
                                                o.popover('show');
                                            });
                                            o.on('mouseleave', function () {
                                                o.popover('hide');
                                            });
                                        };
                                        $.htmlEngine.request(String.format('{0}/properties/system/primitives/raw_string/descriptions', id), d, d, null, 'get');
                                    };
                                    $.htmlEngine.request(String.format('{0}/properties/technology/security/iavm/iavms', id), s, s, null, 'get');

                                    i.on('mouseenter', function (e) {
                                        var bk = methods.content.color(item.severity);
                                        i.css({'cursor': state.current.isAsset ? 'help' : 'pointer', borderColor: bk.cl, backgroundColor: bk.cd});
                                    });

                                    i.on('mouseleave', function (e) {
                                        var bk = methods.content.color(item.severity);
                                        i.css({'cursor': 'default', borderColor: bk.cd, backgroundColor: bk.cl});
                                    });

                                    if(!state.current.isAsset) {
                                        o.on('click', function (e) {
                                            o.mouseout();
                                            state.modalInfo.pageModal({top: 100});
                                            state.modalInfo.pageModal('show', {
                                                glyph: '',
                                                hasClose: true,
                                                css: {margin: '20px 20px 20px 82px', paddingTop: '0'},
                                                width: $(window).width() - 118,
                                                onClose: function (e) {
                                                },
                                                header: function () {
                                                    var header = dCrt('div');
                                                    var t = state.current.item ? state.current.item.title : state.opts.data.title;
                                                    var ft = "Filter Type: All";
                                                    if (state.opts.groups) {
                                                        ft += " > ";
                                                        $.each(state.opts.groups, function () {
                                                            var grp = this;
                                                            ft += String.format("{0}: {1} > ", grp.label, grp.extValue ? grp.extValue : grp.value);
                                                        });
                                                        ft = ft.substr(0, ft.length - 3);
                                                    }
                                                    var ftd = dCrt('div').html(ft);
                                                    var u = state.current.item ? state.current.item[state.KEY_ID] : state.opts.data[state.KEY_ID];
                                                    var hContent = dCrt('h5').append(dLink(String.format("{0}: {1}", methods.getEtv(), t), u));
                                                    header.append(hContent).append(ftd);
                                                    return header;
                                                },
                                                body: function (body) {
                                                    body.children().remove();
                                                    var r1 = dCrt('div').addClass('row').css({
                                                        position: 'relative',
                                                        marginTop: '0px',
                                                        paddingTop: '0'
                                                    });
                                                    body.append(r1);
                                                    var n = ($(window).height() - 220) + 'px';
                                                    body.css({minHeight: n, height: n, maxHeight: n});
                                                    var vuln = item.vuln;
                                                    var vT = dLabel('Vuln', dLink((vuln.vuln ? vuln.vuln : vuln.title), id));
                                                    var vId = dLabel('Vuln Id', $.jCommon.string.replaceAll(vuln.vulnId, '_rule', ''));
                                                    var sev = dLabel('Severity', FnFactory.getSeverityLabel(vuln.severity, (vuln.vulnType ? vuln.vulnType : vuln[state.KEY_ID])));
                                                    var dt = (vuln.published ? vuln.published : (vuln.publishedOn ? vuln.publishedOn : null));
                                                    var pub = dLabel('Published', (dt ? $.jCommon.dateTime.dateOnly(dt) : "Unknown"));
                                                    var total = dLabel("Assets affected", item.rank);
                                                    var d1 = dCrt('div').addClass('col-md-12').append(vT).append(vId).append(sev).append(pub).append(total);

                                                    d1.children().css({marginRight: '5px'});
                                                    r1.append(d1);

                                                    var r2 = dCrt('row').addClass('row');
                                                    body.append(r2);
                                                    var d2 = dCrt('div').addClass('col-md-12 pSummary').css({
                                                        margin: '0',
                                                        padding: '0'
                                                    });
                                                    r2.append(d2);
                                                    item.params = state.opts.data;
                                                    var pd = {
                                                        title: vuln.title, vulnId: vuln.vulnId,
                                                        et_exact: state.current.et_exact
                                                    };
                                                    o.params = {};
                                                    o.params[state.KEY_ID] = state.current.item[state.KEY_ID];
                                                    o.params.et_view = state.current.et_view;
                                                    o.params.et_exact = state.current.et_exact;
                                                    methods.getFilters(pd, state.current.item);
                                                    var ah = d2.availHeight(348);
                                                    dMax(d2, ah);
                                                    window.setTimeout(function () {
                                                        ah -= 71;
                                                        d2.pVulnAssets({
                                                            title: 'Assets by Vulnerability',
                                                            glyph: 'glyphicons-list',
                                                            offset: {
                                                                parent: 0,
                                                                table: 0
                                                            },
                                                            minHeight: ah,
                                                            height: ah,
                                                            maxHeight: ah,
                                                            limit: 60,
                                                            data: pd
                                                        });
                                                    }, 300);
                                                }
                                            });
                                        });
                                    }
                                    on++;
                                    if (on >= 5) {
                                        return false;
                                    }
                                });
                            }
                            make();
                            methods.resize.register(make);
                        }
                    },
                    detail: function (item, detail, cback, async) {
                        if(item._sd && item._sd[detail]){
                            if($.isFunction(cback)){
                                cback();
                            }
                        }
                        else {
                            var s = function (data) {
                                if('sum'!==detail) {
                                    item._sd[detail] = (undefined === data) ? false : data;
                                }
                                if ($.isFunction(cback)) {
                                    cback(data);
                                }
                            };
                            var f = function (j, t, e) {
                                item._sd[detail] = false;
                                if ($.isFunction(cback)) {
                                    cback();
                                }
                            };
                            var grps = (state.opts.data && state.opts.data.groups) ? $.jCommon.array.clone(state.opts.data.groups) : [];
                            if(state.opts.groups){
                                $.each(state.opts.groups, function () {
                                   grps.push(this);
                                });
                            }
                            var m = (grps.length>0) ? 'post' : 'get';
                            $.htmlEngine.request(methods.content.smry.url(detail, item), s, f, (grps.length>0) ? grps : null, m, !async ? false : true, 60000);
                        }
                    },
                    node: function (label, value, css, link) {
                        var r = dCrt('div');
                        var n;
                        if(label){
                            n = dCrt('div').addClass('data-label').css({fontWeight: 'bold'});
                            n.append(String.format('{0}: ', label));
                            r.append(n);
                        }
                        if(!value){
                            value = 0;
                        }
                        n = dCrt('div').addClass('data-value');
                        n.append(value);
                        r.append(n);
                        if(css){
                            r.css(css);
                        }
                        if(link){
                            r.append(link);
                        }
                        return r;
                    },
                    top: function (data, node) {
                        var c = dCrt('div').css({margin: '0 15px 0 15px'});
                        node.append(c);

                        var header = dCrt('h5').addClass('default-grey').css({padding: '10px'}).html('Top 5');
                        c.append(header);
                        var grp = dCrt('div').addClass('list-group no-radius');
                        c.append(grp);
                        var on=0;
                        $.each(data.results, function () {
                            var i = this;
                            var l = dCrt('li').addClass('list-group-item list-sum-item no-radius').css({margin: '5px 5px'});
                            grp.append(l);
                            var b = dCrt('span').addClass('badge ' + i.severity.toLowerCase()).css({marginRight: '5px'}).attr('title', i.severity).html('&nbsp;');
                            l.append(b);
                            var t = dCrt('a').attr('href', i[state.KEY_ID]).attr('target', '_blank').html(i.title);
                            l.append(t);
                            var desc = $.jCommon.json.getProperty(i, '/system/primitives/raw_string/descriptions.results.value');
                            if(desc){
                                desc = $.jCommon.string.ellipsis(desc, 256, false);
                                var d = dCrt('p').html(desc).css({marginLeft: '5px'});
                                l.append(d);
                            }
                            on++;
                            if(on===5){
                                return false;
                            }
                        });
                    },
                    a: function (data, node){
                        if(node.loaders('exists')) {
                            node.loaders('hide');
                        }
                        if(state.current.isAsset){
                            node.hide();
                        }
                        if (!data._sd.count) {
                            var n = methods.content.smry.node("Assets", '0 [0]');
                            node.append(n);
                            return false;
                        }
                        var c = data._sd.count.count;
                        var lk1 = (c.inherited>0);

                        var cin = $.jCommon.number.commas(c.inherited);
                        var cun = $.jCommon.number.commas(c.exact);
                        var cai;
                        try {
                            cai = data._sd.count.count.avail.inherited;
                        }
                        catch (e){cai=0;}
                        var d = {mvId: data._sd.count.mvId, title: data.title, vertexType: data.vertexType, filter: 'all_details', detail: state.details.asset, et_view: state.current.et_view};

                        d.ditprId = data.ditprId;
                        d[state.KEY_ID] = data[state.KEY_ID];
                        d.et_exact = state.current.et_exact;
                        d.count = cin;
                        d.cai = cai;
                        d.ci = c.inherited;
                        d._count = c;
                        d.treed = state.opts.treed;
                        methods.getFilters(d, data);
                        if(data._sd.count.asOf){
                            d.asOf = data._sd.count.asOf;
                        }
                        var lsId = $.jCommon.getRandomId("pSum");
                        var param = $.jCommon.storage.setItem(lsId, d, true);
                        if(param){
                            lsId = param;
                        }
                        var span = dCrt('span');
                        var link = dCrt((lk1 ? 'a' : 'span')).attr('href', String.format("{0}?d={1}", _aUrl,  lsId)).attr('target', '_blank').html(cin);
                        if(!state.opts.adhocLnk.attr('href')) {
                            state.opts.adhocLnk.attr('href', String.format("/pages/reports/adhoc/index.html?d={0}", lsId));
                        }

                        if(lk1){
                            link.attr('title', "Represents how many assets are associated with this system.");
                        }
                        var vb = state.current.isOrg ? 'organizational' : 'location';
                        if(!state.current.isSys && lk1) {
                            link.attr('title', 'Represents how many assets are assigned to this ' + vb + ' level and all subordinates. ');
                        }
                        if(data.ditprId){
                            var ditpr = dCrt('span').html(data.ditprId);
                            node.append(methods.content.smry.node("DITPR ID", ditpr).attr('title', "Referenced by HBSS Operation Attributes file."));
                        }
                        if(data.coamsId || data.nameId){
                            var cid = dCrt('span').html(data.nameId ? data.nameId : data.coamsId);
                            node.append(methods.content.smry.node("COAMS ID", cid).attr('title', "Referenced by HBSS Operation Attributes file."));
                        }
                        if(cai>c.inherited){
                            node.append(dCrt('div').html(String.format("Working with {0} Scoped Assets of {1} found", $.jCommon.number.commas(cin), $.jCommon.number.commas(cai))));
                        }
                        else{
                            node.append(dCrt('div').html(String.format("{0} Assets found", $.jCommon.number.commas(cai))));
                        }
                        span.append(link);
                        var grouped = (state.opts && state.opts.groups && state.opts.groups.length || state.opts.data && state.opts.data.groups && state.opts.data.groups.length>0);
                        if(!state.current.isSys && !grouped && cin!==cun) {
                            var lk2 = (cun>0);
                            var hl = String.format('{0}?et_view={1}&et_exact=true', data[state.KEY_ID], state.current.et_view);
                            var l2 = dCrt(lk2 ? 'a' : 'span').css({marginLeft: '5px'}).attr('href', hl).attr('target', '_blank').html(String.format('[{0}]', cun));
                            l2.attr('title', 'Represents how many assets are directly assigned to this ' + vb + ' level.');
                            span.append(l2);
                        }
                        var glyph = methods.getDownloadGlyph(function (glyph, ldr) {
                            var key = data[state.KEY_ID];
                            var detail =  "csv";
                            var view = d.et_view;
                            var filter = d.filter;
                            var ex = false;
                            var url = lusidity.environment('host-download') + key + String.format('/hierarchy/details?detail={0}&view={1}&filter={2}&exact={3}&start=0&limit=1000000', detail, view, filter, state.current.et_exact);

                            var s = function (nData) {
                                if (nData) {
                                    var u = methods.getDownloadUrl(nData.url);
                                    window.location.assign(u);
                                }
                                if(ldr) {
                                    ldr.hide();
                                    glyph.show();
                                }
                            };
                            $.htmlEngine.request(url, s, s, d.groups, 'post');
                        });
                        span.append(glyph);
                        node.append(methods.content.smry.node("Assets", span));
                    },
                    b: function (data, node) {
                        if(!state.opts.panels.breadcrumb){
                            return false;
                        }
                        var s = function (data) {
                            if(data && data.results){
                                var ol = dCrt('ol').addClass('breadcrumb no-radius').css({margin: '0 0', fontSize: '12px'});
                                node.append(ol);
                                var on = 0;
                                $.each(data.results, function () {
                                    if(on>0) {
                                        var item = this;
                                        var l = dCrt('li');
                                        var a = dCrt('a').attr("href", String.format('{0}?et_view={1}', item[state.KEY_ID], state.current.et_view)).attr('target', '_blank').html(item[state.KEY_TITLE]);
                                        ol.append(l.append(a));
                                    }
                                    on++;
                                });
                            }
                        };
                        var f = function () {};
                        $.htmlEngine.request(state.current.item[state.KEY_ID]+'/breadcrumb',s,f,null);
                    },
                    e: function (data,node){
                        var n = methods.content.smry.node("Enclaves", 'placeholder');
                        node.append(n);
                    },
                    i: function (data,node){
                        if(node.loaders('exists')) {
                            node.loaders('hide');
                        }
                        if(!data._sd.iavm){
                            var n = methods.content.smry.node("IAVMs", '0');
                            node.append(n);
                            return false;
                        }
                        var perc = (data._sd.iavm.passed/data._sd.iavm.total).toFixed(2);
                        perc = (perc*100).toFixed(0);
                        var n = methods.content.smry.node("IAVMs", ($.jCommon.is.numeric(perc) ? perc : 0) +'%');
                        node.append(n);

                        function make(lbl, num, d, isDiv, dwnld) {
                            var r = $.isNumeric(num)?num: 0;
                            var n = $.jCommon.number.commas(r);
                            if(r>0 && d) {
                                d.title = data.title;
                                d.ditprId = data.ditprId;
                                d.detail = state.details.asset;
                                d.et_view = state.current.et_view;
                                d[state.KEY_ID] = data[state.KEY_ID];
                                d.count = num;
                                d.et_exact = state.current.et_exact;
                                methods.getFilters(d, data);
                                if(data.asOf){
                                    d.asOf = data.asOf;
                                }
                                var lsId = $.jCommon.getRandomId("pSum");
                                var param = $.jCommon.storage.setItem(lsId, d, true);
                                if(param){
                                    lsId = param;
                                }
                                if(isDiv) {
                                    r = dCrt('div').html(n);
                                }
                                else {
                                    r = dCrt('a').attr('href', String.format("{0}?d={1}", (d.filter==='iavm_unique_failed') ? _iUrl:_iaUrl, lsId)).attr('target', '_blank').html(n);
                                }
                                if(dwnld) {
                                    var glyph = methods.getDownloadGlyph(function (glyph, ldr) {
                                        var key = d[state.KEY_ID];
                                        var detail = "csv";
                                        var view = d.et_view;
                                        var filter = d.filter;
                                        var exact = state.current.et_exact;
                                        var url = lusidity.environment('host-download') + key + String.format('/hierarchy/details?detail={0}&view={1}&filter={2}&exact={3}&start=0&limit=1000000', detail, view, filter, exact);
                                        var s = function (nData) {
                                            if (nData) {
                                                var u = methods.getDownloadUrl(nData.url);
                                                window.location.assign(u);
                                            }
                                            if (ldr) {
                                                ldr.hide();
                                                glyph.show();
                                            }
                                        };
                                        $.htmlEngine.request(url, s, s, d.groups, 'post');
                                    });
                                    var t = dCrt('span').append(glyph);
                                    r.append(t);
                                }
                            }
                            else{r=n;}
                            return methods.content.smry.node(lbl, r, {display: 'inline-block'}, glyph);
                        }
                        var p = dCrt('span').addClass('font-green-med').append(make('Passed', data._sd.iavm.passed, {filter: 'iavm_passed'}, true, false));
                        var f = dCrt('span').addClass('font-red-med').html(make('Failed', data._sd.iavm.failed, {filter: 'iavm_failed'}, true, true));
                        var u = dCrt('span').attr('title', 'Represents list of Unique IAVMs and assets affected by that IAVM').html(make('Unique IAVMs', data._sd.iavm.u_failed, {filter: 'iavm_unique_failed'}, false, true));
                        var au = dCrt('span').attr('title', 'Represents list of Unique Assets and the open IAVMs for each').html(make('Unique Assets', data._sd.iavm.a_u_failed, {filter: 'iavm_unique_asset_failed'}, false, true));
                        var t = dCrt('span').html(String.format('Total: {0}', $.jCommon.number.commas(data._sd.iavm.total)));
                        var d = dCrt('div').append(p).append(dCrt('br')).append(f).append(dCrt('br')).append(t).append(dCrt('br')).append(u).append(dCrt('br')).append(au);
                        d.children().css({marginRight: '5px'});
                        node.append(d);
                    },
                    r: function (data,node) {
                        /*
                         if(!data._sd.risk){
                         node.append(dCrt('div').css({height: '100%', width: '100%', testAlign: 'middle', verticalAlign: 'center'}).html("Unavailable"));
                         return false;
                         }*/
                        state.current.item.metrics = data._sd.risk;
                        function make() {
                            node.children().remove();

                            var rubric = dCrt('div').addClass('metric-card');
                            node.append(rubric);
                            rubric.jVulnMatrix({
                                type: 'card', data: state.current.item, offsetWidth: state.opts.card.offsetWidth,
                                title: 'Risk Matrix',
                                glyph: "glyphicons-info-sign"
                            });
                        }
                        make();
                        methods.resize.register(make);
                    },
                    v: function (data, node){
                        if(node.loaders('exists')) {
                            node.loaders('hide');
                        }
                        if(!data._sd.vuln){
                            return false;
                        }
                        var v = data._sd.vuln;
                        var fld = v.catI+v.catII+v.catIII+v.critical+v.high+v.medium+v.low+(v.unknown ? v.unknown : 0);
                        if((!v.passed || v.passed === 0) && (v.total && v.total>0)){
                            v.passed  = v.total - fld;
                        }
                        var ttl = v.passed+fld;
                        var perc = (v.passed/ttl).toFixed(2);
                        perc = (perc*100).toFixed(0);
                        if(perc>0) {
                            var n = methods.content.smry.node("Findings", ($.jCommon.is.numeric(perc) ? perc : 0) + '%');
                            node.append(n);
                        }

                        function make(lbl, num, d, isDiv, uniq) {
                            var r = $.isNumeric(num)?num: 0;
                            var n = $.jCommon.number.commas(r);
                            var te = "Enumerated";
                            var tu = "Unique";
                            if(r>0 && d) {
                                if (isDiv) {
                                    r = dCrt('div').html(n).attr('title', te);
                                }
                                else {
                                    d.title = data.title;
                                    d.ditprId = data.ditprId;
                                    d.detail = state.details.asset;
                                    d.et_view = state.current.et_view;
                                    d.vertexType = data.vertexType;
                                    d[state.KEY_ID] = data[state.KEY_ID];
                                    d.count = num;
                                    d.et_exact = state.current.et_exact;
                                    methods.getFilters(d, data);
                                    if(data._sd.vuln.asOf){
                                        d.asOf = data._sd.vuln.asOf;
                                    }
                                    var lsId = $.jCommon.getRandomId("pSum");
                                    var param = $.jCommon.storage.setItem(lsId, d, true);
                                    if(param){
                                        lsId = param;
                                    }
                                    var a = dCrt('a').attr('title', te).attr('href', String.format("{0}?d={1}", _vUrl, lsId)).attr('target', '_blank').html(n);
                                    if (uniq && data._sd.vuln[uniq]) {
                                        r = dCrt('span');
                                        r.append(a);
                                        var u = dCrt('span').css({'paddingLeft': '5px'}).attr('title', tu).html(String.format('({0})', data._sd.vuln[uniq]));
                                        r.append(u);
                                    }
                                    else {
                                        r = a;
                                    }
                                    var glyph = methods.getDownloadGlyph(function (glyph, ldr) {
                                        var key = d[state.KEY_ID];
                                        var detail = "csv";
                                        var view = d.et_view;
                                        var filter = d.filter;
                                        var exact = state.current.et_exact;
                                        var url = lusidity.environment('host-download') + key + String.format('/hierarchy/details?detail={0}&view={1}&filter={2}&exact={3}&start=0&limit=1000000', detail, view, filter, exact);

                                        var s = function (nData) {
                                            if (nData) {
                                                var u = methods.getDownloadUrl(nData.url);
                                                window.location.assign(u);
                                            }
                                            if(ldr) {
                                                ldr.hide();
                                                glyph.show();
                                            }
                                        };
                                        $.htmlEngine.request(url, s, s, d.groups, 'post');
                                    });
                                    var t = dCrt('span').append(glyph);
                                    r.append(t);
                                }
                            }
                            else{r=n;}
                            return methods.content.smry.node(lbl, r, {display: 'inline-block'});
                        }
                        var d1 = dCrt('div');
                        if(v.passed>0) {
                            var p = dCrt('span').addClass('font-green-med').append(make('Passed', v.passed, {filter: 'passed'}, true));
                            d1.append(p).append(dCrt('br'));
                        }
                        var f = dCrt('span').addClass('font-red-med').append(make('Failed', fld, {filter: 'failed'}, false, 'u_failed'));
                        d1.append(f);
                        if(v.passed>0){
                            var t = dCrt('span').append(make('Total', ttl));
                            d1.append(dCrt('br')).append(t);
                        }

                        d1.children().css({marginRight: '5px'});
                        node.append(d1);
                        var c1 = dCrt('span').append(make('CAT I', v.catI, {filter: 'catI'}, false, 'u_catI'));
                        var c2 = dCrt('span').append(make('CAT II', v.catII, {filter: 'catII'}, false, 'u_catII'));
                        var c3 = dCrt('span').append(make('CAT III', v.catIII, {filter: 'catIII'}, false, 'u_catIII'));
                        var d2 = dCrt('div').append(c1).append(dCrt('br')).append(c2).append(dCrt('br')).append(c3);
                        d2.children().css({marginRight: '5px'});
                        node.append(d2);

                        var c = dCrt('span').append(make('Critical', v.critical, {filter: 'critical'}, false, 'u_critical'));
                        var h = dCrt('span').append(make('High', v.high, {filter: 'high'}, false, 'u_high'));
                        var m = dCrt('span').append(make('Medium', v.medium, {filter: 'medium'}, false, 'u_medium'));
                        var l = dCrt('span').append(make('Low', v.low, {filter: 'low'}, false, 'u_low'));
                        var unk = dCrt('span').append(make('Info', v.unknown, {filter: 'info'}, false, 'u_unknown'));
                        var d3 = dCrt('div').append(c).append(dCrt('br')).append(h).append(dCrt('br')).append(m).append(dCrt('br')).append(l).append(dCrt('br')).append(unk);
                        d3.children().css({marginRight: '5px'});
                        node.append(d3);
                    }
                }
            }
        };
        //public methods

        state.resize = function () {
            methods.resize.init();
        };

        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.pSummary.defaults = {
        calcHeights: true,
        "panels": {
            "breadcrumb": true,
            "title": true,
            "findings": true,
            "topFindings": true,
            "risk": true
        },
        view: 'all',
        adjustHeight: 0,
        cascade: true,
        card:{
            offsetWidth: 0
        }
    };


    //Plugin Function
    $.fn.pSummary = function(method, options) {
        if (method === undefined) method = {};
        if (typeof method === "object") {
            return this.each(function() {
                new $.pSummary($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $pSummary = $(this).data('pSummary');
            switch (method) {
                case 'exists': return (null!==$pSummary && undefined!==$pSummary && $pSummary.length>0);break;
                case 'resize': $pSummary.resize();break;
                case 'state':
                default: return $pSummary;
            }
        }
    };

    $.pSummary.call= function(elem, options){
        elem.pSummary(options);
    };

    try {
        $.htmlEngine.plugins.register("pSummary", $.pSummary.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
