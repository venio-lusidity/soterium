if(!pl_init){
    pl_init = true;
    $.login.authorized({"groups": ["admin"], c: function (d) {
        if(d.auth){

        }
        else{
            window.location = d.redirectUrl;
        }
    }});
    window.setTimeout(load, 300);
    function load() {
        lusidity.jServer({
            pnlTabNodes: $('.tabs'),
            pnlTabContent: $('.tabs-content'),
            view: 'none'
        });
    }
}
