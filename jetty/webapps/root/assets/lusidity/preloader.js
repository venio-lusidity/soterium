{
    var d = true;
    var v = d ? ((new Date()) - (new Date(1970, 1, 1))) : '030820181808';
    var appVersion = '?v=' + v;
    var head = document.getElementsByTagName('head')[0];
    function make(filename) {
        var s = document.createElement('script');
        s.setAttribute('type', 'text/javascript');
        s.setAttribute('src', filename + appVersion);
        head.appendChild(s);
    }
    make("/assets/lusidity/page.loader.js");
    function check() {
        try {
            pageLoader._default();
        }
        catch (e) {
            window.setTimeout(check, 100);
        }
    }
    window.setTimeout(check, 100);
}