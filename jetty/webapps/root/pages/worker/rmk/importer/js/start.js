if(!pl_init){
    pl_init = true;
    $.login.authorized({"groups": ["admin"], "r": true});
    window.setTimeout(load, 300);
    function load() {
        $('.page-content').show();
        $('.page-content').fileWorker({
            menuNode: $('#menu-bar'),
            pnlLeftNode: $('.panel-left'),
            pnlMiddleNode: $('.panel-middle')
        });
    }
}
