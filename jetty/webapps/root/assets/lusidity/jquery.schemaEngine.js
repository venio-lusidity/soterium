; (function ($) {

    //Object Instance
    $.schemaEngine = function (el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.opts = {};
        if(options.isView)
        {
            state.opts = options;
        }
        else{
            state.opts.data = options;
        }
        state.properties = [];
        state.exlusions = null;

        // Store a reference to the environment object
        el.data("schemaEngine", state);

        // Private environment methods
        var thumbnail;
        var methods = {
            init: function () {
                if (state.opts.isView) {
                    state.opts.schema = methods.getView();
                }
                else {
                    methods.getSchemaForType();
                }
            },
            getView: function(){
                var view = state.opts.view;
                if(!$.jCommon.string.startsWith(view, "/"))
                {
                    view = "/" + view;
                }
                view = "views" + view;

                var vSchema = $.schemaEngine.loadSchema(view);
                if(!$.jCommon.is.object(vSchema))
                {
                    vSchema = {
                        "schemas": [{"entities": "entities" } ]
                    };
                }
                vSchema.schemas = methods.map(vSchema.schemas);

                if(vSchema.scripts && $.jCommon.is.array(vSchema.scripts))
                {
                    var body = $("body");
                    $.each(vSchema.scripts, function(){
                        if(this.src) {
                            var s = $(document.createElement("script"));
                            s.attr("src", this.src + appVersion);
                            s.attr("type", "text/javascript");
                            body.append(s);
                        }
                    });
                }
                return vSchema;
            },
            getSchemaForType: function()
            {
                var exists = false;
                var item = state.opts.data["vertexType"];
                var t = $.jCommon.string.getLast(item, "/");
                var title;


                if(!$.jCommon.string.empty(t))
                {
                    title = $.jCommon.string.camelCase(t, "_", " ");
                    state.opts.instanceOf = t.toLocaleLowerCase();
                    state.opts.data.instanceOf = {
                        "name": title,
                        "uri": item
                    };
                    state.opts.schema = state.getSchema(state.opts.instanceOf);
                    exists = (null !== state.opts.schema);
                }
                if(!exists)
                {
                    state.opts.instanceOf = 'entity';
                    state.opts.data.instanceOf = {
                        "name": "Entity",
                        "uri": "/type/entity"
                    };
                    state.opts.schema = state.getSchema(state.opts.instanceOf);
                    if(!$.jCommon.string.empty(title))
                    {
                        state.opts.data.instanceOf.name = title;
                    }
                }

                if(null===state.opts.schema){
                    state.opts.schema = {};
                }

                var imageName = $.jCommon.string.empty(state.opts.schema.image) ? state.opts.instanceOf.replace(' ', '') : state.opts.schema.image;
                state.opts.image = '/assets/img/types/' + imageName + '.png';
                if (!state.opts.schema) {
                    state.opts.schema = state.getSchema('topic');
                }
                if(state.opts.schema.zones){
                    $.each(state.opts.schema.zones, function(){
                        if($.jCommon.is.object(this)){
                            this.id = methods.getRandomId('zone');
                        }
                        if(this.schemas){
                            this.schemas = methods.map(this.schemas);
                        }
                    });
                }
                if(state.opts.schema.scripts && $.jCommon.is.array(state.opts.schema.scripts))
                {
                    var body = $("body");
                    $.each(state.opts.schema.scripts, function(){
                        if(this.src) {
                            var s = $(document.createElement("script"));
                            s.attr("src", this.src);
                            s.attr("type", "text/javascript");
                            body.append(s);
                        }
                    });
                }
            },
            getRandomId: function(prefix){
                var rn = function(){return Math.floor(Math.random()*999999);};
                return prefix + '_' + rn() + '_' + rn();
            },
            map: function(schemas) {
                var results = [];
                $.each(schemas, function () {
                    var schema = this;
                    if (this.common && $.jCommon.is.array(this.common)) {
                        $.each(this.common, function() {
                            var item = this;
                            schema = {
                                "id": methods.getRandomId("schema"),
                                "layout": "html",
                                "type": "div",
                                "schemas": [
                                    {
                                        "id": methods.getRandomId("schema"),
                                        "layout": "html",
                                        "html": "<div class=\"data-label\">" + item.label + "</div>"
                                    },
                                    {
                                        "id": methods.getRandomId("schema"),
                                        "layout": "data",
                                        "property": item.property,
                                        "type": "div",
                                        "dataType": item.dataType ? item.dataType : 'string',
                                        "format": item.format ? item.format : null,
                                        "cls": "data-value"
                                    }
                                ]
                            };
                            results.push(schema);
                        });
                    }
                    else {
                        schema.id = methods.getRandomId("schema");
                        if(schema.schemas){
                            schema.schemas = methods.map(schema.schemas);
                        }
                        results.push(schema);
                    }
                });
                return results;
            }
        };
        var dynamicType ={
            basic: {
                "property":"",
                "dataType": "string",
                "key": {
                    "title": "",
                    "cls": "key italics",
                    "css": {"color": "#f2dede"}
                }
            },
            entity:{
                "property":"",
                "value":{}
            }
        };
        state.getImage = function () {
            return state.opts.image;
        };
        state.getSchema = function (schemaType) {
            return $.schemaEngine.loadSchema(schemaType);
        };
        //environment: Initialize
        return methods.init();
    };
    $.schemaEngine.loadSchema = function (schemaType, callback) {
        schemaType = schemaType.replace(' ', '');
        var url = '/assets/lusidity/types/' + schemaType +
            (!$.jCommon.string.endsWith(schemaType, '.json', true) ? '.json' : '') +
                '?nocache=' + Math.floor(Math.random()*999999);
        var schema = null;
        $.ajax({
            async: false,
            url: url,
            type: 'get',
            dataType: 'json',
            success: function (data) {
                schema = data;
            },
            error: function (xhr, textStatus, errorThrown) {
            }
        });
        return schema;
    };

    $.schemaEngine.createEntityMap = function(data)
    {
        var results = [];
        data = $.jCommon.json.sortKeys(data);
        $.each(data, function(key, value){
            var add = false;
            if($.jCommon.is.array(value))
            {
                var test = value[0];
                add = $.jCommon.json.hasProperty(test, lusidity.uriKey);
            }
            else{
                add = $.jCommon.json.hasProperty(value, lusidity.uriKey);
            }

            if(add)
            {
                var title = $.jCommon.string.getLast(key, "/");
                title = $.jCommon.string.camelCase(title, "_", " ");
                var result = {key: key, title: title};
                results.push(result);
            }
        });

        results = { entities: results};

        return results;
    };
    //Plugin Function
    $.fn.schemaEngine = function (method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function () {
                new $.schemaEngine($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $schemaEngine = $(this).data('schemaEngine');
            switch (method) {
                case 'getImage': $schemaEngine.getImage(); break;
                case 'getWidget': return $schemaEngine.getView(options); break;
                case 'state':
                    return $schemaEngine; break;
                default:
                    return false;
            }
        }
    }

})(jQuery);