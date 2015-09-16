/**
 * @class CMDBuild.WidgetBuilders.TextualQueryAttribute
 * @extends CMDBuild.WidgetBuilders.BaseAttribute
 **/
var translation = CMDBuild.Translation.management.findfilter;
Ext.ns("CMDBuild.WidgetBuilders");
CMDBuild.WidgetBuilders.TextualQueryAttribute = function(){};
CMDBuild.extend(CMDBuild.WidgetBuilders.TextualQueryAttribute, CMDBuild.WidgetBuilders.BaseAttribute);
/**
 * @override
 */
CMDBuild.WidgetBuilders.TextualQueryAttribute.prototype.getQueryOptions = function() {
	var operator = CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator;
	return [
		[operator.BEGIN, translation.begin],
		[operator.CONTAIN, translation.like],
		[operator.END, translation.end],
		[operator.EQUAL, translation.equals],
		[operator.NOT_BEGIN, translation.dontbegin],
		[operator.NOT_CONTAIN, translation.dontlike],
		[operator.NOT_END, translation.dontend],
		[operator.NOT_EQUAL, translation.different],
		[operator.NOT_NULL, translation.notnull],
		[operator.NULL, translation.nullo]
	];
};

CMDBuild.WidgetBuilders.TextualQueryAttribute.prototype.getDefaultValueForQueryCombo = function() {
	return CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator.CONTAIN;
};

/**
 * @override
 */
CMDBuild.WidgetBuilders.TextualQueryAttribute.prototype.needsDoubleFielForQuery = function() {
	return false;
};