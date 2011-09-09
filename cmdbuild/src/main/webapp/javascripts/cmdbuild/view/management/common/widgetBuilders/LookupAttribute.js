/**
 * @class CMDBuild.WidgetBuilders.LookupAttribute
 * @extends CMDBuild.WidgetBuilders.ComboAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders"); 
CMDBuild.WidgetBuilders.LookupAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.LookupAttribute, CMDBuild.WidgetBuilders.ComboAttribute);

/**
 * @override
 * @param attribute
 * @return CMDBuild.Management.LookupCombo
 */
CMDBuild.WidgetBuilders.LookupAttribute.prototype.buildAttributeField = function(attribute) {
	return CMDBuild.Management.LookupCombo.build(attribute);
};

/**
 * @override
 */
CMDBuild.WidgetBuilders.LookupAttribute.prototype.markAsRequired = function(field, attribute) {
	//do nothing because the LookupField class manage this function
	return field;
};