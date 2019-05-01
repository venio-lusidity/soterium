

;(function ($) {

    //Object Instance
    $.image = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data};
        state.opts = $.extend({}, $.image.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("image", state);

        // Private environment methods
        methods = {
            init: function () {
                $.htmlEngine.loadFiles(state.worker.node, state.opts.name, state.opts.cssFiles);
                state.worker.node.children().remove();
                try {
                    methods.checkDebug(state.opts);
                    methods.conditional();
                    methods.process();
                }
                catch (e) {
                    console.log(e);
                }
            },
            checkDebug: function (worker) {
                if (worker.schema && worker.schema.debug) {
                    console.log('debug in schema');
                }
                return true;
            },
            conditional: function () {
                // TODO: implement this.
            },
            process: function () {
                if (state.opts.image) {
                    var img = dCrt("img").attr("src", state.opts.image);
                    if (state.opts.cls) {
                        img.addClass(state.opts.cls);
                    }
                    img.on("imageLoaded", function () {
                        state.worker.node.append(img);
                        state.worker.parentNode.attr('data-valid', true).show();
                    });
                    $.jCommon.image.setClass(img, state.opts.image);
                }
                else if (state.opts.glyph) {
                    var glyph = dCrt('span').addClass(state.opts.glyph);
                    if (state.opts.cls) {
                        glyph.addClass(state.opts.cls);
                    }
                    if (state.opts.css) {
                        glyph.css(state.opts.css);
                    }
                    state.worker.node.append(glyph);
                    state.worker.parentNode.attr('data-valid', true).show();
                }
                else {
                    var url = url = $.jCommon.json.getProperty(state.worker, state.opts.property);

                    if (null !== url && undefined !== url) {
                        var on = 6;
                        var onSuccess = function (data) {
                            if ($(data).length > 0) {
                                state.worker.data = data;
                                methods.loaded();
                                if (state.opts.refresh < 4) {
                                    state.opts.refresh++;
                                    window.setTimeout(function () {
                                        lusidity.environment('getImages', {
                                            url: url,
                                            onSuccess: onSuccess,
                                            onError: null
                                        });
                                    }, 30000);
                                }
                            }
                            else {
                                if (state.opts.retries < 60) {
                                    state.opts.retries++;
                                    window.setTimeout(function () {
                                        lusidity.environment('getImages', {
                                            url: url,
                                            onSuccess: onSuccess,
                                            onError: null
                                        });
                                    }, 2500);
                                }
                            }
                        };
                        lusidity.environment('getImages', {url: url, onSuccess: onSuccess, onError: null});
                    }
                }
            },
            show: function (elem, idx) {
                elem.attr('data-valid', true).show();
                if (idx <= 1 && elem.parent()) {
                    methods.show(elem.parent(), (idx + 1));
                }
            },
            loaded: function () {
                var url = $.image.getPreferredImageUrl(state.worker, state);
                if (null == url || !url.isUrl) {
                    methods.showMissing(url);
                }
                else {
                    if (state.opts.lastValue === undefined || !$.jCommon.string.equals(state.opts.lastValue, url.original)) {
                        state.worker.node.show().css({visibility: "hidden"});

                        state.worker.parentNode.removeClass("property").addClass(state.opts.containerCls);
                        state.worker.node.remove();
                        state.worker.node = $.htmlEngine.createElement({type: "img", cls: state.opts.imgCls});
                        state.worker.parentNode.append(state.worker.node);

                        function loadError() {
                            var e = $.Event('engineImageLoadError');
                            e.imageNode = state.worker.node;
                            state.worker.node.trigger(e);
                            methods.showMissing(url);
                        }

                        state.worker.node.bind('imageLoaded', function (e) {
                            state.worker.node.css({visibility: "visible"});
                            state.worker.node.on('load', function () {
                                if (state.worker.missingNode) {
                                    state.worker.missingNode.hide();
                                }
                                var event = $.Event('engineImageLoaded');
                                event.imageNode = state.worker.node;
                                event.size = {width: state.worker.node.width(), height: state.worker.node.height()};
                                state.worker.node.trigger(event);

                                if (state.worker.missingNode) {
                                    state.worker.missingNode.hide();
                                }

                                if (state.opts.onImageLoaded && $.isFunction(state.opts.onImageLoaded)) {
                                    state.opts.onImageLoaded(event);
                                }

                                methods.show(state.worker.parentNode, 0);
                            });
                            state.worker.node.on('error', function () {
                                loadError();
                            });
                            state.worker.node.attr('src', url.original);
                            switch (state.worker.schema.effect) {
                                default:
                                    state.worker.node.fadeIn(300);
                                    break;
                                case 'slideDown':
                                    state.worker.node.slideDown(300);
                                    break;
                            }
                            methods.show(state.worker.parentNode, 0);
                        });

                        state.worker.node.bind('imageLoadError', function () {
                            loadError();
                        });

                        if (!state.worker.schema.value || !state.worker.schema.value.deferLoad) {
                            if (url.isSecure
                                && !$.jCommon.string.contains('s3.amazonaws.com')) {
                                $.jCommon.image.setClass(state.worker.node, url.original, 0);
                            }
                            else {
                                lusidity.environment('getImage', {
                                    element: state.worker.node,
                                    url: url.original,
                                    onLoad: function () {
                                        //TODO: need to finish this.
                                    },
                                    onError: function () {
                                        state.worker.node.hide();
                                    }
                                });
                            }
                        }
                        else {
                            state.worker.node.attr('data-defer', encodeURI(url.original));
                        }
                        state.worker.parentNode.attr('data-valid', true).show();
                        if (state.worker.parentNode.parent() && !state.worker.parentNode.parent().is(':visible')) {
                            state.worker.parentNode.parent().attr('display', '');
                        }
                    }
                    state.opts.lastValue = url.original;
                }
            },
            showMissing: function (url) {
                if (null === url) {
                    if (state.opts.showMissing) {
                        if (!state.worker.missingNode) {

                            state.worker.missingNode = $.htmlEngine.createElement({type: 'div', cls: 'no-image'});
                            var inner = $.htmlEngine.createElement({type: 'div'}, null, state.worker.data).html('No Image Available');

                            state.worker.missingNode.append(inner);
                            state.worker.missingNode.insertBefore(state.worker.parentNode);
                            state.worker.parentNode.attr('data-valid', false).hide();
                        }
                        methods.show(state.worker.missingNode, 0);
                    }
                    else {
                        return false;
                    }
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    $.image.getPreferredImageUrl = function(worker, state)
    {
        if($(worker.data).length<=0){
            return $.jCommon.url.create($.htmlEngine.getString(worker.data));
        }
        var providers = ((state.opts.preferredProviders) ?
            state.opts.preferredProviders : {
            "results": ["themoviedb", "openlibrary", "freebase", "/amazon", "wikipedia"]
        });
        var usages = ((state.opts.preferredUsage) ?
            state.opts.preferredUsage : {
            "results":  [ "tile", "preview", "thumbnail", "original" ]
        });
        var results = {
            tile: null,
            preview: null,
            thumbnail: null
        };
        var result=null;
        var images = $.jCommon.json.hasProperty(worker.data, "results", 0) ? worker.data.results : worker.data;
        $.each(providers.values, function(){
            var exists = false;
            var provider = this.toString();

            $.each(usages.values, function(){
                var usage = this.toString();

                function validateImage(imgData)
                {
                    var source = imgData["sourceUri"];
                    if($.jCommon.string.contains(source, provider, true))
                    {
                        var preferred = imgData[usage + "Uri"];
                        if(!$.jCommon.string.empty(preferred))
                        {
                            var uri = $.jCommon.url.create(preferred);
                            if(uri.isUrl)
                            {
                                result = uri;
                                exists = true;
                                return false;
                            }
                        }
                    }
                    return true;
                }

                if(images)
                {
                    $.each(images, function(){
                        return validateImage(this);
                    });
                }
                else
                {
                    validateImage(images);
                }

                if(exists){
                    return false;
                }
            });
            if(exists){
                return false;
            }
        });
        if(result===null){
            result = ((results.tile !== null) ? result = results.tile: (results.preview !== null) ?
                result = results.preview : (results.thumbnail !== null) ? result = results.thumbnail : null);
        }
        return result;
    };

    //Default Settings
    $.image.defaults = {
        retries: 0,
        refresh: 0,
        name: "image",
        maxItems: 1,
        imgCls: "large",
        containerCls: "cover-art",
        effect: "slideDown",
        isAsync: true,
        preferredUsage: {
            property: "recommendedUsage",
            values: [ "tile", "preview", "thumbnail", "original" ]
        },
        preferredProviders: {
            values: ["themoviedb", "openlibrary", "freebase", "/amazon", "wikipedia"]
        },
        "property": "parentData./vertex/uri",
        showMissing: true
    };


    //Plugin Function
    $.fn.image = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.image($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $image = $(this).data('image');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $image;
            }
        }
    };

    $.image.call= function(elem, options){
        elem.image(options);
    };

    try {
        $.htmlEngine.plugins.register("image", $.image.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
