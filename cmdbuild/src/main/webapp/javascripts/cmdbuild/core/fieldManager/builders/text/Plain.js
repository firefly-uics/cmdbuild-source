(function () {

	Ext.define('CMDBuild.core.fieldManager.builders.text.Plain', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @param {Boolean} withEditor
		 *
		 * @returns {Ext.grid.column.Column or Object}
		 */
		buildColumn: function(withEditor) {
			withEditor = Ext.isBoolean(withEditor) ? withEditor : false;

			return this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.HIDDEN) ? {} : Ext.create('Ext.grid.column.Column', {
				dataIndex: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				editor: withEditor ? this.buildEditor() : null,
				flex: 1,
				hidden: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.SHOW_COLUMN),
				sortable: true,
				text: this.applyMandatoryLabelFlag(this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION))
			});
		},

		/**
		 * @returns {Object}
		 */
		buildEditor: function() {
			return this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.HIDDEN) ? {} : {
				xtype: 'textfield',
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE)
			};
		},

		/**
		 * @returns {Ext.form.field.TextArea}
		 */
		buildField: function() {
			return Ext.create('Ext.form.field.TextArea', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION)
					|| this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME)
				),
				hidden: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.HIDDEN),
				labelAlign: 'right',
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxWidth: CMDBuild.BIG_FIELD_WIDTH,
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE)
			});
		}
	});

})();