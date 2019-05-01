

;(function ($) {

    //Object Instance
    $.jTimePicker = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.jTimePicker.defaults, options);

        // Store a reference to the environment object
        el.data("jTimePicker", state);

        // Private environment methods
        methods = {
            init: function() {
                var outer = dCrt('div').css({position: 'relative', display: 'inline-block'});
                var node = state;
                if (!state.is('input')) {
                    node = dCrt('input');
                    state.append(node);
                }
                outer.insertBefore(node);
                outer.append(node);

                if(state.opts.readOnly){
                    node.attr('readonly', 'readonly');
                }

                var id = $.jCommon.getRandomId('tp');
                node.attr('id', id);
                if (!node.val()) {
                    node.val(state.opts.value);
                }

                var c = dCrt('div').css({position: 'absolute', width: 'inherit', zIndex: '2147470000'}).hide();
                var box = dCrt('div').addClass(state.opts.boxCls).css({position: 'relative'});
                var h = 130;
                if(state.opts.hasBtn){
                    h = 168;
                }
                dMax(box, h,  219);
                outer.append(c.append(box));

                methods.make(node, box, 200);
                methods.bind(node, c);
                state.last = node.val();
            },
            bind: function (node, c) {
                node.on('focus', function (e) {
                    if(state.opts.focus && $.isFunction(state.opts.focus)){
                        state.opts.focus(e, node, node.val());
                    }
                    c.show();
                });
                node.on('click', function (e) {
                    if(state.opts.clicked && $.isFunction(state.opts.clicked)){
                        state.opts.clicked(e, node, node.val());
                    }
                    c.show();
                });
                node.on('blur', function (e) {
                    if(!state.hovering) {
                        c.hide();
                        if (state.changed && state.opts.close && $.isFunction(state.opts.close)) {
                            state.opts.close(e, node, node.val());
                        }
                    }
                    else{
                        console.log('hovering no blur');
                    }
                });
            },
            make: function (node, box, width) {
                var tbl = dCrt('table');
                box.append(tbl);
                dMax(tbl, null, width);

                var row = dCrt('tr');
                tbl.append(row);
                var cmn = {verticalAlign: 'middle', textAlign: 'center', padding: '2px 2px'};
                var hh = dCrt('td').html('Hours').addClass(state.opts.hdrCls).css(cmn);
                var mm = dCrt('td').html('Minutes').addClass(state.opts.hdrCls).css(cmn).css({paddingLeft: '10px'});
                row.append(hh).append(mm);

                var rc = dCrt('tr');
                tbl.append(rc);
                dMax(rc, null, width);

                hh = dCrt('td').css({textAlign: 'center'});
                mm = dCrt('td').css({paddingLeft: '10px', textAlign: 'center'});
                rc.append(hh).append(mm);

                function mr(tbl, values, isHrs) {
                    var r = dCrt('tr');
                    tbl.append(r);
                    $.each(values, function () {
                        var cellValue = this;
                        var cell = dCrt('td').addClass('bordered-trans').html(cellValue).css({
                            padding: '2px 2px',
                            cursor: 'pointer'
                        });
                        cell.on('mouseover', function () {
                            state.hovering = true;
                            cell.removeClass('bordered-trans').addClass(state.opts.hoverCls);
                        });
                        cell.on('mouseleave', function () {
                            state.hovering = false;
                            cell.removeClass(state.opts.hoverCls).addClass('bordered-trans');
                        });
                        cell.on('click', function (e) {
                            var val = node.val();
                            var t = isHrs ? hh : mm;
                            t.find('.selected').removeClass('selected');
                            cell.addClass('selected');
                            val = methods.fix(val, cellValue, isHrs);
                            node.val(val);
                            node.focus();
                            state.changed = true;
                            if(state.btn){
                                state.btn.removeAttr('disabled');
                            }
                        });
                        r.append(cell);
                    });
                }

                var rh = dCrt('table').css({margin: '2px 2px'});
                hh.append(rh);

                mr(rh, ['00', '01', '02', '03', '04', '05'], true);
                mr(rh, ['06', '07', '08', '09', '10', '11'], true);
                mr(rh, ['12', '13', '14', '15', '16', '17'], true);
                mr(rh, ['18', '19', '20', '21', '22', '23'], true);

                var rm = dCrt('table').css({margin: '2px 2px'});
                mm.append(rm);
                mr(rm, ['00', '05', '10']);
                mr(rm, ['15', '20', '25']);
                mr(rm, ['30', '35', '40']);
                mr(rm, ['45', '50', '55']);

                if (state.opts.hasBtn) {
                    state.btn = dCrt('btn').addClass('btn ' + state.opts.btnCls).html('Save').attr('disabled', 'disabled');
                    var br = dCrt('tr');
                    var bc = dCrt('td').attr('colspan', '2').css({textAlign: 'right', paddingRight: '5px'});
                    tbl.append(br.append(bc.append(state.btn)));
                    state.btn.on('click', function () {
                        state.hovering = false;
                        node.blur();
                    });
                }
            },
            fix: function (val, cellValue, isHours) {
                var hrs;
                var min;
                if(val && $.jCommon.string.contains(val, ":")) {
                    hrs = $.jCommon.string.getFirst(val, ':');
                    min = $.jCommon.string.getLast(val, ':');
                }
                if(isHours){
                    hrs = cellValue;
                }
                else{
                    min=cellValue;
                }
                if(!$.jCommon.string.isNumber(hrs)){
                    hrs = '23';
                }
                if(!$.jCommon.string.isNumber(min)){
                    min = '50';
                }
                if(hrs.len===1){
                    hrs = '0'+hrs;
                }
                if(min.len===1){
                    min = '0'+min;
                }
                return String.format('{0}:{1}', hrs, min);
            }
        };
        //public methods

        // Initialize
        methods.init();
    };

    //Default Settings
    $.jTimePicker.defaults = {
        boxCls: 'drop-box',
        btnCls: 'green',
        hdrCls: 'blue',
        hoverCls: 'blue bordered box-shadow',
        value: '23:50'
    };


    //Plugin Function
    $.fn.jTimePicker = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            new $.jTimePicker($(this), method);
        } else {
            // Helper strings to quickly perform functions
            var $jTimePicker = $(this).data('jTimePicker');
            switch (method) {
                case 'exists': return (null!==$jTimePicker && undefined!==$jTimePicker && $jTimePicker.length>0);
                case 'state':
                default: return $jTimePicker;
            }
        }
    };

})(jQuery);
