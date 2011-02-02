/**
 * @class CMDBuild.WidgetBuilders.SimpleQueryAttribute
 * @extends CMDBuild.WidgetBuilders.BaseAttribute
 **/
var translation = CMDBuild.Translation.management.findfilter;

CMDBuild.WidgetBuilders.SimpleQueryAttribute = function(){};
CMDBuild.extend(CMDBuild.WidgetBuilders.SimpleQueryAttribute, CMDBuild.WidgetBuilders.BaseAttribute);
/**
 * @override
 */
CMDBuild.WidgetBuilders.SimpleQueryAttribute.prototype.getQueryOptions = function() {
	return [
		['equals',translation.equals],
		['null',translation.nullo],
		['different',translation.different]
	];
};
/**
 * @override
 */
CMDBuild.WidgetBuilders.SimpleQueryAttribute.prototype.needsDoubleFielForQuery = function() {
	return false;
};