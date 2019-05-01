

;(function ($) {

    //Object Instance
    $.serverMessages = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.serverMessages.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.sUrl = lusidity.environment('host-primary');

        // Store a reference to the environment object
        el.data("serverMessages", state);

        // Private environment methods
        methods = {
            init: function() {
                methods.html.init();
            },
            createNode: function(type, css, cls) {
                return $(document.createElement(type));
            },
            html:{
                init: function () {

                },
                create: function (data) {
                    var hd = dCrt('h4').css({textAlign: 'center', margin: '5px 5px'}).html("Currently Importing");
                    state.append(hd);
                    var tbl = dCrt('table').addClass('table table-striped');
                    state.append(tbl);
                    if(data.processing){
                        var item = data.processing;
                        if(item.type && item.type.value) {
                            var r = dCrt('tr');
                            var td = dCrt('div').html('Type: ' + item.type.value);
                            r.append(td);
                            tbl.append(r);
                        }

                        if(item.estimated && item.estimated.value) {
                            var r = dCrt('tr');
                            var td = dCrt('div').html('Estimated: ' + item.estimated.value);
                            r.append(td);
                            tbl.append(r);
                        }

                        if(item.processed && item.processed.value) {
                            var r = dCrt('tr');
                            var td = dCrt('div').html('Processed: ' + item.processed.value + ' of ' + item.total.value);
                            r.append(td);
                            tbl.append(r);
                        }
                    }
                    else{
                        var r = dCrt('tr');
                        var td = dCrt('div').html('No files are currently processing.');
                        r.append(td);
                        tbl.append(r);
                    }

                    var hd = dCrt('h4').css({textAlign: 'center', margin: '5px 5px'}).html("Import History");
                    state.append(hd);

                    tbl = dCrt('table').addClass('table table-striped');
                    state.append(tbl);

                    var options = [
                        {label: 'HBSS Assets', key: '', type: 'HbssAssetsImporter'},
                        {label: 'HBSS Opts Attr', key: '', type: 'HbssOpsImporter'},
                        {label: 'HBSS Deleted', key: '', type: 'HbssDeletedImporter'},
                        {label: 'HBSS STIG Scans', key: '', type: 'HbssStigImporter'},
                        {label: 'BL Software', key: '', type: 'BladeLogicSoftwareImporter'},
                        {label: 'BL Compliance', key: '', type: 'BladeLogicComplianceImporter'},
                        {label: 'BL CVEs', key: '', type: 'BladeLogicCveImporter'},
                        {label: 'SC Conus', key: '', type: 'AcasApiImporter'},
                        {label: 'SC Conus 1', key: '', type: 'AcasApiImporter'},
                        {label: 'SC Conus 2', key: '', type: 'AcasApiImporter'},
                        {label: 'SC Conus 3', key: '', type: 'AcasApiImporter'},
                        {label: 'SC Conus 4', key: '', type: 'AcasApiImporter'},
                        {label: 'SC Conus 5', key: '', type: 'AcasApiImporter'},
                        {label: 'SC Conus 6', key: '', type: 'AcasApiImporter'},
                        {label: 'SC Conus UR', key: '', type: 'AcasApiImporter'},
                        {label: 'SC Conus PAC', key: '', type: 'AcasApiImporter'},
                        {label: 'SCCM', key: '', type: 'SccmImporter'}
                    ];

                    $.each(options, function () {
                        var item = this;
                        var r = dCrt('tr');
                        var td = dCrt('div');
                        r.append(td);
                        tbl.append(r);
                        var v = this.label;
                        var dt;
                        try {
                            if (data.history) {
                                $.each(data.history, function () {
                                    var c;
                                    var hist = this;
                                    $.each(hist.importerTypes, function () {
                                        if (!$.jCommon.string.empty(this) &&
                                            $.jCommon.string.endsWith(this, item.type, true)) {
                                            c = hist;
                                        }
                                    });
                                    if (c) {
                                        dt = $.jCommon.dateTime.defaultFormat(c.modifiedWhen);
                                        return false;
                                    }
                                });
                            }
                        }
                        catch(e){}
                        td.html(v += (dt) ? ': ' + dt : ' no history found.');
                    });
                }
            }
        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.serverMessages.defaults = {};


    //Plugin Function
    $.fn.serverMessages = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.serverMessages($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $serverMessages = $(this).data('serverMessages');
            switch (method) {
                case 'exists': return (null!=$serverMessages && undefined!=$serverMessages && $serverMessages.length>0);
                case 'state':
                default: return $serverMessages;
            }
        }
    };

})(jQuery);
