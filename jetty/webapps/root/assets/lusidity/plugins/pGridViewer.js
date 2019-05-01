;(function ($) {
    //Object Instance
    $.pGridViewer = function (el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data};
        state.opts = $.extend({}, $.pGridViewer.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');

        // Store a reference to the environment object
        el.data('pGridViewer', state);
        var musts = [];
        state._init = false;
        state._resized = false;

        // Private environment methods
        methods = {
            init: function () {
                state.worker.node.attr('data-valid', true).css({height: '100%', overflow: 'hidden'}).show();
                // $.htmlEngine.loadFiles(state.worker.propertyNode, state.opts.name, state.opts.cssFiles);
                state.body = state.opts.title ? $.htmlEngine.panel(state.worker.node, state.opts.glyph, state.opts.title, null, false, null, null) : dCrt('div');
                if (!state.opts.title) {
                    state.worker.node.append(state.body);
                }
                state.body.css({height: 'inherit'});
                if(!state.opts.hInherit) {
                    var h = state.body.availHeight();
                    if(h>0) {
                        dHeight(state.body, 0, h, h);
                    }
                }
                state.body.css({overflow: 'hidden', fontSize: '12px', position: 'relative'});

                function make(){
                    state.body.children().remove();
                    state.grid = dCrt('div').css({height: 'inherit'});
                    state.body.append(state.grid);
                    state.grid.pGrid(state.opts);
                }

                function start() {
                    make();
                    lusidity.environment('onResize', function () {
                        if (!state.body.is(":visible")) {
                            state._resized = true;
                        }
                    });
                    state._init = true;
                }

                var btn;
                if (state.opts.initOnClick) {
                    btn = $('#'+state.opts.initOnClick);
                }
                if (btn && btn.length > 0) {
                    btn.on('click', function (){
                        if (!state._init) {
                            methods.ready({
                                onVisible: function (h, w) {
                                    start();
                                }
                            });
                        }
                        else if(state._resized){
                            methods.ready({
                                onVisible: function (h, w) {
                                    state.reset({reset: true});
                                    state._resized = false;
                                }
                            });
                        }
                    });
                    if(state.opts.active){
                        start();
                    }
                }
                else {
                    start();
                }
            },
            ready: function (o) {
                if(state.worker.node.jNodeReady("exists")){
                    state.worker.node.jNodeReady("start", o);
                }
                else {
                    state.worker.node.jNodeReady(o);
                }
            },
            resize: function (opts) {
                if(!state.opts.hInherit) {
                    dHeight(state.body, 0, 0, 0);
                    var h = state.body.availHeight();
                    if(h>0) {
                        dHeight(state.body, 0, h, h);
                    }
                }
                state.resize(opts);
            },
            setOnResize: function (opts) {
                if(!opts){
                    opts={};
                }
                if(state.opts.offset && state.opts.offset.onResize){
                    opts.offset = state.opts.offset;
                    opts.offset.table = state.opts.offset.onResize;
                }
                return opts;
            },
            getSeverity: function (s) {
                var v = $.jCommon.string.toTitleCase(s);
                switch (s.toLowerCase()) {
                    case 'cat i':
                    case 'cat_i':
                        v = 'CAT I';
                        break;
                    case 'cat ii':
                    case 'cat_ii':
                        v = 'CAT II';
                        break;
                    case 'cat iii':
                    case 'cat_iii':
                        v = 'CAT III';
                        break;
                }
                return v;
            }
        };

        methods.init();
        state.resize = function (opts) {
            if(state._init) {
                opts = methods.setOnResize(opts);
                state.grid.pGrid('resize', opts);
            }
        };
        state.reset = function (opts) {
            if(state._init) {
                methods.ready({
                    onVisible: function (h, w) {
                        if (!state.opts.hInherit) {
                            dHeight(state.body, 0, 0, 0);
                            var h = state.body.availHeight();
                            if (h > 0) {
                                dHeight(state.body, 0, h, h);
                            }
                        }
                        opts = methods.setOnResize(opts);
                        opts.resizing = true;
                        state.grid.pGrid('reset', opts);
                    }
                });
            }
        };
        return state;
    };

    //Default Settings
    $.pGridViewer.defaults = {
        fill: true,
        minHeight: 0,
        maxHeight: 0,
        limit: 90,
        height: 0,
        offset: {
            parent: 0,
            table: 0
        }
    };


    //Plugin Function
    $.fn.pGridViewer = function (method, options) {
        if (method === undefined) method = {};

        if (typeof method === 'object') {
            return new $.pGridViewer($(this), method);
        } else {
            // Helper strings to quickly perform functions
            var $pGridViewer = $(this).data('pGridViewer');
            switch (method) {
                case 'exists':
                    return (null !== $pGridViewer && undefined !== $pGridViewer && $pGridViewer.length > 0);
                case 'reset':
                    $pGridViewer.reset(options);
                    break;
                case 'resize':
                    $pGridViewer.resize(options);
                    break;
                case 'state':
                default:
                    return $pGridViewer;
            }
        }
    };

    $.pGridViewer.call = function (elem, options) {
        elem.pGridViewer(options);
    };

    try {
        $.htmlEngine.plugins.register('pGridViewer', $.pGridViewer.call);
    }
    catch (e) {
        console.log(e);
    }

})(jQuery);