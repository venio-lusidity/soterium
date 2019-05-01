

;(function ($) {

    //Object Instance
    $.jAlert = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.jAlert.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.outer = dCrt('div').addClass('alert no-radius').hide();
        state.inner = dCrt('div').addClass('alert-inner');
        var timer = null;

        // Store a reference to the environment object
        el.data("jAlert", state);

        // Private environment methods
        methods = {
            init: function() {
                state.append(state.outer);
                state.outer.append(state.inner);
            },
            autoHide: function (seconds) {
                timer = window.setTimeout(function () {
                    methods.hide();
                    timer = null;
                }, (seconds * 1000));
            },
            clear: function () {
                if (timer) {
                    clearTimeout(timer);
                }
                methods.hide();
                state.inner.children().remove();
                state.outer.removeClass('alert-danger alert-success alert-info alert-warning');
            },
            yellow: function (text) {
                state.outer.say('alert-warning', text);
            },
            green: function (text) {
                state.outer.say('alert-success', text);
            },
            blue: function (text) {
                state.outer.say('alert-info', text);
            },
            red: function (text) {
                state.outer.say('alert-danger', text);
            },
            say: function (cls, text) {
                methods.clear();
                state.outer.addClass(cls);
                state.inner.append(text);
            },
            show: function (timeout) {
                if(timeout) {
                    state.outer.slideDown(300, function () {
                        if (timeout && $.jCommon.is.numeric(timeout)) {
                            methods.autoHide(timeout);
                        }
                    });
                }
                else{
                    state.outer.show();
                }
            },
            hide: function () {
                state.outer.slideUp(300, function () {
                    state.inner.html('');
                });
            }
        };
        state.say = function (options) {
            methods.say(options.cls, options.msg);
        };

        state.show = function (options) {
            methods.show(options);
        };

        state.hide = function () {
            methods.show();
        };

        // Initialize
        methods.init();
    };
    $.jAlert.show = function (node, timeout) {
        node.jAlert('show', timeout);
    };
    $.jAlert.hide = function (node) {
        node.jAlert('hide');
    };
    $.jAlert.yellow = function (node, text) {
        node.jAlert('say', {cls: 'alert-warning', msg: text});
    };
    $.jAlert.green = function (node, text) {
        node.jAlert('say', {cls: 'alert-success', msg: text});
    };
    $.jAlert.blue = function (node, text) {
        node.jAlert('say', {cls: 'alert-info', msg: text});
    };
    $.jAlert.red = function (text) {
        node.jAlert('say', {cls: 'alert-danger', msg: text});
    };

    //Default Settings
    $.jAlert.defaults = {};


    //Plugin Function
    $.fn.jAlert = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.jAlert($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $jAlert = $(this).data('jAlert');
            switch (method) {
                case 'exists': return (null!==$jAlert && undefined!==$jAlert && $jAlert.length>0);
                case 'show':
                    $jAlert.show(options);
                    break;
                case 'hide':
                    $jAlert.hide(options);
                    break;
                case 'say':
                    $jAlert.say(options);
                    break;
                case 'state':
                default: return $jAlert;
            }
        }
    };

})(jQuery);
