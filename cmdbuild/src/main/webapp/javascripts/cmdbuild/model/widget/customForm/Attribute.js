(function() {

	Ext.require(['CMDBuild.core.proxy.CMProxyConstants']);

	/**
	 * Adapter model class to old FieldManager implementation
	 * TODO: delete on full FieldManager implementation
	 */
	Ext.define('CMDBuild.model.widget.customForm.Attribute', {
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
		 * Create a translation layer to adapt old CMDBuild field definition with new one.
		 * To avoid this should be necessary to refactor FieldManager class.
		 *
		 * @returns {Object}
		 */
		getAdaptedData: function() {
			var objectModel = this.getData();

			objectModel['fieldmode'] = this.get(CMDBuild.core.proxy.CMProxyConstants.WRITABLE) ? 'write' : 'read';
			objectModel['fieldmode'] = this.get(CMDBuild.core.proxy.CMProxyConstants.HIDDEN) ? CMDBuild.core.proxy.CMProxyConstants.HIDDEN : objectModel['fieldmode'];
			objectModel['isbasedsp'] = this.get(CMDBuild.core.proxy.CMProxyConstants.SHOW_COLUMN);
			objectModel['isnotnull'] = this.get(CMDBuild.core.proxy.CMProxyConstants.MANDATORY);

			switch (objectModel[CMDBuild.core.proxy.CMProxyConstants.TYPE]) {
				case 'lookup': {
					objectModel['lookup'] = this.get(CMDBuild.core.proxy.CMProxyConstants.LOOKUP_TYPE);
					objectModel['lookupchain'] = _CMCache.getLookupchainForType(this.get(CMDBuild.core.proxy.CMProxyConstants.LOOKUP_TYPE));
				} break;

				case 'reference': {
					objectModel['referencedClassName'] = this.get(CMDBuild.core.proxy.CMProxyConstants.TARGET_CLASS);
					objectModel[CMDBuild.core.proxy.CMProxyConstants.META] = {};

					// New filter object structure adapter
					if (!Ext.isEmpty(this.get(CMDBuild.core.proxy.CMProxyConstants.FILTER))) {
						objectModel[CMDBuild.core.proxy.CMProxyConstants.FILTER] = this.get(CMDBuild.core.proxy.CMProxyConstants.FILTER)[CMDBuild.core.proxy.CMProxyConstants.EXPRESSION];

						Ext.Object.each(this.get(CMDBuild.core.proxy.CMProxyConstants.FILTER)[CMDBuild.core.proxy.CMProxyConstants.CONTEXT], function(key, value, myself) {
							objectModel[CMDBuild.core.proxy.CMProxyConstants.META]['system.template.' + key] = value;
						}, this);
					}
				} break;
			}

			objectModel[CMDBuild.core.proxy.CMProxyConstants.TYPE] = this.get(CMDBuild.core.proxy.CMProxyConstants.TYPE).toUpperCase();

			return objectModel;
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function() {
			var customValidationValue = false;

			switch (this.get(CMDBuild.core.proxy.CMProxyConstants.TYPE)) {
				case 'decimal': {
					customValidationValue = (
						!Ext.isEmpty(this.get(CMDBuild.core.proxy.CMProxyConstants.SCALE))
						&& !Ext.isEmpty(this.get(CMDBuild.core.proxy.CMProxyConstants.PRECISION))
						&& this.get(CMDBuild.core.proxy.CMProxyConstants.SCALE) < this.get(CMDBuild.core.proxy.CMProxyConstants.PRECISION)
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