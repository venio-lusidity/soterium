var grid_grp_init;
var lusidity;
function start() {
    if((typeof jQuery !== 'undefined')){
        if(!grid_grp_init) {
            grid_grp_init = true;
            lusidity = $('.grid-grp-content');
            var href = window.location.href.toString();
            var hrefUrl = $.jCommon.url.create(href);
            var pId = hrefUrl.getParameter('pId');
            var sId = hrefUrl.getParameter('sId');

            function load(scriptUri, onLoaded) {
                var scriplets = $('#scriplets');
                var script = $(document.createElement('script'));
                $(scriplets).append(script);
                if ($.isFunction(onLoaded)) {
                    script.on('load', function () {
                        window.setTimeout(function () {
                            onLoaded();
                        }, 300);
                    });
                }
                script.attr("type", "text/javascript");
                script.attr("src", scriptUri);
            }

            if (pId && sId) {
                var hd = $('head');
                var ver = $.jCommon.getRandomId('nc');
                var css = [
                    '/assets/jquery/jquery-ui.min.css',
                    '/assets/bootstrap/css/bootstrap.min.css',
                    '/assets/fonts/sofiapro_regular_macroman/stylesheet.css',
                    '/assets/fonts/sofiapro_lightitalic_macroman/stylesheet.css',
                    '/assets/fonts/sofiapro_regular_macroman/stylesheet.css',
                    '/assets/fonts/sofiapro_lightitalic_macroman/stylesheet.css',
                    '/assets/css/style.css',
                    '/assets/css/color.css',
                    '/assets/icon8/os/css/styles.min.css',
                    '/assets/glyphicons/css/glyphicons.css',
                    '/assets/glyphicons/css/glyphicons-filetypes.css',
                    '/assets/glyphicons/css/glyphicons-halflings.css',
                    '/assets/glyphicons/css/glyphicons-social.css',
                    '/assets/css/tabs.css',
                    '/assets/css/elusidate.css',
                    '/assets/bootstrap/css/bootstrap-tour.min.css'
                ];

                $.each(css, function () {
                    $.jCommon.load.css(this.toString() + '?v=' + ver);
                });
                var scripts = ['/assets/lusidity/jquery.htmlEngine.js', '/assets/lusidity/jquery.environment.js'];
                var loaded = 0;
                function check() {
                    if (loaded === scripts.length && undefined !== $.htmlEngine && undefined !== $.environment) {
                        scripts = [
                            '/assets/js/loaders/jquery.loaders.js',
                            '/assets/js/jquery.scrollHandler.js',
                            '/assets/js/jNodeReady.js',
                            '/assets/lusidity/plugins/jquery.osIcon.js',
                            '/assets/lusidity/plugins/pGrid_v1.js',
                            '/assets/js/jFilterBar_v7.js'
                        ];
                        loaded = 0;
                        function check2() {
                            if (loaded === scripts.length && undefined !== $.pFilterBar && undefined !== $.pGrid) {
                                var server = {
                                    primary: $.jCommon.string.contains(href, "stgw.rmk.disa.mil") ? 'https://stg-1.rmk.disa.mil/svc' : ($.jCommon.string.contains(href, 'rmk.disa.mil', true) ? 'https://svc-1.rmk.disa.mil/svc' : 'https://' + hrefUrl.hostNoPort + ':8443/svc'),
                                    secondary: $.jCommon.string.contains(href, "stgw.rmk.disa.mil") ? 'https://stg-1.rmk.disa.mil/svc' : ($.jCommon.string.contains(href, 'rmk.disa.mil', true) ? 'https://svc-2.rmk.disa.mil/svc' : 'https://' + hrefUrl.hostNoPort + ':8443/svc'),
                                    'delete': $.jCommon.string.contains(href, "stgw.rmk.disa.mil") ? 'https://stg-1.rmk.disa.mil/svc' : ($.jCommon.string.contains(href, 'rmk.disa.mil', true) ? 'https://svc-2.rmk.disa.mil/svc' : 'https://' + hrefUrl.hostNoPort + ':8443/svc'),
                                    hosts: []
                                };
                                if ($.jCommon.string.contains(href, "stgw.rmk.disa.mil")) {
                                    server.hosts.push({title: "Athena", url: 'https://stg-1.rmk.disa.mil/svc'});
                                }
                                else if ($.jCommon.string.contains(href, "rmk.disa.mil")) {
                                    server.hosts.push({title: "Athena", url: 'https://svc-1.rmk.disa.mil/svc'});
                                    server.hosts.push({title: "Hercules", url: 'https://svc-2.rmk.disa.mil/svc'});
                                }
                                else {
                                    server.hosts.push({
                                        title: "Athena",
                                        url: 'https://' + hrefUrl.hostNoPort + ':8443/svc'
                                    });
                                }
                                lusidity.environment({
                                    mockData: false,
                                    serviceHostUri: server.primary,
                                    server: server
                                });
                                var data = window.parent.getGroupData(pId, sId);
                                lusidity.css({
                                    maxHeight: data.maxHeight + 'px',
                                    overflow: 'hidden'
                                });
                                lusidity.on('table-view-row-added', function (e) {
                                    window.parent.tableViewRowAdded(pId, sId, e);
                                });
                                lusidity.on('table-view-rows-loaded', function (e) {
                                    window.parent.tableViewRowsLoaded(pId, sId, e);
                                });
                                lusidity.on('table-view-loaded', function (e) {
                                    window.parent.tableViewLoaded(pId, sId, e);
                                });
                                lusidity.pGrid(data);
                            }
                            else {
                                window.setTimeout(check2, 100);
                            }
                        }
                        $.each(scripts, function () {
                            load(this.toString() + '?v=' + ver, function () {
                                loaded++;
                            });
                        });
                        check2();
                    }
                    else {
                        window.setTimeout(check, 100);
                    }
                }

                $.each(scripts, function () {
                    load(this.toString() + '?v=' + ver, function () {
                        loaded++;
                    });
                });
                check();
            }
        }
    }
    else{
        window.setTimeout(start, 100);
    }
}
start();

