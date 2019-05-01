

;(function ($) {

    //Object Instance
    $.assetSources = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.assetSources.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("assetSources", state);

        // Private environment methods
        methods = {
            init: function() {
                methods.header.init();
                methods.get(state.opts.pnlLeftNode, 'Assets', 'com.lusidity.domains.electronic.network.Asset');
                methods.get(state.opts.pnlRightNode, 'Assets', 'com.lusidity.domains.electronic.network.AcasInvalidAsset');
                lusidity.environment('onResize', function(){
                    methods.resize();
                });
            },
            exists: function (node) {
                return (node && (node.length>0));
            },
            get: function (node, title, type) {
                var s = function (data) {
                    if(data && data.results && data.results.length>0) {
                        methods.content.init(node, title, type, data);
                    }
                    else{
                        methods.noData();
                    }
                };
                var f = function () {
                    methods.noData(node);
                };
                var d = {vertexType: '/electronic/network/assets_source_details', type: type};
                var q = QueryFactory.assetSources(d);
                var url = methods.getUrl('/query', 0, 1);
                $.htmlEngine.request(url, s, f, q, 'post');
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
            header: {
                init: function () {
                    var h = dCrt("div").addClass('jumbotron no-radius').css({margin: "40px 5px 5px 5px", padding: '20px 20px'});
                    state.opts.pnlHeaderNode.append(h);
                    var t = dCrt('h3').html("Asset Sources");
                    h.append(t);
                }
            },
            noData: function (node) {
                node.children().remove();
                var c = dCrt('div');
                node.append(c);
                var hd = dCrt('h4').html("No results.").addClass('letterpress').css({margin: '20px 10px 0 10px'});
                c.append(hd);
            },
            content:{
                init: function(node, title, type, data){
                    var d = data.results[0];
                    var keys = $.jCommon.json.sortKeys(d);

                    function v(val){
                        return val ? val : 0;
                    }

                    node.append(methods.node("Type", d.type));
                    node.append(methods.node("As of", $.jCommon.dateTime.defaultFormat(d.createdWhen)));
                    node.append(methods.node("Total", $.jCommon.number.commas(d.total)));
                   // node.append(methods.node("Unknown", v(d.dtl_ua)));
                    node.append(dCrt('br'));
                    node.append(methods.node("HBSS", v(d.dtl_hbss)));
                    node.append(methods.node("HBSS only", v(d.dtl_hbss_only)));
                    node.append(dCrt('br'));
                    node.append(methods.node("ACAS", v(d.dtl_acas)));
                    node.append(methods.node("ACAS only", v(d.dtl_acas_only)));
                    node.append(dCrt('br'));

                    $.each(keys, function (k, v) {
                        if($.jCommon.string.startsWith(k, 'dtl') && !$.jCommon.string.contains(k, 'hbss')
                            && !$.jCommon.string.contains(k, 'acas')) {
                            var t = $.jCommon.string.replaceAll(k, 'dtl_', '');
                            t = $.jCommon.string.replaceAll(t, '_', ' ');
                            t = $.jCommon.string.toTitleCase(t);
                            node.append(methods.node(t, d[k]));
                        }
                    });
                }
            },
            node: function (label, value, css, link) {
                var r = dCrt('div').css({marginLeft: '5px'});
                var n;
                if(label){
                    n = dCrt('div').addClass('data-label');
                    n.append(String.format('{0}: ', label));
                    r.append(n);
                }
                if(value){
                    n = dCrt('div').addClass('data-value');
                    if($.jCommon.is.numeric(value)){
                        value = $.jCommon.number.commas(value);
                    }
                    n.append(value);
                    r.append(n);
                }
                if(css){
                    r.css(css);
                }
                if(link){
                    r.append(link);
                }
                return r;
            },
            resize: function () {}
        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.assetSources.defaults = {};


    //Plugin Function
    $.fn.assetSources = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.assetSources($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $assetSources = $(this).data('assetSources');
            switch (method) {
                case 'exists': return (null!==$assetSources && undefined!==$assetSources && $assetSources.length>0);
                case 'state':
                default: return $assetSources;
            }
        }
    };

})(jQuery);
