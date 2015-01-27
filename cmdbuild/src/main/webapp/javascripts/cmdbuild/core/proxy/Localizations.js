(function() {

	Ext.define('CMDBuild.core.proxy.Localizations', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.model.Localizations'
		],

		singleton: true,

		/**
		 * @return {Ext.data.Store}
		 */
		getLanguagesStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.Localizations.translation',
				proxy: {
					type: 'ajax',
					url: 'services/json/utils/listavailabletranslations',
					reader: {
						type: 'json',
						root: 'translations'
					}
				},
				sorters: {
					property: 'name',
					direction: 'ASC'
				}
			});
		}
	});

})();