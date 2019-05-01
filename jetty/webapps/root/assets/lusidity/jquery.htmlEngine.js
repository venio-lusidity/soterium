;var dCrt = function (tagName) {
    return $(document.createElement(tagName));
};
var dMax = function (node, h, w) {
    if (undefined!==h) {
        if ($.jCommon.is.numeric(h)) {
            h = h + 'px';
        }
        node.css({maxHeight: h, minHeight: h, height: h});
    }
    if (undefined!==w) {
        if ($.jCommon.is.numeric(w)) {
            w = w + 'px';
        }
        node.css({maxWidth: w, minWidth: w, width: w});
    }
};
var applyPx = function (node, key, val) {
    var v = '';
    if($.jCommon.is.numeric(val) && val>0){
        v = val+'px';
    }
    else if(val && val==='auto'){
        v=val;
    }
    node.css(key, v);
};
var dHeight = function (node, min, h, max) {
    applyPx(node, 'min-height', min);
    applyPx(node, 'height', h);
    applyPx(node, 'max-height', max);
};
var dWidth = function (node, min, w, max) {
    applyPx(node, 'min-width', min);
    applyPx(node, 'width', w);
    applyPx(node, 'max-width', max);
};
var dLog = function (format, args) {
    var r = format;
    for(var i=1;i<=arguments.length;i++){
        var srch = '{' + (i-1) + '}';
        var v = arguments[i];
        r = r.replace(srch, v);
    }
};
// lbl: string or node
// lbl+maxChars : lbl can only be a string.
// href: relative or fully qualified.
// tip: can only be a string
// maxChars: can only be numeric
var dLink = function(lbl, href, tip, maxChars){
   var r =  dCrt('a').attr('href', href).attr('target', '_blank');
   if(tip){
       r.attr('title', tip);
   }
    if(lbl){
        if(maxChars){
            lbl = $.jCommon.string.ellipsis(lbl, maxChars);
        }
        r.append(lbl);
    }
   return r;
};
var dAttr = function(elementType, attrName, value){
    return $(String.format('{0}[{1}="{2}"]', elementType, attrName, value));
};
var dLabel = function(l, v){
    var r = dCrt('div');
    var lbl = dCrt('span').append(l);
    var sep = dCrt('span').html(":&nbsp;");
    var val = dCrt('span').append(v);
    return r.append(lbl).append(sep).append(val);
};
var jObj = function (idOrCls) {
    return ($.jCommon.string.startsWith('.')) ? $(idOrCls) : $('#' + idOrCls);
};
(function ($) {

    //Object Instance
    $.htmlEngine = function(el, options) {
        var state = el,
            methods = {};
        state.opts = $.extend({}, $.htmlEngine.defaults, options);
        state.isUpdating = false;
        state.workers = {};
        state.isMobile = lusidity.environment('isMobile');
        state.isIPad = lusidity.environment('isIPad');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("htmlEngine", state);

        // Private environment methods
        methods = {
            init: function() {
                var isValid = methods.isValid();
                if(!isValid){
                    return false;
                }
                methods.plugins.load();
                $.htmlEngine.addStyling(state.opts.schema, state);
                $.htmlEngine.addAttributes(state.opts.schema, state);
                methods.handler.init(state, state.opts.schema.schemas, state.opts.data);
            },
            autoHide: function(){
                if(state.opts.autoHide){
                    var hide = true;
                    var properties = state.find('[data-valid="true"]');
                    if(properties.length>0){
                        if(state.parent().length>0){
                            state.parent().removeClass('hidden');
                            state.parent().show();
                        }
                    }
                    else{
                        if(state.parent().length>0){
                            state.parent().addClass('hidden');
                            state.parent().hide();
                        }
                    }
                }
            },
            checkDebug: function(worker){
                if(worker.schema && worker.schema.debug){
                    console.log('debug in schema');
                }
                return true;
            },
            data: {
                compare: function(conditions, worker){
                    var result = false;
                    var expr = null;
                    var operator = 'default';
                    $.each(conditions, function(){
                        if(this.operator){
                            //  determine if this is an operator and if it is set the next comparison up for proper eval.
                            operator = this.operator.toLowerCase();
                            return true;
                        }
                        //  get the value of the property
                        this.actual = $.jCommon.json.getProperty(worker.data, this.valueOf, this.valueType);
                        function equals(obj1, obj2, comparators){
                            var result = false;
                            $.each(comparators, function(){
                                switch (this.toLowerCase()){
                                    case 'gt':
                                        result = obj1 > obj2;break;
                                    case 'lt':
                                        result = obj1 < obj2;break;
                                    case 'eq':
                                        if(null===obj2)
                                        {
                                            result = (null === obj1) || (undefined === obj1);
                                        }
                                        else {
                                            result = obj1 === obj2;
                                        }
                                        break;
                                    case 'contains':
                                        result = $.jCommon.string.contains(obj1, obj2, true);break;
                                    case 'startsWith':
                                        result = $.jCommon.string.startsWith(obj1, obj2, true);break;
                                    case 'endsWith':
                                        result = $.jCommon.string.endsWith(obj1, obj2, true);break;
                                }
                                if(result){
                                    return false;
                                }
                            });
                            return result;
                        }
                        //  TODO: Add support for "not"
                        switch (operator){
                            case 'and':
                                result = (result && equals(this.actual, this.compareTo, this.comparators));
                                break;
                            case 'or':
                                result = (result || equals(this.actual, this.compareTo, this.comparators));
                                break;
                            default:
                                result = equals(this.actual, this.compareTo, this.comparators);
                                break;

                        }
                        //  reset the operator
                        operator = 'default';
                    });
                    return result;
                },
                getHyperlink: function(data, schema)
                {
                    return ($.jCommon.json.hasProperty(data, schema.hrefProperty)) ?
                        data[schema.hrefProperty] : ($.jCommon.is.string(data)) ?
                        data : null
                },
                getString: function(itemValue){
                    return $.htmlEngine.getString(itemValue);
                },
                getDataFromKeys: function(data, keys){
                    var result = $.extend({}, data);
                    if($(keys).length>0)
                    {
                        result = result[keys[0]];
                        var len = keys.length;
                        for(var i=1;i<len;i++)
                        {
                            try
                            {
                                if($.jCommon.is.array(result))
                                {
                                    result = result[0];
                                }
                                result = result[keys[i]];
                            }
                            catch(e){ result = null;}
                        }
                    }
                    return result;
                },
                isExcluded: function(worker, excludedValue){
                    var excluded = false;
                    if (worker.schema.exclusions && $.jCommon.is.string(excludedValue)) {
                        $.each(worker.schema.exclusions, function () {
                            if ($.jCommon.string.contains(excludedValue, this, true)){
                                worker.node.attr("excluded", "true");
                                excluded = true;
                                return false;
                            }
                        });
                    }
                    return excluded;
                },
                isValid: function(worker){
                    var valid = ($(worker.data).length>0);
                    if(valid && worker.schema && worker.schema.property){
                        valid = (worker.data[worker.schema.property] !== undefined);
                    }
                    return valid;
                },
                mapActions: function(data, mapType){
                    var results = [];
                    $.each(data, function () {
                        $.each(this.jActions, function(){
                            var type = this.type.toLowerCase();
                            if($.jCommon.string.equals(mapType, type, true)){
                                results.push(this);
                            }
                        });
                    });
                    return results;
                },
                transform: function(worker){
                    if (worker.data === undefined) {
                        return false;
                    }
                    if((worker.schema.invoke && worker.schema.invoke)) {
                        methods.plugins.invoke(worker);
                    }
                    else {
                       if (worker.data && ($.jCommon.is.array(worker.data)
                            || (worker.data.results && $.jCommon.is.array(worker.data.results)))) {
                            methods.handle.array(worker);
                        }
                        else if ($.jCommon.is.object(worker.data)) {
                           methods.handle.object(worker);
                        }
                        else{
                           if (!methods.data.isExcluded(worker, worker.data)) {
                               methods.handle.string(worker);
                           }
                       }
                    }
                }
            },
            handle:{
                array: function(worker){
                    if($.jCommon.json.hasProperty(worker, "schema.maxItems") && worker.schema.maxItems === 1){

                        worker.data =(worker.data.results ? worker.data.results[0] : worker.data[0]);
                        methods.layouts._default(worker);
                    }
                    else{
                        var on=0;
                        var data = (worker.data.results ? worker.data.results : worker.data);
                        var len = data.length;
                        if(!worker.schema.maxItems){
                            worker.schema.maxItems = len;
                        }
                        var hm = $.jCommon.json.hasProperty(worker, "schema.maxItems");
                        var m = hm ? worker.schema.maxItems : 10;
                        $.each(data, function(){
                            if(hm && on === m){
                                return false;
                            }
                            var item  = $.extend(true, {}, worker);
                            item.data = this;
                            item.parentData = worker.parentData;
                            methods.layouts._default(item);
                        });
                    }
                },
                edit: {
                    init: function (worker, txt) {
                        if($.jCommon.string.empty(txt.toString())){
                            return false;
                        }
                        if (worker.schema.debug) {
                            methods.checkDebug(worker);
                        }
                        switch (worker.schema.editType){
                            case 'vertexType':
                                methods.handle.edit.vt(worker, txt);
                            break;
                            default:
                                worker.node.append(txt);
                                break;
                        }
                    },
                    input: function (worker, txt, c, n, t) {
                        if(t.is('div')){
                            t.css({display: 'inline-block'});
                            t.jNodeReady({ onReady: function () {
                                var len = (t.width()+20)+'px';
                                n.css({width: len});
                            }});
                        }
                        n.css({display: 'inline-block'}).hide();
                        var p = $.htmlEngine.pencil().css({marginRight: '5px', color: '#c3c3c3c3'});
                        c.css({whiteSpace: 'nowrap'});
                        c.prepend(p);
                        var msg = 'Click to Edit, Escape or click out of to cancel and Enter or Tab to save.';
                        worker.node.css({cursor: 'pointer'}).attr('title', msg);
                        worker.node.on('click', function () {
                            if(t.is(':visible')) {
                                t.hide();
                                n.show();
                                n.select();
                                worker.node.attr('title', msg);
                            }
                        });
                        function update(save) {
                            n.hide();
                            var v = c.inputValidator('validate', {input: n});
                            if(save && v) {
                                var v = n.val();
                                var cx = t.html();
                                if(!$.jCommon.string.equals(cx, v, true)) {
                                    var s = function (data) {
                                        t.html(v);
                                        n.val(v);
                                    };
                                    var f = function (j, t, e) {
                                        console.log(j);
                                        console.log(t);
                                        console.log(e);
                                    };
                                    var content = (worker.data[state.KEY_ID] ? worker.data : (worker.parentData[state.KEY_ID] ? worker.parentData : null));
                                    if (content && $.jCommon.json.hasProperty(content, worker.schema.property)) {
                                        var url = content[state.KEY_ID];
                                        content[worker.schema.property] = v;
                                        $.htmlEngine.request(url, s, f, content, 'post');
                                    }
                                }
                            }
                            else{
                                n.val(t.html());
                            }
                            t.show();
                        }
                        var tbd = false;
                        n.on('blur', function () {
                            if(!tbd) {
                                update(false);
                            }
                        });
                        n.on('keydown', function (e) {
                            if(e.which===9){
                                tbd = true;
                                update(true);
                                var inputs = $('body').nextAll('input');
                                var f = false;
                                $.each(inputs, function () {
                                    if(n===$(this)){
                                        f = true;
                                        return true;
                                    }
                                    if(f){
                                        $(this).parent().parent().click();
                                        return false;
                                    }
                                });
                                tbd = false;
                            }
                        });
                        n.on('keyup', function (e) {
                            if(e.which===13){
                                update(true);
                            }
                            else if(e.which===27){
                                n.blur();
                            }
                        });
                    },
                    vt: function (worker, txt) {
                        var t = worker.data.vertexType ? worker.data.vertexType : worker.parentData.vertexType;
                        if(!t){
                            worker.node.append(txt);
                            return false;
                        }
                        t = $.jCommon.string.getLast(t, "/");
                        switch(t){
                            case 'phone_number':
                                if(!$.jCommon.string.contains(txt, '.')){
                                    txt = $.jCommon.string.toPhoneNumber(txt);
                                }
                                var n = dCrt('input').attr('placeholder', 'Enter your phone number').hide();
                                var t = dCrt('div').html(txt);
                                var c = dCrt('div');
                                c.append(n).append(t);
                                worker.node.append(c);
                                methods.handle.edit.input(worker, txt, c, n, t);
                                //{phone: [$('#phone')], extension: [$("#extension")], email: [$("#email"), $("#supervisorEmail")]}
                                c.inputValidator({phone: [n]});
                                n.val(txt);
                                break;
                            case 'email':
                                var n = dCrt('input').attr('placeholder', 'Enter your phone number').hide();
                                n.val(txt);
                                var t = dCrt('div').html(txt);
                                var c = dCrt('div');
                                c.append(n).append(t);
                                worker.node.append(c);
                                methods.handle.edit.input(worker, txt, c, n, t);
                                c.inputValidator({email: [n]});
                                break;
                            default:
                                worker.node.append(txt);
                                break;
                        }
                    }
                },
                object: function(worker){
                    methods.checkDebug(worker);
                    var apply = true;
                    if ($.jCommon.json.hasProperty(worker, "schema.conditions")) {
                        apply = methods.data.compare(worker.schema.conditions, worker);
                    }
                    if (apply) {
                        if ($.jCommon.json.hasProperty(worker, "schema.hrefProperty")
                            || $.jCommon.json.hasProperty(worker, "schema.hrefQuery")) {
                            methods.layouts.hyperLink(worker);
                        }
                        else {
                            var data;
                            if ($.jCommon.json.hasProperty(worker, "schema.property")
                                && $.jCommon.json.hasProperty(worker.data, worker.schema.property)) {
                                data = $.jCommon.json.getProperty(worker.data, worker.schema.property);
                            }
                            else {
                                data = $.extend(true, {}, worker.data);
                            }

                            var text;
                            if ($.jCommon.json.hasProperty(worker, "schema.property")) {
                                var value = $.jCommon.json.getProperty(data, worker.schema.property);
                                if (null !== value && undefined !== value) {
                                    if ($.jCommon.json.hasProperty(worker, "schema.schemas")) {
                                        methods.handler.init(worker.node, worker.schema.schemas, value, data);
                                        worker.node.attr('data-valid', true).show();
                                        return false;
                                    }
                                    else {
                                        text = $.htmlEngine.getString(value);
                                    }
                                }
                                else{
                                    worker.node.attr('data-valid', false).hide();
                                }
                            }
                            else if ($.jCommon.json.hasProperty(worker, "schema.schemas")) {
                                methods.handler.init(worker.node, worker.schema.schemas, data, data);
                                return false;
                            }
                            else {
                                text = $.jCommon.is.string(data) ? data : $.htmlEngine.getString(data);
                            }
                            if (!$.jCommon.string.empty(text)) {
                                var item = $.extend(true, {}, worker);
                                if(worker.schema.urlify){
                                    text = $.htmlEngine.urlify(text);
                                }
                                text = $.htmlEngine.returnify(text);
                                item.data = text;
                                methods.layouts._default(item);
                                worker.node.attr('data-valid', true).show();
                            }
                        }
                    }
                },
                string: function(worker) {
                    var text = methods.data.getString(worker.data);
                    if (worker.schema.urlify) {
                        text = $.htmlEngine.urlify(text);
                    }
                    text = $.htmlEngine.returnify(text);
                    var url = $.jCommon.url.create(text);
                    if (url.isUrl || $.jCommon.json.hasProperty(worker, "schema.hrefQuery")) {
                        methods.layouts.hyperLink(worker);
                        return false;
                    }
                    if (!$.jCommon.string.empty(text)) {
                        if (!worker.schema.dataType) {
                            worker.schema.dataType = 'string'
                        }
                        switch (worker.schema.dataType) {
                            case 'base64':
                                text = atob(text);
                                break;
                            case 'date':
                            case 'time':
                                var result;
                                if ($.jCommon.json.hasProperty(worker, "schema.format")) {
                                    result = $.jCommon.dateTime.format(text, worker.schema.format);
                                }
                                else {
                                    result = $.jCommon.dateTime.minutesToHours(text);
                                }
                                text = (!result) ? '' : result;
                                break;
                            case 'measurement':
                                if ($.jCommon.json.hasProperty(worker, "schema.format")) {
                                    text = $.jCommon.number.convert(text, worker.schema.format);
                                }
                                break;
                            case 'maxChars':
                            default:
                                if ($.jCommon.json.hasProperty(worker, "schema.maxChars")
                                    && $.jCommon.is.numeric(worker.schema.maxChars)
                                    && (text.length > worker.schema.maxChars)) {
                                    text = $.jCommon.string.ellipsis(text, worker.schema.maxChars, worker.schema.trimLeft);
                                }
                                break;
                        }
                        if (!$.jCommon.string.contains(worker.node.html(), text)) {
                            if ($.jCommon.json.hasProperty(worker, "schema.favIcon")) {
                                var urlFav = $.jCommon.url.create(
                                    $.jCommon.is.object(worker.schema.favIcon)
                                        ? worker.schema.favIcon.src : (methods.data.getString(state.opts.data[lusidity.uriKey])));
                                if (urlFav.isUrl) {
                                    var img = $.htmlEngine.createElement({
                                        type: 'img',
                                        cls: 'fav'
                                    }, null, worker.data);
                                    if ($.jCommon.is.object(worker.schema.favIcon)) {
                                        $.htmlEngine.addStyling(worker.schema.favIcon, img);
                                    }
                                    img.attr('src', urlFav.favIcon);
                                    var link = dCrt('a').attr('href', urlFav.original);
                                    link.append(img);
                                    link.insertBefore(worker.node);
                                    link.append(worker.node);
                                }
                            }

                            if ($.jCommon.json.hasProperty(worker, "schema.hrefProperty")
                                || $.jCommon.json.hasProperty(worker, "schema.hrefQuery")) {
                                methods.layouts.hyperLink(worker, text);
                            }
                            else {
                                if(worker.schema.editType){
                                    methods.handle.edit.init(worker, text);
                                }
                                else {
                                    worker.node.append(text);
                                }
                            }
                            worker.node.attr('data-valid', true).show();
                        }
                    }
                },
                toggleBlock: function(worker){
                    var block = $(worker.node.closest('.block'));
                    var isValid = (worker.node.attr('data-valid')  !== undefined
                    && worker.node.attr('data-valid') === "true");
                    if(isValid){
                        worker.node.show();
                        if(block.length>0 && !block.is(':visible')){
                            block.slideDown(300);
                        }
                    }
                    else{
                        worker.node.hide();
                    }
                    if(state.opts.autoHide) {
                        methods.autoHide();
                    }
                }
            },
            handler: {
                init: function(parentNode, schemas, data, parentData){
                    $.each(schemas, function () {
                        var schema = this;
                        var skip = false;
                        if(!this.layout){
                            this.layout = "data";
                        }
                        var worker = state.workers[schema.id];
                        if (worker === undefined) {
                            if (!schema.id) {
                                schema.id = $.htmlEngine.getRandomId("schema");
                            }
                            state.workers[schema.id] = {
                                schema: schema,
                                node: null,
                                data: data ? data : parentData,
                                parentNode: parentNode
                            };
                            worker = state.workers[schema.id];
                        }

                        if(schema.debug){
                        }

                        if (schema.conditions) {
                            skip = !methods.data.compare(schema.conditions, worker);
                        }
                        if (skip) {
                            return true;
                        }
                        if ($.jCommon.is.array(data) && !$.jCommon.json.hasProperty(data[0], schema.property)) {
                            worker.data = null;
                        }
                        else if ($.jCommon.is.object(data)) {
                            worker.parentData = (undefined !== parentData && null !== parentData) ? parentData : data;
                            worker.data = $.jCommon.json.getProperty(data, worker.schema.property);
                            if (null === worker.data) {
                                worker.data = data.results ? data.results : data;
                            }
                        }
                        methods.checkDebug(worker);
                        methods.handler.layouts[worker.schema.layout](worker);
                    });
                },
                layouts: {
                    html: function(worker){
                        worker.node = $.htmlEngine.createElement(worker.schema);
                        worker.parentNode.append(worker.node);
                        if(worker.schema.text){
                            worker.node.append(worker.schema.text);
                        }
                        else if(worker.schema && worker.schema.schemas){
                            var cd = $.jCommon.json.getProperty(worker.data, worker.schema.property);
                            methods.handler.init(worker.node, worker.schema.schemas, cd, worker.data);
                        }
                    },
                    data: function(worker){
                        worker.node = $.htmlEngine.createElement(worker.schema);
                        worker.parentNode.append(worker.node);
                        if($.jCommon.is.array(worker.data) && worker.schema.schemas){
                            $.each(worker.data, function(){
                                methods.handler.init(worker.node, worker.schema.schemas, this, worker.data);
                            });
                        }
                        else {
                            methods.data.transform(worker);
                            /*
                            if (worker.schema && worker.schema.schemas) {
                                var cd = $.jCommon.json.getProperty(worker.data, worker.schema.property);
                                methods.handler.init(worker.node, worker.schema.schemas, cd, worker.data);
                            }    */
                        }
                    },
                    load: function(worker){
                        try {
                            worker.node = $.htmlEngine.createElement(worker.schema);
                            worker.parentNode.append(worker.node);
                            var url = worker.data[state.KEY_ID] + "/properties" + worker.schema.load;
                            if (worker.schema.direction) {
                                url += "?direction=" + worker.schema.direction;
                            }
                            function isLoading(loading) {
                                worker.node.attr('data-loading', loading);
                                if (worker.node.parent()) {
                                    worker.node.parent().attr('data-loading', loading);
                                }
                            }

                            worker.node.parent().attr('data-loading', true);
                            worker.node.attr('data-valid', true).show();

                            if (!$.jCommon.string.empty(url)) {
                                var f = function (jqXHR, textStatus, errorThrown) {
                                    worker.parentNode.attr('data-loading', false).hide();
                                };
                                var s = function (data) {
                                    if (data && data.results && data.results.length > 0) {
                                        methods.checkDebug(worker);
                                        $.each(data.results, function () {
                                            methods.handler.init(worker.node, worker.schema.schemas, this, worker.data);
                                            if (worker.schema.maxItems === 1) {
                                                return false;
                                            }
                                        });
                                        if (state.opts.autoHide) {
                                            methods.autoHide();
                                        }
                                    }
                                };
                                $.htmlEngine.request(url, s, f, null, 'get');
                            }
                            else {
                                isLoading(false);
                            }
                        }
                        catch(e){
                            console.log(e);
                        }
                    },
                    plugin: function(worker) {
                        if (worker.schema.type) {
                            worker.node = $.htmlEngine.createElement(worker.schema);
                            worker.parentNode.append(worker.node);
                        }
                        else {
                            worker.node = worker.parentNode;
                        }
                        if (worker.schema.started && worker.schema.once) {
                            return false;
                        }
                        methods.plugins.invoke(worker);
                        worker.schema.started = true;
                    }
                }
            },
            isValid: function(){
                if(!state.opts.schema){
                    console.log("A schema is required.");
                    return false;
                }
                if(!state.opts.schema.layout){
                    console.log("The schema must have property layout, with a value of summary or default.");
                    return false;
                }
                if(!state.opts.schema.schemas){
                    console.log("The schema must have property schemas.");
                    return false;
                }
                return true;
            },
            layouts: {
                _default: function(worker) {
                    if (worker.data === undefined) {
                        return false;
                    }
                    if((worker.schema.invoke && worker.schema.invoke)) {
                        methods.plugins.invoke(worker);
                    }
                    else {
                        if ($.jCommon.is.string(worker.data)) {
                            if (!methods.data.isExcluded(worker, worker.data)) {
                                methods.handle.string(worker);
                            }
                        }
                        else if (worker.data && ($.jCommon.is.array(worker.data)
                            || (worker.data.results && $.jCommon.is.array(worker.data.results)))) {
                            methods.handle.array(worker);
                        }
                        else if ($.jCommon.is.object(worker.data) && worker.schema.schemas) {
                            methods.handler.init(worker.node, worker.schema.schemas, worker.data, worker.parentData);
                        }
                        else if ($.jCommon.is.object(worker.data)) {
                            methods.handle.object(worker);
                        }
                    }
                },
                hyperLink: function(worker, txt) {
                    var href = methods.data.getHyperlink(worker.data, worker.schema);
                    if ((href === null || !href.isUrl) && worker.parentData) {
                        href = methods.data.getHyperlink(worker.parentData, worker.schema);
                    }

                    if (null !== href) {
                        href = $.jCommon.url.create($.htmlEngine.getString(href));
                    }

                    var text = txt ? txt : $.jCommon.is.string(worker.data) ? worker.data :
                        ($.jCommon.json.hasProperty(worker, "schema.property") && $.jCommon.json.hasProperty(worker.data, worker.schema.property)) ?
                            $.htmlEngine.getString(worker.data[worker.schema.property]) : $.htmlEngine.getString(worker.data);

                    if (!$.jCommon.string.empty(text) && $.jCommon.element.contains(worker.node, text)) {
                        // this value is has already been set.
                        return false;
                    }

                    if ($.jCommon.string.empty(text)) {
                        text = 'unknown';
                    }

                    if (null === href || (!href.isUrl && worker.schema.hrefQuery)) {
                        var oldRef = worker.node.attr('href');
                        var newRef = worker.schema.hrefQuery + text;
                        if ($.jCommon.string.equals(oldRef, newRef, true)) {
                            return false;
                        }
                        href = $.jCommon.url.create(newRef);
                    }

                    if ($.jCommon.json.hasProperty(worker, "schema.maxChars")
                        && $.jCommon.is.numeric(worker.schema.maxChars)
                        && (text.length > worker.schema.maxChars)) {
                        text = $.jCommon.string.ellipsis(text, worker.schema.maxChars, worker.schema.trimLeft);
                    }

                    if (href.isUrl && !$.jCommon.string.empty(text)) {
                        var link = dCrt('a');
                        worker.node.append(link);
                        if (null === link) {
                            link = $.htmlEngine.createElement({type: 'a'}, worker.schema, worker.data);
                            worker.node.append(link);
                        }
                        link.attr('href', href.original);
                        var img;
                        if ($.jCommon.json.hasProperty(worker, "schema.favIcon")
                            && worker.schema.favIcon) {
                            img = $.htmlEngine.createElement({type: 'img', cls: 'fav'}, null, worker.data);
                            if ($.jCommon.is.object(worker.schema.favIcon)) {
                                $.htmlEngine.addStyling(worker.schema.favIcon, img);
                                img.attr('src', worker.schema.favIcon.src);
                            }
                            else {
                                img.attr('src', href.favIcon);
                            }
                            link.append(img);
                        }
                        if (!worker.schema.favIconOnly && !$.jCommon.string.empty(text)) {
                            link.append($.htmlEngine.createElement(
                                {type: 'div', cls: "link-text"}, null, worker.data
                            ).html(text));
                        }
                        worker.node.attr('data-valid', true).show();
                    }
                    else{
                        return false;
                    }
                }
            },
            plugins:{
                load: function() {
                    if (state.opts.schema.styles) {
                        $.each(state.opts.schema.styles, function () {
                            $.jCommon.load.css(this);
                        });
                    }
                    if (state.opts.schema.plugins) {
                        $.each(state.opts.schema.plugins, function () {
                            $.jCommon.load.script(this);
                        });
                    }
                },
                invoke: function(worker){
                    if(worker.schema.name){
                        var plugin = $.htmlEngine.plugins.get(worker.schema.name);
                        if(plugin) {
                            plugin(worker.node, worker);
                        }
                    }
                }
            }
        };
        //public methods

        //environment: Initialize
        methods.init();
    };

    $.htmlEngine.stabilize = function (node, stabilize, offset) {
        if(stabilize){
            var h = node.innerHeight();
            if(offset){
                h+=offset;
            }
            if(h>0) {
                node.css({minHeight: h + 'px', height: h + 'px', overflow: 'hidden'});
            }
        }
        else{
            node.css({minHeight: '', height: '', overflow: ''});
        }
    };
    
    $.htmlEngine.adjustHeight = function (node, fill, offsetHeight, fillNode, onResize) {
        var h = 100;
        if (offsetHeight || fill) {
            function set() {
                dMax(node, h);
                node.css({
                    padding: '0px 0 0px 0',
                    overflowX: 'hidden',
                    overflowY: 'auto'
                });
                if(node && node.body){
                   dMax(node.body, h)
                }
            }
            function sh() {
                var oah = offsetHeight ? offsetHeight : 0;
                var ah = fillNode ? node.height() : node.offset().top;
                var wh = $(window).height();
                if (ah > 0 && wh>0) {
                    if (fill) {
                        h = (wh-ah) - oah;
                    }
                    if(h<20){
                        h=20;
                    }
                    set();
                    lusidity.environment('onResize', function () {
                        sh();
                        if($.isFunction(onResize)){
                            onResize();
                        }
                    });
                }
                else {
                    var sw = new oStopWatch();
                    sw.waitAsync(100, sh);
                }
            }
            sh();
        }
    };

    $.htmlEngine.toIndexValue = function (v) {
        return v;// $.jCommon.string.empty(v) ? null : v.replace(/[^\w]/gi, '').toLowerCase();
    };

    $.htmlEngine.dropDown = function (selections, id) {
        id = ($.jCommon.string.empty(id)) ?  $.htmlEngine.getRandomId('dd') : id;
        var r = $(document.createElement('div')).addClass('dropdown');
        var btn = $(document.createElement('button')).addClass('btn btn-default dropdown-toggle').attr('id', id)
            .attr("type", "button").attr("data-toggle", "dropdown").attr("aria-haspopup", "true").attr("aria-expanded", "true");
        var c = $(document.createElement('span')).addClass('caret');
        btn.append(c);
        r.append(btn);
        var u = $(document.createElement('ul')).addClass('dropdown-menu').attr('aria-labelledby', id);
        r.append(u);
        function select(data){
            c.remove();
            btn.html('');
            btn.append(data.label);
            c = $(document.createElement('span')).css({marginLeft: '5px'}).addClass('caret');
            btn.append(c);
        }
        $.each(selections, function () {
            var item = this;
            var l = $(document.createElement('li'));
            var i = $(document.createElement('div')).append(item.label);
            if(item.css){
                l.css(item.css);
            }
            if(item.attr){
                $.each(item.attr, function (k, v) {
                   l.attr(k, v);
                });
            }
            if(item.title){
                l.attr("title", item.title);
            }
            l.append(i);
                i.on('click', function (e) {
                    var p = true;
                    if($.isFunction(item.beforeClick)){
                        p = item.beforeClick(e, l, item);
                        if(!p){
                            console.log('drop down prevented');
                        }
                    }
                    if(p) {
                        select(item);
                        if (item.onClick && $.isFunction(item.onClick)) {
                            item.onClick(l, item);
                        }
                    }
                });
            u.append(l);
            if(item.selected){
                select(item);
            }
        });
        return r;
    };

    $.htmlEngine.ellipsis = function(text, maxChars){
        if(!$.jCommon.is.empty(text) && text.length>maxChars){
            var itemValue = text.substring(0, maxChars);
            if(!$.jCommon.string.endsWith(itemValue, '...')){
                if($.jCommon.string.endsWith(itemValue, ' ')){
                    itemValue = itemValue.substring(0, itemValue.length-1);
                }
                itemValue += '...';
            }
            return itemValue;
        }
        return text;
    };

    $.htmlEngine.indicators = function(options){
        var result;

        switch(options.type){
            default:
            case "spinner":
                result = dCrt('div').addClass('loading-indicator')
                    .css({position: 'relative', width: options.width + 'px', height: options.height + 'px'});
                var img = dCrt('img')
                    .css({width: options.width + 'px', height: options.height + 'px'})
                    .attr('src', '/assets/img/loading.gif');
                result.append(img);
                break;
        }
        if(options.css){
            result.css(options.css);
        }
        return result;
    };
    $.htmlEngine.noImage = function(img, textOnly){
        if(!textOnly){
            img.attr('src', '/assets/img/no-image.png').addClass('is-portrait').removeClass('is-landscape');
        }
        else{
            img.parent().parent().css({position: 'relative'});
            var noImage = dCrt('div').html('No Image Available').addClass('no-image');
            noImage.insertBefore(img.parent());
            img.parent().remove();
        }
    };
    
    $.htmlEngine.getSpinner = function(){
        return dCrt('img').attr('src', '/assets/img/refreshing64.gif').css({
            'height': '16px',
            position: 'relative',
            top: '-4px'
        });
    };

    $.htmlEngine.exists = function () {
        return true;
    };

    $.htmlEngine.mediaObject = function (opts) {
        var m = dCrt('div').addClass("media");
        var ml = dCrt('div').addClass("media-left");
        var mb = dCrt('div').addClass("media-body");
        m.append(ml).append(mb);

        if(opts.src.is('span')){
            opts.src.css({fontSize: '48px'});
            ml.append(opts.src.addClass('media-obj'));
        }
        else{
            var img = dCrt('img').addClass('media-obj').attr('src', state.opts.src);
            ml.append(img);
        }

        if(opts.title){
            var h = dCrt('h4').addClass('media-heading').append(opts.title);
            mb.append(h);
        }

        if(opts.subtext){
            var sub = dCrt('div').append(opts.subtext);
            mb.append(sub);
        }
        return m;
    };

    $.htmlEngine.getString = function(itemValue){
        // do not use isNaN it breaks everything.
        //noinspection JSComparisonWithNaN
        if(itemValue === undefined || itemValue === null || itemValue === NaN){
            return null;
        }
        if(typeof itemValue === 'string' || itemValue instanceof String){
            itemValue = itemValue.toString();
        }
        else if(itemValue.values !== undefined){
            itemValue = $.htmlEngine.getString(itemValue.values);
        }
        else if ($.isArray(itemValue) && itemValue.length>0) {
            itemValue = $.htmlEngine.getString(itemValue[0]);
        }
        else if(itemValue.title !== undefined ){
            itemValue = $.htmlEngine.getString(itemValue.title);
        }
        else if(itemValue.text !== undefined) {
            itemValue = $.htmlEngine.getString(itemValue.text);
        }
        else if(itemValue.name !== undefined){
            itemValue = $.htmlEngine.getString(itemValue.name);
        }
        else if(itemValue.label !== undefined){
            itemValue = $.htmlEngine.getString(itemValue.label);
        }
        else if(itemValue.uri !== undefined){
            itemValue = $.htmlEngine.getString(itemValue.uri);
        }
        else if(itemValue.value !== undefined){
            itemValue = $.htmlEngine.getString(itemValue.value);
        }
        else
        {
            var t = typeof itemValue;
            t = t.toString();
            switch (t)
            {
                case "object":
                case "null":
                case "undefined":
                    itemValue = null;
                    break;
                default:
                    itemValue = itemValue.toString();
                    break;
            }
        }
        return itemValue;
    };

    $.htmlEngine.inputRemove = function(){
        var wrapper = dCrt('div').addClass('input-remove');
        var input = dCrt('input');
        var icon = dCrt('span');
        icon.addClass('glyphicon glyphicon-remove').attr("aria-hidden", "true");
        var clear = dCrt('div').addClass('input-clear').hide().append(icon);
        wrapper.append(input).append(clear);
        input.bind('keyup', function(){
            var text = input.val();
            if($.jCommon.is.empty(text) || text === ""){
                clear.hide();
            }
            else{
                clear.show();
            }
        });
        clear.bind('click', function(){
            input.val('').focus();
        });
        return wrapper;
    };

    $.htmlEngine.createElement = function(schema, formating, data){
        var result = (schema.html) ? $(schema.html) : dCrt(((schema && schema.type) ? schema.type : 'div'));
        if(!schema.type){
            try {
                schema.type = result[0].tagName.toLowerCase();
            }
            catch (e){
                schema.type = "div";
            }
        }
        if(schema.id){
            result.attr("schema-id", schema.id);
        }
        $.htmlEngine.addStyling(formating, result);
        $.htmlEngine.addStyling(schema, result);
        $.htmlEngine.addAttributes(schema, result);
        $.htmlEngine.addAttributes(formating, result);
        return result;
    };
    
    $.htmlEngine.panel =function(container, glyph, title, url, borders, actions, menu, fill, smallHeader){
        var result = dCrt('div');
        var options = {
            glyph: glyph,
            title: title,
            url: url,
            borders: borders,
            content: result,
            body:{
                css: {padding: 0}
            },
            actions: actions ? actions : [],
            menu: menu,
            fill: fill,
            smallHeader: smallHeader
        };
        container.panel(options);
        return result;
    };

    $.htmlEngine.pencil = function () {
        return $.htmlEngine.glyph('glyphicon-pencil');
    };
    
    $.htmlEngine.glyph = function (glyph) {
        var r;
        if($.jCommon.string.startsWith(glyph, "glyphicon")) {
            var gp = $.jCommon.string.replaceAll(glyph, 'glyphicons ', '');
            gp = $.jCommon.string.replaceAll(glyph, 'glyphicon ', '');
            var g = ($.jCommon.string.contains(glyph, 'glyphicons')) ? 'glyphicons' : 'glyphicon';
            r = dCrt('span').addClass(g + ' ' + glyph).attr('aria-hidden', 'true');
        }
        else{
            r = dCrt('span');
            var img = dCrt('img').addClass('icon-glyph').attr('src', glyph);
            r.append(img);
        }
        return r;
    };

    $.htmlEngine.loadFiles = function(node, name, files){
        if(!$.jCommon.string.empty(name) || $.jCommon.is.array(files)) {
            var loaded = false;
            var onLoaded = function () {
                if (node && node.length > 0) {
                    var e = jQuery.Event("cssFileLoaded");
                    node.trigger(e);
                }
            };
            var cssFiles = [];
            if (!$.jCommon.string.empty(name)) {
                var rnd = Math.floor(Math.random() * 999999);
                cssFiles.push('/assets/lusidity/plugins/css/' + name + '.css?nocache=' + rnd);
            }
            if (files) {
                $.each(files, function () {
                    cssFiles.push(this);
                });
            }
            $.jCommon.load.css(cssFiles, onLoaded);
        }
    };

    $.htmlEngine.getRandomId = function(prefix){
        return $.jCommon.getRandomId(prefix);
    };

    $.htmlEngine.setElementType =  function(schema){
        if(schema === undefined){
            schema = {type: 'div'};
        }
        if(!schema.type){
            schema.type = 'div';
        }
        if(!schema.type && schema.hrefProperty){
            schema.type = 'a';
        }
        return schema;
    };

    $.htmlEngine.addStyling = function(schema, element) {
        if (schema !== undefined && schema instanceof Object) {
            if (schema.cls) {
                element.addClass(schema.cls);
            }
            if (schema.css) {
                element.css(schema.css);
            }
        }
    };

    $.htmlEngine.addAttributes = function(schema, element) {
        if (schema && schema.attr) {
            $.each(schema.attr, function (key, value) {
                try {
                    element.attr(key, value);
                } catch (e) {
                }
            });
        }
    };

    $.htmlEngine.listen = {
        added: function(node){
            var last = node.children().length;

            var listen = function(){
                window.setTimeout(function(){
                    check();
                },250);
            };

            var check = function(){
                var current = node.children().length;
                if(current>last || current<last){
                    var e = jQuery.Event("elementAdded");
                    e.added = $(node.children[last]);
                    e.current = current;
                    e.last = last;
                    node.trigger(e);
                    last = current;
                }
                listen();
            };
            listen();
        },
        hasHtml: function(node){
            var stop = false;

            var listen = function(){
                window.setTimeout(function(){
                    check();
                },250);
            };

            var check = function() {
                if (!$.jCommon.string.empty(node.html())) {
                    var e = jQuery.Event("elementHasHTML");
                    node.trigger(e);
                    stop=true;
                }

                if(!stop){
                    listen();
                }
            };

            listen();
        },
        loaded: function(node){
            var stop = false;

            var listen = function(){
                window.setTimeout(function(){
                    check();
                },250);
            };

            var check = function() {
                var loading = elem.find('[data-loading="true"]').length;
                if (loading <= 0) {
                    var e = jQuery.Event("elementsLoaded");
                    elem.trigger(e);
                    stop=true;
                }

                if(!stop){
                    listen();
                }
            };

            listen();
        }
    };

    $.htmlEngine.request = function (url, onSuccess, onFail, value, methodType, async, timeout, xhrFields) {
        if(undefined===async || null===async){
            async = true;
        }
        //noinspection JSUnusedLocalSymbols
        var action = {
            connector: null,
            async: async,
            xhrFields: xhrFields ? xhrFields : {},
            data: (undefined !== value) ? JSON.stringify(value) : null,
            methodType: (undefined !== methodType) ? methodType : 'get',
            showProgress: false,
            onbeforesend: {
                message: {msg: null, debug: false},
                execute: function () {
                }
            },
            oncompleted: {
                execute: function (jqXHR, textStatus) {
                }
            },
            onsuccess: {
                message: {msg: null, debug: false},
                execute: function (data) {
                    if (onSuccess && $.isFunction(onSuccess)) {
                        onSuccess(data);
                    }
                    else {
                        dLog("Although response was successful, I don't know what to do with it.");
                    }
                }
            },
            onfailure: {
                message: {msg: null, debug: false},
                execute: function (jqXHR, textStatus, errorThrown) {
                    if (onFail && $.isFunction(onFail)) {
                        onFail(jqXHR, textStatus, errorThrown);
                    }
                    else {
                        dLog('Sorry, we cannot find the property requested.&nbsp;&nbsp;'
                            + 'You can try refreshing the&nbsp;<a href="' + window.location.href + '">page</a>.');
                    }
                }
            },
            url: url
        };
        if(timeout && $.jCommon.is.numeric(timeout)){
            action.timeout = timeout;
        }
        return lusidity.environment('request', action);
    };

    $.htmlEngine.busy = function(node, opts){
        if(!$.jCommon.is.object(opts) || !opts.type){
            opts = {type: 'cube'};
        }
        if(node.loaders('exists')){
            node.loaders('show', opts);
        }
        else{
            node.loaders(opts);
        }
    };

    $.htmlEngine.returnify = function(text) {
        if (!$.jCommon.string.empty(text)) {
            text = $.jCommon.string.replaceAll(text, /\r\n/, "<br>");
            text = $.jCommon.string.replaceAll(text, /\n\r/, "<br>");
            text = $.jCommon.string.replaceAll(text, /\r/, "<br>");
            text = $.jCommon.string.replaceAll(text, /\n/, "<br>");
            text = $.jCommon.string.replaceAll(text, "<br\>", "<br>");
            text = $.jCommon.string.replaceAll(text, "<br \>", "<br>");
        }
        return text;
    };

    $.htmlEngine.urlify = function(text) {
        if ($.jCommon.string.contains(text, "http")) {
            var temp = $.htmlEngine.returnify(text);
            temp = $.jCommon.string.replaceAll(temp, "<br>", " ");
            var parts = temp.split(" ");
            var found = [];
            $.each(parts, function () {
                if(!$.jCommon.string.empty(this)) {
                    var part = this.toString();
                    if (($.jCommon.string.startsWith(part, "http://")
                        || $.jCommon.string.startsWith(part, "https://")) && !$.jCommon.array.contains(found, part)) {
                        found.push(part);
                    }
                }
            });

            $.each(found, function () {
                var part = this.toString();
                var urlRegex = /(http?:\/\/[^\s]+)/g;
                if ($.jCommon.string.contains(text, "https")) {
                    urlRegex = /(https?:\/\/[^\s]+)/g;
                }
                text = text.replace(urlRegex, '<a href="' + part + '" target="_blank">' + part + '</a>');
            });
        }
        return text;
    };

    $.htmlEngine.commingSoon = function (msg) {
        var j = dCrt('div').addClass('jumbotron').css({margin: '10px 10px'});
        var h = dCrt('h1').html('Coming Soon!');
        j.append(h);
        if(msg){
            var p = dCrt('p').html(msg);
            j.append(p);
        }
        return j;
    };

    $.htmlEngine.msg = function (node, title, msg) {
        var c = dCrt('div');
        if(title){
            var t = dCrt('h4').html(title).addClass('letterpress');
            c.append(t);
        }
        if(msg) {
            var m = dCrt('p').html(msg);
            c.append(m);
        }
        if(node.height()<100) {
            node.append(c).css({width: '100%', height: '100px'});
        }
        var d = $.jCommon.element.getDimensions(c);
        c.css({position: 'absolute', top: '50%', left: '50%', marginTop: ((d.h/2)*-1)+'px', marginLeft: ((d.w/2)*-1)+'px', textAlign: 'center'});
    };

    //Default Settings
    $.htmlEngine.defaults = {
        some: 'default values'
    };

    $.htmlEngine.compliant = function(item){
        var clr;var tip;var lbl;var glyph;var fclr;
        if($.jCommon.string.contains(item.relatedId, 'electronic_network_acas_invalid_asset')){
            item.compliant = 'unknown';
        }
        if(!item.compliant) {
            item.compliant = 'unknown';
        }
        var lwd = item.compliant ? item.compliant.toLowerCase() : "";
        lwd = $.jCommon.string.replaceAll(lwd, " ", "_");
        switch (lwd) {
            case 'yes':
                fclr = 'font-green-dk';
                lbl = "Yes";
                clr = 'dark-green';
                tip = 'Yes, HBC compliant ';
                glyph = 'glyphicons-ok';
                break;
            case 'no':
                lbl = "No";
                fclr = 'font-red-dk';
                clr = 'dark-red';
                tip = 'No, HBC non-compliant';
                glyph = 'glyphicons-remove';
                break;
            case 'unauthorized':
                lbl = "Unauthorized";
                fclr = 'high-font';
                clr = 'high';
                tip = 'Unauthorized, This operating system is unauthorized and is no longer supported';
                glyph = 'glyphicons-ban-circle';
                break;
            case 'no_policy':
                lbl = "No Policy";
                fclr = 'highlight-font';
                clr = 'highlight';
                tip = "No Policy, No known policy for this operating system";
                glyph = 'glyphicons-exclamation-sign';
                break;
            default:
                lbl = "Unknown";
                fclr = 'font-grey-md';
                clr = 'med-grey';
                tip = "Unknown, No known operating system for this device";
                glyph = 'glyphicons glyphicons-question-sign';
                break;
        }
        return {clr: clr, tip: tip, label: lbl, fclr: fclr, glyph: glyph};
    };

    $.htmlEngine.plugins = {
        register: function (name, script) {
            if(!$.htmlEngine.plugins.data){
                $.htmlEngine.plugins.data = {};
            }
            $.htmlEngine.plugins.data[name] = script;
        },
        get: function(name){
            var r;
            if($.htmlEngine.plugins.data){
                r = $.htmlEngine.plugins.data[name];
            }
            return r;
        }
    };


    //Plugin Function
    $.fn.htmlEngine = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.htmlEngine($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $htmlEngine = $(this).data('htmlEngine');
            switch (method) {
                case 'update': $htmlEngine.update(options);break;
                case 'fromData': return $htmlEngine.fromData();break;
                case 'state':
                default: return $htmlEngine;
            }
        }
    };

})(jQuery);
