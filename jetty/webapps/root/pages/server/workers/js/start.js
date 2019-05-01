if(!pl_init){
    pl_init = true;
    $.login.authorized({"groups": ["admin"], "r": true, c: function () {
        lusidity.jAthena({
            pnlTabNodes: $('.tabs'),
            pnlTabContent: $('.tabs-content'),
            view: 'workers'
        });
    }});
}
