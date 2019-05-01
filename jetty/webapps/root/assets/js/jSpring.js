
;(function ($) {

    //Object Instance
    $.spring = function (el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.spring.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("spring", state);

        // Private environment methods
        methods = {
            init: function () {
                if (!$.jCommon.is.array(state.opts.leafs)) {
                    return false;
                }
                methods.html.init();
            },
            html: {
                init: function () {
                    var aid = $.jCommon.getRandomId('spring');
                    state.group = dCrt("div").addClass('panel-group no-border').attr('id', aid).attr('role', 'tablist').attr('aria-multiselectable', 'true');
                    var nodes = [];
                    $.each(state.opts.leafs, function () {
                        var item = this;
                        var hid = $.jCommon.getRandomId('head');
                        var id = $.jCommon.getRandomId('panel');
                        var leaf = dCrt('div').addClass('panel panel-default spring-panel');
                        state.group.append(leaf);
                        item.leaf = leaf;

                        var head = dCrt('div').addClass('panel-heading spring-heading').attr('id', hid).attr('role', 'tab').css(state.opts.hdrCss);
                        leaf.append(head);
                        var th = dCrt('div').addClass('spring-title');
                        head.append(th);
                        var lId = $.jCommon.getRandomId('link');
                        var a = dCrt('a').attr('id', lId);
                        if(state.opts.collapseAll) {
                            a.attr("role", "button").attr("data-toggle", "collapse")
                                .attr("data-parent", '#' + aid).attr("href", '#' + id)
                                .attr("aria-expanded", !state.opts.collapsed).attr("aria-controls", id);
                        }
                        th.append(a);
                        var chevron = methods.html.chevron(state.opts.collapsed);
                        a.append(chevron.addClass('spring-chev'));
                        var t = dCrt(item.link ? 'a' : 'div').append(item.title);
                        if (item.link) {
                            t.attr('target', '_blank').attr('href', item.link);
                        }
                        th.append(t);

                        var e = {leaf: leaf, item: item, node: th, spring: true };
                        if($.isFunction(state.opts.nodeCount)){
                            state.opts.nodeCount(e);
                        }
                        if($.isFunction(state.opts.onVisible)){
                           nodes.push(e);
                        }

                        var bw = dCrt('div').attr('id', id);
                        if(state.opts.collapseAll) {
                            bw.addClass('panel-collapse collapse').attr('role', 'tabpanel').attr('aria-labelledby', hid);
                        }
                        if(!state.opts.collapsed){
                            bw.addClass('in');
                        }
                        leaf.append(bw);
                        var body = dCrt('div').addClass('panel-body spring-body');
                        if(!state.opts.collapseAll && state.opts.collapsed){
                            body.hide();
                        }
                        bw.append(body);
                        body.append(item.content);
                        if(!state.opts.collapsed&& $.isFunction(item.onExpand)){
                            item.onExpand(item, leaf);
                        }
                        chevron.on('click', function (e) {
                            var check;
                            if(state.opts.collapseAll) {
                                var d = state.group.find('.glyphicon-triangle-top');
                                if (d) {
                                    check = d.parent().attr('id');
                                    d.removeClass('glyphicon-triangle-top').addClass('glyphicon-triangle-bottom');
                                }
                                if(!check || check !== lId) {
                                    a.find('span').removeClass('glyphicon-triangle-bottom').addClass('glyphicon-triangle-top');
                                }
                            }
                            else{
                                if(chevron.hasClass('expanded')){
                                    chevron.removeClass('glyphicon-triangle-top expanded').addClass(('glyphicon-triangle-bottom'));
                                    body.slideUp();
                                }
                                else{
                                    chevron.addClass('glyphicon-triangle-top expanded').removeClass(('glyphicon-triangle-bottom'));
                                    body.slideDown();
                                }
                            }

                            var expand = chevron.hasClass('glyphicon-triangle-top');
                            if(expand && item.onExpand && $.isFunction(item.onExpand)){
                                item.onExpand(item, leaf);
                            }
                        });
                    });
                    if(state.opts.scroller && $.isFunction(state.opts.onVisible)){
                        function check() {
                            $.each(nodes, function () {
                                if(state.opts.scroller.scrollHandler('isInViewport', this.leaf)){
                                    state.opts.onVisible(this);
                                }
                            });
                        }
                        state.opts.scroller.scrollHandler({
                            adjust: 0,
                            start: function () {
                            },
                            stop: function () {
                               check();
                            },
                            top: function () {},
                            bottom: function () {}
                        });
                        check();
                    }
                    state.append(state.group);
                },
                chevron: function (collapsed) {
                    var t = 'glyphicon-triangle-top';
                    var b = 'glyphicon-triangle-bottom';
                    return $.htmlEngine.glyph(collapsed ? b : t).css({paddingRight: '10px'});
                }
            }
        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.spring.defaults = {
        collapsed: true,
        collapseAll: true,
        offset: {
            title: '-4px',
            chevron: '0px'
        },
        hdrCss: {fontSize: '14px', padding: '10px'}
    };


    //Plugin Function
    $.fn.spring = function (method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function () {
                new $.spring($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $spring = $(this).data('spring');
            switch (method) {
                case 'exists':
                    return (null !== $spring && undefined !== $spring && $spring.length > 0);
                case 'state':
                default:
                    return $spring;
            }
        }
    };

})(jQuery);
