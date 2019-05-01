
if(!pl_init){
    pl_init = true;
    $.login.authorized({"groups": ["admin"], "r": true});
    window.setTimeout(load, 300);
    function load() {
        var viewer = $('.viewer-node');
        var mb = $('#menu-bar');
        var pln = $('.panel-left');
        mb.menuBar({
            target: pln, buttons: [
                {name: 'Export', glyphicon: 'glyphicon glyphicon-save-file', tn: 'export', title: 'Export Account Data', cls: 'blue icon-size' }
            ]
        });
        lusidity.acl({
            menuNode: mb,
            discoverNode: $('.filtered-search'),
            viewerNode: viewer,
            editNode: $('.edit-node'),
            addNode: $('.add-node'),
            pnlLeftNode: pln,
            pnlMiddleNode: $('.panel-middle'),
            limit: 1000
        });
        $('#summary-tab').on('click', function () {
            lusidity.enclaves('summary');
        });
    }
}