(function() {

	Ext.define('CMDBuild.core.proxy.widgets.CustomForm', {

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		singleton: true,

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getImportFileFormatStore: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, CMDBuild.core.proxy.CMProxyConstants.NAME],
				data: [
					[CMDBuild.Translation.csv, CMDBuild.core.proxy.CMProxyConstants.CSV]
				],
				sorters: [
					{ property: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, direction: 'ASC' }
				]
			});
		}
	});

})();