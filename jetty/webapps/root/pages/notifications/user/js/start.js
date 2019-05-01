if (!pl_init){
    pl_init = true;
    var pl = $('.panel-left');
    var ml = $('.panel-middle');
    pl.jNodeReady({onReady: function () {
        pl.notifications({
            url: null,
            view: 'widget',
            fill: true
        });
    }});
    ml.jNodeReady({onReady: function () {
        ml.notifications({
            url: null,
            view: 'content',
            fill: true,
            data: "View messages here."
        });
        var mb = $('#menu-bar');
        mb.menuBar({
            target: ml, buttons: [
                {name: '', glyphicon: 'glyphicons-check', tn: 'read', title: 'Mark as Read', cls: 'blue'}
                /*{name: '', glyphicon: 'glyphicon-save', tn: 'arc', title: 'Archive', cls: 'blue'}*/
            ]
        });
    }});

}
