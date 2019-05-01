; (function ($) {
    $.jCommon = {
        colors: {
            getRandomColor : function() {
                var letters = '0123456789ABCDEF';
                var color = '#';
                for (var i = 0; i < 6; i++) {
                    color += letters[Math.floor(Math.random() * 16)];
                }
                return color;
            },
            toHex: function (c) {
                var hex = c.toString(16);
                return hex.length === 1 ? "0" + hex : hex;
            },
            lighten: function (col, amt) {
                var usePound = true;
                if ( col[0] === "#" ) {
                    col = col.slice(1);
                    usePound = true;
                }
                var num = parseInt(col,16);

                function rng(val) {
                    if ( val > 255 ) val = 255;
                    else if  (val < 0) val = 0;
                    return val;
                }

                var r = (num >> 16) + amt;
                r = rng(r);

                var b = ((num >> 8) & 0x00FF) + amt;
                b = rng(b);

                var g = (num & 0x0000FF) + amt;
                g = rng(g);

                return (usePound?"#":"") + (g | (b << 8) | (r << 16)).toString(16);
            }
        },
        storage:{
            setItem: function(id, data, isParam){
                try{
                    var d = btoa(JSON.stringify(data));
                    if(isParam){
                       return d;
                    }
                    else {
                        data._last__ = Date.now();
                        localStorage.setItem(id, d);
                    }
                }
                catch(e){
                    if($.jCommon.string.contains(e, 'exceeded the quota')) {
                        $.jCommon.storage.evict(id, data);
                    }
                }
            },
            test: function () {
                var n = 1000;
                var w = "";
                for(var i=0;i<1000;i++){
                    w+="M";
                }
                var keys = [];
                for(var i=0;i<n;i++){
                    var id = $.jCommon.getRandomId("test_");
                    keys.push(id);
                    $.jCommon.storage.setItem(id, {test: w});
                }
                for(var i=0;i<keys.length;i++){
                    var key = keys[i];
                    var v = localStorage.getItem(key);
                    if(v){
                        console.log('stored: ' + v ? 'true' : 'false');
                    }
                }
            },
            evict: function (id, data) {
                try {
                    if (id && data) {
                        var len = localStorage.length;
                        var fKey = null;
                        var d1 = null;
                        for (var i = 0; i < len; i++) {
                            var key = localStorage.key(i);
                            var v = localStorage.getItem(key);
                            if (v) {
                                v = JSON.parse(atob(v));
                            }
                            if (!v) {
                                continue;
                            }
                            if (v && !v._last__) {
                                fKey = key;
                                break;
                            }
                            var d2 = new Date(v._last__);
                            var t1 = (null !== d1) ? d1.getTime() : null;
                            var t2 = d2.getTime();
                            if (null === d1) {
                                fKey = key;
                                d1 = d2;
                            }
                            else if (t2 < t1) {
                                fKey = key;
                                d1 = d2;
                            }
                        }
                        if (null !== fKey) {
                            localStorage.removeItem(key);
                            $.jCommon.storage.setItem(id, data, false);
                        }
                    }
                }
                catch (e){
                    console.log(e);
                }
            },
            getItem: function (id) {
                var r = localStorage.getItem(id);
                if(r){
                    r = JSON.parse(atob(r));
                    r._last__ = Date.now();
                }
                else{
                    r = JSON.parse(atob(id));
                    r._last__ = Date.now();
                }
                return r;
            }
        },
        array: {
            clone: function (obj) {
                if (!$.jCommon.is.empty(obj)) {
                    var target = new obj.constructor();
                    for (var key in target) { delete target[key]; }
                    return $.extend(true, target, obj);
                }
                return [];
            },
            contains: function(items, item, key)
            {
                var r;
                if(key){
                    r = false;
                    $.each(items, function () {
                        if($.jCommon.string.contains(key, ".")){
                            if($.jCommon.json.getProperty(this, key) === item){
                                r=true;
                                return false;
                            }
                        }
                        else if((this[key]===item[key])){
                            r=true;
                            return false;
                        }
                    });
                }
                else{
                    r = $.inArray(item, items) > -1
                }
                return r;
            },
            addAll: function(a, b){
                return a.concat(b);
            },
            insertAt: function (obj, item, idx) {
                var rs = [];
                if ($.jCommon.is.array(obj) && $.jCommon.is.object(item)) {
                    var on = 0;
                    $.each(obj, function () {
                        if (idx === on) {
                            rs.push(item);
                        }
                        rs.push(this);
                        on++;
                    });
                }
                return rs;
            },
            removeValue: function(from, value){
                var r = [];
                $.each(from, function () {
                   if(!$.jCommon.string.equals(this, value, true)){
                       r.push(this);
                   }
                });
                return r;
            },
            removeRange: function (from, to) {
                var rest = this.slice((to || from) + 1 || this.length);
                this.length = from < 0 ? this.length + from : from;
                return this.push.apply(this, rest);
            },
            remove: function(items, itemRemove, key){
                var rs = [];
                var expected = itemRemove[key];
                $.each(items, function(){
                    var actual = this[key];
                    if(!$.jCommon.string.equals(actual, expected)){
                        rs.push(this);
                    }
                });
                return rs;
            },
            count: function(array){
                 var r = 0;
                $.each(array, function() {
                   r ++;
                });
                return r;
            },
            sort: function (array, properties) {
                var sorter = {
                    asc: function (a, b, prop) {
                        var compareFrom = sorter.getValue(a, prop);
                        var compareTo = sorter.getValue(b, prop);
                        return $.jCommon.object.compare(compareFrom, compareTo);
                    },
                    desc: function (a, b, prop) {
                        return sorter.asc(a, b, prop) * -1;
                    },
                    getValue: function (obj, prop) {
                        try {
                            var p = prop.property;
                            if($.jCommon.json.hasProperty(obj, p)) {
                                var value = $.jCommon.json.getProperty(obj, p);
                                var num;
                                var date;
                                if ($.jCommon.string.contains(value, ":") || prop.type === 'date') {
                                    try {
                                        date = $.jCommon.dateTime.fromString(value)
                                    } catch (e) {
                                    }
                                }
                                else if ($.jCommon.is.numeric(value)) {
                                    try {
                                        num = parseInt(value);
                                    } catch (e) {
                                    }
                                }
                                return ($.jCommon.is.numeric(num) ? num : $.jCommon.is.date(date) ? date : !$.jCommon.string.empty(value) ? value.toString().toLowerCase() : null);
                            }
                            else{
                                return null;
                            }
                        }
                        catch (e){
                            return null;
                        }
                    }
                };

                // actual implementation
                var sort_by = function(properties) {
                    // final comparison function
                    return function(A, B) {
                        var result;
                        for (var i = 0; i < properties.length; i++) {
                            result = 0;
                            var prop = properties[i];
                            result = prop.asc ? sorter.asc(A, B, prop) : sorter.desc(A, B, prop);
                            if (result !== 0) break;
                        }
                        return result;
                    }
                };
                return array.sort(sort_by(properties));
            },
            sortKey: function (obj) {
                var r = {};
                var k = [];
                $.each(obj, function (key, value) {
                    k.push(key);
                });
                k = k.sort();
                $.each(k, function () {
                    r[this] = obj[this];
                });
                return r;
            }
        },
        copy: {
            createNode: function (text) {
                var node = document.createElement('pre');
                node.style.width = '1px';
                node.style.height = '1px';
                node.style.position = 'fixed';
                node.style.top = '5px';
                node.textContent = text.trim();
                return node;
            },
            copyNode: function (node) {
                var selection = getSelection();
                selection.removeAllRanges();

                var range = document.createRange();
                range.selectNodeContents(node);
                selection.addRange(range);

                var r = document.execCommand('copy');
                selection.removeAllRanges();
                return r;
            },
            copyText:  function (text) {
                var node = $.jCommon.copy.createNode(text);
                document.body.appendChild(node);
                var r = $.jCommon.copy.copyNode(node);
                document.body.removeChild(node);
                return r;
            },
            copyInput: function (node) {
                node.select();
                var r =document.execCommand('copy');
                getSelection().removeAllRanges();
                return r;
            }
        },
        dateTime: {
            fromString: function (dt) {
                var date;
                try {
                    date = dt.toString().replace('/Date(', '').replace(')/', '');
                    date = new Date(($.jCommon.is.numeric(date) ? parseInt(date) : date));
                }catch(e){
                    date = 'Invalid Date'
                }
                return ((date === 'Invalid Date') ? false : date);
            },
            format: function (dt, format) {
                if(dt && $.jCommon.string.startsWith(dt.toString(), 'pt', true)){
                    var ts = parseInt(dt.replace(/\D/g,''));
                    dt = new Date();
                    dt.setHours(0);
                    dt.setMinutes(0);
                    dt.setMilliseconds(0);
                    dt.setSeconds(ts);
                    var h = dt.getHours();
                    var m = ('0'+dt.getMinutes()).slice(-2);
                    var s = ('0'+dt.getSeconds()).slice(-2);
                    return h + ':' + m + ':' + s;
                }
                else{
                    var ndt = $.jCommon.is.date(dt) ? dt : $.jCommon.dateTime.fromString(dt);

                    function internalFormat(date) {
                        var returnStr = '';
                        var replace = Date.replaceChars;
                        for (var i = 0; i < format.length; i++) {
                            var curChar = format.charAt(i); if (i - 1 >= 0 && format.charAt(i - 1) === "\\") {
                                returnStr += curChar;
                            }
                            else if (replace[curChar]) {
                                returnStr += replace[curChar].call(date);
                            } else if (curChar !== "\\") {
                                returnStr += curChar;
                            }
                        }
                        return (((returnStr === undefined) || returnStr.toLowerCase().indexOf('nan')) > -1 ? false : returnStr);
                    }
                    return internalFormat(ndt);
                }
            },
            defaultFormat: function (dt) {
                var r;
                try{
                    r = $.jCommon.dateTime.format(dt, 'M j\\, Y\\, g\\:i a');
                }
                catch (e){
                    r = dt;
                }
                return r;
            },
            dateOnly: function (dt) {
                var r = $.jCommon.dateTime.defaultFormat(dt);
                if(r) {
                    r = r.split(",");
                    r = String.format('{0}, {1}', r[0], r[1]);
                }
                return r;
            },
            parse: function (dt) {
                if(!$.jCommon.is.date(dt)){
                    return new Date(dt);
                }
                return dt;
            },
            isToday: function(dt){
                var a = new Date(Date.now());
                var b = $.jCommon.dateTime.parse(dt);
                return (a.getDate()===b.getDate())&&(a.getMonth()===b.getMonth())&&(a.getFullYear()===b.getFullYear());
            },
            isAfter: function(dt){
                var a = new Date(Date.now());
                var b = $.jCommon.dateTime.parse(dt);
                return (!isNaN(b) && a>=b)
            },
            minutesToHours: function(min)
            {
                var r;
                try
                {
                    var t = parseInt(min);
                    var hours = Math.floor(t/60);
                    var minutes = (t % 60);
                    r = hours + ":" + minutes + " (" + min + " minutes)";
                }
                catch(e){ r = null;}

                return r;
            }
        },
        element: {
            contains: function(elem, txt)
            {
                return $.jCommon.string.contains($(elem).html(), txt, true);
            },
            getDimensions: function(elem){
                var test = elem.clone()
                    .css({position: 'absolute', top: '-10000', display: 'block', opacity: 1, visibility: 'visible'});
                $('body').prepend(test);
                var r = {
                    h: test.height(),
                    w: test.width()
                };
                test.remove();
                return r;
            },
            getParent: function (node, selectors) {
                var r;
                function check(n, selector) {
                    r = null;
                    if (n.is(selector)) {
                        r = n;
                    }
                    else {
                        if (n.parent()) {
                            r = check(n.parent(), selector);
                        }
                    }
                    return r;
                }
                if($.jCommon.is.array(selectors)){
                    var n = node;
                    $.each(selectors, function () {
                        var p = n.parent();
                        var t = this.toString();
                        var r = check(p, t);
                        if(r){
                            n = r;
                        }
                        else{
                            n = null;
                            return false;
                        }
                    });
                    var l = selectors[selectors.length-1];
                    if(n && n.is(l)){
                        r=n;
                    }
                }
                else{
                    r = g(node.parent(), selectors);
                }
                return r;
            },
            maxHeight: function (elem) {
                return Math.max(elem.scrollHeight, elem.offsetHeight, elem.clientHeight);
            },
            getAvailableWidth: function(elem)
            {
                var width = elem.width();
                var pl = elem.css('padding-left');
                var pr = elem.css('padding-right');
                if($.jCommon.string.contains(pl, 'px', true)){
                    pl = parseInt(pl.replace('px', ''));
                    if($.jCommon.is.numeric(pl)){
                        width-=pl;
                    }
                }
                if($.jCommon.string.contains(pr, 'px', true)){
                    pr = parseInt(pr.replace('px', ''));
                    if($.jCommon.is.numeric(pr)){
                        width-=pr;
                    }
                }
                return width;
            },
            isOpaque: function(elem){
                return (elem.css('opacity') && (parseInt(elem.css('opacity'))>0));
            },
            isVisible: function(elem){
                return (elem.css('visibility') && $.jCommon.string.equals(elem.css('visibility'), 'visible'));
            }
        },
        load:{
            css: function(cssUri, onLoaded){

                if($.jCommon.is.array(cssUri)){
                    $.each(cssUri, function(){
                        $.jCommon.load.css(this, onLoaded);
                    });
                }
                else {
                    var exists = false;
                    var link = $(document).find('link[href="' + cssUri + '"]');
                    if (link.length === 0) {
                        var head = $("head");
                        link = document.createElement("link");
                        $(head).append(link);
                        if ($.isFunction(onLoaded)) {
                            link.onload = onLoaded;
                        }
                        link.setAttribute("rel", "stylesheet");
                        link.setAttribute("type", "text/css");
                        link.setAttribute("href", cssUri.toString());
                    }
                    else if ($.isFunction(onLoaded)) {
                        onLoaded();
                    }
                }
            },
            script: function(scriptUri, onLoaded, appendFirst){
                if($.jCommon.is.array(scriptUri)){
                    $.each(scriptUri, function(){
                        $.jCommon.load.script(scriptUri, onLoaded);
                    });
                }
                else {
                    var script = $(document).find('script[src="' + scriptUri + '"]');
                    if (script.length === 0) {
                        var body = $("body");
                        script = document.createElement("script");
                        if(appendFirst){
                            $(body).append(script);
                        }
                        if ($.isFunction(onLoaded)) {
                            script.onload = function(){
                                window.setTimeout(function () {
                                    onLoaded();
                                }, 300);
                            };
                        }
                        script.setAttribute("type", "text/javascript");
                        script.setAttribute("src", scriptUri);
                        if(!appendFirst) {
                            $(body).append(script);
                        }
                    }
                    else if ($.isFunction(onLoaded)) {
                        window.setTimeout(function () {
                            onLoaded();
                        }, 300);
                    }
                }
            }
        },
        file: {
            getExtension: function (ext) {
                ext = ext.toLowerCase().split('?');
                ext = ext[0].split('/');
                ext = ext[ext.length - 1];
                ext = ext.split('.');
                if (ext !== null && ext.length > 1) {
                    ext = ext[ext.length - 1];
                }
                return ext;
            },
            getFileName: function (inputfile) {
                var path = $(inputfile).val();
                if (path === null || path.length === 0) {
                    return '';
                }
                var paths = path.split('\\');
                return paths[(paths.length - 1)];
            }
        },
        getRandomId: function(prefix){
            var rn = $.jCommon.getRandomNumber();
            var rn2 = $.jCommon.getRandomNumber();
            return prefix + '_' + rn + '_' + rn2;
        },
        getRandomNumber: function(){
            return Math.floor(Math.random()*999999);
        },
        image: {
            setClass: function (img, src, retries, loader) {
                var retry = $.jCommon.is.numeric(retries);
                if(loader){
                    img.attr('src', '/assets/img/loader/loader_blue_32.gif').show();
                }
                if(loader===undefined)
                {
                    loader=false;
                }
                var test = new Image();
                test.src = src;
                test.onload = function () {
                    if(loader){
                        img.attr('src', '').hide();
                    }
                    var isPortrait = (this.height > this.width);
                    img.addClass(isPortrait ? 'is-portrait' : 'is-landscape');
                    var e = jQuery.Event("imageLoaded");
                    e.isPortrait = isPortrait;
                    e.size = { width: this.width, height: this.height };
                    img.trigger(e);
                };
                test.onerror = function () {
                    if(retry && retries<60){
                        window.setTimeout(function(){
                            $.jCommon.image.setClass(img, src, retries+1, loader);
                        }, 2500);
                    }
                    else{
                        if(loader){
                            img.attr('src', '').hide();
                        }
                        var e = jQuery.Event("imageLoadError");
                        img.trigger(e);
                    }
                };
            }
        },
        hasValue: function(obj)
        {
            return ($.jCommon.is.object(obj) ||
                $.jCommon.is.array(obj) ||
                $.jCommon.is.date(obj) ||
                $.jCommon.is.bool(obj) ||
                $.jCommon.is.numeric(obj) ||
                $.jCommon.is.string(obj) ||
                $.jCommon.is.Function(obj) ||
                $.jCommon.is.url(obj) ||
                $.jCommon.is.guid(obj) ||
                $.jCommon.is.object(obj)) && (obj !== undefined);
        },
        object: {
            compare: function(compareFrom, compareTo){
                var r;
                var isStr = true;
                if(!$.jCommon.is.empty(compareFrom) && $.jCommon.is.empty(compareTo)){
                    r = 1;
                }
                else if(!$.jCommon.is.empty(compareTo) && $.jCommon.is.empty(compareFrom)){
                    r = -1;
                }
                else if($.jCommon.is.empty(compareTo) && $.jCommon.is.empty(compareFrom)){
                    r = 1;
                }
                else if($.jCommon.is.numeric(compareFrom) && !$.jCommon.is.date(compareFrom)){
                    compareFrom = parseInt(compareFrom);
                    isStr = false;
                }
                else if($.jCommon.is.numeric(compareTo) && !$.jCommon.is.date(compareTo)){
                    compareTo = parseInt(compareTo);
                    isStr = false;
                }
                else if($.jCommon.is.date(compareTo) || $.jCommon.is.date(compareFrom)){
                    isStr = false;
                }

                if(($.jCommon.is.numeric(compareTo) && isNaN(compareTo)) || compareTo === undefined || compareTo === null)
                {
                    r = 1;
                }
                else if(($.jCommon.is.numeric(compareTo) && isNaN(compareFrom)) || compareFrom === undefined || compareFrom === null)
                {
                    r = -1;
                }
                else if(($.jCommon.is.date(compareTo) && isNaN(compareTo)) || compareTo === undefined || compareTo === null)
                {
                    r = 1;
                }
                else if(($.jCommon.is.date(compareTo) && isNaN(compareFrom)) || compareFrom === undefined || compareFrom === null)
                {
                    r = -1;
                }

                if(!r) {
                    if((null===compareFrom) && (null===compareTo)){
                        r = 1;
                    }
                    else if ($.jCommon.is.date(compareFrom) && $.jCommon.is.date(compareTo)) {
                        isStr = false;
                        r = compareFrom.getTime() > compareTo.getTime() ? 1 : -1;
                    }
                    else {
                        if (isStr) {
                            if (compareFrom) {
                                compareFrom = compareFrom.toString().toLowerCase();
                            }
                            if (compareTo) {
                                compareTo = compareTo.toString().toLowerCase();
                            }
                        }

                        if (compareFrom === compareTo) {
                            r = 0;
                        }
                        else {
                            r = compareFrom > compareTo ? 1 : -1;
                        }
                    }
                }
                return r;
            }
        },
        is: {
            object: function (o) { return o !== undefined && window.Object.prototype.toString.call(o) === '[object Object]'; },
            array: function (o) { return o !== undefined && window.Object.prototype.toString.call(o) === '[object Array]'; },
            date: function (o) { return o !== undefined && window.Object.prototype.toString.call(o) === '[object Date]'; },
            bool: function (o) {return o !== undefined && window.Object.prototype.toString.call(o) === '[object Boolean]';},
            numeric: function (o) {
                return (null!==o) && (undefined !== o) && !isNaN(Number(o));
            },
            string: function (o) { return o !== undefined && window.Object.prototype.toString.call(o) === '[object String]'; },
            Function: function (o) { return o !== undefined && window.Object.prototype.toString.call(o) === '[object Function]'; },
            url: function (uri) {
                if ($.jCommon.is.empty(uri)) {
                    return false;
                }
                if (($.jCommon.string.startsWith(uri, '/') &&
                    !$.jCommon.string.startsWith(uri, '//'))) {
                    uri = window.location.origin + uri;
                }
                var isUrl = new RegExp('^(http|https)://.*?', 'gi');
                return isUrl.test(uri);
            },
            guid: function (guid) {
                var guidEmpty = '00000000-0000-0000-0000-000000000000';
                var expr = new RegExp('^[{|\\(]?[0-9a-fA-F]{8}[-]?([0-9a-fA-F]{4}[-]?){3}[0-9a-fA-F]{12}[\\)|}]?$', 'gi');
                return (!$.jCommon.is.empty(guid) && (guid !== guidEmpty) && expr.test(guid));
            },
            empty: function (o) {
                return ((null === o) || (o === undefined));
            },
            visible: function (elem) {
                return $(elem).is(':visible');
            },
            email: function (t) {
                var expr = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/gi;
                if ($.jCommon.is.empty(t)) {
                    return false;
                }
                return expr.test(t);
            }
        },
        json:{
            prettyPrint: function (obj) {
                return JSON.stringify(obj, undefined, 5);
            },
            pretty: function(obj)
            {
                var json = JSON.stringify(obj, undefined, 5);

                json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
                    var cls = 'number';
                    var isKey = false;
                    if (/^"/.test(match)) {
                        if (/:$/.test(match)) {
                            cls = 'key';
                            isKey = true;
                        } else {
                            cls = 'string';
                        }
                    } else if (/true|false/.test(match)) {
                        cls = 'boolean';
                    } else if (/null/.test(match)) {
                        cls = 'null';
                    }
                    var d = $(document.createElement('div'));
                    var s = $(document.createElement('span')).addClass(cls).html(match);
                    if(isKey) {
                        var key = $.jCommon.string.replaceAll(match, '"', "");
                        key = $.jCommon.string.replaceAll(key, ':', "");
                        s.attr('data-key', key);
                    }

                    d.append(s);
                    var r = d.html();
                    d.remove();
                    return r;
                });
            },
            getLength: function(obj)
            {
                if(!obj){
                    return 0;
                }
                var len = $.jCommon.is.array(obj) ? $(obj).length : 0;
                if($.jCommon.is.object(obj))
                {
                    len=0;
                    $.each(obj, function(key, value){
                        len++;
                    });
                }
                return len;
            },
            isEmpty: function (obj, path) {
                var len = $.jCommon.json.getLength(obj);
                if(len>0){
                    len = $.jCommon.json.getLength($.jCommon.json.getProperty(obj, path));
                }
                return (len===0);
            },
            hasProperty: function(obj, path, idx){
                if(!$.jCommon.is.numeric(idx)){
                    idx = 0;
                }
                var r = false;
                if($.jCommon.is.object(obj) && !$.jCommon.string.empty(path)){
                    var paths = path.split('.');
                    if(paths.length>0){
                        var data = $.extend({}, true, obj);
                        var on;
                        $.each(paths, function(){
                            if(data !== undefined) {
                                try {
                                    var value = data[this];
                                    if($.jCommon.is.array(value))
                                    {
                                        value = value[0];
                                    }

                                    r = $.jCommon.hasValue(value);
                                    if (r)
                                    {
                                        data = value;
                                    }
                                }
                                catch (e){
                                    r = false;
                                }
                            }
                            else{
                                r = false;
                            }
                            on++;
                            if(on<idx || !r)
                            {
                                return false;
                            }
                        });
                    }
                }
                return r;
            },
            getProperty: function(obj, path, dataType, idx){
                if(!path || path==='/' || path==='/root'){
                    return obj;
                }
                if(!idx){
                    idx=0;
                }
                var r = null;
                if($.jCommon.is.object(obj) && !$.jCommon.string.empty(path)){
                    try {
                        var paths = path.split('.');
                        if (paths.length > 0) {
                            r = $.extend({}, true, obj);
                            $.each(paths, function () {
                                if ($.jCommon.is.array(r) && r.length > 0) {
                                    r = r[0];
                                }
                                if ($.jCommon.hasValue(r)) {
                                    try {
                                        r = r[this];
                                    }
                                    catch (e) {
                                        r = null;
                                    }
                                }
                                else {
                                    r = null;
                                }
                                if (null === r) {
                                    return false;
                                }
                            });
                        }
                    }catch (e){
                        r = obj;
                    }
                }

                if(r !== undefined && r!==null && !$.jCommon.string.empty(dataType))
                {
                    switch (dataType){
                        case 'boolean':
                            r = (((r === true) || (r === 'true'))
                            || ((r === 1) || (r.toString() === '1')));
                            break;
                        case 'string':
                            r = r.toString();
                            break;
                        case 'dateTime':
                            r = (($.jCommon.is.string(r)) ? $.jCommon.dateTime.fromString(r) : r);
                            break;
                        case 'integer':
                        case 'number':
                            r = (($.jCommon.is.string(r)) ? parseInt(r) : r);
                            break;
                        default:
                            break;
                    }
                }
                return r;
            },
            sortKeys: function(map) {
                var keys = [];
                $.each(map, function(key, value){
                    keys.push(key);
                });
                keys = keys.sort();
                var newmap = {};
                $.each(keys, function(){
                    newmap[this] = map[this];
                });
                return newmap;
            },
            getSortedKeyArray: function(o){
                var keys = [];
                if(o) {
                    $.each(o, function (key, value) {
                        keys.push({key: key});
                    });
                }
                return o ? $.jCommon.array.sort(keys, true, ["key"]) : keys;
            },
            matches: function (expected, actual, inclusions, key1, key2) {

                if(!key1){
                    key1='';
                }
                if(!key2){
                    key2='';
                }
                // does not handle arrays as main object.
                if($.jCommon.is.array(expected)){
                    return false;
                }
                if($.jCommon.is.object(expected) && !$.jCommon.is.object(actual)){
                    return false;
                }

                var r = [];

                function add(ar1, ar2) {
                    $.each(ar2, function () {
                        ar1.push(this);
                    });
                }

                function audit(obj1, obj2, k1, k2) {
                    if($.jCommon.string.empty(k1)){
                        return false;
                    }
                    if($.jCommon.string.empty(k2)){
                        return false;
                    }

                    if(!$.jCommon.is.empty(obj1) && !$.jCommon.is.empty(obj2) && $.jCommon.string.equals(obj1.toString(), obj2.toString(), true)){
                        var i = {m1: obj1, m2: obj2, k1: k1, k2: k2};
                        r.push(i);
                    }
                }

                $.each(expected, function (key, value) {
                    // key = '/system/primitives/uri_value/identifiers'
                    // fix contains to be parted by the delimiter
                    var other = actual[key];
                    if($.jCommon.is.array(value)) {
                        $.each(value, function () {
                            var obj1 = this;
                            if ($.jCommon.is.array(other)) {
                                $.each(other, function () {
                                    var obj2 = this;
                                    var k = ($.jCommon.string.empty(key1) ? key : key1 + '.' + key);
                                    var v = $.jCommon.json.matches(obj1, obj2, null, k, k);
                                    add(r, v);
                                });
                            }
                            else if($.jCommon.is.object(other)) {
                                var k = ($.jCommon.string.empty(key1) ? key : key1 + '.' + key);
                                var v = $.jCommon.json.matches(obj1, value, null, k, k);
                                add(r, v);
                            }
                        });
                    }
                    else if($.jCommon.is.object(value)){
                        if ($.jCommon.is.array(other)) {
                            $.each(other, function (oK, oV) {
                                var obj2 = this;
                                var k = ($.jCommon.string.empty(key1) ? key : key1 + '.' + key);
                                var v = $.jCommon.json.matches(value, obj2, null, k, k + '.' + oK);
                                add(r, v);
                            });
                        }
                        else if($.jCommon.is.object(other)) {
                            var k = ($.jCommon.string.empty(key1) ? key : key1 + '.' + key);
                            var v = $.jCommon.json.matches(value, other, null, k, k + '.' + oK);
                            add(r, v);
                        }
                    }
                    else{
                        if(other){
                            var k1 = ($.jCommon.string.empty(key1) ? key : key1 + '.' + key);
                            var k2 = ($.jCommon.string.empty(key2) ? k1 : key2 + '.' + key);
                            audit(value, other, k1, k2);
                        }
                    }
                });

                return r;
            },
            filter: function(items, inclusions, exclusions){
                var r = items;

                function contains(obj1) {
                    var c = false;
                    $.each(inclusions, function () {
                        c = $.jCommon.string.equals(this, obj1, true);
                        return !c;
                    });
                    return c;
                }
                function excluded(obj1) {
                    var c = false;
                    if(!exclusions){
                        return false;
                    }
                    $.each(exclusions, function () {
                        c = $.jCommon.string.startsWith(this, obj1, true) || $.jCommon.string.equals(this, obj1, true);
                        return !c;
                    });
                    return c;
                }
                if($.jCommon.is.array(inclusions)) {
                    var t = [];
                    $.each(r,function(){
                        var i = this;
                        if((contains(i.k1) || contains(i.k2)) && (!excluded(i.k1) && !excluded(i.k2))) {
                            t.push(i);
                        }
                    });
                    r = t;
                }
                return r;
            },
            hightlightText: function (txt, hlt, cls) {
                var a = txt.toLowerCase();
                var b = hlt.toLowerCase();
                if(!$.jCommon.string.contains(a, b)){
                    return "The phrase was found within the property specified.";
                }
                var idx = a.indexOf(b);
                var aLen = txt.length;
                var bLen = hlt.length;
                var c = txt.substring(0, idx);
                var d = txt.substring(idx, (idx+bLen));
                var e = txt.substring((idx+bLen), aLen);
                c = (c) ? c : '';
                d = (d) ? d : '';
                e = (e) ? e : '';
                if(!cls){
                    cls = 'highlight';
                }
                return c + '<span class="' + cls + '">' + d + '</span>' + e;
            },
            hightlight: function(node, filters, cls, keyNum, exclusions){
                var pre = node;
                if(!pre.is('pre')){
                    pre = node.find('pre');
                }
                if(pre){
                    function excluded(obj1) {
                        var c = false;
                        if(!exclusions){
                            return false;
                        }
                        $.each(exclusions, function () {
                            c = $.jCommon.string.startsWith(obj1, this, true) || $.jCommon.string.equals(obj1, this, true);
                            return !c;
                        });
                        return c;
                    }
                    $.each(filters, function(){
                        var filter = this;
                        var key = filter['k' + keyNum];
                        var parts = key.split('.');
                        var fNodes = pre;
                        fNodes = fNodes.find('span[data-key="' +parts[(parts.length -1)]+ '"]');
                        if(fNodes.length > 0){
                            $.each(fNodes, function(){
                                var fNode = $(this).next();
                                if(fNode.length > 0) {
                                    var actual = fNode.html();
                                    actual = $.jCommon.string.stripStart(actual);
                                    actual = $.jCommon.string.stripEnd(actual);
                                    var expected = filter['m' + keyNum];
                                    if($.jCommon.string.equals(expected, actual, true) && !excluded(actual) && !excluded(expected)){
                                        fNode.addClass(cls);
                                    }
                                }
                            });

                        }
                    });

                }
            }
        },
        number: {
            fromPx: function(value){
              var r = 0;
              if(value){
                  r = $.jCommon.string.replaceAll(value, 'px', '');
                  r = $.jCommon.is.numeric(r) ? parseInt(r) : 0;
              }
              return r;
            },
            toFixed: function (value, places) {
                var v = value ? value.toString() : null;
                if(v && $.jCommon.string.contains(v, ".")) {
                    var f = $.jCommon.string.getFirst(v, ".");
                    var l = $.jCommon.string.getLast(v, ".");
                    v = String.format('{0}.{1}', f, l.length>places ? l.substring(0, places) : l);
                }
                return v;
            },
            parse: function (num) {
                var r = 0;
                if(num){
                    r = num.replace(/\D/g,'');
                    if(r){
                        r = parseInt(r);
                    }
                }
                return $.jCommon.is.numeric(r) ? r : 0;
            },
            commas: function(num){
                var r = num;
                if(num) {
                    var str = num.toString();
                    r = '';
                    var len = (str.length - 1);
                    var on = 0;
                    for (len; len >= 0; len--) {
                        if ((on % 3) === 0 && on > 0) {
                            r = "," + r;
                        }
                        r = str[len] + r;
                        on++;
                    }
                    r = $.jCommon.string.replaceAll(r, ",,", ",");
                }
                return r;
            },
            getConversion: function(format)
            {
                var conversion = [];
                conversion.push({ format: "mtf", rate: 3.28084 }); // meter to foot
                conversion.push({ format: "ftm", rate: 0.3048 }); // foot to meter
                var r = 0;
                $.each(conversion, function(){
                    if(this.format===format)
                    {
                        r = this.rate;
                        return false;
                    }
                });
                return r;
            },
            isEven: function (num) {
                return ((num%2)===0);
            },
            convert: function(value, format) {
                var r = value;
                try {
                    var conversion = $.jCommon.number.getConversion(format);
                    r = r*conversion;

                    switch (format)
                    {
                        case "mtf":
                            var parts = r.toString().split(".");
                            r = "";
                            var feet = parts[0];
                            if(!$.jCommon.string.empty(feet))
                            {
                                r = feet + "'";
                            }
                            if(parts.length>0) {
                                var inches = "." + parts[1];
                                inches = Math.round(inches*12);
                                r += (r.length>0 ? " ": "") + inches + '"';
                            }
                            break;
                        case "ftm":
                            break;
                    }
                }catch (e){}
                return r;
            },
            percentage: function(total, part, formated){
                var perc = (part/total).toFixed(2);
                var r = parseInt((perc*100).toFixed(0));
                if(formated){
                    r = String.format("{0}{1}%", ((r<=0) ? '<' : ''), r);
                }
                return r;
            }
        },
        stopWatch: function() {
            this.endTime = null;
            this.start = function () {
                this.startTime = new Date();
                this.endTime = null;
            };
            this.stop = function(){
                this.endTime = new Date();
            };
            this.getElapsed = function(){
                var now = new Date();
                return (this.startTime) ? ((this.endTime) ? this.endTime-this.startTime : now-this.startTime) : 0;
            };
            this.isLTE = function(millis){
                return this.getElapsed()<=millis;
            };
            this.isGTE = function(millis){
                return this.getElapsed()>=millis;
            };
        },
        string: {
            toCodex: function (s, key) {
                var result = s;
                if(result) {
                    var lex = "0lm2uvi3abg4hno5pcd6ef7jk8wx9yz0qrst";
                    var len = lex.length;
                    var x = result.length;
                    var next = (key > 25) ? 0 : key;
                    next = (next < 0) ? 0 : next;
                    for (var i = 0; i < x; i++) {
                        try {
                            var find = lex.substring(i, i + 1);
                            if (find) {
                                var replace = lex.substring(next, next + 1);
                                result = result.replaceAt(i, replace);
                                next++;
                                if (next >= len) {
                                    next = 0;
                                }
                            }
                        }
                        catch (e) {
                            console.log(e);
                        }
                    }
                }
                return result;
            },
            makeKey: function (s) {
                return ($.jCommon.string.empty(s)) ? '' : s.toLowerCase().replace(/[^a-z0-9+]+/gi, "_");
            },
            stripNonAlphaNumeric:function(s){
                return s ? s.replace(/[W_]+/g," ") : s;
            },
            stripStart: function (s) {
                return ($.jCommon.string.empty(s)) ? '' : s.substring(1);
            },
            stripEnd: function (s) {
                return ($.jCommon.string.empty(s)) ? '' : s.substring(0, s.length-1);
            },
            decode: function(s){
                var r = null;
                try{r= (!$.jCommon.string.empty(s) ? atob(s) : s);}catch (ignored){}
                return r;
            },
            encode: function(s){
                var r = null;
                try{r=!$.jCommon.string.empty(s) ? btoa(s) : s;}catch (ignored){}
                return r;
            },
            containsAny: function(s, items){
              var r = false;
              $.each(items, function () {
                 r = $.jCommon.string.contains(s, this, true);
                 return !r;
              });
              return r;
            },
            contains: function (s, t, i) {
                if ($.jCommon.is.array(t)) {
                    var r = false;
                    $.each(t, function () {
                        r = $.jCommon.string.contains(s, this, i ? i : false);
                        if (r) {
                            return false;
                        }
                    });
                    return r;
                }
                if (s === undefined || t === undefined) {
                    return false;
                }
                else if($.jCommon.string.startsWith(s,t,i) ||
                    $.jCommon.string.endsWith(s,t,i) ||
                    $.jCommon.string.equals(s,t,i)){
                    return true;
                }
                s = ' ' + s.toString() + ' ';
                t = t.toString();
                if (i) {
                    return (-1 !== (s.toLowerCase().indexOf(t.toLowerCase())));
                }
                else {
                    return (-1 !== s.toString().indexOf(t.toString()));
                }
            },
            equals: function (s, t, i) {
                if (s === undefined && t === undefined) {
                    return true;
                }
                else if(s === null && t === null){
                    return true;
                }
                else if (s === undefined || t === undefined || s===null || t===null) {
                    return false;
                }
                if ($.jCommon.is.array(t)) {
                    var r = false;
                    $.each(t, function () {
                        r = $.jCommon.string.equals(s, this, i ? i : false);
                        if (r) {
                            return false;
                        }
                    });
                    return r;
                }
                s = s.toString();
                t = t.toString();
                if (i) {
                    return (s.toLowerCase() === t.toLowerCase());
                }
                else {
                    return (s.toString() === t.toString());
                }
            },
            endsWith: function (s, t, i) {
                if (s === undefined || t === undefined) {
                    return false;
                }
                if ($.jCommon.is.array(t)) {
                    var r = false;
                    $.each(t, function () {
                        r = $.jCommon.string.endsWith(s, this, i ? i : false);
                        if (r) {
                            return false;
                        }
                    });
                    return r;
                }
                if (i === false) {
                    return (t.toString() === s.toString().substring(s.length - t.length));
                }
                else {
                    return (t.toLowerCase() === s.toString().substring(s.length - t.length).toLowerCase());
                }
            },
            ellipsis: function(text, maxChars, trimLeft){
                if(!text){
                    return '';
                }
                var r = '';
                if(text.length<=maxChars){
                    return text;
                }
                if(!$.jCommon.is.empty(text)){
                    text = text.toString();
                    var dif = (text.length - maxChars);
                    if(dif>0){
                        var i = trimLeft ? dif : 0;
                        var on = 0;
                        for(i;i<text.length;i++){
                            if(on<maxChars){
                                if(on+1===maxChars
                                    && !trimLeft
                                    && text.charAt(i) === (" " || "." || "!" || "?")){
                                    on++;
                                    continue;
                                }
                                r += text.charAt(i);
                            }
                            on++;
                        }
                        if($.jCommon.is.empty(r)){
                            r = text;
                        }
                        else if(trimLeft){
                            r = "..." + r;
                        }
                        else if(!$.jCommon.string.endsWith(r, "...")){
                            r+= "...";
                        }

                    }
                    return r;
                }
                return text;
            },
            empty: function (t) {
                return (t===undefined || t === null || t==="" || $.jCommon.is.empty(t) || t.length===0);
            },
            format: function (s) {
                var args = arguments;
                return s.replace(/\{(\d+)\}/g, function (m, n) { return args[n]; });
            },
            replaceAll: function(str, find, replace){
                var r = str;
                if(str) {
                    try {
                        r = str.replace(new RegExp(find, 'ig'), replace);
                    }
                    catch(e) {
                        r = str;
                    }
                }
                return r;
            },
            spaceIt: function (s) {
                return (s) ? s.replace(/([A-Z])/g, ' $1').trim() : s;
            },
            startsWith: function (s, t, i) {
                var r = false;
                if ($.jCommon.string.empty(s) || $.jCommon.string.empty(t)) {
                    return false;
                }
                s = s.toString();
                if ($.jCommon.is.array(t)) {
                    $.each(t, function () {
                        r = $.jCommon.string.startsWith(s, this, i ? i : false);
                        if (r) {
                            return false;
                        }
                    });
                }
                else if (i === false) {
                    r = (t.toString() === s.substring(0, t.length));
                } else {
                    r = (t.toLowerCase() === s.substring(0, t.length).toLowerCase());
                }
                return r;
            },
            toTitleCase: function(str){
                return !$.jCommon.string.empty(str) ? str.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();}) : str;
            },
            camelCase: function(text, spl, sep){
                var r="";
                try {
                    var array = text.split(spl);
                    $.each(array, function () {
                        var str = this.toString();
                        if (r.length > 0) {
                            r += sep;
                        }
                        r += str.charAt(0).toUpperCase();
                        r += str.slice(1).toLowerCase();
                    });
                }
                catch(e)
                {
                    r = text;
                }
                return r;
            },
            getFirst: function(text, sep){
                var r = null;
                try
                {
                    var array = text.split(sep);
                    $.each(array, function(){
                        r = this;
                        return $.jCommon.string.empty(r);
                    });
                }catch(e){}
                return r;
            },
            getLast: function(text, sep){
                var r = text;
                try
                {
                    if(sep) {
                        var array = text.split(sep);
                        r = array[array.length - 1];
                    }
                    else{
                        r = text.substring(text.length-1);
                    }
                }catch(e){}
                return r;
            },
            spaceAtCapitol: function (text) {
              var r= text;
              if(r){
                  r = r.replace( /([a-z])([A-Z])/g, "$1 $2");
              }
              return r;
            },
            toPhoneNumber: function (text) {
                var r = "";
                var len = text ? text.length : 0;
                if(len===10) {
                    for (var i = 0; i < len; i++) {
                        switch (i) {
                            case 3:
                                r += ".";
                                break;
                            case 6:
                                r += ".";
                                break;
                        }
                        r += text.charAt(i);
                    }
                }
                return r;
            },
            isNumber: function (txt) {
                var r;
                try{
                    r = parseInt(txt);
                    r = $.jCommon.is.numeric(r);
                }catch (e){}
                return r;
            }
        },
        bool:{
            parse: function(b){
                return (($.jCommon.is.bool(b) && b) || (($.jCommon.is.string(b)) && $.jCommon.string.equals(b,'true'))) ? true : false;
            }
        },
        url: {
            create: function (url) {
                var r = null;

                try
                {
                    if($.jCommon.string.empty(url))
                    {
                        throw "not valid";
                    }

                    var original = url.toString();
                    if (($.jCommon.string.startsWith(original, '/') &&
                        !$.jCommon.string.startsWith(original, '//'))) {
                        original = $.jCommon.url.getOrigin() + original;
                    }
                    if(!$.jCommon.is.url(original)){
                        return {
                            isUrl: false,
                            original: original
                        }
                    }
                    var s = ((!$.jCommon.string.startsWith(original, 'http')) ? 'http://' : '') + original;
                    s = s.split('/');
                    var protocol = s[0];
                    var host = s[2];
                    var hostNoPort = host.split(':')[0];
                    var port;
                    try {
                        port = host.split(':')[1];
                    } catch (e) {
                    }
                    var relativePath = '';
                    if (s.length > 3) {
                        for (var i = 3; i < s.length; i++) {
                            relativePath += '/' + s[i];
                        }
                    }
                    var parameters = null;
                    var paramsAsString = null;
                    if (original.indexOf('?') > -1) {
                        parameters = {};
                        paramsAsString = original.slice(original.indexOf('?') + 1);
                        var hashes = paramsAsString.split('&');
                        //noinspection JSDuplicatedDeclaration
                        for (var i = 0; i < hashes.length; i++) {
                            var h = hashes[i].split('=');
                            parameters[h[0]] = decodeURI(h[1]);
                        }
                    }

                    var hash = null;
                    if($.jCommon.string.contains(relativePath, "#", false)){
                        var parts = relativePath.split('#');
                        if(parts.length === 2) {
                            relativePath = parts[0];
                            hash = parts[1];
                        }
                    }

                    s = hostNoPort.split('.');
                    var domain = s[s.length - 2] + '.' + s[s.length - 1];

                    var favIcon = 'https://www.google.com/s2/favicons?domain=' + domain;
                    // 'https://getfavicon.appspot.com/' + protocol + "//" + domain;
                    r =  {
                        original: original,
                        protocol: protocol,
                        domain: domain,
                        isUrl: true,
                        host: host,
                        port: port,
                        hostNoPort: hostNoPort,
                        relativePath: relativePath,
                        favIcon: favIcon,
                        params: parameters,
                        paramsAsString: paramsAsString,
                        hash: hash,
                        isSecure: $.jCommon.string.startsWith(protocol, 'https:'),
                        getHostPath: function () {
                            return this.protocol + '//' + host;
                        },
                        hasParam: function (param) {
                            return $.jCommon.is.object(this.params) && !$.jCommon.is.empty(this.params[param]);
                        },
                        getParameter: function (param) {
                            return $.jCommon.is.object(this.params) ? this.params[param] : null;
                        }
                    };
                }
                catch(e)
                {
                    r =  {
                        original: url,
                        isUrl: false
                    };
                }
                return r;
            },
            getOrigin: function(){
                return window.location.protocol + '//' + window.location.hostname
                    + (!$.jCommon.string.empty(window.location.port) ? ':' + window.location.port : '');
            },
            decode: function (s) {
                var o = s; var binVal, t; var r = /(%[^%]{2})/;
                while ((m = r.exec(o)) !== null && m.length > 1 && m[1] !== '') {
                    b = parseInt(m[1].substr(1), 16);
                    t = String.fromCharCode(b); o = o.replace(m[1], t);
                } return o;
            },
            encode: function (c) {
                var o = ''; var x = 0; c = c.toString(); var r = /(^[a-zA-Z0-9_.]*)/;
                while (x < c.length) {
                    var m = r.exec(c.substr(x));
                    if (m !== null && m.length > 1 && m[1] !== '') {
                        o += m[1]; x += m[1].length;
                    } else {
                        if (c[x] === ' ') o += '+'; else {
                            var d = c.charCodeAt(x); var h = d.toString(16);
                            o += '%' + (h.length < 2 ? '0' : '') + h.toUpperCase();
                        } x++;
                    }
                } return o;
            }
        },
        size: {
            getBoxed: function(options){
                var gr = 1.618;
                function wide(width){
                    var height = (Math.round((width/16)*9));
                    return {w: width, h: height};
                }
            }
        },
        cookie: {
            create: function(name, value, minutes){
                if (minutes) {
                    var date = new Date();
                    date.setTime(date.getTime()+(minutes*60*1000));
                    var expires = "; expires="+date.toGMTString();
                }
                else var expires = "";
                document.cookie = name+"="+value+expires+"; path=/";
            },
            eraseAll: function(){
                var cookies = document.cookie.split(";");
                for (var i = 0; i < cookies.length; i++) {
                    $.jCommon.cookie.erase(cookies[i].split("=")[0]);
                }
            },
            erase: function(name){
                $.jCommon.cookie.create(name,"",-1);
            },
            read: function(name){
                var nameEQ = name + "=";
                var ca = document.cookie.split(';');
                for(var i=0;i < ca.length;i++) {
                    var c = ca[i];
                    while (c.charAt(0)===' ') c = c.substring(1,c.length);
                    if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length,c.length);
                }
                return null;
            }
        },
        isReady: function(){return true}
    };

    String.prototype.width = function(font) {
        var f = font || '12px arial',
            o = $('<div>' + this + '</div>')
                .css({'position': 'absolute', 'float': 'left', 'white-space': 'nowrap', 'visibility': 'hidden', 'font': f})
                .appendTo($('body')),
            w = o.width();

        o.remove();

        return w;
    };

    String.prototype.replaceAt=function(index, replacement) {
        return this.substr(0, index) + replacement+ this.substr(index + replacement.length);
    };

    String.prototype.replaceAll = function (token, newToken, ignoreCase) {
        var _token;
        var str = this + '';
        var i = -1;

        if (typeof token === 'string') {
            if (ignoreCase) {
                _token = token.toLowerCase();
                while ((
                    i = str.toLowerCase().indexOf(
                        token, i >= 0 ? i + newToken.length : 0
                    ) ) !== -1
                    ) {
                    str = str.substring(0, i) +
                        newToken +
                        str.substring(i + token.length);
                }

            } else {
                return this.split(token).join(newToken);
            }
        }
        return str;
    };
    Date.prototype.getJulian = function() {
        return Math.floor((this / 86400000) - (this.getTimezoneOffset()/1440) + 2440587.5);
    };
})(jQuery);

