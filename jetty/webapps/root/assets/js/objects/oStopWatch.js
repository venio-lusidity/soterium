

function oStopWatch(){
    var from;
    var to;
    var sw = this;
    this.start = function (log) {
        from = Date.now();
        if(log){
            console.log('started: '+from);
        }
    };
    this.getStartTime = function (log) {
        var r = new Date(from);
        if(log){
            console.log('started: '+r);
        }
        return r;
    };
    this.stop = function (log) {
        to = Date.now();
        if(log){
            console.log('stopped: '+to);
        }
    };
    this.getStopTime = function (log) {
        var r = new Date(to);
        if(log){
            console.log('stopped: '+to);
        }
        return r;
    };
    this.getFinalTime = function (log) {
        var r = 0;
        if(from && to) {
            r = Math.abs(from - to);
        }
        if(log){
            console.log('elapsed: '+r);
        }
        return r;
    };
    this.waitAsync = function (delay, func) {
        if(!from){
            sw.start();
        }
        var rdy = false;
        function check() {
            if(delay<sw.getElapsed()){
                sw.stop();
                if(func && typeof func === 'function'){
                    func.apply(this, arguments);
                }
            }
            else{
                window.setTimeout(check, 50);
            }
        }
        check();
    };
    this.wait = function (delay, func) {
        if(!from){
            sw.start();
        }
        var rdy = false;
        function check() {
            if(delay<sw.getElapsed()){
                rdy = true;
            }
            else{
                window.setTimeout(check, 50);
            }
        }
        check(delay);
        while (!rdy){
            check();
        }
        if(func && typeof func == 'function'){
            func.apply(this, arguments);
        }
        sw.stop();
    };
    this.getElapsed = function (log) {
        var r=0;
        if(from) {
            var n = Date.now();
            r = Math.abs(from - n);
        }
        if(log){
            console.log('elapsed: '+r);
        }
        return r;
    };
    this.getSeconds = function (log) {
        var r = Math.ceil((this.getElapsed()/1000));
        if(log){
            console.log('elapsed: '+r);
        }
        return r;
    }
}