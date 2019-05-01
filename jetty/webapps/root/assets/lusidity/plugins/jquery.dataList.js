;(function ($) {

    //Object Instance
    $.dataList = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.dataList.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.on = 0;

        // Store a reference to the environment object
        el.data("dataList", state);

        // Private environment methods
        methods = {
            init: function() {
                state.worker.node.attr('data-valid', true);
                state.worker.node.jNodeReady({onVisible: function () {
                    state.body = $.htmlEngine.panel(
                        state.worker.node, state.opts.glyph, state.opts.title, state.opts.url ? state.opts.url : null /* url */, false /* borders */
                    );
                    methods.adjustHeight();
                    if (!state.opts.hasHeader) {
                        state.worker.node.panel('hideHeader');
                    }
                    if (state.opts.body) {
                        if (state.opts.body.attr) {
                            $.htmlEngine.addAttributes(state.opts.body, state.body);
                        }
                        if (state.opts.body.css) {
                            $.htmlEngine.addStyling(state.opts.body, state.body);
                        }
                        if (state.opts.body.cls) {
                            state.body.addClass(state.opts.body.cls);
                        }
                    }
                    var _init = false;
                    var s = function (data) {
                        if(state.ldr) {
                            state.ldr.loaders('hide');
                        }
                        if (data && data.results) {
                            state.start +=  data.results.length;
                            state.next = data.next;
                            state.hits = $.jCommon.is.numeric(data.hits) ? data.hits : state.start;
                            state.excluded = $.jCommon.is.numeric(data.excluded) ? data.excluded : 0;
                            if(state.opts.showExcluded){
                                state.worker.node.panel('updateHeader', {
                                    glyph: state.opts.glyph,
                                    title: String.format("found: {0} excluded: {1}", state.hits, state.excluded)
                                });
                            }
                            else if (state.opts.hasItemCounter) {
                                state.worker.node.panel('updateHeader', {
                                    glyph: state.opts.glyph,
                                    title: String.format("{0} {1}/{2}", state.opts.title, ((state.start > state.hits) ? state.hits : state.start), state.hits)
                                });
                            }
                            methods.html.init(data);
                            state.body.scrollHandler('start');
                        }
                        else{
                            if(state.opts.missing) {
                                state.worker.node.panel('updateHeader', {
                                    glyph: state.opts.glyph,
                                    title: String.format("{0}", state.opts.title)
                                });
                                state.worker.node.attr('data-valid', true).show();
                                methods.html.putMissing();
                            }
                        }
                        if(!_init){
                            state.body.jNodeReady({onReady: function () {
                                state.css({overflowX: 'hidden', overflowY: 'hidden'});
                                methods.resize();
                                state.body.scrollHandler({
                                    adjust: 10,
                                    start: function () {
                                    },
                                    stop: function () {
                                    },
                                    top: function () {
                                    },
                                    bottom: function () {
                                        if (state.start < state.hits) {
                                            page();
                                        }
                                    }
                                });
                                _init = true;
                            }});
                        }
                    };
                    var f = function () {
                        if(state.ldr) {
                            state.ldr.loaders('hide');
                        }
                    };

                    function page() {
                        if(state.opts.loaderId) {
                            if(!state.ldr) {
                                state.ldr = $('#' + state.opts.loaderId);
                            }

                            $.htmlEngine.busy(state.ldr, {cls: 'floater-left', type: 'cylon', top: '2px', left: '0', pre: 'true'});
                        }
                        if (!state.start) {
                            state.start = 0;
                        }
                        if (!state.hits || state.start < state.hits) {
                            if (state.opts.query && state.opts.query.fn) {
                                var fn = QueryFactory[state.opts.query.fn];
                                var q = fn(state.worker.data);
                                if ($.jCommon.is.string(q)) {
                                    var url = methods.getUrl(q, ($.jCommon.is.numeric(state.next) ? state.next : state.start), state.opts.limit);
                                    $.htmlEngine.request(url, s, f, null, 'get');
                                }
                                else {
                                    $.htmlEngine.request(methods.getQueryUrl(state.start, state.opts.limit), s, f, q, 'post');
                                }
                            }
                            else {
                                var url = state.worker.data[state.KEY_ID] + '/properties' + state.opts.property;

                                var prm = state.opts.direction? 'direction=' + state.opts.direction: '';
                                if (state.opts.limit) {
                                    prm += (prm.length>0 ? '&': '') +'start=' + state.start + '&limit=' + state.opts.limit;
                                }
                                url += (prm.length>0 ? '?' + prm: '');
                                var r = state.worker.data[state.opts.property];
                                if (r) {
                                    var data = {results: []};
                                    if (r.results) {
                                        data.results = r.results
                                    }
                                    else if ($.jCommon.is.array(r)) {
                                        data.results = r;
                                    }
                                    else {
                                        data.results.push(r);
                                    }
                                    s(data);
                                }
                                else {
                                    $.htmlEngine.request(url, s, f, null, 'get');
                                }
                            }
                        }
                        else {
                            if(state.ldr) {
                                state.ldr.loaders('hide');
                            }
                        }
                    }
                    page();
                }});
            },
            adjustHeight: function () {
                if (state.opts.adjustHeight || state.opts.fill) {
                    $.htmlEngine.adjustHeight(state, state.opts.fill, state.opts.adjustHeight, false);
                }
            },
            getUrl: function (url, start, limit) {
                if (undefined === start) {
                    start = 0;
                }
                if (undefined === limit) {
                    limit = 0;
                }
                return url + ($.jCommon.string.contains(url, "?") ? '&' : '?') + 'start=' + start + '&limit=' + limit;
            },
            getQueryUrl: function (start, limit) {
               return methods.getUrl('/query', start, limit);
            },
            resize: function () {
                if(!state.opts.fill){
                    return false;
                }
                var h = state.body.availHeight();
                h = state.body.availHeight(state.opts.adjustBodyHeight);
                state.worker.node.panel('resize');
                state.body.css({height: h + 'px', maxHeight: h+'px', minHeight: h+'px', overflowY: 'auto', overflowX: 'hidden'});
            },
            addDemoData: function (results,counter) {
                var clone1 = results.slice(0);
                for(var i = 0, l = counter; i < l; i++ ) results = $.merge($.merge([], results), clone1);
                return results;
            },
            html:{
                init: function(data){
                    var results = data.results;
                    results = methods.addDemoData(results);
                    var sort = [];
                    if(!state.opts.noSort) {
                        if (state.opts.sort) {
                            $.each(state.opts.sort, function (key, value) {
                                sort.push({property: key, asc: $.jCommon.string.equals(value, "asc")});
                            })
                        }
                        else {
                            sort.push({property: "title", asc: true});
                        }
                    }
                    results = $.jCommon.array.sort(results, sort);
                    $.each(results, function(){
                        var item = this;
                        var result = $(document.createElement('div')).addClass('data-list');
                        if(state.opts.properties) {
                            methods.html.properties(result, item);
                        }
                        else if(state.opts.contextual ){
                            methods.html.contextual(result, item);
                        }
                        if(result.children().length>0) {
                            state.body.append(result);
                        }
                    });
                },
                contextual: function(result, item){
                    var parts = state.opts.contextual.split("\[\[");
                    if(!parts || parts.length>0){
                        var r = '';
                        var len = parts.length;
                        for(var i=0;i<len;i++){
                            var part = parts[i];
                            if($.jCommon.string.contains(part, "\]\]")){
                                var sub = part.split(']]');
                                var t = sub[0];
                                var ps = t.split(":");
                                var l = ps.length;
                                var value;
                                for(var j=0;j<l;j+=2){
                                    var k = ps[j];
                                    var v = ps[j+1];
                                    switch (k){
                                        case 'key':
                                            value = item[v];
                                            break;
                                        case 'type':
                                            if(value && v==='date'){
                                                value = $.jCommon.dateTime.defaultFormat(value);
                                            }
                                            break;
                                        case 'fn':
                                            value = FnFactory[v](value);
                                            break;
                                    }
                                }
                                if(!value){
                                    value = "Unknown";
                                }
                                r+=value;
                                if(sub.length===2){
                                    r+=sub[1];
                                }
                            }
                            else {
                                r+=part;
                            }
                        }
                        if(r && r.length>0){
                            result.append(r);
                        }
                    }
                },
                properties: function(result, item){
                    var pOn=0;
                    var last=null;
                    var nodeItem = dCrt('div');
                    $.each(state.opts.properties, function(){
                        var value;
                        if(this.property==='#'){
                            value = (state.on+1)+'.&nbsp;';
                        }
                        else if(this.property && $.jCommon.string.startsWith(this.property, "fn::")){
                            var parts = this.property.split("::");
                            if(parts.length>1 && $.isFunction(FnFactory[parts[1]])) {
                                value = FnFactory[parts[1]](parts, item);
                            }
                        }
                        else if(this.property==='vertexType'){
                            value = dCrt("div");
                            var o = this;
                            o.data = item;
                            value.semanticType(o);
                        }
                        else {
                            // todo: work with the new format "fn:: and call FnFactory to retrieve value.
                            value = $.jCommon.json.getProperty(item, this.property);
                        }

                        if((null!==value) && this.dataType && $.jCommon.string.equals(this.dataType, "dateOnly")){
                            value = $.jCommon.dateTime.dateOnly(value);
                        }
                        else if((null!==value) && this.dataType && $.jCommon.string.equals(this.dataType, "dateShort")){
                            value = $.jCommon.dateTime.defaultFormat(value);
                        }
                        if(value) {
                            if (pOn > 0 && last!=='#') {
                                if(state.opts.blocked){
                                    nodeItem.append("<br/>")
                                }
                                else {
                                    var splitter = $(document.createElement("span")).html(state.opts.splitter ? "|" : "&nbsp;");
                                    if(state.opts.splitter) {
                                        splitter.css({margin: '0 5px 0 5px'});
                                    }
                                    nodeItem.append(splitter);
                                }
                            }
                            if (item.vertexType === '/common/email') {
                                if(this.property === 'value'){
                                    var mailNode = $(document.createElement('a'))
                                        .attr('href', 'mailto:' + value).html(value);
                                    nodeItem.append(mailNode);
                                }
                                else if(this.property === 'category') {
                                    var mailCatNode = $(document.createElement('span'))
                                        .html($.jCommon.string.toTitleCase($.jCommon.string.replaceAll(value,'_',' ')));
                                    nodeItem.append(mailCatNode);
                                }
                            }
                            else if (item.vertexType === '/common/phone_number') {
                                if(this.property === 'value'){
                                    var phoneNode = $(document.createElement('span'))
                                        .html($.jCommon.string.toPhoneNumber(value));
                                    nodeItem.append(phoneNode);
                                }
                                else if(this.property === 'category') {
                                    var pCatNode = $(document.createElement('span'))
                                        .html($.jCommon.string.toTitleCase($.jCommon.string.replaceAll(value,'_',' ')));
                                    nodeItem.append(pCatNode);
                                }
                            }
                            else{
                                if (this.label) {
                                    var lbl = $(document.createElement('div')).addClass('data-label').html(this.label);
                                    nodeItem.append(lbl);
                                }
                                var did = this.delinkIfDeprecated && state.worker.data.deprecated;
                                var type = (this.linked && !did) ? "a" : "div";
                                var node = $(document.createElement(type)).addClass('data-value').html(value);
                                if(this.linked && !did){
                                    var key = this.hrefProperty ? this.hrefProperty : state.KEY_ID;
                                    var href = item[key];
                                    if(item.relatedUri){
                                        href = item.relatedUri;
                                    }
                                    if(this.href){
                                        href = $.jCommon.string.replaceAll(this.href, "\\[href\\]", item[key]);
                                    }
                                    if(this.params){
                                        var prm = '';
                                        $.each(this.params, function (key, value) {
                                            if(prm.length>0){
                                                prm+="&";
                                            }
                                            prm += String.format('{0}={1}', key, value);
                                        });
                                        if(prm.length>0){
                                            href = String.format('{0}?{1}', href, prm);
                                        }
                                    }
                                    node.attr('href', href).attr('target', '_blank');
                                }
                                nodeItem.append(node);
                            }
                            pOn++;
                            last=this.property;
                        }
                    });
                    result.append(nodeItem);
                    state.on++;
                },
                putMissing: function(){
                   var missing = state.opts.missing;
                   var result = dCrt('div');
                   var p = dCrt('p').html(missing);
                   result.append(p);
                   state.body.append(result);
                }

            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    // if you want the zone to adjust its height set adjustHeight to 0 or more.
    //Default Settings
    $.dataList.defaults = {
        limit: 100,
        hasItemCounter: true,
        paging: true,
        hasHeader: true,
        missing: null,
        adjustBodyHeight: 0,
        splitter: true
    };


    //Plugin Function
    $.fn.dataList = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.dataList($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $dataList = $(this).data('dataList');
            switch (method) {
                case 'state':
                default: return $dataList;
            }
        }
    };

    $.dataList.call= function(elem, options){
        elem.dataList(options);
    };

    try {
        $.htmlEngine.plugins.register("dataList", $.dataList.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
