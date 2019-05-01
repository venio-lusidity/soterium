if(!pl_init){
    pl_init = true;
    $.login.authorized({"groups": ["log managers"], "r": true});
    window.setTimeout(load, 300);
    function load() {
        var mb = $('#menu-bar');
        lusidity.activityLog({
            menuNode: mb,
            pnlMiddleNode: $('.panel-middle'),
            limit: 1000
        });
    }
}
