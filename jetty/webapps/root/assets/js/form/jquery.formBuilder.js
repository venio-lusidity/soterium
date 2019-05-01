;
(function ($) {

    //Object Instance
    $.formBuilder = function (el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.formBuilder.defaults, options);
        state.result = state.opts.defaultData;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("formBuilder", state);

        // Private environment methods
        methods = {
            init: function () {
                state.opts.isUpload = $.jCommon.string.equals(state.opts.mode, 'upload', true);
                $.htmlEngine.loadFiles(state, null, ['/assets/js/form/css/formBuilder.css']);
                if(state.opts.isUpload){
                    state.body = $(document.createElement('div'));
                    state.append(state.body);
                }
                else {
                    state.body = methods.html.createPanel();
                }
                if(state.opts.panelCss){
                    state.body.css(state.opts.panelCss);
                }
                var h = state.body.availHeight(10);
                if(state.opts.fill) {
                    dMax(state.body.parent(), h);
                    state.body.css({height: 'inherit'})
                }
                state.form = dCrt(state.opts.isUpload? 'div': 'form');
                state.body.append(state.form);
                if(!methods.editing()){
                    methods.actions.add();
                }
                else if(methods.editing()){
                    methods.actions.edit();
                }
            },
            editing: function(){
                return $.formBuilder.isEditing(state);
            },
            actions: {
                add: function () {
                    if($.isFunction(state.opts.display)){
                        state.opts.display(state);
                    }
                    if(state.opts.steps){
                        methods.steps.init();
                    }
                    else {
                        methods.html.create(state.form, state.opts.nodes);
                        methods.html.commonBtns();
                    }
                },
                cancel: function () {
                    state.loaders('hide');
                    methods.actions.clear();
                    if($.isFunction(state.opts.close)){
                        state.opts.close(state);
                    }
                },
                clear: function(){
                },
                edit: function () {
                    methods.actions.add();
                }
            },
            getId: function(id){
                return $.formBuilder.getId(id);
            },
            getTitle: function(data){
                var result = null;
                if(null!==data && undefined!==data) {
                    result = data.title;
                    if (!result) {
                        var titles = data[state.KEY_TITLE];
                        var uri = data[state.KEY_ID];
                        if (null !== titles && null !== uri) {
                            result = titles.values[0].value;
                        }
                    }
                    if(!result){
                        result = data.value;
                    }
                }
                return result;
            },
            getUri: function(data){
                var result = null;
                if(null!==data && undefined!==data){
                    result = (null!==data.otherId && undefined!==data.otherId) ? data.otherId : data[state.KEY_ID];
                }
                return result;
            },
            html: {
                commonBtns: function () {
                    if(!$.jCommon.string.equals(state.opts.mode, "select")) {
                        var isUpload = state.opts.isUpload;
                        state.save = $(document.createElement('button')).attr('type', 'submit')
                            .addClass('btn green').html(isUpload ? 'Upload' : 'Save');
                        state.cancel = $(document.createElement('button')).attr('type', 'button')
                            .addClass('btn ' + (isUpload ? 'red' : 'blue')).html(isUpload ? 'Stop' : 'Cancel');

                        var btnBar = $(document.createElement('div')).addClass('btn-bar');
                        btnBar.append(state.save);
                        if (state.opts.isDeletable()) {
                            var del = $(document.createElement('button')).attr('type', 'button')
                                .addClass('btn red').html('Delete');
                            btnBar.append(del);
                            del.on('click', function () {
                                methods.deleteAction();
                            });
                        }

                        btnBar.append(state.cancel);

                        if (isUpload) {
                            state.cancel.hide();
                        }

                        state.form.append(btnBar);

                        state.cancel.on('click', function () {
                            if (!isUpload) {
                                methods.actions.cancel();
                            }
                        });

                        if (!isUpload) {
                            state.form.on('submit', function (e) {
                                e.preventDefault();
                                e.stopPropagation();
                                methods.submit();
                            });
                        }
                    }
                },
                create: function (pNode, nodes) {
                    var on=0;
                    $.each(nodes, function () {
                        var node = methods.html[this.node](pNode, this);
                        if(on===0) {
                            if(this.focussed && !state.steps) {
                                node.focus();
                            }
                        }
                        on++;
                    });
                },
                createNode: function (pNode, config) {
                     return $.formBuilder.createNode(pNode, config, state);
                },
                createLabel: function (txt, id) {
                    return $.formBuilder.createLabel(text, id);
                },
                createPanel: function () {
                    methods.actions.clear();
                    var r = $.htmlEngine.panel(
                        state,
                        state.opts.glyph ? state.opts.glyph : "glyphicons glyphicons-file",
                        state.opts.title,
                        null,
                        state.opts.borders,
                        state.opts.actions,
                        state.opts.menu
                    );
                    state.find('.panel').css({height: 'inherit'});
                    if(state.opts.hideHeader){
                        state.panel('hideHeader');
                    }
                    return r;
                },
                br: function (pNode, config) {
                    var node = $(document.createElement('br')).css({clear: 'both'});
                    pNode.append(node);
                    return node;
                },
                raw: function (pNode, config) {
                    var node = config.getNode();
                    pNode.append(node);
                    if(config.onReady){
                        config.onReady(node);
                    }
                    return node;
                },
                repeater: function (pNode, config) {
                    var c = $(document.createElement('div'));
                    pNode.append();
                    pNode.append(c);

                    function row(p){
                        var first = (p.children().length===0);
                        p.find('.repeater-remove').show();
                        var row = $(document.createElement('div')).addClass('repeater').css({paddingLeft: '24px', position: 'relative'});
                        row.attr('key', config.key);
                        p.append(row);
                        methods.html.create(row, config.nodes);
                        $('.form-group').css({marginBottom: '5px'});
                        if(!first) {
                            var del = $(document.createElement('div')).addClass('repeater-remove')
                                .css({position: 'absolute', left: "0", top: '4px'})
                                .append($.htmlEngine.glyph("glyphicon-remove")).css({
                                    color: 'red',
                                    fontSize: '16px',
                                    cursor: 'pointer'
                                });
                            row.append(del);
                            del.on('click', function () {
                                first = (p.children().length === 1);
                                if (first) {
                                    return false;
                                }
                                row.remove();
                            });
                        }
                    }
                    row(c);

                    var a = $(document.createElement("div")).css({marginTop: '5px'});
                    var add = $(document.createElement('button')).attr('type', 'button').addClass('btn blue')
                        .append($.htmlEngine.glyph("glyphicon-plus")).append('&nbsp;Add');
                    a.append(add);

                    add.on('click', function () {
                        if(methods.isValid(c, true, "All rows must be completed or removed.")){
                            $.each(config.nodes, function () {
                               this.label=null;
                            });
                            row(c);
                        }
                    });
                    pNode.append(a);
                    return c;
                },
                checkbox: function (pNode, config) {
                    var c = $(document.createElement('div')).css({clear: 'both'});
                    var node = $(document.createElement('input')).attr('type', 'checkbox');
                    if(config.title){
                        node.attr('title', config.title);
                    }
                    var l = $(document.createElement('label'));
                    c.append(l.append(node));
                    if(config.label){
                        l.append('&nbsp;' + config.label);
                    }
                    if(config.id){
                        node.attr('id', config.id);
                    }
                    if(config.css){
                        c.css(config.css);
                    }
                    if(config.cls){
                        c.addClass(config.cls);
                    }
                    if(config.attr){
                        $.each(config.attr, function (key, value) {
                            node.attr(key, value);
                        })
                    }
                    if(config.onChanged && $.isFunction(config.onChanged)){
                        node.on('change', function () {
                            var checked = node.is(':checked');
                            config.onChanged(node, checked);
                        });
                    }
                    pNode.append(c);
                    $.formBuilder.inputBind(node, pNode, config, state);
                    node.unbind('blur');
                    return node;
                },
                datetime: function(pNode, config){
                    config.type = 'text';
                    config.node = 'input';
                    var node = methods.html.input(pNode, config);
                    var options = {};
                    if(config.max){
                        options.maxDate = config.max;
                    }
                    if(config.min !== undefined){
                        options.minDate = config.min;
                    }
                    node.datepicker(options);
                    return node;
                },
                dropdown: function (pNode, config) {
                    config.type = 'text';
                    config.node = 'input';
                    var node = methods.html.input(pNode, config);
                    if(config.hasCarrot){

                    }
                    var options = [];
                    var selected;
                    var value;
                    $.each(config.options, function () {
                        options.push(this.displayed);
                        if(state.opts.data && methods.editing()){
                            if($.jCommon.json.hasProperty(state.opts.data, config.id)){
                                var d = state.opts.data[config.id];
                                if($.jCommon.string.equals(d, this.value) || $.jCommon.string.equals(d, this.displayed)){
                                    selected = this.displayed;
                                    value = this.value ? this.value : this.displayed;
                                }
                            }
                        }
                        if (this['default'] && !selected) {
                            selected = this.displayed;
                            value = this.value ? this.value : this.displayed;
                        }
                    });
                    options.sort();

                    config.node = 'dropdown';

                    if(selected) {
                        node.val(selected);
                    }
                    if(value) {
                        node.attr('actual', value);
                        state.result[config.id] = value;
                    }
                    
                    node.autocomplete({
                        delay: 0,
                        source: function (request, response) {
                            response(options);
                        },
                        minLength: 0
                    }).focus(function(){
                        $(this).select();
                        $(this).keydown();
                    });
                    node.on( "autocompleteselect", function(event, ui) {
                        if(config.onChanged && $.isFunction(config.onChanged)){
                            window.setTimeout(function(){
                                config.onChanged(node, event, ui);
                            },200);
                        }
                    });
                    node.on('blur', function () {
                        var txt = node.val();
                        if ($.jCommon.string.empty(txt)) {
                            node.val(selected);
                        }
                        else {
                            $.each(config.options, function () {
                                if ($.jCommon.string.equals(txt, this.displayed, true)) {
                                    selected = this.displayed;
                                    value = this.value ? this.value : this.displayed;
                                    return false;
                                }
                            });
                            node.val(selected);
                            node.attr('actual', value);
                            state.result[config.id] = value;
                        }
                    });
                    if(config.options.selectionsCss){
                    }
                    var last = node.val();
                    node.on('keyup', function (e) {
                        e.preventDefault();
                        e.stopPropagation();
                        var txt = node.val();
                        if(!$.jCommon.string.equals(txt, last, true)) {
                            var code = parseInt(e.keyCode);
                            if (code === 27/*esc*/) {
                            }
                            else if (code === 9 /*tab*/) {
                            }
                            else if (code === 38 /*up arrow*/) {
                            }
                            else if (code === 40 /*down arrow*/) {
                            }
                            else if (code !== 13) {
                                var widget = node.autocomplete("widget");
                                var f = 'ui-state-suggested';
                                widget.children().removeClass(f);
                                $.each(widget.children(), function () {
                                    var li = $(this);
                                    var actual = li.html();
                                    if ($.jCommon.string.startsWith(actual, txt, true)) {
                                        li.addClass(f);
                                        node.val(actual);
                                        node.context.setSelectionRange(txt.length, actual.length);
                                        return false;
                                    }
                                });
                            }
                            last = txt;
                        }
                    });
                    if(config.afterNode && $.isFunction(config.afterNode)){
                        config.afterNode(node);
                    }
                    return node;
                },
                input: function (pNode, config) {
                    var node = $.formBuilder.input(pNode, config, state);
                    if(config.afterNode && $.isFunction(config.afterNode)){
                        config.afterNode(node, config);
                    }
                    if(config.authorized){
                        $.login.authorized({"groups": config.authorized}, function (d) {
                            if(!d.auth){
                                node.attr('readonly', true);
                            }
                        });
                    }
                    return node;
                },
                upload: function(pNode, config){
                    config.type = 'file';
                    config.node = 'input';

                    var container = $(document.createElement('div'));
                    state.form.append(container);
                    state.form.attr('method', 'post');

                    var upload = $(document.createElement('span')).addClass('btn green fileinput-button');
                    var icon = $(document.createElement('i')).addClass('glyphicon glyphicon-plus').css({marginRight: '5px'});
                    var msg = $(document.createElement('span')).html(config.placeholder);
                    state.fileUploader = $('<input type="file" name="files[]" multiple>');
                    upload.append(icon).append(msg).append(state.fileUploader);
                    if(config.acceptedFileTypes){
                        state.fileUploader.attr('accept', config.acceptedFileTypes);
                    }

                    container.append(upload);

                    if(config.msg){
                        var required = $(document.createElement('div')).html(config.msg).css({margin: '5px 0 0 0'});
                        container.append(required);
                    }

                    var selected = $(document.createElement('div')).css({margin: '5px 0 0 0'});
                    container.append(selected);

                    var progressOuter = $(document.createElement('div')).addClass('progress yellow').css({marginTop: '10px'});
                    var progressInner = $(document.createElement('div')).addClass('progress-bar progress-bar-success med-black');
                    progressOuter.append(progressInner);
                    container.append(progressOuter);

                    function reset(d){
                        d.files = [];
                        progressInner.css(
                            'width',
                            '0%'
                        );
                        selected.html('');
                    }

                    var userAborted = false;
                    state.fileUploader.fileupload({
                        autoUpload: false,
                        dataType: 'json',
                        type: 'post',
                        maxNumberOfFiles: 1,
                        add: function (e, data) {
                            selected.children().remove();
                            $.each(data.files, function (index, file) {
                                selected.html("Selected: <strong>" + file.name + "</strong>");
                            });
                            state.save.unbind('click').on('click', function () {
                                data.submit();
                            });
                            state.cancel.unbind('click').on('click', function(e){
                                userAborted = true;
                                data.abort();
                                reset(data);
                                selected.html("Upload aborted");
                            });
                        },
                        submit: function(e, data){
                            var url = state.opts.getUrl();
                            if(url){
                                state.save.hide();
                                state.cancel.show();
                                data.url = url;
                            }
                            else{
                                return false;
                            }
                        },
                        done: function (e, data) {
                            state.save.show();
                            state.cancel.hide();
                            if($.isFunction(config.success)){
                                config.success(e, data);
                            }
                            reset(data);
                        },
                        fail: function (e, data) {
                            state.save.show();
                            state.cancel.hide();
                            if(!userAborted) {
                                if ($.isFunction(config.fail)) {
                                    config.fail(e, data);
                                }
                            }
                            reset(data);
                            userAborted = false;
                        },
                        progressall: function (e, data) {
                            var progress = parseInt(data.loaded / data.total * 100, 10);
                            progressInner.css(
                                'width',
                                progress + '%'
                            );
                        }
                    })
                        .prop('disabled', !$.support.fileInput)
                        .parent().addClass($.support.fileInput ? undefined : 'disabled');

                    return container;
                },
                principal: function(pNode, config){
                    config.node = 'input';
                    var node = methods.html.input(pNode, config);
                    node[config.modal.name]({parentNode: node.parent(), node: node, schema: {plugin: config.modal.plugin}});
                    function setValue(elem, displayed, value){
                        elem.val(displayed);
                        elem.attr('actual', value);
                        state.result[config.id] = value;
                    }
                    node.on('modalSelected', function(e){
                        setValue(node, e.selected.displayed, e.selected.value);
                        node.val(e.selected.displayed);
                        node.attr('actual', e.selected.value);
                        state.result[config.id] = e.selected.value;
                        if(config.onChanged && $.isFunction(config.onChanged)){
                            config.onChanged(node, e.item);
                        }
                    });
                    node.on('keyup', function(e){
                        if(e.keyCode===32){
                            try{node[config.modal.name]('show')}catch(e){}
                        }
                    });
                    config.node = 'modal';
                    if(state.opts.data && methods.editing() && $.jCommon.string.startsWith(config.id, '/')) {
                        var success = function (data) {
                            if (data) {
                                data = (data.results) ? data.results[0] : data;
                                var id = methods.getUri(data);
                                var title = methods.getTitle(data);
                                setValue(node, title, id);
                            }
                        };
                        $.htmlEngine.request(methods.getUri(state.opts.data) + '/properties' + config.id, success, null, null, 'get');
                    }
                    return node;
                },
                modal: function(pNode, config){
                    config.node = 'input';
                    var node = methods.html.input(pNode, config);
                    node[config.modal.name]({parentNode: node.parent(), node: node, schema: {plugin: config.modal.plugin}});
                    function setValue(elem, displayed, value){
                        elem.val(displayed);
                        elem.attr('actual', value);
                        state.result[config.id] = value;
                    }
                    node.on('modalSelected', function(e, data){
                        setValue(node, data.selected.displayed, data.selected.value);
                        node.val(data.selected.displayed);
                        node.attr('actual', data.selected.value);
                        state.result[config.id] = data.selected.value;
                        if(config.onChanged && $.isFunction(config.onChanged)){
                            config.onChanged(node, data.item);
                        }
                    });
                    node.on('keyup', function(e){
                        if(e.keyCode===32){
                            try{node[config.modal.name]('show')}catch(e){}
                        }
                    });
                    config.node = 'modal';
                    if(state.opts.data && methods.editing() && $.jCommon.string.startsWith(config.id, '/')) {
                        var success = function (data) {
                            if (data) {
                                data = (data.results) ? data.results[0] : data;
                                var id = methods.getUri(data);
                                var title = methods.getTitle(data);
                                setValue(node, title, id);
                            }
                        };
                        $.htmlEngine.request(methods.getUri(state.opts.data) + '/properties' + config.id, success, null, null, 'get');
                    }
                    return node;
                },
                textarea: function (pNode, config) {
                    var node = methods.html.createNode(pNode, config);
                    node.on('focus', function () {
                        var txt = $(this).val();
                        if (txt) {
                            txt = txt.trim();
                            $(this).val(txt);
                        }
                    });
                    function setValue(elem, value, id){
                        elem.val(value);
                        elem.attr('id', id);
                        if(config.map){
                            if(!config.map.direction){
                                config.map.direction = 'out';
                            }
                            elem.val(value);
                            state.result[config.id] = {vertexType: config.map.vertexType, direction: config.map.direction};
                            state.result[config.id][config.map.key] = value;
                            state.result[config.id][state.KEY_ID] = id;
                        }
                        else {
                            state.result[config.id] = value;
                        }
                    }
                    node.on('blur', function () {
                        setValue(node, node.val(), node.attr('id'));
                    });
                    if(state.opts.data && methods.editing()){
                        if($.jCommon.json.hasProperty(state.opts.data, config.id)){
                            var value = state.opts.data[config.id];
                            if(value.results){
                                value = value.results[0].value;
                            }
                            node.val(value);
                        }
                        else if($.jCommon.string.startsWith(config.id, '/')){
                            var success = function(data){
                                if(data) {
                                    data = (data.results) ? data.results[0]:data;
                                    var id = methods.getUri(data);
                                    var title = methods.getTitle(data);
                                    setValue(node, title, id);
                                }
                            };
                            $.htmlEngine.request(methods.getUri(state.opts.data) + '/properties' + config.id, success, null, null, 'get');
                        }
                    }
                    return node;
                }
            },
            upload: function(){
                state.fileUploader.submit();
            },
            deleteAction: function(){
                if(state.opts.mode==='edit' && state.opts.data && $.isFunction(state.opts.deleteMessage)){
                    var btnBar = $(document.createElement('div')).addClass('btn-bar');
                    var del = $(document.createElement('button')).attr('type', 'button')
                        .addClass('btn btn-danger').html('Delete');
                    btnBar.append(del);
                    del.on('click', function () {
                        state.pageModal('hide');
                        if($.isFunction(state.opts.onDelete)){
                            state.opts.onDelete(state.opts.data);
                        }
                        else{
                            lusidity.info.yellow("The delete function is not defined.");
                            lusidity.info.show(5);
                        }
                    });

                    var cancel = $(document.createElement('button')).attr('type', 'button')
                        .addClass('btn btn-default btn-info').html('Cancel');
                    btnBar.append(cancel);

                    cancel.on('click', function(){
                        state.pageModal('hide');
                        if(state.opts.onCancel){
                            state.opts.onCancel();
                        }
                    });

                    if(!state.pageModal('exists')) {
                        state.pageModal();
                    }
                    state.pageModal('show', {
                        glyph: 'glyphicon-warning-sign yellow',
                        header: 'Delete confirmation required.',
                        body: function(body){
                           state.opts.deleteMessage(body, state.opts.data);
                        },
                        footer: btnBar,
                        hasClose: true});
                }
            },
            isValid: function(elem, show, errorMsg){
                var inputs = elem.find('input');
                var txtAreas = elem.find('textarea');
                var msg, error;
                var result = true;

                $.each(inputs, function(){
                    var input = $(this);
                    var key = input.attr('key');
                    var label = input.attr('label');
                    var val = input.attr('actual');
                    if(!val){
                        val = input.val();
                    }
                    var required = input.attr('required');
                    required = $.jCommon.string.equals(required, 'required');
                    error = (input.hasClass('hasError') || (required && $.jCommon.string.empty(val)));
                    if(error){
                        input.removeClass('hasError').addClass('hasError');
                        msg = (errorMsg) ? errorMsg : ((!label) ? key : label) + ' is required.';
                        result = false;
                    }
                    return result;
                });
                if(!error) {
                    $.each(txtAreas, function () {
                        var input = $(this);
                        var key = input.attr('key');
                        var label = input.attr('label');
                        var val = input.val();
                        var required = input.attr('required');
                        error = (input.hasClass('hasError') || (required && $.jCommon.string.empty(val)));
                        if (error) {
                            msg = (errorMsg) ? errorMsg : ((!label) ? key : label) + ' is required.';
                            result = false;
                        }
                        return result;
                    });
                }
                if(msg && show){
                    lusidity.info.red(msg);
                    lusidity.info.show(5);
                }
                return result;
            },
            transform: function(elem){
                var inputs = elem.find('input');
                var txtAreas = elem.find('textarea');
                var msg, error;
                var data = {};

                function addValue(input, item, repeat) {
                    var repeater = input.attr('repeat');
                    if(!repeater || repeat) {
                        var key = input.attr('key');
                        var label = input.attr('label');
                        var val = input.attr('actual');
                        if (!val) {
                            val = input.val();
                        }
                        var required = input.attr('required');
                        required = $.jCommon.string.equals(required, 'required');
                        error = (input.hasClass('hasError') || (required && $.jCommon.string.empty(val)));
                        if (error) {
                            msg = ((!label) ? key : label) + ' is required.';
                            return false;
                        }
                        item[key] = val;
                    }
                }
                $.each(inputs, function(){
                    var input = $(this);
                    addValue(input, data);
                });
                if(!error) {
                    $.each(txtAreas, function () {
                        var input = $(this);
                        addValue(input, data);
                    });
                }
                var rows = elem.find('.repeater');
                $.each(rows, function () {
                    var row = $(this);
                    inputs = row.find('input');
                    txtAreas = row.find('textarea');
                    var key = row.attr('key');
                    if(key) {
                        if(!data[key]){
                            data[key]=[];
                        }
                        var o = {};
                        $.each(inputs, function () {
                            var input = $(this);
                            addValue(input, o, true);
                        });
                        if (!error) {
                            $.each(txtAreas, function () {
                                var input = $(this);
                                addValue(input, o, true);
                            });
                            data[key].push(o);
                        }
                    }
                    else{
                        error = true;
                        msg = "Repeater is missing key."
                    }
                });
                if(error){
                    data.error_msg = msg;
                }
                return {data: data, error: error};
            },
            steps: {
                init: function () {
                    state.form.parent().css({overflowX: 'hidden', overflowY: 'hidden'});
                    state.form.children().remove();
                    state.form.addClass('form-wizard');
                    var c = dCrt('div').addClass();
                    state.form.append(c);
                    state.steps={on:0, nodes:[], total: 0, left: 0 };

                    state.steps.form = c;
                    methods.steps.next(0);
                },
                create: function (config) {
                    state.steps.total+=1;
                    var node = state.steps.nodes[state.steps.on];
                    if(!node){
                        node = dCrt('div').addClass('node-step');
                        state.steps.nodes.push(node);
                        state.steps.form.append(node);
                        methods.html.create(node, config.nodes);
                    }
                    if(config.css){
                        node.css(config.css);
                    }
                    methods.steps.btnBar(node, config);
                },
                next: function (n) {
                    var last = state.steps.on;
                    var config;
                    if(n>0){
                        if(methods.isValid(state.steps.nodes[last], true)) {
                            config = state.opts.steps[last];
                            if($.isFunction(config.onValidate)){
                                config.onValidate(state.steps.nodes[last]);
                            }
                        }
                        else {return false;}
                    }
                    state.steps.on+=n;
                    if(state.steps.on===state.opts.steps.length){
                        methods.submit();
                    }
                    else {
                        if (state.steps.on <= 0) {
                            state.steps.on = 0;
                        }
                        config = state.opts.steps[state.steps.on];
                        if (!config.init) {
                            methods.steps.create(config);
                        }

                        if (last !== state.steps.on) {
                            if(state.steps.nodes[last]) {
                                state.steps.nodes[last].hide();
                            }
                            state.steps.nodes[state.steps.on].show();
                        }
                        var t = state.opts.title + ': Step ' + (state.steps.on + 1) + ' of ' + state.opts.steps.length;
                        state.panel('updateHeader', {title: t});
                        if (!config.init && $.isFunction(config.onReady)) {
                           config.onReady(state.steps.nodes[state.steps.on]);
                        }
                        config.init = true;
                    }
                },
                btnBar: function (pNode, config) {
                    if(!state.steps.btns){
                        state.css({position: 'relative'});
                        state.steps.btns = dCrt('div').addClass('fw-btn-bar');
                        state.append(state.steps.btns);
                    }
                    state.steps.btns.children().remove();

                    var title = 'Next';
                    if((state.steps.on+1)===state.opts.steps.length) {
                        title='Finish';
                    }
                    state.steps.prev = dCrt('button').attr('type', 'submit').addClass('btn blue').attr('action', 'prev').html('Back');
                    state.steps.next = dCrt('button').attr('type', 'button').addClass('btn blue').attr('action', 'next').html(title);
                    state.steps.bCover = dCrt('div').css({position: 'absolute', top: '0', height: '44px', width: '100%', background: 'transparent' }).hide();

                    function stylize(node) {
                        if(config.buttons.disabled){
                            node.attr('disabled', 'disabled');
                        }
                        if(config.buttons.cls){
                            node.addClass(config.buttons.cls)
                        }
                    }
                    if(config.buttons){
                        stylize(state.steps.prev);
                        stylize(state.steps.next);
                    }
                    if(state.steps.on<=0){
                        state.steps.prev.attr('disabled', 'disabled');
                    }
                    state.steps.btns.append(state.steps.prev).append(state.steps.next).append(state.steps.bCover);

                    state.steps.next.on('click', function (e) {
                        state.steps.prev.removeAttr('disabled');
                        methods.steps.next(1);
                        if((state.steps.on+1)===state.opts.steps.length) {
                            title='Finish';
                        }
                        else{
                            title="Next";
                        }
                        if(config.validate){
                            state.steps.next.attr('disabled', 'disabled');
                        }
                        state.steps.next.html(title);

                    });
                    state.steps.prev.on('click', function (e) {
                        e.preventDefault();
                        e.stopPropagation();
                        if((state.steps.on-1)<0){
                            return false;
                        }
                        if((state.steps.on-1)===0){
                            state.steps.prev.attr('disabled', 'disabled');
                        }
                        else{
                            state.steps.prev.removeAttr('disabled');
                        }
                        state.steps.next.removeAttr('disabled');
                        state.steps.next.html('Next');
                        methods.steps.next(-1);
                    });
                }
            },
            submit: function () {
                if($.jCommon.string.equals(state.opts.mode, 'upload')){

                }
                else {
                    var result = methods.transform(state);
                    if(state.opts.defaultData){
                        result.data = $.extend({},state.opts.defaultData, result.data);
                    }
                    if (result.error && $.isFunction(state.opts.formError)) {
                        state.opts.formError(result.data.error_msg);
                    }
                    else {
                        var msg = {save: true, cancel: false};
                        if ($.isFunction(state.opts.before)) {
                            var r = state.opts.before();
                            if(undefined!==r){
                                msg = r;
                            }
                        }
                        if(msg.save) {
                            state.children().hide();
                            $.htmlEngine.busy(state, {type: 'cube'});
                            window.setTimeout(function () {
                                var onSuccess = function (data) {
                                    state.result = data;
                                    if ($.isFunction(state.opts.onSuccess)) {
                                        state.opts.onSuccess({
                                            item: data,
                                            mode: state.opts.mode,
                                            editing: methods.editing()
                                        });
                                    }
                                    state.loaders('hide');
                                };
                                var onFail = function (jqXHR, textStatus, errorThrown) {
                                    methods.actions.cancel();
                                    if ($.isFunction(state.opts.onFailed)) {
                                        state.opts.onFailed(jqXHR, textStatus, errorThrown);
                                    }
                                    state.loaders('hide');
                                };
                                state.result.vertexType = (!state.result.vertexType) ? (state.opts.vertexType) ? state.opts.vertexType : null : state.result.vertexType;
                                if ($.jCommon.string.empty(result.data.vertexType)) {
                                    state.loaders('hide');
                                    lusidity.info.red('The vertexType is missing.');
                                    lusidity.info.show();
                                    if ($.isFunction(state.opts.onFailed)) {
                                        state.opts.onFailed(qXHR, textStatus, errorThrown);
                                    }
                                }
                                else {
                                    var url = state.opts.getUrl();
                                    if (methods.editing()) {
                                        state.result[state.KEY_ID] = state.opts.data[state.KEY_ID];
                                    }
                                    state.result = $.extend({}, state.result, result.data);
                                    if(state.opts.debug){
                                        console.log(JSON.stringify(state.result));
                                    }
                                    else {
                                        $.htmlEngine.request(url, onSuccess, onFail, state.result, 'post');
                                    }
                                }
                            }, 300);
                        }
                        else if(msg.cancel){
                            methods.actions.cancel();
                        }
                        else{
                            state.loaders('hide');
                        }
                    }
                }
            }
        };

        state.update = function(data){
            if(methods.editing()) {
                state.opts.data = data;
                methods.actions.cancel();
                methods.html.create();
            }
        };
        state.hd = function(options){
            methods.actions.cancel();
        };
        state.clear = function(options){
            methods.actions.cancel();
        };


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.formBuilder.defaults = {
        adjustWidth: 0,
        hideHeader: false,
        show: false,
        mode: 'add',
        defaultData: {},
        eventSuccess: "formEventSuccess",
        eventFailed: "formEventFailed",
        menu: null,
        actions: null,
        padding: 10,
        step:{
            rightAdjust: 10,
            height: 540,
            delay: 300
        },
        common: {
            width: 300
        },
        selectionsCss:  { height: '200px', 'overflow-y': 'auto', 'overflow-x': 'auto' }        
    };

    $.formBuilder.make = function(pNode, config, state){

    };

    $.formBuilder.isEditing = function(state){
        return $.jCommon.string.equals(state.opts.mode, 'edit', true);
    };

    $.formBuilder.createNode = function(pNode, config, state){
        var group = $(document.createElement('div')).addClass('form-group');
        if(!$.jCommon.string.empty(config.label)){
            var l = $.formBuilder.createLabel(config.label, config.id);
            if(config.title){
                l.attr('title', config.title);
            }
            group.append(l).append('<br/>');
        }
        if(config.groupCss){
            group.css(config.groupCss);
        }
        var node = $(document.createElement(config.node)).addClass('form-control');
        var key = config.id;
        var id = (!config.id) ? $.jCommon.getRandomId('field') : config.id;
        node.attr('id', id);
        node.attr('key', key);
        node.attr('label', !config.label ? id : config.label);
        node.attr('required', (config.required) ? config.required :  false);
        if (config.type) {
            node.attr('type', config.type);
        }
        node.width(config.width ? config.width : state ? state.opts.common.width : 300);
        if (config.css) {
            node.css(config.css);
        }
        group.append(node);
        pNode.append(group);
        return node;
    };

    $.formBuilder.getId = function(id){
        return $.jCommon.string.replaceAll(id.replace("/",""), "/", "_");
    };

    $.formBuilder.input = function(pNode, config, state) {
        var node = $.formBuilder.createNode(pNode, config);
        $.formBuilder.inputBind(node, pNode, config, state);
        return node;
    };

    $.formBuilder.inputBind = function(node, pNode, config, state) {
        if (config['default']) {
            node.val(config['default']);
        }
        if (config.min || config.min === 0) {
            node.attr('min', config.min);
        }
        if (config.max) {
            node.attr('max', config.max);// config.max);
        }
        if(config.readOnly){
            node.attr('readonly', 'readonly');
        }
        if(config.placeholder){
            node.attr('placeholder', config.placeholder);
        }
        if(config.value){
            node.val(config.value);
        }
        node.attr('autocomplete', false);
        if(config.attr){
            $.each(config.attr, function (key, value) {
                node.attr(key, value);
            })
        }
        switch (config.type) {
            case 'number':
                if (config.max) {
                    var str = config.max.toString();
                    var width = (str.length * 10) + 30;
                    node.width(width);
                }
                break;
        }
        function setValue(elem){
            if(state) {
                state.result[config.id] = elem.attr('actual') ? elem.attr('actual') : elem.val();
            }
        }
        node.on('blur', function (e) {
            if (config.min && (config.min===0 || node.val()<config.min)) {
                node.val(config.min);
            }
            if (config.max && config.max>0 && node.val()>config.max) {
                node.val(config.max);
            }
            setValue(node);
            if(config.onChanged && $.isFunction(config.onChanged)){
                config.onChanged(node);
            }
        });
        if(state && state.opts.data){
            if($.jCommon.json.hasProperty(state.opts.data, config.id)){
                node.val(state.opts.data[config.id]);
                setValue(node);
            }
        }
        if(config.onAvailable && $.isFunction(config.onAvailable)){
            config.onAvailable(node);
        }
    };

    $.formBuilder.createLabel = function (txt, id) {
        return $(document.createElement('label')).attr('for', id).html(txt);
    };

    //Plugin Function
    $.fn.formBuilder = function (method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function () {
                new $.formBuilder($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $formBuilder = $(this).data('formBuilder');
            switch (method) {
                case 'exists':
                    return (undefined !== $formBuilder && ($formBuilder.length > 0));
                    break;
                case 'update':
                    $formBuilder.update(options);
                    break;
                case 'cancel':
                    $formBuilder.hd(options);
                    break;
                case 'clear':
                    $formBuilder.clear(options);
                    break;
                case 'input':
                    return $formBuilder.input(options);
                    break;
                case 'txtarea':
                    return $formBuilder.txtarea(options);
                    break;
                case 'checkbox':
                    return $formBuilder.checkbox(options);
                    break;
                case "dropdown":
                    return $formBuilder.dropdown(opitions);
                    break;
                case 'state':
                default:
                    return $formBuilder;
                    break;
            }
        }
    };

})(jQuery);
