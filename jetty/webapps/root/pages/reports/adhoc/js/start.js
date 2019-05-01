if (!pl_init){
    pl_init = true;
    var pl = $('.panel-left');
    var ml = $('.panel-middle');
    pl.jNodeReady({onReady: function () {
        var mb = $('#menu-bar');
        mb.menuBar({
            target: ml, buttons: [
                {name: '', glyphicon: 'glyphicons glyphicons-file-plus', tn: 'add', title: 'Create Report', cls: 'blue'},
                {name: 'sep'},
                {id: 'btn-edit', name: '', glyphicon: 'glyphicons glyphicons-pencil', tn: 'edit', title: 'Edit', cls: 'blue'},
                {id: 'btn-view', name: '', glyphicon: 'glyphicons glyphicons-table', tn: 'view', title: 'View', cls: 'blue'}
            ]
        });
        var opts = {
            pnlLeftNode: pl,
            pnlMiddleNode: ml
        };
        lusidity.adhoc(opts);
    }});
}
