(function ($) {
    var container;
    //var options = {
    //    width: '300',
    //    height: '16px',
    //    outerCSS: '',
    //    innerCSS: '',
    //    percentage: percentage,
        //showPercentage: false,
        //delay: 500
    //};

    var methods = {
        init: function (options) {
            container = this;
            var outer = document.createElement('div');
            var inner = document.createElement('div');

            options.width = options.width ? options.width : '300px';
            options.height = options.height ? options.height : '16px';
            options.percentage = options.percentage && options.percentage > 0 ? options.percentage : 0;

            $(outer).css({ width: options.width, height: options.height });

            if (options.outerCSS) {
                $(outer).addClass(options.outerCSS);
            }
            if (options.innerCSS) {
                $(inner).addClass(options.innerCSS);
            }
            
            $(container).append(outer);
            
            $(inner).css({ height: '100%', width: '0' });
            if (options.showPercentage) {
                var text = document.createElement('div');;
                $(text).css({ width: options.width });
                $(text).html(options.percentage + '%');
                $(inner).append(text);
            }
            
            $(outer).append(inner);
            
            var width = $(outer).width();
            var perc = options.percentage / 100;
            width = width * perc;

            $(inner).animate({ width: width + 'px' }, options.delay, function () { });

        }
    };

    $.fn.percentbar = function (method) {

        // Method calling logic
        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === 'object' || !method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' + method + ' does not exist on jQuery.tooltip');
        }

    };
})(jQuery);