/**
 * @class CMDBuild.WidgetBuilders.DecimalAttribute
 * @extends CMDBuild.WidgetBuilders.RangeQueryAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders");
CMDBuild.WidgetBuilders.DecimalAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.DecimalAttribute, CMDBuild.WidgetBuilders.RangeQueryAttribute);
CMDBuild.WidgetBuilders.DecimalAttribute.prototype.MAXWIDTH = 100;
CMDBuild.WidgetBuilders.DecimalAttribute.prototype.customVType = "numeric";
/**
 * @override
 * @param attribute
 * @return object
 */
CMDBuild.WidgetBuilders.DecimalAttribute.prototype.buildGridHeader = function(attribute) {
	return {
		header: attribute.description,
		sortable : true,
		dataIndex : attribute.name,
		hidden: !attribute.isbasedsp,
		flex: this.MAXWIDTH
	};
};
/**
 * @override
 * @param attribute
 * @return Ext.form.field.Number
 */
CMDBuild.WidgetBuilders.DecimalAttribute.prototype.buildAttributeField = function(attribute) {
	return Ext.create('Ext.form.field.Number', {
		labelAlign: "right",
		labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
		fieldLabel: attribute.description || attribute.name,
		decimalPrecision: 20,
		name: attribute.name,
		allowBlank: !attribute.isnotnull,
		width: CMDBuild.core.constants.FieldWidths.LABEL + this.MAXWIDTH,
		scale: attribute.scale,
		precision: attribute.precision,
		decimalPrecision: 20, // Set to max field allowednumber
		hideTrigger: true,
		vtype: this.customVType,
		CMAttribute: attribute
	});
};