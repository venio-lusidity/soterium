;(function ($) {

    //Object Instance
    $.softwareCompliant = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.softwareCompliant.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.KEY_APPS = '/technology/software/applications';
        state.KEY_VER = '/technology/software_version/version.version';
        state.green = "#49b26f";
        state.red = "#d9534f";
        state.gray = "#a9a9a9";
        state.blue = "#0080ff";
        state.black = "#333333";
        state.start = 0;
        state.isInvalid = $.jCommon.string.contains(state.worker.data.vertexType, 'acas_invalid_asset');

        // Store a reference to the environment object
        el.data("softwareCompliant", state);

        // Private environment methods
        methods = {
            init: function() {
                state.worker.node.attr('data-valid', true).show();
                var s = function (data) {
                    methods.html.init(data);
                };
                var u = state.worker.data[state.KEY_ID] + "/software/compliance";
                $.htmlEngine.request(u, s, s, null, 'get');
            },
            getQueryUrl: function (start, limit) {
                if (undefined === start) {
                    start = 0;
                }
                if (undefined === limit) {
                    limit = 0;
                }
                return '/query?start=' + start + '&limit=' + limit;
            },
            resize: function () {
                var h = state.body.availHeight();
                state.body.css({height: h + 'px', maxHeight: h+'px', overflowY: 'auto', overflowX: 'hidden'});
            },
            html:{
                init: function(data){
                    state.body= $.htmlEngine.panel(
                        state.worker.node, state.opts.glyph, state.opts.title, null, false /* borders */, true
                    );
                    var c = dCrt('div');
                    state.body.append(c);

                    var st = $.htmlEngine.compliant(data);
                    state.worker.node.panel('updateHeader',
                        { glyph: String.format('{0} {1}', st.glyph, st.fclr), tip: st.tip, title: st.tip, url: null }
                    );
                    methods.html.policies(c, data);
                },
                policies: function(node, data, os){
                    var results = data.results;
                    if(!(results && results.length>0)){
                        //return false;
                    }
                    results = $.jCommon.array.sort(results, [{property: "title", asc: true}]);
                    function splitter(node){
                        if (node.children().length>0) {
                            var splitter = $(document.createElement("span")).html("|").css({margin: '0 5px 0 5px'});
                            node.append(splitter);
                        }
                    }
                    var hp = false;
                    $.each(results, function() {
                        var item = this;
                        if (item.policy && item.policy.lid) {
                            hp = true;
                            return false;
                        }
                    });
                    $.each(results, function(){
                        var item = this;
                        var ver;
                        var clr = state.blue;
                        var tip='';

                        if(!item.policy && !hp){
                            ver = 'Unknown';
                            tip = "Expected Version displays \"Unknown\" when the operating system is unknown."
                        }
                        else if(item.disabled){
                            ver = 'Disabled';
                        }
                        else if(!item.required){
                            ver = 'Not Required'
                        }
                        else{
                            ver = $.jCommon.json.getProperty(item.policy, state.KEY_VER);
                            clr = state.black;
                        }
                        var n = dCrt('div').addClass('data-list');
                        node.append(n);
                        var t = dCrt('div').addClass('data-value').attr('href', item[state.KEY_ID]).attr('target', '_blank').html(item.label);
                        n.append(t);
                        splitter(n);
                        var l = dCrt(item.policy ? 'a' : 'div').addClass('data-label').html('expected version:');
                        if(item.policy){
                            l.attr('href', item.policy[state.KEY_ID]).attr('target', '_blank');
                        }
                        n.append(l);
                        var v = dCrt('div').addClass('data-value').css('color', clr).attr('title', tip).html(ver);
                        n.append(v);
                        splitter(n);
                        l = dCrt(item.software ? 'a' : 'div').addClass('data-label').html('actual version:');
                        if(item.software){
                            l.attr('href', item.software[state.KEY_ID]).attr('target', '_blank');
                        }
                        n.append(l);

                        var av;
                        clr = state.gray;
                        var tip;
                        if(!item.policy && !item.software){
                            av = 'Unknown';
                        }
                        else if(!item.software && item.disabled){
                            av = "Disabled";
                            clr = state.blue;
                        }
                        else if(!item.software){
                            av = item.required ? "Missing" : "Not Required";
                            clr = state.red;
                        }
                        else{
                            av = $.jCommon.json.getProperty(item.software, state.KEY_VER);
                            clr = (item.compliant && item.software) ? state.green : state.red;
                            if(item.disabled){
                                clr = state.blue;
                            }
                        }
                        if(!item.required){
                            clr = state.blue;
                        }
                        var p = dCrt('div').addClass('data-value').css({color: clr}).html(av);
                        n.append(p);
                    });
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.softwareCompliant.defaults = {};


    //Plugin Function
    $.fn.softwareCompliant = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.softwareCompliant($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $softwareCompliant = $(this).data('softwareCompliant');
            switch (method) {
                case 'state':
                default: return $softwareCompliant;
            }
        }
    };

    $.softwareCompliant.call= function(elem, options){
        elem.softwareCompliant(options);
    };

    try {
        $.htmlEngine.plugins.register("softwareCompliant", $.softwareCompliant.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
