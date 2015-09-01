(function() {

	Ext.define('CMDBuild.core.proxy.widgets.CustomForm', {

		requires: ['CMDBuild.core.proxy.Constants'],

		singleton: true,

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getImportFileFormatStore: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.proxy.Constants.DESCRIPTION, CMDBuild.core.proxy.Constants.NAME],
				data: [
					[CMDBuild.Translation.csv, CMDBuild.core.proxy.Constants.CSV]
				],
				sorters: [
					{ property: CMDBuild.core.proxy.Constants.DESCRIPTION, direction: 'ASC' }
				]
			});
		}
	});

})();