

;(function ($) {

    //Object Instance
    $.releaseNotes = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.releaseNotes.defaults, options);
        state.url = $.jCommon.url.create(window.location.href);
        state.current={
            start: 0,
            limit: 1000,
            id: null,
            selected: null,
            node: null
        };
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        var leftNode = $('.panel-left');
        var contentNode = $('.panel-middle');

        // Store a reference to the environment object
        el.data("releaseNotes", state);

        // Private environment methods
        methods = {
            init: function() {
               methods.html[state.opts.view].init();
            },
            url: function () {
                return '/notes?start=' + state.current.start + '&limit=' + state.current.limit;
            },
            get: function () {
                //state.panel.panel('busy');
                var s = function (data) {
                    methods.panel(data);
                    //state.panel.panel('busy');
                    methods.html[state.opts.view].create(data);
                };
                var f = function () {
                    state.panel.panel('notBusy');
                    content.html("No release notes found.");
                };
                $.htmlEngine.request(methods.url(), s, f, null, 'get');
            },
            panel: function (data) {
                state.panel = dCrt('div');
                var count = 0;
                state.append(state.panel);
                state.body = $.htmlEngine.panel(state.panel, 'glyphicons glyphicons-newspaper', 'Release Notes (' + data.results.length + ')',state.opts.url,false,[]);
            },
            html:{
                content: {
                    init: function () {
                        state.children().remove();
                        if($.jCommon.is.string(state.opts.data)) {
                            state.append(dCrt('h4').html(state.opts.data));
                        }
                        else if(state.opts.data){
                            var dt = $.jCommon.dateTime.defaultFormat(state.opts.data.releasedOn);
                            var img =  $.htmlEngine.glyph('glyphicons glyphicons-newspaper');
                            var content = $.htmlEngine.mediaObject({src: img, title: state.opts.data.title, subtext: dt});
                            state.append(content.css({margin: '10px 10px 20px 10px'}));
                            var details = dCrt('div');
                            content.append(details);

                            var bullets = state.opts.data['results'];
                            if(bullets){
                                var l = dCrt('ul');
                                $.each(bullets, function(){
                                   var b = dCrt('li').css({marginRight: '10px'});
                                   var item = this;
                                   var m =dCrt('p').html(item.label);
                                   b.append(m);
                                   if(item.content) {
                                       var sub = dCrt('ul');
                                       $.each(item.content, function(){
                                          var b1 = dCrt('li').css({marginRight: '10px'});
                                          var subItem = this;
                                          var m1 =dCrt('p').html(subItem.text);
                                          b1.append(m1);
                                          sub.append(b1);
                                       });
                                       b.append(sub);
                                   }
                                   l.append(b);
                                });
                                details.append(l);
                            }
                        }
                    }
                },
                widget: {
                    init: function () {
                        state.children().remove();
                        function getNotes() {
                            methods.get();
                        }
                        getNotes();
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
                            state.body.html("No release notes found.");
                        }
                    },
                    make: function (data) {
                        state.body.children().remove();
                        if(data.results.length>=3){
                            state.body.css({maxHeight: ''});
                        }
                        var count = 0;
                        var results = $.jCommon.array.sort(data.results, [{property: 'releasedOn', asc: false}]);
                        $.each(results, function () {
                            count++;
                            var item = this;
                            var n = dCrt('div').addClass('notification');
                            var g = dCrt('span').addClass('glyphicons glyphicons-newspaper').css({marginRight: '5px', position: 'relative', top: '-4px'});
                            var l;
                            var gl = dCrt('span');
                            l = dCrt('span').html(item.title);
                            gl.addClass('link').css({cursor: 'pointer', textDecoration: 'underline'});
                            gl.append(g).append(l);
                            n.append(gl);
                            var d = dCrt('span').html(" | " + $.jCommon.dateTime.defaultFormat(item.releasedOn));
                            n.append(d);
                            if(null !== state.current.id){
                                if($.jCommon.string.equals(item.title, state.current.id)){
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
                            state.body.append(n);
                        });
                    },
                    updateContent: function (node, item) {
                        state.body.find('.selected').removeClass('selected');
                        node.addClass('selected');
                        contentNode.releaseNotes({
                            url: null,
                            view: 'content',
                            fill: true,
                            data: item
                        });
                    }
                }
            }
        };
        //public methods
        // Initialize
        methods.init();
    };

    //Default Settings
    $.releaseNotes.defaults = {
        css: {minHeight: '50px', height: '50px'},
        delay: 60000
    };


    //Plugin Function
    $.fn.releaseNotes = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.releaseNotes($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var releaseNotes = $(this).data('releaseNotes');
            switch (method) {
                case 'exists': return (null!==$releaseNotes && undefined!==$releaseNotes && $releaseNotes.length>0);
                case 'state':
                default: return $releaseNotes;
            }
        }
    };

})(jQuery);