function Utils() {

}

Utils.prototype = {
    constructor: Utils,
    isElementInView: function (element, fullyInView) {
        var pageTop = $(window).scrollTop();
        var pageBottom = pageTop + $(window).height();
        var elementTop = $(element).offset().top;
        var elementBottom = elementTop + $(element).height();

        if (fullyInView === true) {
            return ((pageTop < elementTop) && (pageBottom > elementBottom));
        } else {
            return ((elementTop <= pageBottom) && (elementBottom >= pageTop));
        }
    }
};

jQuery.fn.extend({
    isEmpty: function () {
        return $(this).children().length===0;
    },
    availHeight: function (adjust) {
        var r;
        try{
            r =$(window).height()-this.offset().top-(($.jCommon.is.numeric(adjust)) ? adjust:0);
        }catch(e){}
        return !r ? 0 : r;
    },
    availWidth: function (adjust) {
        var r;
        try{
        r = $(window).width()-this.offset().left-(($.jCommon.is.numeric(adjust)) ? adjust:0);
        }catch(e){}
        return !r ? 0 : r;
    },
    isInViewport: function(el){
        //special bonus for those using jQuery
        if (typeof jQuery === "function" && el instanceof jQuery) {
            el = el[0];
        }

        var rect = el.getBoundingClientRect();

        return (
            rect.top >= 0 &&
            rect.left >= 0 &&
            rect.bottom <= (this.height() || window.innerHeight || document.documentElement.clientHeight) && /*or $(window).height() */
            rect.right <= (this.width() || window.innerWidth || document.documentElement.clientWidth) /*or $(window).width() */
        );
    },
    isInViewVertically: function(el){
        var node = this;
        if (typeof jQuery === "function" && el instanceof jQuery) {
            el = el[0];
        }
        if (typeof jQuery === "function" && node instanceof jQuery) {
            node = node[0];
        }
        var rect = el.getBoundingClientRect();
        var port = node.getBoundingClientRect();
        var top = port.top;
        var btm = port.bottom;
        return (
            rect.top >= top &&
            rect.bottom <= btm
        );
    },
    isInView: function (fullyInView, node) {
        var pageTop = (node) ? node.offset().top : $(window).scrollTop();
        var pageBottom =(node) ? (pageTop + node.height()) : (pageTop + $(window).height());
        var elementTop =  $(this).offset().top;
        var elementBottom = elementTop + $(this).height();
        var r;
        if (fullyInView === true) {
            r = ((pageTop < elementTop) && (pageBottom > elementBottom));
        } else {
            r  =((elementTop <= pageBottom) && (elementBottom >= pageTop));
        }
        return r;
    }
});

