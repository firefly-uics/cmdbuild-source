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

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.csv.getRecords });

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
				url: CMDBuild.core.proxy.index.Json.csv.storeRecords
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

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.csv.updateRecords });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CSV, parameters);
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
