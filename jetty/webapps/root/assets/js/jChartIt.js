;(function ($) {

    //Object Instance
    $.jChartIt = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.jChartIt.defaults, options);

        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';

        // Store a reference to the environment object
        el.data("jChartIt", state);

        // Private environment methods
        methods = {
            init: function() {
                // expects [{value: 2, color: hash, hoverColor: hash, label: "", legend: {onMake: function(node, item), onClick: function(node, item), data: any data}, url: ""}]

                var values = [];
                var labels = [];
                var colors = [];
                var hvrColors = [];
                var data = state.opts.sorted ? state.opts.data : $.jCommon.array.sort(state.opts.data, [{property: 'value', asc: false}, {property: 'label', asc: true}]);
                var on = 0;
                $.each(data, function () {
                    values.push(this.value);
                    labels.push(this.label);
                    colors.push(this.color);
                    hvrColors.push(this.hoverColor);
                    on++;
                    if((state.opts.limit>0) && (on===state.opts.limit)){
                        return false;
                    }
                });
                var node = dCrt('div');
                state.append(node);

                if(state.opts.title){
                    var title = dCrt('div').html(state.opts.title);
                    node = $.htmlEngine.panel(node, state.opts.glyph, title, null, null, true);
                    title.parent().css({top: '-4px'});
                    node.css({padding: '10px 5px'}).addClass("me");
                    var h = (state.height/2) + 10;
                    dHeight(node,h,h,h);
                }
                methods.chart(values, labels, colors, hvrColors, node, data);
                lusidity.environment('onResize', function () {
                    methods.resize();
                });
            },
            exists: function (node) {
                return (node && (node.length>0));
            },
            resize: function () {
            },
            legend: function (node, data) {
                if (state.opts.legend) {
                    var mh = state.opts.height;
                    var len = data.length;
                    var hdr = dCrt('div').addClass('blue').css({padding: '2px 2px'}).html(state.opts.legendOverride ? state.opts.legend : String.format('{0}: {1} out of {2}', state.opts.legend, len, state.opts.max));
                    if(state.opts.tip){
                        hdr.attr('title', state.opts.tip);
                    }
                    node.append(hdr);
                    mh -= 24;
                    var mc = dCrt('div').css({padding: '2px 2px'});
                    dHeight(mc, mh, mh, mh);
                    node.append(mc);
                    return mc;
                }
            },
            chart: function (values, labels, colors, hvrColors, node, data) {
                var row = dCrt('div');
                node.append(row);
                var l;var m; var r;
                if(state.opts.chartOnly){
                    l = m = r = row;
                }
                if(state.opts.chartAndLegend){
                    row.addClass('row');
                    l = dCrt('div').addClass('col-md-6');
                    m = dCrt('div').addClass('col-md-6');
                    r = dCrt('div').addClass('col-md-4');
                    dHeight(l, h, h, h);
                    dHeight(m, h, h, h);
                    row.append(l).append(m);
                    var mc = methods.legend(m, data);
                    $.each(data, function () {
                        if ($.isFunction(this.legend.onMake)) {
                            this.legend.onMake(mc, rc, this);
                        }
                    });
                }
                else {
                    row.addClass('row');
                    var h = state.opts.height;
                    dHeight(row, h, h, h);
                    l = dCrt('div').addClass('col-md-4');
                    m = dCrt('div').addClass('col-md-3');
                    r = dCrt('div').addClass('col-md-5');
                    dHeight(l, h, h, h);
                    dHeight(m, h, h, h);
                    dHeight(r, h, h, h);
                    row.append(l).append(m).append(r);

                    var mc = methods.legend(m, data);

                    var rc = dCrt('div');
                    dHeight(rc, h, h, h);
                    r.append(rc);

                    $.each(data, function () {
                        if ($.isFunction(this.legend.onMake)) {
                            this.legend.onMake(mc, rc, this);
                        }
                    });
                }

                state.opts.ctx = dCrt('canvas').attr('height', h);
                state.opts.ctx .globalCompositeOperation='destination-over';
                l.append(state.opts.ctx);
                var item = {
                    labels: labels,
                    datasets: [
                        {
                            data: values,
                            backgroundColor: colors,
                            hoverBackgroundColor: hvrColors,
                            borderWidth: 0
                        }
                    ]
                };

                var opts = {
                    type: state.opts.type,
                    data: item,
                    titleFontSize: 8,
                    options: {
                        maintainAspectRatio: state.opts.maintainAspectRatio,
                        tooltips: {
                            tooltipTemplate: "<%if (label){%><%}%><%= value %>: <%=label%>",
                            percentageInnerCutout : 70,
                            titleFontSize: 8,
                            titleFontColor: "#ddd",
                            mode: 'point'
                        },
                        animateScale: true,
                        legend: {
                            display: false
                        },
                        onClick: function (e) {
                            if($.isFunction(state.opts.onClick)){
                                var ap = chart.getElementsAtEvent(e);
                                state.opts.onClick(e, chart, ap);
                            }
                        }
                    }
                };
                switch (state.opts.type){
                    case 'bar':
                        opts.options.scales = {
                            yAxes:[{
                               display: true,
                               ticks:{
                                   suggestedMin: 0
                               }
                            }],
                            xAxes: [{
                                display: false
                            }]
                        };
                        break;
                }
                var chart = new Chart(state.opts.ctx, opts);
            }
        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.jChartIt.defaults = {
        sorted: false /*is the data already sorted*/,
        maintainAspectRatio: false,
        chartOnly: false,
        type: 'pie',
        height: 100,
        width: 100,
        limit: 10,
        glyph: "glyphicon-info-sign"
    };


    //Plugin Function
    $.fn.jChartIt = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.jChartIt($(this), method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $jChartIt = $(this).data('jChartIt');
            switch (method) {
                case 'exists': return (null!==$jChartIt && undefined!==$jChartIt && $jChartIt.length>0);
                case 'state':
                default: return $jChartIt;
            }
        }
    };

})(jQuery);
