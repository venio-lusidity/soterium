;(function ($) {
    //Object Instance
    $.pAuthorizedLink = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);
        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.pAuthorizedLink.defaults, options.schema.plugin ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        // Store a reference to the environment object
        el.data("pAuthorizedLink", state);
        // Private environment methods
        methods = {
            init: function() {
                methods.html.create();
            },
            html:{
                create: function(){
                    var a = dCrt('a');
                    if(state.opts.linkHtml){
                       a.html(state.opts.linkHtml);
                    }
                    if(state.opts.linkAttributes) {
                        $.each(state.opts.linkAttributes, function() {
                            var attribute = this;
                            a.attr(attribute.id, attribute.value);
                        });
                    }
                    a.on('click', function (e) {
                        var v = state.opts.lCache[state.opts.vertexUri];
                        if(v === "none") {
                            methods.linkClick(state.opts.vertexUri, state.opts.view);
                        }
                        if(state.opts.lCache[state.opts.vertexUri] === "denied"){
                                e.preventDefault();
                                e.stopPropagation();
                                var btnBar = $(document.createElement('div')).addClass('btn-bar');
                                var ok = $(document.createElement('button')).attr('type', 'button')
                                    .addClass('btn btn-default btn-info').html('OK');
                                btnBar.append(ok);

                                ok.on('click', function(){
                                    state.pageModal('hide');
                                });
                                if(!state.pageModal('exists')) {
                                    state.pageModal();
                                }
                                state.pageModal('show', {
                                    glyph: 'glyphicon-warning-sign yellow',
                                    header: 'Not Authorized.',
                                    body: function(body){
                                        var title = state.opts.linkHtml;
                                        if(!title){
                                            title = state.opts.vertexUri;
                                        }
                                        var msg = $(document.createElement('div')).css({verticalAlign: 'middle', height: '32px'});
                                        var question = $(document.createElement('div')).html('You are not authorized to view "<strong>' + title + '</strong>".');
                                        msg.append(question);
                                        body.append(msg);
                                    },
                                    footer: btnBar,
                                    hasClose: true
                                });
                            }

                    });
                    //state.worker.node.children().remove();
                    state.worker.node.append(a);
                }
                
            },
            sleep: function(mills){
            window.setTimeout(function(){
            }, mills);
            },
            authUrl: function (data, view) {
                return String.format('{0}/hierarchy/details?detail={1}&view={2}&exact={3}&_nocache={4}', data, 'auth', view, false, $.jCommon.getRandomId('c'));
            },
            linkClick: function(item, view){
                var u = methods.authUrl(item, view);
                var s = function (data) {
                    if (!data || !data.authorized) {
                        state.opts.lCache[state.opts.vertexUri]= "denied";
                        console.log("set denied - new value: " +state.opts.lCache[state.opts.vertexUri]);
                    }
                    else{state.opts.lCache[state.opts.vertexUri]= "allowed";
                        console.log("set allowed - new value " +state.opts.lCache[state.opts.vertexUri]);
                    }
                };
                var f= function () {

                };
                $.htmlEngine.request(u, s, f, null, "get", false);
            }
        };
        //public methods

        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.pAuthorizedLink.defaults = {
        view: null,
        linkAttributes:[],
        linkHtml:null,
        vertexUri:null,
        schema: {   
        }
    };


    //Plugin Function
    $.fn.pAuthorizedLink = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.pAuthorizedLink($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $pAuthorizedLink = $(this).data('pAuthorizedLink');
            switch (method) {
                case 'exists': return (null!==$pAuthorizedLink && undefined!==$pAuthorizedLink && $pAuthorizedLink.length>0);break;
                case 'state':
                default: return $pAuthorizedLink;
            }
        }
    };

    $.pAuthorizedLink.call= function(elem, options){
        elem.pAuthorizedLink(options);
    };

    try {
        $.htmlEngine.plugins.register("pAuthorizedLink", $.pAuthorizedLink.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
