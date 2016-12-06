(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.navigation.chronology.Record', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DATE, type: 'date' },
			{ name: CMDBuild.core.constants.Proxy.ENTRY_TYPE, type: 'auto' }, // Selected entryType
			{ name: CMDBuild.core.constants.Proxy.ITEM, type: 'auto' }, // Item selected from grid
			{ name: CMDBuild.core.constants.Proxy.MODULE_ID, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.SECTION, type: 'auto', defaultValue: {} },  // Form tab object
			{ name: CMDBuild.core.constants.Proxy.SUB_SECTION, type: 'auto', defaultValue: {} }  // Form tab sub-section
		],

		/**
		 * @param {Object} data
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (data) {
			data = Ext.isObject(data) ? data : {};

			// Apply defaults
			Ext.applyIf(data, {
				entryType :{},
				item :{},
				section :{},
				subSection :{}
			});

			data[CMDBuild.core.constants.Proxy.DATE] = new Date();
			data[CMDBuild.core.constants.Proxy.ENTRY_TYPE] = Ext.create(
				Ext.isNumber(data[CMDBuild.core.constants.Proxy.ENTRY_TYPE][CMDBuild.core.constants.Proxy.ID])
					? 'CMDBuild.model.navigation.chronology.PropertyNumber' : 'CMDBuild.model.navigation.chronology.PropertyString',
				data[CMDBuild.core.constants.Proxy.ENTRY_TYPE]
			);
			data[CMDBuild.core.constants.Proxy.ITEM] = Ext.create(
				Ext.isNumber(data[CMDBuild.core.constants.Proxy.ITEM][CMDBuild.core.constants.Proxy.ID])
					? 'CMDBuild.model.navigation.chronology.PropertyNumber' : 'CMDBuild.model.navigation.chronology.PropertyString',
				data[CMDBuild.core.constants.Proxy.ITEM]
			);
			data[CMDBuild.core.constants.Proxy.SECTION] = Ext.create(
				Ext.isNumber(data[CMDBuild.core.constants.Proxy.SECTION][CMDBuild.core.constants.Proxy.ID])
					? 'CMDBuild.model.navigation.chronology.PropertyNumber' : 'CMDBuild.model.navigation.chronology.PropertyString',
				data[CMDBuild.core.constants.Proxy.SECTION]
			);
			data[CMDBuild.core.constants.Proxy.SUB_SECTION] = Ext.create(
				Ext.isNumber(data[CMDBuild.core.constants.Proxy.SUB_SECTION][CMDBuild.core.constants.Proxy.ID])
					? 'CMDBuild.model.navigation.chronology.PropertyNumber' : 'CMDBuild.model.navigation.chronology.PropertyString',
				data[CMDBuild.core.constants.Proxy.SUB_SECTION]
			);

			this.callParent(arguments);
		},

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 *
		 * @returns {Boolean}
		 */
		equals: function (record) {
			return (
				Ext.getClassName(record) == Ext.getClassName(this)
				&& this.get(CMDBuild.core.constants.Proxy.MODULE_ID) == record.get(CMDBuild.core.constants.Proxy.MODULE_ID)
				&& this.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE).equals(record.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE))
				&& this.get(CMDBuild.core.constants.Proxy.ITEM).equals(record.get(CMDBuild.core.constants.Proxy.ITEM))
				&& this.get(CMDBuild.core.constants.Proxy.SECTION).equals(record.get(CMDBuild.core.constants.Proxy.SECTION))
				&& this.get(CMDBuild.core.constants.Proxy.SUB_SECTION).equals(record.get(CMDBuild.core.constants.Proxy.SUB_SECTION))
			);
		},

		/**
		 * @param {Array or String} property
		 *
		 * @returns {Mixed}
		 *
		 * @override
		 */
		get: function (property) {
			if (Ext.isArray(property) && !Ext.isEmpty(property)) {
				var returnValue = this;

				Ext.Array.forEach(property, function (propertyName, i, allPropertyNames) {
					if (Ext.isObject(returnValue) && !Ext.Object.isEmpty(returnValue))
						if (Ext.isFunction(returnValue.get)) { // Ext.data.Model manage
							returnValue = returnValue.get(propertyName);
						} else if (!Ext.isEmpty(returnValue[propertyName])) { // Simple object manage
							returnValue = returnValue[propertyName];
						} else { // Not found
							returnValue = null;
						}
				}, this);

				return returnValue;
			}

			return this.callParent(arguments);
		},

		/**
		 * @param {Array or String} attributePath
		 *
		 * @returns {Boolean}
		 */
		isEmpty: function (attributePath) {
			if (!Ext.isEmpty(attributePath)) {
				var requiredValue = this.get(attributePath);

				if (Ext.isObject(requiredValue))
					return Ext.Object.isEmpty(requiredValue);

				return Ext.isEmpty(requiredValue);
			}

			return Ext.Object.isEmpty(this.getData());
		}
	});

})();
