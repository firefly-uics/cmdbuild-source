/**
 * @class CMDBuild.WidgetBuilders.BooleanAttribute
 * @extends CMDBuild.WidgetBuilders.SimpleQueryAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders");
CMDBuild.WidgetBuilders.BooleanAttribute = function(){};
CMDBuild.extend(CMDBuild.WidgetBuilders.BooleanAttribute, CMDBuild.WidgetBuilders.SimpleQueryAttribute);
/**
 * @override
 * @return Ext.grid.CheckColumn
 */
CMDBuild.WidgetBuilders.BooleanAttribute.prototype.buildGridHeader = function(attribute) {
	var headerWidth =  attribute.name.length * 9;

	var h = Ext.create('Ext.grid.column.CheckColumn', {
		dataIndex: attribute.name,
		text: attribute.description,
		hidden: !attribute.isbasedsp,
		width: headerWidth,
		sortable: true,
		processEvent: Ext.emptyFn // Makes column readOnly
	});

	if (
		!Ext.isEmpty(attribute)
		&& !Ext.isEmpty(attribute.fieldmode)
		&& attribute.fieldmode == "read"
	) {
		h = Ext.create('Ext.grid.column.CheckColumn', {
			dataIndex: attribute.name,
			text: attribute.description,
			hidden: !attribute.isbasedsp,
			width: headerWidth,
			sortable: true,
			processEvent: Ext.emptyFn // Makes column readOnly
		});
	}

	return h;
};
/**
 * @override
 * @return CMDBuild.view.common.field.display.Boolean
 */
CMDBuild.WidgetBuilders.BooleanAttribute.prototype.buildReadOnlyField = function(attribute) {
	var field = Ext.create('CMDBuild.view.common.field.display.Boolean', {
		labelAlign: "right",
		labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
		fieldLabel: attribute.description,
		name: attribute.name,
		disabled: false,

		/**
		 * Validate also display field
		 *
		 * @override
		 */
		isValid: function() {
			if (this.allowBlank)
				return true;

			return !Ext.isEmpty(this.getValue());
		}
	});

	return this.markAsRequired(field, attribute);
};
/**
 * @override
 * @return Ext.form.field.Checkbox
 */
CMDBuild.WidgetBuilders.BooleanAttribute.prototype.buildAttributeField = function(attribute) {
	return Ext.create('Ext.form.field.Checkbox', {
		labelAlign: "right",
		fieldLabel: attribute.description || attribute.name,
		labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
		uncheckedValue: false,
		inputValue: true,
		name: attribute.name,
		CMAttribute: attribute
	});
};