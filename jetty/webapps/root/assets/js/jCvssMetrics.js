; (function ($) {
    /*
}
    //Object Instance
    {
        "veryLow":{
        "start": 1,
            "end": 3
    },
        "low":{
        "start": 4,
            "end": 12
    },
        "medium":{
        "start": 13,
            "end": 19
    },
        "high":{
        "start": 20,
            "end": 22
    },
        "critical":{
        "start": 23,
            "end": 25
    }
    }    */
    $.cvssMetrics = {
        color:{
            getColor: function(score){
                score = Math.floor(Math.round((score/2)));
                var c;
                switch (score){
                    default:
                    case 5:
                        c = "critical";
                        break;
                    case 4:
                        c = "high";
                        break;
                    case 3:
                        c = "medium";
                        break;
                    case 2:
                        c= "low";
                        break;
                    case 1:
                        c= "veryLow";
                        break;
                    case 0:
                        c = "grey";
                        break;
                }
                return c;
            },
            getLikelihood: function(metrics){
                if(metrics) {
                    var score = metrics.exploitScore ? metrics.exploitScore : 10;
                    score = (score == 0) ? 2 : score;
                    return $.cvssMetrics.color.getColor(score);
                }
                else{
                    return $.cvssMetrics.color.getColor(0);
                }
            },
            getImpact: function(metrics) {
                if (metrics) {
                    var score = metrics.impactScore ? metrics.impactScore : 10;
                    score = (score == 0) ? 2 : score;
                    return $.cvssMetrics.color.getColor(score);
                }
                else{
                    return $.cvssMetrics.color.getColor(0);
                }
            },
            getAxisColor: function(metrics){
                if(metrics) {
                    return metrics.matrix && metrics.matrix.serverity ? metrics.matrix.severity : $.cvssMetrics.color.getColor(5);
                }
                else{
                    return $.cvssMetrics.color.getColor(5);
                }
            }
        }
    };
})(jQuery);
