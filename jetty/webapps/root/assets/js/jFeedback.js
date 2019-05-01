

;(function ($) {

    //Object Instance
    $.feedback = function(el, options) {
        var state = $(el),
            methods = {};
        state.container = $(el);

        state.opts = $.extend({}, $.feedback.defaults, options);
        state.isMobile = $.jCommon.string.contains(navigator.userAgent.toLowerCase(), ['android','iphone', 'ipad'], true);

        // Store a reference to the environment object
        $.data(el, "feedback", state);

        // Private environment methods
        methods = {
            init: function() {
                state.pageModal();
                state.on('click', function () {
                    state.pageModal('show', {
                        header: methods.header(),
                        footer: methods.footer,
                        body: methods.body(),
                        hasClose: true});
                });
            },
            ajax:{
                send: function (data) {
                    var url = '/feedback';
                    //noinspection JSUnusedLocalSymbols
                    var action = {
                        connector: null,
                        async: true,
                        data: JSON.stringify(data),
                        methodType: 'post',
                        onsuccess: {
                            message: { msg: null, debug: false },
                            execute: function (data) {
                                //lusidity.info.blue('message sent:' + data.sent);
                               // lusidity.info.show();
                            }
                        },
                        onfailure: {
                            message: { msg: null, debug: false },
                            execute: function (jqXHR, textStatus, errorThrown) {
                               // console.log('message failed.');
                            }
                        },
                        url: url
                    };
                    lusidity.environment('request', action);
                }
            },
            collectData: function(text){
                var data = {};
                data.subject = state.opts.subject;
                data.description = text;
                if(state.opts.satisfied.find("input").is(":checked")){
                    data.feedback = "satisfied";
                }
                else if(state.opts.bug.find("input").is(":checked")){
                    data.feedback = "bug";
                }
                else if(state.opts.suggest.find("input").is(":checked")){
                    data.feedback = "suggestion";
                }
                if(state.opts.includeWebLink){
                    data.url = window.location.href;
                    if(state.opts.data) {
                        if (state.opts.data.queryPhrase) {
                            data.queryPhrase = state.opts.data.queryPhrase;
                        }
                        else if (state.opts.data.title) {
                            data.title = state.opts.data.title;
                        }
                    }
                }
                if(state.opts.includeBrowserInfo){
                    if(navigator){
                        data.browser = {};
                        $.each(navigator, function(key, value){
                            if($.jCommon.is.string(value)){
                                data.browser[key] = value;
                            }
                        });
                    }
                }

                return data;
            },
            header: function () {
                var header = $(document.createElement('div'));
                var hContent = $(document.createElement('h4'));
                var hIcon = $(document.createElement('img')).attr('src', '/assets/img/comment.png')
                    .css({width: '48px', marginRight: '5px' });
                hContent.append(hIcon).append(state.opts.name + ' feedback');
                header.append(hContent);
                return header;
            },
            body: function(){
                var container = $(document.createElement('div'));
                var feedback = $(document.createElement('div')).css({marginBottom: '5px'});
                feedback.html("Would you like to send feedback about " + state.opts.name + "?");
                container.append(feedback);

                var lbl;
                if(state.opts.data && state.opts.data.queryPhrase){
                    lbl = 'Include my search for \"' + state.opts.data.queryPhrase + '\"';
                }
                else if(state.opts.data && state.opts.data.title){
                    lbl = 'Include a link to the lusidity page for \"' + state.opts.name + '\"';
                }
                else{
                    lbl = 'Include a link to the current \"' + state.opts.name + '\" page.';
                }

                container.append(methods.html.checkbox(true, lbl, function(elem){
                    state.opts.includeWebLink = elem.is(':checked');
                }).css({marginLeft: "20px"}));
                var collect = methods.html.checkbox(true, 'Can we collect information about your browser?' +
                    '<br/>This will only include browser name and version information.', function(elem){
                    state.opts.includeBrowserInfo = elem.is(':checked');
                });
                container.append(collect.hide());

                state.opts.satisfied = methods.html.radio(true, 'Inquiry', 'Were you satisfied with your experience?', 1).css({marginLeft: "20px"});
                state.opts.bug  = methods.html.radio(false, 'Bug', 'Did you encounter a problem?', 2).css({marginLeft: "20px"});
                state.opts.suggest = methods.html.radio(false, 'Feature', 'Do you have a suggestion for improving \"' + state.opts.name + '\"?', 3).css({marginLeft: "20px"});
                container.append(state.opts.satisfied);
                container.append(state.opts.bug);
                container.append(state.opts.suggest);

                state.opts.includeBrowserInfo = true;
                state.opts.includeWebLink = true;
                state.opts.subject = 'Inquiry';

                var label = $(document.createElement('div')).html('Please tell us your thoughts or experience. (Required)');
                container.append(label);

                state.opts.textarea = $(document.createElement('textarea'))
                    .css({width: '97%', margin: '5px auto 5px auto', height: '84px'});
                container.append(state.opts.textarea);


                state.opts.textarea.bind('keyup', function(){
                    var text = state.opts.textarea.val();
                    if(text && text.length>0){
                        state.opts.fSend.removeClass('disabled');
                    }
                    else{
                        state.opts.fSend.addClass('disabled');
                    }
                });

                return container;
            },
            footer: function () {
                var mFooter = $(document.createElement('div'));
                var fClose = $(document.createElement('button')).addClass('btn').html('Close');
                state.opts.fSend = $(document.createElement('button')).addClass('btn disabled').html('Send');
                mFooter.append(fClose).append(state.opts.fSend);

                fClose.on('click', function () {
                    state.pageModal('hide');
                });

                state.opts.fSend.on('click', function(){
                    var text = state.opts.textarea.val();
                    if(text && text.length>0){
                        methods.ajax.send(methods.collectData(text));
                    }
                    state.pageModal('hide');
                });

                return mFooter;
            },
            html:{
                checkbox: function(selected, text, onChanged){
                    var label = $(document.createElement('label')).addClass('checkbox');
                    var input = $(document.createElement('input')).attr('type', 'checkbox');
                    if(selected){
                        input.attr('checked', '');
                    }
                    label.append(input);
                    label.append(text);
                    input.bind('change', function(){
                        onChanged(input);
                    });
                    return label;
                },
                radio: function(selected, value, text, idx){
                    var label = $(document.createElement('label')).addClass('radio');
                    var input = $(document.createElement('input'))
                        .attr('type', 'radio')
                        .attr('id', 'optionsRadios' + idx)
                        .attr('name', 'optionsRadios')
                        .attr('value', value);
                    if(selected){
                        input.attr('checked', '');
                    }
                    label.append(input);
                    label.append(text);
                    input.bind('change', function(){
                        if(input.is(':checked')){
                            state.opts.subject = value;
                        }
                    });
                    return label;
                }
            }
        };
        //public methods
        state.update = function(options){
            state.opts = $.extend({}, state.opts, options);
        };

        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.feedback.defaults = {
        name: "Soterium"
    };

    //Plugin Function
    $.fn.feedback = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.feedback(this, method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $lusidityFeedback = $(this).data('feedback');
            switch (method) {
                case 'update':
                    $lusidityFeedback.update();
                    break;
                case 'state':
                default: return $lusidityFeedback;
            }
        }
    }

})(jQuery);
