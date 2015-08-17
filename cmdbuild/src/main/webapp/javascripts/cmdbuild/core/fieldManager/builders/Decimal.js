(function () {

	/**
	 * Specific field attributes:
	 * 		- {Number} precision: max number digits
	 * 		- {Number} scale: max digits after comma
	 *
	 * Examples:
	 * 		12.345 -> { precision: 5, scale: 3 }
	 */
	Ext.define('CMDBuild.core.fieldManager.builders.Decimal', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

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
				dataIndex: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				editor: withEditor ? this.buildEditor() : null,
				flex: 1,
				sortable: true,
				text: this.applyMandatoryLabelFlag(this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION)),
				width: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME).length * 9
			});
		},

		/**
		 * @returns {Object}
		 */
		buildEditor: function() {
			return {
				xtype: 'numberfield',
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				hideTrigger: true, // Hides selecting arrows
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
				precision: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.PRECISION),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				scale: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.SCALE),
				vtype: 'numeric'
			};
		},

		/**
		 * @returns {Ext.form.field.Number}
		 */
		buildField: function() {
			return Ext.create('Ext.form.field.Number', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION)
					|| this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME)
				),
				hideTrigger: true, // Hides selecting arrows
				labelAlign: 'right',
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxWidth: CMDBuild.SMALL_FIELD_WIDTH,
				name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME),
				precision: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.PRECISION),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.WRITABLE),
				scale: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.SCALE),
				vtype: 'numeric'
			});
		},

		/**
		 * @returns {Object}
		 */
		buildStoreField: function() {
			return { name: this.cmfg('attributeModelGet', CMDBuild.core.proxy.CMProxyConstants.NAME), type: 'float', useNull: true };
		}
	});

})();