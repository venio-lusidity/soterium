;(function ($) {

    //Object Instance
    $.rssReader = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.rssReader.defaults, options);

        // Store a reference to the environment object
        el.data("rssReader", state);

        function RSSImage(item){
            if(item.image){
                item = item.image;
            }

            if(item.url && $.jCommon.string.endsWith(item.url, 'jpg', true)
                && $.jCommon.string.endsWith(item.url, 'gif', true)
                && $.jCommon.string.endsWith(item.url, 'png', true)) {
                this.url = item.url;
                this.description = item.description;
                this.height = item.height;
                this.link = item.link;
                this.title = item.title;
                this.width = item.width;
            }
        }
        function RSS(data, feed){
            var item = data.rss ? data.rss.channel : null;
            this.feed = feed;
            if(!item && data.rdf_RDF){
                item = data.rdf_RDF.channel;
                item.item = data.rdf_RDF.item;
            }
            if(item) {
                this.link = feed.externalUrl ? feed.externalUrl : item.link;
                this.description = item.description;
                this.rights = item.rights ? item.rights : item.dc_rights;
                this.language = item.language ? item.language : item.dc_language;
                this.title = item.title;
                this.pubDate = item.pubDate ? item.pubDate : item.dc_date;

                this.image = new RSSImage(item);
                this.items = item.item;
            }
        }

        // Private environment methods
        methods = {
            init: function() {
                state.append(state.loader);

                if(state.opts.feeds){

                    state.addClass('rss-panel');
                    $.each(state.opts.feeds, function(){
                        var feed = this;
                        var placeholder = $(document.createElement('div'));
                        state.append(placeholder);
                        $.htmlEngine.busy(placeholder, {type: 'cube'});
                        var onSuccess = function(data){
                            methods.html.create(data, feed, placeholder);
                            placeholder.loaders('hide');
                        };
                        var onFail = function(){
                            methods.failed(feed, placeholder);
                            placeholder.loaders('hide');
                        };
                        methods.get(feed.url, (state.opts.onSuccess ? state.opts.onSuccess : onSuccess), onFail, true);
                    });

                }
            },
            failed: function(feed, placeholder){
                var msg = $(document.createElement('div')).addClass('italics centered')
                    .html(feed.title + ' unavailable.').css({marginBottom: '10px'});
                placeholder.append(msg);
            },
            get: function(url, onSuccess, onFail, async) {
                async = async ? true : false;
                url = "/rss?url=" + encodeURI(url);
                if(state.opts.offline){
                    url += '&offline=true'
                }
                $.htmlEngine.request(url, onSuccess, onFail, null, "get", async);
            },
            html: {
                create: function(data, feed, placeholder){
                    var rss = new RSS(data, feed);
                    if(rss.items){
                        var on = 0;
                        var panel = $(document.createElement('div')).addClass('panel panel-default ' + state.opts.panelCls);
                        var panelHeading = $(document.createElement('div')).addClass('panel-heading');
                        var link = $(document.createElement('a')).addClass('rss-title').attr('href', rss.link)
                            .attr('target', '_blank').html(rss.title);
                        panelHeading.append(link);

                        var panelBody = $(document.createElement('div')).addClass('panel-body');
                        placeholder.append(panel);

                        panel.append(panelHeading).append(panelBody);

                        function make(item){
                            if (item.title && item.title.length>0) {
                                var node = $(document.createElement('node')).addClass('rss-feed');
                                var isLink = $.jCommon.json.hasProperty(item, 'link');
                                var title = $(document.createElement(isLink ? 'a' : 'div')).html(item.title).addClass('title');
                                if (isLink) {
                                    title.attr('href', item.link).attr('target', '_blank');
                                }
                                node.append(title);
                                if (item.pubDate) {
                                    var pubDate = $(document.createElement('div')).html('Publication: ' + item.pubDate).addClass('meta-data');
                                    node.append(pubDate);
                                }
                                var desc = $(document.createElement('div')).html(item.description).addClass('meta-data');
                                node.append(desc);
                                panelBody.append(node);
                                on++;
                            }
                        }
                        if(rss.items.title){
                            make(rss.items);
                        }
                        else {
                            $.each(rss.items, function () {
                                if (on == 0 || on < rss.feed.limit) {
                                    make(this);
                                }
                            });
                        }
                        if(on==0){
                            panelBody.append($(document.createElement('div')).html("Feed not available.").css({paddingLeft: '5px'}));
                        }
                    }
                    else{
                        methods.failed(feed, placeholder);
                    }
                }
            }
        };
        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.rssReader.defaults = {
        itemsPath: 'rdf_RDF.item',
        title: "RSS Feed",
        panelCls: 'no-border',
        limit: 5,
        descOffset: 60
    };


    //Plugin Function
    $.fn.rssReader = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.rssReader($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $rssReader = $(this).data('rssReader');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $rssReader;
            }
        }
    };

})(jQuery);
