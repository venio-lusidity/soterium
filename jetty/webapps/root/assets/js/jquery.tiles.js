;(function ($) {
    //Object Instance
    $.tiles = function(el, options) {
        var state = el,
            methods = {};
        state.opts = $.extend({}, $.tiles.defaults, options);
        state.viewer = $(document.createElement("div"));
        state.showScale = $.jCommon.url.create(window.location.href).hasParam('scale');
        state.resizing = false;
        state.images = [];
        state.worker = null;
        state.queued = 0;
        state.isMobile = $.jCommon.string.contains(navigator.userAgent.toLowerCase(), ['android','iphone', 'ipad'], true);
        state.isIPad = $.jCommon.string.contains(navigator.userAgent.toLowerCase(), ['ipad'], true);
        state.cols = 0;
        state.debug = false;
        state.metaNode = $(document.createElement('div')).addClass('tile-meta').hide();
        state.current = {};

        state.KEY_ID = '/vertex/uri';

        // Store a reference to the environment object
        el.data("tiles", state);

        // Private environment methods
        methods = {
            init: function() {
                $.htmlEngine.loadFiles(state, null, ["/assets/css/tiles.css?nocache=" + $.jCommon.getRandomId('nc')]);
                state.addClass("tile-table");
                function ready() {
                    var sw = methods.getStateWidth();
                    if(sw!=undefined && sw>0){
                        init();
                    }
                    else{
                        if(!state.is('visible')){
                            state.show();
                        }
                        window.setTimeout(function(){
                            ready();
                        }, 300);
                    }
                }
                function init(){
                    state.resizing = true;
                    methods.bind();
                    methods.html.create(state.opts.data);
                    window.setTimeout(function () {
                        methods.resize();
                        state.resizing = false;
                    }, 500);
                    lusidity.environment('onResize', function(){
                        methods.onResize();
                    });
                }
                ready();

                $(document).on('panel-collapse', function () {
                    methods.resize();
                    state.resizing = false;
                });
            },
            bind: function(){
                $(document).on('keyup', function(e){
                    var code = parseInt(e.keyCode);
                    if (code === 27 && state.metaNode.is(':visible')) {
                        state.last = null;
                        state.carrot.hide();
                        state.carrotIn.hide();
                        state.unlink.hide();
                        state.unlink.unbind();
                        state.metaNode.slideUp();
                    }
                });
            },
            getRandomId: function(prefix){
                var rn = function(){return Math.floor(Math.random()*999999);};
                return prefix + '_' + rn() + '_' + rn();
            },
            getStateWidth: function(){
                return state.width();
            },
            getColumns: function(){
                var sw = methods.getStateWidth();
                var cols = Math.floor(sw / state.opts.minWidth);// ( + state.opts.marginLeft));
                cols = cols>state.opts.maxItems ? state.opts.maxItems : cols;
                return cols;
            },
            getWidth: function(){
                var cols = methods.getColumns();
                var sw = methods.getStateWidth();
                var margin = (cols-1)*(state.opts.marginLeft);
                var width = ((sw-margin)/cols)-(state.opts.widthOffset/cols);
                if(cols===1){
                    width = state.opts.minWidth;
                }
                return Math.floor(((width>state.opts.maxWidth) ? state.opts.maxWidth : width));
            },
            getRandomClass: function(){
                if(!state.opts.isColored)
                {
                    return 'bg-white bdr-white';
                }
                var background = [
                    'red',
                    'orange',
                    'yellow',
                    'blue',
                    'green',
                    'purple',
                    'grey'
                ];
                function randomNumber(last){
                    var result = Math.floor(Math.random() * background.length);
                    if(result===last){
                        result = randomNumber(result);
                    }
                    state.opts.lastColor = result;
                    return result;
                }
                var bg = background[randomNumber(state.opts.lastColor)];
                return 'bg-' + bg + (state.opts.includeBorder ? ' bdr-' + bg : '');
            },
            getStatus: function (data) {
                var r = '';
                if(data && !$.jCommon.string.equals(data.compliant, 'unknown')){
                    r = ($.jCommon.string.equals(data.compliant, "yes")) ? "green" : "red";
                }
                return r;
            },
            html:{
                create: function(data){
                    $.each(data.results, function(){
                        try {
                            var item = this;
                            var id = item[state.KEY_ID];
                            var tile = $(document.createElement('div')).attr('title', (item.title)).addClass('tile-cell')
                                .attr('data-id', id).attr('ordinal', item.ordinal).css({textAlign: 'center', verticalAlign: 'middle', padding: '5px'});
                            if (state.debug) {
                                tile.append(dCrt("h6").addClass("shadowed").html(item.ordinal));
                            }
                            if (state.opts.css) {
                                tile.addClass(state.opts.css);
                            }
                            if (item.vertexType) {
                                var name = $.jCommon.string.getLast(item.vertexType, "/");
                                if (!$.jCommon.string.equals(name, "asset", true) && !$.jCommon.string.equals(name, "device", true)) {
                                    tile.css({backgroundImage: 'url("/assets/img/types/' + name + '.png")'});
                                }
                            }
                            if (state.opts.hasMetrics) {
                                if (item.metrics && item.metrics.html && item.metrics.html.cls) {
                                    tile.removeClass('med-black light-grey').addClass(item.metrics.html.cls);
                                }
                            }
                            if (tile.hasClass('med-black')) {
                                tile.removeClass('med-black').addClass('light-grey');
                            }
                            var cKey = '/technology/software/applications/compliancy';

                            tile.on('mouseup', function (e) {
                                if (!state.dragging) {
                                    var event, name;
                                    if (e.which == 1) {
                                        methods.html.meta(tile, item);
                                        name = 'tileNodeLeftClick';
                                    }
                                    else if (e.which == 3) {
                                        //  name = 'tileNodeRightClick';
                                    }
                                    if (name) {
                                        state.current.node = tile;
                                        state.current.node.item = item;
                                        event = jQuery.Event(name, {node: state.current.node});
                                        state.trigger(event);
                                    }
                                }
                            });
                            state.append(tile);

                            if ($.isFunction(state.opts.onImage)) {
                                state.opts.onImage(tile, item);
                            }

                            if (!$.jCommon.string.startsWith(item.vertexType, '/electronic/system/enclave')
                                && !$.jCommon.string.equals(item.compliant, 'unknown')) {
                                var cls = methods.getStatus(item);
                                var t = $(document.createElement('div')).addClass('triangle-top-left');
                                var i = $(document.createElement('div')).addClass('triangle-top-left-inner-' + cls);
                                t.attr('title', "Compliant: " + item.compliant);
                                tile.prepend(i).prepend(t);
                            }
                        }
                        catch(e){
                            console.log(e);
                        }
                    });
                    state.trigger('tileViewDataLoaded', {});
                },
                meta: function(tile, item){
                    if (state.metaNode.is(':visible')) {
                        state.carrot.hide();
                        state.carrotIn.hide();
                        state.unlink.hide();
                        state.unlink.unbind();
                        state.metaNode.slideUp(function(){
                            show();
                        });
                    }
                    else{
                        show();
                    }
                    function show(){
                        if(!state.last || state.last!=tile) {
                            state.last = tile;
                            if (!state.metaBody) {
                                state.metaBody = $(document.createElement('div')).addClass('tile-meta-body').css({padding: '5px 5px 5px 5px'});
                                state.metaNode.append(state.metaBody);
                            }
                            if (!state.carrot) {
                                state.carrot = $(document.createElement('div')).addClass('carrot-up');
                                state.carrotIn = $(document.createElement('div')).addClass('carrot-up-inner');
                                state.metaNode.append(state.carrot).append(state.carrotIn);
                            }
                            if(!state.unlink){
                                state.unlink = $(document.createElement('div')).css({ visibility: 'hidden', position: 'absolute', top: '2px', 'right': '4px', cursor: 'pointer'});
                                state.unlink.append($(document.createElement('span')).attr('title', "Delete link.").addClass('glyphicon glyphicon-remove shadowed')
                                    .css({color: 'red'}));
                            }
                            state.unlink.on('mouseup', function(e){
                                e.preventDefault();
                                e.stopPropagation();
                                if($.isFunction(state.opts.onDelete)){
                                    state.opts.onDelete(tile, item);
                                }
                            });
                            tile.append(state.unlink);
                            state.unlink.show();

                            state.metaBody.children().remove();
                            var url = item[state.KEY_ID];
                            var s = function(data) {
                                var parent = tile.parent();
                                var w = tile.outerWidth();
                                var idx = tile.index();
                                var dif = tile.position().left + (w/2) - (state.opts.carrotWidth/2);
                                state.carrot.css({left: dif + 'px'});
                                state.carrotIn.css({left: dif + 1 + 'px'});
                                state.metaNode.width(parent.width());
                                parent.append(state.metaNode);

                                var content = $(document.createElement('div'));
                                var primaryType = data['vertexType'];
                                var row = $(document.createElement('div')).addClass('row');
                                var col;
                                if($.jCommon.string.startsWith(primaryType, '/electronic/system/enclave')) {
                                    col = $(document.createElement('div')).addClass('col-md-12');

                                    methods.appendNode({label: 'Title', value: data.title, link: data[state.KEY_ID]}, col);
                                    var t = $.jCommon.string.getLast(primaryType, '/');
                                    t = $.jCommon.string.replaceAll(t, "_", " ");
                                    t = $.jCommon.string.toTitleCase(t);
                                    methods.appendNode({label: 'Type', value: t}, col);

                                    row.append(col);

                                    col = $(document.createElement('div')).addClass('col-md-12');

                                    methods.appendNode({label: 'Hostname', value: data.hostname}, col);
                                    methods.appendNode({label: 'Domain', value: data.domainName}, col);

                                    row.append(col);
                                    content.append(row);
                                    if(data['/system/primitives/raw_string/descriptions'] && data['/system/primitives/raw_string/descriptions'].results[0]) {
                                        var desc = $(document.createElement('div'));
                                        methods.appendNode({
                                            label: 'Description',
                                            value: data['/system/primitives/raw_string/descriptions'].results[0].value
                                        }, desc);
                                        content.append(desc);
                                    }
                                }
                                else{
                                    col = $(document.createElement('div')).addClass('col-md-12');

                                    methods.appendNode({label: 'Title', value: data.title, link: data[state.KEY_ID]}, col);
                                    methods.appendNode({label: 'Manufacturer', value: data.manufacturer}, col);
                                    methods.appendNode({label: 'Make', value: data.make}, col);
                                    methods.appendNode({label: 'Model Number', value: data.modelNumber}, col);
                                    methods.appendNode({label: 'Serial Number', value: data.serialNumber}, col);

                                    var os = $(document.createElement('div'));
                                    col.append(os);
                                    var s = function (data) {
                                        if(data ){
                                            var v = $.jCommon.json.getProperty(data, 'results.title');
                                            if(v) {
                                                methods.appendNode({label: 'OS', value: v}, os);
                                            }
                                        }
                                    };
                                    var f= function (){};
                                    $.htmlEngine.request(data[state.KEY_ID]+'/properties/technology/software/operatingSystem', s, f, 'get');

                                    row.append(col);

                                    col = $(document.createElement('div')).addClass('col-md-12');

                                    methods.appendNode({label: 'Hostname', value: data.hostname}, col);
                                    methods.appendNode({label: 'Domain', value: data.domainName}, col);
                                    methods.appendNode({label: 'Last Scanned', value: data.lastScanned ? $.jCommon.dateTime.format(data.lastScanned, 'j\\-M\\-Y') : 'Unknown'}, col);

                                    row.append(col);

                                    col = $(document.createElement('div')).addClass('col-md-12');

                                    var innerTable = $(document.createElement('table'));
                                    col.append(innerTable);
                                    row.append(col);
                                    var na = '/electronic/network/network_adapter/networkAdapters';
                                    if(data[na] && data[na].length>0) {
                                        $.each(data[na], function () {
                                            var tr = $(document.createElement('tr'));
                                            col = $(document.createElement('td')).css({
                                                'padding': '0 5px 0 5px',
                                                'vertical-align': 'top'
                                            });
                                            methods.appendNode({label: "MAC", value: this.macAddress}, col);
                                            tr.append(col);
                                            col = $(document.createElement('td')).css({
                                                'padding': '0 5px 0 5px',
                                                'vertical-align': 'top'
                                            });
                                            methods.appendNode({label: "IP", value: this.ipAddress}, col);
                                            tr.append(col);
                                            innerTable.append(tr);
                                        });
                                    }
                                    content.append(row);
                                }

                                state.metaBody.append(content);
                            };
                            var f = function () {
                                lusidity.info.red("Sorry something went wrong. Please try again.");
                                lusidity.show(5);
                            };
                            $.htmlEngine.request(url, s, f, null, 'get');
                            state.carrot.show();
                            state.carrotIn.show();
                            state.metaNode.slideDown(function () {
                                if(state.opts.scrollNode && state.opts.scrollNode.scrollHandler('exists')) {
                                    var viewable = state.opts.scrollNode.scrollHandler('isInViewport', state.metaNode);
                                    if (!viewable) {
                                        var top = state.opts.scrollNode.scrollTop() + state.metaNode.height() + 20 + tile.height();
                                        state.opts.scrollNode.animate({
                                            scrollTop: top
                                        }, 500);
                                    }
                                }
                            });
                        }
                        else{
                            state.last = null;
                        }
                    }
                }
            },
            appendNode: function(item, content){
                if(undefined!=item.value && null!=item.value) {
                    var node = $(document.createElement('div'));
                    var l = $(document.createElement('span')).append(item.label);
                    var sep = $(document.createElement('span')).append(':').css({marginRight: '5px'});
                    var v = $(document.createElement('span'));
                    if(item.link){
                        var link = $(document.createElement('a')).attr('href', item.link).attr('target', '_blank').html(item.value.toString());
                        v.append(link);
                    }
                    else{
                        v.append(item.value.toString())
                    }
                    node.append(l).append(sep).append(v);
                    content.append(node);
                }
            },
            onResize: function(){
                var rTime = new Date(1, 1, 2000, 12, 0, 0);
                var timeout = false;
                var delta = 500;
                function resizeEnd(){
                    if (new Date() - rTime < delta) {
                        setTimeout(resizeEnd, delta);
                    } else {
                        timeout = false;
                        methods.resize();
                    }
                }
                $(window).resize(function() {
                    rTime = new Date();
                    if (timeout === false) {
                        timeout = true;
                        setTimeout(function(){
                            resizeEnd();
                        }, delta);
                    }
                });
            },
            resize: function(){
                var cols = methods.getColumns();
                if (state.cols !== cols) {
                    var rows = state.find('.tile-row');
                    var tiles = state.find('.tile-cell');
                    state.prepend(tiles);
                    rows.remove();
                    var width = methods.getWidth();
                    var on = 0;
                    var row;
                    $.each(tiles, function () {
                        var tile = $(this);
                        if (on === 0) {
                            row = $(document.createElement('div')).addClass('tile-row');
                            state.append(row);
                        }
                        tile.css({
                            maxHeight: state.opts.minHeight + 'px',
                            height: state.opts.minHeight + 'px',
                            width: width + 'px',
                            maxWidth: width + 'px'
                        });
                        row.append(tile);
                        on++;
                        if (on >= cols) {
                            on = 0;
                        }
                    });
                }
            }
        };
        //public methods
        state.addAll = function(options){
            if(options.data && options.data.results){
                var fData = {results: [] };
                $.each(options.data.results, function(){
                    var a = this;
                    var found = false;
                    $.each(state.opts.data.results, function () {
                        var b = this;
                        if($.jCommon.string.equals(a[state.KEY_ID], b[state.KEY_ID])){
                            found = true;
                            return false;
                        }
                    });
                    if(!found){
                        fData.results.push(a);
                    }
                });
                methods.html.create(fData);
                state.cols = null;
                methods.resize();
                if(fData.results){
                    if(!state.opts.data){
                        state.opts.data={};
                    }
                    if(!state.opts.data.results || !$.jCommon.is.array(state.opts.data.results)){
                        state.opts.data.results=[];    
                    }
                    if(options.data.count) {
                        state.opts.data.count = options.data.count;
                    }
                    if(options.data.excluded) {
                        state.opts.data.excluded = options.data.excluded;
                    }
                    $.each(fData.results, function(){
                        if(state.opts.data.count) {
                            state.opts.data.count++;
                        }
                        state.opts.data.results.push(this);    
                    });
                }
            }
        };
        state.add = function(options){
            if(options.data) {
                var item = { results: []};
                item.push( (options.data.results) ? options.data.results[0] : options.data);
                state.addAll({data: item});                
            }
        };
        state.clear = function(){
            state.find('.tile-cell').remove();
            state.find('.tile-row').remove();
            state.opts.data = null;
        };
        state.resize = function(){
            if(!state.resizing){
                methods.init(true);
                state.queued = (((state.queued-1) <= 0) ? 0 : state.queued-1);
            }
            else{
                state.queued++;
                if(state.queued<2)
                {
                    window.setTimeout(function(){
                        state.resize();
                    }, 100);
                }
            }
        };

        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.tiles.defaults = {maxItems: 20, marginLeft: 10, minHeight: 100, minWidth: 100, maxWidth: 100, widthOffset: 18, items: [], isColored: true, lastColor: 0, carrotWidth: 20 };

    //Plugin Function
    $.fn.tiles = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.tiles($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $tiles = $(this).data('tiles');
            if($tiles){
                switch (method) {
                    case 'add': $tiles.add(options);break;
                    case 'addAll': $tiles.addAll(options);break;
                    case 'clear': $tiles.clear();break;
                    case 'exists': return (null!=$tiles && undefined!=$tiles && $tiles.length>0);
                    case 'resize': $tiles.resize(); break;
                    case 'state':
                    default: return $tiles.state;
                }
            }
        }
    };

})(jQuery);
