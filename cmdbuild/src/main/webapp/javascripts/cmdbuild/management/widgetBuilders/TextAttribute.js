/**
 * @class CMDBuild.WidgetBuilders.TextAttribute
 * @extends CMDBuild.WidgetBuilders.StringAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders"); 
CMDBuild.WidgetBuilders.TextAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.TextAttribute, CMDBuild.WidgetBuilders.StringAttribute);
/**
 * @override
 * @param attribute
 * @return Ext.form.TextArea
 */
CMDBuild.WidgetBuilders.TextAttribute.prototype.buildAttributeField = function(attribute) {
	var attr = Ext.apply({},attribute);
	attr.len = this.MAXWIDTH + 1;
	return CMDBuild.WidgetBuilders.TextAttribute.superclass.buildAttributeField(attr);
};