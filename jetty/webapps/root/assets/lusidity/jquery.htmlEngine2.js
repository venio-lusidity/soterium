;(function ($) {

    //Object Instance
    $.htmlEngine = function(el, options) {
        var state = el,
            methods = {};
        state.opts = $.extend({}, $.htmlEngine.defaults, options);
        state.workers = {};
        state.isUpdating = false;
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
                methods.handler.init(state, state.workers, state.opts.schema.schemas, state.opts.data);
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
                        var obj = null;
                        //  get the object that we need to work with.
                        switch(this.object){
                            case 'state':
                                obj = state;break;
                            case 'worker':
                                obj = worker;break;
                        }
                        //  get the value of the property
                        this.actual = $.jCommon.json.getProperty(obj, this.valueOf, this.valueType);
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
                    return ($.jCommon.json.hasProperty(schema, "value.hrefProperty")
                    && $.jCommon.json.hasProperty(data, schema.hrefProperty)) ?
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
                                worker.node.attr("excluded", "true")
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
                        if ($.jCommon.is.string(worker.data)) {
                            if (!methods.data.isExcluded(worker, worker.data)) {
                                methods.handle.string(worker);
                            }
                        }
                        else if (worker.data && ($.jCommon.is.array(worker.data)
                            || (worker.data.results && $.jCommon.is.array(worker.data.results)))) {
                            methods.handle.array(worker);
                        }
                        else if ($.jCommon.is.object(worker.data)) {
                            if (worker.schema.load || $.jCommon.json.hasProperty(worker, "data.edgeUri")
                                || $.jCommon.json.hasProperty(worker, "data.vertexId")) {
                                methods.handle.load(worker);
                            }
                            else {
                                if (!worker.schema.load && !$.jCommon.json.hasProperty(worker, "data.edgeUri")) {
                                    methods.handle.object(worker);
                                }
                            }
                        }
                    }
                }
            },
            handle:{
                load: function(worker){
                    var url = worker.data[state.KEY_ID] + "/properties" + worker.schema.load;
                    if(worker.schema.direction){
                        url += "?direction=" + worker.schema.direction;
                    }
                    function isLoading(loading){
                        worker.node.attr('data-loading', loading);
                        if(worker.node.parent()){
                            worker.node.parent().attr('data-loading', loading);
                        }
                    }
                    worker.node.parent().attr('data-loading', true);
                    worker.node.attr('data-valid', true).show();

                    if(!$.jCommon.string.empty(url)) {
                        var f = function (jqXHR, textStatus, errorThrown) {
                            worker.parentNode.attr('data-loading', false).hide();
                        };
                        var s = function (data) {
                            if(data && data.results && data.results.length>0){
                                methods.checkDebug(worker);
                                $.each(data.results, function(){
                                    methods.handler.init(worker.node, [], worker.schema.schemas, this, worker.data);
                                    if(worker.schema.maxItems === 1){
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
                    else{
                        isLoading(false);
                    }
                },
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
                        $.each(data, function(){
                            if($.jCommon.json.hasProperty(worker, "schema.maxItems")
                                && on === worker.schema.maxItems){
                                return false;
                            }
                            var item  = $.extend(true, {}, worker);
                            item.data = this;
                            item.parentData = worker.parentData;
                            methods.layouts._default(item);
                        });
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
                                        methods.handler.init(worker.node, worker.workers, worker.schema.schemas, value, data);
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
                                methods.handler.init(worker.node, [], worker.schema.schemas, data, data);
                                return false;
                            }
                            else {
                                text = $.jCommon.is.string(data) ? data : $.htmlEngine.getString(data);
                            }
                            if (!$.jCommon.string.empty(text)) {
                                var item = $.extend(true, {}, worker);
                                item.data = text;
                                methods.layouts._default(item);
                                worker.node.attr('data-valid', true).show();
                            }
                        }
                    }
                },
                string: function(worker){
                    var text = methods.data.getString(worker.data);
                    var url = $.jCommon.url.create(text);
                    if(url.isUrl || $.jCommon.json.hasProperty(worker, "schema.hrefQuery")){
                        methods.layouts.hyperLink(worker);
                        return false;
                    }
                    if(!$.jCommon.string.empty(text)){
                        if(!worker.schema.dataType){
                            worker.schema.dataType = 'string'
                        }
                        switch (worker.schema.dataType) {
                            case 'base64':
                                text = atob(text);
                                break;
                            case 'date':
                            case 'time':
                                var result;
                                if($.jCommon.json.hasProperty(worker, "schema.format")){
                                    result = $.jCommon.dateTime.format(text, worker.schema.format);
                                }
                                else{
                                    result = $.jCommon.dateTime.minutesToHours(text);
                                }
                                text = (!result) ? '' : result;
                                break;
                            case 'measurement':
                                if($.jCommon.json.hasProperty(worker, "schema.format")){
                                    text = $.jCommon.number.convert(text, worker.schema.format);
                                }
                                break;
                            case 'maxChars':
                            default:
                                if($.jCommon.json.hasProperty(worker, "schema.maxChars")
                                    && $.jCommon.is.numeric(worker.schema.maxChars)
                                    && (text.length > worker.schema.maxChars)){
                                    text = $.jCommon.string.ellipsis(text, worker.schema.maxChars, worker.schema.trimLeft);
                                }
                                break;
                        }
                        if(!$.jCommon.string.contains(worker.node.html(), text)){
                            if($.jCommon.json.hasProperty(worker, "schema.favIcon")){
                                var urlFav = $.jCommon.url.create(
                                    $.jCommon.is.object(worker.schema.favIcon)
                                        ? worker.schema.favIcon.src : (methods.data.getString(state.opts.data[lusidity.uriKey])));
                                if(urlFav.isUrl){
                                    var img = $.htmlEngine.createElement({type: 'img', cls: 'fav'}, null, worker.data);
                                    if($.jCommon.is.object(worker.schema.favIcon)){
                                        $.htmlEngine.addStyling(worker.schema.favIcon, img);
                                    }
                                    img.attr('src', urlFav.favIcon);
                                    var link = $(document.createElement('a')).attr('href', urlFav.original);
                                    link.append(img);
                                    link.insertBefore(worker.node);
                                    link.append(worker.node);
                                }
                            }
                            worker.node.html(text);
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
                init: function(parentNode, workers, schemas, data, parentData){
                    $.each(schemas, function () {
                        var schema = this;
                        var worker = workers[schema.id];
                        if (worker === undefined) {
                            if (!schema.id) {
                                schema.id = methods.getRandomId("schema");
                            }
                            workers[schema.id] = {
                                schema: schema,
                                node: null,
                                workers: {},
                                lastValue: null,
                                data: data,
                                parentNode: parentNode
                            };
                            worker = workers[schema.id];
                        }

                        if ($.jCommon.is.array(data) && !$.jCommon.json.hasProperty(data[0], schema.property)) {
                            worker.data = null;
                        }
                        else if ($.jCommon.is.object(data)) {
                            worker.parentData = (undefined !== parentData && null !== parentData) ? parentData : data;
                            worker.data = $.jCommon.json.getProperty(data, worker.schema.property);
                            if(null===worker.data){
                                worker.data = data.results ? data.results : data;
                            }
                        }
                        methods.handler.layouts[worker.schema.layout](worker);
                    });
                },
                layouts: {
                    html: function(worker){
                        // TODO: this node needs to have some kind of check for updating.
                        worker.node = $.htmlEngine.createElement(worker.schema);
                        worker.parentNode.append(worker.node);
                        if(worker.schema.text){
                            worker.node.html(worker.schema.text);
                        }
                        else if(worker.schema && worker.schema.schemas){
                            var cd = $.jCommon.json.getProperty(worker.data, worker.schema.property);
                            methods.handler.init(worker.node, worker.workers, worker.schema.schemas, cd, worker.data);
                        }
                    },
                    data: function(worker){
                        worker.node = $.htmlEngine.createElement(worker.schema);
                        worker.parentNode.append(worker.node);
                        methods.data.transform(worker);
                        if(worker.schema && worker.schema.schemas){
                            var cd = $.jCommon.json.getProperty(worker.data, worker.schema.property);
                            methods.handler.init(worker.node, worker.workers, worker.schema.schemas, cd, worker.data);
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
                        else if ($.jCommon.is.object(worker.data)) {
                            if (worker.schema.load) {
                                methods.handle.load(worker);
                            }
                            else if(worker.schema.schemas) {
                                methods.handler.init(worker.node, [], worker.schema.schemas, worker.data, worker.parentData);
                            }
                        }
                    }
                },
                hyperLink: function(worker){
                    var href = methods.data.getHyperlink(worker.data, worker.schema);
                    if(href===null && worker.parentData)
                    {
                        href = methods.data.getHyperlink(worker.parentData, worker.schema);
                    }

                    if(null!==href)
                    {
                        href = $.jCommon.url.create($.htmlEngine.getString(href));
                    }

                    var text = $.jCommon.is.string(worker.data) ? worker.data :
                        ($.jCommon.json.hasProperty(worker, "schema.property") && $.jCommon.json.hasProperty(worker.data, worker.schema.property)) ?
                            $.htmlEngine.getString(worker.data[worker.schema.property]) : $.htmlEngine.getString(worker.data);

                    if(!$.jCommon.string.empty(text) && $.jCommon.element.contains(worker.node, text))
                    {
                        // this value is has already been set.
                        return false;
                    }

                    if($.jCommon.string.empty(text))
                    {
                        text = 'unknown';
                    }

                    if(null===href || (!href.isUrl && worker.schema.hrefQuery))
                    {
                        var oldRef = worker.node.attr('href');
                        var newRef = worker.schema.hrefQuery + text;
                        if($.jCommon.string.equals(oldRef, newRef, true))
                        {
                            return false;
                        }
                        href = $.jCommon.url.create(newRef);
                    }

                    if($.jCommon.json.hasProperty(worker, "schema.maxChars")
                        && $.jCommon.is.numeric(worker.schema.maxChars)
                        && (text.length > worker.schema.maxChars)){
                        text = $.jCommon.string.ellipsis(text, worker.schema.maxChars, worker.schema.trimLeft);
                    }

                    if(href.isUrl){
                        var link = $(worker.node.find('a[href="' + href.original + '"]'));
                        if(link.length === 0){

                            link = worker.node.is('a') ? worker.node : null;
                            if(null === link){
                                link = $.htmlEngine.createElement({type: 'a'}, worker.schema, worker.data);
                                worker.node.append(link);
                            }
                            link.attr('href', href.original);
                            var img;
                            if($.jCommon.json.hasProperty(worker, "schema.favIcon")
                                && worker.schema.favIcon){
                                img = $.htmlEngine.createElement({type: 'img', cls: 'fav'}, null, worker.data);
                                if($.jCommon.is.object(worker.schema.favIcon)){
                                    $.htmlEngine.addStyling(worker.schema.favIcon, img);
                                    img.attr('src', worker.schema.favIcon.src);
                                }
                                else{
                                    img.attr('src', href.favIcon);
                                }
                                link.append(img);
                            }
                            if(!worker.schema.favIconOnly && !$.jCommon.string.empty(text)){
                                link.append($.htmlEngine.createElement(
                                    {type: 'div', cls: "link-text"}, null, worker.data
                                ).html(text));
                            }
                            worker.node.attr('data-valid', true).show();
                        }
                    }
                    else if(!$.jCommon.string.empty(text)){
                        if(!$.jCommon.element.contains(worker.node, text)){
                            worker.node.html(text);
                            worker.node.attr('data-valid', true).show();
                        }
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
                result = $(document.createElement('div')).addClass('loading-indicator')
                    .css({position: 'relative', width: options.width + 'px', height: options.height + 'px'});
                var img = $(document.createElement('img'))
                    .css({width: options.width + 'px', height: options.height + 'px'})
                    .attr('src', '/assets/img/loader/loader_blue_32.gif');
                result.append(img);
                break;
        }
        return result;
    };
    $.htmlEngine.noImage = function(img, textOnly){
        if(!textOnly){
            img.attr('src', '/assets/img/no-image.png').addClass('is-portrait').removeClass('is-landscape');
        }
        else{
            img.parent().parent().css({position: 'relative'});
            var noImage = $(document.createElement('div')).html('No Image Available').addClass('no-image');
            noImage.insertBefore(img.parent());
            img.parent().remove();
        }
    };

    $.htmlEngine.getString = function(itemValue){
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
        var wrapper = $(document.createElement('div')).addClass('input-remove');
        var input = $(document.createElement('input'));
        var icon = $(document.createElement('span'));
        icon.addClass('glyphicon glyphicon-remove').attr("aria-hidden", "true");
        var clear = $(document.createElement('div')).addClass('input-clear').hide().append(icon);
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
        var result = (schema.html) ? $(schema.html) : $(document.createElement(((schema && schema.type) ? schema.type : 'div')));
        if(!schema.type){
            schema.type = result[0].tagName.toLowerCase();
        }
        $.htmlEngine.addStyling(formating, result);
        $.htmlEngine.addStyling(schema, result);
        $.htmlEngine.addAttributes(schema, result);
        $.htmlEngine.addAttributes(formating, result);
        return result;
    };

    $.htmlEngine.loadFiles = function(elem, name, files){
        if(!$.jCommon.string.empty(name) || $.jCommon.is.array(files)) {
            var loaded = false;
            var onLoaded = function () {
                if (elem && elem.length > 0) {
                    var e = jQuery.Event("cssFileLoaded");
                    elem.trigger(e);
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

    $.htmlEngine.setElementType =  function(schema)
    {
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
                element.attr(key, value);
            });
        }
    };


    $.htmlEngine.listen = {
        added: function(elem){
            var last = elem.children().length;

            var listen = function(){
                window.setTimeout(function(){
                    check();
                },250);
            };

            var check = function(){
                var current = elem.children().length;
                if(current>last || current<last){
                    var e = jQuery.Event("elementAdded");
                    e.added = $(elem.children[last]);
                    e.current = current;
                    e.last = last;
                    elem.trigger(e);
                    last = current;
                }
                listen();
            };
            listen();
        },
        hasHtml: function(elem){
            var stop = false;

            var listen = function(){
                window.setTimeout(function(){
                    check();
                },250);
            };

            var check = function() {
                if (!$.jCommon.string.empty(elem.html())) {
                    var e = jQuery.Event("elementHasHTML");
                    elem.trigger(e);
                    stop=true;
                }

                if(!stop){
                    listen();
                }
            };

            listen();
        },
        loaded: function(elem){
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

    $.htmlEngine.get = function (url, onSuccess, onFail, value, methodType, async) {
        if(undefined===async || null===async){
            async = true;
        }
        if (lusidity.info) {
            lusidity.info.hide();
        }
        //noinspection JSUnusedLocalSymbols
        var action = {
            connector: null,
            async: async,
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
                        lusidity.info.green("Although response was successful, I don't know what to do with it.");
                        lusidity.info.show(5);
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
                        lusidity.info.red('Sorry, we cannot find the property requested.&nbsp;&nbsp;'
                            + 'You can try refreshing the&nbsp;<a href="' + window.location.href + '">page</a>.');
                        lusidity.info.show(5);
                    }
                }
            },
            url: url
        };
        return lusidity.environment('request', action);
    };

    //Default Settings
    $.htmlEngine.defaults = {
        some: 'default values'
    };

    $.htmlEngine.plugins = {
        data: null,
        register: function (name, script) {
            if (null === $.htmlEngine.plugins.data) {
                $.htmlEngine.plugins.data = {};
            }
            if(!$.htmlEngine.plugins.data[name]) {
                $.htmlEngine.plugins.data[name] = script;
            }
        },
        get: function(name){
            return $.htmlEngine.plugins.data[name];
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
