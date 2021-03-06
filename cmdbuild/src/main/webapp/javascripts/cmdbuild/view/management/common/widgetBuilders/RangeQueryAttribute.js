/**
 * @class CMDBuild.WidgetBuilders.RangeQueryAttribute
 * @extends CMDBuild.WidgetBuilders.BaseAttribute
 *
 */
Ext.ns("CMDBuild.WidgetBuilders");
CMDBuild.WidgetBuilders.RangeQueryAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.RangeQueryAttribute, CMDBuild.WidgetBuilders.BaseAttribute);

/**
 * @override
 */
CMDBuild.WidgetBuilders.RangeQueryAttribute.prototype.getQueryOptions = function() {
	var operator = CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator;
	return [
		[operator.EQUAL, CMDBuild.Translation.equals],
		[operator.NULL, CMDBuild.Translation.isNull],
		[operator.NOT_NULL, CMDBuild.Translation.isNotNull],
		[operator.NOT_EQUAL, CMDBuild.Translation.different],
		[operator.GREATER_THAN, CMDBuild.Translation.greaterThan],
		[operator.LESS_THAN, CMDBuild.Translation.lessThan],
		[operator.BETWEEN, CMDBuild.Translation.between]
	];
};