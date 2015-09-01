(function() {

	/**
	 * Subset of all attribute's properties
	 */
	Ext.define('CMDBuild.model.widget.customForm.Attribute', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.EDITOR_TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.FILTER, type: 'auto' },
			{ name: CMDBuild.core.proxy.Constants.LENGTH, type: 'int', defaultValue: 0 },
			{ name: CMDBuild.core.proxy.Constants.LOOKUP_TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.MANDATORY, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.PRECISION, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.SCALE, type: 'int', defaultValue: 0 },
			{ name: CMDBuild.core.proxy.Constants.TARGET_CLASS, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.UNIQUE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.WRITABLE, type: 'boolean' }
		],

		/**
		 * Create a translation layer to adapt old CMDBuild field definition with new one.
		 * To avoid this should be necessary to refactor FieldManager class.
		 *
		 * @returns {Object}
		 */
		getAdaptedData: function() {
			var objectModel = this.getData();

			objectModel['fieldmode'] = this.get(CMDBuild.core.proxy.Constants.WRITABLE) ? 'write' : 'read';
			objectModel['isbasedsp'] = true;

			switch (objectModel[CMDBuild.core.proxy.Constants.TYPE]) {
				case 'LOOKUP': {
					objectModel['lookup'] = this.get(CMDBuild.core.proxy.Constants.LOOKUP_TYPE);
					objectModel['lookupchain'] = [];
				} break;

				case 'REFERENCE': {
					objectModel['referencedClassName'] = this.get(CMDBuild.core.proxy.Constants.TARGET_CLASS);

					// New filter object structure adapter
					objectModel[CMDBuild.core.proxy.Constants.FILTER] = this.get(CMDBuild.core.proxy.Constants.FILTER)[CMDBuild.core.proxy.Constants.EXPRESSION];
					objectModel[CMDBuild.core.proxy.Constants.META] = {};
					Ext.Object.each(
						this.get(CMDBuild.core.proxy.Constants.FILTER)[CMDBuild.core.proxy.Constants.CONTEXT],
						function(key, value, myself) {
							objectModel[CMDBuild.core.proxy.Constants.META]['system.template.' + key] = value;
						},
						this
					);
				} break;
			}

			return objectModel;
		},

		/**
		 * @returns {Boolean}
		 */
		isValid: function() {
			var customValidationValue = false;

			switch (this.get(CMDBuild.core.proxy.Constants.TYPE)) {
				case 'DECIMAL': {
					customValidationValue = (
						!Ext.isEmpty(this.get(CMDBuild.core.proxy.Constants.SCALE))
						&& !Ext.isEmpty(this.get(CMDBuild.core.proxy.Constants.PRECISION))
						&& this.get(CMDBuild.core.proxy.Constants.SCALE) < this.get(CMDBuild.core.proxy.Constants.PRECISION)
					);
				} break;

				case 'STRING': {
					customValidationValue = (
						!Ext.isEmpty(this.get(CMDBuild.core.proxy.Constants.LENGTH))
						&& this.get(CMDBuild.core.proxy.Constants.LENGTH) > 0
					);
				} break;
			}

			return this.callParent(arguments) && customValidationValue;
		}
	});

})();