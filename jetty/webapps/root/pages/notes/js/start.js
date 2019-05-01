if (!pl_init){
    pl_init = true;
    var p = $('.panel-left');
    var m = $('.panel-middle');
    p.jNodeReady({onReady: function () {
        p.releaseNotes({
            url: null,
            view: 'widget',
            fill: true
        });
    }});
    m.jNodeReady({onReady: function () {
        m.releaseNotes({
            url: null,
            view: 'content',
            fill: true,
            data: "View messages here."
        });
    }});

}
