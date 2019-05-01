

;(function ($) {
    //Object Instance
    $.fileWorker = function(el, options) {
        var state = el,
            methods = {};
        state.started=false;
        state.KEY_ID = '/vertex/uri';
        state.opts = $.extend({}, $.fileWorker.defaults, options);
        state.types = [
            {displayed: 'COAMS Locations', value: 'com.lusidity.rmk.importer.coams.CoamsLocationImporter'},
            {displayed: 'COAMS Organizations', value: 'com.lusidity.rmk.importer.coams.CoamsOrgImporter'},
            {displayed: 'COAMS Confidentiality Level', value: 'com.lusidity.rmk.importer.coams.CoamsConfidentialityImporter'},
            {displayed: 'COAMS System (formerly Accreditation)', value: 'com.lusidity.rmk.importer.coams.CoamsAccreditationImporter'},
            {displayed: 'COAMS Roles', value: 'com.lusidity.rmk.importer.coams.CoamsRoleImporter'},
            {displayed: 'Common Vulnerabilities and Exposures', value: 'com.lusidity.rmk.importer.cve.CveImporter'},
            {displayed: 'HBSS Baseline Compliance (2003 xls file only)', value: 'com.lusidity.rmk.importer.compliance.CompliancePolicyImporter'},
            {displayed: 'HBSS Baseline Compliance v2 (2003 xls file only)', value: 'com.lusidity.rmk.importer.compliance.SimpleCompliancePolicyImporter'},
            {displayed: 'IAVM', value: 'com.lusidity.rmk.importer.iavm.IavmImporter'},
            {displayed: 'Security Center Plugin to IAVM (txt file only)', value: 'com.lusidity.rmk.importer.misc.ScpToIavmImporter'},
            {displayed: 'Microsoft Security Bulletin', value: 'com.lusidity.rmk.importer.msb.MsbImporter'},
            {displayed: 'STIG', value: 'com.lusidity.rmk.importer.stig.StigImporter'},
            {displayed: 'SCCM', value: 'com.lusidity.rmk.importer.msc.SccmImporter'},
            {displayed: 'HBSS Assets', value: 'com.lusidity.rmk.importer.hbss.HbssAssetsImporter'},
            {displayed: 'HBSS Deleted', value: 'com.lusidity.rmk.importer.hbss.HbssDeletedImporter'},
            {displayed: 'HBSS Light', value:
            'com.lusidity.rmk.importer.hbss.HbssAssetsImporter,' +
            'com.lusidity.rmk.importer.hbss.HbssOpsImporter,' +
            'com.lusidity.rmk.importer.hbss.HbssDeletedImporter'},
            {displayed: 'HBSS Ops', value: 'com.lusidity.rmk.importer.hbss.HbssOpsImporter'},
            {displayed: 'HBSS STIG', value: 'com.lusidity.rmk.importer.hbss.HbssStigImporter'},
            {displayed: 'HBSS Software', value: 'com.lusidity.rmk.importer.hbss.HbssSoftwareImporter'},
            {displayed: 'HBSS All (No Software)', value:
            'com.lusidity.rmk.importer.hbss.HbssAssetsImporter,' +
            'com.lusidity.rmk.importer.hbss.HbssOpsImporter,' +
            'com.lusidity.rmk.importer.hbss.HbssDeletedImporter,' +
            'com.lusidity.rmk.importer.hbss.HbssStigImporter'},
            {displayed: 'HBSS All', value:
            'com.lusidity.rmk.importer.hbss.HbssAssetsImporter,' +
            'com.lusidity.rmk.importer.hbss.HbssSoftwareImporter,' +
            'com.lusidity.rmk.importer.hbss.HbssOpsImporter,' +
            'com.lusidity.rmk.importer.hbss.HbssDeletedImporter,' +
            'com.lusidity.rmk.importer.hbss.HbssStigImporter'},
            {displayed: 'Blade Logic', value:'com.lusidity.rmk.importer.acss.BladeLogicSoftwareImporter,' +
            'com.lusidity.rmk.importer.acss.BladeLogicComplianceImporter,' +
            'com.lusidity.rmk.importer.acss.BladeLogicCveImporter'},
            {displayed: 'ACAS', value: 'com.lusidity.rmk.importer.acas.AcasApiImporter'},
            {displayed: 'CPE', value: 'com.lusidity.rmk.importer.cpe.CpeImporter'},
            {displayed: 'Jobs', value: 'com.lusidity.rmk.importer.jobs.JobsImporter'},
            {displayed: 'Delete Vertices (cannot be undone once started)', value: 'com.lusidity.rmk.importer.misc.DeleteImporter'},
            {displayed: 'Release Notes (json file only)', value: 'releaseNoteFiles'},
            {displayed: 'Upload a File', value: 'miscFiles', all: true},
            {displayed: 'User Guide', value: 'userGuideFiles', all: true}
        ];
        state.sUrl = lusidity.environment('host-secondary');
        // Store a reference to the environment object
        el.data("fileWorker", state);

        // Private environment methods
        methods = {
            init: function() {
                state.opts.menuNode.menuBar({target: state.opts.menuNode, upload: "Import data",
                    buttons:[]
                });
                var options = {
                    noHead: true,
                    show: true,
                    data: null,
                    mode: 'upload',
                    isDeletable: function () {
                        return false;
                    },
                    deleteMessage: function (body, data) {
                    },
                    cls: 'no-border no-radius',
                    css: {'margin-right': '0'},
                    nodes: [
                        {
                            node: 'dropdown',
                            required: true,
                            id: 'importType',
                            label: "Type of Import",
                            placeholder: 'Select an import type...',
                            options: state.types,
                            css:{width: '100%'}
                        },
                        {
                            node: 'upload',
                            required: true,
                            id: 'uploader',
                            acceptedFileTypes: '.zip',
                            placeholder: "Select a file to upload",
                            msg: 'If you have a large XML file or a group of files including already zipped files, please compress them into one zip file before uploading.',
                            success: function(e, data){
                                state.opts.menuNode.pageModal('hide');
                            },
                            fail: function(e, data){
                                state.opts.menuNode.pageModal('hide');
                                lusidity.info.red('Something happened during the upload please try again.');
                                lusidity.info.show(5);
                            }
                        }
                    ],
                    getUrl: function(url){
                        var result;
                        var type = $('#importType');
                        var actual = type.attr('actual');
                        if(actual){
                            result = state.sUrl +  "/fileupload/importer?type="+actual;
                        }
                        if(undefined===result){
                            type.focus();
                        }
                        return result;
                    }
                };
                state.opts.menuNode.on('menu-bar-upload', function(){
                    if(!state.opts.menuNode.pageModal('exists')) {
                        state.opts.menuNode.pageModal();
                        state.opts.menuNode.pageModal('show', {
                            glyph: 'glyphicon-upload',
                            header: 'Import data',
                            body: function(body){
                                var form = $(document.createElement('div'));
                                body.append(form);
                                form.formBuilder(options);
                            },
                            footer: null,
                            hasClose: true});
                    }
                    state.opts.menuNode.pageModal('show');
                });
                methods.getData();
            },
            getData: function(){
                var success = function(data){
                    methods.html.create(data);
                    window.setTimeout(function(){
                        methods.getData();
                    }, state.opts.ttw);
                };
                var failed = function(jqXHR, textStatus, errorThrown){

                    window.setTimeout(function(){
                        window.location = window.location.href;
                    }, state.opts.ttw);
                };
                $.htmlEngine.request(state.sUrl + "/worker/system/assistant/worker/import_file_worker", success, failed, null, 'get');
            },
            html:{
                create: function(data){
                    if(data) {
                        var controls = $(document.createElement('div'));
                        var force = $(document.createElement("button")).addClass('btn btn-default')
                            .addClass('red').html("Kill").css({marginLeft: '5px', "float": 'right'});
                        controls.append(force);
                        var action = $(document.createElement("button")).addClass('btn btn-default')
                            .addClass((data.started.value) ? 'red' : 'green')
                            .html((data.started.value) ? 'Stop' : 'Start').css({"float": 'right'});
                        controls.append(action);
                        function cmd(c) {
                            var data = {
                                command: 'assistant',
                                params: {
                                    worker: 'ImportFileWorker',
                                    delay: 300,
                                    idleThreshold: 90
                                }
                            };
                            data.params[c] = true;
                            var success = function (data) {
                            };
                            var failed = function () {
                                lusidity.info.red("Ooops, failed to " + c + " the worker.");
                                lusidity.info.show(10);
                            };
                            $.htmlEngine.request(state.sUrl + '/admin/command', success, failed, data, 'post');
                        }

                        force.on('click', function () {
                            action.addClass('disabled');
                            force.addClass('disabled').html('').append($.htmlEngine.getSpinner());
                            cmd('kill');
                        });

                        action.on('click', function () {
                            force.addClass('disabled');
                            action.addClass('disabled').html('').append($.htmlEngine.getSpinner());
                            cmd(data.started.value ? 'stop' : 'start');
                        });

                        var content = $(document.createElement('div'))
                            .append(controls)
                            .append(methods.html.getNode(data.started))
                            .append(methods.html.getNode(data.queued))
                            .append(methods.html.getNode(data.processed))
                            .append(methods.html.getNode(data.delay))
                            .append(methods.html.getNode(data.idleThreshold));

                        if(!state.info) {
                            state.info = methods.html.panel(state.opts.pnlLeftNode, 'glyphicon-dashboard', data.label, null, false);
                            state.info.css({padding: '5px'});
                        }
                        else{
                            state.info.children().remove();
                        }
                        state.info.append(content);
                    }

                    if(!state.pNode){
                        state.pNode = dCrt('div');
                        state.opts.pnlMiddleNode.append(state.pNode);
                    }
                    if(!state.pbNode) {
                        state.pbNode = $.htmlEngine.panel(state.pNode, 'glyphicon-hourglass', 'Currently Processing', null, false);
                    }
                    $.htmlEngine.stabilize(state.pbNode, true, state.offSet);
                    state.pbNode.children().remove();
                    if(data && data.processing && data.processing.file){
                        state.pbNode.addClass('blue');
                        methods.html.info(state.pbNode, data.processing);
                        methods.html.time(state.pbNode, data.processing);
                        methods.html.stats(state.pbNode, data.processing, "processing");
                    }
                    else{
                        state.pbNode.removeClass('blue');
                    }
                    $.htmlEngine.stabilize(state.pbNode, false);

                    methods.html.preProcessor.init();

                    if(!state.qNode){
                        state.qNode = dCrt('div');
                        state.opts.pnlMiddleNode.append(state.qNode);
                    }
                    if(!state.qbNode) {
                        state.qbNode = methods.html.panel(state.qNode, 'glyphicons-list', "Queued", null, false);
                    }
                    $.htmlEngine.stabilize(state.qbNode, true, state.offSet);
                    state.qbNode.children().remove();
                    if(data && data.queue && data.queue.value && data.queue.value.length>0){
                        var q = $.jCommon.array.sort(data.queue.value, [{property: 'createdWhen', asc: true}]);
                        var qc = dCrt('table').addClass('table table-hover');
                        state.qbNode.append(qc);
                        methods.html.getTableRows(['Type', 'File', 'Status', 'Delete'],['importerTypes', 'title', 'status', 'del'], q, qc, 'Failed to delete the item queued.');
                    }
                    $.htmlEngine.stabilize(state.qbNode, false);
                    if(!state.started) {
                        state.started = true;
                        methods.html.history.init(data);
                    }
                },
                info: function (container, item) {
                    container.append(methods.html.getNode(item.type)).css({padding: '5px 5px'});
                    if(item.file && item.file.value){
                        container.append(methods.html.getNode({label: "file", value: item.file.value.originalName}));
                    }
                    container.append(methods.html.getNode(item.message));
                },
                time: function (container, item) {
                    var keys = ["perSecond", "elapsed", item.started ? "started" : "estimated"];
                    var row = dCrt('div').css({whiteSpace: 'nowrap'});
                    container.append(row);
                    $.each(keys, function () {
                        row.append(methods.html.getNode(item[this],{display: 'table-cell', padding: '0 5px 0 0'}));
                    });
                },
                stats: function (container, item, status) {
                    var keys = ["processed", "primary", "innerProcessed", "total", "matches", "skipped", "errors", "created", "updated", "deleted", "queries"];
                    var row = dCrt('div').css({whiteSpace: 'nowrap', borderBottom: '1px solid #d4d4d4'});
                    if($.jCommon.string.equals(status, 'processed', true)){
                        container.addClass('green');
                    }
                    else if($.jCommon.string.equals(status, 'failed', true)){
                        container.addClass('red');
                    }
                    else if($.jCommon.string.equals(status, 'processing', true)){
                        container.addClass('blue');
                    }
                    container.append(row);
                    $.each(keys, function () {
                        row.append(methods.html.getNode(item[this],{display: 'table-cell', padding: '0 5px 0 0'}));
                    });
                },
                preProcessor: {
                    init: function () {
                        if(!state.preNode){
                            state.preNode = dCrt('div');
                            state.opts.pnlLeftNode.append(state.preNode);
                            state.preBody = $.htmlEngine.panel(state.preNode, 'glyphicons-list', 'Pre-Processor', null, false);
                        }
                        var s = function (d) {
                            methods.html.preProcessor.get(d, function () {
                                state.preBody.children().remove();
                            });
                        };
                        var q= {
                            domain: '/jobs/importer/importer_preprocessor',
                            type: '/jobs/importer/importer_preprocessor',
                            lid: null,
                            sort: {on: "createdWhen", direction: "desc"},
                            "native": {
                                query: {
                                    filtered: {
                                        filter: {
                                            bool: {
                                                must: [
                                                    {match: {'deprecated': false}}
                                                ]
                                            }
                                        }
                                    }
                                }
                            }
                        };
                        $.htmlEngine.request("/query?start=0&limit=1", s, s, q, "post");
                    },
                    get: function (data, callback) {
                        if(data && data.results) {
                            var s = function (d) {
                                if($.isFunction(callback)){
                                    callback(d);
                                }
                                methods.html.preProcessor.stats(state.preBody, d);
                            };
                            var lid = data.results[0].lid;
                            var q = {
                                domain: '/object/edge',
                                type: '/jobs/importer/importer_preprocessor',
                                lid: lid,
                                sort: {on: "createdWhen", direction: "asc"},
                                "native": {
                                    query: {
                                        filtered: {
                                            filter: {
                                                bool: {
                                                    must: [
                                                        {term: {'/object/endpoint/endpointFrom.relatedId.raw': lid}},
                                                        {term: {'label.raw': '/system/assistant/message/file_import_message/messages'}},
                                                        {match: {'deprecated': false}}
                                                    ]
                                                }
                                            }
                                        }
                                    }
                                }
                            };
                            $.htmlEngine.request("/query?start=0&limit=30", s, s, q, "post");
                        }
                    },
                    stats: function (container, items) {
                        if(items && items.results && items.results.length>0) {
                            function mk(label, key) {
                                return {label: label, key: key};
                            }

                            var results = $.jCommon.array.sort(items.results, [{property: 'createdWhen', asc: true}]);

                            var keys = [
                                mk('Name', "originalName"),
                                mk('Status', "status"),
                                mk('Bytes', "bytes")];
                            $.each(results, function () {
                                var item = this;
                                var row = dCrt('div').css({
                                    padding: '5px 5px',
                                    whiteSpace: 'nowrap',
                                    borderBottom: '1px solid #d4d4d4'
                                });
                                container.append(row);
                                $.each(keys, function () {
                                    var i = {label: this.label, value: item[this.key]};
                                    var n = methods.html.getNode(i, {padding: '0 5px 0 0', wordWrap: 'normal'});
                                    row.append(n);
                                });
                            });
                        }
                        else{
                            container.children().remove();
                            var row = dCrt('div').css({
                                padding: '5px 5px',
                                whiteSpace: 'nowrap',
                                borderBottom: '1px solid #d4d4d4'
                            });
                            container.append(row);
                            var i = {label: "Nothing to process", value: '0'};
                            row.append(methods.html.getNode(i, {display: 'table-cell', padding: '0 5px 0 0'}));
                        }
                    }
                },
                history: {
                    init: function (data){
                        var limit = 50;
                        state.start = 0;
                        function page(scrolled){
                            $.htmlEngine.busy(state.hNode, {type: 'cube', cover: false});
                            var s = function (d) {
                                if(d && d.hits) {
                                    if(scrolled) {
                                        state.start = d.next;
                                        state.hits = d.hits;
                                        state.hBody.scrollHandler('start');
                                    }
                                    init(state.hBody, d, scrolled);
                                }
                                state.hNode.loaders('hide');
                            };
                            var q= {
                                domain: '/data/importer/importer_history',
                                type: '/data/importer/importer_history',
                                lid: data.lid,
                                sort: {on: "createdWhen", direction: "desc"},
                                "native": {
                                    query: {
                                        filtered: {
                                            filter: {
                                                bool: {
                                                    must: [
                                                        {match: {'deprecated': false}},
                                                        {match: {'root': true}}
                                                    ]
                                                }
                                            }
                                        }
                                    }
                                }
                            };
                            $.htmlEngine.request('/query?start=' + (scrolled ? state.start : 0) + '&limit=' + limit, s, s, q, 'post');
                        }
                        if (!state.hNode) {
                            state.hNode = dCrt('div');
                            state.opts.pnlMiddleNode.append(state.hNode);

                            var options = {
                                target: state.hNode,
                                buttons:[
                                    {
                                        id: 'refresh-history',
                                        glyphicon: 'glyphicon-refresh',
                                        tn: 'refresh-history',
                                        title: 'Refresh',
                                        css: {maxWidth: '40px', maxHeight: '34px', padding: '3px 4px', backgroundColor: '#f5f5f5', borderColor: '#f5f5f5'},
                                        onClick: function(e){
                                            state.start = 0;
                                            state.hits = 0;
                                            state.hBody.children().remove();
                                            methods.html.history.init(data);
                                        }
                                    }
                                ]
                            };
                            state.hBody = $.htmlEngine.panel(state.hNode, 'glyphicons-clock', 'History', null, false, null, options);
                            state.hBody.css({height: '500px', maxHeight: '500px', overflowY: 'auto', overflowX: 'hidden'});
                            state.hBody.scrollHandler({
                                adjust: 10,
                                start: function () {
                                },
                                stop: function () {
                                },
                                top: function () {
                                },
                                bottom: function () {
                                    if (state.start < state.hits) {
                                        page(true);
                                    }
                                }
                            });
                        }
                        var k = '/data/importer/importer_history/histories';
                        function make(node, item, scrolled, on){
                            var id = item.lid;
                            var c = $('div[data-id="' + id + '"]');
                            if (c.length === 0) {
                                if(!scrolled){
                                    state.start+=1;
                                }
                                if (item.value) {
                                    return true;
                                }
                                if (!item.stats) {
                                    item.stats = {message: {}};
                                }
                                try {
                                    item.stats.message.value = item.status;
                                } catch (e) {
                                }
                                try {
                                    c = dCrt('div').attr('data-id', id).css({cursor: 'pointer', backgroundColor: $.jCommon.number.isEven(on) ? '#f5f5f5' : 'transparent'});
                                    if(scrolled) {
                                        node.append(c);
                                    }
                                    else{
                                        node.prepend(c);
                                    }
                                    if (item.started) {
                                        item.stats.started = {
                                            label: "Started",
                                            value: $.jCommon.dateTime.defaultFormat(item.started)
                                        }
                                    }
                                    methods.html.history.info(c, item);
                                    methods.html.history.time(c, item);
                                    methods.html.history.stats(c, item['/data/process_status/processStatus']);
                                    var cld = dCrt('div').addClass('history-children').css({margin: '0 0 10px 20px'});
                                    c.append(cld);
                                    init(cld, item[k], true);
                                    c.on('click', function () {
                                        var nodes = jObj('.history-children');
                                        if(nodes){
                                            nodes.slideUp();
                                        }
                                        cld.slideDown();

                                    })
                                } catch (e) {
                                }
                            }
                        }
                        function init(node, d, scrolled) {
                            if (d && d.results) {
                                $.each(d.results, function () {
                                    make(node, this, scrolled);
                                });
                            }
                        }
                        page(!state.hits);
                    },
                    info: function (container, item) {
                        container.append(methods.html.getNode(item.type)).css({padding: '5px 5px'});

                        if(item.source){
                            var v = item.source;
                            if($.jCommon.string.equals(item.source, item.importer)){
                                v = $.jCommon.string.getLast(v, '.');
                                item.importer = v;
                            }
                            container.append(methods.html.getNode({label: "Source", value: v}));
                        }
                        if(item.importer){
                            container.append(methods.html.getNode({label: "Importer", value: item.importer}));
                        }
                        if(item.originalFileName){
                            container.append(methods.html.getNode({label: "file", value: item.originalFileName}));
                        }

                        if(item.originalFileNames){
                            $.each(item.originalFileNames, function () {
                                if(!$.jCommon.string.endsWith(this, ".jd", true)){
                                    container.append(methods.html.getNode({label: "file", value: this}));
                                }
                            });
                        }

                        if(item.status) {
                            var status = methods.html.getNode({label: 'status', value: item.status});
                            var badge = dCrt('span').addClass('badge').html('&nbsp;');
                            container.append(status.append('&nbsp;').append(badge));
                            var clr = 'green';
                            switch (item.status) {
                                case 'waiting':
                                    clr = 'yellow';
                                    break;
                                case 'failed':
                                    clr = 'red';
                                    break;
                                case 'partial':
                                    clr = 'blue';
                                    break;
                                default:
                                    break
                            }
                            badge.addClass(clr);
                        }
                    },
                    time: function (container, item) {
                        try {
                            var keys = [{
                                label: "Started", key: "started", fn: function (value) {
                                    return $.jCommon.dateTime.defaultFormat(value);
                                }
                            }, {label: "Elapsed", key: "elapsed"}, {label: "Per-second", key: "perSecond"}];
                            var row = dCrt('div').css({whiteSpace: 'nowrap'});
                            container.append(row);
                            $.each(keys, function () {
                                var i = {label: this.label, value: item[this.key], fn: (this.fn? this.fn : null)};
                                row.append(methods.html.getNode(i, {display: 'table-cell', padding: '0 5px 0 0'}));
                            });
                        }
                        catch(e){
                            console.log(e);
                        }
                    },
                    stats: function (container, item) {
                        var k = '/system/primitives/synchronized_integer/';
                        function mk(label, key) {
                            return {label: label, key: k + key};
                        }

                        var keys = [
                            mk('Processed', "processed"),
                            mk('Primary', "primary"),
                            mk('Children Processed', 'innerProcessed'),
                            mk('Total', "total"),
                            mk('Matches', "matches"),
                            mk('Skipped', "skipped"),
                            mk('Errors', "errors"),
                            mk('Created', "created"),
                            mk('Updated', "updated"),
                            mk('Deleted', "deleted"),
                            mk('Queries', "queries")];
                        var row = dCrt('div').css({whiteSpace: 'nowrap', borderBottom: '1px solid #d4d4d4'});
                        container.append(row);
                        $.each(keys, function () {
                            var i = {label: this.label, value: item[this.key].count};
                            row.append(methods.html.getNode(i, {display: 'table-cell', padding: '0 5px 0 0'}));
                        });
                    }
                },
                getTableRows: function(headers, properties, data, container, msg){
                    var thead = $(document.createElement('thead'));
                    var row = $(document.createElement('tr'));
                    $.each(headers, function(){
                        row.append($(document.createElement('th')).html(this));
                    });
                    thead.append(row);
                    container.append(thead);

                    var tbody = $(document.createElement('tbody'));
                    container.append(tbody);
                    try {
                        if (!$.jCommon.is.array(data)) {
                            tbody.append(dCrt('td').attr('colspan', headers.length).html("No results found."));
                            return false;
                        }
                        if (!$.jCommon.is.array(properties)) {
                            console.log('properties is not in the expected format');
                            return false;
                        }
                        $.each(data, function () {
                            var item = this;
                            row = $(document.createElement('tr'));
                            $.each(properties, function () {
                                var value;
                                if ($.jCommon.string.equals(this, 'del')) {
                                    value = $(document.createElement('span')).addClass('glyphicon glyphicon-remove').css({
                                        fontSize: '16px',
                                        cursor: 'pointer',
                                        color: 'red'
                                    });
                                    value.on('click', function () {
                                        var success = function (data) {
                                            methods.getData();
                                        };
                                        var failed = function () {
                                            lusidity.info.red(msg);
                                            lusidity.info.show(5);
                                        };
                                        $.htmlEngine.request(state.sUrl + "/worker/system/assistant/worker/import_file_worker?id=" + item[state.KEY_ID], success, failed, null, 'delete');
                                    });
                                    if (item.status && item.status === "processing") {
                                        //value.hide();
                                    }
                                }
                                else if ($.jCommon.string.equals(this, "originalName") && item.originalName && $.jCommon.string.endsWith(item.title, '.zip', true)) {
                                    value = dCrt('a').attr('href', String.format('https://{0}/svc/file?path=rmk_temp_files&filename={1}', state.sUrl, item.title)).attr('target', '_blank').append(item.originalName);
                                }
                                else if ($.jCommon.string.equals(this, "importerTypes")) {
                                    value = "";
                                    var on = 0;
                                    try {
                                        $.each(item.importerTypes, function () {
                                            var v = $.jCommon.string.getLast(this, ".");
                                            if (on > 0) {
                                                value += ", ";
                                            }
                                            value += v;
                                            on++;
                                        });
                                    }
                                    catch (e){console.log(e);}
                                }
                                else {
                                    value = item[this];
                                }
                                if (item.type) {
                                    $.each(state.types, function () {
                                        if (this.value === value) {
                                            value = this.displayed;
                                            return false;
                                        }
                                    });
                                }
                                var c;
                                if (item.status) {
                                    switch (item.status) {
                                        case 'processing':
                                            c = 'blue';
                                            break;
                                        case 'processed':
                                            c = 'green';
                                            break;
                                        case 'failed':
                                            c = 'red';
                                            break;
                                    }
                                    if (c) {
                                        row.addClass(c);
                                    }
                                }
                                row.append(dCrt('td').append(value));
                            });
                            tbody.append(row);
                        });
                    }catch(e){
                        console.log(e);
                    }
                },
                getNode: function(item, style){
                    var node = $(document.createElement('div'));
                    if(item) {
                        if (style) {
                            node.css(style);
                        }
                        var v = $.jCommon.is.bool(item.value) || !$.jCommon.is.numeric(item.value) ? item.value : $.jCommon.number.commas(item.value);
                        if(undefined!==v && null!==v){
                            v = v.toString();
                        }
                        if(item.fn && $.isFunction(item.fn)){
                            v = item.fn(v);
                        }
                        var l = $(document.createElement('span')).css({fontWeight: 'bold'}).append(item.label);
                        var sep = $(document.createElement('span')).append(':').css({marginRight: '5px'});
                        node.append(l).append(sep);
                        v = $(document.createElement('span')).append(v);
                    }
                    return node.append(v);
                },
                panel: function(container, glyph, title, url, borders, actions, menu){
                    var result = $(document.createElement('div'));
                    var options = {
                        glyph: glyph,
                        title: title,
                        url: url,
                        borders: borders,
                        content: result,
                        body:{
                            css: {padding: 0}
                        },
                        actions: actions ? actions : [],
                        menu: menu
                    };
                    container.panel(options);
                    return result;
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.fileWorker.defaults = {
        ttw: 10000
    };


    //Plugin Function
    $.fn.fileWorker = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.fileWorker($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $fileWorker = $(this).data('fileWorker');
            switch (method) {
                case 'exists': return (null!==$fileWorker && undefined!==$fileWorker && $fileWorker.length>0);
                case 'state':
                default: return $fileWorker;
            }
        }
    };

})(jQuery);
