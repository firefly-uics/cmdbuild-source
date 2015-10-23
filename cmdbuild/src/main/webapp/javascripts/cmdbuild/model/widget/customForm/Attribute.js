(function() {

	/**
	 * Subset of all attribute's properties
	 */
	Ext.define('CMDBuild.model.widget.customForm.Attribute', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.EDITOR_TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FILTER, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.LENGTH, type: 'int', defaultValue: 0 },
			{ name: CMDBuild.core.proxy.CMProxyConstants.LOOKUP_TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.MANDATORY, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PRECISION, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SCALE, type: 'int', defaultValue: 0 },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TARGET_CLASS, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.UNIQUE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.WRITABLE, type: 'boolean' }
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
			objectModel['isbasedsp'] = true;
			objectModel['isnotnull'] = this.get(CMDBuild.core.proxy.CMProxyConstants.MANDATORY);

			switch (objectModel[CMDBuild.core.proxy.CMProxyConstants.TYPE]) {
				case 'LOOKUP': {
					objectModel['lookup'] = this.get(CMDBuild.core.proxy.CMProxyConstants.LOOKUP_TYPE);
					objectModel['lookupchain'] = [];
				} break;

				case 'REFERENCE': {
					objectModel['referencedClassName'] = this.get(CMDBuild.core.proxy.CMProxyConstants.TARGET_CLASS);

					// New filter object structure adapter
					objectModel[CMDBuild.core.proxy.CMProxyConstants.FILTER] = this.get(CMDBuild.core.proxy.CMProxyConstants.FILTER)[CMDBuild.core.proxy.CMProxyConstants.EXPRESSION];
					objectModel[CMDBuild.core.proxy.CMProxyConstants.META] = {};

					Ext.Object.each(this.get(CMDBuild.core.proxy.CMProxyConstants.FILTER)[CMDBuild.core.proxy.CMProxyConstants.CONTEXT], function(key, value, myself) {
						objectModel[CMDBuild.core.proxy.CMProxyConstants.META]['system.template.' + key] = value;
					}, this);
				} break;
			}

			return objectModel;
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function() {
			var customValidationValue = false;

			switch (this.get(CMDBuild.core.proxy.CMProxyConstants.TYPE)) {
				case 'DECIMAL': {
					customValidationValue = (
						!Ext.isEmpty(this.get(CMDBuild.core.proxy.CMProxyConstants.SCALE))
						&& !Ext.isEmpty(this.get(CMDBuild.core.proxy.CMProxyConstants.PRECISION))
						&& this.get(CMDBuild.core.proxy.CMProxyConstants.SCALE) < this.get(CMDBuild.core.proxy.CMProxyConstants.PRECISION)
					);
				} break;

				case 'STRING': {
					customValidationValue = (
						!Ext.isEmpty(this.get(CMDBuild.core.proxy.CMProxyConstants.LENGTH))
						&& this.get(CMDBuild.core.proxy.CMProxyConstants.LENGTH) > 0
					);
				} break;
			}

			return this.callParent(arguments) && customValidationValue;
		}
	});

})();