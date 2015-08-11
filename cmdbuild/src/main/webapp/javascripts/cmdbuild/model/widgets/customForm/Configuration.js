(function() {

	Ext.define('CMDBuild.model.widgets.customForm.Configuration', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: 'alwaysenabled', type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES, type: 'auto' }, // Object to gather all UI disable flags
			{ name: CMDBuild.core.proxy.CMProxyConstants.FORM, type: 'auto' }, // Encoded string of array of CMDBuild.model.widgets.customForm.Attribute models
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.LABEL, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.LAYOUT, type: 'string', defaultValue: 'grid' }, // Widget view mode
			{ name: CMDBuild.core.proxy.CMProxyConstants.REQUIRED, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.VARIABLES, type: 'auto' } // TODO

		],

		/**
		 * @param {Object} data
		 */
		constructor: function(data) {
			var attributesArray = [];

			if (Ext.isFunction(data.getData))
				data = data.getData();

			this.callParent(arguments);

			// Apply form attributes model
			if (Ext.isString(data[CMDBuild.core.proxy.CMProxyConstants.FORM]))
				data[CMDBuild.core.proxy.CMProxyConstants.FORM] = Ext.decode(data[CMDBuild.core.proxy.CMProxyConstants.FORM]);

			Ext.Array.forEach(data[CMDBuild.core.proxy.CMProxyConstants.FORM], function(attributeObject, i, AllAttributesObjects) {
				attributesArray.push(Ext.create('CMDBuild.model.widgets.customForm.Attribute', attributeObject));
			}, this);

			this.set(CMDBuild.core.proxy.CMProxyConstants.FORM, attributesArray);

			// Apply capabilities model
			this.set(
				CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES,
				Ext.create('CMDBuild.model.widgets.customForm.Capabilities', data[CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES])
			);
		}
	});

})();