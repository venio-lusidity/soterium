var TableFactory = {
    linkStatus: [],
    get: function (item, opts) {
        var type = item.edgeType ? item.edgeType: '';
        var mp;
        switch (type) {
            case '/technology/security/vulnerabilities/iavms/iavm_asset_compliance_details':
            case '/technology/security/vulnerabilities/asset_vuln_detail':
            case '/object/edge/software_edge':
                var tk = 'other';
                var uk = "otherId";
                if($.jCommon.string.equals(item.edgeType, '/object/edge/software_edge') && $.jCommon.string.equals(item.displayType, '/electronic/network/asset')){
                    opts.qry.materializedViewType = '/technology/security/vulnerabilities/vulnerability_details';
                    opts.qry.materializedViewKey = 'relatedId';
                    opts.qry.materializedViewWildcard = '*/[lid]';
                    tk = 'title';
                    uk = 'relatedId';
                }
                mp = TableFactory.asset({titleKey: tk, uriKey: uk});
                break;
            default:
                mp = TableFactory._default();
                break;
        }
        return {
            maxHeight: opts.maxHeight,
            sub: false,
            grouped: false,
            hovered: true,
            musts: [],
            showBusy: false,
            colResizable: false,
            singleSort: true,
            paging: {
                enabled: true
            },
            filter: (mp.filter ? mp.filter : {
                enabled: false
            }),
            search: (mp.search ? mp.search : {
                enabled: false
            }),
            onFiltered: function (key, value) {
            },
            onBefore: function (items, node) {
                return items;
            },
            onAfter: function () {
                pageCover.busy(false);
            },
            getQuery: function(){
                return opts.qry;
            },
            mapping: mp.mapping
        };
    },
    _default: function () {
        return {
            mapping: [
                {
                    header: {title: "#", css: {minWidth: '50px', width: '50px', maxWidth: '50px'}},
                    property: '#',
                    type: 'integer',
                    css: {minWidth: '50px', width: '50px', maxWidth: '50px'}
                },
                {
                    header: {title: "Results"}, property: 'title', type: 'string',
                    callback: function (td, item, value) {
                        if (!item.uri) {
                            var t = $.jCommon.string.replaceAll(item.vertexType, "/", "_");
                            t = $.jCommon.string.stripStart(t);
                            item.uri = String.format("/domains/{0}/{1}", t, item.lid);
                        }
                        var c = dCrt('div');
                        var a = dCrt('a').attr('href', item.uri).attr('target', "_blank").html(value);
                        td.append(a);
                        if (item.externalUri) {
                            var div = dCrt('div').css({
                                marginTop: '5px',
                                whiteSpace: 'nowrap'
                            });
                            var span = dCrt('span').addClass('glyphicons glyphicons-new-window').css({marginTop: '-5px'});
                            var ext = dCrt('a').attr('href', item.externalUri).attr('target', "_blank").append(div).append(span).html(item.externalUri);
                            div.append(ext).append(span);
                            td.append(div);
                        }
                        var desc = item.description;
                        var un;
                        if ($.jCommon.string.startsWith(value, "undefined", true)) {
                            un = '<br/>This object was created as a placeholder and will be updated in the future when the data is available.';
                        }
                        if (!$.jCommon.string.empty(desc)) {
                            desc = $.jCommon.string.replaceAll(desc, 'null', '');
                            var p = dCrt('p').append(desc);
                            td.append(p);
                        }
                        if (un) {
                            td.append(un);
                        }
                    }
                }
            ]
        }
    },
    asset: function (opts) {
        return {
            search: {
                enabled: false,
                text: "What are you looking for?",
                btn: "Add",
                properties: [/*'title', 'name'*/]
            },
            mapping: [
                {
                    header: {title: "#", css: {minWidth: '50px', width: '50px', maxWidth: '50px'}},
                    property: '#',
                    type: 'integer',
                    css: {minWidth: '50px', width: '50px', maxWidth: '50px'}
                },
                {
                    header: {title: "RS", tip: "Risk Score", css: {minWidth: '30px', width: '30px', maxWidth: '20px'}},
                    property: '',
                    type: 'integer',
                    callback: function (td, item, value, map, filters) {
                        if ($.jCommon.json.hasProperty(item, 'metrics.html.cls')) {
                            td.addClass(item.metrics.html.cls).attr('title', item.metrics.html.label + ': ' + item.packedVulnerabilityMatrix);
                        }
                    }
                },
                {
                    header: {
                        title: "Asset",
                        property: opts.titleKey,
                        sortable: false,
                        sortType: 'string',
                        sortTipMap: {asc: 'asc', desc: 'desc', 'none': 'Risk Score'},
                        css: {minWidth: '132px'}
                    }, property: opts.titleKey, type: 'string', callback: function (td, item, value) {
                    var a = dCrt('a').attr('href', item[opts.uriKey]).attr('target', "_blank").html(value);
                    td.append(a);
                }
                },
                {
                    header: {
                        title: 'HBC',
                        tip: 'HBSS Baseline Compliance',
                        property: 'compliant',
                        css: {minWidth: '50px', width: '50px', maxWidth: '50px'}
                    },
                    css: {minWidth: '50px', width: '50px', maxWidth: '50px'},
                    property: 'compliant', type: 'string', callback: function (td, item, value, map, filters) {
                    var ct = $.htmlEngine.compliant(item);
                    td.attr('title', ct.tip).addClass(ct.clr);
                    td.osIcon({
                        display: "tile",
                        fontSize: '16px',
                        hasTitle: false,
                        hasVersion: false,
                        linked: true,
                        data: {
                            '/properties/technology/software/operatingSystem': {
                                title: item.os,
                                '/vertex/uri': item.osId
                            }
                        }
                    });
                }
                },
                {
                    header: {title: "System Name", property: 'ditpr'},
                    property: 'ditpr',
                    type: 'string',
                    callback: function (td, item, value) {
                        var d = dCrt('div').addClass('ellipse-it');
                        var v = TableFactory.linkStatus[item.ditprId];
                        if (!v) {
                            TableFactory.linkStatus[item.ditprId] = "none";
                        }
                        d.pAuthorizedLink({
                            lCache: TableFactory.linkStatus,
                            view: "ditpr",
                            vertexUri: item.ditprId,
                            linkAttributes: [{id: "title", value: value},
                                {id: "href", value: item.ditprId + "?et_view=ditpr"}, {id: "target", value: "_blank"}],
                            linkHtml: value,
                            schema: {}
                        });
                        td.append(d);
                    }
                },
                {
                    header: {title: "Location", property: 'location'},
                    property: 'location',
                    type: 'string',
                    callback: function (td, item, value) {
                        if (item.location) {
                            var d = dCrt('div').addClass('ellipse-it');
                            var v = TableFactory.linkStatus[item.locationId];
                            if (!v) {
                                TableFactory.linkStatus[item.locationId] = "none";
                            }
                            d.pAuthorizedLink({
                                lCache: TableFactory.linkStatus,
                                view: "loc",
                                vertexUri: item.locationId,
                                linkAttributes: [{id: "title", value: value},
                                    {id: "href", value: item.locationId + "?et_view=loc"}, {
                                        id: "target",
                                        value: "_blank"
                                    }],
                                linkHtml: item.location,
                                schema: {}
                            });
                            td.append(d);
                        }
                    }
                },
                {
                    header: {title: "Managed By", property: 'managed'},
                    property: 'managed',
                    type: 'string',
                    callback: function (td, item, value) {
                        if (item.managed) {
                            var d = dCrt('div').addClass('ellipse-it');
                            var v = TableFactory.linkStatus[item.managedId];
                            if (!v) {
                                TableFactory.linkStatus[item.managedId] = "none";
                            }
                            d.pAuthorizedLink({
                                lCache: TableFactory.linkStatus,
                                view: "managed",
                                vertexUri: item.managedId,
                                linkAttributes: [{id: "title", value: value},
                                    {id: "href", value: item.managedId + "?et_view=managed"}, {
                                        id: "target",
                                        value: "_blank"
                                    }],
                                linkHtml: item.managed,
                                schema: {}
                            });
                            td.append(d);
                        }
                    }
                },
                {
                    header: {title: "Owned By", property: 'owned'},
                    property: 'owned',
                    type: 'string',
                    callback: function (td, item, value) {
                        if (item.owned) {
                            var d = dCrt('div').addClass('ellipse-it');
                            var v = TableFactory.linkStatus[item.ownedId];
                            if (!v) {
                                TableFactory.linkStatus[item.ownedId] = "none";
                            }
                            d.pAuthorizedLink({
                                lCache: TableFactory.linkStatus,
                                view: "owned",
                                vertexUri: item.ownedId,
                                linkAttributes: [{id: "title", value: value},
                                    {id: "href", value: item.ownedId + "?et_view=owned"}, {
                                        id: "target",
                                        value: "_blank"
                                    }],
                                linkHtml: item.owned,
                                schema: {}
                            });
                            td.append(d);
                        }
                    }
                }
            ]
        };
    }
};