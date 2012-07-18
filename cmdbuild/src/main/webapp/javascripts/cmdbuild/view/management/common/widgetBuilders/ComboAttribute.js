/**
 * @class CMDBuild.WidgetBuilders.ComboAttribute
 * @extends CMDBuild.WidgetBuilders.SimpleQueryAttribute
 */
CMDBuild.WidgetBuilders.ComboAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.ComboAttribute, CMDBuild.WidgetBuilders.SimpleQueryAttribute);

/**
 * @override
 * @return object
 */
CMDBuild.WidgetBuilders.ComboAttribute.prototype.buildGridHeader = function(attribute) {
	return {
		header : attribute.description,
		sortable : true,
		dataIndex : attribute.name,
		hidden: !attribute.isbasedsp,
		flex: 60
	};
};
/**
 * @override
 * @return Ext.form.DisplayField
 */
CMDBuild.WidgetBuilders.ComboAttribute.prototype.buildReadOnlyField = function(attribute) {
	var attr = Ext.apply({}, attribute);
	attr.name = attribute.name+"_value";
	
	return CMDBuild.WidgetBuilders.ComboAttribute.superclass.buildReadOnlyField(attr);
};
/**
 * @override
 * @return string
 */
CMDBuild.WidgetBuilders.ComboAttribute.prototype.getDisplayNameForAttr = function(attribute) {
	return attribute.name+"_value";
};
/**
 * @override
 */
CMDBuild.WidgetBuilders.ComboAttribute.prototype.getQueryOptions = function() {
	return [
		['equals',translation.equals],
		['null',translation.nullo],
		['notnull',translation.notnull],
		['different',translation.different]
	];
};