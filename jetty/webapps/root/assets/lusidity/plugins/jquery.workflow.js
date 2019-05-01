

;(function ($) {

    //Object Instance
    $.workflow = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.workflow.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.GROUPS = ['ISSM'];
        state.authorized = {groups:["ISSM"]};
        state.opts.url = $.jCommon.url.create(window.location.href);
        state.WF_URL = state.opts.url.relativePath + "/workflows";


        // Store a reference to the environment object
        el.data("workflow", state);

        // Private environment methods
        methods = {
            init: function() {
                methods.load();
            },
            html: {
                panel: function (container, glyph, title, url, borders, actions, menu) {
                    var result = $(document.createElement('div'));
                    var options = {
                        glyph: glyph,
                        title: title,
                        url: url,
                        borders: borders,
                        content: result,
                        actions: actions ? actions : [],
                        menu: menu
                    };
                    container.panel(options);
                    return result;
                },
                formCancel: function () {
                    function cancel(node) {
                        if (node && node.formBuilder('exists')) {
                            node.formBuilder('cancel');
                        }
                    }
                    methods.html.tiles.setTileHeader();
                },
                table: function(container, data){
                    var content = $(document.createElement('table')).addClass('table table-hover');
                    container.append(content);
                    methods.html.getTableHead(['Identifier', 'submitted'], content);
                    var items = (data && data.results) ?  $.jCommon.array.sort(data.results, 'asc', ['title']) : [];
                    methods.html.getTableBody(['title', 'dt::createdWhen'], items, content, 0);
                },
                getTableHead: function(headers, container){
                    var thead = $(document.createElement('thead'));
                    var row = $(document.createElement('tr'));
                    $.each(headers, function(){
                        row.append($(document.createElement('th')).html(this));
                    });
                    thead.append(row);
                    container.append(thead);
                },
                getTableBody: function(properties, items, container, on, msg){
                    var tbody = $(document.createElement('tbody'));

                    var results = [];
                    function load(){
                        results = $.jCommon.array.sort(results, [{property: "title", asc: true}]);
                        $.each(results, function(){
                            methods.html.makeRow(tbody, properties, this)
                        });
                    }
                    $.each(items, function(){
                        var s = function(item){
                            results.push(item);
                        };
                        var url = '/vertices/'+this['/object/endpoint/endpointFrom'].relatedId.replace('#','');
                        $.htmlEngine.request(url, s, null, null, 'get');
                    });

                    function check(){
                        if(results.length==items.length){
                            load();
                        }
                        else{
                            window.setTimeout(check, 100);
                        }
                    }
                    container.append(tbody);
                    check();
                },
                makeRow: function(tbody, properties, item){
                    var row = $(document.createElement('tr')).addClass('table-row');
                    tbody.append(row);

                    $.each(properties, function(){
                        var key = this.toString();
                        var value;
                        var td = $(document.createElement('td')).addClass('css_' + key);
                        if($.jCommon.string.equals(key, 'del')){
                            td.css({width: '50px', maxWidth: '50px'});
                            value = $(document.createElement('span')).attr('title', 'Select to remove from ' + state.current.item.title).addClass('glyphicon glyphicon-remove').css({fontSize: '16px', cursor: 'pointer', color: 'red'});
                            value.on('click', function(){
                                var s = function(data){
                                    row.remove();
                                };
                                var f = function(){
                                    lusidity.info.red(msg);
                                    lusidity.info.show(5);
                                };
                                var url = methods.link.url(item);
                                var d = methods.link.data(item, state.current.item);
                                d['delete']= true;
                                $.htmlEngine.request(url, s, f, d, 'post');
                            });
                            if(item.status && item.status === "processing"){
                                value.hide();
                            }
                        }
                        else if($.jCommon.string.equals(key, 'tableLineNumber')){
                            on++;
                            td.css({width: '50px', maxWidth: '50px'});
                            value = $(document.createElement('span')).html(on);
                        }
                        else if(key=='deprecated'){
                            td.css({width: '100px', maxWidth: '100px'});
                            var cb = $(document.createElement('input')).attr('type', 'checkbox');
                            if(item[key]==='true'){
                                cb.attr('checked', 'checked');
                            }
                            value = $(document.createElement('span')).append(cb);
                        }
                        else if(key=="title"){
                            value = $(document.createElement('a')).html(item[key])
                                .attr('href', item[state.KEY_ID]).attr('target', '_blank');
                        }
                        else if($.jCommon.string.startsWith(key, 'dt::')){
                            var last = $.jCommon.string.getLast(key, '::');
                            var dt = new Date(item[last]);
                            var d = $.jCommon.dateTime.format(dt, 'j\\ M Y\\');
                            value = $(document.createElement('span')).html(d);
                        }
                        else{
                            value = $(document.createElement('span')).html(item[key]);
                        }
                        td.css({marginRight: '10px'}).append(value);
                        row.append(td);
                    });

                    row.addClass('selectable');

                    row.on('click', function(){
                        
                    });
                }
            },
            load: function () {
                var s = function(data){
                    methods.content.init(data);
                };
                $.htmlEngine.request(state.WF_URL, s, null, null, 'get', true);
            },
            content: {
                init: function(data) {
                    if(data && data.results){
                        state.worker.node.children().remove();
                        var container = $(document.createElement('div'));
                        state.worker.node.append(container);

                        $.each(data.results, function () {
                            var item = this;
                            var content = methods.html.panel(container, state.opts.glyph, item.title, null, false, null, null);

                            var d = $.jCommon.dateTime.format(item.createdWhen, 'j\\ M Y\\');
                            var dt = $(document.createElement('span')).html(d);
                            
                            
                            
                            // get the history
                        });
                    }
                }
            },
            link: {
                url: function (item, key) {
                    var id = item.uri ? item.uri : item[state.KEY_ID];
                    return id + '/properties' + key;
                },
                data: function (from, to) {
                    return {
                        from: from,
                        to: to
                    };
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.workflow.defaults = {
        glyph: "glyphicons glyphicons-flowchart"
    };


    //Plugin Function
    $.fn.workflow = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.workflow($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $workflow = $(this).data('workflow');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $workflow;
            }
        }
    };

    $.workflow.call= function(elem, options){
        elem.workflow(options);
    };

    try {
        $.htmlEngine.plugins.register("workflow", $.workflow.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
