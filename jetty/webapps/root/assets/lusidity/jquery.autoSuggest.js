;(function ($) {
    $.autoSuggest = function(el, options) {
        if(!$(el).is('div')){
            throw new Error('The auto suggest element must be a div.');
        }
        var state = el,
            methods = {};
        state.opts = $.extend(true, $.autoSuggest.defaults, options);
        state.request = null;
        state.form = $(document.createElement('form'));
        state.input = $(document.createElement('input'));
        state.clear = $(document.createElement('div'));
        state.speech = $(document.createElement('div'));
        state.inner = $(document.createElement('div'));

        if(state.opts.enableLogging){
            console.log('Set the "enableLogging" option to false in order to turn logging off.');
        }
        // Store a reference to the environment object
        el.data("autoSuggest", state);

        // Private environment methods
        methods = {
            init: function() {
                state.form.addClass('elusidate');
                state.append(state.form);
                state.inner.addClass('input-group no-radius');
                if(state.opts.style) {
                    state.inner.css(state.opts.style);
                }
                //.css({display: 'inline-block', margin: '0 auto 0 auto',
               //     'white-space': 'nowrap', position: 'relative'});
                state.form.append(state.inner);

                state.input.attr('type','text').addClass('form-control no-radius');
                state.inner.append(state.input);

                state.icon = $(document.createElement('span'));
                state.icon.addClass("glyphicon glyphicon-remove").attr("aria-hidden", "true");
                state.busy = $.htmlEngine.getSpinner().hide();
                state.clear.addClass('search-clear').css("z-index", "501").hide().append(state.icon);
                state.clear.append(state.busy);
                state.inner.append(state.clear);

                if(state.opts.listener.enabled) {
                    var iconSpeech = $(document.createElement('i'));
                    iconSpeech.addClass("fa fa-microphone");
                    state.speech.addClass('search-mic').css("z-index", "500").append(iconSpeech);
                    state.inner.append(state.speech);
                }

                if(typeof state.opts.input.placeHolder === 'string'){
                    state.input.attr('placeholder', state.opts.input.placeHolder);
                }
                else if(state.opts.enableLogging && $.isFunction(state.opts.input.placeHolder)){
                    state.opts.input.placeHolder();
                }
                
                if(typeof state.opts.input.cls === 'string'){
                    state.input.addClass(state.opts.input.cls);
                }
                else if(state.opts.enableLogging && $.isFunction(state.opts.input.cls)){
                    state.opts.input.cls();
                }

                if(typeof state.opts.input.attributes === 'object'){
                    $.each(state.opts.input.attributes, function(key, value){
                        state.input.attr(key, value);
                    });
                }
                else if(state.opts.enableLogging && $.isFunction(state.opts.input.attr)) {
                    state.opts.input.attr();
                }

                if(typeof state.opts.input.style === 'object'){
                    state.input.css(state.opts.input.style);
                }
                else if(state.opts.enableLogging && $.isFunction(state.opts.input.style)) {
                    state.opts.input.style();
                }

                methods.register.input();
                state.input.addClass('no-radius');
                if(state.opts.button.show){
                    /* Button */
                    state.go = $(document.createElement('span'));
                    state.go.addClass('input-group-addon no-radius').css({cursor: 'pointer'});

                    if(typeof state.opts.button.cls === 'string'){
                        state.go.addClass(state.opts.button.cls);
                    }
                    else if(state.opts.enableLogging && $.isFunction(state.opts.button.cls)){
                        state.opts.button.cls();
                    }

                    if(typeof state.opts.button.attributes === 'object'){
                        $.each(state.opts.button.attributes, function(key, value){
                            state.go.attr(key, value);
                        });
                    }
                    else if(state.opts.enableLogging && $.isFunction(state.opts.button.attr)) {
                        state.opts.button.attr();
                    }

                    if(typeof state.opts.button.style === 'object'){
                        state.go.css(state.opts.button.style);
                    }
                    else if(state.opts.enableLogging && $.isFunction(state.opts.button.style)) {
                        state.opts.button.style();
                    }

                    if(typeof state.opts.button.html === 'string'){
                        state.go.html(state.opts.button.html);
                    }
                    else if(state.opts.enableLogging && $.isFunction(state.opts.button.html)){
                        state.opts.button.html();
                    }

                    state.inner.append(state.go);

                    state.go.on('click', function(){
                        state.submit();
                    })
                }

                /* suggestions */
                var outer = $(document.createElement('div')).css({position: 'absolute', top: '32px'});
                state.suggested = $(document.createElement('ul'));
                state.suggested.addClass('dropdown-menu autosuggest')
                    .attr('role', 'menu')
                    .attr('aria-labelledby', 'dropdownMenu')
                    .css({display: 'block', width: (state.input.width() + 12) + 'px',
                        'text-align': 'left', position: 'relative', top: '-2px' })
                    .attr('mouse-over', 'true');
                outer.append(state.suggested);
                state.form.append(outer);
                state.suggested.hide();

                state.suggested.on('mouseenter', function(e){
                    state.suggested.attr('data-mouseover', 'true');
                });
                state.suggested.on('mouseleave', function(e){
                    state.suggested.attr('data-mouseover', 'false');
                });

                $(window).resize(function(){
                    methods.setInputWidth();
                });
            },
            ajax:{
                autocomplete: function (options) {
                    if(null!==state.request){
                        state.busy.hide();
                        state.request.abort();
                        state.request=null;
                    }
                    //noinspection JSUnusedLocalSymbols
                    var action = {
                        connector: null,
                        async: true,
                        data: null,
                        text: options.text,
                        methodType: 'get',
                        showProgress: false,
                        onbeforesend: {
                            message: { msg: null, debug: false },
                            execute: function () {
                                state.busy.show();
                            }
                        },
                        oncompleted: {
                            execute: function (jqXHR, textStatus) {
                                state.busy.hide();
                            }
                        },
                        onsuccess: {
                            message: { msg: null, debug: false },
                            execute: function (data) {
                                state.busy.hide();
                                state.update({data: data, text: action.text.replaceAll('#', '')});
                            }
                        },
                        onfailure: {
                            message: { msg: null, debug: false },
                            execute: function (jqXHR, textStatus, errorThrown) {
                                state.busy.hide();
                                var data = {};
                                data[state.opts.keys.items] = [{text: 'No results.'}];
                                state.update({data: data, text: action.text});
                            }
                        },
                        url: options.url
                    };
                    state.request = lusidity.environment('request', action);
                }
            },
            register:{
                speech: function(){
                    if(state.opts.listener && state.opts.listener.enabled) {
                        try {
                            if (lusidity.environment('isListening')) {
                                state.opts.listener.recognition.stop();
                            }
                        }
                        catch (e) {
                        }

                        state.opts.listener.elem.show();

                        var recognition = new webkitSpeechRecognition();
                        recognition.onresult = function (event) {
                            state.opts.listener.elem.hide();
                            recognition.stop();

                            var text = null;
                            try {
                                text = event.results[0][0].transcript;
                            }
                            catch (e) {
                            }
                            if (null !== text) {
                                state.input.val(text);
                                state.go.click();
                            }
                            try {
                                if (lusidity.environment('isListening')) {
                                    state.opts.listener.recognition.start();
                                }
                            }
                            catch (e) {
                            }
                        };

                        var isStopped = false;

                        function stop() {
                            if (!isStopped) {
                                if (recognition) {
                                    recognition.stop();
                                }
                                state.opts.listener.elem.hide();
                                try {
                                    if (lusidity.environment('isListening')) {
                                        state.opts.listener.recognition.start();
                                    }
                                }
                                catch (e) {
                                }
                                isStopped = true;
                            }
                        }

                        recognition.start();

                        if (state.opts.listener.stop && !state.opts.listener.stopBinded) {
                            state.opts.listener.binded = true;
                            state.opts.listener.stop.on('click', function () {
                                stop();
                            })
                        }

                        window.setTimeout(function () {
                            stop();
                        }, 10000);
                    }
                },
                input: function(){
                    $(window).resize(function(){
                       methods.setInputWidth();
                    });
                    state.input.attr('onwebkitspeechchange', 'onSpeechChanged(this);');
                    state.input.change(function(){
                        var text = state.input.val();
                        if(text) {
                            state.clear.show();
                        }
                    });
                    state.input.on('keyup', function (e) {
                        e.preventDefault();
                        var subItem = -1;
                        var total = 0;
                        if(state.suggested){
                            subItem = parseInt(state.suggested.attr('data-selected'));
                            total = parseInt(state.suggested.attr('data-total'));
                        }
                        var code = parseInt(e.keyCode);
                        if (code === 27/*esc*/) {
                            $(this).val('');
                            state.suggested.attr('data-mouseover', 'false');
                            $(this).blur();
                            state.clear.hide();
                            methods.show(false, true);
                        }
                        else if(code === 9 /*tab*/){
                            state.suggested.attr('data-mouseover', 'false');
                            methods.show(false);
                        }
                        else if(code === 38 /*up arrow*/ && state.suggested){
                            subItem--;
                            if((subItem) === -1){
                                subItem = total-1;
                            }
                            var on = 0;
                            state.suggested.children().each(function(){
                                $(this).removeClass('active');
                                if(on===subItem){
                                    $(this).addClass('active');
                                    state.suggested.attr('data-selected', subItem);
                                    state.input.val($(this).attr('data-text'));
                                }
                                on++;
                            });
                        }
                        else if(code === 40 /*down arrow*/ && state.suggested){
                            subItem++;
                            if(subItem === total){
                                subItem = 0;
                            }
                            var on = 0;
                            state.suggested.children().each(function(){
                                $(this).removeClass('active');
                                if(on===subItem){
                                    $(this).addClass('active');
                                    state.suggested.attr('data-selected', subItem);
                                    state.input.val($(this).attr('data-text'));
                                }
                                on++;
                            });
                        }
                        else if(code===13){
                            state.input.blur();
                            state.suggested.hide();
                        }
                        else if(code !== 13){
                            var text = state.input.val();
                            state.clear.show();
                            if(text && text.length >0 && $.isFunction(state.opts.onKeyUp)){
                                text = text.replaceAll('#', '');
                                state.opts.onKeyUp(text, state);
                            }
                            else{
                                state.request.abort();
                                state.request = null;
                                state.clear.hide();
                                methods.show(false, true);
                            }
                        }
                    });
                    state.input.on('blur', function(e){
                        if(state.suggested.attr('data-mouseover') === 'false'){
                            if(null !== state.request){
                                state.request.abort();
                                state.request = null;
                            }
                            methods.show(false);
                        }
                    });
                    state.input.on('focus', function(){
                        methods.setInputWidth();
                        methods.show(true);
                    });

                    state.clear.on('click', function(){
                        methods.reset();
                    });

                    state.speech.on('click', function(){
                        methods.register.speech();
                    });

                    state.on('submit', function(e){
                        e.preventDefault();
                        var text = state.input.val();
                        if (null !== text && state.input.placeHolder !== text && text.length > 0
                            && $.isFunction(state.opts.onEnter)) {
                            text = text.replaceAll('#', '');
                            state.opts.onEnter(text);
                        }
                    });
                }
            },
            reset: function(){
                state.input.val('');
                state.input.blur();
                state.input.focus();
                state.clear.hide();
                methods.show(false, true);
            },
            setInputWidth: function(){
                var w = state.input.innerWidth();
                if(state.go){
                    if(w<=state.opts.minWidth){
                        state.go.hide();
                    }
                    else{
                        state.go.show();
                    }
                }
                state.suggested.css({width: w + 'px'});
                state.clear.css({left: (state.input.innerWidth()-32) + 'px'});
                state.speech.css({left: (state.input.innerWidth()-32) + 'px'});
                state.position();
            },
            show: function(show, clear){
                if(state.suggested){
                    if(show){
                        if(!state.suggested.is(':visible')
                            && state.suggested.children().length > 0){
                            state.suggested.slideDown(0);
                        }124
                    }
                    else{
                        if(state.suggested.is(':visible')){
                            state.suggested.slideUp(0);
                        }
                    }

                    if(clear && clear === true){
                         state.suggested.children().remove();
                    }
                }
            }
        };
        //public methods
        state.autocomplete = function(options){
            methods.ajax.autocomplete(options);
        };
        state.position = function(offset){
            var pos = state.inner.position();
            if(offset && offset.top && offset.left &&
                typeof offset.top === 'number' && typeof offset.left === 'number'){
                state.suggested.css({top: (pos.top + offset.top) + 'px', left: (pos.left + offset.left) + 'px'});
            }
            else{
                state.suggested.css({top: '-2px', left: (pos.left + 'px')});
            }
        };

        state.update = function(obj){
            if(obj.data){
                if($(obj.data).length===0){
                    methods.show(false, true);
                    return false;
                }
                state.suggested.children().remove();
                var on=0;
                var terms = [];
                state.map = new ObjectMap('string', 'object', false, false);
                $.each(obj.data[state.opts.keys.items], function(){
                    if(on<state.opts.limit) {
                        var key = $.jCommon.json.getProperty(this, state.opts.keys.text);
                        state.map.put($.jCommon.string.toTitleCase(key), this);
                    }
                    on++;
                });
                on=0;
                var matches = [];
                state.map.sort(true);
                state.map.each(function(entry, index){
                    var text = entry.key;
                    var uri;
                    var data = entry.value ? ($.jCommon.is.array(entry.value) ? entry.value[0]: entry.value):null;
                    if(data && data.uri){
                        uri = data.uri;
                    }
                    if($.jCommon.array.contains(terms, text)){
                        matches.push(text);
                        return true;
                    }
                    terms.push(text);
                    var primaryType = $.jCommon.json.getProperty(this, state.opts.keys.primaryType);
                    var linkUri = $.jCommon.json.getProperty(this, state.opts.keys.uri);

                    if (!$.jCommon.string.empty(text)) {
                        var li = $(document.createElement('li'));
                        li.attr('data-text', text);
                        li.attr('data-index', index);
                        var propertyNode = $(document.createElement('div'))
                            .addClass('a-item favIcon').css({'margin-right': '5px', height: '20px'})
                            .attr('tabindex', '-1')
                            .css({whiteSpace: 'nowrap', overflow: 'hidden', cursor: 'pointer', position: 'relative'})
                            .html(text);
                        li.append(propertyNode);

                        var quickView;
                        function showQuickView(){
                            if( !lusidity.environment('isMobile')) {
                                if (!quickView) {
                                    quickView = $(document.createElement('div')).addClass('quickView');
                                    li.append(quickView);
                                    state.schemaEngine({"vertexType": "slider"});
                                    state.opts.schema = state.schemaEngine('state').opts.schema;
                                    if ($.jCommon.json.hasProperty(state, 'opts.schema.zones')) {
                                        $.each(state.opts.schema.zones, function () {
                                            var zoneSchema = this;
                                            var zone = $(document.createElement('div')).addClass(zoneSchema.zone);
                                            zone.css({position: 'relative'});
                                            quickView.append(zone);
                                            zone.htmlEngine({ schema: zoneSchema, data: entry.value, autoHide: false, isResults: false });
                                        });
                                    }
                                }
                                else{
                                    quickView.show();
                                }
                            }
                        }

                        state.suggested.append(li);
                        li.on('mouseenter', {on: on}, function (e) {
                            state.suggested.children().each(function () {
                                $(this).removeClass('active');
                            });
                            $(this).addClass('active');
                            state.suggested.attr('data-selected', e.data.on);
                            state.input.focus();
                          //  showQuickView();
                        });
                        li.on('click', {on: data}, function (e) {
                            var a = data.title;
                            var matched = $.jCommon.array.contains(matches, text);
                            if($.jCommon.string.contains(a, ':[')){
                                if(matched){
                                    a = state.input.val();
                                }
                                else {
                                    a = data.uri;
                                }
                            }
                            // could use the uri but if there are multiple values for the same text on the one
                            // is in the list.
                            if(!matched && state.opts.enableMouseClickNav && $.jCommon.string.startsWith(a, "/domains")){
                                window.location = a;
                            }
                            else {
                                state.opts.onEnter(a);
                            }
                        });
                        on++;
                        state.suggested.attr('data-selected', '-1');
                        state.suggested.attr('data-total', this.length);
                    }

                });
                methods.show(true);
            }
            else if(state.opts.enableLogging){
                console.log('{ data: [results], text: [some value] } expected.');
            }
        };
        state.focus = function(){
            state.input.blur();
            window.setTimeout(function(){state.input.click()}, 100);
        };

        state.setInputWidth = function(){
            methods.setInputWidth();
        };

        state.updateInput = function(obj){
            if(obj.text && obj.text.length>0){
                state.input.val(obj.text);
                state.clear.show();
            }
            else if(state.opts.enableLogging){
                console.log('{ text: [some value] } expected.');
            }
        };

        state.showRemove = function(){
            state.clear.show();
        };

        state.reset = function(){
            methods.reset();
        };

        state.listen = function(){
            methods.register.speech();
        };

        //environment: Initialize
        methods.init();
    };



    //Default Settings
    $.autoSuggest.defaults = {
        limit: 20,
        minWidth: 242,
        enableLogging: true,
        enableMouseClickNav: true,
        onEnter: function(text){
            console.log('Set the "onEnter" option in order to execute a function when the enter key is pressed.');
        },
        onKeyUp: function(text, container){
            console.log('Set the "onKeyUp" option in order to execute a function when a key is pressed.');
        },
        input:{
           placeHolder: function(){
                console.log('Set the "input.placeHolder" option in order to set the prompt text of the input element.');
           },
           cls: function (){
                console.log('Set the "input.cls" option in order to set custom classes.');
           },
           attributes: function(){
                console.log('Set the "input.attributes" option in order to set custom attributes.');

           },
           style: function(){
                console.log('Set the "input.style" option in order to set custom styles.');
           }
        },
        button:{
            show: true,
            cls: function (){
                console.log('Set the "button.cls" option in order to set custom classes.');
            },
            attributes: function(){
                console.log('Set the "button.attributes" option in order to set custom attributes.');

            },
            style: function(){
                console.log('Set the "button.style" option in order to set custom styles.');
            },
            html: function(){
               console.log('Set the "button.html" option in order to set the inner HTML of the button.' +
                   '(button.show (default) must be set to true.');
            },
            onClick: function(){
                console.log('Set the "button.onClick" option in order to peform an action when the button is clicked.  ' +
                    '(This can be the same function for the "onEnter" option)');
            }
        }
    };

    //Plugin Function
    $.fn.autoSuggest = function(method, options) {
        if (method === undefined) method = {};
        if (typeof method === "object") {
            return this.each(function() {
                new $.autoSuggest($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $autoSuggest = $(this).data('autoSuggest');
            switch (method) {
                case 'autocomplete': $autoSuggest.autocomplete(options);break;
                case 'position': $autoSuggest.position(options); break;
                case 'update': $autoSuggest.update(options);break;
                case 'updateInput': $autoSuggest.updateInput(options);break;
                case 'setInputWidth': $autoSuggest.setInputWidth();break;
                case 'showRemove': $autoSuggest.showRemove();break;
                case 'reset': $autoSuggest.reset();break;
                case 'focus': $autoSuggest.focus();break;
                case 'listen': $autoSuggest.listen();break;
                case 'state':
                    return $autoSuggest;break;
                default:
                    return false;break;
            }
        }
    }

})(jQuery);
