;(function ($) {

    /*http://www.jqueryscript.net/other/jQuery-Plugin-To-Connect-Two-Html-Elements-with-A-Line.html*/
    //Object Instance
    $.mapper = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);
        state.opts = $.extend({}, $.mapper.defaults, options);
        state.from = $(document.createElement("div")).addClass("record origin").css({left: '20px'});
        state.clear = $(document.createElement("div")).css({clear: 'both', height: '0'});
        state.edges = [];
        state.records = [];
        var _dragOptions = {
            cursor: "move",
            start: function(event, ui){
                $('.record').css({zIndex: 999500});
                $('.property-row').css({zIndex: 999500});
                ui.helper.css({zIndex: 999999});
                ui.helper.data('origin', ui.helper.position());
            },
            drag: function(event, ui){
                methods.refresh();
            },
            stop: function(){
                methods.refresh();
            }
        };

        var _rowDragOpt = $.extend(true, { cursor: "move", revert: true} , _dragOptions);

        // Store a reference to the environment object
        el.data("mapper", state);

        // Private environment methods
        methods = {
            init: function() {
                window.setTimeout(function(){
                    methods.load();
                }, 1000);
            },
            load: function(){
                state.connector({offset:{
                    x: -498,
                    y: -140
                }});

                state.append(state.from).append(state.clear);
                methods.createFrom();
                state.from.draggable(_dragOptions);
            },
            createFrom: function(){
                var title = $(document.createElement('div')).addClass("title");
                title.html(state.opts.data.fileName);
                state.from.append(title);
                state.from.attr('data-classname', state.opts.data.fileName);
                $.each(state.opts.data.properties, function(){
                    var item = $(document.createElement('div')).html(this).addClass("property-row origin-import");
                    item.attr('data-property', this);
                    item.attr('data-classname', state.opts.data.fileName);
                    item.draggable(_rowDragOpt);

                    item.mouseup(function(){
                        methods.refresh(item);
                    });

                    state.from.append(item);
                });
            },
            addClass: function(options) {
                options.properties = [];
                options.edges = [];
                var temp = {datatype: 'Text', classname: options.classname};
                var left = state.width()/2;

                var record = $(document.createElement("div")).addClass("record");
                record.css({left: left+'px'});

                var inner = $(document.createElement('div'));
                record.append(inner);

                var remove = $(document.createElement('a')).addClass('add').css({
                    margin: '0 5px 0 5px',
                    width: '20px',
                    'float': 'right',
                    color: '#ff0000'
                });

                var glyph = $(document.createElement('span')).addClass('glyphicon glyphicon-remove').attr('aria-hidden', 'true');
                remove.append(glyph);

                var title = $(document.createElement('div')).addClass("title origin-class");
                title.html(options.classname).attr('data-className', options.classname);
                title.append(remove);
                inner.append(title);

                remove.on('click', function(){
                    record.remove();
                    methods.refresh();
                    var results = [];
                    var exists = false;
                    var lc = options.classname.toLowerCase();
                    $.each(state.records, function(){
                        if(this.classname.toLowerCase() !== lc){
                         results.push(this);
                        }
                    });
                    state.records = results;
                    $.each(state.records, function(){
                        var connections = [];
                        $.each(this.edges, function(){
                            if(this.fromClass.toLowerCase() !== lc && this.toClass != lc){
                                connections.push(this);
                            }
                        });
                        this.edges = connections;
                    });
                });

                record.attr('data-classname', options.classname);

                record.insertBefore(state.from);

                state.records.push(options);

                var property = $(document.createElement('table')).addClass("property-row").css({width: '100%'});
                inner.append(property);

                var row = $(document.createElement("tr"));
                property.append(row);


                var td1 = $(document.createElement("td")).css({width: '155px'});
                var td2 = $(document.createElement("td"));
                var td3 = $(document.createElement("td")).css({width: '30px'});

                row.append(td1).append(td2).append(td3);

                var input = $(document.createElement('input'))
                    .addClass('form-control no-radius')
                    .css({marginRight: '5px'})
                    .attr('type', 'text')
                    .attr('placeholder', 'Property name?');
                td1.append(input);

                var select = $(document.createElement('select')).css({height: '34px'});
                td2.append(select);

                var selections = ['Text', 'Term', 'Boolean', 'Class', 'DateTime', 'Double', 'Enum', 'Float', 'Integer', 'Long', 'RawString'];
                $.each(selections, function(){
                    var option = $(document.createElement('option')).attr('value', this).html(this);
                    select.append(option);
                });

                var add = $(document.createElement('button')).attr('type', 'button')
                    .addClass('nothing no-radius').css({margin: '0 5px 0 5px', width: '20px', color: '#337AB7'});
                var glyph = $(document.createElement('span')).addClass('glyphicon glyphicon-plus').attr('aria-hidden', 'true');
                add.append(glyph);
                td3.append(add);

                add.on('click', function(){
                    var name = input.val();
                    if(undefined!=name && null!==name && name.length>0 && !methods.hasProperty(name, options)) {
                        temp.name = name;
                        temp.datatype = select.val();
                        methods.addProperty(property, temp, options);
                        input.val('').focus();
                    }
                });
                record.draggable(_dragOptions);
                methods.droppable(title, 'drop-class-hover', options);
                input.focus();
            },
            addProperty: function(node, temp, options){
                var exists = methods.hasProperty(temp.name, options);

                if(!exists) {
                    var item = $(document.createElement('table')).addClass("property-row origin-schema").css({height: '30px', width: '100%'});
                    item.insertBefore(node);

                    item.attr('data-classname', temp.classname);
                    item.attr('data-property', temp.name);
                    item.attr('data-datatype', temp.datatype);

                    var row = $(document.createElement("tr"));
                    item.append(row);

                    var td1 = $(document.createElement("td")).css({width: '155px'}).html(temp.name);
                    var td2 = $(document.createElement("td")).html(temp.datatype);
                    var td3 = $(document.createElement("td")).css({width: '30px'});
                    row.append(td1).append(td2).append(td3);

                    var remove = $(document.createElement('a')).addClass('add').css({
                        margin: '0 5px 0 5px',
                        width: '20px',
                        color: '#ff0000'
                    });
                    var glyph = $(document.createElement('span')).addClass('glyphicon glyphicon-minus').attr('aria-hidden', 'true');
                    remove.append(glyph);
                    td3.append(remove);

                    remove.on('click', function(){
                        var results = [];
                        $.each(options.properties, function(){
                            if(this.name.toLowerCase() !== temp.name.toLowerCase()){
                                results.push(this);
                            }
                        });
                        item.remove();

                        var lc = temp.name.toLowerCase();
                        $.each(state.records, function(){
                            var connections = [];
                            $.each(this.edges, function(){
                                if(this.fromProperty.toLowerCase() !== lc && this.toProperty != lc){
                                    connections.push(this);
                                }
                            });
                            this.edges = connections;
                        });
                        methods.refresh();
                        options.properties = results;
                    });

                    item.draggable(_rowDragOpt);
                    methods.droppable(item, 'drop-hover', options);

                    options.properties.push($.extend(true, {}, temp));
                }
                else{
                    state.pageModal();
                    state.pageModal('show', {
                        header: 'Create property failed.',
                        body: 'The property already exists.',
                        footer: null,
                        hasClose: true});
                }
            },
            canLink: function(from, to){
              var result = (from.hasClass("property-row"))
                  && from.data('classname')!==to.data('classname') && !from.hasClass('origin-class');

                if(from.hasClass('origin-import')){
                    result = to.hasClass('origin-schema');
                }
                else if(from.hasClass('origin-schema')){
                    result = to.hasClass('origin-schema') || to.hasClass('origin-class');
                }
                return result;
            },
            droppable: function(item, hoverClass, data){
                item.droppable({
                    hoverClass: hoverClass,
                    drop: function (event, ui) {
                        event.stopPropagation();
                        window.event.cancelBubble = true;
                        if (methods.canLink(ui.draggable,item)){
                            // rules????

                            var edge = {
                                fromClass: ui.draggable.data('classname'),
                                toClass: item.data('classname'),
                                fromProperty: ui.draggable.data('property'),
                                toProperty: ui.draggable.hasClass("title ui-droppable") ? item.data('classname') : item.data('property'),
                                dataType:  ui.draggable.hasClass("title ui-droppable") ? 'Class' : item.data('datatype')
                            };

                            window.setTimeout(function () {
                                var connected = state.connector('connect', {
                                    left_node: ui.draggable,
                                    right_node: item,
                                    horizontal_gap: 14,
                                    darkLineWidth: 4,
                                    lightLineWidth: 2,
                                    dot: true,
                                    pointer: true,
                                    error: true
                                });
                                if(connected){
                                    data.edges.push(edge);
                                }
                            }, 500);
                        }
                    }
                });
            },
            getRandomId: function(prefix){
                var rn = function(){return Math.floor(Math.random()*999999);};
                return prefix + '_' + rn() + '_' + rn();
            },
            hasProperty: function(property, data){
                var exists;
                $.each(data.properties, function(){
                    exists = (this.name.toLowerCase() === property.toLowerCase());
                    return !exists;
                });
                return exists;
            },
            refresh: function(node, last){
                if(null!=node) {
                    if(undefined==last || null==last){
                       last = {top: null, left: null};
                    }
                    if(node.length>0) {
                        if ((last.top != node.offset().top && last.left != node.offset().left)) {
                            methods.refresh();
                            last.top = node.offset().top;
                            last.left = node.offset().left;
                            window.setTimeout(function () {
                                methods.refresh(node, last);
                            }, 10);
                        }
                    }
                }
                else{
                    try {
                        state.connector('refresh');
                    }
                    catch (e) {
                    }
                }
            },
            exists: function(classname){
                var exists = false;
                $.each(state.records, function(){
                    exists = (this.classname == classname);
                    return !exists;
                });
                return exists;
            }
        };
        //public methods
        state.addClass = function(options){
            var exists = (undefined != options.classname && null!=options.classname);
            if(exists){
                exists = methods.exists(options.classname);
                if(!exists){
                    methods.addClass(options);
                }
                else{
                    state.pageModal();
                    state.pageModal('show', {
                        header: 'Create class failed.',
                        body: 'The class already exists.',
                        footer: null,
                        hasClose: true});
                }
            } 
        };

        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.mapper.defaults = {
        width: "100px",
        height: "200px"
    };


    //Plugin Function
    $.fn.mapper = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.mapper($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $mapper = $(this).data('mapper');
            switch (method) {
                case 'addClass': $mapper.addClass(options);break;
                case 'addProperty': $mapper.addProperty(options);break;
                case 'state':
                default: return $mapper;
            }
        }
    };

})(jQuery);
