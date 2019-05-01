;(function ($) {

    //Object Instance
    $.notification = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.notification.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("notification", state);

        // Private environment methods
        methods = {
            init: function() {
                methods.html.init();
                state.worker.node.attr('data-valid', true).show();
            },
            createNode: function(type, css, cls) {
                return $(document.createElement(type));
            },
            get: function (property, success, failed) {
                var url = state.worker.data[state.KEY_ID] + '/properties' + property;
                $.htmlEngine.request(url, success, failed);
            },
            html: {
                init: function () {
                    var c = dCrt('div');
                    state.worker.node.append(c);
                    var txt = 'There is a workflow process that requires your attention please click the link below:';
                    if(state.worker.data.completed){
                        txt = 'The workflow process has completed.'
                        + '<br/>To review the workflow process click the link or copy and paste the url below into your browser:'
                    }
                    var a = dCrt('div').html('There is a workflow').html(txt);
                    c.append(a).append(dCrt("br"));
                    methods.html.targets(c);
                    c.append(dCrt("br"));
                    c.append(dCrt("div").html("Initiated by:"));
                    c.append(dCrt("br"));
                    methods.html.initiated(c);

                    if(!state.worker.data.completed){
                        c.append(dCrt("br"));
                        c.append(dCrt("div").html("The following reviewers/approvers have been notified and can also perform this action:"));
                        c.append(dCrt("br"));
                        methods.html.auditors(c);
                    }
                },
                auditors: function (container) {
                    var data = state.worker.data['/acs/security/base_principal/peoples'];
                    if(data && data.results){
                        $.each(data.results, function () {
                            var item = this;
                            var c = dCrt('div');
                            var t = dCrt('a').attr('href', item[state.KEY_ID]).attr('target', '_blank').html(item.title);
                            c.append(t);
                            container.append(c);
                        });
                    }
                },
                initiated: function (container) {
                    var data = state.worker.data['/people/person/initiators'];
                    if(data && data.results){
                        $.each(data.results, function () {
                            var item = this;
                            var c = dCrt('div');
                            var t = dCrt('a').attr('href', item[state.KEY_ID]).attr('target', '_blank').html(item.title);
                            c.append(t);
                            container.append(c);
                        });
                    }
                },
                targets: function (container) {
                    var data = state.worker.data['/ta/data_vertex/targets'];
                    if(data && data.results){
                        $.each(data.results, function () {
                            var item = this;
                            if(!$.jCommon.string.equals(item.vertexType, '/process/workflow/workflow_step', true)) {
                                var c = dCrt('div');
                                var t = dCrt('a').attr('href', item[state.KEY_ID]).attr('target', '_blank').html(item.title);
                                c.append(t);
                                container.append(c);
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
    $.notification.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.notification = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.notification($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $notification = $(this).data('notification');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $notification;
            }
        }
    };

    $.notification.call= function(elem, options){
        elem.notification(options);
    };

    try {
        $.htmlEngine.plugins.register("notification", $.notification.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
