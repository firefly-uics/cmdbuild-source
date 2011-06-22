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
		dataIndex : attribute.name+"_value",
		hidden: !attribute.isbasedsp,
		fixed: false,
		width: 60,
		flex: 1
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