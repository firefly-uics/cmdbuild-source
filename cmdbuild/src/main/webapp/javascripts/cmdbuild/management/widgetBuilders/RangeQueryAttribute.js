/**
 * @class CMDBuild.WidgetBuilders.RangeQueryAttribute
 * @extends CMDBuild.WidgetBuilders.BaseAttribute
 * */
var translation = CMDBuild.Translation.management.findfilter;
Ext.ns("CMDBuild.WidgetBuilders");
CMDBuild.WidgetBuilders.RangeQueryAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.RangeQueryAttribute, CMDBuild.WidgetBuilders.BaseAttribute);

/**
 * @override
 */
CMDBuild.WidgetBuilders.RangeQueryAttribute.prototype.getQueryOptions = function() {
	return [
	    ['equals',translation.equals],
		['null',translation.nullo],
		['notnull',translation.notnull],
		['different',translation.different],
		['major',translation.major],
		['minor',translation.minor],
		['between',translation.between]
    ];
};
/**
 * @override
 */
CMDBuild.WidgetBuilders.RangeQueryAttribute.prototype.buildFieldsetForFilter = function(fieldId, field, query, originalFieldName) {
	var field2 = field.cloneConfig(); 
	field2.name += "_end";
	field2.hideLabel = true;
	field2.disable();

	query.on('select', function(query, selection, id) {
		selection = CMDBuild.Utils.getFirstSelection(selection);
		if (selection.data.id === "between") {
			field.enable();
			field2.enable();
		} else if (selection.data.id === 'null' || selection.data.id === 'notnull') {
			field.disable();
			field2.disable();
		} else {
			field.enable();
			field2.disable();
		}
	});

	function selectionNeedsNoValue(selection) {
		selection = CMDBuild.Utils.getFirstSelection(selection);
		return ['null','notnull'].indexOf(selection.data.id) >= 0;
	}

	query.on('select',function(query, selection, id) {
		if (selectionNeedsNoValue(selection)) {
			field.disable();
		} else {
			field.enable();
		}
	});

	return this.genericBuildFieldsetForFilter(fieldId, [field, field2], query, originalFieldName);
};