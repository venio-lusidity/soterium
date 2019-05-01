;(function ($) {

    //Object Instance
    $.enclaveMenuBar = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.enclaveMenuBar.defaults, options.schema.plugin);
        state.opts.name = options.schema.name;
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        // Store a reference to the environment object
        el.data("enclaveMenuBar", state);


        // Private environment methods
        methods = {
            init: function() {
                if(!state.opts.disabled) {
                    state.worker.node.menuBar({target: state.worker.node, add: "POAM"});

                    var modals = $(document.createElement('li'));
                    state.worker.node.append(modals);

                    state.poam = $(document.createElement('div'));
                    modals.append(state.poam);
                    state.worker.node.on('menu-bar-add', function () {
                        if (!state.poam.pageModal('exists')) {
                            state.poam.pageModal();
                            state.poam.pageModal('show', {
                                glyph: 'glyphicons-list-alt',
                                header: {
                                    object: 'POAM request.',
                                    cls: 'blue'
                                },
                                body: function (body) {
                                    body.append("Comming soon.");
                                },
                                footer: null,
                                hasClose: true
                            });
                        }
                        state.poam.pageModal('show');
                    });
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.enclaveMenuBar.defaults = {};


    //Plugin Function
    $.fn.enclaveMenuBar = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.enclaveMenuBar($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $enclaveMenuBar = $(this).data('enclaveMenuBar');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $enclaveMenuBar;
            }
        }
    };

    $.enclaveMenuBar.call= function(elem, options){
        elem.enclaveMenuBar(options);
    };

    try {
        $.htmlEngine.plugins.register("enclaveMenuBar", $.enclaveMenuBar.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
