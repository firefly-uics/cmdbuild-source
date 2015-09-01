(function () {

	Ext.define('CMDBuild.core.fieldManager.builders.Double', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @param {Boolean} withEditor
		 *
		 * @returns {Ext.grid.column.Column}
		 *
		 * NOTE: cannot implement Ext.grid.column.Number because don't recognize not anglosaxon number formats
		 */
		buildColumn: function(withEditor) {
			withEditor = Ext.isBoolean(withEditor) ? withEditor : false;

			return Ext.create('Ext.grid.column.Column', {
				dataIndex: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				editor: withEditor ? this.buildEditor() : null,
				flex: 1,
				sortable: true,
				text: this.applyMandatoryLabelFlag(this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.DESCRIPTION)),
				width: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME).length * 9
			});
		},

		/**
		 * @returns {Object}
		 */
		buildEditor: function() {
			return {
				xtype: 'numberfield',
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				hideTrigger: true, // Hides selecting arrows
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				vtype: 'numeric'
			};
		},

		/**
		 * @returns {Ext.form.field.Number}
		 */
		buildField: function() {
			return Ext.create('Ext.form.field.Number', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.DESCRIPTION)
					|| this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME)
				),
				hideTrigger: true, // Hides selecting arrows
				labelAlign: 'right',
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxWidth: CMDBuild.SMALL_FIELD_WIDTH,
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				vtype: 'numeric'
			});
		},

		/**
		 * @returns {Object}
		 */
		buildStoreField: function() {
			return { name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME), type: 'float', useNull: true };
		}
	});

})();