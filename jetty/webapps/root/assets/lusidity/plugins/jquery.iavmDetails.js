

;(function ($) {

    //Object Instance
    $.iavmDetails = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.iavmDetails.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.KEY_SUMMARY = '/electronic/reports/iavm_breakdown_item/summary';
        state.baseUrl = lusidity.environment('host-primary');
        // Store a reference to the environment object
        el.data("iavmDetails", state);

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
                    type: '/electronic/reports/iavm_breakdown_report'
                };
                $.htmlEngine.request(uri, s, f, d, 'post');
            },
            html: {
                init: function (data) {
                    if (data && data.reports) {
                        var d = dCrt('div').css({lineHeight: '20px', margin: '5px 8px'});
                        var s = dCrt('span').addClass('glyphicon glyphicon-new-window').html("&nbsp;");
                        var link =  dLink('','/pages/reports/ibr/index.html?id='+data.origin[state.KEY_ID])

                            .append(s).append("View full IAVM Breakdown Report");
                        d.append(link);

                        var t = dCrt('table').addClass('table table-striped');
                        if(state.opts.panel){
                            var body = $.htmlEngine.panel(state.worker.node, "glyphicons glyphicons-charts", "IAVM Breakdown", null, false);
                            body.append(d).append(t);
                        }
                        else {
                            state.worker.node.append(d).append(t);
                        }
                        var keys = ["d_total", "v_total", "a_total", "compliant", "non_compliant", "unknown"];
                        if(data.reports.length>0) {
                            var r = data.reports[0];
                            $.each(keys, function () {
                                var key = this;
                                var items = r[state.KEY_SUMMARY];
                                $.each(items, function () {
                                    var item = this;
                                    if($.jCommon.string.equals(item.key, key)){
                                        methods.html.details(t, key, item, data);
                                        return false;
                                    }
                                });
                            });
                        }
                    }
                },
                details: function (tbl, key, item, data) {
                    var r = dCrt('tr');
                    tbl.append(r);

                    var clr = item.key;
                    switch(clr){
                        case "non_compliant":
                            clr = "#f0ab36";
                            break;
                        case "unknown":
                            clr="#9781f1";
                            break;
                        default:
                        case "compliant":
                            clr = '#45a9e4';
                            break;
                    }

                    var td = dCrt('td').width("30px");
                    var m = dCrt('span').addClass('glyphicon glyphicon-minus').css({color: clr});
                    td.append(m);
                    r.append(td);

                    var td = dCrt('td').html(item[state.KEY_TITLE]);
                    r.append(td);
                    // V 12 <-- 30000 --> A 30
                    // one to one relationship between a given IAVM A and an Asset.
                    var a_msg = 'Unique Assets';
                    var v_msg = 'Unique IAVMs(Enumerated)';
                    var td4;
                    var td1 = dCrt('td').css({textAlign: 'right'}).html(state.opts.thin ? 'A' : 'Assets');
                    r.append(td1);
                    var td2 = dCrt('td').html(item.d_count).css({textAlign: 'right', paddingRight: '5px'});
                    r.append(td2);
                    var td3 = dCrt('td').css({textAlign: 'right'}).html(state.opts.thin ? 'I' : 'IAVMs');
                    r.append(td3);
                    if($.jCommon.string.contains(key, 'a_total')){
                        td4 = dCrt('td').html(item.a_count).css({textAlign: 'right'})
                    }
                    else if($.jCommon.string.contains(key, 'non_compliant')) {
                        var s1 = dCrt('span').html(item.v_count);
                        var s2 = dCrt('span').html("(" + item.a_count + ")");
                        td4 = dCrt('td').append(s1).append(s2).css({textAlign: 'right'});
                    }
                    else if($.jCommon.string.contains(key, 'compliant')) {
                        var t = dCrt('span').html(item.v_count);
                        td4 = dCrt('td').append(t).css({textAlign: 'right'});
                    }
                    else{
                        td2.html('');
                        td3.html('');
                        td4 = dCrt('td').html(item.d_count).css({textAlign: 'right'})
                    }
                    r.append(td4);

                    if(state.opts.thin){
                        td1.attr('title', 'Assets');
                        td3.attr('title', 'IAVMs');
                    }

                    if(!$.jCommon.string.contains(data.origin.vertexType, "enclave")) {
                        td3.html('');
                        td4.html('');
                    }
                    if($.jCommon.string.equals(key, "unknown")) {
                        td.html('');
                        var a = dCrt('a').attr('href', '#'+item.key).html(item[state.KEY_TITLE]);
                        td.append(a);
                        td2.html('');
                        td3.html('');
                        a_msg = 'The total number of assets that do not have any IAVM related scans recorded.';
                        v_msg = a_msg;
                    }
                    if($.jCommon.string.contains(key, 'total')){
                        td1.html('');
                        td2.html('');
                        td3.html('');
                        a_msg='';
                        v_msg='';
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
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.iavmDetails.defaults = {
        limit: 20
    };


    //Plugin Function
    $.fn.iavmDetails = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.iavmDetails($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $iavmDetails = $(this).data('iavmDetails');
            switch (method) {
                case 'exists': return (null!=$iavmDetails && undefined!=$iavmDetails && $iavmDetails.length>0);
                case 'state':
                default: return $iavmDetails;
            }
        }
    };

    $.iavmDetails.call= function(elem, options){
        elem.iavmDetails(options);
    };

    try {
        $.htmlEngine.plugins.register("iavmDetails", $.iavmDetails.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
