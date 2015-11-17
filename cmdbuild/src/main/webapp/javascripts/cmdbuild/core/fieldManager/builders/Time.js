(function () {

	Ext.define('CMDBuild.core.fieldManager.builders.Time', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: [
			'CMDBuild.core.configurations.DataFormat',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Number}
		 */
		headerWidth: 60,

		/**
		 * @param {Boolean} withEditor
		 *
		 * @returns {Ext.grid.column.Date or Object}
		 */
		buildColumn: function(withEditor) {
			withEditor = Ext.isBoolean(withEditor) ? withEditor : false;

			return this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN) ? {} : Ext.create('Ext.grid.column.Date', {
				dataIndex: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				editor: withEditor ? this.buildEditor() : null,
				flex: 1,
				format: CMDBuild.core.configurations.DataFormat.getTime(),
				hidden: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.SHOW_COLUMN),
				hideTrigger: true, // Hides date picker
				renderer: this.rendererColumn,
				scope: this,
				sortable: true,
				text: this.applyMandatoryLabelFlag(this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)),
				width: this.headerWidth
			});
		},

		/**
		 * @returns {Object}
		 */
		buildEditor: function() {
			return this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN) ? {} : {
				xtype: 'datefield',
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				format: CMDBuild.core.configurations.DataFormat.getTime(),
				name: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				vtype: 'time'
			};
		},

		/**
		 * @returns {Ext.form.field.Date}
		 */
		buildField: function() {
			return Ext.create('Ext.form.field.Date', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
					|| this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME)
				),
				format: CMDBuild.core.configurations.DataFormat.getTime(),
				hidden: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN),
				hideTrigger: true, // Hides date picker
				labelAlign: 'right',
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxWidth: CMDBuild.SMALL_FIELD_WIDTH,
				name: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				vtype: 'time'
			});
		},

		/**
		 * @returns {Object}
		 */
		buildStoreField: function() {
			return { name: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME), type: 'date', dateFormat: CMDBuild.core.configurations.DataFormat.getTime() };
		}
	});

})();