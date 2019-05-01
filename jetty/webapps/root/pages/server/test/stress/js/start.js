if(!pl_init){
    pl_init = true;
    $.login.authorized({"groups": ["admin"], "r": true});
    window.setTimeout(load, 300);
    function load() {
        lusidity.jAthena({
            pnlTabNodes: $('.tabs'),
            pnlTabContent: $('.tabs-content'),
            view: 'testLoad'
        });
    }
}
