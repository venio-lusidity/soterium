

;(function ($) {

    //Object Instance
    $.vulnDetails = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.vulnDetails.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.KEY_SUMMARY = '/electronic/reports/vulnerability_breakdown_item/summary';
        // Store a reference to the environment object
        el.data("vulnDetails", state);

        // Private environment methods
        methods = {
            init: function () {
                state.worker.node.attr('data-valid', true).show();

                $.htmlEngine.busy(state, {type: 'cube'});
                var s = function (data) {
                    methods.html.init(data);
                    state.loaders('hide');
                };
                var f = function () {state.loaders('hide');};
                var uri = state.worker.data[state.KEY_ID] + '/report';
                var d = {
                    verbose: false,
                    type: '/electronic/reports/vulnerability_breakdown_report'
                };
                $.htmlEngine.request(uri, s, f, d, 'post');
            },
            html: {
                init: function (data) {
                    if (data && data.reports) {
                        try {
                            var d = dCrt('div').css({lineHeight: '20px', margin: '5px 8px'});
                            var s = dCrt('span').addClass('glyphicon glyphicon-new-window').html("&nbsp;");
                            var link = dCrt('a').attr('href', '/pages/reports/vbr/index.html?id=' + data.origin[state.KEY_ID]).attr('target', '_blank')
                                .append(s).append("View full Vulnerability Breakdown Report");
                            d.append(link);
                            s.hide();
                            link.hide();

                            var t = dCrt('table').addClass('table table-striped');
                            if (state.opts.panel) {
                                var body = $.htmlEngine.panel(state.worker.node, "glyphicons glyphicons-charts", "Vulnerability Breakdown", null, false);
                                body.append(d).append(t);
                            }
                            else {
                                state.worker.node.append(d).append(t);
                            }
                            var keys = ["d_total", "v_total", "a_total", "passed", "unknown", "catI", "catII", "catIII", "critical", "high", "medium", "low"];
                            if (data.reports.length > 0) {
                                var r = data.reports[0];
                                $.each(keys, function () {
                                    var key = this;
                                    var items = r[state.KEY_SUMMARY];
                                    $.each(items, function () {
                                        var item = this;
                                        if ($.jCommon.string.equals(item.key, key)) {
                                            methods.html.details(t, key, item, data);
                                            return false;
                                        }
                                    });
                                });
                            }
                        } catch(e){}
                    }
                },
                details: function (tbl, key, item, data) {
                    var r = dCrt('tr');
                    tbl.append(r);
                    var isSys = $.jCommon.string.contains(data.origin.vertexType, 'enclave');
                    if(!isSys && $.jCommon.string.startsWith(item[state.KEY_TITLE], "no ", true)){
                        return false;
                    }
                    var clr = item.key;
                    switch(clr){
                        case "critical":
                        case "catI":
                            clr = "critical";
                            break;
                        case "high":
                        case "catII":
                            clr = "high";
                            break;
                        case "medium":
                        case "catIII":
                            clr = "medium";
                            break;
                        case "low":
                            clr = "low";
                            break;
                        case "vulnUnknown":
                        case "unknown":
                            clr="#9781f1";
                            break;
                        default:
                        case "passed":
                            clr = '#45a9e4';
                            break;
                    }

                    var td = dCrt('td').width("30px");
                    var m = dCrt('span').addClass('glyphicon glyphicon-minus').addClass(clr + '-font');
                    td.append(m);
                    r.append(td);

                    var td = dCrt('td').html(item[state.KEY_TITLE]);
                    r.append(td);

                    var a_msg = 'Unique Assets';
                    var v_msg = isSys ? 'Unique Vulnerabilities (Enumerated)' : 'Unique Vulnerabilities';
                    var td4;
                    var td1 = dCrt('td').css({textAlign: 'right'}).html(state.opts.thin ? 'A' : 'Assets');
                    r.append(td1);
                    var td2 = dCrt('td').html(item.d_count).css({textAlign: 'right', paddingRight: '5px'});
                    r.append(td2);
                    var td3 = dCrt('td').css({textAlign: 'right'}).html(state.opts.thin ? 'V' : 'Vulnerabilities');
                    r.append(td3);
                    if($.jCommon.string.contains(key, 'a_total')){
                        td4 = dCrt('td').html(item.a_count).css({textAlign: 'right'})
                    }
                    else if(!$.jCommon.string.contains(key, 'total') && !$.jCommon.string.contains(key, 'unknown') &&
                        !$.jCommon.string.contains(key, 'passed')) {
                        var s1 = dCrt('span').html(item.v_count);
                        td4 = dCrt('td').css({textAlign: 'right'}).append(s1);
                        if (isSys) {
                            var s2 = dCrt('span').html("(" + item.a_count + ")");
                            td4.append(s2);
                        }
                    }
                    else{
                        td4 = dCrt('td').html(item.d_count).css({textAlign: 'right'})
                    }
                    r.append(td4);
                    if(state.opts.thin){
                        td1.attr('title', 'Assets');
                        td3.attr('title', 'Vulnerabilities');
                    }
                    if(!isSys) {
                        td1.html('');
                        td2.html('');
                    }

                    if($.jCommon.string.equals(key, "passed") || $.jCommon.string.equals(key, "unknown")) {
                        td.html('');
                        var a = dCrt('a').attr('href', '#'+item.key).html(item[state.KEY_TITLE]);
                        td.append(a);
                        td2.html('');
                        td3.html('');
                        a_msg = $.jCommon.string.equals(key, "passed") ?
                            'The total number of assets that did not fail any scans' : 'The total number of assets that do not have any scans recorded at all.';
                        v_msg = a_msg;
                    }
                    else if($.jCommon.string.contains(key, 'total')){
                        if(!isSys) {
                            r.hide();
                        }
                        else{
                            td1.html('');
                            td2.html('');
                            td3.html('');
                            a_msg = '';
                            v_msg = '';
                        }

                    }
                    else{
                        td.html('');
                        var a = dCrt('span').html(item[state.KEY_TITLE]);
                        td.append(a);
                    }
                    td1.attr('title', '');
                    td2.attr('title', a_msg).css({cursor: "help"});
                    td3.attr('title', '');
                    td4.attr('title', v_msg).css({cursor: "help"});
                    tbl.append(r);
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.vulnDetails.defaults = {
        limit: 20
    };


    //Plugin Function
    $.fn.vulnDetails = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.vulnDetails($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $vulnDetails = $(this).data('vulnDetails');
            switch (method) {
                case 'exists': return (null!==$vulnDetails && undefined!==$vulnDetails && $vulnDetails.length>0);
                case 'state':
                default: return $vulnDetails;
            }
        }
    };

    $.vulnDetails.call= function(elem, options){
        elem.vulnDetails(options);
    };

    try {
        $.htmlEngine.plugins.register("vulnDetails", $.vulnDetails.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
