;
(function ($) {

    //Object Instance
    $.organization = function (el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.organization.defaults, options);

        // Store a reference to the environment object
        el.data("organization", state);

        // Private environment methods
        methods = {
            init: function () {
                methods.html.menuBar();
                methods.html.treeView();
            },
            html: {
                menuBar: function () {
                    $('#menu-bar').menuBar({target: $('.viewer'), add: "Add"});
                },
                treeView: function () {
                    var treeView = $.htmlEngine.plugins.get('treeView');
                    state.root = $(".organizations");
                    if (treeView) {
                        treeView(state.root, {
                            propertyNode: state.root,
                            valueNode: $(".organizations-list"),
                            plugin: {
                                name: 'treeView',
                                mapper: {
                                    id: '/vertex/uri',
                                    uri: '/vertex/uri',
                                    title: 'title'
                                },
                                get: {
                                    rootUrl: function(data){
                                        return '/organization';
                                    },
                                    propertyUrl: function(data){
                                        return '/properties/organization/organization/branch';
                                    }
                                },
                                defaultPhrase: 'organizations',
                                viewerNode: $(".viewer"),
                                limit: 100
                            }
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
    $.organization.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.organization = function (method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function () {
                new $.organization($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $organization = $(this).data('organization');
            switch (method) {
                case 'some method':
                    return 'some process';
                case 'state':
                default:
                    return $organization;
            }
        }
    };

})(jQuery);
