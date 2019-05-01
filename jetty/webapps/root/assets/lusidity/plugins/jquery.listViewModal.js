;(function ($) {

    //Object Instance
    $.listViewModal = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.listViewModal.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        // Store a reference to the environment object
        el.data("listViewModal", state);

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
                            header: state.opts.title ? state.opts.title : "Set the title value.",
                            glyph: state.opts.glyph,
                            body: function(container){
                                var s = function(data){
                                    if(data && data.results && data.results.length>0) {
                                        methods.html.results(container, data);
                                    }
                                    else{
                                        state.worker.node.pageModal('hide');
                                    }
                                };
                                var f = function(){
                                    state.worker.node.pageModal('hide');
                                };
                                if(state.opts.get.rootUrl && $.isFunction(state.opts.get.rootUrl)){
                                    $.htmlEngine.request(state.opts.get.rootUrl(), s, f, null, 'get');
                                }
                                else if(state.opts.get.query && $.isFunction(state.opts.get.query)){
                                    $.htmlEngine.request('/query?limit=100', s, f, state.opts.get.query(), 'post');
                                }
                            },
                            footer: null,
                            hasClose: true
                        });
                    });
                },
                results: function(container, data){
                    data.results = $.jCommon.array.sort(data.results, [{property: "title", asc: true}]);
                    $.each(data.results, function(){
                        var item = this;
                        var result = $(document.createElement('div')).addClass('result-group-item selectable').attr('data-name', item.name).css({cursor: 'pointer'});
                        var title = $(document.createElement('div')).html(item.title).css({marginLeft: '10px'});
                        result.append(title);
                        if(item.description){
                            var desc = $(document.createElement('div')).html(item.description).css({marginLeft: '15px', marginRight: "15px"});
                            result.append(desc);
                        }
                        container.append(result);
                        result.on("click", function(){
                            state.selected = item;
                            container.children().removeClass('selected');
                            result.addClass('selected');
                            state.worker.node.pageModal('hide');
                            state.trigger('modalSelected', [{selected: {displayed: item[state.opts.mapper.displayed], value: item[state.opts.mapper.value]}, item: item}]);
                        })
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

    //Default Settings
    $.listViewModal.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.listViewModal = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.listViewModal($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $listViewModal = $(this).data('listViewModal');
            switch (method) {
                case 'show': return $listViewModal.show();break;
                case 'value': return $listViewModal.value();break;
                case 'state':
                default: return $listViewModal;
            }
        }
    };

    $.listViewModal.call= function(elem, options){
        elem.listViewModal(options);
    };

    try {
        $.htmlEngine.plugins.register("listViewModal", $.listViewModal.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
