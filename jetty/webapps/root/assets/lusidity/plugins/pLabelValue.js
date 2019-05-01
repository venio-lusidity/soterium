;(function ($) {

    //Object Instance
    $.pLabelValue = function(el, options) {
        var state = el,
            methods = {};
        state.container = $(el);

        state.worker = (options.schema && options.schema.plugin) ? options : {node: state, data: options.data };
        state.opts = $.extend({}, $.pLabelValue.defaults, (options.schema && options.schema.plugin) ? options.schema.plugin : options);
        state.opts.name = ((options.schema) ? options.schema.name : '');
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.linkStatus = {};
        // Store a reference to the environment object
        el.data("pLabelValue", state);

        // Private environment methods
        methods = {
            init: function() {
                state.worker.node.attr('data-valid', true).show();
                methods.get();
            },
            createNode: function(type, css, cls) {
                return $(document.createElement(type));
            },
            get: function(){
                var url = state.opts.query? '/query' :methods.getUrl();
                var s = function (data) {
                    if (data && data.results && data.results.length > 0){
                        methods.html.init(data);
                    }else{
                        state.worker.node.hide();
                    }
                };
                var qry;
                var mt = 'get';
                if (state.opts.query && state.opts.query.fn){
                    mt = 'post';
                    qry = QueryFactory[state.opts.query.fn](state.worker.data);
                }
                $.htmlEngine.request(url,s,s,qry,mt);
            },
            getUrl: function (start, limit) {
                if (undefined === start) {
                    start = 0;
                }
                if (undefined === limit) {
                    limit = state.opts.limit;
                }
                return state.worker.data[state.KEY_ID] + '/properties'+ state.opts.property + '?start=' + start + '&limit=' + limit + '&direction='+ state.opts.direction;
            },
            html:{
                init: function (data) {
                    var node = dCrt('div');
                    state.worker.node.append(node);
                    var lbl = state.opts.label;
                    if(!$.jCommon.string.empty(lbl)){
                        methods.html.label(node, lbl, true);
                    }
                    var ctn = dCrt('div').css({ padding: '0 2px 0 10px'});
                    node.append(ctn);
                    $.each(data.results, function () {
                       var item = this;
                       var childNode = dCrt('div');
                       ctn.append(childNode);
                       methods.html.create(item,childNode);
                    });
                },
                create: function (item, childNode) {
                    var on = 0;
                    $.each(state.opts.properties, function(){
                        var hasCss = false;
                        var intNode = childNode;
                       var val = item[this.property];
                       if(!val){
                           return true;
                       }
                        if(this.label){
                            var lbl = dCrt('div');
                            intNode.append(lbl);
                            intNode = lbl;
                            hasCss = true;
                            if(!$.jCommon.string.empty(this.label)){
                                methods.html.label(intNode, this.label, hasCss);
                            }
                        }                         
                        var elem = dCrt('div');
                        if(this.linked && !(this.delinkIfDeprecated && state.worker.data.deprecated)){
                            var url = item[state.KEY_ID];
                            var view ='';
                            if(this.params){
                                var pr = '';
                                $.each(this.params, function (k, v) {
                                    if(k==="et_view"){view = k;}
                                    pr = String.format(
                                        '{0}{1}{2}',
                                        pr,
                                        (pr.length > 0)? '&&': '',
                                        k+'='+v
                                    )
                                });
                                if(pr.length > 0){
                                    url += '?' + pr;
                                }
                            }
                            if(this.needsAuth){
                                var v = state.linkStatus[item[state.KEY_ID]];
                                if(!v){
                                    state.linkStatus[item[state.KEY_ID]] = "none";
                                }
                                elem.pAuthorizedLink({lCache: state.linkStatus, view:view,vertexUri:item[state.KEY_ID],linkAttributes:[{id:"title",value:val},
                                    {id:"href", value:url},{id:"target", value: "_blank"}], linkHtml:val, schema:{}});
                                elem = elem.find('a');
                            }
                            else {
                                elem = dCrt('a').attr('href', url).attr('target', '_blank').html(val);
                            }
                        }
                        else{
                            elem.html(val);}
                       if(on > 0){
                           elem.css({ marginLeft: '5px'})
                       }
                       if(hasCss){
                           elem.addClass('data-value');
                       }
                       intNode.append(elem);
                       on++;
                    });
                },
                label: function (node, lbl,hasCss) {
                    var newCss = hasCss? 'data-label': '';
                    var elem = dCrt('div')
                        .addClass(newCss)
                        .css({ fontWeight: 'bold', marginLeft:'5px'})
                        .html(lbl);
                    node.append(elem);
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.pLabelValue.defaults = {
        limit: 5,
        direction:'OUT'
    };


    //Plugin Function
    $.fn.pLabelValue = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.pLabelValue($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $pLabelValue = $(this).data('pLabelValue');
            switch (method) {
                case 'exists': return (null!==$pLabelValue && undefined!==$pLabelValue && $pLabelValue.length>0);
                case 'state':
                default: return $pLabelValue;
            }
        }
    };

    $.pLabelValue.call= function(elem, options){
        elem.pLabelValue(options);
    };

    try {
        $.htmlEngine.plugins.register("pLabelValue", $.pLabelValue.call);
    }
    catch(e){
        console.log(e);
    }

})(jQuery);
