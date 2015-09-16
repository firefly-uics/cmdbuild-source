(function() {

	Ext.define('CMDBuild.core.proxy.localization.importExport.Csv', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		exports: function(parameters) {
			parameters.form.submit({
				url: CMDBuild.core.proxy.Index.localizations.importExport.exportCsv,
				params: parameters.params,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		imports: function(parameters) {
			parameters.form.submit({
				url: CMDBuild.core.proxy.Index.localizations.importExport.importCsv,
				params: parameters.params,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},
	});

})();