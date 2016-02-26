(function() {

	Ext.define('CMDBuild.core.proxy.localization.Export', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		exports: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.localizations.importExport.exportCsv });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getStoreFileFormat: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.NAME],
				data: [
					[CMDBuild.Translation.csv, CMDBuild.core.constants.Proxy.CSV]
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getStoreEnabledLaguages: function () {
			var data = [];

			Ext.Object.each(CMDBuild.configuration.localization.getEnabledLanguages(), function (tag, languageModel, myself) {
				data.push([tag, languageModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION)]);
			}, this);

			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.TAG, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: data,
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		}
	});

})();
