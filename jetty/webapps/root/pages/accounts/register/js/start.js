lusidity.on('lusidityReady', function(e){
    if(!pl_init){
        pl_init = true;
        lusidity.accountRegister(
            {
                pnlMiddleNode: $('.panel-middle'),
                url: '/register', type: 'post', data: e.auth
            }
        );
        var content = $('.content');
        content.find('.page-panel').show();
        content.loaders('hide');
    }
});