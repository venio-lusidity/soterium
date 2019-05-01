var jIavmFactory ={
    totals: function (data, item) {
        return {
            url: String.format('{0}/refine/iavm', data['/vertex/uri']),
            data: data
        }
    },
    iavms : function(opts){
        var q = {
            asFilterable: true,
            domain: 'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
            type:'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
            'native': {
                query : {
                    bool: {
                        must: []
                    }
                }
            },
            sort: [
                {property: 'packedVulnerabilityMatrix', asc: false},
                {property: 'other.folded', asc: true}
            ]
        };
        var must = q['native'].query.bool.must;
        if(opts.data && opts.data.noticeId){
            var frmt = String.format('"{0}.raw":"{1}"', 'noticeId', opts.data.noticeId);
            var match = '{"match":{'+frmt+'}}';
            must.push(JSON.parse(match));
        }
        if(opts.type){
            var frmt = String.format('"{0}.raw":"{1}"', 'result', opts.type);
            var match = '{"match":{'+frmt+'}}';
            must.push(JSON.parse(match));
        }
        if(opts.filters && opts.filters.length>0){
            $.each(opts.filters, function () {
                var frmt = String.format('"{0}.folded":"{1}"', this.fKey, this.value.toLowerCase());
                var match = '{"match":{'+frmt+'}}';
                must.push(JSON.parse(match));
            });
        }
        return q;
    },
    details: function (data, sort) {
        var q = {
            asFilterable: true,
            domain: 'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
            type:'technology_security_vulnerabilities_iavms_iavm_asset_compliance_details',
            'native': {
                query : {
                    bool: {
                        must: [
                            {'match':{'result.raw': 'failed'}}
                        ]
                    }
                }
            },
            sort: [
            {property: 'noticeYear', asc: false},
            {property: 'noticeType.folded', asc: true},
            {property: 'noticeNum', asc: false}
        ]
        };
        q['native'].aggs = {
            agg_result: {
                terms: {
                    size: 0,
                    field: 'noticeId.raw'
                },
                aggs: {
                    only_one_post: {top_hits: {size: '1'}}
                }
            }};
       /*
            'order':{
                _term: 'desc'
            }
        */
        var must = q['native'].query.bool.must;
        var uri = data['/vertex/uri'];
        if($.jCommon.string.contains(uri, "asset")){
            var frmt = String.format('"otherId.raw":"{0}"',uri);
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
    }
};