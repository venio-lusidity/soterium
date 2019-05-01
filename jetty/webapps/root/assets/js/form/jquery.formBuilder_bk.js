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
                $.htmlEngine.loadFiles(state, null, ['/assets/js/form/css/formBuilder.css']);

                if(!methods.editing()){
                    methods.actions.add();
                }
                else if(methods.editing()) {
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
                    if(!state.loaders('exists')) {
                        state.loaders({type: 'cube'});
                        state.loader = true;
                    }
                    state.loaders('show');
                    methods.html.create();
                    state.loaders('hide');
                },
                cancel: function () {
                    methods.actions.clear();
                    if($.isFunction(state.opts.close)){
                        state.opts.close(state);
                    }
                    state.loaders('hide');
                },
                clear: function(){
                    if(state.opts.panel){
                        state.opts.panel.empty();
                        state.opts.panel.remove();
                    }
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
                create: function () {
                    methods.html.createPanel();
                    var on=0;
                    var isUpload = $.jCommon.string.equals(state.opts.mode, 'upload');
                    state.save = $(document.createElement('button')).attr('type', 'submit')
                        .addClass('btn green').html(isUpload? 'Upload':'Save');
                    state.cancel = $(document.createElement('button')).attr('type', 'button')
                        .addClass('btn ' + (isUpload ? 'red': 'blue')).html(isUpload? 'Stop' : 'Cancel');

                    $.each(state.opts.nodes, function () {
                        var node = methods.html[this.node](this);
                        if(on===0) {
                            if(this.focussed) {
                                node.focus();
                            }
                        }
                        on++;
                    });

                    var btnBar = $(document.createElement('div')).addClass('btn-bar');
                    btnBar.append(state.save);
                    if(state.opts.isDeletable()){
                        var del = $(document.createElement('button')).attr('type', 'button')
                            .addClass('btn red').html('Delete');
                        btnBar.append(del);
                        del.on('click', function () {
                            methods.deleteAction();
                        });
                    }

                    btnBar.append(state.cancel);

                    if(isUpload) {
                        state.cancel.hide();
                    }

                    state.opts.form.append(btnBar);

                    state.cancel.on('click', function () {
                        if(!isUpload) {
                            methods.actions.cancel();
                        }
                    });

                    if(!isUpload) {
                        state.opts.form.on('submit', function (e) {
                            e.preventDefault();
                            e.stopPropagation();
                            methods.submit();
                        });
                    }
                },
                createNode: function (container, config) {
                   return $.formBuilder.createNode(container, config, state);
                },
                createLabel: function (txt, id) {
                    return $.formBuilder.createLabel(text, id);
                },
                createPanel: function () {
                    methods.actions.clear();
                    state.opts.panel = $(document.createElement('div'));
                    state.append(state.opts.panel);
                    state.opts.form = $(document.createElement($.jCommon.string.equals(state.opts.mode, 'upload')? 'div' : 'form'));
                    state.opts.panel.panel({
                        glyph: state.opts.glyph ? state.opts.glyph : "glyphicons glyphicons-file",
                        title: state.opts.title,
                        noHead: state.opts.noHead,
                        url: state.opts.url,
                        borders: state.opts.borders,
                        content: state.opts.form,
                        actions: state.opts.actions
                    });
                    /*
                    state.opts.panel = $(document.createElement('div'))
                        .addClass('panel panel-default').addClass(state.opts.cls).css(state.opts.css);
                    var heading = $(document.createElement('div')).addClass('panel-heading').html(state.opts.title);
                    if(!state.opts.title){
                        heading.hide();
                    }
                    state.opts.panelBody = $(document.createElement('div')).addClass('panel-body');
                    state.opts.panel.append(heading).append(state.opts.panelBody);

                    state.opts.panelBody.append(state.opts.form);
                    state.append(state.opts.panel);
                    */
                },
                checkbox: function (config) {
                    return methods.html.createNode(state.opts.form, config);
                },
                datetime: function(config){
                    config.type = 'text';
                    config.node = 'input';
                    var node = methods.html.input(config);
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
                dropdown: function (config) {
                    config.type = 'text';
                    config.node = 'input';
                    var node = methods.html.input(config);
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
                                config.onChanged(node, null);
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
                input: function (config) {
                    var node = $.formBuilder.input(state.opts.form, config, state);
                    if(config.afterNode && $.isFunction(config.afterNode)){
                        config.afterNode(node);
                    }
                    return node;
                },
                upload: function(config){
                    config.type = 'file';
                    config.node = 'input';

                    var container = $(document.createElement('div'));
                    state.opts.form.append(container);
                    state.opts.form.attr('method', 'post');

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
                principal: function(config){
                    config.node = 'input';
                    var node = methods.html.input(config);
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
                modal: function(config){
                    config.node = 'input';
                    var node = methods.html.input(config);
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
                textarea: function (config) {
                    var node = methods.html.createNode(state.opts.form, config);
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
            transform: function(elem){
                var inputs = elem.find('input');
                var txtAreas = elem.find('textarea');
                var msg, error;
                var data = {};
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
                        msg = ((!label) ? key : label) + ' is required.';
                        return false;
                    }
                    data[key] = val;
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
                            msg = ((!label) ? key : label) + ' is required.';
                            return false;
                        }
                        data[key] = val;
                    });
                }
                if(error){
                    data.error_msg = msg;
                }
                return {data: data, error: error};
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
                            if (!state.loaders('exists')) {
                                state.loaders({type: 'cube'});
                                state.loader = true;
                            }
                            state.loaders('show');

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
                                };
                                var onFail = function (jqXHR, textStatus, errorThrown) {
                                    methods.actions.cancel();
                                    if ($.isFunction(state.opts.onFailed)) {
                                        state.opts.onFailed(jqXHR, textStatus, errorThrown);
                                    }
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
                                    $.htmlEngine.request(url, onSuccess, onFail, state.result, 'post');
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
        show: false,
        mode: 'add',
        defaultData: {},
        eventSuccess: "formEventSuccess",
        eventFailed: "formEventFailed",
        common: {
            width: 300
        }
    };

    $.formBuilder.make = function(container, config, state){

    };

    $.formBuilder.isEditing = function(state){
        return $.jCommon.string.equals(state.opts.mode, 'edit', true);
    };

    $.formBuilder.createNode = function(container, config, state){
        var group = $(document.createElement('div')).addClass('form-group')
            .append($.formBuilder.createLabel(config.label, config.id)).append('<br/>');
        if(config.groupCss){
            group.css(config.groupCss);
        }
        var node = $(document.createElement(config.node)).addClass('form-control');
        var key = (!config.id) ? 'field_' + state.opts.form.children().length : config.id;
        var id = $.formBuilder.getId(key);
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
        container.append(group);
        return node;
    };

    $.formBuilder.getId = function(id){
        return $.jCommon.string.replaceAll(id.replace("/",""), "/", "_");
    };

    $.formBuilder.input = function(container, config, state){
        var node = $.formBuilder.createNode(container, config);
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
        if(state && state.opts.data && $.formBuilder.isEditing(state)){
            if($.jCommon.json.hasProperty(state.opts.data, config.id)){
                node.val(state.opts.data[config.id]);
                setValue(node);
            }
        }
        if(config.onAvailable && $.isFunction(config.onAvailable)){
            config.onAvailable(node);
        }
        return node;
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
