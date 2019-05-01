if(!pl_init){
    pl_init = true;
    var data = [
        {
            title: "Red Hat Enterprise Linux 6...",
            link: "/domains/...",
            groupedItems: [],
            content: dCrt('div').html("I am leaf 2")
        },
        {
            title: "Red Hat Enterprise Linux 5...",
            link: "/domains/...",
            groupedItems: [],
            content: dCrt('div').html("I am leaf 2")
        },
        {
            title: "Red Hat Enterprise Linux 4...",
            link: "/domains/...",
            groupedItems: [],
            content: dCrt('div').html("I am leaf 2")
        }
    ];
    var pnl = $('.panel-middle');
    pnl.show();
    var header = dCrt('div');
    header.append(dCrt('h2').html('Time Picker Test (timepicker.js)'));
    pnl.append(header);

    var node = dCrt('div');
    node.append(dCrt('h2').html('Spring Test'));
    pnl.append(node);
    node.spring({leafs: data});
}
else{
    window.setTimeout(function () {
        load();
    }, 300);
}
