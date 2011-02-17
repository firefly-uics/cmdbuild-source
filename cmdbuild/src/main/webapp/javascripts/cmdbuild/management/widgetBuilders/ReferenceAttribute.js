/**
 * @class CMDBuild.WidgetBuilders.ReferenceAttribute
 * @extends CMDBuild.WidgetBuilders.ComboAttribute
 */
CMDBuild.WidgetBuilders.ReferenceAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.ReferenceAttribute, CMDBuild.WidgetBuilders.ComboAttribute);

/**
 * @override
 * @param attribute
 * @return CMDBuild.Management.ReferenceCombo
 */
CMDBuild.WidgetBuilders.ReferenceAttribute.prototype.buildAttributeField = function(attribute) {
	return CMDBuild.Management.ReferenceField.build(attribute);
};