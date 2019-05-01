if(!pl_init){
    pl_init = true;
    $.login.authorized({"groups": ["admin"], c: function (d) {
        if(d.auth){}
        else{
            window.location = d.redirectUrl;
        }
    }});
    window.setTimeout(load, 300);
    function load() {
        lusidity.jClassDiagram({
            pnlLeftNode: $('.panel-left'),
            pnlViewerNode: $('#viewer-node'),
            pnlQueryNode: $('#query-node'),
            queryNode: $('#query-json'),
            queryResults: $('#query-results'),
            btnSubmit: $('#btn-submit'),
            startNode: $('#page-start'),
            limitNode: $('#page-limit')
        });
    }
}
