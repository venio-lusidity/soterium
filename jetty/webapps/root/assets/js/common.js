var lusidity, pageCover;
var _init = false;
var elementTypes = {
    '/electronic/device/network/asset': 'A',
    '/electronic/device': 'D',
    '/electronic/system/enclave/network_enclave': 'NE',
    '/electronic/system/enclave/system_enclave': 'SE',
    '/electronic/system/enclave/virtual_enclave': 'VE'
};
var _asi = {
    msg: String.format('<div style="text-align:center;font-weight:bold;">{0}</div>', 'RMK will be conducting an ASI on [DateTime]-17:00.'),
    wmsg: String.format('<div style="text-align:center;font-weight:bold;">{0}</div>', 'RMK will be conducting an ASI on [DateTime]-17:00.'),
    delay: false,
    wait: 1000,
    web: false, /* For ASI set to false for web accessible set to true */
    groups: ['maintenance'], /* Groups authorized access when web is available.*/
    enabled: false, /*pmcs mode enabled?*/
    starts: '11/02/2018 14:00' /*when the asi starts format mm/dd/yyyy HH:mm*/
};
if($.jCommon.string.contains(_asi.msg, "[DateTime]")){

    var b = $.jCommon.dateTime.parse(_asi.starts);
    // 26 October 2018 from 1400
    console.log("figure out the proper format for date....");
    _asi.msg = _asi.msg.replace('[DateTime]', $.jCommon.dateTime.defaultFormat(b));
}
function rColors(cls) {
    var c = cls.toLowerCase().replaceAll(' ', '_');
    var r;
    switch (c){
        case 'cat_i':
        case 'critical':
        case 'veryhigh':
            r = '#d9534f';
            break;
        case 'high':
            r = '#f0ab36';
            break;
        case 'cat_ii':
        case 'medium':
            r = '#ecf028';
            break;
        case 'cat_iii':
        case 'low':
            r = '#49b26f';
            break;
        case 'verylow':
        case 'info':
            r = '#45a9e4';
            break;
    }
    return r;
}
function _chk(cb){
    if(_asi.enabled && _asi.starts && !$.jCommon.string.contains(window.location.href, '/notification?')){
        var std = false;
        var s = function (d) {
            if(d && d.auth) {
                if($.isFunction(cb)) {
                    cb();
                }
            }
            else{
                function chk() {
                    if($.jCommon.dateTime.isAfter(_asi.starts)){
                        window.location = "/notification?status=2&msg="+btoa(_asi.msg);
                    }
                    else{
                        if(!std) {
                            std=true;
                            cb();
                        }
                    }
                    if($.jCommon.dateTime.isToday(_asi.starts)){
                        window.setTimeout(chk, (1000*60));
                    }
                }
                chk();
            }
        };
        if(_asi.web){
            console.log('asi: web enabled');
            var grps = {failed: s, groups:_asi.groups};
            $.login.authorized(grps, s);
        }
        else{
            s({auth: false});
        }
    }
    else if($.isFunction(cb)) {
        cb();
    }
}

