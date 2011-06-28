/**
 * @class CMDBuild.WidgetBuilders.TimeAttribute
 * @extends CMDBuild.WidgetBuilders.StringAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders"); 
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
		labelAlign: "right",
		fieldLabel: attribute.description,
		name: attribute.name,
		allowBlank: !attribute.isnotnull,
		format: this.format,
		vtype: "time",
		CMAttribute: attribute
	});	
};