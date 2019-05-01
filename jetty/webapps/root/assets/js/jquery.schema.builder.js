;(function ($) {

    //Object Instance
    $.schemaBuilder = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.opts = $.extend({}, $.schemaBuilder.defaults, options);

        // Store a reference to the environment object
        el.data("schemaBuilder", state);

        // Private environment methods
        methods = {
            init: function() {
                var info = null;
                var prevStep = 1;

                function resize() {
                    window.setTimeout(function () {
                        $('.steps').css({minHeight: ($(window).height() - 74) + 'px'});
                        $('#mapping').css({minHeight: ($(window).height() - 162) + 'px'});
                    }, 300);
                }

                resize();

                $(window).resize(function(){
                    resize();
                });

                $('.nav-list-item').on('click', function(){
                    $('.nav-list-item').parent().removeClass('active');
                    $(this).parent().addClass("active");
                    var step = this.dataset.idx;
                    $('#step'+prevStep).fadeOut("slow", function(){
                        $('#step'+step).fadeIn("slow");
                        prevStep = step;
                    });
                });
                $('#step1').show();

                var prevId = null;
                $('#importTypes').find('a').on('click', function(){
                    var id = this.dataset.id;
                    if(null!=prevId){
                        $("#"+prevId).fadeOut('slow', function(){
                            $('#'+id).fadeIn('slow');
                        });
                    }
                    else{
                        $('#'+id).fadeIn('slow');
                    }
                    $('#importTypes').find('button').html($(this).html() + '<span class="caret"></span>');
                    prevId = id;
                });

                $("#className").keyup(function(){
                    var value = $(this).val();
                    if(null!=value && undefined!=value){
                        if(!$('#dropZone').is(':visible')){
                            $("#coverZone").fadeOut('slow', function(){
                                $('#dropZone').fadeIn('slow');
                            });
                        }
                    }
                    else if(!$('#coverZone').is(':visible')){
                        $("#dropZone").fadeOut(200, function(){
                            $('#coverZone').fadeIn(200);
                        });
                    }
                });
                $('#addClass').on('click', function(){
                    var body = $('<div class="input-group">' +
                    '<span class="input-group-addon" id="className">&nbsp;&nbsp;&nbsp;</span>' +
                    '<input type="text" class="form-control" placeholder="Enter the class name." aria-describedby="className">' +
                    '</div>');

                    var footer = $(document.createElement('button')).attr('type', 'button').addClass('btn btn-success').html('Create');
                    body.css({magin: '0 5px 0 5px'});

                    body.find('input').on('keyup', function (e){
                        var code = parseInt(e.keyCode);
                        if(code === 13){
                            footer.click();
                        }
                    });
                    footer.on('click', function(){
                        var value = body.find('input').val();
                        if(undefined!=value && null!=value){
                            $('#mapping').mapper('addClass', {classname: value});
                        }
                        $('#addClass').pageModal('hide');
                    });
                    $('#addClass').pageModal();
                    $('#addClass').pageModal('show', {
                        header: 'Create Class',
                        body: body,
                        footer: footer,
                        hasClose: true
                    });
                    window.setTimeout(function(){
                        body.find('input').focus();
                    }, 300);
                });
                $("#dropZone").on('click', function(){
                    $('#fileupload').click();
                });

                $('#fileupload').fileupload({
                    url: site.environment('sUri')+'/dataUpload',
                    dropZone: $("#dropZone"),
                    dataType: 'json',
                    type: 'post',
                    start:function(e){
                        $('.process').show();
                    },
                    done: function (e, data) {
                        info = data.result;
                        info.className = $('#className').val();
                        $('#coverZone2').hide();
                        $('#step2Title').show().html('Mapping ' + info.fileName);
                        $('[data-idx="2"]').click();
                        $('#mapping').mapper({data: info});
                        window.setTimeout(function(){
                            $('.process-bar').css({ width: '0%' }).html('');
                        }, 500);
                    },
                    progressall: function (e, data) {
                        var progress = (parseInt(data.loaded / data.total * 100, 10))+'%';
                        $('.process-bar').css({ width: progress }).html();
                    }
                });
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.schemaBuilder.defaults = {
    };


    //Plugin Function
    $.fn.schemaBuilder = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.schemaBuilder($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $schemaBuilder = $(this).data('schemaBuilder');
            switch (method) {
                case 'state':
                default: return $schemaBuilder;
            }
        }
    };

})(jQuery);
