(function () {

	Ext.define('CMDBuild.core.fieldManager.builders.Boolean', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @param {Boolean} withEditor
		 *
		 * @returns {Ext.grid.column.CheckColumn or Object}
		 */
		buildColumn: function(withEditor) {
			withEditor = Ext.isBoolean(withEditor) ? withEditor : false;

			return this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.HIDDEN) ? {} : Ext.create('Ext.grid.column.CheckColumn', {
				dataIndex: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				flex: 1,
				hidden: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.SHOW_COLUMN),
				renderer: this.rendererColumn,
				scope: this,
				sortable: true,
				text: this.applyMandatoryLabelFlag(this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION)),
				width: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME).length * 9
			});
		},

		/**
		 * @returns {Ext.form.field.Checkbox}
		 */
		buildField: function() {
			return Ext.create('Ext.form.field.Checkbox', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION)
					|| this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME)
				),
				hidden: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.HIDDEN),
				inputValue: true,
				labelAlign: 'right',
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				uncheckedValue: false
			});
		},

		/**
		 * @returns {Object}
		 */
		buildStoreField: function() {
			return { name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME), type: 'boolean' };
		}
	});

})();