;(function ($) {

    $.tourHelper = {
        tour: {
            load: function (path) {
                $.getJSON(path, function (json) {
                    $.tourHelper.tour.start(json);
                });
            },
            start: function (data) {
                var tour = new Tour({storage: false});
                /*Dynamically adding options has a bug - for now, just add steps */
                /*var tour;
                if(data && data.properties){
                    var props = [];
                    $.each(data.properties, function () {
                        var item = this;
                        props.push(item);
                    });
                    tour = new Tour(props[0]);
                }*/
                if (data && data.steps) {
                    $.each(data.steps, function () {
                        var item = this;
                        var el = item.element;
                        var t = item.title;
                        var c = item.content;
                        tour.addStep({
                            element: el,
                            title: t,
                            content: c
                        });
                    });
                    // Initialize the tour
                    tour.init();
                    // Start the tour
                    tour.start();
                }
            }

        }
    }
})(jQuery);
