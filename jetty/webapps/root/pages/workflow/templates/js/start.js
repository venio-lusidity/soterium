if(!pl_init){
    pl_init = true;
    $.login.authorized({"groups": ["admin"], "r": true});
    window.setTimeout(load, 300);
    function load(){
        $('.page-content').workflow({
            pnlLeft: $('.panel-left'),
            pnlMiddle: $('.panel-middle'),
            pnlRight: $('.panel-right'),
            commonMenuNode: $('#menu-bar'),
            workflow:{
                menuNode: $('#menu-bar-workflow'),
                formNode: $('#form-workflow'),
                list: $('.panel-body.workflow-list')
            }
        });
    }
}
