(function () {

	Ext.define('CMDBuild.core.proxy.utility.ImportCsv', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.core.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		getRecords: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.csv.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CSV, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		storeRecords: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getCsvUtility(),
				url: CMDBuild.core.proxy.index.Json.csv.import.update
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CSV, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		updateRecords: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.csv.import.updateRecords });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CSV, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		upload: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.csv.import.create });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		}
	});

})();
