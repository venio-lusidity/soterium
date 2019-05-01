;(function ($) {
    //Object Instance
    $.lusid = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.lusid.defaults, options);
        state.contentZone = $('.panel-middle');
        state.sideNav = $('.panel-left');
        state.url = $.jCommon.url.create(window.location.href);
        state.opts.initialized = false;
        state.opts.status = {
            state: null,
            prev: null
        };
        var status = {
            prev: null,
            state: null
        };

        // Store a reference to the environment object
        el.data("entity", state);

        var levelTypes = {
            summary: 'Summary',
            complete: 'Complete',
            state: 'State'
        };
        var modeTypes = {
            quick: 'Quick',
            async: 'Async',
            sync: 'Sync'
        };

        // Private environment methods
        methods = {
            init: function() {
                var url = state.opts.url.relativePath;
                var s = function(data) {
                    if(data._response_code){
                        var msg = btoa("<strong>Unauthorized:</strong> You do not have sufficient permissions to view the page requested.");
                        window.location = "/notification?status="+data._response_code + '&msg='+msg;
                    }
                    if (data) {
                        if(data.title) {
                            $("title").html(data.title);
                        }
                        state.opts.data = data;
                        methods.html.init();
                    }
                };
                var f = function (jqXHR, textStatus, errorThrown) {
                    console.log(jqXHR);
                    console.log(textStatus);
                    console.log(errorThrown);
                };
                $.htmlEngine.request(url, s, f, null, 'get');
            },
            gis: function (isClass) {
                return ($(window).innerWidth() < 560) ? (isClass ? 'medium' : 128) :
                    ($(window).innerWidth() < 840) ? (isClass ? 'medium' : 128) : (isClass ? 'large' : 256);
            },
            getStatus: function(){
                state.opts.isEnriched = false;
                function pull(){

                    var s =  function(data){
                        try{
                            state.opts.data = $.extend({}, state.opts.data, data);
                            methods.html.init();
                            sleep(2000);
                        }
                        catch(e){
                            sleep(2000);
                        }
                    };
                    var f =  function(jqXHR, textStatus, errorThrown){
                        sleep(2000);
                    };
                    $.htmlEngine.request(state.opts.url.relativePath, s, f, null, 'get');
                }
                var on=0;
                function check(){
                    if(!state.opts.initialized)
                    {
                        methods.init();
                    }
                }

                function sleep(mills){
                    window.setTimeout(function(){
                        $('.loading-indicator').hide();
                        //check();
                    }, mills);
                }
                if(state.opts.initialized)
                {
                    sleep(2000);
                }
                else
                {
                    check();
                }
            },
            resize: function(){

            },
            getDataForZone: function(schemas, data){
                var results = {};
                if(schemas){
                    $.each(schemas, function(){
                        if(this.source && data[this.property]){
                            results[this.property] = data[this.property];
                        }
                    });
                }
                if(!results.uri){
                    results.uri = state.opts.data.uri;
                }
                return results;
            },
            html: {
                init: function(){
                    if(state.url.hasParam('json') || state.url.hasParam('generate')){
                        $('div.page').remove();
                        $('body').css({'overflow': 'auto'});
                        var map = state.url.hasParam('json') ? $.jCommon.json.sortKeys(state.opts.data)
                            : $.schemaEngine.createEntityMap(state.opts.data);
                        var div = $(document.createElement("pre")).append($.jCommon.json.pretty(map));
                        $('body').append(div);
                    }
                    else
                    {
                        methods.html.zones();
                    }
                },
                edit: function(){
                    var container = $('.content');
                    var first = $(container.children()[0]);
                    var menu = $(document.createElement('div'));
                    menu.addClass('item-menu');
                    menu.insertAfter(first);

                    $('ul.login').bind('loggedOut', function(){
                        menu.remove();
                    });

                    function createMenuItem(icon, text){
                        var menuItem = $(document.createElement('div'));
                        menuItem.css({position: 'relative'}).addClass('inline buttonize ease');

                        var img = $(document.createElement('i'));
                        img.addClass(icon + ' inline');
                        menuItem.append(img);

                        var content = $(document.createElement('div'));
                        content.addClass('inline').css({marginLeft: '2px'}).html(text);
                        menuItem.append(content);
                        return menuItem;
                    }

                    var edit = createMenuItem('icon-pencil', 'edit');
                    menu.append(edit);
                    var view = createMenuItem('icon-file', 'view');
                    view.hide();
                    menu.append(view);

                    edit.bind('click', function(){
                        edit.hide();
                        view.show();
                        var edits = $('.edit');
                        $.each(edits, function(){
                            var id = $(this).attr('data-id');
                            var node = $('#' + id);
                            if(node.length>0){
                                $(this).show();
                                node.hide();
                            }
                        });
                    });
                    view.bind('click', function(){
                        edit.show();
                        view.hide();
                        var edits = $('.edit');
                        $.each(edits, function(){
                            var id = $(this).attr('data-id');
                            var node = $('#' + id);
                            if(node.length>0){
                                $(this).hide();
                                node.show();
                            }
                        });
                    });
                },
                zones: function(){
                    state.schemaEngine(state.opts.data);
                    var schema = state.schemaEngine('state').opts.schema;

                    if(state.opts.initialized && state.opts.schema){
                        state.opts.schema = $.extend({}, schema, state.opts.schema);
                    }
                    else{
                        state.opts.schema = schema;
                    }

                    if($.jCommon.json.hasProperty(state, 'opts.schema.zones')){
                        $.each(state.opts.schema.zones, function(){
                            var zoneSchema = this;
                            var zone = $(zoneSchema.zone);
                            if($(zone).length===1){
                                if(!state.opts.initialized){
                                    if(zoneSchema.cls){
                                        $("body").addClass(zoneSchema.cls);
                                    }
                                    zone.htmlEngine({ schema: zoneSchema, data: state.opts.data, autoHide: true, isResults: false });
                                }
                                else{
                                    zone.htmlEngine('update', { schema: zoneSchema, data: state.opts.data, autoHide: false, isResults: false});
                                }
                            }
                        });
                    }
                    else{
                        console.log("schema missing zones property.");
                    }
                }
            }
        };
        methods.getStatus();
    };

    //Default Settings
    $.lusid.defaults = {
        data: null,
        debug : false,
        initialized : false,
        isPartial: true,
        lastUpdated : new Date(),
        url : $.jCommon.url.create(window.location.href)
    };


    //Plugin Function
    //noinspection JSUnusedLocalSymbols
    $.fn.lusid = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return new $.lusid($(this),method);
        } else {
            // Helper strings to quickly perform functions
            var $lusid = $(this).data('lusid');
            switch (method) {
                case 'state':
                default: return $lusid; break;
            }
        }
    }

})(jQuery);