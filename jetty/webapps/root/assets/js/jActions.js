;(function ($) {

    //Object Instance
    $.jActions = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.jActions.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("actions", state);

        // Private environment methods
        methods = {
            init: function() {
                if(state.opts.actions && state.opts.actions.length>0) {
                    methods.make();
                }
            },
            make: function(){
                state.css('position', 'relative');
                var menu = dCrt('div').css({position: 'absolute', right: '5px', top: '0', color: '#c3c3c3'});
                state.append(menu);
                var parent = dCrt('ul').addClass("nav nav-pills").css({color: "#33333", textAlign: 'left'});
                menu.append(parent);
                var remove = true;
                $.each(state.opts.actions, function(){
                    var act = this;
                    if(act.items && act.items.length>0 && (act.glyph || act.title)) {
                        remove = false;
                        var li = dCrt('li').addClass('dropdown').attr('role', 'presentation');
                        parent.append(li);
                        if(act.tooltip){
                            li.attr('title', act.tooltip);
                        }
                        var a = dCrt('a').addClass('dropdown-toggle').attr('data-toggle', 'dropdown')
                            .attr('href', '#').attr('role', 'button').attr('aria-haspopup', 'true').attr('aria-expanded', 'false').css({color: '#c3c3c3'});
                        if (act.glyph) {
                            var i = dCrt('span').addClass('glyphicon ' + act.glyph).css({
                                marginRight: '5px',
                                fontSize: '16px',
                                top: '8px'
                            });
                            a.append(i);
                        }
                        if (act.title) {
                            var span = dCrt('div').html(act.title)
                                .css({position: 'relative', top: '6px', display: 'inline-block',  marginRight: '5px'});
                            a.append(span);
                        }
                        var carrot = dCrt('span').addClass('glyphicon glyphicon-chevron-down').css({
                            fontSize: '8px',
                            top: '6px'
                        });
                        a.append(carrot);
                        li.append(a);
                        var child = dCrt('ul').addClass('dropdown-menu dropdown-menu-right');
                        li.append(child);
                        $.each(act.items, function(){
                            var sel = this;
                            if(sel.title || sel.glyph) {
                                var sub = dCrt('li');
                                if(sel.tooltip){
                                    sub.attr('title', sel.tooltip);
                                }
                                var glyph,title,img;
                                child.append(sub);
                                var wrapper = dCrt('div').addClass('action-item').css({margin: '2px 5px', textAlign: 'left'});
                                sub.append(wrapper);
                                if (sel.glyph) {
                                    glyph = dCrt('span').addClass('glyphicon ' + sel.glyph).css({
                                        marginRight: '5px',
                                        fontSize: '16px',
                                        position: 'relative',
                                        top: '4px'
                                    });
                                    wrapper.append(glyph);
                                }
                                else if(sel.img) {
                                    var s = dCrt('span').css({
                                     marginRight: '10px',
                                     fontSize: '18px',
                                     position: 'relative',
                                     top: '4px'
                                     });
                                    img = dCrt('img').attr('src', sel.img).css({
                                        height: '18px',
                                        width: '18px'});
                                    wrapper.append(s).append(img);
                                }
                                if (sel.title) {
                                    title = dCrt('div').html(sel.title).css({display: 'inline-block'});
                                    wrapper.append(title);
                                }
                                if(sel.mouseEnter && $.isFunction(sel.mouseEnter)){
                                    var a1 = sel.mouseEnter;
                                    sub.css({cursor: 'pointer'});
                                    sub.on('mouseenter', function(e){
                                        e.preventDefault();
                                        e.stopPropagation();
                                        if(sel.glyph){
                                            a1(sub, glyph, title, state.opts, e);
                                        }
                                        else{
                                            a1(sub, img, title, state.opts, e);
                                        }
                                    });
                                }
                                if(sel.mouseLeave && $.isFunction(sel.mouseLeave)){
                                    var a2 = sel.mouseLeave;
                                    sub.css({cursor: 'pointer'});
                                    sub.on('mouseleave', function(e){
                                        e.preventDefault();
                                        e.stopPropagation();
                                        if(sel.glyph){
                                           a2(sub, glyph, title, state.opts, e);
                                        }
                                        else{
                                            a2(sub, img, title, state.opts, e);
                                        }
                                    });
                                }
                                if(sel.clicked && $.isFunction(sel.clicked)){
                                    var a3 = sel.clicked;
                                    sub.css({cursor: 'pointer'});
                                    sub.on('click', function(e){
                                        if(act.stayOpen || sel.stayOpen) {
                                            e.preventDefault();
                                            e.stopPropagation();
                                        }
                                        if(sel.glyph){
                                            a3(sub, glyph, title, sel, e);
                                        }
                                        else{
                                            a3(sub, img, title, sel, e);
                                        }

                                    });
                                }
                                if(sel.onCreated && $.isFunction(sel.onCreated)){
                                    sub.css({cursor: 'pointer'});
                                    if(sel.glyph){
                                        sel.onCreated(sub, glyph, title, sel);
                                    }else{
                                        sel.onCreated(sub, img, title, sel);
                                    }
                                }
                            }
                        });
                    }
                });
                if(remove){
                    parent.remove();
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.jActions.defaults = {
        actions: [
            {
                glyph: "glyphicon-cog",
                title: "item 1",
                "items":[
                    {
                        glyph: "glyphicon-asterisk",
                        title: "item 1",
                        img: null,
                        mouseEnter: function (node, glyph, title, data) {},
                        mouseLeave: function (node, glyph, title, data) {},
                        clicked: function(node, glyph, title, data){},
                        onCreated: function (node, glyph, title, data) {}
                    }
                ]
            }
        ]
    };


    //Plugin Function
    $.fn.jActions = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.jActions($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $actions = $(this).data('actions');
            switch (method) {
                case 'exists': return (null!=$actions && undefined!=$actions && $actions.length>0);
                case 'state':
                default: return $actions;
            }
        }
    };

})(jQuery);
