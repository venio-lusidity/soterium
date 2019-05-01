;(function ($) {

    //Object Instance
    $.pageModal = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.opts = $.extend({}, $.pageModal.defaults, options);
        state.modal = $(document.createElement('div')).addClass('modal').attr('role', 'dialog').css({overflow: 'hidden'});
        if(state.opts.fade){
            state.modal.addClass('fade');
        }
        state.dialog = $(document.createElement('div')).addClass('modal-dialog');
        state.content = $(document.createElement('div')).addClass('modal-content');
        state.header = null;
        state.body = null;
        state.footer = null;
        state.close = null;

        // Store a reference to the environment object
        el.data("pageModal", state);

        // Private environment methods
        methods = {
            init: function() {
                if(state.opts.top){
                    state.dialog.css({top: state.opts.top+'px'});
                }
                if(state.opts.css){
                    state.dialog.css(state.opts.css);
                }
                state.modal.append(state.dialog);
                state.dialog.append(state.content);
                state.modal.on('hidden.bs.modal', function(){
                    if(state.opts.onHide && $.isFunction(state.opts.onHide)){
                        state.opts.onHide();
                    }
                    else{
                        state.h();
                    }
                });
            },
            clear: function(){
                state.content.empty();
                state.header = null;
                state.body = null;
                state.footer = null;
                state.close = null;
            },
            set:{
                close: function(){
                    state.close = $(document.createElement('button'))
                        .attr('type', 'button').attr('class', 'close').attr('data-dismiss', 'modal')
                        .css({'position': 'absolute', top: '16px', 'right': '8px'})
                        .html('<span aria-hidden="true">&times;</span><span class="sr-only">Close</span>');

                    if(null!==state.header && undefined !== state.header) {
                        state.header.append(state.close);
                    }
                    else if(null!==state.body && undefined !== state.body) {
                        state.body.append(state.close);
                    }

                    if(!state.opts.hasClose){
                        state.close.hide();
                    }
                },
                header: function() {
                    if (!state.header) {
                        state.header = $(document.createElement('div')).addClass('modal-header').css({position: 'relative', padding: '10px 10px'});
                    }
                    state.header.children().remove();
                    var cls = "glyphicon ";
                    var glyph = state.opts.glyph;

                    var isBS = !$.jCommon.string.startsWith(state.opts.glyph, "glyphicons ");
                    if (!isBS) {
                        cls = "";
                    }
                    state.icon = $(document.createElement('span')).addClass(cls + state.opts.glyph).css({
                        marginRight: '5px',
                        fontSize: '24px'
                    });
                    state.link = $(document.createElement(state.opts.url ? 'a' : 'span')).addClass('panel-heading-title').css({
                        position: 'relative',
                        top: isBS ? '-4px' : '8px'
                    });

                    var title = state.opts.header ? state.opts.header : state.opts.title;
                    if(state.opts.header.title){
                        title = state.opts.header.title;
                        if(state.opts.header.cls){
                            state.link.addClass(cls)
                        }
                    }
                    state.link.html(title);


                    if (state.opts.url) {
                        state.link.attr('href', state.opts.url).attr('target', '_blank')
                    }
                    state.header.append(state.icon).append(state.link);

                    state.content.append(state.header);
                },
                body: function(){
                    if(null!==state.opts.body && undefined !== state.opts.body){
                        state.body = $(document.createElement('div')).css({position: 'relative'}).addClass('modal-body');
                        state.content.append(state.body);
                        if($.isFunction(state.opts.body)){
                            state.opts.body(state.body);
                        }
                        else if(state.opts.body.object){
                            if (state.opts.body.cls) {
                                state.body.addClass(state.opts.body.cls);
                            }
                            state.body.append(state.opts.body.object);
                        }
                        else {
                            state.body.append(state.opts.body);
                        }
                    }
                },
                footer: function(){
                   if(null!==state.opts.footer && undefined !== state.opts.footer){
                       state.footer = $(document.createElement('div')).css({position: 'relative'}).addClass('modal-footer');

                       if(state.opts.footer.object)
                       {
                           if (state.opts.footer.cls) {
                               state.footer.addClass(state.opts.footer.cls);
                           }
                           state.footer.append(state.opts.footer.object);
                       }
                       else {
                           state.footer.append(state.opts.footer);
                       }
                       state.content.append(state.footer);
                   }
                }
            },
            show: function(opts){
                if(opts) {
                    methods.clear();
                    state.opts = $.extend({}, state.opts, opts);

                    methods.set.header();
                    methods.set.body();
                    methods.set.footer();
                    methods.set.close();
                    if (opts.height) {
                        //state.dialog.height(opts.height);
                        var h = $.jCommon.is.numeric(opts.width) ? opts.height + 'px' : opts.height;
                        state.dialog.css({minHeight:h, height: h, maxHeight: h});
                    }
                    else{
                        var h = '';
                        state.dialog.css({minHeight:h, height: h, maxHeight: h});
                    }
                    if (opts.width) {
                        //state.dialog.width(opts.width);
                        var w = $.jCommon.is.numeric(opts.width) ? opts.width + 'px' : opts.width;
                        state.dialog.css({minWidth:w, width: w, maxWidth: w});
                    }
                    else {
                        var w = '';
                        state.dialog.css({minWidth:w, width: w, maxWidth: w});
                    }
                    if(state.opts.css){
                        state.dialog.css(state.opts.css);
                    }
                }

                state.modal.modal('show');
            }
        };

        state.show = function(options){
            methods.show(options);
        };

        state.h = function(){
            state.close.click();
            if($.isFunction(state.opts.onClose)){
                state.opts.onClose();
            }
        };

        state.clear = function(){
            state.close.click();
            methods.clear();
        };

        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.pageModal.defaults = {
        fade: true,
        header: null,
        body: null,
        footer: null,
        hasClose: true
    };


    //Plugin Function
    $.fn.pageModal = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.pageModal($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $pageModal = $(this).data('pageModal');
            switch (method) {
                case 'show': $pageModal.show(options);break;
                case 'hide': $pageModal.h();break;
                case 'clear': $pageModal.clear();break;
                case 'exists': return ((undefined!==$pageModal)&&(null!==$pageModal));break;
                case 'state':
                default: return $pageModal;
            }
        }
    }

})(jQuery);
