var jIavmAssetFactory ={
    totals: function (data, item) {
        return {
            url: String.format('{0}/refine/iavm/assets', data['/vertex/uri']),
            data: data
        }
    },
    details: function (data) {
        var q = {
            asFilterable: true,
            domain: 'technology_security_vulnerabilities_iavms_iavm_asset_unique_details',
            type: 'technology_security_vulnerabilities_iavms_iavm_asset_unique_details',
            'native': {
                query: {
                    bool: {
                        must: [
                            {'match': {'result.raw': 'failed'}}
                        ]
                    }
                }
            },
            sort: [
                {property: 'packedVulnerabilityMatrix', asc: false},
                {property: 'title.folded', asc: true}
            ]
        };
        var must = q['native'].query.bool.must;
        var uri = data['/vertex/uri'];
        if($.jCommon.string.contains(uri, "asset")){
            var frmt = String.format('"relatedId.raw":"{0}"',uri);
            var match = '{"match":{'+frmt+'}}';
            must.push(JSON.parse(match));
        }
        if (!data.et_exact) {
            var frmt = String.format('"{0}.raw":"{1}*"', data.prefixKey, data.prefixTree);
            var match = '{"wildcard":{' + frmt + '}}';
            must.push(JSON.parse(match));
        }
        else {
            var frmt = String.format('"{0}.raw":"{1}"', data.prefixKey, data.prefixTree);
            var match = '{"match":{' + frmt + '}}';
            must.push(JSON.parse(match));
        }
        if (data.filters && data.filters.length > 0) {
            $.each(data.filters, function () {
                var frmt = String.format('"{0}.folded":"{1}"', this.fKey, this.value.toLowerCase());
                var match = '{"match":{' + frmt + '}}';
                must.push(JSON.parse(match));
            });
        }
        return q;
    }
};