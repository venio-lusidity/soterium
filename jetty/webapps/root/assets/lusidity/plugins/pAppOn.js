;(function ($) {

    //Object Instance
    $.pAppOn = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.pAppOn.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("pAppOn", state);

        // Private environment methods
        methods = {
            init: function() {
                state.worker.node.jNodeReady({onVisible: function () {
                        state.worker.node.attr('data-valid', true).show();
                        state.body = dCrt('div');
                        state.worker.node.append(state.body);
                        methods.html.init();
                        methods.resize();
                        lusidity.environment("onResize", function () {
                            methods.resize();
                            state.body.pGrid("resize", {maxHeight: state.body.height()});
                        });
                    }});
            },
            resize: function () {
                if(!state.opts.fill){
                    return false;
                }
                var h = state.worker.node.availHeight(state.opts.adjustBodyHeight);
                state.worker.node.panel('resize');
                state.body.css({height: h + 'px', maxHeight: h+'px', minHeight: h+'px', overflowY: 'auto', overflowX: 'hidden'});
            },
            html: {
                init: function () {
                    var sd = true;
                    var qry = QueryFactory.softwareToDevices(state.worker.data);
                    qry.include_edge = true;
                    var opts = TableFactory.get({
                        edgeType: '/object/edge/software_edge',
                        displayType: '/electronic/network/asset'
                    }, {qry: qry, maxHeight: state.body.height()});

                    if(sd){
                        opts.onBefore = function (items, node) {
                            if(items){
                                $.each(items, function () {
                                    if(this._edge){
                                        this.sourceDate = this._edge.createdWhen;
                                    }
                                });
                            }
                            return items;
                        };
                        opts.mapping.push(
                            {
                                header: {title: 'Source Date', property: 'sourceDate', css: {width: '150px'}},
                                property: 'sourceDate', type: 'date', callback: function (td, item, value) {
                                if(value){
                                    td.append( $.jCommon.dateTime.defaultFormat(value));
                                }
                            }
                            });
                    }
                    state.body.pGrid(opts);
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.pAppOn.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.pAppOn = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.pAppOn($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $pAppOn = $(this).data('pAppOn');
            switch (method) {
                case 'exists': return (null!==$pAppOn && undefined!==$pAppOn && $pAppOn.length>0);
                case 'state':
                default: return $pAppOn;
            }
        }
    };

    $.pAppOn.call= function(elem, options){
        elem.pAppOn(options);
    };

    try {
        $.htmlEngine.plugins.register("pAppOn", $.pAppOn.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
