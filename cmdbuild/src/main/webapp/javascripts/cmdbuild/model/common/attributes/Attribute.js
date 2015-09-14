(function() {

	Ext.define('CMDBuild.model.common.attributes.Attribute', {
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
		 * @returns {Boolean}
		 */
		isValid: function() {
			var customValidationValue = true;

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