

;(function ($) {

    //Object Instance
    $.jEnclavesTree = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.jEnclavesTree.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        state.qry = {
            ditpr: {
                request: function (data, success, failed) {
                    var p = {
                        uri: data ? data[state.KEY_ID] : null
                    };
                    $.htmlEngine.request("/enclave/nodes", success, failed, p, 'post');
                }
            },
            org: {
                root: function (item, et_view) {
                    //return QueryFactory.rootDisa();
                    return QueryFactory.rootOrganization(item, et_view);
                },
                children: function (item, et_view) {
                    return QueryFactory.childOrganizations(item, et_view);
                }
            },
            loc: {
                root: function () {
                    return QueryFactory.rootLocation();
                },
                children: function (item) {
                    return QueryFactory.childLocations(item);
                }
            }
        };

        // Store a reference to the environment object
        el.data("jEnclavesTree", state);

        // Private environment methods
        methods = {
            init: function() {
                var qry = ($.jCommon.string.equals(state.opts.et_view, 'location', true) ? state.qry.loc :
                    $.jCommon.string.startsWith(state.opts.et_view, 'ditpr', true) ? state.qry.ditpr : state.qry.org);
                methods.create(qry);
            },
            create: function (qry) {
                state.children().remove();
                var label = 'title';
                if($.jCommon.string.equals(state.opts.et_view, 'ditprId', true)){
                    label = 'ditprId';
                }
                var node = dCrt('div');
                state.append(node);
                var opts = {
                    devOnly: false,
                    parentNode: state,
                    name: 'treeView',
                    onTreeNodeLeftClick: state.opts.onTreeNodeLeftClick,
                    mapper: {
                        id: state.KEY_ID,
                        uri: state.KEY_ID,
                        label: label
                    },
                    hasChildren: function (node, data) {
                    },
                    onTreeNodeCounted: function (e) {
                        if(e.node.icon && e.item.vertexType==='/organization/organization') {
                            var path = ['government', 'united states', 'department of defense'];
                            if ($.jCommon.string.equals(e.item.title, path, true)) {
                                e.node.icon.click();
                            }
                        }
                    },
                    sort: function (data) {
                        var r = data;
                        if(r && r.results){
                            r.results = $.jCommon.array.sort(r.results, [{property: 'title', asc: true}]);
                        }
                        return r;
                    },
                    limit: 1000,
                    rootSelectable: false,
                    rootSelected: false,
                    defaultSelectable: false,
                    isDraggable: false,
                    isDroppable: false,
                    totals: true,
                    tooltip: 'title'
                };

                if(qry.root){
                    opts.get = {
                        rootQuery: function (data) {
                            return qry.root(data, state.opts.et_view);
                        },
                        childQuery: function (data) {
                            return qry.children(data, state.opts.et_view);
                        }
                    };
                }
                else{
                    opts.get = {
                        request: function(data, success, failed){
                            qry.request(data, success, failed);
                        }
                    }
                }
                node.treeView(opts);
            },
            exists: function (node) {
                return (node && (node.length>0));
            }
        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.jEnclavesTree.defaults = {};


    //Plugin Function
    $.fn.jEnclavesTree = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.jEnclavesTree($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $jEnclavesTree = $(this).data('jEnclavesTree');
            switch (method) {
                case 'exists': return (null!==$jEnclavesTree && undefined!==$jEnclavesTree && $jEnclavesTree.length>0);
                case 'state':
                default: return $jEnclavesTree;
            }
        }
    };

})(jQuery);
