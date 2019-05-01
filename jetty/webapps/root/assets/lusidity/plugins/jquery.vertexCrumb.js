

;(function ($) {

    //Object Instance
    $.vertexCrumb = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.vertexCrumb.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.KEY_ORG = '/organization/organization/organizations';

        // Store a reference to the environment object
        el.data("vertexCrumb", state);

        // Private environment methods
        methods = {
            init: function() {
               // $.htmlEngine.loadFiles(state.worker.propertyNode, state.opts.name, state.opts.cssFiles);
                methods.html.init(state.worker.node);
                state.worker.node.attr('data-valid', true).show();
            },
            html:{
                init: function (node) {
                    var s = function (data) {
                        if(data && data.results){
                            var bc = $('.breadcrumb-zone');
                            bc.show();
                            var ol = dCrt('ol').addClass('breadcrumb no-radius');
                            node.append(ol);
                            $.each(data.results, function () {
                                var item = this;
                                var l = dCrt('li');
                                var a = dCrt(state.opts.linked ? 'a' : 'span').attr("href", item[state.KEY_ID]).attr('target', '_blank').html(item[state.KEY_TITLE]);
                                ol.append(l.append(a));
                            });
                            window.setTimeout(function () {
                                lusidity.resizePage(bc.height());
                                $('.content').css({marginTop: bc.height() + 'px'})
                            }, 300);
                        }
                    };
                    var f = function () {};
                    $.htmlEngine.request(state.worker.data[state.KEY_ID]+'/breadcrumb',s,f,null);
                }
            },
            createNode: function(type, css, cls) {
                return $(document.createElement(type));
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.vertexCrumb.defaults = {
        linked: false,
        direction: 'out',
        limit: 20
    };


    //Plugin Function
    $.fn.vertexCrumb = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.vertexCrumb($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $vertexCrumb = $(this).data('vertexCrumb');
            switch (method) {
                case 'exists': return (null!=$vertexCrumb && undefined!=$vertexCrumb && $vertexCrumb.length>0);
                case 'state':
                default: return $vertexCrumb;
            }
        }
    };

    $.vertexCrumb.call= function(elem, options){
        elem.vertexCrumb(options);
    };

    try {
        $.htmlEngine.plugins.register("vertexCrumb", $.vertexCrumb.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
