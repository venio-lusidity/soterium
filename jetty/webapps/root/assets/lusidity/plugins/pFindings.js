

;(function ($) {

    //Object Instance
    $.pFindings = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.pFindings.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        var _aUrl = '/pages/enclaves/assets/index.html';
        var _vUrl = '/pages/enclaves/vuln/index.html';
        var _iUrl = '/pages/iavms/iavm/index.html';
        var _isAsset = false;

        // Store a reference to the environment object
        el.data("pFindings", state);

        // Private environment methods
        methods = {
            init: function () {
                _isAsset = $.jCommon.string.endsWith(state.opts.data.vertexType, 'asset');
                state.worker.node.attr('data-valid', true).show();
                state.opts.isOrg = $.jCommon.string.contains(state.opts.data.vertexType, "organization");

                state.opts.isOrg = $.jCommon.string.contains(state.opts.data.vertexType, "organization");
                state.opts.isLoc = $.jCommon.string.contains(state.opts.data.vertexType, "location");
                state.opts.isSys = $.jCommon.string.contains(state.opts.data.vertexType, "enclave");

                if (state.opts.show.assets) {
                    methods.get("count", function (data) {
                        var d = dCrt('div');
                        state.worker.node.append(d);
                        methods.a(data, d);
                    });
                }

                if (state.opts.show.vulns) {
                    methods.get("vuln", function (data) {
                        var d = dCrt('div');
                        state.worker.node.append(d);
                        methods.v(data, d);
                    });
                }
            },
            url: function (detail) {
                return String.format('{0}/hierarchy/details?detail={1}&view={2}&_nocache={3}', state.opts.data[state.KEY_ID], detail, state.opts.et_view, $.jCommon.getRandomId('c'));
            },
            get: function (detail, callback) {
                var s = function (data) {
                    if (data) {
                        callback(data);
                    }
                };
                var f = function (j, t, e) {

                };
                $.htmlEngine.request(methods.url(detail), s, f, null, 'get', true);
            },
            getViewType: function () {
                var t;
                switch (state.opts.et_view) {
                    case 'ditpr':
                        t = "System Name";
                        break;
                    case 'managed':
                        t = "Managed By";
                        break;
                    case 'owned':
                        t = "Owned By";
                        break;
                    case 'asset':
                        t = "Asset";
                        break;
                    case 'location':
                        t = "Location";
                        break;
                    default:
                        t = "Unknown";
                        break;
                }
                return t;
            },
            getPrefixKey: function () {
                var r;
                switch (state.opts.et_view) {
                    case 'owned':
                        r = 'prefixOwned';
                        break;
                    case 'managed':
                        r = 'prefixManaged';
                        break;
                    case 'location':
                        r = 'prefixLocation';
                        break;
                    default:
                        r = 'prefixDitpr';
                        break;
                }
                return r;
            },
            a: function (data, node) {
                var c = data.count;
                var lk1 = (c.inherited>0);
                var cin = $.jCommon.number.commas(c.inherited);
                var cun = $.jCommon.number.commas(c.exact);

                var p = {
                    mvId: data.count.mvId,
                    title: state.opts.data.title,
                    vertexType: state.opts.data.vertexType,
                    filter: 'all',
                    detail: 'asset',
                    et_view: state.opts.et_view
                };
                p[state.KEY_ID] = state.opts.data[state.KEY_ID];
                p.prefixTree = state.opts.et_view === 'ditpr' ? state.opts.data.prefixDitpr : state.opts.data.prefixTree;
                p.prefixKey = methods.getPrefixKey();
                p.exact = _isAsset;
                p.count = cin;
                if (data.count.asOf) {
                    p.asOf = data.count.asOf;
                }
                else{
                    p.asOf = Date.now();
                }
                var lsId = $.jCommon.getRandomId("pSum");
                var param = $.jCommon.storage.setItem(lsId, p, true);
                if(param){
                    lsId = param;
                }
                var span = dCrt('span');
                var link = dCrt((lk1 ? 'a' : 'span')).attr('href', String.format("{0}?d={1}", _aUrl, lsId)).attr('target', '_blank').html(cin);
                if (lk1) {
                    link.attr('title', "Represents how many assets are associated with this system.");
                }
                var vb = state.opts.isOrg ? 'organizational' : 'location';
                if(!state.opts.isSys && lk1) {
                    link.attr('title', 'Represents how many assets are assigned to this ' + vb + ' level and all subordinates.');
                }
                if (data.ditprId) {
                    var ditpr = dCrt('span').html(data.ditprId);
                    node.append(methods.content.smry.node("DITPR ID", ditpr).attr('title', "Referenced by HBSS Operation Attributes file."));
                }
                span.append(link);
                if(state.opts.isOrg && cin!==cun) {
                    var lk2 = (cun>0);
                    var hl = String.format('{0}?et_view={1}&et_exact=true', state.opts.data[state.KEY_ID], state.opts.et_view);
                    var l2 = dCrt(lk2 ? 'a' : 'span').css({marginLeft: '5px'}).attr('href', hl).attr('target', '_blank').html(String.format('[{0}]', cun));
                    l2.attr('title', 'Represents how many assets are directly assigned to this organizational level.');
                    span.append(l2);
                }
                var link = dCrt('span').append(dLink(methods.getViewType(), state.opts.data[state.KEY_ID] + '?et_view=' + state.opts.et_view));
                state.worker.node.append(link);
                var n = methods.node("Assets", span).css({display: 'inline-block', margin: '5px'});
                node.append(link).append(n);
            },
            v: function (data, node) {
                var fld = data.catI + data.catII + data.catIII + data.critical + data.high + data.medium + data.low + (data.unknown ? data.unknown : 0);
                var ttl = data.passed + fld;
                var perc = (data.passed / ttl).toFixed(2);
                perc = (perc * 100).toFixed(0);

                function rtn(node) {
                    if (node.children().length > 0) {
                        rtn.append(dCrt('br'));
                    }
                }

                function make(lbl, num, d, isDiv, uniq) {
                    var r = $.isNumeric(num) ? num : 0;
                    var n = $.jCommon.number.commas(r);
                    var te = "Enumerated";
                    var tu = "Unique";
                    if (r > 0 && d) {
                        if (isDiv) {
                            r = dCrt('div').html(n).attr('title', te);
                        }
                        else {
                            d.prefixTree = state.opts.et_view === 'ditpr' ? state.opts.data.prefixDitpr : state.opts.data.prefixTree;
                            d.prefixKey = methods.getPrefixKey();
                            d.exact = _isAsset;
                            d.title = state.opts.data.title;
                            d.detail = 'asset';
                            d.et_view = state.opts.et_view;
                            d.vertexType = state.opts.data.vertexType;
                            d[state.KEY_ID] = data[state.KEY_ID];
                            d.count = num;
                            if (data.asOf) {
                                d.asOf = data.asOf;
                            }
                            var lsId = $.jCommon.getRandomId("pSum");
                            var param = $.jCommon.storage.setItem(lsId, d, true);
                            if(param){
                                lsId = param;
                            }
                            var a = dCrt('a').attr('title', te).attr('href', String.format("{0}?d={1}", _vUrl, lsId)).attr('target', '_blank').html(n);
                            if (uniq && data[uniq]) {
                                r = dCrt('span');
                                r.append(a);
                                var u = dCrt('span').css({'paddingLeft': '5px'}).attr('title', tu).html(String.format('({0})', data[uniq]));
                                r.append(u);
                            }
                            else {
                                r = a;
                            }
                        }
                    }
                    else {
                        r = n;
                    }
                    return methods.node(lbl, r, {display: 'inline-block'});
                }

                if (state.opts.show.passed || state.opts.show.failed || state.opts.show.total || stat.opts.show.percentage) {
                    var d1 = dCrt('div');

                    if (state.opts.show.percentage) {
                        var n = methods.node("Findings", ($.jCommon.is.numeric(perc) ? perc : 0) + '%');
                        rtn(d1);
                        d1.append(n);
                    }

                    if (state.opts.show.passed) {
                        var p = dCrt('span').addClass('font-green-med').append(make('Passed', data.passed, {filter: 'passed'}, true));
                        rtn(d1);
                        d1.append(p);
                    }
                    if (state.opts.show.failed) {
                        var f = dCrt('span').addClass('font-red-med').append(make((ttl === 0 ? '' : 'Failed'), fld, {filter: 'failed'}, false, 'u_failed'));
                        rtn(d1);
                        d1.append(f);
                    }
                    if (state.opts.show.total) {
                        var t = dCrt('span').append(make('Total', ttl));
                        rtn(d1);
                        d1.append(t);
                    }
                    d1.children().css({marginRight: '5px'});
                    node.append(d1);
                }


                if (state.opts.show.catI || state.opts.show.catII || state.opts.show.catIII) {
                    var d2 = dCrt('div');
                    if (state.opts.show.catI) {
                        var c1 = dCrt('span').append(make('CAT I', data.catI, {filter: 'catI'}, false, 'u_catI'));
                        d2.append(c1);
                    }
                    if (state.opts.show.catII) {
                        var c2 = dCrt('span').append(make('CAT II', data.catII, {filter: 'catII'}, false, 'u_catII'));
                        rtn(d2);
                        d2.append(c2);
                    }
                    if (state.opts.show.catIII) {
                        var c3 = dCrt('span').append(make('CAT III', data.catIII, {filter: 'catIII'}, false, 'u_catIII'));
                        rtn(d2);
                        d2.append(c3);
                    }
                    var d2 = dCrt('div').append(c1).append(dCrt('br')).append(c2).append(dCrt('br')).append(c3);
                    d2.children().css({marginRight: '5px'});
                    node.append(d2);
                }

                if (state.opts.show.critical || state.opts.show.high || state.opts.show.medium || state.opts.show.info) {
                    var d3 = dCrt('div').append(c);
                    if (state.opts.show.critical) {
                        var c = dCrt('span').append(make('Critical', data.critical, {filter: 'critical'}, false, 'u_critical'));
                        rtn(d2);
                        d3.append(c);
                    }
                    if (state.opts.show.high) {
                        var h = dCrt('span').append(make('High', data.high, {filter: 'high'}, false, 'u_high'));
                        rtn(d2);
                        d3.append(h);
                    }
                    if (state.opts.show.medium) {
                        var m = dCrt('span').append(make('Medium', data.medium, {filter: 'medium'}, false, 'u_medium'));
                        rtn(d2);
                        d3.append(m);
                    }
                    if (state.opts.show.low) {
                        var l = dCrt('span').append(make('Low', data.low, {filter: 'low'}, false, 'u_low'));
                        rtn(d2);
                        d3.append(l);
                    }
                    if (state.opts.show.info) {
                        var unk = dCrt('span').append(make('Info', data.unknown, {filter: 'info'}, false, 'u_unknown'));
                        rtn(d2);
                        d3.append(unk);
                    }
                    var d3 = dCrt('div').append(c).append(dCrt('br')).append(h).append(dCrt('br')).append(m).append(dCrt('br')).append(l).append(dCrt('br')).append(unk);
                    d3.children().css({marginRight: '5px'});
                    node.append(d3);
                }
            },
            node: function (label, value, css, link) {
                var r = dCrt('div');
                var n;
                if (label) {
                    n = dCrt('div').addClass('data-label').css({fontWeight: 'bold'});
                    n.append(String.format('{0}: ', label));
                    r.append(n);
                }
                if (value) {
                    n = dCrt('div').addClass('data-value');
                    n.append(value);
                    r.append(n);
                }
                if (css) {
                    r.css(css);
                }
                if (link) {
                    r.append(link);
                }
                return r;
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.pFindings.defaults = {
        show: {
            viewType: true,
            assets: true,
            vulns: false,
            percentage: false,
            passed: false,
            failed: false,
            total: false,
            catI: false,
            catII: false,
            catIII: false,
            critical: false,
            high: false,
            medium: false,
            low: false,
            info: false
        }
    };


    //Plugin Function
    $.fn.pFindings = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.pFindings($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $pFindings = $(this).data('pFindings');
            switch (method) {
                case 'exists': return (null!=$pFindings && undefined!=$pFindings && $pFindings.length>0);
                case 'state':
                default: return $pFindings;
            }
        }
    };

    $.pFindings.call= function(elem, options){
        elem.pFindings(options);
    };

    try {
        $.htmlEngine.plugins.register("pFindings", $.pFindings.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
