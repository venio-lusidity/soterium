if(!pl_init){
    pl_init = true;
    $.login.authorized({"groups": ["admin"], "r": true});
    window.setTimeout(load, 300);
    function load() {
        $('.page-content').show();
        $('.page-content').importerHistory({
            menuNode: $('#menu-bar'),
            pnlMiddleNode: $('.panel-middle')
        });
    }
}
