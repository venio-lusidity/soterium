;(function ($) {
    //Object Instance
    $.workflow = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.workflow.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.KEY_DESC = '/system/primitives/raw_string/descriptions';
        state.current = {stepOn:0};
        state.stepTypes = [
            {displayed: 'Parallel', value: 'parallel'},
            {displayed: 'Sequential', value: 'sequential'}
        ];
        state.actionTypes = [
            {displayed: 'Approve', value: 'approve'},
            {displayed: 'Notify', value: 'notify'},
            {displayed: 'Review', value: 'review'}
        ];
        state.expireTypes = [
            {displayed: 'Hours', value: 'hours'},
            {displayed: 'Days', value: 'days'},
            {displayed: 'Months', value: 'months'},
            {displayed: 'None', value: 'none', 'default': true}
        ];

        state.expireActions = [
            {displayed: 'Reject', value: 'reject', desc: 'Reject to originator.', 'default': true},
            {displayed: 'Previous', value: 'previous', desc: 'Reject to previous step could be originator.'},
            {displayed: 'Next', value: 'next', desc: 'Skip and move on to next step.'}
        ];

        // Store a reference to the environment object
        el.data("workflow", state);

        // Private environment methods
        methods = {
            init: function() {
                methods.menu.init();
            },
            html: {
                panel: function (container, options) {
                    var result = $(document.createElement('div'));
                    options.content = result;
                    options.body = {css:{padding: '0px'}};
                    container.panel(options);
                    return result;
                }
            },
            menu: {
                init: function(){
                    methods.workflow.list.init();
                    methods.menu.workflow.init();
                },
                workflow: {
                    remove: function(){
                        var btnBar = $(document.createElement('div')).addClass('btn-bar');
                        var delButton = $(document.createElement('button')).attr('type', 'button')
                            .addClass('btn btn-danger').html('Delete');
                        btnBar.append(delButton);
                        delButton.on('click', function () {
                           pageCover.busy(true);
                            var s = function (data) {
                                if (data && data.results) {
                                    $.each(data.results, function () {
                                        var item = this;
                                        var success = function (data) {
                                            if (data.error) {
                                                lusidity.info.red(data.error);
                                                lusidity.show(5);
                                            }
                                        };
                                        $.htmlEngine.request(item[state.KEY_ID], s, null, null, 'delete');
                                    });
                                }
                                var success = function(data){
                                    methods.workflow.list.init();
                                };
                                $.htmlEngine.request(state.current.workflowItem[state.KEY_ID], success, null, null, 'delete');
                                state.pageModal('hide');
                                pageCover.busy(false);
                            };
                            var f = function(j,t,e){
                                state.pageModal('hide');
                                pageCover.busy(false);
                            };
                            methods.workflow.step.get(s,f);
                        });

                        var cancel = $(document.createElement('button')).attr('type', 'button')
                            .addClass('btn btn-default btn-info').html('Cancel');
                        btnBar.append(cancel);

                        cancel.on('click', function(){
                            state.pageModal('hide');
                        });

                        if(!state.pageModal('exists')) {
                            state.pageModal();
                        }
                        state.pageModal('show', {
                            glyph: 'glyphicon-warning-sign yellow',
                            header: 'Delete confirmation required.',
                            body: function(body){
                                var title = state.current.workflowItem.title;
                                var msg = $(document.createElement('div')).css({verticalAlign: 'middle', height: '32px'});
                                var question = $(document.createElement('div')).html('Click Delete to delete "<strong>' + title + '</strong>".');
                                msg.append(question);
                                var statement = $(document.createElement('p')).html(
                                    'Once Deleted, there is no way to recover "<strong>' + title + '</strong>".'
                                );
                                body.append(statement).append(msg);
                            },
                            footer: btnBar,
                            hasClose: true
                        });
                    },
                    init: function() {
                        state.opts.workflow.menuNode.menuBar({
                            target: state.opts.workflow.menuNode,
                            buttons: [
                                {id: 'add-workflow', glyphicon: 'glyphicons glyphicons-file-plus', tn: 'add-workflow',
                                    title: 'New Workflow Template', cls: 'green', css: {maxWidth: '40px', maxHeight: '34px', padding: '3px 4px'}},
                                {id: 'delete-workflow', glyphicon: 'glyphicons glyphicons-file-minus', tn: 'delete-workflow',
                                    title: 'Delete Workflow Template', cls: 'red', css: {maxWidth: '40px', maxHeight: '34px', padding: '3px 4px'}, disabled: true},
                                {name: 'sep'},
                                {id: 'edit-workflow', glyphicon: 'glyphicon-plus', tn: 'edit-workflow', title: 'Add Workflow Step', cls: 'green', disabled: true}
                            ]
                        });
                        var nodeDel = $('#delete-workflow');
                        var nodeEdit = $('#edit-workflow');
                        state.opts.workflow.menuNode.on('menu-bar-edit-workflow', function(){
                            if(state.current.workflowItem) {
                               methods.workflow.load(true);
                            }
                        });
                        state.opts.workflow.menuNode.on('menu-bar-delete-workflow', function(){
                            methods.menu.workflow.remove();
                        });
                        state.opts.workflow.menuNode.on('menu-bar-add-workflow', function () {
                            var previousVisible = [];
                            $.each(state.opts.pnlMiddle.children(), function() {
                                if ($(this).is(":visible")) {
                                    previousVisible.push($(this));
                                }
                                $(this).hide();
                            });
                            state.opts.pnlMiddle.loaders('show');
                            state.opts.previousVisible = previousVisible;

                            state.opts.workflow.formNode.children().remove();
                            state.opts.workflow.formNode.show();

                            var form = $(document.createElement('div'));
                            state.opts.workflow.formNode.append(form);

                            methods.workflow.form(form, "Create New Workflow", null, true);
                            methods.workflow.diagram.init("New Workflow Model", 'glyphicons glyphicons-flowchart');
                            if(state.opts.workflow.list){
                                var n = state.opts.workflow.list.find('.selected');
                                if(n.length>0){
                                    n.removeClass('selected');
                                }
                            }
                            nodeDel.attr('disabled','disabled');
                            nodeEdit.attr('disabled','disabled');
                        });
                    }
                }
            },
            workflow: {
                load: function(edit){
                    var s = function(data){
                        state.current.stepOn = 0;
                        methods.workflow.diagram.init(state.current.workflowItem.title, 'glyphicons glyphicons-flowchart', data);
                        if(edit) {
                            methods.workflow.step.init(null, state.current.stepOn);
                        }
                    };
                    methods.workflow.step.get(s);
                },
                form: function (container, title, data, show) {
                    var mode = !data ? 'add' : 'edit';
                    container.formBuilder({
                        title: title,
                        borders: false,
                        css: {'margin-right': '0'},
                        panelCss: {margin: '10px'},
                        glyph: 'glyphicons glyphicons-file',
                        url: null,
                        actions: [],
                        show: false,
                        data: data,
                        defaultData:{
                            vertexType: '/process/workflow/workflow'
                        },
                        mode: mode,
                        before: function () {
                        },
                        isDeletable: function () {
                            return (null!==data);
                        },
                        deleteMessage: function (body, data) {
                            var title = methods.getTitle(data);
                            var msg = $(document.createElement('div')).css({verticalAlign: 'middle', height: '32px'});
                            var question = $(document.createElement('div')).html('Click Delete to delete "<strong>' + title + '</strong>".');
                            msg.append(question);
                            var statement = $(document.createElement('p')).html(
                                'Once Deleted, there is no way to recover "<strong>' + title + '</strong>".'
                            );
                            body.append(statement).append(msg);
                        },
                        onDelete: function (item) {
                        },
                        close: function (node) {
                            state.opts.workflow.formNode.hide();
                            if(undefined!== state.opts.previousVisible && null!==state.opts.previousVisible){
                                $.each(state.opts.previousVisible, function(){
                                    $(this).show();
                                });
                            }
                            state.opts.previousVisible = null;
                        },
                        display: function (node) {
                        },
                        formError: function (msg) {
                            lusidity.info.red(msg);
                            lusidity.info.show(5);
                        },
                        onSuccess: function (data) {
                            container.loaders('hide');
                            if(data.item && data.item.result) {
                                state.current.workflowItem = data.item.result;
                                methods.workflow.list.init();
                                state.current.stepOn = 0;
                                methods.workflow.load(true);
                            }
                        },
                        onFailed: function () {
                            lusidity.info.red("Sorry, something went wrong.");
                            lusidity.info.show(5);
                        },
                        nodes: [
                            {
                                focussed: false,
                                node: 'modal',
                                type: 'text',
                                placeholder: 'Click to Select...',
                                modal: {
                                    name: "listViewModal",
                                    plugin: {
                                        title: 'Select a Workflow Type',
                                        glyph: 'glyphicons glyphicons-flowchart',
                                        get: {
                                            rootUrl: function (data) {
                                                return "/workflow/types";
                                            }
                                        },
                                        mapper: {
                                            value: state.KEY_ID,
                                            displayed: 'title',
                                            description: 'description'
                                        },
                                        limit: 10000,
                                        fade: false
                                    }
                                },
                                linked: true,
                                id: 'workflowType',
                                label: 'Workflow Type',
                                readOnly: true,
                                required: true,
                                onChanged: function(node, item){
                                    var text = node.val();
                                    if(text) {
                                        var title = $('#title');
                                        if (title.length > 0) {
                                            var v = title.val();
                                            if ($.jCommon.string.empty(v)) {
                                                title.focus();
                                                title.val(text);
                                                title.blur();
                                                methods.workflow.diagram.changeHeader(text);
                                            }
                                        }
                                    }
                                    if(item && item.description){
                                        var desc = $('#system_primitives_raw_string_descriptions');
                                        var v = desc.val();
                                        if($.jCommon.string.empty(v)){
                                            desc.focus();
                                            desc.val(item.description);
                                            desc.blur();
                                        }
                                    }
                                }
                            },
                            {
                                node: 'input',
                                type: 'text',
                                required: true,
                                id: 'title',
                                label: "Title",
                                placeholder: 'Enter a friendly name.',
                                onChanged: function(node){
                                    var text = node.val();
                                    if(text) {
                                        methods.workflow.diagram.changeHeader(text);
                                    }
                                    else{
                                        methods.workflow.diagram.changeHeader('New workflow Model');
                                    }
                                }
                            },
                            {
                                node: 'textarea',
                                required: false,
                                id: state.KEY_DESC,
                                map: {
                                    direction: 'out',
                                    key: 'value',
                                    vertexType: '/system/primitives/raw_string'
                                },
                                label: "Description",
                                css: {width: '100%', height: '100px'}
                            }
                        ],
                        getUrl: function () {
                            return (null===data) ? '/domains/' + 'process_workflow_workflow' + '/new' : data[state.KEY_ID];
                        }
                    });
                },
                diagram: {
                    test: function(workflowType) {
                        var on = 0;
                        function test(workflowType) {
                            window.setTimeout(function () {
                                methods.workflow.diagram.insert({
                                    title: "Step " + (on + 1),
                                    workflowType: workflowType
                                }, 'square blue', true);
                                on++;
                                if (on < 1) {
                                    test('sequential');
                                }
                                else if (on < 3) {
                                    test('parallel');
                                }
                                else if (on < 6) {
                                    test('sequential');
                                }
                            }, 500);
                        }
                        test(workflowType);
                    },
                    changeHeader: function(title, glyph, url){
                        var options = {};
                        if(title){
                            options.title = title;
                        }
                        if(glyph){
                            options.glyph = glyph;
                        }
                        if(url){
                            options.url = url;
                        }
                        state.diagram.nodePanel.panel('updateHeader', options);
                    },
                    init: function(title, glyph, data){
                        if(state.diagram && state.diagram.root){
                            state.diagram.node.remove();
                            state.diagram.root.remove();
                        }
                        state.diagram = {rows:0,top:0};
                        var h = state.opts.pnlRight.innerHeight() + 'px';
                        state.diagram.root = $(document.createElement('div')).css({minHeight: h, height: h});
                        state.opts.pnlRight.append(state.diagram.root);

                        var options = {
                            glyph: glyph,
                            title: title,
                            url: null,
                            borders: false,
                            actions: []
                        };
                        state.diagram.nodePanel = $(document.createElement('div'));
                        state.diagram.root.append(state.diagram.nodePanel);
                        state.diagram.nodePanel.css({minHeight: 'inherit', height: 'inherit', position: 'relative'});
                        state.diagram.node = methods.html.panel(state.diagram.nodePanel, options);
                        state.diagram.node.addClass('workflow-diagram');
                        state.diagram.node.jNodeReady({onReady: function(){
                            state.diagram.node.connector({distance: 50, offset:{x: 10,y: 60}});
                            state.diagram.start = methods.workflow.diagram.insert({title: 'Start', workflowType: 'sequential'}, 'circle green wf-start', false);
                            state.diagram.end = methods.workflow.diagram.insert({title: 'End', workflowType: 'sequential'}, 'circle red wf-end', false);
                            state.diagram.last = state.diagram.start;
                            //methods.workflow.diagram.test('sequential');
                            if(data && data.results){
                                state.current.stepOn = 0;
                                var items = $.jCommon.array.sort(data.results, true, ["ordinal"]);
                                $.each(items, function(){
                                    var item = this;
                                    item.stepOn = state.current.stepOn;
                                    if(item.ordinal!==item.stepOn){
                                        item.ordinal=item.stepOn;
                                        var s = function(data){};
                                        var f = function(){};
                                        $.htmlEngine.request(item[state.KEY_ID], s, f, item, 'post', true);
                                    }
                                    state.current.stepOn++;
                                    item.title = "Step " + (item.stepOn+1);
                                    var node = methods.workflow.diagram.insert({
                                        title: item.title,
                                        item: item,
                                        workflowType: item.stepType
                                    }, 'square blue', true);
                                });
                            }
                        }});
                    },
                    connect: function(fromNode, toNode, dot, pointer, status){
                        state.diagram.node.connector('connect', {
                            type: 'elbow',
                            status: status ? status : 'blue',
                            fromNode: fromNode,
                            toNode: toNode,
                            horizontal_gap: 0,
                            darkLineWidth: 4,
                            lightLineWidth: 2,
                            fromDot: dot,
                            toArrow: pointer,
                            error: true
                        });
                    },
                    insert: function(data, cls, clickable){
                        if(data.item){
                            data.item.title = data.title;
                        }
                        var node = $(document.createElement('div')).addClass('node ' + cls).html(data.title);
                        node.data('item', data).css({cursor: 'pointer'});

                        if(data && data.item && data.item.expireType){
                            var s = $.htmlEngine.glyph("glyphicons-clock glyph-step");
                            node.append(s);

                            var i;
                            switch (data.item.expireAction){
                                case 'next':
                                    i = $.htmlEngine.glyph('glyphicons glyphicons-arrow-right');
                                    break;
                                case 'previous':
                                    i = $.htmlEngine.glyph('glyphicons glyphicons-arrow-left');
                                    break;
                                case 'reject':
                                    i = $.htmlEngine.glyph('glyphicons glyphicons-fire');
                                    break;
                            }
                            node.append(i.css({position: 'relative', top: '-10px'}));
                        }

                        if(state.diagram.last){
                            node.insertAfter(state.diagram.last);
                        }
                        else{
                            state.diagram.node.append(node);
                        }
                        if(state.diagram.end && state.diagram.end.length>0) {
                            state.diagram.node.connector('disconnect', {
                                fromNode: state.diagram.last,
                                toNode: state.diagram.end
                            });
                        }
                        methods.workflow.diagram.move(node, data);
                        if(node.prev().length>0) {
                            var prev = node.prev();
                            if(prev && prev.hasClass('node')) {
                                function find(n) {
                                    var r = null;
                                    if (n.length > 0 && n.hasClass('node')) {
                                        if (n.data('item').workflowType === 'sequential') {
                                            r = n;
                                        }
                                        else if (n.prev().length > 0) {
                                            r = find(n.prev());
                                        }
                                    }
                                    return r;
                                }

                                var a = (node.data('item').workflowType === 'sequential') ?
                                    prev :
                                    find(prev);
                                if (a && a.length > 0) {
                                    methods.workflow.diagram.connect(a, node, true, true);
                                }
                            }
                        }
                        if(state.diagram.end && state.diagram.end.length>0) {
                            methods.workflow.diagram.move(state.diagram.end);
                            methods.workflow.diagram.connect(node, state.diagram.end, true, true);
                            state.diagram.top = node.position().top;
                        }
                        state.diagram.last = node;
                        if(clickable) {
                            node.on('click', function () {
                                methods.workflow.step.init(data.item, data.item.stepOn);
                                $(".page-content").animate({scrollTop: 0}, "slow");
                            });
                        }
                        else{ node.css({cursor: ''});}
                        return node;
                    },
                    move: function(node) {
                        var type = node.data('item').workflowType;
                        methods.workflow.diagram.types[type](node);
                    },
                    moveTo: function(node, top, left){
                        if (top + node.outerHeight() > state.diagram.node.innerHeight()) {
                            var h = state.diagram.node.height() + node.outerHeight()+50;
                            state.diagram.node.height(h);
                        }
                        node.css({left: left + 'px', top: top + 'px'});
                    },
                    types: {
                        sequential: function(node){
                            var gap = 50;
                            var prev = node.prev();
                            state.diagram.top = ((prev.position() && prev.hasClass('node')) ? prev.position().top + prev.outerHeight() + gap:5);
                            if(!state.diagram.left){
                                state.diagram.left = ((state.diagram.node.innerWidth()/2) - (node.outerWidth()/2))
                            }
                            methods.workflow.diagram.moveTo(node, state.diagram.top, state.diagram.left);
                        },
                        parallel: function(node){
                            var gap = 50;
                            var prev = node.prev();
                            var left=0;
                            if(prev.data('item').workflowType === 'sequential'){
                                state.diagram.top = ((prev.position() && prev.hasClass('node')) ? prev.position().top + prev.outerHeight() + gap  : 0);
                                left = 5;
                            }
                            else if(prev.data('item').workflowType === 'parallel'){
                                left = ((prev.position() && prev.hasClass('node')) ? prev.position().left + prev.outerWidth() + gap : 0);
                            }

                            if ((left + node.outerWidth() + 5) > state.diagram.node.connector('size').width) {
                                state.diagram.rows += 1;
                                state.diagram.top += node.outerHeight() + gap;
                                left = 5;
                            }
                            methods.workflow.diagram.moveTo(node, state.diagram.top, left);
                        }
                    }
                },
                list: {
                    init: function(){
                        if(state.opts.workflow.formNode) {
                            state.opts.workflow.formNode.children().remove();
                            state.opts.workflow.formNode.hide();
                        }
                        if(state.diagram){
                            state.diagram.root.children().remove();
                        }
                        var s = function(data){
                            methods.workflow.list.create(state.opts.workflow.list, data);
                        };
                        var f = function(){

                        };
                        $.htmlEngine.request('/workflow/workflows', s, f, null, 'get', true);
                    },
                    create: function(container, data){
                        if(data && data.results){
                            container.children().remove();
                            var group = $(document.createElement('div')).addClass('results-group');
                            container.append(group);
                            $.each(data.results, function(){
                                var item = this;
                                var strong = $(document.createElement('strong')).html(item.title);
                                var wf = $(document.createElement('div')).addClass('result-group-item selectable').append(strong);
                                if(item[state.KEY_DESC]){
                                    try {
                                        var d = item[state.KEY_DESC].results[0].value;
                                        var desc = $(document.createElement('div')).html(d).css({
                                            marginLeft: '15px',
                                            marginRight: '15px'
                                        });
                                        wf.append(desc);
                                    }catch(e){}
                                }
                                if(state.current.workflowItem && state.current.workflowItem[state.KEY_ID] === item[state.KEY_ID]){
                                    wf.addClass('selected');
                                }
                                group.append(wf);

                                wf.on('click', function(){
                                    state.opts.workflow.formNode.children().remove();
                                    state.opts.workflow.formNode.show();
                                    $('#delete-workflow').removeAttr('disabled');
                                    $('#edit-workflow').removeAttr('disabled');
                                    group.children().removeClass('selected');
                                    wf.addClass('selected');
                                    state.current.workflowItem = item;
                                    methods.workflow.load(false);
                                });
                            });
                        }
                    }
                },
                step: {
                    get: function(success, failed){
                        var s = function(data){
                            if($.isFunction(success)){
                                success(data);
                            }
                        };
                        var f = function(j,t,e){
                            if($.isFunction(failed)){
                                failed(j,t,e);
                            }
                            else {
                                lusidity.info.red("Sorry, something went wrong.");
                                lusidity.info.show(5);
                            }
                        };
                        var url = state.current.workflowItem[state.KEY_ID] + '/properties/process/workflow/workflow_step/steps?direction=out&sortOn=ordinal';
                        $.htmlEngine.request(url, s, f, null, 'get', true);
                    },
                    init: function(data, stepOn){
                        state.current.steps = [];
                        state.opts.workflow.formNode.children().remove();
                        state.opts.workflow.formNode.show();
                        var form = $(document.createElement('div'));
                        form.width(state.opts.workflow.formNode.innerWidth());
                        state.opts.workflow.formNode.append(form);
                        methods.workflow.step.form(form, state.current.workflowItem.title + ": Step " + (stepOn+1), data);

                        lusidity.environment('onResize', function(){
                            window.setTimeout(function(){
                                $.each(state.current.steps, function(){
                                   var step = this;
                                   step.node.width(state.opts.workflow.formNode.innerWidth());
                                });
                            }, 300);
                        });
                    },
                    form: function(container, title, data, show) {
                        var mode = !data ? 'add' : 'edit';
                        var ordinal = $.jCommon.string.equals(mode, 'add') ? state.current.stepOn : data.stepOn;
                        container.formBuilder({
                            title: title,
                            borders: false,
                            css: {'margin-right': '0'},
                            panelCss: {margin: '10px'},
                            glyph: 'glyphicons glyphicons-inbox-plus',
                            url: null,
                            actions: [],
                            show: false,
                            data: data,
                            defaultData:{
                                vertexType: '/process/workflow/workflow_step',
                                edgeKey: '/process/workflow/workflow_step/steps',
                                edgeDirection: 'out',
                                fromId: state.current.workflowItem[state.KEY_ID],
                                ordinal: ordinal
                            },
                            mode: mode,
                            before: function () {
                            },
                            isDeletable: function () {
                                return true;
                            },
                            deleteMessage: function (body, data) {
                                var title = data.title;
                                var msg = $(document.createElement('div')).css({verticalAlign: 'middle', height: '32px'});
                                var question = $(document.createElement('div')).html('Click Delete to delete "<strong>' + title + '</strong>".');
                                msg.append(question);
                                var statement = $(document.createElement('p')).html(
                                    'Once Deleted, there is no way to recover "<strong>' + title + '</strong>".'
                                );
                                body.append(statement).append(msg);
                            },
                            onDelete: function (item) {
                                var success = function (data) {
                                    if (!data.error) {
                                        methods.workflow.load(true);
                                    }
                                    else {
                                        lusidity.info.red(data.error);
                                        lusidity.show(5);
                                    }
                                };
                                $.htmlEngine.request(item[state.KEY_ID], success, null, null, 'delete');
                            },
                            close: function (node) {
                                state.opts.workflow.formNode.hide();
                                if(undefined!== state.opts.previousVisible && null!==state.opts.previousVisible){
                                    $.each(state.opts.previousVisible, function(){
                                        $(this).show();
                                    });
                                }
                                state.opts.previousVisible = null;
                            },
                            display: function (node) {
                            },
                            formError: function (msg) {
                                lusidity.info.red(msg);
                                lusidity.info.show(5);
                            },
                            onSuccess: function (data) {
                                container.loaders('hide');
                                if(data && data.item && data.item.result) {
                                    methods.workflow.load(!$.jCommon.string.equals(data.mode, 'edit'))
                                }
                                else{
                                    lusidity.info.red("Sorry, something went wrong.");
                                    lusidity.info.show(5);
                                }
                            },
                            onFailed: function () {
                                lusidity.info.red("Sorry, something went wrong.");
                                lusidity.info.show(5);
                            },
                            nodes: [
                                {
                                    focussed: false,
                                    node: 'dropdown',
                                    required: true,
                                    id: 'stepType',
                                    label: "Step type",
                                    placeholder: 'Select a type...',
                                    options: state.stepTypes,
                                    css:{width: '100%'}
                                },
                                {
                                    node: 'dropdown',
                                    required: true,
                                    id: 'actionType',
                                    label: "Action Type",
                                    placeholder: 'Select a type...',
                                    options: state.actionTypes,
                                    css:{width: '100%'}
                                },
                                {
                                    node: 'principal',
                                    type: 'text',
                                    placeholder: 'Click to Select...',
                                    modal: {
                                        name: "principalModal",
                                        plugin: {
                                            title: 'User, Groups and Roles Selector',
                                            glyph: 'glyphicons glyphicons-group',
                                            get: {
                                                rootUrl: function (data) {
                                                    return '/acs/security/principals';
                                                }
                                            },
                                            mapper: {
                                                value: state.KEY_ID,
                                                title: 'title',
                                                description: 'description'
                                            },
                                            limit: 10000,
                                            fade: false
                                        }
                                    },
                                    linked: true,
                                    id: '/acs/security/base_principal/auditors',
                                    label: 'Auditor',
                                    readOnly: true,
                                    required: true,
                                    onChanged: function(node, item){
                                        if(node && item) {
                                            var text = node.val();
                                            if (text) {
                                                var title = $('#title');
                                                if (title.length > 0) {
                                                    var v = title.val();
                                                    if ($.jCommon.string.empty(v)) {
                                                        title.focus();
                                                        title.val(text);
                                                        title.blur();
                                                        methods.workflow.diagram.changeHeader(text);
                                                    }
                                                }
                                            }
                                            if (item.description) {
                                                var desc = $('#system_primitives_raw_string_descriptions');
                                                var v = desc.val();
                                                if ($.jCommon.string.empty(v)) {
                                                    desc.focus();
                                                    desc.val(item.description);
                                                    desc.blur();
                                                }
                                            }
                                        }
                                    }
                                },
                                {
                                    node: 'dropdown',
                                    required: false,
                                    id: 'expireType',
                                    label: "Expire unit",
                                    placeholder: 'Select a unit...',
                                    options: state.expireTypes,
                                    groupCss: {display: "inline-block"},
                                    css:{width: '150px'},
                                    onChanged: function(node){
                                        var txt = node.val();
                                        var n = $('#expiresIn');
                                        if($.jCommon.string.equals(txt, 'none', true)){
                                            n.focus();n.val(0);n.blur();
                                            n.attr('disabled', 'disabled');
                                        }
                                        else{
                                            n.removeAttr('disabled');
                                        }
                                    }
                                },
                                {
                                    node: 'input',
                                    type: 'number',
                                    required: false,
                                    min: 0,
                                    max: 0,
                                    id: 'expiresIn',
                                    label: "Expires In",
                                    groupCss: {display: "inline-block", marginLeft: '10px'},
                                    css:{width: '100px'},
                                    afterNode: function(node){
                                        var txt = $('#expireType').val();
                                        if($.jCommon.string.equals(txt, 'none', true)){
                                            node.attr('disabled', 'disabled');
                                        }
                                        else{
                                            node.removeAttr('disabled');
                                        }
                                    },
                                    onChanged: function(node){
                                        var v = node.val();
                                        var n = $('#expireAction');
                                        if(null===v || undefined===v || v<=0){
                                            n.attr('disabled', 'disabled');
                                        }
                                        else{
                                            n.removeAttr('disabled');
                                        }
                                    }
                                },
                                {
                                    node: 'dropdown',
                                    required: false,
                                    id: 'expireAction',
                                    label: "Expire Action",
                                    placeholder: 'Select a type...',
                                    groupCss: {display: "inline-block", marginLeft: '10px'},
                                    options: state.expireActions,
                                    css:{width: '100px'},
                                    afterNode: function(node){
                                        var txt = $('#expireType').val();
                                        if($.jCommon.string.equals(txt, 'none', true)){
                                            node.attr('disabled', 'disabled');
                                        }
                                        else{
                                            node.removeAttr('disabled');
                                        }
                                    }
                                },
                                {
                                    node: 'textarea',
                                    'required': false,
                                    id: state.KEY_DESC,
                                    map: {
                                        direction: 'out',
                                        key: 'value',
                                        vertexType: '/system/primitives/raw_string'
                                    },
                                    label: "Description",
                                    css: {width: '100%', height: '100px'}
                                }
                            ],
                            getUrl: function () {
                                return (null===data) ? '/domains/' + 'vertexType' + '/new' : data[state.KEY_ID];
                            }
                        });
                    }
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.workflow.defaults = {
        some: 'default values'
    };


    //Plugin Function
    $.fn.workflow = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.workflow($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $workflow = $(this).data('workflow');
            switch (method) {
                case 'exists': return (null!==$workflow && undefined!==$workflow && $workflow.length>0);
                case 'state':
                default: return $workflow;
            }
        }
    };

})(jQuery);
