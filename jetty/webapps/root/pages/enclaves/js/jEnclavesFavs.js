

;(function ($) {

    //Object Instance
    $.jEnclavesFavs = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.jEnclavesFavs.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.current ={};

        // Store a reference to the environment object
        el.data("jEnclavesFavs", state);

        state.qry = {
            ditpr: {
                children: function (data) {
                    return QueryFactory.ditpr.children(data);
                }
            },
            org: {
                children: function (item) {
                    return QueryFactory.childOrganizations(item);
                }
            },
            loc: {
                children: function (item) {
                    return QueryFactory.childLocations(item);
                }
            }
        };

        // Private environment methods
        methods = {
            init: function() {
                var node;
                state.children().remove();
                state.on('treeViewDataLoaded', function () {
                    state.unbind('treeViewDataLoaded');
                });
                state.on('treeViewLoaded', function () {
                    if(node){
                        node.parent().prepend(node);
                        node.css({fontWeight: 'bold'});
                    }
                });
                state.on('treeNodeCreated', function (e) {
                    if(e.item && state.current.item && e.item.lid === state.current.item.lid){
                        $(e.item.node.find('.title-header')[0]).addClass('selected');
                    }
                });
                state.on('treeViewInitDataLoaded', function (e) {
                    state.current = e.item;
                });
                state.on('treeNodeDefaultSelected', function (e) {
                    node = e.node;
                });

                // todo verify that we don't need the bind.
                //methods.bindTree(state);
                state.treeView({
                    parentNode: state.parent(),
                    name: 'treeView',
                    onTreeNodeLeftClick: state.opts.onTreeNodeLeftClick,
                    mapper: {
                        id: state.KEY_ID,
                        uri: state.KEY_ID,
                        label: 'title'
                    },
                    get: {
                        rootQuery: false,
                        rootUri: function (data) {
                            return '/rmk/favorites/infrastructure';
                        },
                        childQuery: function (data) {
                            return ($.jCommon.string.equals(state.current.et_view, 'location', true) ? state.qry.loc.children(data) :
                                $.jCommon.string.startsWith(state.current.et_view, 'ditpr', true) ? state.qry.ditpr.children(data) : state.qry.org.children(data));
                        },
                        countQuery: function (data) {
                            var r ={
                                domain: '/object/edge/infrastructure_edge',
                                type: data.vertexType,
                                lid: data.lid,
                                "native": {"query":{"filtered":{"filter":{"bool":{"must":[{"match":{"/object/endpoint/endpointFrom.relatedId.raw": data.lid}},{"match":{"label.raw":"/electronic/base_infrastructure/infrastructures"}},{"match":{"deprecated":false}}]}}}}}
                            };
                            return r;
                        }
                    },
                    post: {
                        url: function (target) {
                            var id = target[state.KEY_ID];
                            return (id ? id : target) + '/properties/electronic/base_infrastructure/infrastructures';
                        },
                        data: function (other) {
                            var id = other[state.KEY_ID];
                            var vertexType = other['vertexType'];
                            return {
                                edgeDirection: "out",
                                otherId: id ? id : other,
                                vertexType: vertexType
                            }
                        }
                    },
                    onDelete: function (node, data) {
                        node.hide();
                        var s = function (data) {
                            if (data && data.removed) {
                                node.remove();
                            }
                            else {
                                node.show();
                            }
                        };
                        $.htmlEngine.request("/rmk/favorites/infrastructure", s, s, data, 'delete');
                    },
                    limit: 10000,
                    allowDuplicates: true,
                    expandRoot: false,
                    expandable: false,
                    rootSelectable: true,
                    rootSelected: state.opts.rootSelected,
                    defaultSelectable: state.opts.rootSelected,
                    defaultCss: {"font-weight": "bold"},
                    isDraggable: false,
                    isDroppable: false,
                    deletable: true,
                    tooltip: 'title',
                    totals: true
                });
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
    $.jEnclavesFavs.defaults = {};


    //Plugin Function
    $.fn.jEnclavesFavs = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.jEnclavesFavs($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $jEnclavesFavs = $(this).data('jEnclavesFavs');
            switch (method) {
                case 'exists': return (null!==$jEnclavesFavs && undefined!==$jEnclavesFavs && $jEnclavesFavs.length>0);
                case 'state':
                default: return $jEnclavesFavs;
            }
        }
    };

})(jQuery);
