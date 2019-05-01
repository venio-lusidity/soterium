;
(function ($) {

    //Object Instance
    $.menuBar = function (el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.menuBar.defaults, options);
        state.btn = {};
        // Store a reference to the environment object
        el.data("menuBar", state);

        // Private environment methods
        methods = {
            init: function () {
                $.htmlEngine.loadFiles(state, null, ['/assets/js/form/css/menuBar.css']);
                methods.menu.create();
            },
            menu: {
                create: function () {
                    // state.css({minHeight: "48px"});
                    function getLi() {
                        return dCrt('li');
                    }

                    function getButton(txt, glyphicon, title, cls, id) {
                        var result = dCrt('button')
                            .css({height: '36px', maxHeight: '36px', padding: '6px 10px 5px 6px'})
                            .addClass('mb-btn btn ' + ($.jCommon.string.empty(cls) ? 'green': cls));
                        
                        var gs = $.jCommon.string.startsWith(glyphicon, 'glyphicons');
                        if(glyphicon) {
                            var icon = dCrt('span').addClass(
                                (gs ? 'glyphicons ':'glyphicon ') + glyphicon);
                            if(gs){
                                icon.css({position: 'relative', top: '-3px'})
                            }
                            result.append(icon);
                        }
                        if(txt){
                            result.append(dCrt('span').append(txt));
                        }

                        if(title){
                            result.attr('title', title);
                        }
                        if(id){
                            result.attr('id', id);
                        }

                        return result;
                    }
                    var ul = dCrt('ul').addClass('nav navbar-nav navbar-left menu-bar ls-menu-bar');
                    state.append(ul);
                    var li;
                    if (state.opts.separator) {
                       ul.append(getLi().append(dCrt('div').addClass('divider-vertical').html('&nbsp;')));
                    }
                    if (state.opts.buttons) {
                        $.each(state.opts.buttons, function(){
                            var item = this;
                            if($.jCommon.string.equals(item.name, 'sep')){
                                var sep = getLi().append(dCrt('div').addClass('divider-vertical').html('&nbsp;'));
                                ul.append(sep);
                                if(item.css){
                                    sep.css(item.css);
                                }
                            }
                            else {
                                var btn = getButton(item.name, item.glyphicon, item.title, item.cls, item.id);
                                if(item.disabled){
                                    btn.attr('disabled', 'disabled');
                                }
                                var li = getLi().append(btn);
                                if(item.css){
                                    btn.css(item.css);
                                }
                                ul.append(li);
                                if($.isFunction(item.onClick)){
                                    btn.on('click', function (e) {
                                        e.btn = btn;
                                        item.onClick(e);
                                    })
                                }
                                btn.on('click', function (e) {
                                    state.opts.target.trigger('menu-bar-' + item.tn, {event: e, btn: btn});
                                });
                            }
                        });
                    }

                    if (state.opts.add) {
                        state.btn.add = getButton(state.opts.add, 'glyphicon-plus');
                        ul.append(getLi().append(state.btn.add));
                        state.btn.add.on('click', function (e) {
                            state.opts.target.trigger('menu-bar-add');
                        });
                    }
                    if (state.opts.addDisabled) {
                        state.btn.addDisabled = getButton(state.opts.addDisabled, 'glyphicon-plus');
                        ul.append(getLi().append(state.btn.addDisabled));
                        state.btn.addDisabled.attr('disabled', 'disabled').css({backgroundColor:'lightGray', color:'darkGray'});
                    }
                    if (state.opts.edit) {
                        state.btn.edit = getButton(state.opts.edit, 'glyphicon-pencil');
                        ul.append(getLi().append(state.btn.edit));
                        state.btn.edit.on('click', function (e) {
                            state.opts.target.trigger("menu-bar-edit");
                        });
                    }

                    if (state.opts.upload) {
                        state.btn.upload = getButton(state.opts.upload, 'glyphicon-upload');
                        ul.append(getLi().append(state.btn.upload));
                        state.btn.upload.on('click', function (e) {
                            state.opts.target.trigger('menu-bar-upload');
                        });
                    }
                    if (state.opts.uploadDisabled) {
                        state.btn.uploadDisabled = getButton(state.opts.uploadDisabled, 'glyphicon-upload');
                        ul.append(getLi().append(state.btn.uploadDisabled));
                        state.btn.uploadDisabled.attr('disabled', 'disabled').css({backgroundColor:'lightGray', color:'darkGray'});
                    }
                }
            }
        };
        //public methods
        state.h = function(options){
            var btn = state.btn[options.action];
            if(btn){
                btn.fadeOut();
            }
        };
        state.s = function(options){
            var btn = state.btn[options.action];
            if(btn){
                btn.fadeIn();
            }
        };


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.menuBar.defaults = {
        separator: true,
        target: $(document)
    };


    //Plugin Function
    $.fn.menuBar = function (method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function () {
                new $.menuBar($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $menuBar = $(this).data('menuBar');
            switch (method) {
                case 'hide': $menuBar.h(options);break;
                case 'show': $menuBar.s(options);break;
                case 'state':
                default:
                    return $menuBar;
            }
        }
    };

})(jQuery);