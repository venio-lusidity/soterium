;(function ($) {

    //Object Instance
    $.pAssetEventMsg = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.pAssetEventMsg.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        var key_event = '/electronic/asset_event';
        var key_imp = '/technology/security/vulnerabilities/vulnerability_importer';
        var key_fct = '/event/event_fact/facts';

        // Store a reference to the environment object
        el.data("pAssetEventMsg", state);

        // Private environment methods
        methods = {
            init: function () {
                // $.htmlEngine.loadFiles(state.worker.propertyNode, state.opts.name, state.opts.cssFiles);
                state.worker.node.attr('data-valid', true).show();
                methods.collect();
            },
            collect: function () {
                var items = [];
                var evnts = state.worker.data[key_event+'/events'];
                if (evnts) {
                    $.each(evnts, function () {
                        var event = this;
                        var facts = event[key_fct];
                        $.each(facts, function () {
                            if(!this.fact){
                                this.fact = this.value;
                            }
                            if (this.key === 'expiredOn') {
                                event.scannedOn = this.fact;
                                return false;
                            }
                        });
                        items.push(event);
                    });
                }
                var imp = state.worker.data[key_imp+'/importers'];
                if (imp) {
                    $.each(imp, function () {
                        if (this.role === "Delete"){
                            items.push(this);
                        }
                    });
                }
                var node = dCrt(state.opts.type ? state.opts.type :'div').css({margin: '5px 5px', padding: '5px 5px'});
                if(state.opts.cls){
                    node.css(state.opts.cls);
                }
                if(state.opts.css){
                    node.css(state.opts.css);
                }
                state.worker.node.append(node);
                if (items && items.length > 0) {
                    items = $.jCommon.array.sort(items, [{property: "scannedOn", asc: false}]);
                    methods.html.init(node, items);
                }
                else {
                    methods.html._default(node);
                }
            },
            html: {
                init: function (node, items) {
                    var item = items[0];
                    var txt;
                    var stale = state.worker.data.modifiedWhen;
                    if(item.vertexType === key_event) {
                        var days;
                        var dt;
                        var facts = item[key_fct];
                        $.each(facts, function () {
                            if (this.key === 'days') {
                                days = this.fact;
                            }
                            else if(this.key==='expiredOn'){
                                dt = $.jCommon.dateTime.defaultFormat(this.fact);
                                stale = this.fact;
                            }
                        });
                        txt = String.format("This asset has a heartbeat date of {0} which is more than {1} days old and is now considered retired.", dt ? dt : "(no heartbeat found)", days);
                    }
                    else if(item.vertexType === key_imp){
                        stale = item.scannedOn;
                        txt = String.format("This asset was deprecated by {0} on {1}, using {2}.",
                                item.source,
                                $.jCommon.dateTime.defaultFormat(item.scannedOn),
                                FnFactory.toTitleCase($.jCommon.string.getLast(item.importer, "."))
                            );
                    }
                    else{
                        methods.html._default(node);
                    }

                    if(txt){
                        node.append(txt);
                    }

                    if(state.opts.del_msg && stale){
                        var dt1= $.jCommon.dateTime.defaultFormat(stale);
                        var p = dCrt('div').css({margin: '10px 10px 10px 15px', textAlign: 'left'}).html(
                            String.format("The data for this asset is stale as of {0} and should not be used for data analysis or any other consideration.", dt1 ? dt1 : "(no heartbeat found)")
                        );
                        state.worker.node.append(p);
                    }
                },
                _default: function (node) {
                    node.append("Not enough information to determine the cause of deprecation of this asset programmatically.")
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.pAssetEventMsg.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.pAssetEventMsg = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.pAssetEventMsg($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $pAssetEventMsg = $(this).data('pAssetEventMsg');
            switch (method) {
                case 'exists': return (null!==$pAssetEventMsg && undefined!==$pAssetEventMsg && $pAssetEventMsg.length>0);
                case 'state':
                default: return $pAssetEventMsg;
            }
        }
    };

    $.pAssetEventMsg.call= function(elem, options){
        elem.pAssetEventMsg(options);
    };

    try {
        $.htmlEngine.plugins.register("pAssetEventMsg", $.pAssetEventMsg.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
