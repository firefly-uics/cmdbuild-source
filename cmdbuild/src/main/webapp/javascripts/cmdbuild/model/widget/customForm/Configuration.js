(function() {

	Ext.define('CMDBuild.model.widget.customForm.Configuration', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: 'alwaysenabled', type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.CAPABILITIES, type: 'auto' }, // Object to gather all UI disable flags
			{ name: CMDBuild.core.proxy.Constants.DATA, type: 'auto' }, // Encoded array of CMDBuild.model.common.Generic models strings
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.LABEL, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.LAYOUT, type: 'string', defaultValue: 'grid' }, // Widget view mode [grid|form]
			{ name: CMDBuild.core.proxy.Constants.MODEL, type: 'auto' }, // Encoded array of CMDBuild.model.widget.customForm.Attribute models strings
			{ name: CMDBuild.core.proxy.Constants.REQUIRED, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.VARIABLES, type: 'auto' } // Unmanaged variables

		],

		/**
		 * @param {Object} data
		 */
		constructor: function(data) {
			this.callParent(arguments);

			// Apply form model attributes model
			this.set(CMDBuild.core.proxy.Constants.MODEL, data[CMDBuild.core.proxy.Constants.MODEL]);

			// Decode data string
			this.set(CMDBuild.core.proxy.Constants.DATA, data[CMDBuild.core.proxy.Constants.DATA]);

			// Apply capabilities model
			this.set(CMDBuild.core.proxy.Constants.CAPABILITIES, data[CMDBuild.core.proxy.Constants.CAPABILITIES]);
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
				case CMDBuild.core.proxy.Constants.CAPABILITIES: {
					newValue = Ext.create('CMDBuild.model.widget.customForm.Capabilities', newValue);
				} break;

				case CMDBuild.core.proxy.Constants.DATA: {
					newValue = Ext.isString(newValue) ? Ext.decode(newValue) : newValue;

					var attributesArray = [];

					Ext.Array.forEach(newValue, function(attributeObject, i, AllAttributesObjects) {
						attributesArray.push(Ext.create('CMDBuild.model.common.Generic', attributeObject));
					}, this);

					newValue = attributesArray;
				} break;

				case CMDBuild.core.proxy.Constants.MODEL: {
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