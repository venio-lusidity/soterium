;(function ($) {

    //Object Instance
    $.adhoc = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.adhoc.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.selections = [];
        state.canned = false;
        var _eNode;
        var _vNode;

        // Store a reference to the environment object
        el.data("adhoc", state);

        // Private environment methods
        methods = {
            init: function () {
                _eNode =  $('#btn-edit');
                _vNode = $('#btn-view');

                state.opts.editNode = dCrt('div').addClass('work-node');
                state.opts.viewNode = dCrt('div').addClass('work-node').hide();
                methods.html.content.inform(state.opts.viewNode, 'Report data not available yet.<br/><br/>Select a saved report or build a new one by clicking the "Create Report" or "Edit" button.');
                state.opts.pnlMiddleNode.append(state.opts.viewNode).append(state.opts.editNode);

                var href = window.location.href.toString();
                state.opts.url = $.jCommon.url.create(href);

                var d = state.opts.url.getParameter('d');
                if(d) {
                    //parsing string from url
                    d = $.jCommon.string.getFirst(d, '#');
                    state.opts.current = $.jCommon.storage.getItem(d);
                    var cnt = state.opts.current._count;
                    state.opts.current.max = state.opts.current.et_exact ? cnt.exact : cnt.inherited;
                }

                methods.load();
                methods.html.builder.init();
                methods.html.canned.init();
                methods.html.content.init();

                state.opts.pnlMiddleNode.on('menu-bar-add', function (e) {
                   methods.reset();
                });

                state.opts.pnlMiddleNode.on('menu-bar-edit', function (e) {
                    if(methods.tgl(_eNode, _vNode)){

                    }
                });

                state.opts.pnlMiddleNode.on('menu-bar-view', function (e) {
                    if(methods.tgl(_vNode, _eNode)){
                    }
                });

                _eNode.addClass('active');

                //glyphicon glyphicon-filter
                lusidity.environment('onResize', function () {
                    methods.resize();
                });
            },
            getId: function (prfx) {
                var d = new Date();
                return String.format("{0}_{1}", prfx, d.getTime());
            },
            tgl: function (a, b) {
                a.addClass('active');
                b.removeClass('active');
                if(a.attr('id')==='btn-edit'){
                    state.opts.editNode.show();
                    state.opts.viewNode.hide();
                }
                else{
                    state.opts.editNode.hide();
                    state.opts.viewNode.show();
                }

                return true;
            },
            tglEnbld: function (rmv, group, rule, footer) {
                function tgl(enbl, node){
                    if(node && node.length>0){
                        if(enbl){
                            node.removeAttr('disabled');
                        }
                        else{
                            node.attr('disabled', 'disable');
                        }
                    }
                }

                tgl((rmv && (Object.keys(state.groups).length > 1)), $('.rmv-group'));

                if(state.addGroupNode) {
                    if(group){
                        tgl((Object.keys(state.selections).length >= 2), state.addGroupNode);
                    }
                    else{
                        tgl(group, state.addGroupNode);
                    }
                }

                tgl(rule, $('.add-rule'));
                tgl(footer, state.footerNode ? state.footerNode.children() : null);
            },
            reset: function () {
                state.canned = false;
                methods.tgl(_eNode, _vNode);
                methods.html.content.inform(state.opts.viewNode, 'Report data not available yet.<br/><br/>Select a saved report or build a new one by clicking the "Create Report" or "Edit" button.');
                state.opts.editNode.children().remove();
                state.selections = [];
                methods.html.builder.init();
                state.opts.title = null;
                state.opts.pnlLeftNode.find('li.active').removeClass('active');
            },
            resize: function () {
                var dif = 0;
                if(state.builderNode){
                    var h = state.opts.pnlMiddleNode.height();
                    var diff = state.builderNode.parent().prev().height();
                    h-=(diff+65);
                    dHeight(state.builderNode, h, h, h);
                }
                var nodes = $('.brace-node');
                if(nodes && nodes.length>0){
                    $.each(nodes, function () {
                        var node = $(this);
                        var prnt = node.parent().parent().find('.prop-group');
                        var h = prnt.height()+9;
                        var cb = node.find('.curly-brace');
                        dHeight(cb, h, h, h);
                        var btn = node.parent().find('.btn-operator');
                        var bh = btn.height();
                        h = (h/2)-15;
                        btn.css({top: h+'px'});
                    });
                }
            },
            getView: function (cls, lbl, view) {
                var n = state.opts.views[cls];
                if(!n){
                    console.log(cls);
                }
                $.each(n, function () {
                    if(this.label === lbl){
                        state.opts.current[view] = this;
                        return false;
                    }
                });
            },
            html: {
                builder: {
                    init: function () {
                        var c = dCrt('span').css({position: 'relative', top: '-8px'});
                        var h = dCrt('span').css({wordBreak: 'break-all'});
                        var t =state.opts.current.title;
                        var type = $.adhoc.getEtv(state.opts.current);

                        var tl = dCrt('span').append(String.format("Report Builder for {0}: ", type));
                        var hl = dCrt('span').append(dLink(t, state.opts.current[state.KEY_ID] + '?et_view=' + state.opts.current.et_view)).css({marginRight: '100px', wordBreak: 'break-all'});

                        state.opts.hdrInfoNode = dCrt('span').append(String.format(' {0} Total Assets', $.jCommon.number.commas(state.opts.current.max)));
                        h.append(tl).append(hl);
                        c.append(state.opts.hdrInfoNode).append(dCrt('br').css({lineHeight: '2px'})).append(h);

                        state.opts.editNode.children().remove();
                        var panel = dCrt('div');
                        state.opts.editNode.append(panel);

                        state.builderNode = $.htmlEngine.panel(panel, "glyphicons glyphicons-database-search", c, null, false, null, null, true, false);
                        state.builderNode.addClass('builder').css({padding: '0 5px 5px 5px'});
                        state.footerNode = dCrt('div').css({minHeight: '34px', margin: '5px 5px', paddingBottom: '5px'});
                        state.builderNode.parent().append(state.footerNode);

                        methods.html.builder.schemas();
                        methods.html.builder.actions.init();
                        methods.resize();
                    },
                    actions: {
                        init: function () {
                            var qry = dCrt('button').addClass('btn btn-success').css({"float": 'right'}).html("Go").attr('title', "Run the report");
                            state.footerNode.append(qry);

                            var sv = dCrt('button').addClass('btn btn-primary').css({"float": 'right', marginRight: '5px'}).html("Save").attr('title', "Save the report and run it");
                            state.footerNode.append(sv);

                            state.footerNode.children().attr('disabled', 'disabled');

                            function go() {
                                _vNode.click();
                                state.opts.viewNode.children().remove();
                                var ldr = dCrt('div');
                                state.opts.viewNode.append(ldr);
                                methods.html.content.inform(ldr, "Querying Data", true);
                                $.htmlEngine.busy(ldr, {type: 'dots', cover: false, adjustWidth: 2, adjustHeight: -7});
                            }

                            function clicked(a, b) {
                                a.attr('disabled', 'disabled');
                                var spnr = $.htmlEngine.getSpinner();
                                spnr.css({top: '-2px', left: '4px'});
                                a.append(spnr);
                                b.attr('disabled', 'disabled');
                                return spnr;
                            }

                            function done(spnr) {
                                spnr.remove();
                                qry.removeAttr('disabled');
                                sv.removeAttr('disabled');
                            }

                            qry.on('click', function () {
                                var spnr = clicked(qry, sv);
                                go();
                                methods.html.builder.actions.query(!state.canned, null, function () {
                                    done(spnr);
                                });
                            });

                            var title;
                            var save;
                            var cancel;
                            sv.on('click', function () {
                                var spnr = clicked(sv, qry);
                                state.opts.editNode.pageModal();
                                state.opts.editNode.pageModal('show', {
                                    glyph: 'glyphicons glyphicons-folder-open',
                                    hasClose: true,
                                    header: function () {
                                        var header = dCrt('div').css({position: 'relative', top: '-6px'});
                                        var hContent = dCrt('h4').html("Save Report");
                                        header.append(hContent);
                                        return header;
                                    },
                                    body: function (body) {
                                        body.children().remove();
                                        var node = dCrt('div');
                                        var grp = dCrt('div').addClass('input-group');
                                        var sp = dCrt('span').addClass('input-group-addon').html('Title');
                                        var inpt = dCrt('input').addClass('form-control').attr('type', 'text').attr('placeholder', "Enter a unique title for your report");
                                        inpt.on('keyup', function () {
                                            title = inpt.val();
                                            if(title && title.length>0){
                                                save.removeAttr('disabled').addClass('btn-primary').removeClass('default-grey');
                                            }
                                            else{
                                                save.attr('disabled', 'disabled').removeClass('btn-primary').addClass('default-grey');
                                            }
                                        });
                                        body.append(node.append(grp.append(sp).append(inpt)));
                                        window.setTimeout(function () {
                                            inpt.focus();
                                        }, 300);
                                    },
                                    footer: function () {
                                        var lFooter = dCrt('div');
                                        var btnBar = dCrt('div').addClass('btn-bar');
                                        save = dCrt('button').attr('type', 'button')
                                            .addClass('btn default-grey').html('Save').attr('disabled', 'disabled');

                                        cancel = dCrt('button').attr('type', 'button')
                                            .addClass('btn btn-default btn-warning').html('Cancel');

                                        btnBar.append(cancel).append(save);
                                        save.on('click', function (e) {
                                            state.opts.editNode.pageModal('hide');
                                            go();
                                            state.opts.last = title;
                                            methods.html.builder.actions.query(true, title, function () {
                                                done(spnr);
                                            });
                                        });
                                        cancel.on('click', function () {
                                            state.opts.editNode.pageModal('hide');
                                            done(spnr);
                                        });
                                        lFooter.append(btnBar);
                                        return lFooter;
                                    }
                                });
                            });
                        },
                        query: function (save, title, cback) {
                            var evals = [];
                            $.each(state.selections, function () {
                                var gi = state.groups[this.prntId];
                                var ia = ((this.role==='filter') ? (gi.operator === 'and') : true);
                                var bo = ((this.role==='filter') ? (gi.bg === 'and') : true);
                                var d = {bg: bo, isAnd: ia, item: this.item, role: this.role};
                                if(gi && gi.groupId){
                                    d.gId = gi.groupId;
                                }
                                evals.push(d);
                            });
                            if(save) {
                                state.opts.last = title;
                            }
                            var qry = {
                                save: save,
                                title: title,
                                lid: state.opts.current[state.KEY_ID],
                                et_view: state.opts.current.et_view,
                                exact: state.opts.current.et_exact,
                                query: evals
                            };
                            var s = function (data) {
                                state.opts.sw.stop(false);
                                methods.html.canned.init();
                                if(data && data.results){
                                    var items = {};
                                    methods.html.content.view(data);
                                }
                                else{
                                    methods.html.content.inform(state.opts.viewNode, 'No results found<br/><br/>Click the "Edit" button to modify the report.');
                                }
                                if($.isFunction(cback)){
                                    window.setTimeout(function () {
                                        cback(data);
                                    }, 300);
                                }
                            };

                            $.htmlEngine.request('/adhoc/query?limit=1000000', s, s, qry, 'post');

                        }
                    },
                    schemas: function () {
                        var s = function (data) {
                            if (data && data.results) {
                                state.schemas = data;
                                state.schemas.results = $.jCommon.array.sort(state.schemas.results, [{property: "label", asc: true}]);
                                methods.html.builder.wizard.init();
                            }
                        };
                        var f = function () {

                        };
                        $.htmlEngine.request("/adhoc/schemas", s, f, null, "get", false);
                    },
                    wizard: {
                        init: function () {
                            state.builderNode.children().remove();

                            /*
                                var a = methods.html.content.header("Please choose a saved report or select a report type below to start building a new one.");
                                state.builderNode.append(a);
                            */

                            var grp = dCrt('div').addClass('btn-group').attr('data-toggle', 'buttons').css({marginTop: '5px', position: 'relative'});
                            var on = 0;
                            var node = dCrt('div');
                            state.schemas.results = $.jCommon.array.sort(state.schemas.results, [{property: 'label', asc: true}]);
                            $.each(state.schemas.results, function () {
                                var item = this;
                                var id = methods.getId('refine');
                                if(item.label==='Asset'){
                                    return true;
                                }
                                on++;
                                var lbl = dCrt('label').addClass('btn btn-default').attr('cat', item.cls);
                                var btn = dCrt('input').attr('id', id).attr('value', on).attr('type', 'radio');
                                lbl.append(btn).append(item.label);
                                grp.append(lbl);
                                lbl.on('click', function (e) {
                                    if (node) {
                                        node.children().remove();
                                        state.selections = [];
                                    }
                                    state.opts.current.cat = item.cls;
                                    methods.getView(item.cls, "Details", "catView");
                                    state.selections.push({id: id, role: "cat", item: item, node: node, schema: null});
                                    state.opts.groupsNode = dCrt('div').addClass('decision-groups');
                                    node.append(state.opts.groupsNode);
                                    state.groups = {};
                                    methods.html.builder.wizard.decision(item, state.opts.groupsNode, false);
                                });
                            });

                            state.builderNode.append(grp).append(node);
                        },
                        decision: function (item, prnt, added) {
                            if(!prnt){return false;}

                            var tbl = dCrt('table');
                            prnt.append(tbl);

                            var row = dCrt('tr');
                            tbl.append(row);

                            var id = methods.getId('group');
                            var grpItem = {row: row, bg: 'and', operator: 'and', groupId: id};

                            var on = 0;

                            if(added){
                                // between groups work in progress
                                var td = dCrt('td').attr('colspan', '2');
                                row.append(td);

                                var btwnGrp = dCrt('div').css({clear: 'both', minHeight: '36px', margin: '8px 5px 0px 86px'});
                                var btwnGrpBtn = dCrt('div').addClass('btn-group btn-operator gp-op').attr('data-toggle', 'buttons').css({
                                    position: 'relative',
                                    "float": "left"
                                });
                                td.append(btwnGrp.append(btwnGrpBtn));

                                $.each(["and", "or"], function () {
                                    var txt = this.toString();
                                    var lbl = dCrt('label').addClass('btn btn-primary operator operator-between ' + ((on === 0) ? "active" : "")).attr('key', txt).css({fontSize: '12px'});
                                    var btn = dCrt('input').attr('value', on).attr('type', 'radio');
                                    lbl.append(btn).append(txt);
                                    btwnGrpBtn.append(lbl);
                                    lbl.on('click', function (e) {
                                        node.attr('operator', txt);
                                        grpItem.bg = txt;
                                        state.canned = false;
                                    });
                                    on++;
                                });
                                row = dCrt('tr');
                                tbl.append(row);
                            }

                            var td1 = dCrt('td').addClass('td-op');
                            var td2 = dCrt('td');
                            row.append(td1).append(td2);


                            var brace1 = dCrt('div').addClass('brace-node');
                            var brace2 = dCrt('div').addClass('curly-brace');
                            var brace3 = dCrt('div').addClass('brace brace-top');
                            var brace4 = dCrt('div').addClass('brace brace-bottom');
                            brace1.append(brace2.append(brace3).append(brace4));
                            td1.append(brace1);

                            var dgNode = dCrt('div').attr("id", id).addClass('decision-group');
                            td2.append(dgNode);

                            var node = dCrt('div').addClass('prop-group');
                            var fNode = dCrt('div').css({position: 'relative'});
                            dgNode.append(node).append(fNode);

                            grpItem.node = node;
                            state.groups[id] = grpItem;
                            prnt.css({position: 'relative'});

                            var grp1 = dCrt('div').addClass('btn-group-vertical btn-operator').attr('data-toggle', 'buttons').css({
                                position: 'relative',
                                "float": "left"
                            });
                            on = 0;
                            $.each(["and", "or"], function () {
                                var txt = this.toString();
                                var lbl = dCrt('label').addClass('btn btn-primary operator ' + ((on === 0) ? "active" : "")).attr('key', txt).css({fontSize: '12px'});
                                var btn = dCrt('input').attr('value', on).attr('type', 'radio');
                                lbl.append(btn).append(txt);
                                grp1.append(lbl);

                                lbl.on('click', function (e) {
                                    node.attr('operator', txt);
                                    grpItem.operator = txt;
                                    state.canned = false;
                                });
                                on++;
                            });
                            td1.append(grp1);
                            td1.children().hide();

                            var addNode = dCrt('div').addClass('add-node').css({position: 'relative'});
                            fNode.append(addNode);

                            $.each(
                                [{lbl: "add group", glyph: "glyphicon-plus", cls: 'btn-success add-group disable-group', onClick: function (e, btn, bi) {

                                    methods.html.builder.wizard.decision(item, state.opts.groupsNode, true);
                                    methods.resize();
                                    methods.tglEnbld(true, false, false, false);
                                    state.canned = false;
                                }},
                                    {prnt: node, lbl: "remove group", glyph: "glyphicon-remove rmv-group disable-group",cls: 'btn-success btn-danger rmv-group', onClick: function (e, btn, bi) {
                                        methods.html.builder.wizard._rmvGroup(id);
                                    }},
                                    {prnt: node, lbl:"add rule", glyph: "glyphicon-plus add-rule", cls: 'btn-success add-rule  disable-group', onClick: function (e, btn, bi) {

                                        state.canned = false;
                                        if(state.addGroupNode) {
                                            state.addGroupNode.attr('disabled', 'disabled');
                                        }
                                        if(state.footerNode) {
                                            state.footerNode.children().attr('disabled', 'disabled');
                                        }
                                        btn.removeClass('active');

                                        prnt.css({position: 'relative'});

                                        var chld = dCrt('div').addClass('bottom-grey filter-group').css({"clear": "both", marginTop: "5px", padding: '0 5px 0 5px'});
                                        node.append(chld);
                                        $('.add-rule').attr('disabled', 'disabled');
                                        $(".add-group").attr('disabled', 'disabled');
                                        if(node.children().length>3){
                                            td1.children().show();
                                        }
                                        methods.html.builder.wizard.refine(item, chld, id);
                                        methods.resize();
                                    }}], function () {
                                    var bi = this;

                                    var z = (bi.lbl === 'add group');
                                    var btn = (z) ? $('.add-group') : false;
                                    if(!btn || (btn.length===0)) {
                                        btn = dCrt('btn').addClass('btn btn-success ' + bi.cls).attr('type', 'button').css({
                                            fontSize: '12px',
                                            marginRight: '5px'
                                        });
                                        var gl = $.htmlEngine.glyph(bi.glyph).css({marginRight: '5px'});
                                        btn.append(gl).append(bi.lbl);
                                    }

                                    if(z) {
                                        state.addGroupNode = btn;
                                        btn.unbind();
                                        btn.attr('disabled', 'disabled');
                                    }

                                    if(bi.prnt){
                                        bi.prnt.prepend(btn);
                                    }
                                    else{
                                        addNode.append(btn);
                                    }

                                    btn.on('click', function (e) {
                                        bi.onClick(e, btn, bi);
                                    });

                                    if(bi.lbl==='add rule') {
                                        btn.click();
                                    }
                                });
                            methods.resize();
                        },
                        _rmvGroup: function (id) {
                            var item = state.groups[id];
                            var rmvls = item.node.find('.rmv-select');
                            $.each(rmvls, function () {
                                $(this).click();
                            });
                            var t = $(item.row.closest('table'));
                            if(t && t.length>0) {
                                var n = t.find('.decision-group');
                                state.addGroupNode.hide();
                                $('body').append(state.addGroupNode);
                                var p = t.parent();
                                t.remove();
                                var c = p.children();
                                t = $(c[c.length-1]);
                                t.find('.add-node').after(state.addGroupNode);
                                state.addGroupNode.show();
                                delete state.groups[id];
                            }
                        },
                        _rmv: function (node, id, schema, refine) {
                            var temp = [];
                            $.each(state.selections, function () {
                                if($.jCommon.string.equals(this.id, id)){
                                    return true;
                                }
                                temp.push(this);
                            });
                            state.selections = temp;
                        },
                        refine: function (schema, prnt, prntId, isChild) {
                            if(!prnt){
                                return false;
                            }
                            var scma;
                            if(schema && !isChild){
                                scma = $.extend({}, schema, true);
                            }
                            else if(isChild){
                                scma = schema;
                            }
                            else{
                                console.log("schema missing");
                                return false;
                            }
                            var id = methods.getId('refine');
                            var node = dCrt('div').attr('id', id).css({
                                position: 'relative',
                                margin: '5px 20px 5px ' + (isChild ? '45px' : '5px')
                            });
                            var _rmv = dCrt('div').addClass('rmv-select').css({
                                cursor: 'pointer',
                                position: 'absolute',
                                top: '2px',
                                right: '-18px'
                            }).attr("title", "remove rule");
                            _rmv.append(dCrt('span').addClass('glyphicon glyphicon-remove critical-font')).css({fontSize: '12px'});
                            node._rmv = _rmv;
                            node.append(_rmv);
                            prnt.append(node);

                            function rmv() {
                                var p = $('#'+prntId);
                                var chld = $(p.children()[0]);
                                if(chld.children().length<=3){
                                    p.find('.rmv-select').hide();

                                   // $(p.closest('td')).prev().children().hide();
                                }
                                else{
                                    p.find('.rmv-select').show();
                                    $(p.closest('td')).prev().children().show();
                                }
                                methods.tglEnbld(true, true, true, true);
                            }
                            rmv();
                            _rmv.on('click', function () {
                                state.canned = false;
                                methods.html.builder.wizard._rmv(node, id, scma, true);
                                prnt.remove();
                                methods.resize();
                                rmv();
                            });
                            var sel = dCrt('div').css({marginTop: '7px'}).hide();

                            function rd(){
                                var grp = dCrt('div').addClass('btn-group key-select').attr('data-toggle', 'buttons').css({position: 'relative'});
                                var on = 0;

                                scma.filters = $.jCommon.array.sort(scma.filters, [{property: 'label', asc: true}]);
                                $.each(scma.filters, function () {
                                    var item = $.extend({}, this, true);
                                    on++;
                                    var lbl = dCrt('label').addClass('btn btn-default').attr('key', item.key);
                                    if(item.tip){
                                        lbl.attr('title', item.tip);
                                    }
                                    var btn = dCrt('input').attr('id', id).attr('value', on).attr('type', 'radio');
                                    lbl.append(btn).append(item.label);
                                    grp.append(lbl);
                                    function onAfter(valid) {
                                        if(valid) {
                                            methods.tglEnbld(true, true, true, true);
                                        }
                                        else{
                                            methods.tglEnbld(false, false, false, false);
                                        }
                                        if(item.filters){
                                            /*
                                            // work in progress for sub filtering based on property selected
                                            var id = methods.getId('refine_child');
                                            var node = dCrt('div').attr('id', id).css({
                                                position: 'relative',
                                                margin: '5px 20px 5px 5px'
                                            });
                                            prnt.append(node);
                                            methods.html.builder.wizard.refine(item, node, id, true);
                                            */
                                        }
                                    }
                                    lbl.on('click', function () {
                                        sel.children().remove();
                                        var f = methods.html.builder.wizard.forms;
                                        sel.css({fontWeight: 'normal'});
                                        switch (item.type) {
                                            case 'string':
                                            default:
                                                f._string(item, sel, scma, onAfter);
                                                break;
                                        }

                                        var temp = [];
                                        $.each(state.selections, function () {
                                            if($.jCommon.string.equals(this.id, id)){
                                                return true;
                                            }
                                            temp.push(this);
                                        });
                                        state.selections = temp;

                                        state.selections.push({
                                            id: id,
                                            role: "filter",
                                            item: item,
                                            node: node,
                                            prnt: prnt,
                                            prntId: prntId,
                                            schema: $.jCommon.array.clone(scma)
                                        });
                                        methods.resize();
                                    });
                                });
                                node.append(grp).append(sel);
                                methods.tglEnbld(false, false, false, false);
                            }
                            rd();
                        },
                        forms: {
                            _selString: function (filter, node) {
                                var op = node.find('li[key="'+ filter.item.operator +'"]');
                                if(op){
                                    op.click();
                                }

                                var input = node.find('.string-val');
                                if(input){
                                    input.val(filter.item.value);
                                    input.blur();
                                }
                            },
                            _string: function (item, node, schema, onAfter) {
                                var selected = false;
                                var op = item.defaultOperator ? item.defaultOperator : 'exact';
                                var lbl = dCrt('div').html(item.label).css({display: 'inline-block', margin: "0 2px 0 2px"});
                                var group = dCrt('div').css({position: 'relative', top: '-2px', margin: "0 10px 0 5px", display: 'inline-block', whiteSpace: 'nowrap'});
                                var grp = dCrt('div').addClass("btn-group grp-string-val").css({marginLeft: "2px"});
                                var sp = dCrt('span').css({marginLeft: "5px"});
                                node.append(lbl).append(group.append(grp).append(sp));
                                node.show();

                                function m() {
                                    sp.children().remove();
                                    var ac = dCrt('input').addClass('string-val');
                                    sp.append(ac);
                                    ac.autocomplete({
                                        minLength: 0,
                                        source: function (request, response) {
                                            var term = request.term;
                                            var s = function (data) {
                                                if(data && data.results){
                                                    var items = [];
                                                    if(data.results && data.results.length>0) {
                                                        var srtd = $.jCommon.array.sort(data.results, [{property: 'title', asc: true}]);
                                                        $.each(srtd, function () {
                                                            var lbl = item.fn ? FnFactory.process(item.fn, {label: this.title}) : this.title;
                                                            items.push({label: lbl, value: this.title});
                                                        });
                                                    }
                                                    // delegate back to autocomplete, but extract the last term
                                                    response(items);
                                                }
                                            };
                                            var data = {
                                                et_view: state.opts.current.et_view,
                                                cls: schema.cls,
                                                key: item.key,
                                                type: item.type,
                                                term: term,
                                                operator: op,
                                                id: state.opts.current[state.KEY_ID]
                                            };
                                            $.htmlEngine.request("/refine/class?limit=25", s, s, data, "post");
                                        },
                                        optionName: "terms.label",
                                        select: function (event, ui) {
                                            var term = ui.item.value.toString();
                                            ac.val(term);
                                            selected = true;
                                            ac.blur();
                                        }
                                    });
                                    if(item.cls){
                                        ac.addClass(item.cls);
                                    }
                                    if(item.css){
                                        ac.css(item.css);
                                    }
                                    var frd = false;
                                    var term;
                                    var last=null;
                                    ac.on('focus', function () {
                                        last = ac.val();
                                        ac.select();
                                        var term = ac.val();
                                        if(!term){
                                            term = "*";
                                        }
                                        ac.data("uiAutocomplete").search(term);
                                    });
                                    ac.on('change', function () {
                                        last = null;
                                    });
                                    ac.on('blur', function () {
                                        term = ac.val();
                                        console.log(term);
                                        if(term && last && $.jCommon.string.equals(last, term, false)){
                                            return false;
                                        }
                                        state.canned = false;
                                        if(term && $.jCommon.string.equals(op, 'exact', true) && !selected && !state.ignore){
                                            ac.val('');
                                            ac.focus();
                                            lusidity.info.red('You must select one of the suggestions.  Suggestions require three or more characters to display.');
                                            lusidity.info.show(5);
                                        }
                                        if (term) {
                                            item.value = term;
                                            item.operator = op;
                                            item.prntId = schema.prntId;
                                            ac.removeClass('red');
                                            if($.isFunction(onAfter)){
                                                onAfter(true);
                                            }
                                            if(!frd) {
                                                methods.html.builder.wizard.refine(schema);
                                                frd=true;
                                            }
                                        }
                                        else {
                                            if($.isFunction(onAfter)){
                                                onAfter(false);
                                            }
                                            item.value=null;
                                            item.operator = null;
                                            var next = node.parent().next();
                                            var id = (next ? next.attr('id') : null);
                                            if(next && id!==null){
                                                methods.html.builder.wizard._rmv(next, id, schema, false);
                                            }
                                            ac.addClass('red');
                                            ac.focus();
                                            frd=false;
                                        }
                                        selected=false;
                                    });
                                }

                                var sel = dCrt('span').html(op).css({marginRight: '2px'});
                                var btn = dCrt('button').addClass('btn btn-default dropdown-toggle')
                                    .css({padding: '2px 2px 2px 2px', margin: '0'})
                                    .attr("data-toggle", "dropdown").attr("aria-haspopup", true).attr("aria-expanded", false)
                                    .append(sel).append(dCrt('span').addClass("caret"));

                                var ul = dCrt('ul').addClass("dropdown-menu no-radius");
                                var slcts = item.operators ? item.operators : ["contains", "exact", "starts with"];
                                $.each(slcts, function () {
                                    var term = this;
                                    var li = dCrt('li').html(this).attr('key', this).css({cursor: "pointer", padding: "2px"});
                                    ul.append(li);
                                    li.on('click', function () {
                                        op = term;
                                        sel.html(term);
                                        var next = node.parent().next();
                                        var id = (next ? next.attr('id') : null);
                                        if(next && id!==null){
                                            methods.html.builder.wizard._rmv(next, id, schema, false);
                                        }
                                        m();
                                    });
                                });

                                grp.append(btn).append(ul).append(sp);

                                m();
                            }
                        }
                    }
                },
                canned: {
                    init: function () {
                        if(!state.cannedNode) {
                            var panel = dCrt('div');
                            state.opts.pnlLeftNode.append(panel);
                            state.cannedNode = $.htmlEngine.panel(panel, "glyphicons glyphicons-folder-open", "Saved Reports", null, false, null, null);
                            state.opts.pnlLeftNode.append(panel);
                        }
                        var s = function (data) {
                            if(data && data.results){
                                methods.html.canned.make(data, state.cannedNode);
                            }
                        };
                        $.htmlEngine.request("/adhoc/query", s, s, 'get');
                    },
                    make: function (data, node) {
                        node.children().remove();
                        var a = [];
                        var b = [];
                        $.each(data.results, function () {
                            var item = this;
                            var lid = item[state.KEY_ID];
                            var n = dCrt('li').addClass('list-group-item no-radius no-border clickable').attr('lid', lid).css({position: 'relative', padding: "2px 20px 2px 5px"});
                            if(state.opts.last === lid || state.opts.last===item.title){
                                n.addClass('active');
                            }
                            var t = dCrt('span').html(item.title);
                            var i = dCrt('span').addClass('glyphicon glyphicon-remove critical-font').css({position: 'absolute', top: '6px', right: '2px', fontSize: '12px'}).hide();
                            i.on('click', function (e) {
                                e.preventDefault();
                                e.stopPropagation();
                                n.hide();
                                var s = function () {
                                    n.remove();
                                };
                                var f = function () {
                                    n.show()
                                };
                                $.htmlEngine.request(item[state.KEY_ID], s, f, null, 'delete');
                            });

                            n.append(t).append(i);
                            n.on('mouseover', function () {
                               i.show();
                            });
                            n.on('mouseout', function () {
                                i.hide();
                            });
                            n.on('click', function () {
                                state.opts.last = lid;
                                state.ignore = true;
                                methods.reset();
                                state.opts.title = item.title;
                                state.opts.pnlLeftNode.find('li.active').removeClass('active');
                                n.addClass('active');
                                var f = methods.html.builder.wizard.forms;

                                var filters = [];

                                function failed() {
                                    lusidity.info.red("The saved query is no longer supported.");
                                    lusidity.info.show(5);
                                    return false;
                                }

                                $.each(item.query, function () {
                                    var filter = this;
                                    if (filter.role === 'cat') {
                                        var lbl = $('label[cat="' + filter.item.cls + '"]');
                                        if (lbl) {
                                            lbl.click();
                                        }
                                        else {
                                           return failed();
                                        }
                                    }
                                    else if (filter.role === 'filter') {
                                        filter.ordinal = filter.isAnd ? 1 : 0;
                                        filters.push(filter);
                                    }
                                });
                                filters = $.jCommon.array.sort(filters, [{property: "gId", asc: true}]);

                                var isAnd = true;
                                var on = 0;
                                var len = filters.length;
                                var gId;

                                $.each(filters, function () {
                                    var filter = this;
                                    var rule = false;
                                    if ((on > 0) && !$.jCommon.string.equals(filter.gId, gId)) {
                                        state.addGroupNode.click();
                                    }
                                    else if(on>0 && on<len){
                                        rule = true;
                                    }
                                    gId = filter.gId;

                                    function g() {
                                        return $('.key-select');
                                    }

                                    var grps = g();
                                    var grp = grps.length>1 ? grps[grps.length-1] : grps;

                                    if(rule && grp){
                                        var prop = $(grp.closest('.prop-group'));
                                        prop.find('btn.add-rule').click();
                                    }

                                    isAnd = filter.isAnd;
                                    var lbl = g().find('label[key="' + filter.item.key + '"]');
                                    if(lbl.length>1){
                                        lbl = lbl[lbl.length-1];
                                    }
                                    var row = $(grp.closest('tr'));
                                    if(row && !isAnd) {
                                        var op = row.find('label[key="' + (isAnd ? "and" : "or") + '"]');
                                        op.click();
                                    }
                                    if(lbl) {
                                        var node = $(lbl.closest('.filter-group'));
                                        lbl.click();
                                        switch (item.type) {
                                            default:
                                            case "string":
                                                f._selString(filter, node);
                                                break;
                                        }
                                    }
                                    else{
                                        return failed();
                                    }

                                    var bg = filter.bg;
                                    if(row && !bg){
                                        var gop = row.prev().find('.gp-op');
                                        if(gop){
                                            gop = gop.find('label[key="' + ((bg==='and') ? "and" : "or") + '"]');
                                            if(gop){
                                                gop.click();
                                            }
                                        }
                                    }
                                    on++;
                                });
                                methods.tglEnbld(true, true, true, true);
                                state.ignore = false;
                                state.canned = true;
                            });
                            if(item.type==='canned'){
                                a.push(n);
                            }
                            else{
                                b.push(n);
                            }
                        });
                        function m(title, items){
                            var nd = dCrt('div');
                            var t = dCrt('div').html(title).addClass('font-grey-md');
                            var u = dCrt('ul').addClass('list-group no-radius').css({marginBottom: '5px'});
                            nd.append(t).append(u);
                            node.append(nd);

                            $.each(items, function () {
                                u.append(this);
                            });
                            return nd;
                        }
                        if(a.length>0){
                            state.opts.yrNode = m('Your Reports', a);
                        }
                        if(b.length>0){
                            b = $.jCommon.array.sort(b, [{property: 'createdWhen', asc: true}]);
                            state.opts.asrNode = m('Auto Saved Reports', b);
                        }
                    }
                },
                content: {
                    init: function () {
                        //methods.html.content.inform(state.opts.editNode, "Select or Build a Query");
                        // var qb = dCrt('div');
                        // state.opts.editNode.append(qb);
                        //qb.queryBuilder(options);
                    },
                    header: function (msg) {
                        var h = state.opts.editNode.availHeight(0);
                        var c = dCrt('div');
                        var hd = dCrt('h5').html(msg);//.addClass('letterpress');
                        c.append(hd);
                        return c;
                    },
                    inform: function (node, msg, counter) {
                        node.children().remove();
                        var h = node.availHeight(0);
                        var c = dCrt('div');
                        var hd = dCrt('h4').append(msg).addClass('letterpress');
                        c.append(hd);

                        if(counter) {
                            state.opts.sw = new oStopWatch();
                            state.opts.sw.start();
                            function check() {
                                window.setTimeout(function () {
                                    var elpsd =  state.opts.sw.getSeconds();
                                    hd.html(msg + " (" + elpsd + ")");
                                    check();
                                }, 1000);
                            }
                            check();
                        }

                        node.append(c);
                        var d = $.jCommon.element.getDimensions(c);
                        c.css({
                            position: 'absolute',
                            top: '25%',
                            left: '50%',
                            marginTop: ((d.h / 2) * -1) + 'px',
                            marginLeft: ((d.w / 2) * -1) + 'px',
                            textAlign: 'center'
                        });
                    },
                    view: function (data) {
                        state.opts.viewNode.children().remove();
                        var node = dCrt('div');
                        state.opts.viewNode.append(node);
                        node[state.opts.current.catView.name]({hdrInfoNode: state.opts.hdrInfoNode, data: data, current: state.opts.current, sw: state.opts.sw, title: state.opts.title});
                    }
                }
            },
            load: function () {
                $.each(state.opts.views, function () {
                    var n = this;
                    $.each(n, function () {
                        if(this.file){
                            pageLoader._load(this.file, "js");
                        }
                    });
                });
            }
        };
        //public methods

        // Initialize
        methods.init();
    };

    $.adhoc.getEtv  = function(current){
        var t;
        switch (current.et_view) {
            case 'ditpr':
                t = "System Name";
                break;
            case 'managed':
                t = "Managed By";
                break;
            case 'owned':
                t = "Owned By";
                break;
            case 'location':
                t = "Located In";
                break;
            case 'asset':
                t = "Asset";
                break;
            default:
                t = "Unknown";
                break;
        }

        if(current.et_exact){
            t = String.format('Directly {0}', t);
        }

        return t;
    };

    //Default Settings
    $.adhoc.defaults = {
        current: {},
        views : {
            "com.lusidity.domains.technology.Software":[
                {
                    label: "Details",
                    file: "/pages/reports/adhoc/js/jquery.adHocSoftware.js",
                    name: "adHocSoftware"
                }
            ],
            "com.lusidity.domains.technology.security.vulnerabilities.AssetVulnDetail":[
                {
                    label: "Details",
                    file: "/pages/reports/adhoc/js/jquery.adHocFindings.js",
                    name: "adHocScp"
                }
            ],
            "com.lusidity.domains.technology.security.vulnerabilities.iavms.IavmAssetComplianceDetails":[
                {
                    label: "Details",
                    file: "/pages/reports/adhoc/js/jquery.adHocIavm.js",
                    name: "adHocIavm"
                }
            ]
        }
    };


    //Plugin Function
    $.fn.adhoc = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.adhoc($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $adhoc = $(this).data('adhoc');
            switch (method) {
                case 'exists': return (null!==$adhoc && undefined!==$adhoc && $adhoc.length>0);
                case 'state':
                default: return $adhoc;
            }
        }
    };

})(jQuery);