(function(window) {

    $type = String;
    $type.__typeName = 'String';
    $type.__class = true;

    $prototype = $type.prototype;
    $prototype.endsWith = function String$endsWith(suffix) {
        /// <summary>Determines whether the end of this instance matches the specified string.</summary>
        /// <param name="suffix" type="String">A string to compare to.</param>
        /// <returns type="Boolean">true if suffix matches the end of this instance; otherwise, false.</returns>
        return (this.substr(this.length - suffix.length) === suffix);
    };

    $prototype.startsWith = function String$startsWith(prefix) {
        /// <summary >Determines whether the beginning of this instance matches the specified string.</summary>
        /// <param name="prefix" type="String">The String to compare.</param>
        /// <returns type="Boolean">true if prefix matches the beginning of this string; otherwise, false.</returns>
        return (this.substr(0, prefix.length) === prefix);
    };

    $prototype.trim = function String$trim() {
        /// <summary >Removes all leading and trailing white-space characters from the current String object.</summary>
        /// <returns type="String">The string that remains after all white-space characters are removed from the start and end of the current String object.</returns>
        return this.replace(/^\s+|\s+$/g, '');
    };

    $prototype.trimEnd = function String$trimEnd() {
        /// <summary >Removes all trailing white spaces from the current String object.</summary>
        /// <returns type="String">The string that remains after all white-space characters are removed from the end of the current String object.</returns>
        return this.replace(/\s+$/, '');
    };

    $prototype.trimStart = function String$trimStart() {
        /// <summary >Removes all leading white spaces from the current String object.</summary>
        /// <returns type="String">The string that remains after all white-space characters are removed from the start of the current String object.</returns>
        return this.replace(/^\s+/, '');
    };

    $type.format = function String$format(format, args) {
        /// <summary>Replaces the format items in a specified String with the text equivalents of the values of   corresponding object instances. The invariant culture will be used to format dates and numbers.</summary>
        /// <param name="format" type="String">A format string.</param>
        /// <param name="args" parameterArray="true" mayBeNull="true">The objects to format.</param>
        /// <returns type="String">A copy of format in which the format items have been replaced by the   string equivalent of the corresponding instances of object arguments.</returns>
        return String._toFormattedString(false, arguments);
    };

    $type._toFormattedString = function String$_toFormattedString(useLocale, args) {
        var r = '';
        var format = args[0];

        for (var i = 0; ; ) {
            // Find the next opening or closing brace
            var open = format.indexOf('{', i);
            var close = format.indexOf('}', i);
            if ((open < 0) && (close < 0)) {
                // Not found: copy the end of the string and break
                r += format.slice(i);
                break;
            }
            if ((close > 0) && ((close < open) || (open < 0))) {

                if (format.charAt(close + 1) !== '}') {
                    throw new Error('format stringFormatBraceMismatch');
                }

                r += format.slice(i, close + 1);
                i = close + 2;
                continue;
            }

            // Copy the string before the brace
            r += format.slice(i, open);
            i = open + 1;

            // Check for double braces (which display as one and are not arguments)
            if (format.charAt(i) === '{') {
                r += '{';
                i++;
                continue;
            }

            if (close < 0) throw new Error('format stringFormatBraceMismatch');


            // Find the closing brace

            // Get the string between the braces, and split it around the ':' (if any)
            var brace = format.substring(i, close);
            var colonIndex = brace.indexOf(':');
            var argNumber = parseInt((colonIndex < 0) ? brace : brace.substring(0, colonIndex), 10) + 1;

            if (isNaN(argNumber)) throw new Error('format stringFormatInvalid');

            var argFormat = (colonIndex < 0) ? '' : brace.substring(colonIndex + 1);

            var arg = args[argNumber];
            if (typeof (arg) === "undefined" || arg === null) {
                arg = '';
            }

            // If it has a toFormattedString method, call it.  Otherwise, call toString()
            if (arg.toFormattedString) {
                r += arg.toFormattedString(argFormat);
            }
            else if (useLocale && arg.localeFormat) {
                r += arg.localeFormat(argFormat);
            }
            else if (arg.format) {
                r += arg.format(argFormat);
            }
            else
                r += arg.toString();

            i = close + 1;
        }

        return r;
    }

})(window);

