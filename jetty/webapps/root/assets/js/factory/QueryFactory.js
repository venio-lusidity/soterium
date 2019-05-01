var QueryFactory = {
    matchAll: function (domain, type) {
        return {
            domain: domain,
            type: (undefined === type || null === type) ? domain : type,
            "native": {query: {"match_all": {}}}
        }
    },
    ditpr: {
        root: function (data) {
            return {
                domain: "/electronic/system/enclave/virtual_enclave",
                "native": {query: {filtered: {filter: {bool: {must: [{term: {"title.raw": "Enterprise"}}]}}}}}
            };
        },
        children: function (data) {
            if (null !== data) {
                return {
                    domain: '/object/edge/infrastructure_edge',
                    type: data.vertexType,
                    lid: data.lid,
                    sort: {on: '/object/endpoint/endpointFrom.ordinal', direction: 'desc'},
                    totals: false,
                    "native": {
                        query: {
                            filtered: {
                                filter: {
                                    bool: {
                                        must: [
                                            {term: {'/object/endpoint/endpointFrom.relatedId.raw': data.lid}},
                                            {term: {'label.raw': '/electronic/base_infrastructure/infrastructures'}}
                                        ],
                                        should: [
                                            {'term': {'/object/endpoint/endpointTo.relatedType.raw': '/electronic/system/enclave/system_enclave'}},
                                            {'term': {'/object/endpoint/endpointTo.relatedType.raw': '/electronic/system/enclave/network_enclave'}},
                                            {'term': {'/object/endpoint/endpointTo.relatedType.raw': '/electronic/system/enclave/virtual_enclave'}}
                                        ]
                                    }
                                }
                            }
                        }
                    }
                };
            }
        }
    },
    rootOrganizations: function (data) {
        return {
            domain: '/organization/organization',
            "native": {query: {filtered: {filter: {bool: {should: [{term: {"title.raw": "Root Organizations"}}, {term: {"title.raw": "Defense Information Systems Agency"}}]}}}}}
        };
    },
    rootOrganization: function (data, et_view) {
        var r = {
            domain: '/organization/organization',
            "native": {query: {filtered: {filter: {bool: {must: [{term: {"title.raw": "Root Organizations"}}]}}}}}
        };
        if(et_view){
            r.params = [{key: "et_view", value: et_view}];
        }
        return r;
    },
    rootDisa: function (data) {
        return {
            domain: '/organization/organization',
            "native": {query: {filtered: {filter: {bool: {must: [{term: {"title.raw": "Defense Information Systems Agency"}}]}}}}}
        };
    },
    childOrganizations: function (data, et_view) {
        var r = {
            domain: '/object/edge/organization_edge',
            type: '/organization/organization',
            lid: data.lid,
            "native": {
                query: {
                    filtered: {
                        filter: {
                            bool: {
                                must: [
                                    {term: {'/object/endpoint/endpointFrom.relatedId.raw': data.lid}},
                                    {term: {'label.raw': '/organization/organization/organizations'}}
                                ]
                            }
                        }
                    }
                }
            }
        };
        if(et_view){
            r.params = [{key: "et_view", value: et_view}];
        }
        return r;
    },
    rootLocation: function (data) {
        return {
            domain: '/location/location',
            "native": {query: {filtered: {filter: {bool: {should: [{term: {"title.raw": "Locations"}}]}}}}}
        };
    },
    childLocations: function (data) {
        return {
            asFilterable: true,
            domain: '/object/edge/location_edge',
            type: '/location/location',
            lid: data.lid,
            "native": {
                query: {
                    filtered: {
                        filter: {
                            bool: {
                                must: [
                                    {term: {'/object/endpoint/endpointFrom.relatedId.raw': data.lid}},
                                    {term: {'label.raw': '/location/location/places'}}
                                ]
                            }
                        }
                    }
                }
            }
        };
    },
    getOtherEnd: function (edges, key /*fromEndpoint or toEndpoint*/, callback /* function to pass the data back to */) {
        var r;
        if (!($.jCommon.string.equals(key, "fromEndpoint") || $.jCommon.string.equals(key, "toEndpoint"))) {
            throw "key must be fromEndpoint or toEndpoint.";
        }
        if (!$.isFunction(callback)) {
            throw "callback must be a function";
        }
        if (edges && edges.results) {
            r = $.extend({}, edges);
        }
        else if ($.jCommon.is.array(edges)) {
            r = {hits: edges.length, next: 0, limit: 0};
        }
        else {
            r = {hits: 0, next: 0, limit: 0};
        }
        r.results = [];
        var urls = [];
        var v = ((($.jCommon.is.array(edges)) ? edges : (edges.results)) ? edges.results : []);
        $.each(v, function () {
            var item = this;
            var j = JSON.parse(item[key]);
            var url = "/domains/" + j.classKey + "/" + j.id;
            urls.push(url);
        });

        var on = 0;
        var t = urls.length;
        var s = function (data) {
            if (data) {
                r.results.push(data);
            }
            on++;
            if (on >= t) {
                callback(r);
            }
        };
        $.each(urls, function () {
            $.htmlEngine.request(this, s, s, null, 'get');
        });
    },
    deviceToEnclave: function (data) {
        return {
            domain: "/object/edge/infrastructure_edge",
            type: null,
            lid: data.lid,
            sort: {on: "title", direction: "asc"},
            "native": {
                query: {
                    filtered: {
                        filter: {
                            bool: {
                                must: [
                                    {term: {"/object/endpoint/endpointTo.relatedId.raw": data.lid}},
                                    {term: {"label.raw": "/electronic/base_infrastructure/infrastructures"}}
                                ]
                            }
                        }
                    }
                }
            }
        };
    },
    stigRuleToStigGroup: function (data) {
        return {
            domain: "/object/edge",
            type: "/technology/security/vulnerabilities/stig/release/group/stig_group",
            lid: data.lid,
            sort: {on: "title", direction: "asc"},
            "native": {
                query: {
                    filtered: {
                        filter: {
                            bool: {
                                must: [
                                    {term: {"/object/endpoint/endpointTo.relatedId.raw": data.lid}},
                                    {term: {"label.raw": "/technology/security/vulnerabilities/stig/release/group/rule/stig_rule/rules"}}
                                ]
                            }
                        }
                    }
                }
            }
        };
    },
    softwareToDevices: function (data) {
        return {
            domain: "/object/edge/software_edge",
            type: null,
            lid: data.lid,
            sort: {on: "/object/endpoint/endpointFrom.label.folded", direction: "asc"},
            "native": {
                query: {
                    filtered: {
                        filter: {
                            bool: {
                                must: [
                                    {term: {"/object/endpoint/endpointTo.relatedId.raw": data.lid}}
                                ]
                            }
                        }
                    }
                }
            }
        };
    },
    policyToDevices: function (data) {
        return {
            domain: "/object/edge",
            type: "/electronic/network/asset",
            lid: data.lid,
            sort: {on: "title", direction: "asc"},
            "native": {
                query: {
                    filtered: {
                        filter: {
                            bool: {
                                must: [
                                    {term: {"/object/endpoint/endpointTo.relatedId.raw": data.lid}},
                                    {term: {"label.raw": "/technology/software_compliance_policy/compliance"}}
                                ]
                            }
                        }
                    }
                }
            }
        };
    },
    pluginToIavms: function (data) {
        return {
            domain: "/object/edge",
            type: "/technology/security/vulnerabilities/acas/security_center_plugin",
            lid: data.lid,
            sort: {on: "title", direction: "asc"},
            "native": {
                query: {
                    filtered: {
                        filter: {
                            bool: {
                                must: [
                                    {term: {"/object/endpoint/endpointFrom.relatedId.raw": data.lid}},
                                    {term: {"label.raw": "/technology/security/iavm/iavms"}}
                                ]
                            }
                        }
                    }
                }
            }
        };
    },
    pluginToDevices: function (data) {
        return {
            domain: "/object/edge/vulnerability_edge",
            type: null,
            lid: data.lid,
            sort: {on: "title", direction: "asc"},
            "native": {
                query: {
                    filtered: {
                        filter: {
                            bool: {
                                must: [
                                    {term: {"/object/endpoint/endpointTo.relatedId.raw": data.lid}},
                                    {term: {"result": "failed"}},
                                    {term: {"label.raw": "/technology/security/vulnerabilities/base_vulnerability/vulnerabilities"}}
                                ]
                            }
                        }
                    }
                }
            }
        };
    },
    stigCatI: function (data) {
        return QueryFactory.stigCat(data, 10);
    },
    stigCatII: function (data) {
        return QueryFactory.stigCat(data, 5);
    },
    stigCatIII: function (data) {
        return QueryFactory.stigCat(data, 1);
    },
    stigCat: function (data, min) {
        return {
            domain: '/object/edge/vulnerability_edge',
            type: data.vertexType,
            lid: data.lid,
            sort: {on: 'ordinal', direction: 'desc'},
            totals: false,
            "native": {
                query: {
                    filtered: {
                        filter: {
                            bool: {
                                range: {
                                    "/object/endpoint/endpointFrom.ordinal": {
                                        "gte": min
                                    }
                                },
                                must: [
                                    {term: {'/object/endpoint/endpointFrom.relatedId.raw': data.lid}},
                                    {term: {'label.raw': '/technology/security/vulnerabilities/base_vulnerability/vulnerabilities'}},
                                    {term: {'/object/endpoint/endpointTo.relatedType.raw': '/technology/security/vulnerabilities/stig/release/group/rule/stig_rule'}}
                                ]
                            }
                        }
                    }
                }
            }
        };
    },
    enclaveToDevices: function (data) {
        return data["/vertex/uri"] + '/hierarchy/details?detail=asset&view=ditpr&filter=all';
    },
    vulnToDevices: function (data) {
        return data["/vertex/uri"] + '/vulnerability/devices';
    },
    groupsToPrincipals: function (data) {
        return {
            domain: "/object/edge/principal_edge",
            type: "/acs/security/authorization/group",
            lid: data.lid,
            sort: {on: "title", direction: "asc"},
            "native": {
                query: {
                    filtered: {
                        filter: {
                            bool: {
                                must: [
                                    {term: {"/object/endpoint/endpointFrom.relatedId.raw": data.lid}},
                                    {term: {"label.raw": "/acs/security/base_principal/principals"}}
                                ]
                            }
                        }
                    }
                }
            }
        };
    },
    positionsToPrincipals: function (data) {
        return {
            domain: "/object/edge/principal_edge",
            type: "/organization/personnel_position",
            lid: data.lid,
            sort: {on: "title", direction: "asc"},
            "native": {
                query: {
                    filtered: {
                        filter: {
                            bool: {
                                must: [
                                    {term: {"/object/endpoint/endpointFrom.relatedId.raw": data.lid}},
                                    {term: {"label.raw": "/acs/security/base_principal/principals"}}
                                ]
                            }
                        }
                    }
                }
            }
        };
    },
    PrincipalsToPositions: function (data) {
        return {
            domain: "/object/edge/principal_edge",
            type: "/organization/personnel_position",
            lid: data.lid,
            sort: {on: "title", direction: "asc"},
            "native": {
                query: {
                    filtered: {
                        filter: {
                            bool: {
                                must: [
                                    {term: {"/object/endpoint/endpointTo.relatedId.raw": data.lid}},
                                    {term: {"label.raw": "/acs/security/base_principal/principals"}}
                                ]
                            }
                        }
                    }
                }
            }
        };
    },
    PrincipalsToIdentities: function (data) {
        return {
            domain: "/object/edge",
            type: "/people/person",
            lid: data.lid,
            sort: {on: "title", direction: "asc"},
            "native": {
                query: {
                    filtered: {
                        filter: {
                            bool: {
                                must: [
                                    {term: {"/object/endpoint/endpointFrom.relatedId.raw": data.lid}},
                                    {term: {"label.raw": "/acs/security/identity/identities"}}
                                ]
                            }
                        }
                    }
                }
            }
        };
    },
    AuthToPrincipals: function (data) {
        return {
            domain: "/object/edge/authorized_edge",
            lid: data.lid,
            sort: {on: "title", direction: "asc"},
            "native": {
                query: {
                    filtered: {
                        filter: {
                            bool: {
                                must: [
                                    {term: {"/object/endpoint/endpointTo.relatedId.raw": data.lid}},
                                    {term: {"label.raw": "/acs/security/base_principal/authorized"}}
                                ]
                            }
                        }
                    }
                }
            }
        };
    },
    assetSources: function (data) {
        if (null !== data) {
            return {
                domain: data.vertexType,
                type: data.vertexType,
                lid: null,
                sort: {on: 'createdWhen', direction: 'desc'},
                "native": {
                    query: {
                        filtered: {
                            filter: {
                                bool: {
                                    must: [
                                        {term: {'type.raw': data.type}}
                                    ]
                                }
                            }
                        }
                    }
                }
            };
        }
    },
    getImporterLastSeen: function (data) {
        return {
            domain: '/object/edge/property_match_edge',
            type: '/electronic/network/asset',
            sort: {on: "createdWhen", direction: "desc"},
            lid: data.lid,
            "native": {
                query: {
                    filtered: {
                        filter: {
                            bool: {
                                must: [
                                    {term: {"/object/endpoint/endpointFrom.relatedId.raw": data.lid}}
                                ]
                            }
                        }
                    }
                }
            }
        };
    }
};