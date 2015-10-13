(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * Subset of all attribute's properties
	 */
	Ext.define('CMDBuild.model.widget.customForm.Attribute', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.EDITOR_TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.FILTER, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.LENGTH, type: 'int', defaultValue: 0 },
			{ name: CMDBuild.core.constants.Proxy.LOOKUP_TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.MANDATORY, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PRECISION, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.SCALE, type: 'int', defaultValue: 0 },
			{ name: CMDBuild.core.constants.Proxy.TARGET_CLASS, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.UNIQUE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.WRITABLE, type: 'boolean' }
		],

		/**
		 * Create a translation layer to adapt old CMDBuild field definition with new one.
		 * To avoid this should be necessary to refactor FieldManager class.
		 *
		 * @returns {Object}
		 */
		getAdaptedData: function() {
			var objectModel = this.getData();

			objectModel['fieldmode'] = this.get(CMDBuild.core.constants.Proxy.WRITABLE) ? 'write' : 'read';
			objectModel['isbasedsp'] = true;
			objectModel['isnotnull'] = this.get(CMDBuild.core.constants.Proxy.MANDATORY);

			switch (objectModel[CMDBuild.core.constants.Proxy.TYPE]) {
				case 'LOOKUP': {
					objectModel['lookup'] = this.get(CMDBuild.core.constants.Proxy.LOOKUP_TYPE);
					objectModel['lookupchain'] = [];
				} break;

				case 'REFERENCE': {
					objectModel['referencedClassName'] = this.get(CMDBuild.core.constants.Proxy.TARGET_CLASS);

					// New filter object structure adapter
					objectModel[CMDBuild.core.constants.Proxy.FILTER] = this.get(CMDBuild.core.constants.Proxy.FILTER)[CMDBuild.core.constants.Proxy.EXPRESSION];
					objectModel[CMDBuild.core.constants.Proxy.META] = {};
					Ext.Object.each(this.get(CMDBuild.core.constants.Proxy.FILTER)[CMDBuild.core.constants.Proxy.CONTEXT], function(key, value, myself) {
						objectModel[CMDBuild.core.constants.Proxy.META]['system.template.' + key] = value;
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

			switch (this.get(CMDBuild.core.constants.Proxy.TYPE)) {
				case 'DECIMAL': {
					customValidationValue = (
						!Ext.isEmpty(this.get(CMDBuild.core.constants.Proxy.SCALE))
						&& !Ext.isEmpty(this.get(CMDBuild.core.constants.Proxy.PRECISION))
						&& this.get(CMDBuild.core.constants.Proxy.SCALE) < this.get(CMDBuild.core.constants.Proxy.PRECISION)
					);
				} break;

				case 'STRING': {
					customValidationValue = (
						!Ext.isEmpty(this.get(CMDBuild.core.constants.Proxy.LENGTH))
						&& this.get(CMDBuild.core.constants.Proxy.LENGTH) > 0
					);
				} break;
			}

			return this.callParent(arguments) && customValidationValue;
		}
	});

})();