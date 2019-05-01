;(function ($) {

    //Object Instance
    $.mindMap = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.mindMap.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        var center = {x: 0, y: 0};
        var level = {
            summary: "summary",
            complete: "complete"
        };

        // Store a reference to the environment object
        el.data("mindMap", state);

        // Private environment methods
        methods = {
            init: function() {
               // $.htmlEngine.loadFiles(state.worker.propertyNode, state.opts.name, state.opts.cssFiles);
                methods.html.create();
                state.worker.propertyNode.attr('data-valid', true).show();
            },
            get: function(url, onSuccess, onFail) {
                //noinspection JSUnusedLocalSymbols
                var action = {
                    connector: null,
                    async: true,
                    data: null,
                    methodType: 'get',
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
                                lusidity.info.show(10);
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
                                lusidity.info.show(10);
                            }
                        }
                    },
                    url: url
                };
                lusidity.environment('request', action);
            },
            getUri: function(data){
                return data['otherId'] ? data['otherId'] : data["/vertex/uri"];
            },
            getId: function(data){
                var id = methods.getUri(data);
                if(id){
                    id = $.jCommon.string.replaceAll(id, "/", "_");
                }
                return id;
            },
            getTitle: function(data){
                var result = data.title;
                if(!result) {
                    var titles = data['title'];
                    var uri = data['/vertex/uri'];
                    if (null != titles && null != uri) {
                        result = titles.values[0].value;
                    }
                }
                return result;
            },
            getRandomClass: function(){
                var c;
                var n = methods.getRandomInt(1, 3);
                switch (n){
                    case 1:
                        c = "red";
                        break;
                    case 2:
                        c = "yellow";
                        break;
                    case 3:
                        c = "green";
                        break;
                    default:
                        c = 'grey';
                        break;
                }
                return c;
            },
            getRandomInt: function (min, max) {
                return Math.floor(Math.random() * (max - min + 1)) + min;
            },
            html:{
                create: function(){
                    var height = state.opts.height;
                    if(height==="inherit"){
                        height = state.worker.valueNode.height();
                    }
                    var width = state.opts.width;
                    if(width==="inherit"){
                        width = state.worker.valueNode.width();
                    }
                    height += state.opts.adjust.height;
                    width += state.opts.adjust.width;
                    state.opts.viewerNode = $(document.createElement('div'))
                        .css({position: "relative", height: height, width: width, minHeight: height, minWidth: width});
                    state.worker.valueNode.append(state.opts.viewerNode);
                    center.x = state.opts.viewerNode.width()/2;
                    center.y = state.opts.viewerNode.height()/2;
                    var mapOptions = {cls: 'circle', svgOffset:{ top: 0, left: 0}};
                    if(state.opts.options){
                       mapOptions = $.extend({}, mapOptions, state.opts.options)
                    }
                    state.opts.viewerNode.mindmap(mapOptions);
                    var enclave = {
                        start: 0,
                        limit: 5
                    };
                    enclave.root = methods.html.device(state.worker.parentData, state.worker.parentData.terrainScore, state.opts.viewerNode, null);
                    if(null!=enclave.root){
                        var success = function(data){
                            enclave.devices = data;
                            methods.html.addDevices(enclave);
                        };
                        var url = methods.getUri(state.worker.data) + "/properties/electronic/system/infrastructure?sort=desc&limit=5";
                        methods.get(url, success, null);
                    }

                },
                addDevices: function(enclave){
                    var on = 0;
                    var len = enclave.devices.results.length;
                    var size = 0;

                    $.each(enclave.devices.results, function(){
                        if(on>=enclave.start) {
                            var item = this;
                            if (size >= enclave.limit) {
                                enclave.start = on;
                                var onClick = function(e, node){
                                    var node = state.opts.viewerNode.mindmap("removeNode", node);
                                    methods.html.addDevices(enclave);
                                };
                                var node = methods.html.device(null, "more...", state.opts.viewerNode, enclave.root, onClick, onClick);
                                return false;
                            }
                            var onSuccess = function (item) {
                                var leftClick = function(e, node){
                                    if(null!=node && undefined != node) {
                                        node.runDefault = true;
                                    }
                                };
                                var rightClick = function(e, node){
                                    if(null!=node && undefined != node) {
                                        node.runDefault = false;
                                        var uri = node.el.attr('uri');
                                        lusidity.environment("openUri", {uri: uri, focus: false});
                                    }
                                };
                                var node = methods.html.device(item, null, state.opts.viewerNode, enclave.root, leftClick, rightClick);
                            };
                            var url = methods.getUri(item);
                            methods.get(url + "?level=" + level.summary, onSuccess, null);
                            size++;
                        }
                        on++;
                    });
                },
                device: function(data, title, container, parent, leftClick, rightClick){
                    var result = null;
                    var uri = methods.getUri(data);
                    var isEnclave = $.jCommon.string.startsWith(data.vertexType, "/electronic/network/system/enclave", true);
                    if(null!=uri) {
                        var tip = methods.getTitle(data);
                        result = $(document.createElement('div'))
                            .attr('uri', uri).attr('id', 'node_' + methods.getId(data))
                            .css({top: center.y + 'px', left: center.x + 'px'}).addClass('cvss');
                        container.append(result);
                        var score = $(document.createElement("div")).addClass('score shadowed');

                        var clr,type;
                        if(data.metrics) {
                            result.addClass($.cvssMetrics.color.getAxisColor(data.metrics));
                            type = elementTypes[data["vertexType"]];
                            if(!type){
                                console.log("unknown type: " + data["vertexType"]);
                            }
                            else{
                                title = type;
                            }
                            clr = $.cvssMetrics.color.getAxisColor(data.metrics);
                        }
                        if(isEnclave){
                            result.removeClass("circle").addClass("enclave");
                        }
                        if(state.opts.options.named){
                            title = methods.getTitle(data);
                            score.html(title);
                        }
                        result.addClass(clr ? clr : 'blue').append(score);

                        if(null!=tip){
                            result.attr('data-toggle', "tooltip").attr('title', tip);
                        }
                        result = container.mindmap('addNode',{
                            parent: parent,
                            node: result,
                            name: title,
                            underlying: data,
                            leftClick: function(e, node){
                                if(leftClick && $.isFunction(leftClick)){
                                    leftClick(e, node);
                                }
                            },
                            rightClick: function(e, node){
                                if(rightClick && $.isFunction(rightClick)){
                                    rightClick(e, node);
                                }
                            }
                        });
                    }
                    return result;
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.mindMap.defaults = {
        limit: 30,
        height: 300,
        width: 300,
        adjust: {
            width: 0,
            height: 0
        }
    };


    //Plugin Function
    $.fn.mindMap = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.mindMap($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $mindMap = $(this).data('mindMap');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $mindMap;
            }
        }
    };

    $.mindMap.call= function(elem, options){
        elem.mindMap(options);
    };

    try {
        $.htmlEngine.plugins.register("mindMap", $.mindMap.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
