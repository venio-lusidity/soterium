;(function ($) {

    //Object Instance
    $.treeViewCommon = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.treeViewCommon.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        state.current = {
            item: null,
            node: null,
            treeNode: null
        };

        // Store a reference to the environment object
        el.data("treeViewCommon", state);

        // Private environment methods
        methods = {
            init: function() {
               // $.htmlEngine.loadFiles(state.worker.node, state.opts.name, state.opts.cssFiles);
                methods.treeView.init();
                state.worker.node.attr('data-valid', true).show();
            },
            bind: function(){
                state.worker.node.on('treeNodeLeftClick', function(e) {
                    var item = e.node.data('item');
                    if ($.jCommon.string.startsWith(item['vertexType'], '/electronic/system/enclave')) {
                        state.current.item = methods.getData(item);
                        var cId = state.current.item[state.KEY_ID];
                        var id = item[state.KEY_ID];
                        if (e.node.icon && e.node.icon.hasClass('glyphicon-plus')) {
                            e.node.icon.click();
                        }
                        if (!$.jCommon.string.equals(cId, id)) {
                            methods.clear();
                            methods.html.tiles.init();
                        }
                        state.current.treeNode = e.node;
                    }
                    else {
                        window.open(item[state.KEY_ID]);
                    }
                });
            },
            ajax:{
                get: function(url, onSuccess, onFail, value, methodType) {
                    if(lusidity.info){
                        lusidity.info.hide();
                    }
                    state.opts.busy = true;
                    //noinspection JSUnusedLocalSymbols
                    var action = {
                        connector: null,
                        async: true,
                        data: (undefined!=value) ? JSON.stringify(value) : null,
                        methodType: (undefined!=methodType) ? methodType: 'get',
                        showProgress: false,
                        onbeforesend: {
                            message: {msg: null, debug: false},
                            execute: function () {
                            }
                        },
                        oncompleted: {
                            execute: function (jqXHR, textStatus) {
                            }
                        },
                        onsuccess: {
                            message: {msg: null, debug: false},
                            execute: function (data) {
                                if (onSuccess && $.isFunction(onSuccess)) {
                                    onSuccess(data);
                                }
                                else {
                                    lusidity.info.green("Although response was successful, I don't know what to do with it.");
                                    lusidity.info.show(5);
                                }
                            }
                        },
                        onfailure: {
                            message: {msg: null, debug: false},
                            execute: function (jqXHR, textStatus, errorThrown) {
                                if (onFail && $.isFunction(onFail)) {
                                    onFail(jqXHR, textStatus, errorThrown);
                                }
                                else {
                                    lusidity.info.red('Sorry, we cannot find the resource requested.&nbsp;&nbsp;'
                                    + 'You can try refreshing the&nbsp;<a href="' + window.location.href + '">page</a>.');
                                    lusidity.info.show(5);
                                }
                            }
                        },
                        url: url
                    };
                    lusidity.environment('request', action);
                }
            },
            url: function (start, limit) {
                return '/query?start=' + start + '&limit=' + limit ;
            },
            query:{
                root: function(data){
                    return {
                        domains: state.worker.data.primaryType,
                        "filters":[
                            {"operator": "must", "propertyName": "lid", "type": "raw", "value": state.worker.data[state.KEY_ID]}
                        ],
                        "sort":[{"propertyName": "title", "direction": "asc"}]
                    };
                },
                child: function(data){
                    if(data) {
                        return {
                            domains: ["/object/edge"],
                            "filters": [
                                {
                                    "operator": "must",
                                    "propertyName": "fromEndpoint",
                                    "type": "raw",
                                    "value": data["/vertex/uri"]
                                },
                                {
                                    "operator": "must",
                                    "propertyName": "label",
                                    "type": "raw",
                                    "value": "/electronic/system/infrastructure"
                                }
                            ],
                            "sort": [{"propertyName": "title", "direction": "asc"}]
                        };
                    }
                }
            },
            treeView: {
                init: function () {
                    state.worker.node.loaders({type: 'cube', css:{marginTop: '50px'}});
                    state.worker.node.on('treeViewDataLoaded', function(){
                        state.worker.node.unbind('treeViewDataLoaded');
                        state.worker.node.loaders('hide');
                    });
                    var treeView = $.htmlEngine.plugins.get('treeView');
                    if (treeView) {
                        treeView(state.worker.node, {
                            node: state.worker.node,
                            plugin: {
                                name: 'treeView',
                                mapper: {
                                    id: state.KEY_ID,
                                    uri: state.KEY_ID,
                                    label: 'title'
                                },
                                get: {
                                    rootQuery: function(data){
                                        return methods.query.root(data);
                                    },
                                    childQuery: function(data){
                                        return methods.query.child(data);
                                    }
                                },
                                post:{
                                    url: function(target){
                                        var id = target[state.KEY_ID];
                                        return (id ? id : target) + '/properties/electronic/system/infrastructure';
                                    },
                                    data: function(other){
                                        var id = other[state.KEY_ID];
                                        var vertexType = other['vertexType'];
                                        return {edgeDirection: "out", otherId: id ? id : other, vertexType: vertexType }
                                    }
                                },
                                viewerNode: $(".viewer"),
                                limit: 1000,
                                rootSelectable: true,
                                rootSelected: true
                            }
                        });
                    }
                },
                add: function(data){
                    state.worker.node.treeView('add', data);
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.treeViewCommon.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.treeViewCommon = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.treeViewCommon($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $treeViewCommon = $(this).data('treeViewCommon');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $treeViewCommon;
            }
        }
    };

    $.treeViewCommon.call= function(elem, options){
        elem.treeViewCommon(options);
    };

    try {
        $.htmlEngine.plugins.register("treeViewCommon", $.treeViewCommon.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
