(function () {

	Ext.define('CMDBuild.core.fieldManager.builders.text.Plain', {
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
		 */
		buildColumn: function(withEditor) {
			return Ext.create('Ext.grid.column.Column', {
				dataIndex: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				editor: withEditor ? this.buildEditor() : null,
				flex: 1,
				sortable: true,
				text: this.applyMandatoryLabelFlag(this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.DESCRIPTION))
			});
		},

		/**
		 * @returns {Object} editorObject
		 */
		buildEditor: function() {
			return {
				xtype: 'textfield',
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE)
			};
		},

		/**
		 * @returns {Ext.form.field.TextArea} field
		 */
		buildField: function() {
			return Ext.create('Ext.form.field.TextArea', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.DESCRIPTION)
					|| this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME)
				),
				labelAlign: 'right',
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxWidth: CMDBuild.BIG_FIELD_WIDTH,
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE)
			});
		}
	});

})();