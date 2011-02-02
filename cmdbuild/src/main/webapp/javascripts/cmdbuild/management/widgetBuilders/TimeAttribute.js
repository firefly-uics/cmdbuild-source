/**
 * @class CMDBuild.WidgetBuilders.TimeAttribute
 * @extends CMDBuild.WidgetBuilders.StringAttribute
 */
CMDBuild.WidgetBuilders.TimeAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.TimeAttribute, CMDBuild.WidgetBuilders.DateAttribute);
CMDBuild.WidgetBuilders.TimeAttribute.prototype.format = "H:i:s";
/**
 * @override
 * @param attribute
 * @return
 */
CMDBuild.WidgetBuilders.TimeAttribute.prototype.buildAttributeField = function(attribute) {
	return new Ext.form.TextField({
		fieldLabel: attribute.description,
		name: attribute.name,
		allowBlank: !attribute.isnotnull,
		width:100,
		format: this.format,
		vtype: "time"
	});	
};