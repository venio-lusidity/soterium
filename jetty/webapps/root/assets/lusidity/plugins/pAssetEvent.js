;(function ($) {

    //Object Instance
    $.pAssetEvent = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.pAssetEvent.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("pAssetEvent", state);

        // Private environment methods
        methods = {
            init: function() {
                state.worker.node.attr('data-valid', true).show();
                state.opts.searchNode = dCrt('div').css({position: 'relative', clear: 'both'});
                state.opts.bodyNode = dCrt('div').css({overflowX: "hidden", overflowY: "auto"});
                state.worker.node.append(state.opts.searchNode);
                state.worker.node.append(state.opts.bodyNode);
                if(state.opts.search.enabled) {
                    methods.search();
                }
                state.opts.searchNode.jNodeReady({onVisible: function () {
                    methods.resize();
                    methods.collect();
                }});
                lusidity.environment("onResize", function () {
                    methods.resize();
                });
            },
            collect: function () {
                var items = [];
                var im = state.worker.data['/technology/security/vulnerabilities/vulnerability_importer/importers'];
                if(im){
                    items = $.jCommon.array.addAll(items, im);
                }
                var hst = state.worker.data['/system/primitives/text_value/hosts'];
                if(hst){
                    items = $.jCommon.array.addAll(items, hst);
                }
                var events = state.worker.data['/electronic/asset_event/events'];
                if(events){
                    items = $.jCommon.array.addAll(items, events);
                }
                var ls = [];
                var s = function (data) {
                    if(data && data.results){
                        items = $.jCommon.array.addAll(items, data.results);
                    }
                    if(items && items.length>0) {
                        methods.fix(items);
                        methods.html.init(items);
                        if(state.opts.search.enabled) {
                            state.opts.searchNode.pSearchFilter('suggest', {
                                items: state.opts.rows
                            });
                        }
                    }
                };
                var q = QueryFactory.getImporterLastSeen(state.worker.data);
                $.htmlEngine.request("/query?limit="+state.opts.limit, s, s, q, "post");
            },
            fix: function (items) {
                $.each(items, function () {
                    if(this.importer){
                        this.importer = FnFactory.classTypeToName(this.importer);
                    }
                });
            },
            resize: function () {
                var av = state.opts.bodyNode.availHeight(10);
                dHeight(state.opts.bodyNode, av, av, av);
            },
            search: function () {
                state.opts.search.properties = state.opts.search.properties.sort();
                state.opts.search.onSelected = function () {
                    methods.resize();
                };
                var node = dCrt('div').addClass("pop-content");
                state.opts.search.tooltip.body = node;
                var a = dCrt('div');
                var hdr1 = dCrt('div').html('You can type just about any word you see in the history, other than a label and dates.  Each filter is used as an "AND" to further refine your query.  For more specific filter see "Property Filtering" below.');
                var lbl = dCrt('div').addClass('data-label').html('<strong>Example</strong>:');
                var val = dCrt('div').addClass('data-value').html('asset');
                a.append(hdr1).append(lbl).append(val);

                var b = dCrt('div');
                var hdr2 = dCrt('h5').html("Property Filtering").addClass('bordered default-grey pop-title');
                lbl = dCrt('div').addClass('data-label').html('<strong>Example</strong>:');
                val = dCrt('div').addClass('data-value').html('role::asset (property<strong>::</strong>value)');
                b.append(hdr2).append(lbl).append(val);

                var c = dCrt('div').css({marginBottom: '5px'});
                var lbl2 = dCrt('div').addClass('data-label').html('<strong>Note</strong>: Requires two colons.');
                c.append(lbl2);

                var d = dCrt('div');
                var h = dCrt('h5').html("<strong>Available Properties</strong>").addClass('data-label bordered-bottom').css({paddingBottom: '2px'});
                var u = dCrt('ul').css({margin: '2px 0 2px 20px'});
                d.append(h).append(u);
                $.each(state.opts.search.properties, function () {
                    var li = dCrt('li').html(this);
                    u.append(li);
                });
                node.append(a).append(b).append(c).append(d);
                state.opts.searchNode.pSearchFilter({search: state.opts.search,
                    defaultSort: [{property: 'createdWhen', asc: false, type: 'date'}],
                    filterTooltip: 'Filter a history category by clicking the arrow and making a selection.',
                    onSorted: function (items) {
                        methods.html.rows(items);
                    },
                    selections: [
                        {lbl: "All", key: "category",  value: "All", sort: [{property: 'createdWhen', asc: false, type: 'date'}]},
                        {lbl: "Change Log", key: "category",  value: "Change Log", sort: [{property: 'createdWhen', asc: false, type: 'date'}]},
                        {lbl: "Matched On", key: "category",  value: "Matched On", sort: [{property: 'createdWhen', asc: false, type: 'date'}]},
                        {lbl: "Most Recent Importer", key: "category",  value: "Most Recent Importer", sort: [{property: 'importer', asc: true},{property: 'role', asc: true},{property: 'scannedOn', asc: false, type: 'date'}]}
                    ]
                });
            },
            html:{
                init: function (items) {
                    state.opts.rowsNode = dCrt('div');
                    state.opts.bodyNode.append(state.opts.rowsNode);
                    items = $.jCommon.array.sort(items, [{property: "createdWhen", asc: false, type: "date"}]);
                    methods.html.rows(items);

                },
                rows: function (items) {
                    state.opts.rowsNode.children().remove();
                    $.each(items, function () {
                        if(!this._row) {
                            var d = false;
                            var rw = dCrt('div').addClass("data-list").attr('data-id', this.lid);
                            state.opts.rowsNode.append(rw);
                            switch (this.vertexType) {
                                case '/technology/security/vulnerabilities/vulnerability_importer':
                                    this.category = "Most Recent Importer";
                                    methods.html.a(rw, this);
                                    break;
                                case '/system/primitives/text_value':
                                    this.category = "Change Log";
                                    methods.html.b(rw, this);
                                    break;
                                case '/object/importer/object_match':
                                    this.category = "Matched On";
                                    this.role = "Matched On";
                                    d=methods.html.c(rw, this);
                                    break;
                                case '/electronic/asset_event':
                                    this.category = "Change Log";
                                    methods.html.d(rw, this);
                                    break;
                                default:
                                    console.log(this);
                                    break;
                            }
                            this._row = d ? false : rw;
                            state.opts.rows.push(this);
                        }
                        else{
                            state.opts.rowsNode.append(this._row);
                        }
                    });
                },
                a: function (rw, item) {
                    var p = ["source:Source", "importer:Importer", "role:Role", "scannedOn:Scanned On", "modifiedWhen:Processed On"];
                    var on = 0;
                    var max = p.length;
                    $.each(p, function () {
                        var k = $.jCommon.string.getFirst(this, ":");
                        var lbl = $.jCommon.string.getLast(this, ":");
                        var v = item[k];
                        if(k) {
                            methods.html.label(rw, k, lbl, v, on, max);
                            on++;
                        }
                    });
                },
                b: function (rw, item) {
                    var p = ["label:Source", "value:Host", "role:Role", "createdWhen:Modified On"];
                    item.source = item.label;
                    item.role = "Host";
                    item.host = item.value;
                    var on = 0;
                    var max = p.length;
                    $.each(p, function () {
                        var k = $.jCommon.string.getFirst(this, ":");
                        var lbl = $.jCommon.string.getLast(this, ":");
                        var v = item[k];
                        if(k) {
                            methods.html.label(rw, k, lbl, v, on, max);
                            on++;
                        }
                    });
                },
                c: function (rw, item) {
                    var t = FnFactory.toTitleCase(item['title']);
                    item['title'] = t;
                    var mo;
                    if(item.key && item.value){
                        mo = String.format('<strong>{0}</strong>: {1}', item.key, item.value);
                    }
                    var cat = 'm';
                    if($.jCommon.string.contains(t, 'deprecated', true)){
                        cat='d';
                    }
                    else if(!mo){
                        cat='f';
                    }
                    item._cat = (cat==='f') ? 'found' : (cat==='d') ? 'deprecated' : 'matched';
                    item.matched = ((cat==='f') || (cat==='d')) ? $.jCommon.dateTime.defaultFormat(item["lastSeen"]) : mo;
                    var txt = String.format("{0} <strong>{1} on</strong>: {2}{3} <strong>processed on</strong>: {4}",
                        t,
                        item._cat,
                        item.matched,
                        (cat==='m') ? ' <strong>last seen on</strong>: ' + $.jCommon.dateTime.defaultFormat(item["lastSeen"]) : '',
                        $.jCommon.dateTime.defaultFormat(item["createdWhen"])
                    );

                    if(item["lastSeen"]===undefined){
                       rw.remove();
                       return true;
                    }
                    rw.append(txt);
                },
                d: function (rw, item) {
                    item.importer = FnFactory.toTitleCase($.jCommon.string.getLast(item.sourceType, '.'));
                    item.source = item.importer;

                    $.each(item['/event/event_fact/facts'], function () {
                        if(!this.fact){
                            this.fact = this.value;
                        }
                        item[this.key] = this.fact;
                    });

                    var p = ["importer:Importer", "role:Role", "scannedOn:Scanned On", "modifiedWhen:processed on"];
                    var on = 0;
                    var max = p.length;
                    $.each(p, function () {
                        var k = $.jCommon.string.getFirst(this, ":").toString();
                        var lbl = $.jCommon.string.getLast(this, ":").toString();
                        var v = item[k];
                        if(k) {
                            if(k==='importer'){
                                v = FnFactory.toTitleCase($.jCommon.string.spaceAtCapitol(v));
                                item[k] = v;
                            }
                            methods.html.label(rw, k, lbl, v, on, max);
                            on++;
                        }
                    });

                    function mk(prefix, lbl) {
                        var txt = item[prefix];
                        var d = item[prefix+'DitprId'];
                        if(d){
                            txt = d+' '+txt;
                        }
                        var v = dLink(txt, item[prefix+'Id']);
                        methods.html.label(rw, prefix, lbl, v, on, max);
                        on++;
                    }
                    if(item.original){
                        rw.append(dCrt("span").html("|").css({margin: "0 5px"}));
                        mk('original', 'Previous');
                    }

                    if(item.current){
                        rw.append(dCrt("span").html("|").css({margin: "0 5px"}));
                        mk('current', 'Current');
                    }
                },
                label: function (rw, k, lbl, v, on, max) {
                    rw.append(dCrt("div").addClass("data-label").html(lbl+":"));

                    if($.jCommon.string.containsAny(k, ["scannedOn", 'createdWhen', 'modifiedWhen'])){
                        v = $.jCommon.dateTime.defaultFormat(v);
                    }
                    rw.append(dCrt("div").addClass("data-value").append(v));
                    if(on<(max-1)){
                        rw.append(dCrt("span").html("|").css({margin: "0 5px"}));
                    }
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.pAssetEvent.defaults = {
        limit: 1000,
        offsetHeight: 0,
        rows: [],
        search: {
            enabled: true,
            text: "What are you looking for?",
            btn: "Add",
            properties:[
                "source",
                "host",
                "importer",
                "role",
                "matched"
            ],
            tooltip:{
                enabled: true,
                title: "<strong>Filtering</strong>",
                body: "Set the body as either a node or txt.",
                placement: 'left'
            }
        }
    };


    //Plugin Function
    $.fn.pAssetEvent = function(method, options) {
        if (method === undefined) method = {};
        if (typeof method === "object") {
            return this.each(function() {
                new $.pAssetEvent($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $pAssetEvent = $(this).data('pAssetEvent');
            switch (method) {
                case 'exists': return (null!==$pAssetEvent && undefined!==$pAssetEvent && $pAssetEvent.length>0);
                case 'state':
                default: return $pAssetEvent;
            }
        }
    };

    $.pAssetEvent.call= function(elem, options){
        elem.pAssetEvent(options);
    };

    try {
        $.htmlEngine.plugins.register("pAssetEvent", $.pAssetEvent.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
