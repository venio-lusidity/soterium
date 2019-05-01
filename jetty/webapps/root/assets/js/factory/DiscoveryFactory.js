;var DiscoveryFactory = {
    aggSummary: function (item, node, leaf, on) {
        if (leaf.title) {
            var t = '';
            if ($.jCommon.is.numeric(item.action.managed)) {
                t += String.format("Managed By Assets: <strong>{0}</strong>", item.action.managed);
            }
            if ($.jCommon.is.numeric(item.action.owned)) {
                if (t.length > 0) {
                    t += ' ';
                }
                t += String.format("Owned By Assets: <strong>{0}</strong>", item.action.owned);
            }
            if ($.jCommon.is.numeric(item.action.ditpr)) {
                if (t.length > 0) {
                    t += "&nbsp;";
                }
                t += String.format("System Assets: <strong>{0}</strong>", item.action.ditpr);
            }
            if ($.jCommon.is.numeric(item.action.location)) {
                if (t.length > 0) {
                    t += "&nbsp;";
                }
                t += String.format("Location Assets: <strong>{0}</strong>", item.action.location);
            }
            if ($.jCommon.is.numeric(item.action.asset)) {
                if (t.length > 0) {
                    t += "&nbsp;";
                }
                t += String.format("Findings: <strong>{0}</strong>", item.action.asset);
            }

            if (t.length > 0) {
                leaf.title.append(dCrt('span').append(t).css({marginLeft: '5px'}));
            }
            leaf.onExpand = function () {
                $.jCommon.load.css('/assets/css/jVulnMatrix.css', function () {
                    $.jCommon.load.script('/assets/js/jVulnMatrix.js', function () {
                        $.jCommon.load.script('/assets/lusidity/plugins/pSummary.js', function () {
                            if (item.action.types && item.action.types.length > 0) {
                                $.each(item.action.types, function () {
                                    var elem = dCrt('div');
                                    node.append(elem);
                                    var ev = this.toString();
                                    elem.pSummary({
                                        'export': true,
                                        data: item.action.data,
                                        dashboard: true,
                                        et_view: ev,
                                        cascade: false
                                    });
                                });
                            }
                        });
                    });
                });
            };
        }
        else {
            $.jCommon.load.script('/assets/lusidity/plugins/pFindings.js?_nc='+$.jCommon.getRandomId('nc'), function () {
                $.each(item.action.types, function () {
                    var elem = dCrt('div');
                    node.append(elem);
                    var ev = this.toString();
                    elem.pFindings({data: item.action.data, et_view: ev});
                });
            }, true);
        }
    }
};