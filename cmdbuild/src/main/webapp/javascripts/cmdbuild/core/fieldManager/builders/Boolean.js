(function () {

	Ext.define('CMDBuild.core.fieldManager.builders.Boolean', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @param {Boolean} withEditor
		 *
		 * @returns {Ext.grid.column.CheckColumn}
		 */
		buildColumn: function(withEditor) {
			withEditor = Ext.isBoolean(withEditor) ? withEditor : false;

			return Ext.create('Ext.grid.column.CheckColumn', {
				dataIndex: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				flex: 1,
				sortable: true,
				text: this.applyMandatoryLabelFlag(this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.DESCRIPTION)),
				width: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME).length * 9
			});
		},

		/**
		 * @returns {Ext.form.field.Checkbox}
		 */
		buildField: function() {
			return Ext.create('Ext.form.field.Checkbox', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.DESCRIPTION)
					|| this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME)
				),
				labelAlign: 'right',
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				submitValue: false
			});
		},

		/**
		 * @returns {Object}
		 */
		buildStoreField: function() {
			return { name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME), type: 'boolean' };
		}
	});

})();