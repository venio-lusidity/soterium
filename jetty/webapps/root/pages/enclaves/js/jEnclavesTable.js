

;(function ($) {

    //Object Instance
    $.jEnclavesTable = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.jEnclavesTable.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("jEnclavesTable", state);

        // Private environment methods
        methods = {
            init: function() {
                try {
                    var url = String.format('{0}/hierarchy/details?detail={1}&view={2}',  data[state.KEY_ID], detail, state.opts.et_view);
                }
                catch (e){
                    console.log(e);
                }
            },
            make: function () {
                var h = state.height();
                var c = dCrt('div').css({minHeight: h+'px', height: h+'px'});
                state.append(c);
                c.on('filter-table-view-loaded', function (e) {
                    c.jFilterBar('resize');
                });
                c.jFilterBar(methods.options(state.opts.item));
            },
            options: function (item) {
                return {
                    filter: false,
                    view: "details",
                    title: "Assets",
                    realTime: true,
                    settings: null,
                    paging: false,
                    item: item,
                    getUrl: function (data, start, limit) {
                       return methods.url('asset', data);
                    },
                    sortable: true,
                    sortOn: [ {property: 'title', asc: true}],
                    lists: {
                        groups: [
                            {
                                label: 'System Name',
                                property: 'enclave',
                                hidden: false
                            },
                            {
                                label: 'Location',
                                property: 'location'
                            },
                            {
                                label: 'Managed By',
                                property: 'managed'
                            },
                            {
                                label: 'Owned By',
                                property: 'owned'
                            }
                        ]
                    },
                    actions: [
                    ],
                    details: {
                        minHeight: 600,
                        keyId: 'lid',
                        limit: (item._sd && item._sd.count.count) ? item._sd.count.count : 100,
                        filter: {
                            enabled: true,
                            properties: ['title', 'ditpr', 'ditprAltId', 'location', 'managed', 'owned' ]
                        },
                        search: {
                            enabled: true,
                            text: "Build a list",
                            btn: "Add",
                            properties: ['title', 'ditpr', 'ditprAltId', 'location', 'managed', 'owned' ]
                        },
                        onFiltered: function (key, value) {

                        },
                        onBefore: function (items) {
                            $.each(items, function () {
                                this.location = (this.location ? this.location.toUpperCase() : null);
                                this.owned = (this.owned ? this.owned.toUpperCase() : null);
                                this.managed = (this.managed ? this.managed.toUpperCase(): null);
                            });
                            return $.jCommon.array.sort(items, [{property: "title", asc: true}]);
                        },
                        mapping: [
                            {header: {title: "#", css: {width: '20px'}}, property: '#', type: 'integer', css: {width: '20px'}},
                            {header: { title: "Asset", property: 'title', css: {minWidth: '132px'}}, property: 'title', type: 'string', callback: function (td, item, value) {
                                var a = dCrt('a').attr('href', item.relatedId).attr('target', "_blank").html(value);
                                td.append(a);
                            }},
                            {header: { title: "System Name", property: 'ditpr'}, property: 'ditpr', type: 'string', callback: function (td, item, value) {
                                if(item.ditpr || item.enclave) {
                                    var a = dCrt('a').attr('href', item.ditprId).attr('target', "_blank").html(item.ditpr);
                                    td.append(a);
                                }
                            }},
                            {header: { title: "DITPR ID", property: 'ditprAltId', css: {width: '80px'}}, property: 'ditprAltId', type: 'string', callback: function (td, item, value) {
                                if(item.ditprAltId) {
                                    var a = dCrt('a').attr('href', item.ditprId).attr('target', "_blank").html(value);
                                    td.append(a);
                                }
                            }},
                            {header: { title: "Location", property: 'location'}, property: 'location', type: 'string', callback: function (td, item, value) {
                                if(item.location) {
                                    var a = dCrt('a').attr('href', item.locationId).attr('target', "_blank").html(value);
                                    td.append(a);
                                }
                            }},
                            {header: { title: "Managed By", property: 'managed'}, property: 'managed', type: 'string', callback: function (td, item, value) {
                                if(item.managed) {
                                    var a = dCrt('a').attr('href', item.managedId).attr('target', "_blank").html(value);
                                    td.append(a);
                                }
                            }},
                            {header: { title: "Owned By", property: 'owned'}, property: 'owned', type: 'string', callback: function (td, item, value) {
                                if(item.owned) {
                                    var a = dCrt('a').attr('href', item.ownedId).attr('target', "_blank").html(value);
                                    td.append(a);
                                }
                            }}
                        ]
                    }
                };
            },
            exists: function (node) {
                return (node && (node.length>0));
            },
            url: function (detail, data) {
                return String.format('{0}/hierarchy/details?detail={1}&view={2}',  data[state.KEY_ID], detail, state.opts.et_view);
            }
        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.jEnclavesTable.defaults = {};


    //Plugin Function
    $.fn.jEnclavesTable = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.jEnclavesTable($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $jEnclavesTable = $(this).data('jEnclavesTable');
            switch (method) {
                case 'exists': return (null!==$jEnclavesTable && undefined!==$jEnclavesTable && $jEnclavesTable.length>0);
                case 'state':
                default: return $jEnclavesTable;
            }
        }
    };

})(jQuery);
