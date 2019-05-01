;(function ($) {

    //Object Instance
    $.inputValidator = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.inputValidator.defaults, options);

        // Store a reference to the environment object
        el.data("inputValidator", state);

        // Private environment methods
        methods = {
            init: function() {
                if(state.opts.phone){
                    $.each(state.opts.phone, function(){
                        methods.bindings.phone(this);
                        this.attr('data-type', 'phone');
                    });
                }
                if(state.opts.extension){
                    $.each(state.opts.extension, function(){
                        methods.bindings.extension(this);
                        this.attr('data-type', 'extension');
                    });
                }
                if(state.opts.email){
                    $.each(state.opts.email, function(){
                        methods.bindings.email(this);
                        this.attr('data-type', 'email');
                    });
                }
                if(state.opts.text){
                    $.each(state.opts.text, function(){
                        methods.bindings.text(this);
                        this.attr('data-type', 'text');
                    });
                }
            },
            bindings: {
                text: function(input){
                    input.on('blur', function(e){
                        $(this).removeClass("hasError").removeClass("success");
                        var result = $(this).val();
                        if(result && result.length>0) {
                            $(this).addClass("success");
                        }
                        else{
                            $(this).addClass("hasError")
                        }
                    });
                    input.on('keyup', function(e){
                        $(this).removeClass("hasError").removeClass("success");
                    });
                },
                extension: function(input){
                    input.on('blur', function(e){
                        $(this).removeClass("hasError").removeClass("success");
                        var result = methods.validate.extension($(this));
                        if(!result) {
                            $(this).addClass("hasError");
                        }
                        else{
                            $(this).addClass("success")
                        }
                    });
                    input.on('keyup', function(e){
                        $(this).removeClass("hasError").removeClass("success");
                        var txt = $(this).val();
                        var result = '';
                        if(!$.jCommon.string.empty(txt)) {
                            if ($.jCommon.is.numeric(txt)) {
                                result = txt;
                            }
                            else {
                                result = txt.substr(0, txt.length - 1);
                            }
                        }
                        $(this).val(result);
                    });
                },
                phone: function(input){
                    input.on('blur', function(e){
                        $(this).removeClass("hasError").removeClass("success");
                        var result = methods.validate.phone($(this));
                        if(!result) {
                            $(this).addClass("hasError");
                        }
                        else{
                            $(this).addClass("success")
                        }
                    });
                    input.on('keyup', function(e){
                        $(this).removeClass("hasError").removeClass("success");
                        var txt = $(this).val();
                        if(!txt){
                            return false;
                        }
                        var result = '';
                        var key = $.jCommon.string.getLast(txt);
                        if($.jCommon.is.numeric(key)) {
                            var len = txt.length;
                            var stop = false;
                            if(len<=12) {
                                for (var i = 0; i < len; i++) {
                                    var c = txt.charAt(i);
                                    switch (i) {
                                        case 6:
                                        case 2:
                                            if ($.jCommon.is.numeric(c)) {
                                                result += c;
                                                var dot = txt.charAt(i + 1);
                                                if (!$.jCommon.string.equals(dot, '.')) {
                                                    result += '.';
                                                }
                                            }
                                            else {
                                                stop = true;
                                            }
                                            break;
                                        case 7:
                                        case 3:
                                            if ($.jCommon.is.numeric(c)) {
                                                result += c;
                                                i++;
                                            }
                                            else if ($.jCommon.string.equals(c, '.')) {
                                                result += c;
                                            }
                                            else {
                                                stop = true;
                                            }
                                            break;
                                        default :
                                            if ($.jCommon.is.numeric(c)) {
                                                result += c;
                                            }
                                            else {
                                                stop = true;
                                            }
                                            break;
                                    }
                                    if (stop) {
                                        break;
                                    }
                                }
                            }
                            else{
                                result = txt.substr(0, 12);
                            }
                        }
                        else if(!$.jCommon.string.empty(key)&&!$.jCommon.string.empty(txt)){
                            result = txt.substr(0, txt.length-1);
                        }
                        $(this).val(result);
                    });
                },
                email: function(input){
                    input.on('blur', function(e){
                        $(this).removeClass("hasError").removeClass("success");
                        var result = methods.validate.email($(this));
                        if(!result) {
                            $(this).addClass("hasError");
                        }
                        else{
                            $(this).addClass("success")
                        }
                    });
                    input.on('keyup', function(e){
                        $(this).removeClass("hasError").removeClass("success");
                    });
                }
            },
            validate: {
                extension: function(input){
                    var txt = input.val();
                    return $.jCommon.is.numeric(txt) || $.jCommon.string.empty(txt);
                },
                phone: function(input){
                    var result = false;
                    var txt = input.val();
                    if(!$.jCommon.is.empty(txt)){
                        var len = txt.length;
                        result = (len==12);
                        if(result){
                            for (var i = 0; i < len; i++) {
                                var c = txt.charAt(i);
                                switch (i){
                                    case 3:
                                    case 7:
                                        result = $.jCommon.string.equals(c, '.');
                                        break;
                                    default:
                                        result = $.jCommon.is.numeric(c);
                                        break
                                }
                                if(!result){
                                    break;
                                }
                            }
                        }
                    }
                    return result;
                },
                email: function(input){
                    var result = false;
                    var txt = input.val();
                    if(!$.jCommon.is.empty(txt)){
                        result = $.jCommon.is.email(txt);
                    }
                    return result;
                }
            }
        };
        //public methods

        state.validate = function(options){
            var result = false;
            if(options.input) {
                var type = options.input.attr('data-type');
                result = methods.validate[type](options.input);
            }
            return result;
        };


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.inputValidator.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.inputValidator = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.inputValidator($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $inputValidator = $(this).data('inputValidator');
            switch (method) {
                case 'validate': return $inputValidator.validate(options);
                case 'state':
                default: return $inputValidator;
            }
        }
    };

})(jQuery);
