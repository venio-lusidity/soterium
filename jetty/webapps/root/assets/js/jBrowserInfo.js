;(function ($) {
    $.jBrowserInfo = {
        isKindOf: function (name) {
            var b = $.jBrowserInfo.get();
            return $.jCommon.string.contains(b.appVersion, name, true);
        },
        isChrome: function () {
            return $.jBrowserInfo.isKindOf("chrome");
        },
        isIE: function () {
            return $.jBrowserInfo.isKindOf("Microsoft Internet Explorer");
        },
        get: function () {
            r = {navigator: navigator};
            r.appVersion = navigator.appVersion;
            r.appAgent = navigator.userAgent;
            r.name = navigator.appName;
            r.appAgent = '' + parseFloat(navigator.appVersion);
            r.majVersion = parseInt(navigator.appVersion, 10);
            var nameOffset, verOffset, ix;

            // In Opera, the true version is after "Opera" or after "Version"
            if ((verOffset = r.appAgent.indexOf("Opera")) !== -1) {
                r.name = "Opera";
                r.appAgent = r.appAgent.substring(verOffset + 6);
                if ((verOffset = r.appAgent.indexOf("Version")) !== -1)
                    r.appAgent = r.appAgent.substring(verOffset + 8);
            }
            // In MSIE, the true version is after "MSIE" in userAgent
            else if ((verOffset = r.appAgent.indexOf("MSIE")) !== -1) {
                r.name = "Microsoft Internet Explorer";
                r.appAgent = r.appAgent.substring(verOffset + 5);
            }
            // In Chrome, the true version is after "Chrome"
            else if ((verOffset = r.appAgent.indexOf("Chrome")) !== -1) {
                r.name = "Chrome";
                r.appAgent = r.appAgent.substring(verOffset + 7);
            }
            // In Safari, the true version is after "Safari" or after "Version"
            else if ((verOffset = r.appAgent.indexOf("Safari")) !== -1) {
                r.name = "Safari";
                r.appAgent = r.appAgent.substring(verOffset + 7);
                if ((verOffset = r.appAgent.indexOf("Version")) !== -1)
                    r.appAgent = r.appAgent.substring(verOffset + 8);
            }
            // In Firefox, the true version is after "Firefox"
            else if ((verOffset = r.appAgent.indexOf("Firefox")) !== -1) {
                r.name = "Firefox";
                r.appAgent = r.appAgent.substring(verOffset + 8);
            }
            // In most other browsers, "name/version" is at the end of userAgent
            else if ((nameOffset = r.appAgent.lastIndexOf(' ') + 1) <
                (verOffset = r.appAgent.lastIndexOf('/'))) {
                r.name = r.appAgent.substring(nameOffset, verOffset);
                r.appAgent = r.appAgent.substring(verOffset + 1);
                if (r.name.toLowerCase() === r.name.toUpperCase()) {
                    r.name = navigator.appName;
                }
            }
            // trim the r.appAgent string at semicolon/space if present
            if ((ix = r.appAgent.indexOf(";")) !== -1)
                r.appAgent = r.appAgent.substring(0, ix);
            if ((ix = r.appAgent.indexOf(" ")) !== -1)
                r.appAgent = r.appAgent.substring(0, ix);

            r.majVersion = parseInt('' + r.appAgent, 10);
            if (isNaN(r.majVersion)) {
                r.appAgent = '' + parseFloat(navigator.appVersion);
                r.majVersion = parseInt(navigator.appVersion, 10);
            }
            return r;
        }
    };
})(jQuery);
