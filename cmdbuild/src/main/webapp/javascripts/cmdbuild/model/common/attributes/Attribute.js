(function() {

	Ext.require(['CMDBuild.core.proxy.CMProxyConstants']);

	Ext.define('CMDBuild.model.common.attributes.Attribute', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.EDITOR_TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FILTER, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.HIDDEN, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.LENGTH, type: 'int', defaultValue: 0 },
			{ name: CMDBuild.core.proxy.CMProxyConstants.LOOKUP_TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.MANDATORY, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PRECISION, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SCALE, type: 'int', defaultValue: 0 },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SHOW_COLUMN, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TARGET_CLASS, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TYPE, type: 'string', convert: toLowerCase }, // Case insensitive types
			{ name: CMDBuild.core.proxy.CMProxyConstants.UNIQUE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.WRITABLE, type: 'boolean', defaultValue: true }
		],

		/**
		 * Function to translate old CMDBuild attributes configuration objects to new one used from new FieldManager
		 *
		 * @param {Object} data
		 */
		setAdaptedData: function(data) {
			if (!Ext.isEmpty(data) && Ext.isObject(data)) {
				this.set(CMDBuild.core.proxy.CMProxyConstants.LENGTH, data['len']);
				this.set(CMDBuild.core.proxy.CMProxyConstants.LOOKUP_TYPE, data[CMDBuild.core.proxy.CMProxyConstants.LOOKUP]);
				this.set(CMDBuild.core.proxy.CMProxyConstants.MANDATORY, data['isnotnull']);
				this.set(CMDBuild.core.proxy.CMProxyConstants.SHOW_COLUMN, data['isbasedsp']);
				this.set(CMDBuild.core.proxy.CMProxyConstants.UNIQUE, data['isunique']);

				if (!Ext.isEmpty(data['fieldmode']))
					if (data['fieldmode'] == CMDBuild.core.proxy.CMProxyConstants.WRITE) {
						this.set(CMDBuild.core.proxy.CMProxyConstants.WRITABLE, true);
					} else if (data['fieldmode'] == CMDBuild.core.proxy.CMProxyConstants.HIDDEN) {
						this.set(CMDBuild.core.proxy.CMProxyConstants.HIDDEN, true);
					}

				// ForeignKey's specific
				if (!Ext.isEmpty(data['fkDestination']))
					this.set(CMDBuild.core.proxy.CMProxyConstants.TARGET_CLASS, data['fkDestination']);
			}
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function() {
			var customValidationValue = true;

			switch (this.get(CMDBuild.core.proxy.CMProxyConstants.TYPE)) {
				case 'decimal': {
					customValidationValue = (
						!Ext.isEmpty(this.get(CMDBuild.core.proxy.CMProxyConstants.SCALE))
						&& !Ext.isEmpty(this.get(CMDBuild.core.proxy.CMProxyConstants.PRECISION))
						&& this.get(CMDBuild.core.proxy.CMProxyConstants.SCALE) < this.get(CMDBuild.core.proxy.CMProxyConstants.PRECISION)
					);
				} break;

				case 'foreignkey': {
					customValidationValue = (
						!Ext.isEmpty(this.get(CMDBuild.core.proxy.CMProxyConstants.TARGET_CLASS))
					);
				} break;

				case 'string': {
					customValidationValue = (
						!Ext.isEmpty(this.get(CMDBuild.core.proxy.CMProxyConstants.LENGTH))
						&& this.get(CMDBuild.core.proxy.CMProxyConstants.LENGTH) > 0
					);
				} break;
			}

			return this.callParent(arguments) && customValidationValue;
		}
	});

	/**
	 * @param {String} value
	 * @param {Object} record
	 *
	 * @returns {String}
	 *
	 * @private
	 */
	function toLowerCase(value, record) {
		return value.toLowerCase();
	}

})();