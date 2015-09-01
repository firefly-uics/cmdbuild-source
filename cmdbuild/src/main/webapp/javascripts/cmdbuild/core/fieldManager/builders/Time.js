(function () {

	Ext.define('CMDBuild.core.fieldManager.builders.Time', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		format: 'H:i:s',

		/**
		 * @cfg {Number}
		 */
		headerWidth: 60,

		/**
		 * @param {Boolean} withEditor
		 *
		 * @returns {Ext.grid.column.Date}
		 */
		buildColumn: function(withEditor) {
			withEditor = Ext.isBoolean(withEditor) ? withEditor : false;

			return Ext.create('Ext.grid.column.Date', {
				dataIndex: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				editor: withEditor ? this.buildEditor() : null,
				flex: 1,
				format: this.format,
				hideTrigger: true, // Hides date picker
				sortable: true,
				text: this.applyMandatoryLabelFlag(this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.DESCRIPTION)),
				width: this.headerWidth
			});
		},

		/**
		 * @returns {Object}
		 */
		buildEditor: function() {
			return {
				xtype: 'datefield',
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				format: this.format,
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				vtype: 'time'
			};
		},

		/**
		 * @returns {Ext.form.field.Date}
		 */
		buildField: function() {
			return Ext.create('Ext.form.field.Date', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.DESCRIPTION)
					|| this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME)
				),
				format: this.format,
				hideTrigger: true, // Hides date picker
				labelAlign: 'right',
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxWidth: CMDBuild.SMALL_FIELD_WIDTH,
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.WRITABLE),
				vtype: 'time'
			});
		},

		/**
		 * @returns {Object}
		 */
		buildStoreField: function() {
			return { name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.Constants.NAME), type: 'date', dateFormat: this.format };
		}
	});

})();