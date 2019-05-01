;(function ($) {

    //Object Instance
    $.diagram = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);
        state.opts = $.extend({}, $.diagram.defaults, options);
        // Store a reference to the environment object
        el.data("diagram", state);

        var nodes = [];
        var Node = window.Node,
            Segment = window.Segment;

        var NODE_DIMENSIONS = {
            w: 100,
            h: 100
        };

        // Private environment methods
        var methods = {
            init: function (opts, data) {
                $(this).diagram('destroy');
                nodes = [];
            },
            destroy: function () {
                $(this).children().remove();
            },
            nodeClicked: function (opts) {
                window.open(opts.xrefs[0].xref);
            },
            create: function (options) {
                var on = nodes.length;
                var node=null;
                var last = false;
                var position = {
                    x: (last ? ((state.opts.spacing.left * 2) + state.opts.left)
                        : (state.opts.spacing.left + state.opts.left)),
                    y: state.opts.top + (!last ? (state.opts.spacing.top * on) : 50)
                };
                var perc = Math.round(options.diagram * 100) + '%';

                var child = {
                    title: options.data.title ? options.data.title : 'Unknown name',
                    data: options.data,
                    position: position,
                    titleCls: '',
                    cls: last ? 'square last' : 'circle',
                    contentCls: state.opts.containerCls,
                    content: "hello world",
                    stage: state,
                    w: NODE_DIMENSIONS.w,
                    h: NODE_DIMENSIONS.h,
                    x: position.x,
                    y: position.y,
                    index: Math.floor((Math.random() * 99999) + 1),
                    nodeClick: function (e) {
                        //nodeClicked(nodeData);
                    },
                    titleClick: function (e) {
                        methods.nodeClicked(options);
                    }
                };
                var found = false;
                $.each(nodes, function () {
                    if (options.data[options.linkKey] === this.data[options.linkKey]) {
                        node = this;
                        found = true;
                        return false;
                    }
                });
                if (!found) {
                    if (last) {
                        child.position = position;
                        child.x = position.x;
                        child.y = position.y;
                        child.content = null;
                        child.contentCls = '';
                    }
                    node = new Node(child);
                    nodes.push(node.attach());
                }
                new Segment({
                    h: 2,
                    linkText: perc,
                    stage: state,
                    origin: options.parent,
                    destination: node,
                    index: Math.round(Math.random() * 9999),
                    nodeToType: (last ? 'square' : 'circle')
                }).attach();

                return node;
            },
            unselectable: function (children) {
                $.each(children, function () {
                    if (!$(this).is('input')) {
                        $(this).addClass('unselectable');
                    }
                    var children = $(this).children();
                    if ($(children).length > 0) {
                        methods.unselectable(children);
                    }
                });
            },
            noOverlap: function () {
                var found = false;
                state.children().each(function () {
                    if ($(this).hasClass('node') && !$(this).hasClass('first')) {
                        var cur = this;
                        state.children().each(function () {
                            if ($(this).hasClass('node') && !$(this).hasClass('first')) {
                                if (this != cur) {
                                    if (isObjOnObj(cur, this)) {
                                        methods.moveOne();
                                        found = true;
                                        return false;
                                    }
                                }
                            }
                        });
                        if (found) {
                            return false;
                        }
                    }
                });
            },
            moveOne: function () {
                state.children().each(function () {
                    if ($(this).hasClass('node') && !$(this).hasClass('root')) {
                        var offset = $(this).offset();
                        var adjust = { x: (offset.left < center.x ? -10 : 10), y: (offset.top < center.y ? -10 : 10) };
                        var top = offset.top + adjust.y;
                        var left = offset.left + adjust.x;
                        $(this).addClass('selected');
                        $(this).animate({ top: top, left: left }, 200, function () {
                            state.children().removeClass('selected');
                        });
                    }
                });
            },
            isObjOnObj: function (a, b) {
                var offsetA = $(a).offset();
                var offsetB = $(b).offset();
                var al = offsetA.left;
                var ar = offsetA.left + $(a).width();
                var bl = offsetB.left;
                var br = offsetB.left + $(b).width();

                var at = offsetA.top;
                var ab = offsetA.top + $(a).height();
                var bt = offsetB.top;
                var bb = offsetB.top + $(b).height();

                //if (bl > ar || br < al) { return false; }//overlap not possible
                //if (bt > ab || bb < at) { return false; }//overlap not possible

                if (bl > al && bl < ar) { return true; }
                if (br > al && br < ar) { return true; }

                if (bt > at && bt < ab) { return true; }
                if (bb > at && bb < ab) { return true; }

                return false;
            }

        };
        //public methods
        state.create = function(options){
            return methods.create(options);
        };

        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.diagram.defaults = {
        left: 0,
        top: 0,
        spacing: {
            left: 10,
            top: 10
        }
    };


    //Plugin Function
    $.fn.diagram = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.diagram($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $diagram = $(this).data('diagram');
            switch (method) {
                case 'addNode': return $diagram.create(options);break;
                case 'state':
                default: return $diagram;
            }
        }
    };

})(jQuery);
