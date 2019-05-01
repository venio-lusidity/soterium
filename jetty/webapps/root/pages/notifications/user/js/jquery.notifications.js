

;(function ($) {

    //Object Instance
    $.notifications = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.notifications.defaults, options);
        state.url = $.jCommon.url.create(window.location.href);
        state.current={
            start: 0,
            limit: 1000,
            id: state.url.getParameter('id'),
            selected: null,
            node: null
        };
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.isNotifiationPage = $.jCommon.string.contains(state.url.relativePath, 'notifications', true);
        var leftNode = $('.panel-left');
        var contentNode = $('.panel-middle');
        var menuBar = $('.menu-bar');

        // Store a reference to the environment object
        el.data("notifications", state);

        // Private environment methods
        methods = {
            init: function() {
               methods.html[state.opts.view].init();
                if(state.isNotifiationPage){
                }
            },
            url: function () {
                return '/notifications?start=' + state.current.start + '&limit=' + state.current.limit;
            },
            get: function () {
                //state.panel.panel('busy');
                var s = function (data) {
                    methods.panel(data);
                    //state.panel.panel('busy');
                    methods.html[state.opts.view].create(data);
                    methods.html.menu();
                };
                var f = function () {
                    state.panel.panel('notBusy');
                    content.html("No notifications found.");
                };
                $.htmlEngine.request(methods.url(), s, f, null, 'get');
            },
            panel: function (data) {
                state.panel = dCrt('div');
                var count = 0;
                state.append(state.panel);
                if(data.results.length > 0){
                    $.each(data.results, function () {
                       var item = this;
                        if(!item.read){
                            count++;
                        }
                    });
                }
                state.body = $.htmlEngine.panel(state.panel, 'glyphicons glyphicons-pending-notifications', 'Notifications (' + count + ')',state.opts.url,false,[]);
            },
            html:{
                content: {
                    init: function () {
                        state.children().remove();
                        if($.jCommon.is.string(state.opts.data)) {
                            state.append(dCrt('h4').html(state.opts.data));
                        }
                        else if(state.opts.data){
                            var dt = $.jCommon.dateTime.defaultFormat(state.opts.data.createdWhen);
                            var content = dCrt('div').css({marginLeft: '15px', paddingTop: '10px'});
                            state.append(content);
                            var img =  $.htmlEngine.glyph('glyphicons glyphicons-pending-notifications');
                            img.css({fontSize: '60px'});
                            var d = dCrt('span').css({fontSize: '20px', fontBold: true}).html(dt);
                            content.append(img).append(d);
                            var t = dCrt('div').append(dCrt('span').css({fontSize: 'large', fontBold: true}).html(state.opts.data.title));
                            content.append(t);
                            var details = dCrt('div');

                            var mData = $.jCommon.array.sort(state.opts.data['/system/primitives/raw_string/descriptions'].results, [{property: 'createdWhen', asc: false}]);
                            if(mData){
                                $.each(mData, function(){
                                   var item = this;
                                    var m =dCrt('span').html($.htmlEngine.urlify(item.value));
                                    details.append(m);
                                });
                            }
                            content.append(details);
                        }
                    }
                },
                widget: {
                    init: function () {
                        state.children().remove();
                        var sw;
                        function notify() {
                            methods.get();
                           // sw = new oStopWatch();
                           // sw.waitAsync(state.opts.delay, notify);
                        }
                        notify();
                    },
                    create: function (data) {
                        if(state.current.start===0){
                            state.body.children().remove();
                        }
                        if(state.opts.css) {
                            state.body.css(state.opts.css);
                        }
                        if(data && data.results && data.results.length>0){
                            methods.html.widget.make(data);
                        }
                        else if(state.current.start===0){
                            state.body.html("No notifications found.");
                        }
                    },
                    make: function (data) {
                        state.body.children().remove();
                        if(data.results.length>=3){
                            state.body.css({maxHeight: ''});
                        }
                        var count = 0;
                        data.results = $.jCommon.array.sort(data.results, [{property: 'createdWhen', asc: false}]);
                        $.each(data.results, function () {
                            count++;
                            var item = this;
                            if(state.opts.hideRead && item.read){
                                return true;
                            }
                            var n = dCrt('div').addClass('notification');
                            var g = dCrt('span').addClass('glyphicons glyphicons-bell').css({marginRight: '5px', position: 'relative', top: '-4px'});
                            var l;
                            //n.append(g);
                            var b = $.jCommon.bool.parse(item.read);
                            if(state.isNotifiationPage){
                                var gl = dCrt('span');
                                l = dCrt('span').html(item.title);
                                if(b){
                                    gl.addClass('link').css({cursor: 'pointer', textDecoration: 'underline', color: '#a9a9aa'});
                                }else{
                                    gl.addClass('link').css({cursor: 'pointer', textDecoration: 'underline'});
                                }
                                gl.append(g).append(l);
                                n.append(gl);
                                var d = dCrt('span').html(" | " + $.jCommon.dateTime.defaultFormat(item.createdWhen));
                                n.append(d);
                                if(null !== state.current.id){
                                   if($.jCommon.string.equals(item[state.KEY_ID], state.current.id)){
                                       methods.html.widget.updateContent(n, item);
                                   }
                                }
                                else{
                                    if(count ===1){
                                        state.current.selected = item;
                                        state.current.node = gl;
                                        methods.html.widget.updateContent(n, item);
                                    }
                                }
                                n.on('click', function () {
                                    state.current.node = gl;
                                    state.current.selected = item;
                                    methods.html.widget.updateContent(n, item);
                                });
                            }
                            else {
                                if(b) {
                                    n.addClass('link').css({cursor: 'pointer', textDecoration: 'underline', color: '#a9a9aa'});
                                    l = dLink(item.title, '/notifications?id='+item[state.KEY_ID]).css({color: '#a9a9aa'});
                                }
                                else{
                                    n.addClass('link').css({cursor: 'pointer', textDecoration: 'underline'});
                                    l = dLink(item.title, '/notifications?id='+item[state.KEY_ID]);
                                }
                                n.append(g).append(l);
                            }
                            state.body.append(n);
                        });
                    },
                    updateContent: function (node, item) {
                        state.body.find('.selected').removeClass('selected');
                        node.addClass('selected');
                        contentNode.notifications({
                            url: null,
                            view: 'content',
                            fill: true,
                            data: item
                        });
                    }
                },
                menu: function () {
                    $(document).unbind('menu-bar-read');
                    $(document).on('menu-bar-read', function () {
                        var l = state.current.selected;
                        var g = state.current.node;
                        var id = l.lid;
                        var r = l.read;
                        var message = dCrt('h4').css({color: 'blue', fontBold: true});
                        if (r) {
                            message.html("This message has already been marked as read.");
                            contentNode.append(message);
                        }
                        else{
                            message.html("This message is now marked as read.");
                            var s = function (data) {
                                if (data) {
                                    if ($.jCommon.bool.parse($.jCommon.json.getProperty(data, 'updated', 0))) {
                                        g.removeClass('link');
                                        g.addClass('link').css({cursor: 'pointer', textDecoration: 'underline', color: '#a9a9aa'});
                                        contentNode.append(message);
                                    }
                                }
                            };
                            var f = function () {
                            };
                            $.htmlEngine.request('/notifications', s, f, {id: id}, 'post');
                        }
                    });
                }
            }
        };
        //public methods
        // Initialize
        methods.init();
    };

    //Default Settings
    $.notifications.defaults = {
        hideRead: false,
        css: {minHeight: '50px', height: '50px'},
        delay: 60000
    };


    //Plugin Function
    $.fn.notifications = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.notifications($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $notifications = $(this).data('notifications');
            switch (method) {
                case 'exists': return (null!==$notifications && undefined!==$notifications && $notifications.length>0);
                case 'state':
                default: return $notifications;
            }
        }
    };

})(jQuery);
