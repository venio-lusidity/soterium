if(!pl_init){
    pl_init = true;
    window.setTimeout(load, 300);
    function load(){
        $('.panel-middle').discovery();
    }
}
