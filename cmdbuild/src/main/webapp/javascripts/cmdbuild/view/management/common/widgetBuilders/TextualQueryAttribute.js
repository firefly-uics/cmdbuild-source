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
	return [
        ['like',translation.equals],
		['different',translation.different],
		['contains',translation.like],
		['dontcontains',translation.dontlike],
		['begin',translation.begin],
		['dontbegin',translation.dontbegin],
		['end',translation.end],
		['dontend',translation.dontend],
		['null',translation.nullo]
	];
};
/**
 * @override
 */
CMDBuild.WidgetBuilders.TextualQueryAttribute.prototype.needsDoubleFielForQuery = function() {
	return false;
};