if(!pl_init) {
    pl_init = true;
    var pc = $('.page-content');
    var node = dCrt('div').css({margin: "10px 10px 10px 10px"});
    pc.append(node);
    var tbl = dCrt('table').addClass('table table-bordered');
    var items = [{compliant: 'yes'}, {compliant: 'no'}, {compliant: 'unauthorized'}, {compliant: 'no_policy'}, {compliant: 'unknown'}];
    $.each(items, function () {
        var cp = $.htmlEngine.compliant(this);
        var row = dCrt('tr');
        tbl.append(row);

        var td = dCrt('td').html(cp.label);
        row.append(td);

        td = dCrt('td').html(cp.tip);
        row.append(td);

        td = dCrt('td').addClass(cp.clr).css({minWidth: '50px'});
        row.append(td);

        td = dCrt('td');
        var gl = $.htmlEngine.glyph(cp.glyph).addClass(cp.fclr);
        td.append(gl);
        row.append(td);
    });
    node.append(tbl);
}
