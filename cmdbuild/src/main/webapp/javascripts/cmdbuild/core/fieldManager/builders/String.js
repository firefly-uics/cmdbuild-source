(function () {

	/**
	 * Specific field attributes:
	 * 		- {Number} length: max number digits
	 */
	Ext.define('CMDBuild.core.fieldManager.builders.String', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @param {Boolean} withEditor
		 *
		 * @returns {Ext.grid.column.Column}
		 */
		buildColumn: function(withEditor) {
			withEditor = Ext.isBoolean(withEditor) ? withEditor : false;

			return Ext.create('Ext.grid.column.Column', {
				dataIndex: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				editor: withEditor ? this.buildEditor() : null,
				flex: 1,
				sortable: true,
				text: this.applyMandatoryLabelFlag(this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION))
			});
		},

		/**
		 * @returns {Object}
		 */
		buildEditor: function() {
			return {
				xtype: 'textfield',
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				enforceMaxLength: true,
				maxLength: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.LENGTH),
				name: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE)
			};
		},

		/**
		 * @returns {Ext.form.field.Text}
		 */
		buildField: function() {
			return Ext.create('Ext.form.field.Text', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				enforceMaxLength: true,
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
					|| this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME)
				),
				labelAlign: 'right',
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxLength: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.LENGTH),
				maxWidth: CMDBuild.BIG_FIELD_WIDTH,
				name: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE)
			});
		}
	});

})();