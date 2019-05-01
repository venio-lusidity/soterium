if(!pl_init){
    pl_init = true;
    $('.page-content').css({overflow: 'hidden'});
    window.setTimeout(load, 300);
    function load(){
        $('div.main-content').lusid({debug: false, testData: {}});
    }
}
