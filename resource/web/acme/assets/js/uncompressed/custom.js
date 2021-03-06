/*
 * Copyright 2018 lusidity inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

$(document).ready(function(){
	
	if($(".taskProgress")) {
	
		$(".taskProgress").each(function(){
			
			var endValue = parseInt($(this).html());
											
			$(this).progressbar({
				value: endValue
			});
			
			$(this).parent().find(".percent").html(endValue + "%");
			
		});
	
	}
	
});

/* ---------- Widget Area Functions ---------- */
function widget_area_functions() {
	
	/* ---------- Just Gage Charts ---------- */
	
	var g1;
	
	setInterval(function() {
	          g1.refresh(getRandomInt(0, 100));
	}, 2500);
	
	var g1 = new JustGage({
	    id: "cpu-usage", 
	    value: getRandomInt(0, 100), 
	    min: 0,
	    max: 100,
	    title: "CPU",
	    label: "Usage",
	    levelColorsGradient: false
	 });
	
	/* ---------- Bar Stats ---------- */
	
	if (retina()) {

		$(".bar-stat > .chart").each(function(){

			var chartColor = $(this).css('color');	

			$(this).sparkline('html', {
			    type: 'bar',
			    height: '80', // Double pixel number for retina display
				barWidth: '10', // Double pixel number for retina display
				barSpacing: '4', // Double pixel number for retina display
			    barColor: chartColor,
			    negBarColor: '#eeeeee'}
			);
			
			if (jQuery.browser.mozilla) {
				$(this).css('MozTransform','scale(0.5,0.5)').css('height','40px;');
				$(this).css('height','40px;').css('margin','-20px -30px -20px 30px');
			} else {
				$(this).css('zoom',0.5);
			}

		});

	} else {

		$(".bar-stat > .chart").each(function(){

			var chartColor = $(this).css('color');

			$(this).sparkline('html', {				
			    type: 'bar',
			    height: '40',
				barWidth: '5',
				barSpacing: '2',
			    barColor: chartColor,
			    negBarColor: '#eeeeee'}
			);

		});

	}
	
}