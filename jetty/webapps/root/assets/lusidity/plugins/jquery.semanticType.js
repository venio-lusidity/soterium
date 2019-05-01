;(function ($) {

    //Object Instance
    $.semanticType = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.semanticType.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        if(!state.worker.parentNode){
            state.worker.parentNode = state.worker.node.parent();
        }

        // Store a reference to the environment object
        el.data("semanticType", state);

        // Private environment methods
        methods = {
            init: function() {
                state.worker.node.addClass('semantic-type').children().remove();
                $.htmlEngine.loadFiles(state.worker.parentNode, state.opts.name, state.opts.cssFiles);

                var type = (state.worker.data.relatedType ? state.worker.data.relatedType : (state.worker.data.vertexType ? state.worker.data.vertexType : state.worker.data.uri));
                if(!type){
                    type = $.htmlEngine.getString(state.worker.data);
                }
                if($.jCommon.string.empty(type)){
                    type = 'entity';
                }
                var text = $.jCommon.string.getLast(type, '/');
                text = $.jCommon.string.replaceAll(text, "_", " ");
                text = $.jCommon.string.toTitleCase(text);
                if($.jCommon.string.equals(text, 'acas invalid asset', true)){
                    text = "Unmatched ACAS Only Asset";
                }
                text = FnFactory.toAcronym(text);
                var imgName = $.jCommon.string.replaceAll(text.toLowerCase(), " ", "_");
                if(state.opts.image && state.opts.image !== true){
                    state.worker.node.append(state.opts.image, text);
                }
                else if(state.opts.image){
                    if($.jCommon.string.equals(type, '/electronic/network/asset')
                    || $.jCommon.string.equals(type, '/electronic/network/acas_invalid_asset')
                    || $.jCommon.string.equals(type, '/electronic/device')){
                        methods.osIcon(imgName);
                    }
                    else {
                        var imgUrl = '/assets/img/types/' + imgName + '.png';
                        var image = $.htmlEngine.createElement({
                            type: 'img',
                            attr: {src: imgUrl, alt: text},
                            'cls': 'type-image',
                            title: text
                        });
                        image.css({
                            'float': state.opts['float'],
                            margin: (state.opts['float'] === 'right') ? '0 0 0 5px' : '0 5px 0 0',
                            height: state.opts.height
                        });
                        $.jCommon.image.setClass(image, imgUrl);
                        image.on("imageLoaded", function () {
                            state.worker.node.append(image);
                        });
                        image.on("imageLoadError", function () {
                            image.attr('src', '/assets/img/types/entity.png');
                            state.worker.node.append(image);
                        });
                    }
                }

                if(!state.opts.imageOnly) {
                    var title = $.htmlEngine.createElement({
                        type: 'div',
                        cls: 'type-name'
                    }, null, state.worker.data).html(text);
                   state.worker.node.append(title);
                }

                state.worker.parentNode.attr('data-valid', true).show();
            },
            osIcon: function (imgName, imgTitle) {
                if($.isFunction($.fn.osIcon)){
                    state.worker.node.osIcon( {
                        imageName: imgName,
                        data: state.worker.data,
                        imgTitle: imgTitle,
                        imgName: imgName,
                        color: "#333333",
                        display: state.opts.display,
                        css: {padding: '0px 5px 0px 0px', fontSize: '14px', height: '18px', maxHeight: '18px'},
                        fontSize: '18px',
                        hasTitle: false,
                        hasVersion: false
                    });
                }
                else {
                    window.setTimeout(function () {
                        methods.osIcon();
                    }, 100);
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.semanticType.defaults = {
        'float': 'left',
        image: false,
        imageOnly: false,
        height: '18px',
        display: 'details'
    };


    //Plugin Function
    $.fn.semanticType = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.semanticType($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $semanticType = $(this).data('semanticType');
            switch (method) {
                case 'state':
                default: return $semanticType;
            }
        }
    };

    $.semanticType.call= function(elem, options){
        elem.semanticType(options);
    };

    try {
        $.htmlEngine.plugins.register("semanticType", $.semanticType.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
