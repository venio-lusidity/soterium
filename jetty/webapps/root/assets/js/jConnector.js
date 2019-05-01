;(function ($) {

    //Object Instance
    $.connector = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);
        state.canvas = $(document.createElement('canvas'));
        state.edges = [];
        state.ctx=null; 

        state.opts = $.extend(true, {}, $.connector.defaults, options);

        // Store a reference to the environment object
        el.data("connector", state);

        // Private environment methods
        methods = {
            init: function() {
                state.append(state.canvas);
                window.setTimeout(function(){methods.resize();}, 1000);
                $(window).resize(function(){
                    methods.resize();
                });
            },
            applyId: function(node, prefix){
                var rn = function(){return Math.floor(Math.random()*999999);};
                var id = prefix + '_' + rn() + '_' + rn();
                if(null == methods.getId(node) || undefined == methods.getId(node)){
                    node.attr('data-edgeId', id);
                }
            },
            makeDot: function(point, options){
                state.ctx.fillStyle = options.borderColor;
                var circle = new Path2D();
                circle.moveTo(point.x, point.y);
                circle.arc(point.x, point.y, 8, 0, 2 * Math.PI);
                state.ctx.fill(circle)  
            },
            makeArrow: function(point, options, side) {
                state.ctx.fillStyle = options.borderColor;
                state.ctx.moveTo(point.x, point.y);
                var s = 4;
                if (side === 'top') {
                    state.ctx.lineTo(point.x - s, point.y);
                    state.ctx.lineTo(point.x, point.y + s);
                    state.ctx.lineTo(point.x + s, point.y);
                }
                else if (side === 'bottom') {
                    state.ctx.lineTo(point.x - s, point.y);
                    state.ctx.lineTo(point.x, point.y - s);
                    state.ctx.lineTo(point.x + s, point.y);
                }
                else if (side == 'left') {
                    state.ctx.lineTo(point.x, point.y - s);
                    state.ctx.lineTo(point.x + s, point.y);
                    state.ctx.lineTo(point.x, point.y + s);
                }
                else if (side === 'right') {
                    state.ctx.lineTo(point.x, point.y - s);
                    state.ctx.lineTo(point.x - s, point.y);
                    state.ctx.lineTo(point.x, point.y + s);
                }
                state.ctx.lineTo(point.x, point.y);
                state.ctx.fill();
            },
            setColors: function(options){
                if(!options.borderColor && !options.color) {
                    switch (options.status) {
                        case 'blue':
                            options.borderColor = '#d9edf7';
                            options.color = '#d9edf7';
                            break;
                        case 'green':
                            options.borderColor = '#dff0d8';
                            options.color = '#dff0d8';
                            break;
                        case 'yellow':
                            options.borderColor = '#fcf8e3';
                            options.color = '#fcf8e3';
                            break;
                        case 'red':
                            options.borderColor = '#f2dede';
                            options.color = '#f2dede';
                            break;
                            break;
                        default:
                            options.borderColor = '#ddd';
                            options.color = '#ddd';
                            break;
                    }
                }
                return options;
            },
            makeLine: function(point){
                state.ctx.lineTo(point.x, point.y);
                state.ctx.moveTo(point.x, point.y);
            },
            makeFinal: function(options) {
                var _dash;
                //To decide style of the line. dotted or solid
                switch (options.style) {
                    case 'dashed':
                        _dash = [4, 2];
                        break;
                    case 'dotted':
                        _dash = [4, 2];
                        break;
                    default:
                    case 'solid':
                        _dash = [0, 0];
                        break;
                }

                if (!state.ctx.setLineDash) {
                    state.ctx.setLineDash = function() {}
                } else {
                    state.ctx.setLineDash(_dash);
                }

                state.ctx.lineWidth = options.darkLineWidth || 4;
                state.ctx.strokeStyle = options.borderColor;
                state.ctx.stroke();

                state.ctx.lineWidth = options.lightLineWidth || 2;
                state.ctx.strokeStyle =  options.color;
                state.ctx.stroke();
            },
            collides: function(node, point){
                var offX = state.canvas.parent().offset().left;
                var offY = state.canvas.parent().offset().top;

                var top = node.offset().top-offY;
                var left = node.offset().left-offX;
                var right = left + node.outerWidth();
                var bottom = top + node.outerHeight();

                return (point.x>=left && point.x<=right && point.y>=top && point.y<=bottom);
            },
            connect: {
                elbow: function(options) {
                    try {
                        state.ctx = state.canvas[0].getContext('2d');
                        state.ctx.beginPath();
                        methods.setColors(options);
                        var fromNode = options.fromNode;
                        var toNode = options.toNode;
                        /*
                         options = {
                         fromNode - Left Element - Mandatory
                         toNode - Right Element - Mandatory
                         status - accepted, rejected, modified, (none) - Optional
                         style - (dashed), solid, dotted - Optional	
                         horizontal_gap - (0), Horizontal Gap from original point
                         error - show, (hide) - To show error or not
                         width - (2) - Width of the line
                         }
                         */
                        //Get Left point and Right Point
                        var offX = state.canvas.parent().offset().left;
                        var offY = state.canvas.parent().offset().top;

                        var fromOff = options.fromNode.offset();
                        var toOff = options.toNode.offset();

                        fromOff.left -= offX;
                        toOff.left -= offX;
                        fromOff.top -= offY;
                        toOff.top -= offY;

                        var point = {x: 0, y: 0};
                        if (fromOff.left !== toOff.left && fromOff.top < toOff.top) {
                            options.sides = {
                                from: 'bottom',
                                to: 'top'
                            };
                            point.x = (fromOff.left + (fromNode.outerWidth() / 2));
                            point.y = ((fromOff.top + fromNode.outerHeight()) + ((options.fromArrow) ? 8 : 0));
                            state.ctx.moveTo(point.x, point.y);
                            methods.makeLine(point);

                            if (options.fromDot) {
                                methods.makeDot(point, options);
                            }

                            if (options.fromArrow) {
                                methods.makeArrow(point, options, options.sides.from);
                            }

                            var gapDif1 = ((toOff.top - (fromOff.top + fromNode.outerHeight())) / 2);

                            var test = {y: point.y + gapDif1, x: point.x};

                            $.each(state.edges, function () {
                                var n = this.fromNode;
                                var c = methods.collides(n, test);
                                if (!c) {
                                    n = this.toNode;
                                    c = methods.collides(n, test);
                                }
                                if (c) {
                                    gapDif1 += n.outerHeight();
                                }
                                return !c;
                            });

                            var gapDif2 = ((toOff.top - (fromOff.top + fromNode.outerHeight())) / 2);

                            point.y += gapDif1;
                            methods.makeLine(point);
                            methods.makeDot(point, options);
                            point.x = (toOff.left + (toNode.outerWidth()) / 2);
                            methods.makeLine(point);
                            methods.makeDot(point, options);
                            point.y += (gapDif2 - ((options.toArrow) ? 8 : 0));
                            methods.makeLine(point);
                            if (options.toDot) {
                                methods.makeDot(point, options);
                            }
                            if (options.toArrow) {
                                methods.makeArrow(point, options, options.sides.to);
                            }
                            methods.makeFinal(options);
                        }
                        else if (fromOff.left === toOff.left && fromOff.top > toOff.top) {
                            options.sides = {
                                from: 'top',
                                to: 'bottom'
                            };
                            point.x = (fromOff.left + (fromNode.outerWidth() / 2));
                            point.y = ((fromOff.top) - (options.fromArrow ? 10 : 0));
                            if (options.fromDot) {
                                methods.makeDot(point, options);
                            }
                            if (options.fromArrow) {
                                methods.makeArrow(point, options, options.sides.from);
                            }
                            methods.makeLine(point, options);
                            point.y = ((toOff.top + toNode.outerWidth()) + (options.toArrow ? 10 : 0));
                            methods.makeLine(point, options);
                            if (options.toDot) {
                                methods.makeDot(point, options);
                            }
                            if (options.toArrow) {
                                methods.makeArrow(point, options, options.sides.to);
                            }
                            methods.makeFinal(options);
                        }
                        else if (fromOff.left === toOff.left && fromOff.top < toOff.top) {
                            options.sides = {
                                from: 'bottom',
                                to: 'top'
                            };
                            point.x = (fromOff.left + (fromNode.outerWidth() / 2));
                            point.y = ((fromOff.top + fromNode.outerHeight()) + (options.fromArrow ? 10 : 0));
                            if (options.fromDot) {
                                methods.makeDot(point, options);
                            }
                            if (options.fromArrow) {
                                methods.makeArrow(point, options, options.sides.from);
                            }
                            methods.makeLine(point, options);
                            point.y = (toOff.top - (options.toArrow ? 10 : 0));
                            methods.makeLine(point, options);
                            if (options.toDot) {
                                methods.makeDot(point, options);
                            }
                            if (options.toArrow) {
                                methods.makeArrow(point, options, options.sides.to);
                            }
                            methods.makeFinal(options);
                        }
                        else if (fromOff.left < toOff.left && fromOff.top === toOff.top) {
                            options.sides = {
                                from: 'right',
                                to: 'left'
                            };
                            point.x = ((fromOff.left + fromNode.outerWidth()) + (options.fromArrow ? 10 : 0));
                            point.y = (fromOff.top + fromNode.outerHeight() / 2);

                            if (options.fromDot) {
                                methods.makeDot(point, options);
                            }

                            if (options.fromArrow) {
                                methods.makeArrow(point, options, options.sides.from);
                            }

                            methods.makeLine(point, options);

                            point.x = (toOff.left - (options.toArrow ? 10 : 0));
                            methods.makeLine(point, options);

                            if (options.toDot) {
                                methods.makeDot(point, options);
                            }

                            if (options.toArrow) {
                                methods.makeArrow(point, options, options.sides.to);
                            }

                            methods.makeFinal(options);
                        }
                        else if (fromOff.left > toOff.left && fromOff.top === toOff.top) {
                            options.sides = {
                                from: 'left',
                                to: 'right'
                            };
                            point.x = ((fromOff.left) - (options.fromArrow ? 10 : 0));
                            point.y = (fromOff.top + fromNode.outerHeight() / 2);

                            if (options.fromDot) {
                                methods.makeDot(point, options);
                            }

                            if (options.fromArrow) {
                                methods.makeArrow(point, options, options.sides.from);
                            }

                            methods.makeLine(point, options);

                            point.x = ((toOff.left + toNode.outerWidth()) + (options.toArrow ? 10 : 0));
                            methods.makeLine(point, options);

                            if (options.toDot) {
                                methods.makeDot(point, options);
                            }

                            if (options.toArrow) {
                                methods.makeArrow(point, options, options.sides.to);
                            }

                            methods.makeFinal(options);
                        }
                    } catch (e) {}
                },
                line: function(options){
                    try {
                        state.ctx = state.canvas[0].getContext('2d');
                        state.ctx.beginPath();

                        var _borderColor;
                        var _color;
                        var _dash;
                        var _left = {}; //This will store _left elements offset
                        var _right ={}; //This will store _right elements offset
                        var _error = (options.error == 'show') || false;
                        /*
                         options = {
                         fromNode - Left Element by ID - Mandatory
                         toNode - Right Element ID - Mandatory
                         status - accepted, rejected, modified, (none) - Optional
                         style - (dashed), solid, dotted - Optional	
                         horizontal_gap - (0), Horizontal Gap from original point
                         error - show, (hide) - To show error or not
                         width - (2) - Width of the line
                         }
                         */

                        if ($(options.fromNode).length > 0 && $(options.toNode).length > 0) {

                            //To decide colour of the line
                            if(!options.borderColor && !options.color) {
                                switch (options.status) {
                                    case 'success':
                                        _borderColor = '#d6e9c6';
                                        _color = '#dff0d8';
                                        break;
                                    case 'failed':
                                        _borderColor = '#ebccd1';
                                        _color = '#f2dede';
                                        break;
                                    case 'warning':
                                        _borderColor = '#fcf8e3';
                                        _color = '#fcf8e3';
                                        break;
                                    case 'none':
                                        _borderColor = '#D4D4D4';
                                        _color = '#D4D4D4';
                                        break;
                                    case 'darkBlue':
                                        _borderColor = '#0088cc';
                                        _color = '#777777';
                                        break;
                                    default:
                                        _borderColor = '#bce8f1';
                                        _color = '#d9edf7';
                                        break;
                                }
                            }
                            else{
                                _borderColor = options.borderColor;
                                _color = options.color
                            }


                            //To decide style of the line. dotted or solid
                            switch (options.style) {
                                case 'dashed':
                                    _dash = [4, 2];
                                    break;
                                case 'dotted':
                                    _dash = [4, 2];
                                    break;
                                default:
                                case 'solid':
                                    _dash = [0, 0];
                                    break;
                            }

                            //If fromNode is actually right side, following code will switch elements.
                            options.toNode.each(function(index, value) {
                                _fromNode = options.fromNode;
                                _toNode = $(value);
                                var isLeft = (_fromNode.offset().left <= _toNode.offset().left);
                                if (!options.side && !isLeft) {
                                    _tmp = _fromNode;
                                    _fromNode = _toNode;
                                    _toNode = _tmp;
                                }

                                //Get Left point and Right Point
                                var offX = state.canvas.parent().offset().left;
                                var offY = state.canvas.parent().offset().top;

                                if(options.side){
                                    _left.x = (_fromNode.offset().left + (_fromNode.outerWidth()/2)) - offX ;
                                    _left.y = (_fromNode.offset().top + _fromNode.outerHeight()) - offY;
                                    _right.x = (_toNode.offset().left + (_toNode.outerWidth()/2)) - offX;
                                    _right.y = _toNode.offset().top - offY - (options.pointer ? 8 : 0);
                                }
                                else {
                                    _left.x = (_fromNode.offset().left + _fromNode.outerWidth()) - offX;
                                    _left.y = (_fromNode.offset().top + (_fromNode.outerHeight() / 2)) - offY;
                                    _right.x = (_toNode.offset().left) - offX - (options.pointer ? 8 : 0);
                                    _right.y = (_toNode.offset().top + (_toNode.outerHeight() / 2)) - offY;
                                }

                                var _gap = options.horizontal_gap;

                                // lines must be moved after each draw or the line becomes distorted.
                                state.ctx.moveTo(_left.x, _left.y);
                                state.ctx.lineTo(_left.x + _gap, _left.y);
                                state.ctx.moveTo(_left.x + _gap, _left.y);
                                state.ctx.lineTo(_right.x - _gap, _right.y);
                                state.ctx.moveTo(_right.x - _gap, _right.y);
                                state.ctx.lineTo(_right.x, _right.y);

                                if (!state.ctx.setLineDash) {
                                    state.ctx.setLineDash = function() {}
                                } else {
                                    state.ctx.setLineDash(_dash);
                                }

                                state.ctx.lineWidth = options.darkLineWidth || 4;
                                state.ctx.strokeStyle = _borderColor;
                                state.ctx.stroke();

                                state.ctx.lineWidth = options.lightLineWidth || 2;
                                state.ctx.strokeStyle =  _color;
                                state.ctx.stroke();

                                if(options.dot){
                                    var dot = _left;
                                    if (!isLeft && !options.side){
                                        dot = _right;
                                    }
                                    state.ctx.fillStyle = _borderColor;
                                    var circle = new Path2D();
                                    circle.moveTo(dot.x, dot.y);
                                    circle.arc(dot.x, dot.y, 8, 0, 2 * Math.PI);
                                    state.ctx.fill(circle)
                                }
                                if(options.pointer){
                                    var pointer = _right;
                                    if(!isLeft && !options.side){
                                        pointer = _left;
                                    }
                                    state.ctx.fillStyle =  _borderColor;
                                    state.ctx.moveTo(pointer.x,pointer.y);

                                    if(options.side && options.side==='top'){
                                        state.ctx.lineTo(pointer.x - 8, pointer.y);
                                        state.ctx.lineTo(pointer.x, pointer.y-8);
                                        state.ctx.lineTo(pointer.x+8, pointer.y);
                                    }
                                    else if(options.side && options.side==='bottom'){
                                        state.ctx.lineTo(pointer.x - 8, pointer.y);
                                        state.ctx.lineTo(pointer.x, pointer.y+8);
                                        state.ctx.lineTo(pointer.x+8, pointer.y);
                                    }
                                    else{
                                        if(isLeft) {
                                            state.ctx.lineTo(pointer.x, pointer.y-8);
                                            state.ctx.lineTo(pointer.x + 8, pointer.y);
                                            state.ctx.lineTo(pointer.x, pointer.y+8);
                                        }
                                        else{
                                            state.ctx.lineTo(pointer.x, pointer.y-8);
                                            state.ctx.lineTo(pointer.x - 8, pointer.y);
                                            state.ctx.lineTo(pointer.x, pointer.y+8);
                                        }
                                    }
                                    state.ctx.lineTo(pointer.x,pointer.y);
                                    state.ctx.fill();
                                }

                            });
                        } else {
                            if (_error) alert('Mandatory Fields are missing or incorrect');
                        }
                    } catch (err) {
                        if (_error) alert('Mandatory Fields are missing or incorrect');
                    }
                }
            },
            exists: function(options){
                var exists = false;
                methods.applyId(options.fromNode, 'left');
                methods.applyId(options.toNode, 'right');
                $.each(state.edges, function () {
                    exists = (methods.getId(this.fromNode) === methods.getId(options.fromNode))
                    && (methods.getId(this.toNode) === methods.getId(options.toNode));
                    return !exists;
                });
                return exists;
            },
            getId: function(node){
                return node.attr('data-edgeId');
            },
            redraw: function(){
                try {
                    var temp = [];
                    state.ctx.clearRect(0, 0, state.canvas.width(), state.canvas.height());
                    $.each(state.edges, function () {
                        if($(this.fromNode).length>0 && $(this.toNode).length>0
                        && this.fromNode.is(':visible') && this.toNode.is(':visible')) {
                            methods.connect[this.type](this);
                            temp.push(this);
                        }
                    });
                    state.edges = temp;
                }
                catch(e){}
            },
            resize: function(options){
                if(options){
                    if(options.width){
                        state.canvas.attr('width', options.width);
                    }
                    if(options.height){
                        state.canvas.attr('height', options.height);
                    }
                }
                else {
                    state.canvas.attr('width', state.width()).attr('height', state.height() > 2000 ? state.height() : 2000);
                }
                methods.redraw();
            }
        };
        //public methods
        state.disconnect = function(options){
            var results = [];
            $.each(state.edges, function(){
                 if(this.fromNode !== options.fromNode && this.toNode !== options.toNode){
                     results.push(this);
                 }
            });
            state.edges = results;
            methods.redraw();
        };
        state.connect = function(options){
            var exists = methods.exists(options);
            if(!options.type){
                options.type='line';
            }
            if(!exists) {
                methods.connect[options.type](options);
                state.edges.push(options);
            }
            else{
                state.pageModal();
                state.pageModal('show', {
                    header: 'Create edge failed.',
                    body: 'The edge already exists.',
                    footer: null,
                    hasClose: true});
            }
            methods.redraw();
            return !exists;
        };

        state.size = function (){
             return {width: state.canvas.innerWidth(), height: state.canvas.innerHeight()}
        };

        state.resize = function (options){
            methods.resize(options);
        };

        state.updateOffset = function(options){
            state.opts.offset = options;
            methods.redraw();
        };

        state.refresh = function(){
            methods.redraw();
        };

        //environment: Initialize
        methods.init();
    };
    //Default Settings
    $.connector.defaults = {
        offset: {
            x: 0,
            y: 0
        }
    };                                                              //Plugin Function
    $.fn.connector = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.connector($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $connector = $(this).data('connector');
            switch (method) {
                case 'resize': $connector.resize(options);break;
                case 'size': return $connector.size();break;
                case 'disconnect': return $connector.disconnect(options);break;
                case 'connect': return $connector.connect(options);break;
                case 'refresh': $connector.refresh();break;
                case 'offset': $connector.updateOffset(options);break;
                case 'state':
                default: return $connector;
            }
        }
    };

})(jQuery);