Date.replaceChars = {
    shortMonths: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
    longMonths: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
    shortDays: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
    longDays: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],

    // Day
    d: function () { return (this.getDate() < 10 ? '0' : '') + this.getDate(); },
    D: function () { return Date.replaceChars.shortDays[this.getDay()]; },
    j: function () { return this.getDate(); },
    l: function () { return Date.replaceChars.longDays[this.getDay()]; },
    N: function () { return this.getDay() + 1; },
    S: function () { return (this.getDate() % 10 === 1 && this.getDate() !== 11 ? 'st' : (this.getDate() % 10 === 2 && this.getDate() !== 12 ? 'nd' : (this.getDate() % 10 === 3 && this.getDate() !== 13 ? 'rd' : 'th'))); },
    w: function () { return this.getDay(); },
    z: function () { var d = new Date(this.getFullYear(), 0, 1); return Math.ceil((this - d) / 86400000); }, // Fixed now
    // Week
    W: function () { var d = new Date(this.getFullYear(), 0, 1); return Math.ceil((((this - d) / 86400000) + d.getDay() + 1) / 7); }, // Fixed now
    // Month
    F: function () { return Date.replaceChars.longMonths[this.getMonth()]; },
    m: function () { return (this.getMonth() < 9 ? '0' : '') + (this.getMonth() + 1); },
    M: function () { return Date.replaceChars.shortMonths[this.getMonth()]; },
    n: function () { return this.getMonth() + 1; },
    t: function () { var d = new Date(); return new Date(d.getFullYear(), d.getMonth(), 0).getDate() }, // Fixed now, gets #days of date
    // Year
    L: function () { var year = this.getFullYear(); return (year % 400 === 0 || (year % 100 !== 0 && year % 4 === 0)); },   // Fixed now
    o: function () { var d = new Date(this.valueOf()); d.setDate(d.getDate() - ((this.getDay() + 6) % 7) + 3); return d.getFullYear(); }, //Fixed now
    Y: function () { return this.getFullYear(); },
    y: function () { return ('' + this.getFullYear()).substr(2); },
    // Time
    a: function () { return this.getHours() < 12 ? 'am' : 'pm'; },
    A: function () { return this.getHours() < 12 ? 'AM' : 'PM'; },
    B: function () { return Math.floor((((this.getUTCHours() + 1) % 24) + this.getUTCMinutes() / 60 + this.getUTCSeconds() / 3600) * 1000 / 24); }, // Fixed now
    g: function () { return this.getHours() % 12 || 12; },
    G: function () { return this.getHours(); },
    h: function () { return ((this.getHours() % 12 || 12) < 10 ? '0' : '') + (this.getHours() % 12 || 12); },
    H: function () { return (this.getHours() < 10 ? '0' : '') + this.getHours(); },
    i: function () { return (this.getMinutes() < 10 ? '0' : '') + this.getMinutes(); },
    s: function () { return (this.getSeconds() < 10 ? '0' : '') + this.getSeconds(); },
    u: function () {
        var m = this.getMilliseconds(); return (m < 10 ? '00' : (m < 100 ?
                '0' : '')) + m;
    },
    // Timezone
    e: function () { return "Not Yet Supported"; },
    I: function () { return "Not Yet Supported"; },
    O: function () { return (-this.getTimezoneOffset() < 0 ? '-' : '+') + (Math.abs(this.getTimezoneOffset() / 60) < 10 ? '0' : '') + (Math.abs(this.getTimezoneOffset() / 60)) + '00'; },
    P: function () { return (-this.getTimezoneOffset() < 0 ? '-' : '+') + (Math.abs(this.getTimezoneOffset() / 60) < 10 ? '0' : '') + (Math.abs(this.getTimezoneOffset() / 60)) + ':00'; }, // Fixed now
    T: function () { var m = this.getMonth(); this.setMonth(0); var result = this.toTimeString().replace(/^.+ \(?([^\)]+)\)?$/, '$1'); this.setMonth(m); return result; },
    Z: function () { return -this.getTimezoneOffset() * 60; },
    // Full Date/Time
    c: function () { return this.format("Y-m-d\\TH:i:sP"); }, // Fixed now
    r: function () { return this.toString(); },
    U: function () { return this.getTime() / 1000; }
};