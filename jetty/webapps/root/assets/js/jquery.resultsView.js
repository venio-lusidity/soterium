;(function ($) {

    //Object Instance
    $.resultsView = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.resultsView.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("resultsView", state);

        // Private environment methods
        methods = {
            init: function() {
                if(state.opts.data){
                    methods.html.init(state.opts.data);
                }
            },
            html:{
                init: function(data){
                    if(data.results && data.results.length>0) {
                        $.each(data.results, function(){
                            var item = this;
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
    $.resultsView.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.resultsView = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.resultsView($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $resultsView = $(this).data('resultsView');
            switch (method) {
                case 'exists': return (null!=$resultsView && undefined!=$resultsView && $resultsView.length>0);
                case 'state':
                default: return $resultsView;
            }
        }
    };

})(jQuery);
