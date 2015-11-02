(function() {

	Ext.define('CMDBuild.model.widget.customForm.Configuration', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: 'alwaysenabled', type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES, type: 'auto' }, // Object to gather all UI disable flags
			{ name: CMDBuild.core.proxy.CMProxyConstants.DATA, type: 'auto' }, // Encoded array of CMDBuild.model.common.Generic models strings
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.LABEL, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.LAYOUT, type: 'string', defaultValue: 'grid' }, // Widget view mode [grid|form]
			{ name: CMDBuild.core.proxy.CMProxyConstants.MODEL, type: 'auto' }, // Encoded array of CMDBuild.model.widget.customForm.Attribute models strings
			{ name: CMDBuild.core.proxy.CMProxyConstants.REQUIRED, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.VARIABLES, type: 'auto' } // Unmanaged variables

		],

		/**
		 * @param {Object} data
		 */
		constructor: function(data) {
			this.callParent(arguments);

			// Apply form model attributes model
			this.set(CMDBuild.core.proxy.CMProxyConstants.MODEL, data[CMDBuild.core.proxy.CMProxyConstants.MODEL]);

			// Decode data string
			this.set(CMDBuild.core.proxy.CMProxyConstants.DATA, data[CMDBuild.core.proxy.CMProxyConstants.DATA]);

			// Apply capabilities model
			this.set(CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES, data[CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES]);
		},

		/**
		 * @param {String} fieldName
		 * @param {Object} newValue
		 *
		 * @returns {String}
		 *
		 * @override
		 */
		set: function(fieldName, newValue) {
			if (!Ext.isEmpty(newValue)) {
				switch (fieldName) {
					case CMDBuild.core.proxy.CMProxyConstants.CAPABILITIES: {
						newValue = Ext.create('CMDBuild.model.widget.customForm.Capabilities', newValue);
					} break;

					case CMDBuild.core.proxy.CMProxyConstants.DATA: {
						newValue = Ext.isString(newValue) ? Ext.decode(newValue) : newValue;

						var attributesArray = [];

						Ext.Array.forEach(newValue, function(attributeObject, i, allAttributeObjects) {
							attributesArray.push(Ext.create('CMDBuild.model.common.Generic', attributeObject));
						}, this);

						newValue = attributesArray;
					} break;

					/**
					 * Uses custom Attribute model to adapt to old FieldManager implementation
					 * TODO: delete on full FieldManager implementation
					 */
					case CMDBuild.core.proxy.CMProxyConstants.MODEL: {
						newValue = Ext.isString(newValue) ? Ext.decode(newValue) : newValue;

						var attributesArray = [];

						Ext.Array.forEach(newValue, function(attributeObject, i, allAttributeObjects) {
							attributesArray.push(Ext.create('CMDBuild.model.widget.customForm.Attribute', attributeObject));
						}, this);

						newValue = attributesArray;
					} break;

					default: {
						if (Ext.isString(newValue))
							newValue = Ext.decode(newValue);
					}
				}
			}

			this.callParent(arguments);
		}
	});

})();