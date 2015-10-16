(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.widget.customForm.Configuration', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'alwaysenabled', type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CAPABILITIES, type: 'auto' }, // Object to gather all UI disable flags
			{ name: CMDBuild.core.constants.Proxy.DATA, type: 'auto' }, // Encoded array of CMDBuild.model.common.Generic models strings
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.LABEL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.LAYOUT, type: 'string', defaultValue: 'grid' }, // Widget view mode [grid|form]
			{ name: CMDBuild.core.constants.Proxy.MODEL, type: 'auto' }, // Encoded array of CMDBuild.model.widget.customForm.Attribute models strings
			{ name: CMDBuild.core.constants.Proxy.REQUIRED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.VARIABLES, type: 'auto' } // Unmanaged variables
		],

		/**
		 * @param {Object} data
		 */
		constructor: function(data) {
			data = data || {};

			this.callParent(arguments);

			// Apply form model attributes model
			if (!Ext.isEmpty(data[CMDBuild.core.constants.Proxy.MODEL]))
				this.set(CMDBuild.core.constants.Proxy.MODEL, data[CMDBuild.core.constants.Proxy.MODEL]);

			// Decode data string
			if (!Ext.isEmpty(data[CMDBuild.core.constants.Proxy.DATA]))
				this.set(CMDBuild.core.constants.Proxy.DATA, data[CMDBuild.core.constants.Proxy.DATA]);

			// Apply capabilities model
			if (!Ext.isEmpty(data[CMDBuild.core.constants.Proxy.CAPABILITIES]))
				this.set(CMDBuild.core.constants.Proxy.CAPABILITIES, data[CMDBuild.core.constants.Proxy.CAPABILITIES]);
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
			switch (fieldName) {
				case CMDBuild.core.constants.Proxy.CAPABILITIES: {
					newValue = Ext.create('CMDBuild.model.widget.customForm.Capabilities', newValue);
				} break;

				case CMDBuild.core.constants.Proxy.DATA: {
					newValue = Ext.isString(newValue) ? Ext.decode(newValue) : newValue;

					var attributesArray = [];

					Ext.Array.forEach(newValue, function(attributeObject, i, AllAttributesObjects) {
						attributesArray.push(Ext.create('CMDBuild.model.common.Generic', attributeObject));
					}, this);

					newValue = attributesArray;
				} break;

				case CMDBuild.core.constants.Proxy.MODEL: {
					newValue = Ext.isString(newValue) ? Ext.decode(newValue) : newValue;

					var attributesArray = [];

					Ext.Array.forEach(newValue, function(attributeObject, i, AllAttributesObjects) {
						attributesArray.push(Ext.create('CMDBuild.model.widget.customForm.Attribute', attributeObject));
					}, this);

					newValue = attributesArray;
				} break;

				default: {
					if (Ext.isString(newValue))
						newValue = Ext.decode(newValue);
				}
			}

			this.callParent(arguments);
		}
	});

})();