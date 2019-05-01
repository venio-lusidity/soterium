

;(function ($) {

    //Object Instance
    $.osIcon = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.osIcon.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        var systems = {
            android: {title: 'Android', icon: 'icons8-android-os', keys: ['android']},
            apple: {title: 'Apple', icon: 'icons8-apple-filled', keys: ['apple', 'mac']},
            beos: {title: 'BeOS', icon: 'icons8-beos', keys: ['beos']},
            debian: {title: 'Debian', icon: 'icons8-debian', keys: ['debian']},
            dos: {title: 'DOS', icon: 'icons8-dos', keys: ['dos']},
            freebsd: {title: 'Free BSD', icon: 'icons8-free-bsd-filled', keys: ['free bsd', 'freebsd']},
            haiku: {title: 'Haiku', icon: 'icons8-haiku', keys: ['haiku']},
            linux: {title: 'Linux', icon: 'icons8-linux-filled', keys: ['linux'], weight: 1},
            mandriva: {title: 'Mandriva', icon: 'icons8-mandriva', keys: ['mandriva']},
            netscape: {title: 'Netscape', icon: 'icons8-netscape', keys: ['netscape']},
            os2: {title: 'OS2', icon: 'icons8-os2', keys: ['os2']},
            redhat: {title: 'Red Hat', icon: 'icons8-red-hat', keys: ['rhel', 'red hat', 'redhat']},
            sunos: {title: 'SUNOS', icon: 'icons8-unix', keys: ['sunos'], weight: 2},
            suse: {title: 'SUSE', icon: 'icons8-suse', keys: ['suse'], weight: 2},
            symbian: {title: 'Symbian', icon: 'icons8-symbian', keys: ['symbian']},
            ubuntu: {title: 'Ubuntu', icon: 'icons8-ubuntu', keys: ['ubuntu'], weight: 2},
            unix: {title: 'Unix', icon: 'icons8-unix', keys: ['aix', 'unix', 'hp-ux', 'sparc']},
            windowsxp: {title: 'Windows', icon: 'icons8-windows-xp', keys: ['windows xp'], weight: 2},
            windows: {title: 'Windows 8', icon: 'icons8-windows8', keys: ['windows'], weight: 1}
        };

        var _attempts = 0;

        // Store a reference to the environment object
        el.data("osIcon", state);

        // Private environment methods
        methods = {
            init: function() {
               // $.htmlEngine.loadFiles(state.worker.propertyNode, state.opts.name, state.opts.cssFiles);
                var key = '/properties/technology/software/operatingSystem';
                if(state.worker.data[key]){
                    state.worker.node.attr('data-valid', true).show();
                    var results = [];
                    results.push(state.worker.data[key]);
                    methods.html.init({results: results});
                }
                else {
                    function get() {
                        var url = (state.worker.data[state.KEY_ID] ? state.worker.data[state.KEY_ID] : state.worker.data.uri)  + key;
                        $.htmlEngine.request(url, s, f, null, 'get', true);
                    }
                    var s = function (data) {
                        state.worker.node.attr('data-valid', true).show();
                        methods.html.init(data);
                    };
                    var f = function () {
                        if (_attempts < 5) {
                            _attempts++;
                            window.setTimeout(get, 300);
                        }
                        else {
                            methods.html.init();
                        }
                    };
                    get();
                }
            },
            getOs: function (data) {
                var r;
                try {
                    if (data) {
                        var w = 0;
                        $.each(systems, function () {
                            var s = this;
                            var f = false;
                            $.each(s.keys, function () {
                                if ($.jCommon.string.contains(data.title, this, true)) {
                                    f = true;
                                    return false;
                                }
                            });
                            // using the weight of the system find a better match
                            if (f && (!s.weight || s.weight > w)) {
                                r = $.extend({}, s);
                                r.title = data.title;
                                w = s.weight;
                            }
                        })
                    }
                }catch(e){}
                if(!r){
                    r = {
                        title: (data && data.title) ? data.title : 'OS Unknown',
                        icon: 'glyphicon glyphicon-question-sign'
                    }
                }
                return r;
            },
            html:{
                init: function (data) {
                    var d = (data && data.results && data.results.length>0) ? data.results[0] : null;
                    var os = methods.getOs(d);
                    methods.html.content(d, os);
                },
                all: function () {
                    $.each(systems, function () {
                        methods.html.tile(null, this);
                    });
                    state.worker.node.children().css({display: 'inline-block'});
                },
                content: function (data, os) {
                    var stacked = $.jCommon.string.startsWith(state.opts.display, 'tile');
                    var c = dCrt('div').css({textAlign: stacked ? 'center' : 'left'});
                    if(state.opts.css){
                        c.css(state.opts.css);
                    }
                    var i = dCrt($.jCommon.string.startsWith(os.icon, "glyph") ? 'span' : 'i').css({fontSize: state.opts.fontSize, color: state.opts.color}).attr('title', os.title);
                    if($.jCommon.string.startsWith(os.icon, '&#')) {
                        i.attr('data-icons8', os.icon);
                    }
                    else{
                        i.addClass(os.icon);
                    }
                    i.addClass('os-icon');
                    c.append(i);
                    if(state.opts.hasTitle) {
                        if(!stacked){
                            c.append('&nbsp;')
                        }
                        var t = dCrt(stacked ? 'div' : 'span').html(os.title);
                        c.append(t);
                        if (data && state.opts.hasVersion) {
                            if(!stacked){
                                c.append('&nbsp;')
                            }
                            var v = $.jCommon.json.getProperty(data, '/technology/software_version/version.version');
                            if (v) {
                                c.append(dCrt('div').html(v));
                            }
                            if (data.cpe) {
                                //  c.append(dCrt('div').html(data.cpe).addClass('img-title'));
                            }
                        }
                    }
                    if(state.opts.linked){
                        var a = dCrt('a').attr('href', data[state.KEY_ID]).attr('target', '_blank').append(c);
                        if(state.opts.display === 'inline'){
                            a.css({display: 'inline-block'});
                        }
                        state.worker.node.append(a);
                    }
                    else {
                        if (state.opts.display === 'inline') {
                            c.css({display: 'inline-block'});
                        }
                        state.worker.node.append(c);
                    }
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.osIcon.defaults = {
        color: "#333333",
        display: "tile",
        fontSize: '32px',
        hasTitle: true,
        hasVersion: true
    };


    //Plugin Function
    $.fn.osIcon = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.osIcon($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $osIcon = $(this).data('osIcon');
            switch (method) {
                case 'exists': return (null!==$osIcon && undefined!==$osIcon && $osIcon.length>0);
                case 'state':
                default: return $osIcon;
            }
        }
    };

    $.osIcon.call= function(elem, options){
        elem.osIcon(options);
    };

    try {
        $.htmlEngine.plugins.register("osIcon", $.osIcon.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
