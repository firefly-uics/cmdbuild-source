(function () {

	Ext.define('CMDBuild.core.proxy.Csv', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.core.LoadMask',
			'CMDBuild.core.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		exports: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.csv.exports });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		getRecords: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				method: 'GET',
				url: CMDBuild.core.proxy.index.Json.csv.getRecords,
				loadMask: false,
				callback: function (options, success, response) { // Clears server session data
					CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, {
						method: 'GET',
						url: CMDBuild.core.proxy.index.Json.csv.clearSession,
						loadMask: false
					});

					CMDBuild.core.LoadMask.hide();
				}
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
		},

		/**
		 * @param {Array} excludedValues
		 *
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreImportMode: function (excludedValues) {
			excludedValues = Ext.isArray(excludedValues) ? excludedValues : [];

			var dataValues = [
				[CMDBuild.Translation.replace , 'replace'],
				[CMDBuild.Translation.add, 'add'],
				[CMDBuild.Translation.merge , 'merge']
			];

			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.VALUE],
				data: Ext.Array.filter(dataValues, function (valueArray, i, allValueArrays) {
					return !Ext.Array.contains(excludedValues, valueArray[1]);
				}, this),
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreSeparator: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.VALUE],
				data: [
					[';'],
					[','],
					['|']
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		decode: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.csv.read });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		upload: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.csv.upload });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		}
	});

})();
