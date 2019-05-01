if(!pl_init){
    pl_init = true;
    window.setTimeout(load, 300);
    function load() {
        var pnlMiddle = $(".panel-middle");
        function setup(h) {


            var c = dCrt('div');
            var span1 = dCrt('span').html('Please go to the&nbsp;');
            var link = dCrt('a').attr('href', '/enclaves').html("Enclaves");
            var span2 = dCrt('span').html('&nbsp;and select an Organization or System Name then set your Dashboard by clicking the <span class="glyphicon glyphicon-bookmark"></span> located at the top of the page.');
            var hd = dCrt('h4').append(span1).append(link).append(span2);
            c.append(hd);

            pnlMiddle.append(c);
            var d = $.jCommon.element.getDimensions(c);
            c.css({position: 'absolute', top: '50%', left: '50%', marginTop: ((d.h/2)*-1)+'px', marginLeft: ((d.w/2)*-1)+'px', textAlign: 'center'});

            $('#notify').notifications({
                hideRead: true,
                url: '/notifications',
                view: 'widget',
                css: {height: '100px', minHeight: '100px'}
            });
            $('#ref').documentation({title: "References", url: '/downloads/docs'});
            //https://nvd.nist.gov/download.cfm#RSS
            $('#left-feeds').rssReader({
                offline: false, feeds: [
                    {
                        url: 'http://iase.disa.mil/links/rss-feeds/rss_iase.xml',
                        externalUrl: 'http://iase.disa.mil',
                        limit: 5,
                        title: 'IASE Feed'
                    },
                    {
                        url: 'https://www.us-cert.gov/ncas/current-activity.xml',
                        externalUrl: 'https://www.us-cert.gov',
                        limit: 5,
                        title: 'US-CERT Current Activity'
                    },
                    {
                        url: 'http://www.defense.gov/news/afps2.xml', externalUrl: 'http://www.defense.gov/news/', limit: 5, title: 'DOD News'
                    },
                    {
                        url: 'https://nvd.nist.gov/download/nvd-rss-analyzed.xml', externalUrl: '', limit: 5, title: 'NVD CVE Alerts'
                    },
                    {
                        url: 'https://www.microsoft.com/security/portal/rss/encyclopediarss.aspx',
                        externalUrl: 'https://www.microsoft.com/en-us/security/portal/',
                        limit: 5,
                        title: 'Microsoft Threat encyclopedia changes'
                    }]
            });
            /* $('#right-feeds').rssReader({
                 offline: false, feeds: []
             }); */

            var url = '/rmk/dashboard/heatmap';
            var pm = $('.panel-middle');
            var s = function (data) {
                if(data){
                    pm.pSummary({data: data, dashboard: true, et_view: data.et_view});
                }
            };
            $.htmlEngine.request(url, s, s, null, 'get');
        }

        function check() {
            var h = pnlMiddle.height();
            if (h > 0) {
                setup(h);
            }
            else {
                window.setTimeout(function () {
                    check();
                }, 500);
            }
        }

        check();
    }
}

