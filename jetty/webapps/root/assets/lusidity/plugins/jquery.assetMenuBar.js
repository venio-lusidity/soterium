;(function ($) {

    //Object Instance
    $.assetMenuBar = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);
        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.assetMenuBar.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.types = [
            {displayed: 'STIG Viewer Finding', value:'com.lusidity.rmk.importer.stig.StigFindingImporter', 'default': true}
        ];
        // Store a reference to the environment object
        el.data("assetMenuBar", state);

        // Private environment methods
        methods = {
            init: function() {
                if(!state.opts.enabled){
                    return true;
                }
                state.worker.node.menuBar({target: state.worker.node, uploadDisabled: "Import STIG Finding"});

                var modals = $(document.createElement('li'));
                state.worker.node.append(modals);

                state.stig = $(document.createElement('div'));
                modals.append(state.stig);

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
                            readOnly: true,
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
                                state.stig.pageModal('hide');
                            },
                            fail: function(e, data){
                                state.stig.pageModal('hide');
                                lusidity.info.red('Something happened during the upload please try again.');
                                lusidity.info.show(5);
                            }
                        }
                    ],
                    getUrl: function(){
                        var result;
                        var type = $('#importType');
                        var actual = type.attr('actual');
                        if(actual){
                            result = lusidity.environment('host-primary') +  "/fileupload/importer?type="+actual+"&uri=" + state.worker.data[state.KEY_ID];
                        }
                        if(undefined===result){
                            type.focus();
                        }
                        return result;
                    }
                };
                state.worker.node.on('menu-bar-upload', function(){
                    if(!state.stig.pageModal('exists')) {
                        state.stig.pageModal();
                        state.stig.pageModal('show', {
                            glyph: 'glyphicon-upload',
                            header: 'Import data.',
                            body: function(body){
                                var form = $(document.createElement('div'));
                                body.append(form);
                                form.formBuilder(options);
                            },
                            footer: null,
                            hasClose: true});
                    }
                    state.stig.pageModal('show');
                });
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.assetMenuBar.defaults = {
        enabled: true
    };


    //Plugin Function
    $.fn.assetMenuBar = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.assetMenuBar($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $assetMenuBar = $(this).data('assetMenuBar');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $assetMenuBar;
            }
        }
    };

    $.assetMenuBar.call= function(elem, options){
        elem.assetMenuBar(options);
    };

    try {
        $.htmlEngine.plugins.register("assetMenuBar", $.assetMenuBar.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
