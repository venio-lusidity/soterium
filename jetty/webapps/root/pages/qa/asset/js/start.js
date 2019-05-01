if (!pl_init){
    pl_init = true;
    $.login.authorized({"groups": ["admin"], "r": true});
    var pl = $('.panel-left');
    var ml = $('.panel-middle');
    lusidity.assetMatches({
        url: null,
        view: 'widget',
        fill: true,
        pnlLeftNode: pl,
        pnlMiddleNode: ml
    });
}
