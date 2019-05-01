if(!pl_init){
    pl_init = true;
    var callback = function () {
        // Do something clever here once data has been removed.
    };
    window.setTimeout(load, 100);
    function load() {
        $('.panel-middle').css({overflowX: 'hidden', overflowY: 'auto'});
        lusidity.resizePage();
        function resize(){
           $('.page-content').css({padding: '60px 0 0 0', overflow: 'hidden'});
           lusidity.resizePage(65);
            $('.panel-middle').show();
        }
        resize();
        lusidity.environment('onResize', resize);
        var node = $('.main-content');
        $('#accept').on('click', function (e) {
            window.setTimeout(function () {
                $(document).login('login');
            }, 300);
        });
    }
}

