if(!pl_init){
    pl_init = true;
    $.login.authorized({"groups": ["account managers"], "r": true});
    window.setTimeout(load, 300);
    function load() {
        var viewer = $('.viewer-node');
        var mb = $('#menu-bar');
        lusidity.organization({
            menuNode: mb,
            discoverNode: $('.filtered-search'),
            positionsNode:$('.acl-positions'),
            tabContNode:$('.tab-content'),
            viewerNode: viewer,
            editNode: $('.edit-node'),
            addNode: $('.add-node'),
            pnlLeftNode: $('.panel-left'),
            pnlMiddleNode: $('.panel-middle'),
            limit: 1000
        });

        $('#summary-tab').on('click', function () {
            lusidity.enclaves('summary');
        });
    }
}
