/**
 * @class CMDBuild.WidgetBuilders.DateAttribute
 * @extends CMDBuild.WidgetBuilders.RangeQueryAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders"); 
CMDBuild.WidgetBuilders.DateAttribute = function() {
	this.format = 'd/m/y';
	this.fieldWidth = 200;
	this.headerWidth = 60;
};
CMDBuild.extend(CMDBuild.WidgetBuilders.DateAttribute, CMDBuild.WidgetBuilders.RangeQueryAttribute);
/**
 * @override
 * @return object
 */
CMDBuild.WidgetBuilders.DateAttribute.prototype.buildGridHeader = function(attribute) {	
	return {
		header: attribute.description,
		sortable : true,
		dataIndex : attribute.name,
		hidden: !attribute.isbasedsp,
		fixed: false,
		width: this.headerWidth,
		flex: 1,
		//TODO read the format in the config
		format: this.format
	};
};
/**
 * @override
 * @return Ext.form.DateField
 */
CMDBuild.WidgetBuilders.DateAttribute.prototype.buildAttributeField = function(attribute) {
	return new Ext.form.DateField({
		labelAlign: "right",
		fieldLabel: attribute.description,
		name: attribute.name,
		allowBlank: !attribute.isnotnull,
		format: this.format,//TODO read the format in the config
		width: this.fieldWidth,
		CMAttribute: attribute
	});
};