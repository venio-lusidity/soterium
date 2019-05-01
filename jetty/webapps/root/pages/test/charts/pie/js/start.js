if(!pl_init){
    pl_init = true;

    var pnl = $('.panel-middle');
    pnl.show();

    var KEY_ID = '/vertex/uri';

    var methods = {
        chart: function (node, data) {
            function toHex(c){
                var hex = c.toString(16);
                return hex.length === 1 ? "0" + hex : hex;
            }

            function lighten(col, amt){
                var usePound = true;
                if ( col[0] === "#" ) {
                    col = col.slice(1);
                    usePound = true;
                }

                var num = parseInt(col,16);

                var r = (num >> 16) + amt;

                if ( r > 255 ) r = 255;
                else if  (r < 0) r = 0;

                var b = ((num >> 8) & 0x00FF) + amt;

                if ( b > 255 ) b = 255;
                else if  (b < 0) b = 0;

                var g = (num & 0x0000FF) + amt;

                if ( g > 255 ) g = 255;
                else if  ( g < 0 ) g = 0;

                return (usePound?"#":"") + (g | (b << 8) | (r << 16)).toString(16);
            }

            var software = {};

            $.each(data._subs, function () {
                var item = this;
                var lid = item.lid.toString();
                var r = toHex(item._color.r);
                var g = toHex(item._color.g);
                var b = toHex(item._color.b);
                item._color = '#'+r+g+b;
                item._hvrColor = lighten(item._color, -20);
                var key = $.jCommon.string.makeKey(item.title);
                var val = software[key];
                if(!val){
                    val={
                        label: item.title,
                        folded: item.title.toLowerCase(),
                        value: 0,
                        color: item._color,
                        hoverColor: item._hvrColor,
                        legend: {
                            onMake: function (node, item) {
                                var m = dCrt('div').addClass('media');
                                var ml = dCrt('div').addClass('media-left');
                                var clr = dCrt('div').css({
                                        width: '16px',
                                        height: '16px',
                                        minWidth: '16px',
                                        minHeight: '16px',
                                        maxWidth: '16px',
                                        maxHeight: '16px',
                                        backgroundColor: item.color,
                                        border: '1px solid ' + item.hoverColor});
                                m.append(ml.append(clr));
                                var mb = dCrt('div').addClass('media-body');
                                var n = dCrt('div');
                                var link = dLink(String.format('{0}: {1}', item.value, item.legend.data.title), item.legend.data[KEY_ID]);
                                mb.append(n.append(link));
                                m.append(mb);
                                node.append(m);
                            },
                            onClick: function (node, data) {

                            },
                            data: item
                        }
                    };
                    software[key] = val;
                }
                $.each(data.results, function () {
                    $.each(this._subs, function () {
                        var a = this.toString();
                        if($.jCommon.string.equals(a, lid, true)){
                            val.value+=1;
                            return false;
                        }
                    });
                });
            });

            var options = [];
            $.each(software, function (key, value) {
                if(value.value>0) {
                    options.push(value);
                }
            });

            options = $.jCommon.array.sort(options, [{property: 'folded', asc: true}]);
            function onClick(e, chart, ap) {
                var lbl = ap[0]._view.label;
            }
            var len = options.length;
            var db = (len>6) ? 4 : 12;
            node.jChartIt({title: String.format("Software Summary ({0} Assets)", data.results.length), dividedBy: db, width: 300, height: 300, onClick: onClick, data: options, max: 10, type: 'pie', legend: true});
        }
    };

    var node = dCrt('div').css({margin: '10px 10px'});
    pnl.append(node);

    methods.chart(node, raw_data);
}
else{
    window.setTimeout(function () {
        load();
    }, 300);
}
