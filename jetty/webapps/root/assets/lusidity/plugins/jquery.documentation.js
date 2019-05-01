

;(function ($) {

    //Object Instance
    $.documentation = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.documentation.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("documentation", state);

        // Private environment methods
        methods = {
            init: function() {
                if(state.opts.url) {
                    state.worker.node.attr('data-valid', true).show();
                    var s = function (data) {
                        methods.html.init(data);
                    };
                    var f = function () {
                    };
                    $.htmlEngine.request(state.opts.url, s, f, null, 'get');
                }
            },
            html:{
                init: function (data) {
                    var node = state.worker.node;
                    if(state.opts.title && state.opts.glyph){
                        node = $.htmlEngine.panel(state.worker.node, state.opts.glyph, state.opts.title, null, state.opts.borders, null, null)
                    }
                    var u = dCrt('ul').addClass("list-group");
                    state.worker.node.append(u);
                    var items = $.jCommon.array.sort(data, [{property: 'name', asc: true}]);
                    $.each(items, function () {
                        var item = this;
                        var li = $(document.createElement('li')).addClass("list-group-item");
                        var txt = $.jCommon.string.ellipsis(item.name, 32);
                        var a = $(document.createElement('a')).attr('title', item.name).attr('href', item.href).attr('target', '_blank');
                        if(item.fileType){
                            if(!$.jCommon.string.equals(txt, item.name)) {
                                txt += item.fileType;
                            }
                            var s = dCrt('span').addClass('filetypes filetypes-' + item.fileType).css({marginRight: '5px', color: '#333'});
                            a.prepend(s);
                        }
                        li.append(a.append(txt));
                        u.append(li);
                    });
                }
            }
        };

        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.documentation.defaults = {
        borders: false,
        glyph: "glyphicons glyphicons-book"
    };


    //Plugin Function
    $.fn.documentation = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.documentation($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $documentation = $(this).data('documentation');
            switch (method) {
                case 'exists': return (null!==$documentation && undefined!==$documentation && $documentation.length>0);
                case 'state':
                default: return $documentation;
            }
        }
    };

    $.documentation.call= function(elem, options){
        elem.documentation(options);
    };

    try {
        $.htmlEngine.plugins.register("documentation", $.documentation.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
