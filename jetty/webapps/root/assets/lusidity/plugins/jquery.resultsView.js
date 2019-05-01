;(function ($) {

    //Object Instance
    $.resultsView = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.resultsView.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("resultsView", state);

        // Private environment methods
        methods = {
            init: function() {
                state.worker.node.attr('data-valid', false).hide();
                var s = function(data){
                    if(data && data.results) {
                        methods.html.init(data);
                        state.worker.node.attr('data-valid', true).show();
                    }
                };
                var f = function(){
                };
                var url = state.worker.data[state.KEY_ID] + "/properties" + state.opts.property;
                if(state.opts.limit){
                    url += "?limit=" + state.opts.limit;
                }
                var results = state.worker.data[state.opts.property];
                if(results){
                    var data = {results: results};
                    s(data);
                }
                else {
                    $.htmlEngine.request(url, s, f, null, 'get');
                }
            },
            html:{
                init: function(data){
                    if(data.results && data.results.length>0) {
                        var all = {
                            name: "All"
                        };
                        methods.tabs.add(all, 'selected');

                        $.each(data.results, function(){
                            var item = this;
                            methods.tabs.add(item);

                            var row = $(document.createElement('div')).addClass('result').attr('data-name', item.name);
                            var header = $(document.createElement('h5'));
                            row.append(header);
                            var link = $(document.createElement('a')).attr('href', item.uri).attr('target', '_blank').html(item.title);
                            header.append(link);

                            if(item.externalUri){
                                var external = $(document.createElement('div')).addClass('external-link');
                                var externalLink = $(document.createElement('a')).attr('href', item.externalUri)
                                    .attr('target', '_blank').html(item.externalUri);
                                external.append(externalLink);
                                row.append(externalLink)
                            }

                            if(item.description){
                                var desc = $(document.createElement('div')).addClass('description');
                                var p = $(document.createElement('p')).html(item.description);
                                desc.append(p);
                                row.append(desc);
                            }

                            state.results.append(row);
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
    $.resultsView.defaults = {};


    //Plugin Function
    $.fn.resultsView = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.resultsView($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $resultsView = $(this).data('resultsView');
            switch (method) {
                case 'state':
                default: return $resultsView;
            }
        }
    };

    $.resultsView.call= function(elem, options){
        elem.resultsView(options);
    };

    try {
        $.htmlEngine.plugins.register("resultsView", $.resultsView.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
