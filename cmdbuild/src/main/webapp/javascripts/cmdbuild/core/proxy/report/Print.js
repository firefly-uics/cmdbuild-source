(function() {

	Ext.define('CMDBuild.core.proxy.report.Print', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		createCardDetails: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.report.print.cardDetails });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		createClassSchema: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.report.print.classSchema });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		createDataViewSqlSchema: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.report.print.sqlView });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		createSchema: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.report.print.schema });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		createView: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.report.print.currentView });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters);
		}
	});

})();