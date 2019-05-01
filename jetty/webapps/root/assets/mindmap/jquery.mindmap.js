;(function ($) {

    var TIMEOUT = 4;  // movement timeout in seconds
    var CENTRE_FORCE = 3;  // strength of attraction to the centre by the active node

    function _getRandomId(prefix){
        var rn = function(){return Math.floor(Math.random()*999999);};
        return prefix + '_' + rn() + '_' + rn();
    }
    // Define all Node related functions.
    var Node = function(obj, node, name, parent, opts) {
        this.id = _getRandomId('node');
        node.attr('id', this.id);
        this.obj = obj;
        this.options = obj.options;
        this.nodes = [];
        this.name = name;
        this.item = opts.item;
        this.href = opts.href;
        this.runDefault = false;
        if (opts.url) {
            this.url = opts.url;
        }

        // create the element for display
        this.el = node;
        this.el.addClass('node');

        if (!parent) {
            obj.activeNode = this;
            $(this.el).addClass('active');
            $(this.el).addClass('root');
        } else {
            var lineno = obj.lines.length;
            obj.lines[lineno] = new Line(obj, this, parent);
        }
        if(parent) {
            if (!parent.nodes) {
                parent.nodes = [];
            }
            this.parent = parent;
            this.parent.nodes.push(this);
        }

        // animation handling
        this.moving = false;
        this.moveTimer = 0;
        this.obj.movementStopped = false;
        this.visible = true;
        this.hasLayout = true;
        this.x = 1;
        this.y = 1;
        this.dx = 0;
        this.dy = 0;
        this.hasPosition = false;

        this.content = []; // array of content elements to display onclick;

        this.el.css('position','absolute');

        var thisnode = this;
        this.el.draggable({
            drag: function () {
                obj.root.animateToStatic();
            }
        });

        function disableContextMenu(elem){
            $(elem).on('contextmenu', function(e) {
                return false;
            });
            $.each($(elem).children(), function(){
                disableContextMenu(this);
            });
        }

        disableContextMenu(this.el);

        var stopWatch;
        this.el.on("mousedown", function(e){
            if(e.which==1){
                stopWatch = new $.jCommon.stopWatch();
                stopWatch.start();
            }
        });
        this.el.on("mouseup", function(e){
            var onclick = null;
            if(e.which==1){
                stopWatch.stop();
                if(stopWatch.isLTE(100)) {
                    onclick = opts.leftClick;
                }
            }
            else if(e.which==3){
                onclick = opts.rightClick;
            }
            if(onclick && $.isFunction(onclick)){
                onclick(e, thisnode);
            }
            if(thisnode.runDefault)
            {
                if (obj.activeNode) {
                    obj.activeNode.el.removeClass('active');
                    if (obj.activeNode.parent){
                        obj.activeNode.parent.el.removeClass('activeparent');
                    }
                }
                obj.activeNode = thisnode;
                obj.activeNode.el.addClass('active');

                if (obj.activeNode.parent) {
                    obj.activeNode.parent.el.addClass('activeparent');
                    obj.root.animateToStatic();
                }
            }
        });

        if(obj.options.cls){
            node.addClass(obj.options.cls);
        }
    };

    // ROOT NODE ONLY:  control animation loop
    Node.prototype.animateToStatic = function() {
        clearTimeout(this.moveTimer);
        // stop the movement after a certain time
        var thisnode = this;
        this.moveTimer = setTimeout(function() {
            //stop the movement
            thisnode.obj.movementStopped = true;
        }, TIMEOUT*1000);

        if (this.moving) return;
        this.moving = true;
        this.obj.movementStopped = false;
        this.animateLoop();
    };

    // ROOT NODE ONLY:  animate all nodes (calls itself recursively)
    Node.prototype.animateLoop = function() {
        this.obj.canvas.clear();
        for (var i = 0; i < this.obj.lines.length; i++) {
            this.obj.lines[i].updatePosition();
        }
        if (this.findEquilibrium() || this.obj.movementStopped) {
            this.moving=false;
            return;
        }
        var mynode = this;
        setTimeout(function() {
            mynode.animateLoop();
        }, 5);
    };

    // find the right position for this node
    Node.prototype.findEquilibrium = function() {
        var result = this.display();
        for (var i=0;i<this.nodes.length;i++) {
            result = this.nodes[i].findEquilibrium();
        }
        return result;
    };

    //Display this node, and its children
    Node.prototype.display = function(depth) {
        if(null!=this.obj.activeNode && undefined != this.obj.activeNode) {
            if (this.visible) {
                // if: I'm not active AND my parent's not active AND my children aren't active ...
                if (this.obj.activeNode !== this && this.obj.activeNode !== this.parent && this.obj.activeNode.parent !== this) {
                    // TODO hide me!
                    this.el.hide();
                    this.visible = false;
                }
            } else {
                if (this.obj.activeNode === this || this.obj.activeNode === this.parent || this.obj.activeNode.parent === this) {
                    this.el.show();
                    this.visible = true;
                }
            }
        }
        if (typeof(depth)=='undefined') depth=0;
        this.drawn = true;
        // am I positioned?  If not, position me.
        if (!this.hasPosition) {
            this.x = this.options.mapArea.x/2;
            this.y = this.options.mapArea.y/2;
            this.el.css('left', this.x + "px");
            this.el.css('top', this.y + "px");
            this.hasPosition=true;
        }
        // are my children positioned?  if not, lay out my children around me
        var stepAngle = Math.PI*2/this.nodes.length;
        var parent = this;

        $.each(this.nodes, function(index) {
            var node = this;
            if (!node.hasPosition) {
                if (!node.options.showProgressive || depth<=1) {
                    var angle = index * stepAngle;
                    node.x = (50 * Math.cos(angle)) + parent.x;
                    node.y = (50 * Math.sin(angle)) + parent.y;
                    node.hasPosition=true;
                    node.el.css('left', node.x + "px");
                    node.el.css('top', node.y + "px");
                }
            }
        });
        // update my position
        return this.updatePosition();
    };

    // updatePosition returns a boolean stating whether it's been static
    Node.prototype.updatePosition = function(){
        if ($(this.el).hasClass("ui-draggable-dragging")) {
            this.x = parseInt(this.el.css('left')) + ($(this.el).width() / 2);
            this.y = parseInt(this.el.css('top')) + ($(this.el).height() / 2);
            this.dx = 0;
            this.dy = 0;
            return false;
        }

        //apply accelerations
        var forces = this.getForceVector();
        this.dx += forces.x * this.options.timeperiod;
        this.dy += forces.y * this.options.timeperiod;

        // damp the forces
        this.dx = this.dx * this.options.damping;
        this.dy = this.dy * this.options.damping;

        //ADD MINIMUM SPEEDS
        if (Math.abs(this.dx) < this.options.minSpeed) this.dx = 0;
        if (Math.abs(this.dy) < this.options.minSpeed) this.dy = 0;
        if (Math.abs(this.dx)+Math.abs(this.dy)==0) return true;
        //apply velocity vector
        this.x += this.dx * this.options.timeperiod;
        this.y += this.dy * this.options.timeperiod;
        this.x = Math.min(this.options.mapArea.x,Math.max(1,this.x));
        this.y = Math.min(this.options.mapArea.y,Math.max(1,this.y));
        // display
        var showx = this.x - ($(this.el).width() / 2);
        var showy = this.y - ($(this.el).height() / 2) - 10;
        this.el.css('left', showx + "px");
        this.el.css('top', showy + "px");
        return false;
    };

    Node.prototype.getForceVector = function(){
        var fx = 0;
        var fy = 0;
        var radial = this.options.radial;

        var nodes = this.obj.nodes;
        var lines = this.obj.lines;

        // Calculate the repulsive force from every other node
        for (var i = 0; i < nodes.length; i++) {
            if (nodes[i] == this) continue;
            if (this.options.showSublines && !nodes[i].hasLayout) continue;
            if (!nodes[i].visible) continue;
            // Repulsive force (coulomb's law)
            var x1 = (nodes[i].x - this.x);
            var y1 = (nodes[i].y - this.y);
            var xsign = x1 / Math.abs(x1);
            var ysign = y1 / Math.abs(y1);
            var dist = Math.sqrt((x1 * x1) + (y1 * y1));
            var theta = Math.atan(y1 / x1);
            if (x1 == 0) {
                theta = Math.PI / 2;
                xsign = 0;
            }
            // force is based on radial distance
            var myrepulse = this.options.repulse;
            var f = (myrepulse * radial) / (dist * dist);
            if (Math.abs(dist) < radial) {
                fx += -f * Math.cos(theta) * xsign;
                fy += -f * Math.sin(theta) * xsign;
            }
        }
        // add repulsive force of the "walls"
        //left wall
        var xdist = this.x + $(this.el).width();
        var f = (this.options.wallrepulse * radial) / (xdist * xdist);
        fx += Math.min(2, f);
        //right wall
        var rightdist = (this.options.mapArea.x - xdist);
        var f = -(this.options.wallrepulse * radial) / (rightdist * rightdist);
        fx += Math.max(-2, f);
        //top wall
        var f = (this.options.wallrepulse * radial) / (this.y * this.y);
        fy += Math.min(2, f);
        //bottom wall
        var bottomdist = (this.options.mapArea.y - this.y);
        var f = -(this.options.wallrepulse * radial) / (bottomdist * bottomdist);
        fy += Math.max(-2, f);

        // for each line, of which I'm a part, add an attractive force.
        for (var i = 0; i < lines.length; i++) {
            var otherend = null;
            if (lines[i].start == this) {
                otherend = lines[i].end;
            } else if (lines[i].end == this) {
                otherend = lines[i].start;
            } else continue;
            // Ignore the pull of hidden nodes
            if (!otherend.visible) continue;
            // Attractive force (hooke's law)
            var x1 = (otherend.x - this.x);
            var y1 = (otherend.y - this.y);
            var dist = Math.sqrt((x1 * x1) + (y1 * y1));
            var xsign = x1 / Math.abs(x1);
            var theta = Math.atan(y1 / x1);
            if (x1==0) {
                theta = Math.PI / 2;
                xsign = 0;
            }
            // force is based on radial distance
            var f = (this.options.attract * dist) / 10000;
            if (Math.abs(dist) > 0) {
                fx += f * Math.cos(theta) * xsign;
                fy += f * Math.sin(theta) * xsign;
            }
        }

        // if I'm active, attract me to the centre of the area
        if (this.obj.activeNode === this) {
            // Attractive force (hooke's law)
            var otherend = this.options.mapArea;
            var x1 = ((otherend.x / 2) - this.options.offset.x - this.x);
            var y1 = ((otherend.y / 2) - this.options.offset.y - this.y);
            var dist = Math.sqrt((x1 * x1) + (y1 * y1));
            var xsign = x1 / Math.abs(x1);
            var theta = Math.atan(y1 / x1);
            if (x1 == 0) {
                theta = Math.PI / 2;
                xsign = 0;
            }
            // force is based on radial distance
            var f = (0.1 * this.options.attract * dist * CENTRE_FORCE) / 1000;
            if (Math.abs(dist) > 0) {
                fx += f * Math.cos(theta) * xsign;
                fy += f * Math.sin(theta) * xsign;
            }
        }

        if (Math.abs(fx) > this.options.maxForce) fx = this.options.maxForce * (fx / Math.abs(fx));
        if (Math.abs(fy) > this.options.maxForce) fy = this.options.maxForce * (fy / Math.abs(fy));
        return {
            x: fx,
            y: fy
        };
    };

    // Define all Line related functions.
    function Line(obj, startNode, endNode){
        this.obj = obj;
        this.options = obj.options;
        this.start = startNode;
        this.colour = startNode.options.colour;
        this.size = startNode.options.lineSize;
        this.opacity = startNode.options.lineOpacity;
        this.end = endNode;
    }

    Line.prototype.updatePosition = function(){
        if (this.options.showSublines && (!this.start.hasLayout || !this.end.hasLayout)) return;
        if (!this.options.showSublines && (!this.start.visible || !this.end.visible)) return;

        switch (this.colour) {
            case "red":
                this.strokeStyle = "#ebccd1";
                break;
            case "blue":
                    this.strokeStyle = "#bce8f1";
                break;
            case "green":
                this.strokeStyle = "#d6e9c6";
                break;
            case "yellow":
                this.strokeStyle = "#faebcc";
                break;
            default:
                this.strokeStyle = "#c3c3c3";
                break;
        }
        this.obj.canvas.path("M"+this.start.x+' '+(this.start.y)+"L"+(this.end.x)+' '+this.end.y).attr({stroke: this.strokeStyle, opacity: this.opacity, 'stroke-width': this.size + 'px'});
    };

    //Object Instance
    $.mindmap = function(el, options) {
        var state = $(el),
            methods = {};

        state.options = $.extend({}, $.mindmap.defaults, options);

        // Store a reference to the environment object
        el.data("mindmap", state);

        // Private environment methods
        methods = {
            init: function(){
                state.mindmapInit = true;
                state.nodes = [];
                state.lines = [];
                state.activeNode = null;
                state.animateToStatic = function() {
                    try {
                      state.root.animateToStatic();
                    }catch (e){}
                };

                methods.resize();

                //canvas
                state.options.mapArea.offset = {x:0,y:0};

                state.options.mapArea.x = state.width();
                if (state.options.mapArea.x==-1) {
                    state.options.mapArea.offset.x = state.offset().left;
                }
                state.options.mapArea.y = state.height();
                if (state.options.mapArea.y==-1) {
                    state.options.mapArea.offset.y = state.offset().top;
                }
                //create drawing area
                state.canvas = Raphael(state.options.svgOffset.left, state.options.svgOffset.top,0,0);// Raphael(state.options.mapArea.offset.x, state.options.mapArea.offset.y,state.options.mapArea.x, state.options.mapArea.y);
                //$(state.canvas.canvas).css({top: state.options.mapArea.offset.y + 'px', left: state.options.mapArea.offset.x + 'px'});

                var svg = $('svg');
                state.append(svg.width(state.width()).height(state.height()));

                // Add a class to the object, so that styles can be applied
                $(this).addClass('js-mindmap-active');

                // Add keyboard support (thanks to wadefs)
                $(this).keyup(function(event) {
                    switch (event.which) {
                        case 33: // PgUp
                        case 38: // Up, move to parent
                            if (state.activeNode.parent) {
                                state.activeNode.parent.el.click();
                            }
                            break;
                        case 13: // Enter (change to insert a sibling)
                        case 34: // PgDn
                        case 40: // Down, move to first child
                            if (state.activeNode.children.length) {
                                state.activeNode.children[0].el.click();
                            }
                            break;
                        case 37: // Left, move to previous sibling
                            var activeParent = state.activeNode.parent;
                            if (activeParent) {
                                var newNode = null;
                                if (activeParent.nodes[0] === state.activeNode) {
                                    newNode = activeParent.nodes[activeParent.nodes.length - 1];
                                } else {
                                    for (var i = 1; i < activeParent.nodes.length; i++) {
                                        if (activeParent.nodes[i] === state.activeNode) {
                                            newNode = activeParent.nodes[i - 1];
                                        }
                                    }
                                }
                                if (newNode) {
                                    newNode.el.click();
                                }
                            }
                            break;
                        case 39: // Right, move to next sibling
                            var activeParent = state.activeNode.parent;
                            if (activeParent) {
                                var newNode = null;
                                if (activeParent.nodes[activeParent.nodes.length - 1] === state.activeNode) {
                                    newNode = activeParent.nodes[0];
                                } else {
                                    for (var i = activeParent.nodes.length - 2; i >= 0; i--) {
                                        if (activeParent.nodes[i] === state.activeNode) {
                                            newNode = activeParent.nodes[i + 1];
                                        }
                                    }
                                }
                                if (newNode) {
                                    newNode.el.click();
                                }
                            }
                            break;
                        case 45: // Ins, insert a child
                            break;
                        case 46: // Del, delete this node
                            break;
                        case 27: // Esc, cancel insert
                            break;
                        case 83: // 'S', save
                            break;
                    }
                });
            },
            resize: function() {
                var rTime = new Date(1, 1, 2000, 12, 0, 0);
                var timeout = false;
                var delta = 500;
                function resizeEnd(){
                    if (new Date() - rTime < delta) {
                        setTimeout(resizeEnd, delta);
                    } else {
                        timeout = false;
                        $('svg').width(state.width()).height(state.height());
                        window.setTimeout(function(){
                            state.animateToStatic();
                        }, 100);
                    }
                }
                $(window).resize(function() {
                    rTime = new Date();
                    if (timeout === false) {
                        timeout = true;
                        setTimeout(function(){
                            resizeEnd();
                        }, delta);
                    }
                });
            }
        };
        //public methods
        state.addNode = function(options){
            var node = state.nodes[state.nodes.length] = new Node(state, options.node, options.name, options.parent, options);
            if(null==options.parent || undefined==options.parent){
                state.root = node;
            }
            else if(options.parent){
                options.parent.animateToStatic();
            }
            return node;
        };
        state.removeNode = function (node) {
            node.el.remove();
            var tempNodes = [];
            $.each(state.nodes, function(){
                if (this.id!=node.id) {
                    tempNodes.push(this);
                }
            });
            state.nodes = tempNodes;
            var tempLines = [];
            $.each(state.lines, function(){
                if ((this.start.id != node.id) && (this.end.id != node.id)) {
                    tempLines.push(this);
                }
            });
            state.lines = tempLines;
        };

        state.clear = function(options){
            var result = false;
            if(state.canvas){
                try {
                    state.canvas.clear();
                    state.lines = [];
                    state.nodes = [];
                    result = true;
                }
                catch (e){
                    console.log(e);
                }
            }
            return result;
        };

        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.mindmap.defaults = {
        attract: 15,
        repulse: 6,
        damping: 0.55,
        timeperiod: 10,
        wallrepulse: 0.4,
        radial: 500,
        mapArea: {
            x:-1,
            y:-1
        },
        canvasError: 'alert',
        minSpeed: 0.05,
        maxForce: 0.1,
        showSublines: false,
        updateIterationCount: 20,
        showProgressive: true,
        offset:{ x: 0, y: 0},
        svgOffset:{ top: 0, left: 0},
        timer: 0,
        lineSize: 2,
        lineOpacity:.5,
        colour: 'grey',
        cls: 'leaf'
    };


    //Plugin Function
    $.fn.mindmap = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                return new $.mindmap($(this),method);
            });
        } else{
            var $mindmap = $(this).data('mindmap');
            if($mindmap) {
                switch (method) {
                    case 'addNode':
                        return $mindmap.addNode(options);
                        break;
                    case 'removeNode':
                        return $mindmap.removeNode(options);
                        break;
                    case 'clear':
                        return $mindmap.clear();
                        break;
                    case 'state':
                    default:
                        return $mindmap;
                }
            }
            else{
                return false;
            }
        }
    };

})(jQuery);
