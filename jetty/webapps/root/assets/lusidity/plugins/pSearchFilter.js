;(function ($) {

    // This class can only be used with data that is fully loaded.
    // $.pSearchFilter('suggest', {items: []}
    //  suggest expects an array of items with each item containing an _row.
    //Object Instance
    $.pSearchFilter = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.pSearchFilter.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("pSearchFilter", state);

        // Private environment methods
        methods = {
            init: function() {
                state.worker.node.attr('data-valid', true).show();
                state.opts.searchNode = dCrt('div').addClass('tSearch').css({position: 'relative', clear: 'both'});
                state.worker.node.append(state.opts.searchNode);
                methods.search.init();
            },
            search: {
                enabled: function () {
                    return state.opts.search.enabled;
                },
                filter: function () {
                    state.opts.searchFilterNode.children().remove();
                    function mb(term) {
                        if (!$.jCommon.string.empty(term.name)) {
                            var btn = dCrt('btn').addClass('btn btn-default blue').attr('title', 'Remove filter').attr('type', 'button');
                            btn.append(dCrt('span').html(term.name)).css({margin: '0 10px 10px 0'});
                            state.opts.searchFilterNode.append(btn);
                            if(term.id){
                                btn.attr('id', term.id);
                            }
                            var sp = dCrt('span').addClass('glyphicon glyphicon-remove').css({
                                marginLeft: '5px',
                                color: '#a94442'
                            });
                            btn.append(sp);
                            btn.on('click', function () {
                                var temp = [];
                                $.each(state.opts.suggest, function () {
                                    if (!$.jCommon.string.equals(this.name, term.name)) {
                                        temp.push(this);
                                    }
                                    else if(this.sort && state.opts.defaultSort){
                                        state.opts.sort = state.opts.defaultSort;
                                    }
                                });
                                state.opts.suggest = temp;
                                btn.remove();
                                methods.search.filter();
                            });
                        }
                    }

                    $.each(state.opts.suggest, function () {
                        mb(this);
                    });

                    $.each(state.opts.items, function () {
                        if(this._row) {
                            this._row.show();
                            this._visible = true;
                        }
                    });

                    if(state.opts.sort){
                        state.opts.items = $.jCommon.array.sort(state.opts.items, state.opts.sort);
                        if($.isFunction(state.opts.onSorted)){
                            state.opts.onSorted(state.opts.items)
                        }
                    }

                    $.each(state.opts.items, function () {
                        var item = this;
                        var match = true;
                        $.each(state.opts.suggest, function () {
                            if(!item._visible){
                                return false;
                            }
                            var sgst = this;
                            if($.jCommon.string.contains(sgst.name, '::')){
                                var parts = sgst.name.split("::");
                                var key = parts[0];
                                $.each(state.opts.search.properties, function () {
                                    if($.jCommon.string.equals(key, this, true)){
                                        key = this;
                                        return false;
                                    }
                                });
                                var v = item[key];
                                if(v){
                                    v = v.trim();
                                }
                                match = (v && $.jCommon.string.contains(v, parts[1], true));
                            }
                            else {
                                $.each(state.opts.search.properties, function () {
                                    var v = item[this];
                                    match = (v && $.jCommon.string.contains(v, sgst.name, true));
                                    if(match){
                                        return false;
                                    }
                                });
                            }
                            if(!match){
                                item._visible = false;
                                return false;
                            }
                        });

                        if(!item._visible){
                            if(this._row) {
                                item._row.hide();
                            }
                        }
                    });

                    if($.isFunction(state.opts.search.onSelected)){
                        state.opts.search.onSelected();
                    }
                },
                suggest: function () {
                    $.each(state.opts.items, function () {
                        var item = this;
                        $.each(state.opts.search.properties, function () {
                            var key = this.toString();
                            var term = item[key];
                            if (term && !$.jCommon.array.contains(state.opts.suggestions, term)) {
                                state.opts.suggestions.push(term);
                            }
                        });
                    });
                },
                tooltip:function (node) {
                    if(state.opts.search.tooltip.title) {
                        node.popover({
                            html: true,
                            placement: state.opts.search.tooltip.placement,
                            template: '<div class="popover search-filter-popover" role="tooltip"><div class="arrow"></div><h3 class="popover-title"></h3><div class="popover-content"></div></div>',
                            trigger: 'hover',
                            animated: true,
                            container: 'body',
                            title: state.opts.search.tooltip.title,
                            content: function () {
                                return state.opts.search.tooltip.body;
                            }
                        });
                    }
                },
                init: function () {
                    if (!methods.search.enabled()) {
                        return false;
                    }
                    state.opts.searchNode.children().remove();
                    var node = dCrt('div');
                    if(state.opts.selections && state.opts.selections.length>0){
                        node.css({marginLeft: '100px'});
                        var d = dCrt('div').css({position: 'absolute', top: '0'});
                        state.opts.searchNode.append(d);
                        var btnGrp = dCrt('div').addClass('btn-group');
                        d.append(btnGrp);

                        var sBtn1 = dCrt('button').css({maxWidth: '68px', width: "68px"}).addClass('btn btn-info').html("Filter");
                        btnGrp.append(sBtn1);

                        var sBtn2 = dCrt('button').addClass('btn btn-info')
                            .attr('data-toggle', 'dropdown')
                            .attr('aria-haspopup', 'true')
                            .attr('aria-expanded', 'false');
                        var crt = dCrt('span').addClass('caret');
                        sBtn2.append(crt);

                        if(state.opts.filterTooltip){
                            sBtn1.attr('title', state.opts.filterTooltip);
                            sBtn2.attr('title', state.opts.filterTooltip);
                        }

                        /*
                        add to sBtn2

                        <span class="caret"></span>
                        <span class="sr-only">Toggle Dropdown</span>
                         */
                        btnGrp.append(sBtn2);

                        var ul = dCrt('ul').addClass('dropdown-menu');
                        btnGrp.append(ul);
                        $.each(state.opts.selections, function () {
                            var item = this;
                            var li = dCrt('li');
                            var a = dCrt('div').css({cursor: 'pointer'}).html(item.lbl);
                            item.id = $.jCommon.getRandomId('rmv');
                            li.append(a);
                            ul.append(li);
                            a.on('click', function () {
                                state.opts.sort = item.sort;
                                var term = item.key + "::" + item.value;
                                $.each(state.opts.suggest, function () {
                                    var btn = state.opts.searchNode.find('#'+this.id);
                                    if(btn && btn.length>0){
                                        btn.click();
                                        return true;
                                    }
                                });
                                if(item.value.toLowerCase()!=="all") {
                                    if (!$.jCommon.string.empty(term)) {
                                        state.opts.suggest.push({name: term.trim(), suggested: true, id: item.id});
                                    }
                                }
                                this.value = "";
                                methods.search.filter();
                            });
                        });
                    }


                    state.opts.searchNode.append(node);
                    var grp = dCrt('div').addClass('ui-widget input-group').css({marginBottom: '5px'});
                    node.append(grp);


                    var box = dCrt('input').addClass('form-control').attr('type', 'text').attr('placeholder', state.opts.search.text).css({
                        height: '34px',
                        minWidth: '230px'
                    });
                    methods.search.tooltip(box);
                    grp.append(box);

                    var sp = dCrt('span').addClass('input-group-btn');
                    grp.append(sp);
                    var btn = dCrt('btn').addClass('btn btn-default').attr('title', 'Using this button will allow you to do a partial match.').attr('type', 'button').html(state.opts.search.btn);
                    sp.append(btn);

                    state.opts.searchFilterNode = dCrt('div').css({margin: "0 5px"});
                    state.opts.searchNode.append(state.opts.searchFilterNode);

                    function split(val) {
                        return val.split(/,\s*/);
                    }

                    function extractLast(term) {
                        return split(term).pop();
                    }

                    btn.on('click', function (e) {
                        var term = box.val();
                        if (!$.jCommon.string.empty(term)) {
                            var found = false;
                            $.each(state.opts.suggest, function () {
                                if ($.jCommon.string.equals(this.name, term, true)) {
                                    found = true;
                                    return false;
                                }
                            });
                            if (!found) {
                                var v = {name: term, suggested: false};
                                state.opts.suggest.push(v);
                                methods.search.filter();
                            }
                        }
                        box.blur();
                        box.val('');
                    });

                    // ignore the below
                    box.autocomplete({
                        minLength: 0,
                        source: function (request, response) {
                            state.opts.suggestions.sort();
                            // delegate back to autocomplete, but extract the last term
                            response($.ui.autocomplete.filter(
                                state.opts.suggestions, extractLast(request.term)));
                        },
                        focus: function () {
                            // prevent value inserted on focus
                            return false;
                        },
                        select: function (event, ui) {
                            // add the selected item
                            box.blur();
                            var term = ui.item.value;
                            if (!$.jCommon.string.empty(term)) {
                                state.opts.suggest.push({name: term.trim(), suggested: true});
                            }
                            this.value = "";
                            methods.search.filter();
                            window.setTimeout(function () {
                                box.val('');
                            }, 300);
                        }
                    });
                }
            }
        };
        //public methods
        state.suggest = function (options) {
            state.opts = $.extend(state.opts, options, true);
            methods.search.suggest();
        };

        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.pSearchFilter.defaults = {
        suggest: [],
        suggestions: [],
        search: {
            enabled: true,
            text: "What are you looking for?",
            btn: "Add",
            properties:['title'],
            tooltip:{
                enabled: true,
                title: "Set a title for this tooltip",
                body: "Set the body as either a node or txt.",
                placement: 'left'
            }
        }
    };


    //Plugin Function
    $.fn.pSearchFilter = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.pSearchFilter($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $pSearchFilter = $(this).data('pSearchFilter');
            switch (method) {
                case 'suggest': $pSearchFilter.suggest(options);break;
                case 'exists': return (null!==$pSearchFilter && undefined!==$pSearchFilter && $pSearchFilter.length>0);
                case 'state':
                default: return $pSearchFilter;
            }
        }
    };

    $.pSearchFilter.call= function(elem, options){
        elem.pSearchFilter(options);
    };

    try {
        $.htmlEngine.plugins.register("pSearchFilter", $.pSearchFilter.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
