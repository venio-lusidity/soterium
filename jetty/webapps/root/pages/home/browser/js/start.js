var started = lusidity.attr('page-started');
if(!started) {
    lusidity.attr('page-started', 'true');
    window.setTimeout(load, 300);
    function load() {
        window.setTimeout(function () {
            var node = $('.page-content');
            node.loaders('hide');
            lusidity.info.blue(dCrt('h4').html('This browser is not supported, please use Chrome.'));
            lusidity.info.show();
            if($.jBrowserInfo.isChrome()){
                window.location = "/enclaves";
            }
        }, 1000);
    }
}

