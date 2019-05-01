;(function ($) {

    //Object Instance
    $.activityLog = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.activityLog.defaults, options);
        state.current = {
            items: [],
            content: {}
        };
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.KEY_FROM = "/object/endpoint/endpointFrom";
        var _selected = [];
        
        var _options = [{lbl: "approved", desc: "When an accounts approved", sel: true},
            {lbl: "assignment", desc: "Events that deal with adding, removing or updating a personnel position areas of responsibility", sel: true},
            {lbl: "authorization", desc: "Events that deal with adding, removing or updating an authorization to a vertex", sel: true},
            {lbl: "create", desc: "User creates data", sel: false},
            {lbl: "delete", desc: "HTTP delete request", sel: false, disabled: true},
            {lbl: "disapproved", desc: "When an accounts disapproved", sel: true},
            {lbl: "get", desc: "HTTP get request", sel: false, disabled: true},
            {lbl: "inactivated", desc: "Used for accounts that have been inactivated", sel: true},
            {lbl: "login", desc: "User logged in", sel: true},
            {lbl: "placement", desc: "Events that deal with adding, removing or updating a named personnel position within an organization as the position hired for", sel: true},
            {lbl: "post", desc: "HTTP post request", sel: false, disabled: true},
            {lbl: "put", desc: "HTTP put request", sel: false, disabled: true},
            {lbl: "registered", desc: "Used when a user has registered for a new account", sel: true},
            {lbl: "scoping", desc: "Events that deal with adding, removing or updating a personnel position areas of responsibility", sel: true},
            {lbl: "update", desc: "User updates data", sel: false, disabled: true},
            {lbl: "verified", desc: "Used when the system verifies a user account", sel: false}
        ];

        $.each(_options, function () {
           if(this.sel){
               _selected.push(this);
           }
        });
        

        // Store a reference to the environment object
        el.data('activityLog', state);
        //lusidity.environment('impersonate');
        // Private environment methods
        methods = {
            init: function() {
                methods.content.init();
                //state.leftBody = $.htmlEngine.panel(state.opts.pnlLeftNode, 'glyphicons glyphicons-list', sp, null,false, actions);
               // methods.content.make();
            },
            content: {
                init: function(usr){
                    methods.content.logs();
                },
                logs: function () {
                    state.opts.pnlMiddleNode.children().remove();
                    var container = dCrt('div').addClass('user-logs');
                    state.opts.pnlMiddleNode.append(container);
                    function tgl(glyph, item) {
                        if(glyph.hasClass("glyphicon-ok")){
                            if(_selected.length===1){
                                lusidity.info.red("One filter must be selected.  Selected another filter then deselect " + item.lbl + ".");
                                lusidity.info.show(5);
                                return false;
                            }
                            item.sel = false;
                            glyph.removeClass("glyphicon-ok font-green-dk").addClass("glyphicon-ban-circle font-red-dk");
                            var temp = [];
                            $.each(_selected, function () {
                               if(this.lbl !== item.lbl){
                                   temp.push(this);
                               }
                            });
                            _selected = temp;
                        }
                        else{
                            item.sel = true;
                            glyph.removeClass("glyphicon-ban-circle font-red-dk").addClass("glyphicon-ok font-green-dk");
                            _selected.push(item);
                        }
                        container.jFilterBar("reload")
                    }
                    var items = [];
                    $.each(_options, function () {
                        var item = this;
                        if(item.disabled){
                            return true;
                        }
                        var title = item.lbl;
                        items.push({
                            glyph: item.sel ? "glyphicon-ok font-green-dk" : "glyphicon-ban-circle font-red-dk",
                            title: title,
                            tooltip: item.desc,
                            item: item,
                            clicked: function (node, glyph, title, data) {
                                tgl(glyph, data.item);
                            }
                        })
                    });
                    var opts = {
                        title: "User Activity",
                        treed: false,
                        disableGrpAt: 0,
                        expandGrpAt: 0,
                        maxGroups: 2,
                        showFoundOnly: true,
                        group: {
                            limit: 0,
                            treed: false,
                            enabled: true,
                            store: 'acs_security_loging_user_activity',
                            partition: 'acs_security_loging_user_activity',
                            exclusions: [],
                            filters: [],
                            groups: [
                                {
                                    label: 'User',
                                    key: 'title',
                                    fKey: 'title',
                                    fValKey: 'value'
                                },
                                {
                                    label: 'Action',
                                    key: 'operationType',
                                    fKey: 'operationType',
                                    fValKey: 'value'
                                }
                            ]
                        },
                        actions: [
                            {
                                glyph: "glyphicon-filter",
                                title: "Filter",
                                stayOpen: true,
                                "items": items
                            }
                        ],
                        offset:{
                            parent: 0,
                            header: 0,
                            body: 0
                        },
                        grid: {
                            paging: {
                                enabled: true
                            },
                            offset: {
                                parent: 0,
                                table: -20
                            },
                            singleSort: true,
                            hovered: true,
                            keyId: 'lid',
                            filter: {
                                enabled: true,
                                nullable: false,
                                nullValue: "",
                                store: 'acs_security_loging_user_activity',
                                partition: 'acs_security_loging_user_activity',
                                properties: [{key: 'title', role: 'filter'},{key: 'operationType', role: 'filter'},{key: 'server', role: 'filter'}]
                            },
                            search: {
                                enabled: false,
                                text: "What are you looking for?",
                                btn: "Add",
                                properties: ['title', 'operationType', 'server', 'comment']
                            },
                            getQuery: function () {
                                var shoulds = [];
                                $.each(_selected, function () {
                                    shoulds.push({ term: {operationType: this.lbl}});
                                });
                                return {
                                    domain: 'acs_security_loging_user_activity',
                                    type: 'acs_security_loging_user_activity',
                                    "native": {
                                        query: {
                                            bool: {
                                                should: shoulds
                                            }
                                        }
                                    },
                                    sort: [
                                        {property: 'createdWhen', asc: false}, {property: 'title', asc: 'true'}
                                    ]
                                };
                            },
                            mapping: [
                                {header: {title: "#", css: {minWidth: '50px', width: '50px', maxWidth: '50px'}}, property: '#', type: 'integer', css: {minWidth: '50px', width: '50px', maxWidth: '50px'}},
                                {header: {title: 'When', property: 'createdWhen', css: {maxWidth: '150px'}}, property: 'createdWhen', type: 'string',  callback: function (td, item, value, map, filters) {
                                    if(value){
                                        td.append($.jCommon.dateTime.defaultFormat(value)).attr('title', value);
                                    }
                                }},
                                {header: {title: 'Who', property: 'title', css: {maxWidth: '200px'}}, property: 'title', type: 'string', callback: function (td, item, value, map, filters) {
                                    if(value){
                                        var lnk = dLink(value, item.whoId);
                                        td.append(lnk);
                                    }
                                }},
                                {header: {title: 'What', property: 'what', css: {maxWidth: '200px'}}, property: 'what', type: 'string', callback: function (td, item, value, map, filters) {
                                    if(value){
                                        var lnk = dLink(value, item.whatId);
                                        td.append(lnk);
                                    }
                                }},
                                {header: {title: 'Action', property: 'operationType', css: {maxWidth: '100px'}}, property: 'operationType', type: 'string', callback: function (td, item, value, map, filters) {
                                    if(value){
                                        // may need to rename these.
                                        td.append(value)
                                    }
                                }},
                                {header: {title: 'Server', property: 'server', css: {maxWidth: '100px'}}, property: 'server', type: 'string'},
                                {
                                    header: {title: 'Comment', property: 'comment'},
                                    property: 'comment',
                                    type: 'string',
                                    callback: function (td, item, value, map, filters) {
                                        if (value) {
                                            var v = value;
                                            if($.jCommon.is.object(v) || $.jCommon.is.array(v)){
                                                v = JSON.stringify(v);
                                            }
                                            if($.jCommon.string.contains(v, "[::]")){
                                                var pts = v.split('[::]');
                                                var txt = pts[0];
                                                var vals = pts[1].split(',');
                                                if(vals){
                                                    $.each(vals, function () {
                                                       txt += '<br/>'+this;
                                                    });
                                                }
                                                v = txt;
                                            }
                                            var nd = dCrt('div').append(v).css({position: 'relative'});
                                            td.append(nd);
                                            td.parent().prev().removeAttr('title', '');
                                            td.parent().prev().popover({
                                                container: 'body',
                                                placement: 'top',
                                                trigger: 'manual',
                                                animated: false,
                                                html: true,
                                                content: function(){
                                                    return dCrt('div').append(v).html();
                                                },
                                                title: 'Event Comment'
                                            });
                                            td.on('mouseover', function () {
                                                td.parent().prev().popover('show');
                                            });
                                            td.parent().parent().on('mouseleave', function () {
                                                var pops = $('body').find('.popover');
                                                pops.popover('hide');
                                            });
                                        }
                                    }
                                }
                            ]
                        }
                    };
                    container.jFilterBar(opts);
                }
            }
        };
        //public methods

        //Initialize
        methods.init();
    };

    //Default Settings
    $.activityLog.defaults = {
        limit: 1000
    };


    //Plugin Function
    $.fn.activityLog = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === 'object') {
            return this.each(function() {
                new $.activityLog($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $activityLog = $(this).data('activityLog');
            switch (method) {
                case 'exists': return (null!==$activityLog && undefined!==$activityLog && $activityLog.length>0);
                case 'state':
                default: return $activityLog;
            }
        }
    };

})(jQuery);
