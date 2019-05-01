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
                methods.createOrUpdate(state, state.workers, state.opts.schema.schemas, state.opts.data);
            },
            load: function(uri, onSuccess, onFailure){
                var failure = function (jqXHR, textStatus, errorThrown) {
                    if($.isFunction(onFailure)){
                        onFailure(jqXHR, textStatus, errorThrown);
                    }
                };
                var success = function (data) {
                        if($.isFunction(onSuccess)){
                            onSuccess(data);
                        }
                };
                lusidity.environment('getEntity', { url: uri, onSuccess: success, onFailure: failure});
            },
            log: function(text){
                if(state.isUpdating){
                    console.log(text);
                }
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
            createOrUpdate: function(parent, workers, schemas, data, parentData){
                $.each(schemas, function () {
                    var schema = this;
                    var worker = workers[schema.id];
                    if (worker === undefined) {
                        if(!schema.id){
                            schema.id = methods.getRandomId("schema");
                        }
                        workers[schema.id] = {
                            schema: null,
                            propertyNode: null,
                            keyNode: null,
                            valueNode: null,
                            workers: {},
                            lastValue: null,
                            parent: parent
                        };
                        worker = workers[schema.id];
                    }
                    worker.schema = schema;
                    worker.data = data;

                    if (schema.wrapper) {
                        if (!worker.propertyNode) {
                            worker.propertyNode = methods.html.createElement(schema.node, {cls: 'property'}, worker.data);
                            worker.propertyNode
                                .attr('data-property', worker.schema.property)
                                .attr('data-schema-id', worker.schema.id);
                            parent.append(worker.propertyNode);
                            methods.html.rmvCls(worker.propertyNode, worker.schema.node);

                            if ($.jCommon.json.hasProperty(worker, "schema.node.hrefProperty")
                                && worker.data
                                && worker.data[schema.node.hrefProperty]) {
                                if (worker.propertyNode.is('a')) {
                                    worker.propertyNode.attr('href', worker.data[schema.node.hrefProperty]);
                                }
                                else if (worker.schema.clickable) {
                                    worker.propertyNode.css({'cursor': 'pointer'});
                                    worker.propertyNode.on("click", function () {
                                        window.location = worker.data[schema.node.hrefProperty];
                                    });
                                }
                            }
                            if(schema.dataId && $.jCommon.json.hasProperty(worker.data, schema.dataId)){
                                worker.propertyNode.attr('data-id', worker.data[schema.dataId]);
                            }
                            methods.plugins.before(worker);
                        }
                        if (schema.value) {
                            if (!worker.valueNode) {
                                if (!worker.schema.value) {
                                    worker.schema.value = {};
                                }
                                worker.valueNode = methods.html.createElement(worker.schema.value, {cls: 'value'}, worker.data);
                                worker.propertyNode.append(worker.valueNode);
                                methods.html.rmvCls(worker.valueNode, worker.schema.value);
                            }
                            methods.data.transform(worker);
                        }
                        if (schema.schemas) {
                            methods.createOrUpdate(worker.propertyNode, worker.workers, schema.schemas, data, data);
                        }
                        $.each(worker.propertyNode.children(), function () {
                            var that = $(this);
                            if (that.attr('data-valid') === 'true') {
                                worker.propertyNode.attr('data-valid', true).show();
                                return false;
                            }
                        });

                    }
                    else {
                        if ($.jCommon.is.array(data) && !$.jCommon.json.hasProperty(data[0], schema.property)) {
                            worker.data = null;
                        }
                        else if ($.jCommon.is.object(data)) {
                            worker.parentData = (undefined !== parentData && null !== parentData) ? parentData : data;
                            worker.data = $.jCommon.json.getProperty(data, worker.schema.property);
                            if(null==worker.data){
                                worker.data = data.results ? data.results : data;
                            }
                        }
                        methods.html.createOrUpdate(worker);
                    }

                    if (state.opts.autoHide && worker.propertyNode.attr('data-valid') === 'false') {
                        worker.propertyNode.hide();
                    }
                    else {
                        worker.propertyNode.show();
                    }
                });

                if(state.opts.autoHide) {
                    methods.autoHide();
                }
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
                                        if(null==obj2)
                                        {
                                            result = (null == obj1) || (undefined == obj1);
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
                        && $.jCommon.json.hasProperty(data, schema.value.hrefProperty)) ?
                        data[schema.value.hrefProperty] : ($.jCommon.is.string(data)) ?
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
                    if (worker.schema.value.exclusions && $.jCommon.is.string(excludedValue)) {
                        $.each(worker.schema.value.exclusions, function () {
                            if ($.jCommon.string.contains(excludedValue, this, true)){
                                worker.propertyNode.attr("excluded", "true")
                                excluded = true;
                                return false;
                            }
                        });
                    }
                    return excluded;
                },
                isValid: function(worker){
                    var valid = ($(worker.data).length>0);
                    if(valid && worker.schema.value && worker.schema.value.property){
                        valid = (worker.data[worker.schema.value.property] != undefined);
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
                    if(!worker.schema.layout || worker.schema.layout.toLowerCase() === 'default'){
                        worker.schema.layout = '_default';
                    }
                    try {
                        methods.layouts[worker.schema.layout.toLowerCase()](worker);
                    }
                    catch(e){
                        console.log(worker.schema.layout.toLowerCase() + ': ' + e.message);
                    }
                }
            },
            getRandomId: function(prefix){
                return $.htmlEngine.getRandomId(prefix);
            },
            html: {
                addAttributes: function(schema, element, data){
                   return $.htmlEngine.addAttributes(schema, element, data);
                },
                addStyling: function(schema, element, data){
                    return $.htmlEngine.addStyling(schema, element, data);
                },
                createOrUpdate: function(worker){
                    if(!worker.propertyNode){
                        worker.propertyNode = methods.html.createElement(worker.schema.node, {cls : 'property'}, worker.data);
                        worker.propertyNode
                            .attr('data-property', worker.schema.property)
                            .attr('data-schema-id', worker.schema.id);
                        worker.propertyNode.attr('data-valid', false);
                        worker.parent.append(worker.propertyNode);
                        methods.html.rmvCls(worker.propertyNode, worker.schema.node);

                        if(worker.schema.dataId && $.jCommon.json.hasProperty(worker.data, worker.schema.dataId)){
                            worker.propertyNode.attr('data-id', worker.data[worker.schema.dataId]);
                        }
                    }
                    if(!worker.keyNode && worker.schema.key){
                        worker.keyNode = methods.html.createElement(worker.schema.key, {cls : 'key'}, worker.data);
                        worker.propertyNode.append(worker.keyNode);
                        methods.html.rmvCls(worker.keyNode, worker.schema.key);
                    }

                    if(worker.keyNode && $.jCommon.string.empty(worker.keyNode.html())){
                        worker.keyNode.html((worker.schema.key.property) ?
                            ((worker.data && worker.data[worker.schema.key.property]) ?
                                worker.data[worker.schema.key.property]:'')
                            : worker.schema.key.title);
                    }

                    if(!worker.valueNode)
                    {
                        if(!worker.schema.value){
                            worker.schema.value = {};
                        }
                        worker.valueNode = methods.html.createElement(worker.schema.value, {cls: 'value'}, worker.parentData);
                        worker.propertyNode.append(worker.valueNode);
                        methods.html.rmvCls(worker.valueNode, worker.schema.value);
                    }
                    methods.data.transform(worker);

                    if($.jCommon.json.hasProperty(worker, "schema.value.replacedBy.types")){
                        var exists = false;
                        if(state.opts.data
                            && $.jCommon.json.hasProperty(state, 'opts.data.instanceOf.values')){
                            $.each(state.opts.data.instanceOf.values, function(){
                                if($.jCommon.string.contains(this.identifier, worker.schema.value.replacedBy.types, true)){
                                    exists = true;
                                    return false;
                                }
                            });
                            if(exists){
                                var apply = true;
                                if($.jCommon.json.hasProperty(worker, "schema.value.replacedBy.conditions")){
                                    apply = methods.data.compare(worker.schema.value.replacedBy.conditions, worker);
                                }
                                if(apply){
                                    worker.schema = worker.schema.value.replacedBy;
                                    methods.data.transform(worker);
                                }
                            }
                        }
                    }

                    if(worker.valueNode){
                        $.each(worker.valueNode.children(), function(){
                            var that = $(this);
                            if(that.attr('data-valid') === 'true'){
                                worker.propertyNode.attr('data-valid', true).show();
                                return false;
                            }
                        });
                    }
                },
                createElement: function(schema, formating, data){
                    return $.htmlEngine.createElement(schema, formating, data);
                },
                rmvCls: function(elem, schema){
                    if(schema && $.jCommon.is.array(schema.rmvCls)){
                        $.each(schema.rmvCls, function(){
                            $(elem).removeClass(this.toString());
                        });
                    }
                },
                setElementType: function(schema)
                {
                    return $.htmlEngine.setElementType(schema);
                }
            },
            hasPropertyAndValue: function(worker){

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
                custom: function(worker){
                    if(worker.schema.value){
                        var elem = methods.html.createElement(worker.schema.value, null, null);
                        if(elem.is('img')){
                            elem.hide();
                            var src = elem.attr('src');
                            elem.bind('imageLoaded', function(){
                                elem.show();
                            });
                            $.jCommon.image.setClass(elem, src, 3, false);
                        }
                        worker.valueNode.append(elem);
                        worker.propertyNode.attr('data-valid', true).show();
                    }
                },
                html: function(worker){
                    if(worker.schema.html || worker.schema.value.html) {
                        var html = (worker.schema.html) ? worker.schema.html : worker.schema.value.html;
                        worker.valueNode.append(html);
                        worker.valueNode.removeClass("value");
                        if(null!=html && undefined!=html) {
                            worker.propertyNode.attr('data-valid', true).show();
                            var property = (worker.schema.dependsOn) ? worker.schema.dependsOn : worker.schema.value.dependsOn;
                            if (property) {
                                var value = $.jCommon.json.getProperty(worker.data, property);
                                var show = (null != value && undefined != value);
                                worker.propertyNode.attr('data-valid', show);
                                if (!show) {
                                    worker.propertyNode.hide();
                                }
                            }
                        }
                        else{
                            worker.propertyNode.attr('data-valid', false).hide();
                        }
                    }
                },
                collapse: function(worker){
                    if(!worker.schema.value.collapse.mobileOnly ||
                        $.environment.isMobile()){

                        worker.valueNode.ready(function(){
                            window.setTimeout(function (){
                                resize();
                            }, 100);
                        });
                        function resize() {
                            var height = worker.valueNode.innerHeight();
                            if (height > 0) {
                                var options = {
                                    speed: 300,
                                    collapsedHeight: worker.schema.value.collapse.collapsedHeight
                                };
                                worker.valueNode.readMore(options);
                            }
                            else{
                                window.setTimeout(function (){
                                    resize();
                                }, 100);
                            }
                        }
                    }
                },
                _default: function(worker) {
                    if (worker.data === undefined) {
                        return false;
                    }

                    if((worker.schema.invoke && worker.schema.invoke.before)) {
                        methods.plugins.before(worker);
                    }
                    else if((worker.schema.invoke && worker.schema.invoke.after)) {
                        methods.plugins.after(worker);
                    }
                    else {
                        if ($.jCommon.is.string(worker.data)) {
                            if (!methods.data.isExcluded(worker, worker.data)) {
                                methods.layouts.handle.string(worker);
                            }
                        }
                        else if (worker.data && ($.jCommon.is.array(worker.data)
                            || (worker.data.results && $.jCommon.is.array(worker.data.results)))) {
                            methods.layouts.handle.array(worker);
                        }
                        else if ($.jCommon.is.object(worker.data)) {
                            if (worker.schema.load || $.jCommon.json.hasProperty(worker, "data.edgeUri")
                                || $.jCommon.json.hasProperty(worker, "data.vertexId")) {
                                methods.layouts.handle.load(worker);
                            }
                            else {
                                if (!worker.schema.load
                                    && !$.jCommon.json.hasProperty(worker, "data.edgeUri")
                                    && !methods.plugins.instead(worker)) {
                                    methods.layouts.handle.object(worker);
                                }
                            }
                        }
                    }
                },
                handle:{
                    load: function(worker){

                        var entityUrl = methods.data.getString(
                            methods.data.getString(worker.data[worker.schema.load ? worker.schema.load :
                                $.jCommon.json.hasProperty(worker.data, "edgeUri") ? "edgeUri" :
                                    $.jCommon.json.hasProperty(worker.data, "vertexId") ? "vertexId": null]));

                        if($.jCommon.json.hasProperty(worker, "data.ordinal")){
                            worker.propertyNode.attr("data-ordinal", worker.data.ordinal);
                        }
                        function isLoading(loading){
                            worker.propertyNode.attr('data-loading', loading);
                            if(worker.propertyNode.parent()){
                                worker.propertyNode.parent().attr('data-loading', loading);
                            }
                        }
                        isLoading(true);
                        worker.propertyNode.attr('data-valid', true).show();

                        if(!$.jCommon.string.empty(entityUrl)) {
                            var onFailure = function (jqXHR, textStatus, errorThrown) {
                                isLoading(false);
                            };

                            var onSuccess = function (data) {
                                methods.checkDebug(worker);

                                isLoading(false);
                                var working = $.extend(true, {}, worker);

                                working.data = {};
                                var prop = worker.schema.property;
                                if ($.jCommon.string.contains(prop, '.')) {
                                    prop = $.jCommon.string.getLast(prop, '.');
                                }

                                working.data[prop] = data;

                                if (working.schema.load) {
                                    delete working.schema.load;
                                }

                                if (!methods.plugins.instead(working)) {
                                    methods.layouts._default(working);
                                }

                                if (state.opts.autoHide) {
                                    methods.autoHide();
                                }
                            };

                            lusidity.environment('getEntity', { url: entityUrl, onSuccess: onSuccess, onFailure: onFailure, async: true});
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
                        if ($.jCommon.json.hasProperty(worker, "schema.value.conditions")) {
                            apply = methods.data.compare(worker.schema.value.conditions, worker);
                        }
                        if (apply) {
                            if ($.jCommon.json.hasProperty(worker, "schema.value.hrefProperty")
                                || $.jCommon.json.hasProperty(worker, "schema.value.hrefQuery")) {
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
                                if ($.jCommon.json.hasProperty(worker, "schema.value.property")) {
                                    var value = $.jCommon.json.getProperty(data, worker.schema.value.property);

                                    if (null !== value && undefined !== value) {
                                        if ($.jCommon.json.hasProperty(worker, "schema.value.schemas")) {
                                            methods.createOrUpdate(worker.valueNode, worker.workers, worker.schema.value.schemas, value, data);
                                            worker.propertyNode.attr('data-valid', true).show();
                                            return false;
                                        }
                                        else {
                                            text = $.htmlEngine.getString(value);
                                        }
                                    }
                                    else{
                                        worker.propertyNode.attr('data-valid', false).hide();
                                    }
                                }
                                else if ($.jCommon.json.hasProperty(worker, "schema.value.schemas")) {
                                    methods.createOrUpdate(worker.valueNode, [], worker.schema.value.schemas, data, data);
                                    return false;
                                }
                                else {
                                    text = $.jCommon.is.string(data) ? data : $.htmlEngine.getString(data);
                                }
                                if (!$.jCommon.string.empty(text)) {
                                    var item = $.extend(true, {}, worker);
                                    item.data = text;
                                    methods.layouts._default(item);
                                    worker.propertyNode.attr('data-valid', true).show();
                                }
                            }
                        }
                    },
                    string: function(worker){
                        var text = methods.data.getString(worker.data);
                        var url = $.jCommon.url.create(text);
                        if(url.isUrl || $.jCommon.json.hasProperty(worker, "schema.value.hrefQuery")){
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
                                default:
                                    if($.jCommon.json.hasProperty(worker, "schema.value.maxChars")
                                        && $.jCommon.is.numeric(worker.schema.value.maxChars)
                                        && (text.length > worker.schema.value.maxChars)){
                                        text = $.jCommon.string.ellipsis(text, worker.schema.value.maxChars, worker.schema.value.trimLeft);
                                    }
                                    break;
                            }
                            if(!$.jCommon.string.contains(worker.valueNode.html(), text)){
                                if($.jCommon.json.hasProperty(worker, "schema.value.favIcon")){
                                    var urlFav = $.jCommon.url.create(
                                        $.jCommon.is.object(worker.schema.value.favIcon)
                                            ? worker.schema.value.favIcon.src : (methods.data.getString(state.opts.data[lusidity.uriKey])));
                                    if(urlFav.isUrl){
                                        var img = methods.html.createElement({type: 'img', cls: 'fav'}, null, worker.data);
                                        if($.jCommon.is.object(worker.schema.value.favIcon)){
                                            methods.html.addStyling(worker.schema.value.favIcon, img);
                                        }
                                        img.attr('src', urlFav.favIcon);
                                        var link = $(document.createElement('a')).attr('href', urlFav.original);
                                        link.append(img);
                                        link.insertBefore(worker.valueNode);
                                        link.append(worker.valueNode);
                                    }
                                }
                                worker.valueNode.html(text);
                                worker.propertyNode.attr('data-valid', true).show();
                            }
                        }
                    },
                    toggleBlock: function(worker){
                        var block = $(worker.propertyNode.closest('.block'));
                        var isValid = (worker.propertyNode.attr('data-valid')  !== undefined
                            && worker.propertyNode.attr('data-valid') === "true");
                        if(isValid){
                            worker.propertyNode.show();
                            if(block.length>0 && !block.is(':visible')){
                                block.slideDown(300);
                            }
                        }
                        else{
                            worker.propertyNode.hide();
                        }
                        if(state.opts.autoHide) {
                            methods.autoHide();
                        }
                    }
                },
                hyperLink: function(worker){
                    var href=null;

                    href = methods.data.getHyperlink(worker.data, worker.schema);
                    if(href==null && worker.parentData)
                    {
                        href = methods.data.getHyperlink(worker.parentData, worker.schema);
                    }

                    if(href==null
                        && $.jCommon.json.hasProperty(worker, "schema.property")
                        && $.jCommon.json.hasProperty(worker.data, worker.schema.property)){

                    }


                    if(null!=href)
                    {
                        href = $.jCommon.url.create($.htmlEngine.getString(href));
                    }

                    var text = $.jCommon.is.string(worker.data) ? worker.data :
                        ($.jCommon.json.hasProperty(worker, "schema.property") && $.jCommon.json.hasProperty(worker.data, worker.schema.property)) ?
                        $.htmlEngine.getString(worker.data[worker.schema.property]) : $.htmlEngine.getString(worker.data);

                    if(!$.jCommon.string.empty(text) && $.jCommon.element.contains(worker.valueNode, text))
                    {
                        // this value is has already been set.
                        return false;
                    }

                    if($.jCommon.string.empty(text))
                    {
                        text = 'unknown';
                    }

                    if(null==href || (!href.isUrl && worker.schema.value.hrefQuery))
                    {
                        var oldRef = worker.valueNode.attr('href');
                        var newRef = worker.schema.value.hrefQuery + text;
                        if($.jCommon.string.equals(oldRef, newRef, true))
                        {
                            return false;
                        }
                        href = $.jCommon.url.create(newRef);
                    }

                    if($.jCommon.json.hasProperty(worker, "schema.value.maxChars")
                        && $.jCommon.is.numeric(worker.schema.value.maxChars)
                        && (text.length > worker.schema.value.maxChars)){
                        text = $.jCommon.string.ellipsis(text, worker.schema.value.maxChars, worker.schema.value.trimLeft);
                    }

                    if(href.isUrl){
                        var link = $(worker.valueNode.find('a[href="' + href.original + '"]'));
                        if(link.length === 0){

                            link = worker.valueNode.is('a') ? worker.valueNode :
                                    worker.propertyNode.is('a') ? worker.propertyNode: null;
                            if(null == link){
                                link = methods.html.createElement({type: 'a'}, worker.schema.value, worker.data);
                                worker.valueNode.append(link);
                            }
                            link.attr('href', href.original);
                            var img;
                            if($.jCommon.json.hasProperty(worker, "schema.value.favIcon")
                                && worker.schema.value.favIcon){
                                img = methods.html.createElement({type: 'img', cls: 'fav'}, null, worker.data);
                                if($.jCommon.is.object(worker.schema.value.favIcon)){
                                    methods.html.addStyling(worker.schema.value.favIcon, img);
                                    img.attr('src', worker.schema.value.favIcon.src);
                                }
                                else{
                                    img.attr('src', href.favIcon);
                                }
                               link.append(img);
                            }
                            if(!worker.schema.value.favIconOnly && !$.jCommon.string.empty(text)){
                                link.append(methods.html.createElement(
                                    {type: 'div', cls: "link-text"}, null, worker.data
                                ).html(text));
                            }
                             worker.propertyNode.attr('data-valid', true).show();
                        }
                    }
                    else if(!$.jCommon.string.empty(text)){
                        if(!$.jCommon.element.contains(worker.valueNode, text)){
                            worker.valueNode.html(text);
                            worker.propertyNode.attr('data-valid', true).show();
                        }
                    }
                    else{
                        return false;
                    }
                },
                image: function(worker){
                    worker.valueNode.imageHandler(worker);
                },
                info: function(worker){
                    var url = (methods.data.getString(state.opts.data[lusidity.uriKey]));
                    if(!$.jCommon.string.empty(url)){
                        var options = {
                            url: url,
                            onSuccess: function(data){
                                if(data && $(data.Info).length>0){
                                    worker.data = data.Info;

                                    if(!worker.schema.value){
                                        worker.schema.value = {};
                                    }
                                    worker.schema.value.favIcon = true;
                                    worker.schema.value.hrefProperty = 'actionUri';
                                    methods.layouts._default(worker);
                                    methods.layouts.handle.toggleBlock(worker);
                                }
                            },
                            onFailure: function(jqXHR, textStatus, errorThrown){

                            }
                        };
                        lusidity.environment('getActions', options);
                    }
                },
                social: function(worker){
                    if(worker.valueNode.children().length==0){
                        var social = $('.social');
                        if(social.length>0){
                            social.addClass('inline-top').css({margin: '0 5px 5px 0'});
                            worker.valueNode.append(social);
                            worker.propertyNode.attr('data-valid', true).show();
                        }
                    }
                },
                indicators: function(worker){
                    if(worker.valueNode.children().length===0){
                        worker.valueNode.append($.htmlEngine.indicators(worker.schema.options));
                        worker.propertyNode.attr('data-valid', true).show();
                    }
                },
                type: function(worker){
                    var type = methods.data.getString(worker.data);
                    if(worker.valueNode.children().length === 0){
                        if (!$.jCommon.string.empty(type)) {
                            if (worker.schema.value.image) {
                                var img = methods.html.createElement({type: 'img', cls: 'fav', css: {marginTop: '-4px'}}, null, worker.data);
                                img.attr('src', worker.schema.value.image);
                                worker.valueNode.append(img);
                            }
                            worker.valueNode.append(
                                methods.html.createElement({type: 'div', css: { display: 'inline-block', margin: '2px 0 0 2px' }}, null, worker.data).html(type));
                             worker.propertyNode.attr('data-valid', true).show();
                        }
                    }
                },
                read: function(worker){
                    var url = (methods.data.getString(state.opts.data[lusidity.uriKey]));
                    if(!$.jCommon.string.empty(url)){
                        var options = {
                            url: url,
                            onSuccess: function(data){
                                if(data && data.Read && $(data.Read).length>0){
                                    worker.data = data.Read;
                                    if($.jCommon.json.hasProperty(worker, "schema.view")){
                                        var apply = true;
                                        if($.jCommon.json.hasProperty(worker, "schema.value.conditions")){
                                            apply = methods.data.compare(worker.schema.value.conditions, worker);
                                        }
                                        if(apply){
                                            if(!worker.valueNode.is('div')){
                                                var vn = methods.html.createElement({type: 'div', cls: 'read'}, null, worker.data);
                                                vn.insertBefore(worker.valueNode);
                                                worker.valueNode.remove();
                                                worker.valueNode = vn;
                                            }
                                            else{
                                                worker.valueNode.addClass('read');
                                            }
                                            worker.valueNode.children().remove();

                                            var url = $.jCommon.url.create(worker.data.uri);
                                            if(url.isUrl)
                                            {
                                                var link = methods.html.createElement({type: 'a', cls: "ol-read"}, null, worker.data);
                                                link.attr('target', "_blank");
                                                link.attr('href', worker.data.uri);

                                                var title = methods.html.createElement({type: 'div', cls: "inline"}, null, worker.data);
                                                title.html(worker.data.label).css({color: 'inherit'});
                                                link.append(title);

                                                if(worker.data.property) {
                                                    var srcUrl = $.jCommon.url.create(worker.data.property);
                                                    if(srcUrl.isUrl) {
                                                        var fav = methods.html.createElement(({type: 'img', cls: "fav"}), null, worker.data);
                                                        fav.attr("src", srcUrl.favIcon);

                                                        link.prepend(fav);
                                                    }
                                                }

                                                if($.jCommon.string.startsWith(worker.data.label, "checked out", true))
                                                {
                                                    link.addClass("bg-lt-red bdr-red");
                                                }
                                                else
                                                {
                                                    link.addClass("bg-lt-grey bdr-grey");
                                                }
                                                worker.valueNode.append(link);
                                            }
                                            worker.propertyNode.attr('data-valid', url.isUrl).show();
                                        }
                                    }

                                    methods.layouts.handle.toggleBlock(worker);
                                }
                            },
                            onFailure: function(jqXHR, textStatus, errorThrown){

                            }
                        };
                        lusidity.environment('getActions', options);
                    }
                }
            },
            plugins:{
                load: function() {
                    if (state.opts.schema.styles) {
                        $.each(state.opts.schema.styles, function(){
                            $.jCommon.load.css(this);
                        });
                    }
                    if(state.opts.schema.plugins){
                        $.each(state.opts.schema.plugins, function(){
                            $.jCommon.load.script(this);
                        });
                    }
                },
                invoke: function(worker, invoke){
                    try{
                        var plugin = $.htmlEngine.plugins.get(invoke.name);
                        if(plugin){
                            worker.plugin = invoke;
                            if(state.opts.data.instanceOf && state.opts.data.instanceOf.name){
                                worker.plugin.instanceOf = state.opts.data.instanceOf.name.toLowerCase();
                            }
                            plugin(worker.propertyNode, worker);
                        }
                    }catch(e){
                        console.log(e.message);
                    }
                },
                after: function(worker) {
                    var result = worker.schema.invoke && worker.schema.invoke.after;
                    if(result){
                        methods.plugins.invoke(worker, worker.schema.invoke.after);
                        if(worker.schema.invoke.after.once){
                            delete worker.schema.invoke.after;
                        }
                    }
                    return result;
                },
                instead: function(worker) {
                    var result = (worker.schema.invoke && worker.schema.invoke.instead);
                    if(result){
                        methods.plugins.invoke(worker, worker.schema.invoke.instead);
                        if(worker.schema.invoke.instead.once){
                            delete worker.schema.invoke.instead;
                        }
                    }
                    return result;
                },
                before: function(worker) {
                    var result = (worker.schema.invoke && worker.schema.invoke.before);
                    if(result){
                        methods.plugins.invoke(worker, worker.schema.invoke.before);
                        if(worker.schema.invoke.before.once){
                            delete worker.schema.invoke.before;
                        }
                    }
                    return result;
                }
            }
        };
        //public methods
        state.update = function(options){
            if(options.schema && options.data){
                state.isUpdating =true;
                state.opts.data = options.data;
                state.opts.schema = options.schema;
                methods.init();
            }
        };

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
        schema = $.htmlEngine.setElementType(schema);
        var result = $(document.createElement(((schema && schema.type) ? schema.type : 'div')));
        $.htmlEngine.addStyling(formating, result, data);
        $.htmlEngine.addStyling(schema, result, data);
        $.htmlEngine.addAttributes(schema, result, data);
        $.htmlEngine.addAttributes(formating, result, data);
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

    $.htmlEngine.addStyling = function(schema, element, data) {
        if (schema != undefined && schema instanceof Object) {
            if (schema.cls) {
                element.addClass(schema.cls);
            }
            if (schema.css) {
                element.css(schema.css);
            }
        }
    };

    $.htmlEngine.addAttributes = function(schema, element, data) {
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

    //Default Settings
    $.htmlEngine.defaults = {
        some: 'default values'
    };

    $.htmlEngine.plugins = {
        data: null,
        register: function (name, script) {
            if (null == $.htmlEngine.plugins.data) {
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
                new $.htmlEngine($(this),method);
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
    }

})(jQuery);
