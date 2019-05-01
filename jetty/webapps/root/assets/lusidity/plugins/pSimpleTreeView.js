;(function ($) {

    //Object Instance
    $.pSimpleTreeView = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.pSimpleTreeView.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("pSimpleTreeView", state);

        // Private environment methods
        methods = {
            init: function() {
                // $.htmlEngine.loadFiles(state.worker.propertyNode, state.opts.name, state.opts.cssFiles);
                state.worker.node.attr('data-valid', true).show();
                methods.html.init();
            },
            createNode: function(type, css, cls) {
                return $(document.createElement(type));
            },
            html: {
                init: function () {
                    state.tree = dCrt('div').addClass('tree');
                    state.worker.node.append(state.tree);
                    var root = dCrt('ul').css({margin: '2px 0 2px 0'});
                    state.tree.append(root);
                    var items = $.jCommon.is.array(state.opts.data) ? state.opts.data : null;
                    if(!null===items || items.length===0){
                        root.append(dCrt('li').html("No data."));
                    }
                    methods.html.nodes(root, items, null);
                },
                nodes: function (ul, items, groups) {
                    $.each(items, function () {
                        var item = this;

                        var grps = (null===groups) ? [] : $.jCommon.array.clone(groups);
                        grps.push(item);

                        var li = dCrt('li');
                        ul.append(li);
                        var hdr = dCrt('div').addClass('title-header').html(item.extValue? item.extValue : item.value);
                        li.append(hdr);
                        hdr.on('click', function () {
                            if(state.opts.showSelected){
                                state.tree.find('.selected').removeClass('selected');
                                hdr.addClass('selected');
                            }
                            if($.isFunction(state.opts.onTreeNodeClicked)){
                                state.opts.onTreeNodeClicked({item: li.jStorage('data')})
                            }
                        });
                        var id = $.jCommon.getRandomId('li');
                        li.attr('id', id);

                        var sub = dCrt('ul').attr('id', id+'_ul').hide();
                        li.append(sub);

                        li.jStorage({groups: grps, item: item, id: id, hdr: hdr, parentData: state.opts.parentData});
                        state.opts.nodeCount({item: li.jStorage('data')});
                        var items = this[state.opts.key] ? this[state.opts.key] : null;
                        var icon = dCrt('span').addClass('glyphicon glyphicon-plus').hide();
                        if(state.opts.grouped>grps.length){
                            icon.show();
                        }
                        li.prepend(icon);
                        li.icon = icon;

                        icon.on('click', function () {
                            if(icon.hasClass('glyphicon-plus')){
                                sub.slideDown();
                                if(!icon.hasClass('node-loaded')){
                                    icon.addClass('node-loaded');
                                    if($.isFunction(state.opts.onExpand)) {
                                        state.opts.onExpand({item: li.jStorage('data')});
                                    }
                                }
                                if($.isFunction(state.opts.nodeCount) && !icon.hasClass('node-counted')){
                                    icon.addClass('node-counted');
                                    $.each(sub.children(), function () {
                                        state.opts.nodeCount({item: $(this).jStorage('data')});
                                    });
                                }
                                icon.removeClass('glyphicon-plus').addClass('glyphicon-minus');
                            }
                            else{
                                sub.slideUp();
                                icon.removeClass('glyphicon-minus').addClass('glyphicon-plus');
                            }
                        });

                        /*
                        if($.isFunction(this.getChildren)){
                            if(!this.getChildren()){
                                return true;
                            }

                            function load() {
                                methods.html.nodes(chld, items, grps, (idx+1));
                            }



                        }
                        */
                    });
                }
            }
        };
        //public methods
        state.showExpand = function () {
            state.tree.find('.glyphicon-plus').show();
        };

        state.update = function (options) {
            var ul = $('#'+options.item.id+'_ul');
            if(ul.length>0) {
                state.opts.grouped = options.grouped;
                methods.html.nodes(ul, options.data.results, options.item.groups);
            }
        };


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.pSimpleTreeView.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.pSimpleTreeView = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.pSimpleTreeView($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $pSimpleTreeView = $(this).data('pSimpleTreeView');
            switch (method) {
                case 'exists': return (null!==$pSimpleTreeView && undefined!==$pSimpleTreeView && $pSimpleTreeView.length>0);
                case 'showExpand': $pSimpleTreeView.showExpand();break;
                case 'update': $pSimpleTreeView.update(options);break;
                case 'state':
                default: return $pSimpleTreeView;
            }
        }
    };

    $.pSimpleTreeView.call= function(elem, options){
        elem.pSimpleTreeView(options);
    };

    try {
        $.htmlEngine.plugins.register("pSimpleTreeView", $.pSimpleTreeView.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
