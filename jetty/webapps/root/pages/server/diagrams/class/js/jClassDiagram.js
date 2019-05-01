

;(function ($) {

    //Object Instance
    $.jClassDiagram = function (el, options) {
        var state = $(el),
            methods = {};
        state.container = $(el);
        state.opts = $.extend({}, $.jClassDiagram.defaults, options);
        // Store a reference to the environment object
        el.data("jClassDiagram", state);
        state.current = {};

        // Private environment methods
        methods = {
            init: function () {
                $.login.authorized({"groups": ["admin"], c: function (d) {
                    if(d.auth){
                        methods.get();
                    }
                    else{
                        window.location = d.redirectUrl;
                    }
                }});
            },
            get: function () {
                var s = function (data) {
                    state.current.data = data;
                    methods.html.init();
                };
                var f = function () {
                    lusidity.info.red("Sorry something went wrong, please try again.");
                    lusidity.info.show(5);
                };

                $.htmlEngine.request('/domains', s, f, null, 'get');
            },
            getClassObject: function (cls) {
                var r;
                $.each(state.current.data.results, function () {
                    if($.jCommon.string.equals(this.clsName, cls, true)){
                        r = this;
                        return false;
                    }
                });
                return r;
            },
            query: function (q) {
                state.opts.queryResults.children().remove();
                $.htmlEngine.busy(state, {type: 'cube', cover: true, adjustWidth: 0, adjustHeight: 0});
                var s = function (data) {
                    if(data){
                        var pre = dCrt('pre');
                        var j = $.jCommon.json.pretty(data);
                        pre.append(j);
                        state.opts.queryResults.append(pre);
                    }
                    state.loaders('hide');
                };
                var f = function (data) {
                    state.loaders('hide');
                };
                var start = state.opts.startNode.val();
                var limit = state.opts.limitNode.val();
                $.htmlEngine.request(String.format("/query?start={0}&limit={1}", start, limit), s, f, q, 'post');
            },
            html:{
                init: function () {
                    state.opts.limitNode.on('keyup', function () {
                        state.opts.startNode.val("0");
                        state.opts.startNode.attr('step', state.opts.limitNode.val());
                    });
                    state.opts.limitNode.on('change', function () {
                        state.opts.startNode.val("0");
                        state.opts.startNode.attr('step', state.opts.limitNode.val());
                    });
                    state.opts.pnlLeftNode.children().remove();
                    var c = dCrt('div');
                    state.opts.pnlLeftNode.append(c);
                    var body = $.htmlEngine.panel(c, "glyphicons glyphicons-tree-structure", "Soterium Classes", null, false);
                    state.treeNode = dCrt('div').addClass('tree');
                    body.append(state.treeNode);
                    var results = [];
                    results.push(state.current.data.structure);
                    methods.html.make(state.treeNode, results, 0);
                },
                make: function (node, items, lvl) {
                    items = $.jCommon.array.sort(items, [{property: 'simpleName', asc: true}]);
                    if(items.length>0) {
                        var ul = dCrt('ul');
                        node.append(ul);
                        var r = (lvl===0);
                        if(!r){
                            ul.hide();
                        }
                        $.each(items, function () {
                            var h = this.results && this.results.length>0;
                            var li = methods.html.getTreeNode(this, h, true);
                            if(h){
                                methods.html.make(li, this.results, (lvl+1));
                            }
                            ul.append(li);
                        });
                    }
                },
                getTreeNode: function (item, hasChild, plus) {
                    var li = dCrt('li');
                    var i = $.htmlEngine.glyph('glyphicons-list-alt').css({marginRight: '5px'});
                    var s = dCrt('span').addClass('node-title').html(item.simpleName);
                    var c = dCrt('div').attr('title', item.clsName);
                    s.on('click', function () {
                        state.treeNode.find('.selected').removeClass('selected');
                        c.addClass('selected');
                        state.current.item=item;
                        methods.html.query(item);
                        methods.html.content(item);
                    });
                    if(hasChild){
                        var p = $.htmlEngine.glyph(plus ? 'glyphicon-plus' : 'glyphicon-minus').css({marginRight: '5px', cursor: 'pointer'});
                        c.append(p);
                        p.on('click', function () {
                            if(p.hasClass('glyphicon-plus')) {
                                $(li.find('ul')[0]).slideDown();
                                p.removeClass('glyphicon-plus').addClass('glyphicon-minus');
                            }
                            else{
                                $(li.find('ul')[0]).slideUp();
                                p.removeClass('glyphicon-minus').addClass('glyphicon-plus');
                            }
                        });
                    }
                    return li.append(c.append(i).append(s));
                },
                getNode: function (item, style) {
                    var node = $(document.createElement('div'));
                    if (item) {
                        if (style) {
                            node.css(style);
                        }
                        var v = $.jCommon.is.numeric(item.value) ? $.jCommon.number.commas(item.value) : item.value;
                        if (item.label) {
                            var l = $(document.createElement('span')).css({fontWeight: 'bold'}).append(item.label);
                            var sep = $(document.createElement('span')).append(':').css({marginRight: '5px'});
                            node.append(l).append(sep);
                            v = $(document.createElement('span')).append(v)
                        }
                    }
                    return node.append(v);
                },
                query: function (item) {
                    state.opts.queryNode.val('');
                    var data = methods.getClassObject(item.clsName);
                    if(data && data.key && data.queryable) {
                        state.opts.btnSubmit.removeClass('btn-warning');
                        state.opts.btnSubmit.html('Submit');
                        state.opts.btnSubmit.removeAttr('disabled');
                        var j = QueryFactory.matchAll(data.key, data.key);
                        state.opts.queryNode.val($.jCommon.json.prettyPrint(j));

                        state.opts.btnSubmit.unbind();
                        state.opts.btnSubmit.on('click', function () {
                            var d;
                            try{
                                d = JSON.parse(state.opts.queryNode.val());
                            }catch (e){}
                            if(!d){
                                state.opts.queryNode.html("The JSON is not valid.");
                            }
                            else{
                                methods.query(d);
                            }
                        });
                    }
                    else{
                        state.opts.btnSubmit.attr('disabled', true);
                        state.opts.btnSubmit.addClass('btn-warning');
                        state.opts.btnSubmit.html('This class can not be queried.');
                    }
                },
                content: function (item) {
                    state.opts.pnlViewerNode.children().remove();
                    var node = dCrt('div');
                    state.opts.pnlViewerNode.append(node);
                    var data = methods.getClassObject(item.clsName);
                    if(!data){
                        var msg = dCrt('h4').html('Sorry, we could not find any data for that class.').css({margin: '10px 10px'});
                        node.append(msg);
                        return false;
                    }

                    var body = $.htmlEngine.panel(node, "glyphicons-list-alt", data.simpleName, null, false);
                    body.css({padding: '10px 10px'});

                    var p = [{k: 'clsName', l: "Class Name", d: 'The object class.'},{k: 'indexKey', l: 'Index Key', d: 'This is value is what would be considered a table, store, or index name.'}, {k: 'key', l: 'Key', d: 'This value is used for property names, usually combined with another value to make it unique and is used when querying using the REST API.'}];
                    $.each(p, function () {
                       var v = data[this.k];
                       if(v) {
                           var n = methods.html.getNode({value: v, label: this.l});
                           body.append(n);
                       }
                    });

                    if(data.properties) {
                        body.append(dCrt('h4').html("Properties").css({margin: '10px 0'}));

                        var ul = dCrt('list-group');
                        body.append(ul);

                        $.each(data.properties, function () {
                            var li = dCrt('li').addClass('list-group-item');
                            li.append(methods.html.getNode({label: "Property Name", value: this.name}));
                            li.append(methods.html.getNode({label: "Type:", value: this.type}));
                            var sp = this.schemaProperty;
                            if(sp){
                                var srt = Object.keys(sp);
                                var ulc = dCrt('list-group');
                                li.append(ulc);
                                $.each(srt, function (k, v) {
                                    var lic = dCrt('li').addClass('list-group-item');
                                    lic.append(methods.html.getNode({label: v, value: sp[v]}));
                                    ulc.append(lic)
                                });
                            }
                            ul.append(li);
                        });
                    }
                }
            }

        };

        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.jClassDiagram.defaults = {
    };


    //Plugin Function
    $.fn.jClassDiagram = function (method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function () {
                new $.jClassDiagram($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $jClassDiagram = $(this).data('jClassDiagram');
            switch (method) {
                case 'state':
                default:
                    return $jClassDiagram;
            }
        }
    };

})(jQuery);
