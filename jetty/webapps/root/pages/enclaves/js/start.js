if(!pl_init){
    pl_init = true;
    lusidity.css({overflow: 'hidden'});
    //function load() {
    $.login.authorized({"groups": ["IAVM Summation"], "r": false, "c": function (e) {
        if(e.auth){
            var node = $('.nav-content');
            node.prepend('<li><a class="nav-list-item" data-idx="1" href="/pages/iavms/index.html"><div class="icon-parent"><span class="glyphicons glyphicons-pie-chart"></span></div><div class="panel-heading-title-35">IAVM Summation</div></a></li>')
        }
    }});
    var nv = $('.viewer-node');
    var mb = $('#menu-bar');
    // {name: '', glyphicon: 'glyphicon-education', tn: 'help', title: 'Learn how to use this page', cls: 'blue', id: "tourButton"},
    mb.menuBar({
        target: nv, buttons: [
            {name: '', glyphicon: 'glyphicon-star-empty', tn: 'fav', title: 'Add to Favorites', cls: 'blue'},
            {name: '', glyphicon: 'glyphicon-star', tn: 'default-fav', title: 'Set as Default', cls: 'blue'}
        ]
    });
    var nl = $('.panel-left');
    var nm = $('.panel-middle');
    var ne = $('.edit-node');
    var na = $('.add-node');
    var nf = $('.favorite-node');
    var nt= $('.tree-node');
    lusidity.jEnclaves({
        prsnify: true,
        pnlNodeLeft: nl,
        pnlNodeMiddle: nm,
        nodeViewer: nv,
        nodeMenu: mb,
        nodeAdd: na,
        nodeEdit: ne,
        nodeFav: nf,
        nodeTree: nt
    });
    lusidity.panelCollapse({
        panels: [
            { node: nl, collapsable: true},
            { node: nm}
        ]
    });
    $('#summary-tab').on('click', function () {
        lusidity.enclaves('summary');
    });
    function r() {
        var h = $('.alert-zone').height();
        lusidity.resizePage((h*-1));
    }
    debugger;
    $('#jobs-node').jJobStatus({
        view: 'bar',
        onDone: function (data, node, valid) {
            node.children().remove();
            if(valid){
                node.parent().css({display: 'block'});
                node.append(dCrt('div').css({textAlign: 'middle'}).html('Data jobs in progress.'));
                r();
                node.prev().on('click', function (e) {
                   e.stopPropagation();
                   e.preventDefault();
                   node.parent().slideUp(300, function () {
                       r();
                   });
                });
            }
            else{
                node.parent().css({display: 'none'});
            }
        },
        onClose: function () {
            r();
        },
        jobs: ["com.lusidity.jobs.data.AssetHierarchyJob",
            "com.lusidity.jobs.vulnerability.AggregateCommonDataJob",
            "com.lusidity.jobs.vulnerability.AggregatorEnclaveJob",
            "com.lusidity.jobs.vulnerability.AssetVulnDetailsJob",
            "com.lusidity.jobs.vulnerability.IavmsCompliancyJob",
            "com.lusidity.jobs.vulnerability.IavmToAssetJob",
            "com.lusidity.jobs.vulnerability.VulnerabilityBreakdownJob",
            "com.lusidity.jobs.vulnerability.AuditVulnerabilitiesJob"],
        excluded: []
    });
    if (_asi.enabled) {
        var a;
        function chk() {
            var b = (_asi.web && $.jCommon.dateTime.isAfter(_asi.starts) && _asi.wmsg ? _asi.wmsg : _asi.msg);
            if(b!==a) {
                a = b;
                lusidity.info.hide(function () {
                    lusidity.info.yellow(b);
                    lusidity.info.show(_asi.delay, function (e) {
                    });
                });
            }
            if($.jCommon.dateTime.isToday(_asi.starts)){
                window.setTimeout(chk, (1000*60));
            }
        }
        chk();
    }
}
