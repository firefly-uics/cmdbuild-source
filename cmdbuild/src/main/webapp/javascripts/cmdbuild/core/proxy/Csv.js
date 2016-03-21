(function() {

	Ext.define('CMDBuild.core.proxy.Csv', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.Ajax',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.core.LoadMask',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		exports: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.csv.exports });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		getRecords: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				method: 'GET',
				url: CMDBuild.core.proxy.Index.csv.getCsvRecords,
				callback: function(options, success, response) { // Clears server session data
					CMDBuild.core.interfaces.Ajax.request({
						method: 'GET',
						url: CMDBuild.core.proxy.Index.csv.clearSession
					});

					CMDBuild.core.LoadMask.hide();
				}
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CSV, parameters);
		},

		/**
		 * @param {Array} excludedValues
		 *
		 * @return {Ext.data.ArrayStore}
		 */
		getStoreImportMode: function(excludedValues) {
			excludedValues = Ext.isArray(excludedValues) ? excludedValues : [];

			var dataValues = [
				[CMDBuild.Translation.replace , 'replace'],
				[CMDBuild.Translation.add, 'add'],
				[CMDBuild.Translation.merge , 'merge']
			];

			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.VALUE],
				data: Ext.Array.filter(dataValues, function(valueArray, i, allValueArrays) {
					return !Ext.Array.contains(excludedValues, valueArray[1]);
				}, this),
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getStoreSeparator: function() {
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
		 */
		decode: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.csv.readCsv });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		upload: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.csv.uploadCsv });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		}
	});

})();