$(function() {

    if(lusidity){
        return false;
    }
    lusidity = $('.page-content');
    if(lusidity._started){
        return false;
    }
    lusidity._started = true;
    lusidity.jNodeReady({
        onReady: function () {
            if (!$.jBrowserInfo.isChrome() && !$.jCommon.string.contains(window.location.href, '/browser', true)) {
                window.location = '/browser';
            }
            var clsBan = dCrt('div').addClass('classification-banner');
            var clsBanIn = dCrt('div').html('UNCLASSIFIED//FOUO');
            $('.top-menu').append(clsBan.append(clsBanIn));
            var href = window.location.href.toString();
            lusidity.getSize = function () {
                this.size = {
                    h: $(window).height(),
                    w: $(window).width()
                };
                this.offset = {
                    h: 126,
                    w: 0
                };
                var h = this.size.h - this.offset.h;
                var w = this.size.w - this.offset.w;

                this.offsetSize = {
                    h: h,
                    w: w
                };
                return this;
            };
            lusidity.resizePage = function (os) {
                try {
                    var h = lusidity.getSize().offsetSize.h;
                    var ph = h + ($.jCommon.is.numeric(os) ? os : 0);
                    var ct = $('.main-content');
                    if(ct.length>0){
                        ct.css({minHeight: ph + 'px', height: ph + 'px'});
                    }
                    var pp = $('.page-panel');
                    if(pp.length>0) {
                        pp.css({minHeight: ph + 'px', height: ph + 'px'});
                    }
                    var tabable = $('.tabbable.sizable');
                    if (tabable.length > 0) {
                        tabable.css({minHeight: h - 42 + 'px'});
                        var tc = tabable.find('.tab-content');
                        if (tc.length > 0) {
                            var fh = (h-44);
                            dHeight(tc, fh, fh, fh);
                        }
                    }
                    var p = $('.panel-left');
                    var d =  $('.discover');
                    if (p.length>0 &&  d.length>0) {
                        d.width(p.width());
                    }
                }
                catch (e) {
                    console.log(e);
                }
            };
            lusidity.resizePage();
            pageCover = {
                busy: function (busy) {
                    if (busy) {
                        $.htmlEngine.busy(lusidity, {type: 'cube', cover: true});
                    }
                    else {
                        lusidity.loaders('hide');
                    }
                },
                hide: function () {
                    lusidity.loaders('hide');
                }
            };
            lusidity.uriKey = '/vertex/uri';

            lusidity.on('environmentReady', function () {
                lusidity.info = lusidity.environment('info');
                lusidity.environment('onResize', function () {
                    if(lusidity.resizeDisabled){
                        return false;
                    }
                    lusidity.resizePage();
                });
                $('#feedback').feedback({name: 'RMK'});

                function startUp(e) {
                    $('.main-content').find('.page-panel').show();
                    lusidity.lusidityUI(e);
                }

                $(document).on('error', function () {
                    startUp();
                });

                $(document).on('homePage', function () {
                    startUp();
                });

                function  ln() {
                    $(document).login({
                        loginUrl: '/pki/login',
                        authorizedUrl: '/authorization/authorized',
                        settings: $('#settings'),
                        welcome: $('#welcome'),
                        me: $('#me'),
                        logout: $('#logout'),
                        onAuthenticated: function (e) {
                            var ta = $('textarea');
                            if (ta.length > 0) {
                                ta.on('focus', function () {
                                    var txt = $(this).val();
                                    if (txt) {
                                        txt = txt.trim();
                                        $(this).val(txt);
                                    }
                                });
                            }
                            var mb = $('#msg_board');
                            if (mb.length > 0) {
                                mb.serverMessages();
                            }
                            startUp(e.auth);
                            var voiceEnabled = false;
                            var placeHolder = 'Looking for something?' + (voiceEnabled ? ' or say \'listen\'...' : '');
                            var opts = {
                                type: 'core',
                                enableLogging: true,
                                limit: 25,
                                keys: {
                                    'items': 'results',
                                    'text': 'title',
                                    'primaryType': 'vertexType',
                                    'image': '',
                                    'description': '',
                                    'uri': lusidity.uriKey,
                                    'favIcon': true
                                },
                                onEnter: function (text) {
                                    if ($.jCommon.string.contains(text, ":[")) {
                                        text = $.jCommon.string.getFirst(text, ":[");
                                        text = text.trim();
                                    }
                                    window.location = $.jCommon.url.getOrigin() + '/discover?q=' + text;
                                },
                                onKeyUp: function (text, container) {
                                    container.autoSuggest('autocomplete', {
                                        text: text,
                                        url: '/discover/suggest?phrase="' + encodeURI(text) + '"'
                                    });
                                },
                                input: {
                                    placeHolder: placeHolder,
                                    attributes: {
                                        title: 'Search',
                                        'x-webkit-speech': ''
                                    },
                                    style: {'text-align': 'left'}
                                },
                                button: {
                                    show: true,
                                    attributes: function () {
                                        console.log('Set the "button.attributes" option in order to set custom attributes.');
                                    },
                                    html: '<span class="glyphicon glyphicon-search" aria-hidden="true"></span>',
                                    onClick: function () {
                                        console.log('Set the "button.onClick" option in order to peform an action when the button is clicked.  ' +
                                            '(This can be the same function for the "onEnter" option)');
                                    }
                                },
                                listener: {
                                    enabled: false,
                                    recognition: recognition,
                                    elem: $('.speech-listening'),
                                    stop: $('.listen-stop'),
                                    stopBinded: false
                                }
                            };
                            $('.discover').autoSuggest(opts);
                            if (voiceEnabled) {
                                var recognition = ('webkitSpeechRecognition' in window) ? new webkitSpeechRecognition() : null;
                                if (null !== recognition) {
                                    recognition.continuous = true;
                                    recognition.interimResults = true;
                                }
                                lusidity.environment('isListening', {enabled: voiceEnabled});
                                recognition.onresult = function (event) {
                                    var found = false;
                                    if (event.results) {
                                        $.each(event.results, function () {
                                            $.each(this, function () {
                                                var data = this;
                                                if (null !== data && data.transcript) {
                                                    var text = data.transcript;
                                                    if ($.jCommon.string.contains(text, ['okay lusidity', 'okay lucidity', 'okay lucidity', 'ok lucidity', 'listen'])) {
                                                        found = true;
                                                    }
                                                    else if ($.jCommon.string.contains(text, 'go home')) {
                                                        window.location = lusidity.environment('home');
                                                        found = false;
                                                    }
                                                    else if ($.jCommon.string.contains(text, 'turn off')) {
                                                        lusidity.environment('listen', {enabled: false});
                                                        recognition.stop();
                                                    }
                                                    if (found) {
                                                        return false;
                                                    }
                                                }
                                                if (found) {
                                                    return false;
                                                }
                                            });
                                        });
                                    }
                                    if (found) {
                                        $('.discover').autoSuggest('listen', {});
                                    }
                                };
                                recognition.start();
                            }
                        }
                    });
                }
                _chk(ln);
            });
            debugger;
            var u = 'svc.soterium-dev.com';
            var server = {
                origin: window.location.protocol + '://' + window.location.hostname,
                primary:($.jCommon.string.contains(href, 'rmk.disa.mil', true) ? 'https://svc-1.rmk.disa.mil/svc' : 'https://' + u + ':8443/svc'),
                secondary: ($.jCommon.string.contains(href, 'rmk.disa.mil', true) ? 'https://svc-2.rmk.disa.mil/svc' : 'https://' + u + ':8443/svc'),
                download: ($.jCommon.string.contains(href, 'rmk.disa.mil', true) ? 'https://svc-1.rmk.disa.mil/svc' : 'https://' + u + ':8443/svc'),
                'delete':($.jCommon.string.contains(href, 'rmk.disa.mil', true) ? 'https://svc-2.rmk.disa.mil/svc' : 'https://' + u + ':8443/svc'),
                hosts:[]
            };
            if ($.jCommon.string.contains(href, "rmk.disa.mil")) {
                server.hosts.push({title: "Athena", url: 'https://svc-1.rmk.disa.mil/svc'});
                server.hosts.push({title: "Hercules", url: 'https://svc-2.rmk.disa.mil/svc'});
            }
            else{
                server.hosts.push({title: "Athena-1", url: 'https://' + u + ':8443/svc'});
            }
            lusidity.environment({
                mockData: false,
                serviceHostUri: server.primary,
                server: server
            });
        }
    });
});