var jAssetFactory ={
    totals: function (data, item) {
        var d = $.extend({}, data);
        d.severity = item.key;
        if($.jCommon.string.equals(d.severity, 'unknown', true)){
            d.severity = 'info';
        }
        return {
            url: String.format('{0}/refine/assets', data['/vertex/uri']),
            data: d
        }
    },
    assets: function (data) {
        var q = {
            asFilterable: true,
            domain: 'technology_security_vulnerabilities_vulnerability_details',
            type:'technology_security_vulnerabilities_vulnerability_details',
            sort: [{property: 'packedVulnerabilityMatrix', asc: 'false'},{property: 'title.folded', asc: 'true'}],
            "native": {
                query : {
                    bool: {
                        must: []
                    }
                }
            }
        };
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
        if(data.filters && data.filters.length>0){
            $.each(data.filters, function () {
                var frmt = String.format('"{0}.folded":"{1}"', this.fKey, this.value.toLowerCase());
                var match = '{"match":{'+frmt+'}}';
                must.push(JSON.parse(match));
            });
        }
        return q;
    }
};