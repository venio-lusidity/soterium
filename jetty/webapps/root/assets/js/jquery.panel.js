;(function ($) {

    //Object Instance
    $.panel = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.panel.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("panel", state);

        // Private environment methods
        methods = {
            init: function() {
                methods.make();
                if(state.opts.actions && state.header){
                    state.header.jActions({actions: state.opts.actions});
                }
                if(state.opts.menu && state.header){
                    methods.menu();
                }
                if(state.opts.fill){
                    methods.resize();
                }
                if(state.opts.noPanel){
                    state.header.remove();
                }
            },
            header: function() {
                if (!state.header) {
                    state.header = $(document.createElement('div')).addClass('panel-heading').css({position: 'relative'});
                }
                state.header.children().remove();
                if(state.opts.smallHeader){
                    state.header.addClass('panel-hdr-small');
                }
                var cls = "glyphicon ";
                var isG = $.jCommon.string.startsWith(state.opts.glyph, "glyphicon");
                state.icon = $.htmlEngine.glyph(state.opts.glyph).css({
                    marginRight: '5px'
                });
                state.link = $(document.createElement(state.opts.url ? 'a' : 'span')).append(state.opts.title)
                    .addClass('pnl-title')
                    .css({
                    position: 'relative',
                    top: isG ? '' : '2px'
                });
                if (state.opts.url) {
                    state.link.attr('href', state.opts.url).attr('target', '_blank')
                }
                if(state.opts.tip){
                    state.link.attr('title', state.opts.tip);
                }
                state.header.append(state.icon).append(state.link);
            },
            make: function(){
                state.panel = $(document.createElement('div')).addClass(state.opts.noHead ? '': 'panel panel-default').css({marginBottom: '0'});
                state.append(state.panel);
                if(!state.opts.noHead) {
                    methods.header();
                }
                state.body = $(document.createElement('div')).addClass('panel-body-content' + (state.opts.noHead ? '': ' panel-body'));
                if(state.opts.content) {
                    state.body.append(state.opts.content);
                }
                if(state.opts.body && state.opts.body.attr){
                    $.htmlEngine.addAttributes(state.opts.body, state.body);
                }
                if(state.opts.body && state.opts.body.css){
                    $.htmlEngine.addStyling(state.opts.body, state.body);
                }
                if(state.opts.body && state.opts.body.cls){
                    state.body.addClass(state.opts.body.cls);
                }
                state.panel.append(state.header).append(state.body);
                if(!state.opts.borders && !state.opts.noHead){
                    state.panel.addClass('no-border');
                    state.body.addClass('no-border');
                }
            },
            menu: function(){
                if(state.opts.menu) {
                    state.header.css('position', 'relative');
                    var menu = $(document.createElement('div')).css({
                        position: 'absolute',
                        right: '5px',
                        top: '0',
                        color: '#c3c3c3'
                    });
                    state.header.append(menu);
                    if (state.opts.menu.target) {
                        menu.menuBar(state.opts.menu);
                    }
                    else {
                        menu.append(state.opts.menu);
                    }
                }
            },
            resize: function(){
                var h = state.innerHeight();
                state.panel.css({minHeight: h+ 'px', height: h+ 'px'});
                if(state.header){
                    var hOffset = state.header.outerHeight();
                    h -= hOffset;
                }
                state.body.css({minHeight: h+ 'px', height: h+ 'px', maxHeight: h+'px'});
            }
        };
        //public methods
        state.resize = function () {
            methods.resize();
        };
        state.update = function (content){
            state.body.children().remove();
            state.body.append(content)
        };
        state.updateHeader = function (options) {
            if(!$.jCommon.string.equals(state.opts.glyph, options.glyph) &&
                !$.jCommon.string.empty(options.glyph)) {
                state.icon.removeClass(state.opts.glyph).addClass(options.glyph);
            }
            state.opts = $.extend({}, state.opts, options);
            state.link.children().remove();
            state.link.html('');
            state.link.append(state.opts.title);
            if(state.opts.tip){
                state.link.attr('title', state.opts.tip);
            }
        };
        state.hideHeader = function (options){
            state.header.hide();
        };
        state.getHeader = function () {
          return state.header;
        };
        state.getBody = function () {
            return state.body;
        };
        state.add = function (content){
            state.body.append(content);
        };
        state.busy = function (){
        };
        state.notBusy = function (){
        };
        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.panel.defaults = {
        glyph: "glyphicon-question-sign",
        title: "Change my glyph",
        url: "http://getbootstrap.com/components/",
        noPanel: false,
        borders: false,
        content: null,
        hdr:{
            paddingRight: '60px'
        },
        actions: [
            {
                glyph: "glyphicon-cog",
                title: "item 1",
                "items":[
                    {
                        glyph: "glyphicon-asterisk",
                        title: "item 1",
                        clicked: function(node, glyph, title, data){

                        }
                    }
                ]
            }
        ]
    };


    //Plugin Function
    $.fn.panel = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.panel($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $panel = $(this).data('panel');
            if($panel) {
                switch (method) {
                    case 'resize':
                        $panel.resize();
                        break;
                    case 'hideHeader':
                        $panel.hideHeader(options);
                        break;
                    case 'updateHeader':
                        $panel.updateHeader(options);
                        break;
                    case 'getHeader':
                        return $panel.getHeader();
                        break;
                    case 'getBody':
                        return $panel.getBody();
                        break;
                    case 'update':
                        $panel.update(options);
                        break;
                    case 'add':
                        $panel.add(options);
                        break;
                    case 'exists':
                        return (null !== $panel && undefined !== $panel && $panel.length > 0);
                    case 'busy':
                        $panel.busy();break;
                    case 'notBusy':
                        $panel.notBusy();break;
                    case 'state':
                    default:
                        return $panel;
                }
            }
            else{
                console.log('Cannot find panel.');
            }
        }
    };

})(jQuery);
