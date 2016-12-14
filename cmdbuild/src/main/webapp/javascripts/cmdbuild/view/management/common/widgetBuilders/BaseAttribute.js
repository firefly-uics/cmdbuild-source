(function() {

	/**
	 * @Class CMDBuild.WidgetBuilders.BaseAttribute
	 * Abstract class to define the interface of the CMDBuild attributes
	 **/
	Ext.ns("CMDBuild.WidgetBuilders");
	CMDBuild.WidgetBuilders.BaseAttribute = function () {};

	CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator = {
		EQUAL: "equal",
		NOT_EQUAL: "notequal",
		NULL: "isnull",
		NOT_NULL: "isnotnull",
		GREATER_THAN: "greater",
		LESS_THAN: "less",
		BETWEEN: "between",
		LIKE: "like",
		CONTAIN: "contain",
		NOT_CONTAIN: "notcontain",
		BEGIN: "begin",
		NOT_BEGIN: "notbegin",
		END: "end",
		NOT_END: "notend",

		NET_CONTAINS: "net_contains",
		NET_CONTAINED: "net_contained",
		NET_CONTAINSOREQUAL: "net_containsorequal",
		NET_CONTAINEDOREQUAL: "net_containedorequal",
		NET_RELATION: "net_relation"
	};

	CMDBuild.WidgetBuilders.BaseAttribute.prototype = {
		/**
		 * This template method return a combo-box with the options given
		 * by the getQueryOptions method that must be implemented in the subclasses
		 * @param attribute
		 * @return Ext.form.ComboBox
		 */
		getQueryCombo: function(attribute) {
			var store = new Ext.data.SimpleStore({
				fields: ['id','type'],
				data: this.getQueryOptions()
			});

			return new Ext.form.ComboBox({
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				fieldLabel: attribute.description,
				labelSeparator: "",
				hideLabel: true,
				name: attribute.name + "_ftype",
				queryMode: 'local',
				store: store,
				value: this.getDefaultValueForQueryCombo(),
				valueField: 'id',
				displayField: 'type',
				triggerAction: 'all',
				forceSelection: true,
				allowBlank: true,
				width: 130
			});

		},

		getDefaultValueForQueryCombo: function() {
			return CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator.EQUAL;
		},

		/**
		 * The implementation must return an array to use as data of the store of the query combo
		 * The query combo is the combo-box in the attribute section of the Search window with the
		 * filtering options
		 */
		getQueryOptions: function() {
			throw new Error('not implemented');
		},

		/**
		 * Template method, call the buildAttributeField method that must be implemented in the subclass
		 * @return Ext.form.Field or a subclass in order with the specific attribute
		 */
		buildField: function(attribute, hideLabel) {
			var field = this.buildAttributeField(attribute);
			field.hideLabel = hideLabel;
			return this.markAsRequired(field, attribute);
		},

		buildAttributeField: function() {
			throw new Error('not implemented');
		},

		/**
		 * service function to add an asterisk before the label of a required attribute
		 */
		markAsRequired: function(field, attribute) {
			if (attribute.isnotnull || attribute.fieldmode == "required") {
				field.allowBlank = false;
				if (field.fieldLabel) {
					field.fieldLabel = '* ' + field.fieldLabel;
				}
			}
			return field;
		},

		/**
		 * @return Ext.form.DisplayField
		 */
		buildReadOnlyField: function(attribute) {
			var field = new Ext.form.DisplayField({
				allowBlank: true,
				labelAlign: "right",
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				fieldLabel: attribute.description || attribute.name,
				width: CMDBuild.core.constants.FieldWidths.STANDARD_BIG,
				submitValue: false,
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
		},
		/**
		 * The implementation must return a configuration object for the header of a Ext.GridPanel
		 */
		buildGridHeader: function(attribute) {
			throw new Error('not implemented');
		},
		/***
		 *
		 * @param attribute
		 * @return a Ext.form.field.* used for the attribute in the grid
		 */
		buildCellEditor: function(attribute) {
			return CMDBuild.Management.FieldManager.getFieldForAttr(attribute, readOnly = false);
		}
	};

})();
