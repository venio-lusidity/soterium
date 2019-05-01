if(!pl_init){
    pl_init = true;
    $.login.authorized({"groups": ["account managers"], "r": true});
    window.setTimeout(load, 300);
    function load() {
        var mb = $('#menu-bar');
        lusidity.enclaveScoping({
            menuNode: mb,
            pnlLeftNode: $('.panel-left'),
            pnlMiddleNode: $('.panel-middle'),
            pnlRightNode: $('.panel-right'),
            limit: 1000
        });
    }
}
