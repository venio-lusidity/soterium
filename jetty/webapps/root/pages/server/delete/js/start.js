if(!pl_init){
    pl_init = true;
    $.login.authorized({"groups": ["admin"], "r": true});
    window.setTimeout(load, 300);
    function load() {
        lusidity.deleteVertices({
            pnlMiddleNode: $('.panel-middle'),
            baseUrlNode: $('#base-url'),
            submitNode: $('#submit'),
            txtNode: $('#text-area'),
            progressNode: $('#processing')
        });
    }
}
