;(function ($) {

    //Object Instance
    $.principalModal = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.principalTypes =[
            {type: '/people/person', label: 'User', glyph: 'glyphicons glyphicons-user'},
            {type: '/acs/security/authorization/group', label: 'Group', glyph: 'glyphicons glyphicons-group'}
        ];

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.principalModal.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        // Store a reference to the environment object
        el.data("principalModal", state);

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
                                if(state.opts.get.rootUrl()){
                                    var s = function(data){
                                        if(data) {
                                            methods.html.results(container, data);
                                        }
                                        else{
                                            state.worker.node.pageModal('hide');
                                        }
                                    };
                                    var f = function(){
                                    };
                                    $.htmlEngine.request(state.opts.get.rootUrl(), s, f, null, 'get');
                                }
                            },
                            footer: null,
                            hasClose: true
                        });
                    });
                },
                results: function(container, data){
                    data.results = $.jCommon.array.sort(data.results, [{property: "title", asc: true}]);
                    var outer = $(document.createElement('div')).addClass('panel-contain');
                    container.append(outer);
                    $.each(state.principalTypes, function(){
                        var pt = this;
                        var items = data[pt.type];
                        if(items && items.length>0){
                            var pnl = $(document.createElement('div'));
                            outer.append(pnl);
                            var group = $(document.createElement('div')).addClass('results-group');
                            $.each(items, function(){
                                var item = this;
                                var result = $(document.createElement('div')).addClass('result-group-item selectable').attr('data-name', item.name).css({cursor: 'pointer'});
                                var title = $(document.createElement('div')).html(item.title).css({marginLeft: '10px'});
                                result.append(title);
                                if(item.description){
                                    var desc = $(document.createElement('div')).html(item.description).css({marginLeft: '15px', marginRight: "15px"});
                                    result.append(desc);
                                }
                                group.append(result);
                                result.on("click", function(){
                                    state.selected = item;
                                    state.worker.node.pageModal('hide');
                                    var event = jQuery.Event('modalSelected', {selected: {displayed:item[state.opts.mapper.title], value: item[state.opts.mapper.value]}, item: item});
                                    state.trigger(event);
                                });
                            });
                            pnl.panel({
                                glyph: pt.glyph,
                                title: pt.label,
                                url: null,
                                borders: false,
                                content: group,
                                actions: []
                            });
                        }
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
    $.principalModal.defaults = {
    };


    //Plugin Function
    $.fn.principalModal = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.principalModal($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var principalModal = $(this).data('principalModal');
            switch (method) {
                case 'show': return principalModal.show();break;
                case 'value': return principalModal.value();break;
                case 'state':
                default: return principalModal;
            }
        }
    };

    $.principalModal.call= function(elem, options){
        elem.principalModal(options);
    };

    try {
        $.htmlEngine.plugins.register("principalModal", $.principalModal.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
