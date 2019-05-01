

;(function ($) {

    //Object Instance
    $.deleteVertices = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.deleteVertices.defaults, options);

        // Store a reference to the environment object
        el.data('deleteVertices', state);

        var errors,done;

        // Private environment methods
        methods = {
            init: function() {
                state.opts.progressNode.hide();
                state.opts.pnlMiddleNode.show();
                state.opts.submitNode.on('click', function () {
                    state.opts.submitNode.hide();
                    state.opts.progressNode.show();
                    methods.del();
                    state.opts.submitNode.show();
                    state.opts.progressNode.hide();
                    state.opts.progressNode.children().remove();
                });
            },
            del: function () {
                var lines = state.opts.txtNode.val().split('\n');
                if (!lines || !lines.length > 0) {
                    return false;
                }
                errors = [];
                done = [];
                var len = lines.length;
                var n = (100 / len);
                var lbl = dCrt('div').css({minHeight: '20px', height: '20px'});
                state.opts.progressNode.append(lbl);
                var p = dCrt('div').css({margin: '10px 10px'}).addClass('progress');
                state.opts.progressNode.append(p);
                var pb = dCrt('div').addClass('progress-bar').attr('aria-valuemin', 0).attr('aria-valuemax', 100).attr('aria-valuenow', 0).attr('role', 'progressbar');
                p.append(pb);
                var on = 0;
                for (var i = 0; i < len; i++) {
                    var txt = lines[i].toString();
                    lbl.children().remove();
                    lbl.append(dCrt('strong').html("Deleting:&nbsp;")).append(dCrt('span').html(txt));
                    methods.handle(txt, on);
                    on++;
                    var now = on * n;
                    now = ((now > 100) ? 100 : now);
                    pb.css({width: now + '%'});
                }

                if(errors.length>0){
                    var txt = '';
                    $.each(errors, function () {
                        if(txt.length>0){
                            txt+='\n';
                        }
                        txt+=this.toString();
                    });
                    state.opts.txtNode.val(txt);
                }

                if(done.length>0){
                    var txt = '\n***************************************\nCompleted...';
                    $.each(done, function () {
                        if(txt.length>0){
                            txt+='\n';
                        }
                        txt+=this.toString();
                    });
                    state.opts.txtNode.val(txt);
                }
            },
            handle: function (rp) {
                var url = String.format("{0}/svc{1}", state.opts.baseUrlNode.val(), rp);
                var s = function (data) {
                    done.push(rp);
                };
                var f = function () {
                    errors.push(String.format('Something went wrong while trying to delete, {0}.', rp))
                };
                $.htmlEngine.request(url, s, f, null, 'delete', false);
            }
        };
        //public methods

        //Initialize
        methods.init();
    };

    //Default Settings
    $.deleteVertices.defaults = {};


    //Plugin Function
    $.fn.deleteVertices = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === 'object') {
            return this.each(function() {
                new $.deleteVertices($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $deleteVertices = $(this).data('deleteVertices');
            switch (method) {
                case 'exists': return (null!==$deleteVertices && undefined!==$deleteVertices && $deleteVertices.length>0);
                case 'state':
                default: return $deleteVertices;
            }
        }
    };

})(jQuery);
