;var FnFactory = {
    process: function (cmds, data) {
        // expects fn::function_name::other_values
        if(cmds) {
            var parts = cmds.split("::");
            if (parts && parts.length > 1) {
                var cmd = parts[1];
                return FnFactory[cmd](parts, data);
            }
        }
    },
    getSeverityLabel: function (value, vertexType) {
        var v = $.jCommon.string.toTitleCase(value);
        if(!$.jCommon.string.empty(value) && ($.jCommon.string.contains(vertexType, 'stig', true) || $.jCommon.string.contains(value, 'cat', true))){
            value = value.toLowerCase();
            switch(value){
                case 'high':
                case 'cat i':
                case 'cat_i':
                    v = 'CAT I';
                    break;
                case 'cat ii':
                case 'cat_ii':
                case 'medium':
                    v = 'CAT II';
                    break;
                case 'cat iii':
                case 'cat_iii':
                case 'low':
                    v = 'CAT III';
                    break;
            }
        }
        return v;
    },
    toAcronym: function (val) {
        var r = val;
        if($.jCommon.string.contains(r, 'disa', true)){
            r = $.jCommon.string.replaceAll(r, 'disa', 'DISA');
        }
        if($.jCommon.string.contains(r, 'disa oob', true)){
            r = $.jCommon.string.replaceAll(r, 'oob', 'OOB');
        }
        if($.jCommon.string.contains(r, 'disa prod', true)){
            r = $.jCommon.string.replaceAll(r, 'prod', 'PROD');
        }
        if($.jCommon.string.contains(r, 'sc ', true)){
            r = $.jCommon.string.replaceAll(r, 'sc', 'SC');
        }
        if($.jCommon.string.contains(r, 'acas', true)){
            r = $.jCommon.string.replaceAll(r, 'acas', 'ACAS');
        }
        if($.jCommon.string.contains(r, 'hbss', true)){
            r = $.jCommon.string.replaceAll(r, 'hbss', 'HBSS');
        }
        if($.jCommon.string.contains(r, 'stig', true)){
            r = $.jCommon.string.replaceAll(r, 'stig', 'STIG');
        }
        if($.jCommon.string.contains(r, 'iavm', true)){
            r = $.jCommon.string.replaceAll(r, 'iavm', 'IAVM');
        }
        if($.jCommon.string.contains(r, 'cve', true)){
            r = $.jCommon.string.replaceAll(r, 'cve', 'CVE');
        }
        if($.jCommon.string.contains(r, 'cpe', true)){
            r = $.jCommon.string.replaceAll(r, 'cpe', 'CPE');
        }
        if($.jCommon.string.contains(r, 'ops', true)){
            r = $.jCommon.string.replaceAll(r, 'ops', 'OPS');
        }
        if($.jCommon.string.contains(r, 'api', true)){
            r = $.jCommon.string.replaceAll(r, 'api', 'API');
        }
        if($.jCommon.string.contains(r, 'conus', true)){
            r = $.jCommon.string.replaceAll(r, 'conus', 'CONUS');
        }
        if($.jCommon.string.endsWith(r, 'pac', true)){
            r = r.substring(0, r.length-3) + "PAC";
        }
        if($.jCommon.string.endsWith(r, 'eur', true)){
            r = r.substring(0, r.length-3) + "EUR";
        }
        if($.jCommon.string.endsWith(r, 'jitc', true)){
            r = r.substring(0, r.length-4) + "JITC";
        }
        if($.jCommon.string.startsWith(r, 'sc ', true)){
            r = "SC" + r.substring(2);
        }
        return r;
    },
    classTypeToName: function (val) {
        var t = $.jCommon.string.getLast(val, $.jCommon.string.contains(val, "/") ? "/" : $.jCommon.string.contains(val, ":") ? ":" : ".");
        t = $.jCommon.string.spaceAtCapitol(t);
        t = $.jCommon.string.replaceAll(t, "_", " ");
        t = $.jCommon.string.toTitleCase(t);
        if($.jCommon.string.equals(t, 'acas invalid asset', true)){
            t = "Unmatched ACAS Only Asset";
        }
        return t ? FnFactory.toAcronym(t) : t;
    },
    classToName: function (parts, data) {
        var r = null;
        // expects fn::function_name::key_to_value_of_class
        if(parts.length>2){
            var key = parts[2];
            var value = data[key];
            if(value){
                r = FnFactory.classTypeToName(value);
            }
        }
        return r ? FnFactory.toAcronym(r) : r;
    },
    toDefaultDate: function (parts, data) {
        var r = null;
        // expects fn::function_name::key_to_value_of_class
        if (parts.length > 2) {
            var key = parts[2];
            var value = data[key];
            if (value) {
                r = $.jCommon.dateTime.defaultFormat(value);
            }
        }
        return r;
    },
    toTitleCase: function (str) {
        var r = str;
        if($.jCommon.string.contains(str, "importer", true)){
            r = $.jCommon.string.toTitleCase($.jCommon.string.spaceAtCapitol(str));
        }
        return FnFactory.toAcronym(r);
    },
    propertyToName: function (parts, data) {
        var r = null;
        // expects fn::function_name::key_to_value_of_class
        if (parts.length > 2) {
            var key = parts[2];
            var value = data[key];
            if (value) {
                switch (value) {
                    default:
                        r = $.jCommon.string.getLast(value, "/");
                        r = $.jCommon.string.spaceIt(r);
                        r = $.jCommon.string.toTitleCase(r);
                        break;
                }
            }
        }
        return r;
    }
};