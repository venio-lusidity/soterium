function load() {
    if(lusidity && lusidity.ready && !pl_init){
        pl_init = true;
        lusidity.testing = true;
        var states = new oStates();
        var names = states.get();
        var results = [];
        var values = [{'let': "a", num: 1}, {'let': "b", num: 2}, {'let': "c", num: 3}];
        $.each(values,function () {
            var v = this;
            $.each(names, function () {
                results.push({title: this.title, abr: this.abr, 'let': v['let'], num: v.num});
            });
        });
        var options = {
            groupsEnabled: false,
            filter: null,
            view: "details",
            data: {results: results},
            title: 'States',
            realTime: true,
            settings: null,
            getUrl: function (data, start, limit) {
                return null;
            },
            sortable: true,
            sortOn: [ {property: 'title', asc: false}],
            lists:{
                groups:[],
                filters:[]
            },
            actions: [
                {
                    glyph: "glyphicon-cog",
                    title: "",
                    "items":[
                        {
                            glyph: "glyphicons glyphicons-copy",
                            title: "Copy to Clipboard",
                            onCreated: function (node, glyph, title, data) {
                                node.attr("data-toggle", "tooltip").attr("data-placement", "left")
                                    .attr("title", 'Once copied you can past into Excel by right clicking into the first cell select "Paste Special...", select "Unicode Text or Text" and click "OK"');
                                node.tooltip();

                            },
                            mouseEnter: function (node, glyph, title, data) {
                                ///node.find('.action-tooltip').show();
                            },
                            mouseLeave: function (node, glyph, title, data) {
                                //node.find('.action-tooltip').hide();
                            },
                            clicked: function(node, glyph, title, data){
                                if((state.current.view ==='details')) {
                                    var txt = "";
                                    var tables = state.current.contentNode.find('table');
                                    $.each(tables, function () {
                                        var t = state.current.contentNode.jFilterBar('simple', {table: $(this), styleIt: false});
                                        if (txt.length > 0) {
                                            txt += "<br/>";
                                        }
                                        txt += t[0].outerHTML;
                                    });
                                    lusidity.environment('copy', {txt: "\r\n<br/>" + txt});
                                }
                                else{
                                    lusidity.info.yellow("Copy to Clipboard is only available in details view.");
                                    lusidity.info.show(10);
                                }
                            }
                        }
                    ]
                }
            ],
            details: {
                sort: true,
                search: true,
                mapping: [
                    {header: {title: "#", callback: function (th, node) {
                        th.css({width: '20px'});
                    }}, property: '#', type: 'integer', callback: function (td, item, value) {
                        td.css({width: '20px'});
                    }},
                    {header: { title: "State", sortable: true, property: 'title'}, searchable: true, property: 'title', type: 'string', callback: function (td, item, value) {
                       td.append(value);
                    }},
                    {header: {title: "Abbreviation", tip: "", autoSize: true }, property: 'abr', type: 'string', callback: function (td, item, value) {
                        td.append(value);
                    }},
                    {header: {title: "City", tip: "", property: 'let', sortable: true, width: 60}, property: 'let', type: 'string', callback: function (td, item, value) {
                        td.append(value);
                    }},
                    {header: {title: "Population", tip: "", property: 'num', sortable: true, type: 'num', width: 60}, property: 'num', type: 'integer', callback: function (td, item, value) {
                        td.append(value);
                    }}
                ]
            }
        };

        var pnl = $('.panel-middle');
        pnl.show();
        var node = dCrt('div');
        pnl.append(node);
        node.jFilterBar(options);
    }
}
load();
