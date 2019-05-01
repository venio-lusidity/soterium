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

$(document).ready(function() {

    // activate Nestable for list 1
    $('#nestable').nestable({
        group: 1
    })

    // activate Nestable for list 2
    $('#nestable2').nestable({
        group: 1
    })

    $('#nestable3').nestable();

	$('#nestable-menu').on('click', function(e) {
	        var target = $(e.target),
	            action = target.data('action');
	        if (action === 'expand-all') {
	            $('.dd').nestable('expandAll');
	        }
	        if (action === 'collapse-all') {
	            $('.dd').nestable('collapseAll');
	        }
	});
	
	$('#nestable4').nestable();

});