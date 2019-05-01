if(!pl_init){
    pl_init = true;
    lusidity.resizePage(-45);
    $('.page-content').css({paddingTop: '20px', overflow: 'hidden'}).assetSources({pnlHeaderNode: $('.panel-header'), pnlLeftNode: $('.panel-left'), pnlRightNode: $('.panel-right')});
}
