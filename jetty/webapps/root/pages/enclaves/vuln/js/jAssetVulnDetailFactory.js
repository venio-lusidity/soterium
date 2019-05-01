var jAssetVulnDetailFactory ={
    totals: function (data, item) {
        var d = $.extend({}, data);
        return {
            url: String.format('{0}/refine/vuln', data['/vertex/uri']),
            data: d
        }
    },
    details: function(data, aggOn, sortOn){
        var q = {
            domain: 'technology_security_vulnerabilities_asset_vuln_detail',
            type:'technology_security_vulnerabilities_asset_vuln_detail',
            includeVertex: true,
            asFilterable: true,
            "native": {
                query : {
                    bool: {
                        must: []
                    }
                }
            }
        };
        if(aggOn){
            var fld = sortOn ? sortOn : 'vulnId';
            q.sorted = [{key: "_aggHits", asc: false}, {key: fld, asc: false}];
            q["native"].aggs = {
                agg_result: {
                terms: {
                    size: 0,
                    field: aggOn + ".raw"
                },
                aggs: {
                    only_one_post: {top_hits: {size: "1"}}
                }
            }};
        }
        var must = q["native"].query.bool.must;
        if($.jCommon.string.contains(data.vertexType, "asset")){
            var frmt = String.format('"otherId.raw":"{0}"',data['/vertex/uri']);
            var match = '{"match":{'+frmt+'}}';
            must.push(JSON.parse(match));
        }
        if(!data.et_exact) {
            var frmt = String.format('"{0}.raw":"{1}*"', data.prefixKey, data.prefixTree);
            var match = '{"wildcard":{'+frmt+'}}';
            must.push(JSON.parse(match));
        }
        else{
            var frmt = String.format('"{0}.raw":"{1}"', data.prefixKey, data.prefixTree);
            var match = '{"match":{'+frmt+'}}';
            must.push(JSON.parse(match));
        }
        if(data.filters && data.filters.length>0){
            $.each(data.filters, function () {
                var frmt = String.format('"{0}.folded":"{1}"', this.fKey, this.value.toLowerCase());
                var match = '{"match":{'+frmt+'}}';
                must.push(JSON.parse(match));
            });
        }
        return q;
    },
    aggregation: function (data, aggOn, item, keys) {
        var q = {
            domain: 'technology_security_vulnerabilities_asset_vuln_detail',
            type:'technology_security_vulnerabilities_asset_vuln_detail',
            sorted: [{property: 'published', asc: false}, {property: 'vulnId', asc: true}],
            includeVertex: false,
            asFilterable: true,
            "native": {
                query : {
                    bool: {
                        must: []
                    }
                }
            }
        };
        if(aggOn){
            q["native"].aggs ={agg_result:{terms:{size:0,field: aggs + ".raw"}}}
        }
        var must = q["native"].query.bool.must;
        if(!data.et_exact) {
            var frmt = String.format('"{0}.raw":"{1}*"', data.prefixKey, data.prefixTree);
            var match = '{"wildcard":{'+frmt+'}}';
            must.push(JSON.parse(match));
        }
        else{
            var frmt = String.format('"{0}.raw":"{1}"', data.prefixKey, data.prefixTree);
            var match = '{"match":{'+frmt+'}}';
            must.push(JSON.parse(match));
        }
        if(item){
            $.each(keys, function () {
                var value = item[this];
                if(value) {
                    var frmt = String.format('"{0}.folded":"{1}"', key, value.toLowerCase());
                    var match = '{"match":{' + frmt + '}}';
                    must.push(JSON.parse(match));
                }
            });
        }
        return q;
    }
};