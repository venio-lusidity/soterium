
function getGroupData(parentId, stateId) {
    return $('#'+stateId).jFilterBar('getGroupData', {parentId: parentId});
}
function tableViewRowAdded(parentId, stateId, e) {
    $('#'+parentId).trigger(e);
}
function tableViewRowsLoaded(parentId, stateId, e) {
    $('#'+parentId).trigger(e);
}
function tableViewLoaded(parentId, stateId, e) {
    $('#'+parentId).trigger(e);
}