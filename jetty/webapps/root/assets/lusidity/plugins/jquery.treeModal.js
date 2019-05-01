;(function ($) {

    //Object Instance
    $.treeModal = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);
        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = options.schema.plugin;
        // Store a reference to the environment object
        el.data("treeModal", state);

        // Private environment methods
        methods = {
            init: function() {
               // $.htmlEngine.loadFiles(state.worker.parentNode, state.opts.name, state.opts.cssFiles);
                if(!state.worker.parentNode){
                    state.worker.parentNode = state.worker.node.parent();
                }
                methods.html.create();
            },
            html:{
                create: function(){
                    state.worker.node.pageModal({fade: state.opts.fade});
                    state.worker.node.on('click', function(){
                        state.worker.node.pageModal('show', {
                            header: state.opts.title ? state.opts.title : "Set the header value.",
                            glyph: state.opts.glyph,
                            body: function(container){
                                container.css({overflow: 'hidden'});
                                var viewer = $(document.createElement('div')).css({
                                    clear: 'both',
                                    display: 'block',
                                    overflow: 'scroll',
                                    height: '400px',
                                    marginLeft: '5px',
                                    maxHeight: '400px'});
                                container.append(viewer);
                                var treeView = $.htmlEngine.plugins.get('treeView');
                                if(treeView) {
                                    container.on('treeNodeLeftClick', function(e){
                                        var item = e.node.data('item');
                                        state.selected = item;
                                        state.worker.node.pageModal('hide');
                                        state.trigger('modalSelected', [{selected: {displayed:state.selected[state.opts.mapper.label], value:state.selected[state.opts.mapper.id]}, item: item}]);
                                    });
                                    var opt = {
                                        parentNode: viewer.parent(),
                                        node: viewer,
                                        onDelete: state.opts.onDelete,
                                        schema: {
                                            plugin: state.opts
                                        }
                                    };
                                    opt.schema.plugin.viewerNode = $(".viewer");
                                    opt.schema.plugin.loader = true;
                                    opt.parentNode = viewer.parent();
                                    opt.node = viewer;
                                    opt.onDelete = state.opts.onDelete;
                                    treeView(viewer, opt);
                                }
                            },
                            footer: null,
                            hasClose: true
                        });
                    });
                }
            }
        };
        //public methods
        state.value = function(){
            return state.selected;
        };
        state.show = function(){
            state.worker.node.click();
        };

        //environment: Initialize
        methods.init();
    };


    //Plugin Function
    $.fn.treeModal = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.treeModal($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $treeModal = $(this).data('treeModal');
            switch (method) {
                case 'show': return $treeModal.show();break;
                case 'value': return $treeModal.value();break;
                case 'state':
                default: return $treeModal;
            }
        }
    };

    $.treeModal.call= function(elem, options){
        elem.treeModal(options);
    };

    try {
        $.htmlEngine.plugins.register("treeModal", $.treeModal.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
