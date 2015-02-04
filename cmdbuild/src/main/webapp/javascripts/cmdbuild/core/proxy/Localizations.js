(function() {

	Ext.define('CMDBuild.core.proxy.Localizations', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
//			'CMDBuild.core.proxy.CMProxyUrlIndex', // TODO
			'CMDBuild.model.Localizations'
		],

		singleton: true,

		/**
		 * @return {Ext.data.Store}
		 */
		getCsvSeparatorStore: function() {
			return Ext.create('Ext.data.Store', {
				fields: [CMDBuild.core.proxy.CMProxyConstants.VALUE],
				data: [
					{ value: ';' },
					{ value: ',' },
					{ value: '|' }
				]
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getFileFormatStore: function() {
			return Ext.create('Ext.data.Store', {
				fields: [CMDBuild.core.proxy.CMProxyConstants.NAME, CMDBuild.core.proxy.CMProxyConstants.VALUE],
				data: [
					{ name: '@@ CSV', value: 'csv' }
				]
			});
		},

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
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getSectionsStore: function() {
			return Ext.create('Ext.data.Store', {
				fields: [CMDBuild.core.proxy.CMProxyConstants.NAME, CMDBuild.core.proxy.CMProxyConstants.VALUE],
				data: [
					{ name: '@@ All', value: 'all' },
					{ name: '@@ Classes', value: 'classes' },
					{ name: '@@ Domains', value: 'domains' },
					{ name: '@@ Lookups', value: 'lookups' },
					{ name: '@@ Menus', value: 'menus' },
					{ name: '@@ Reports', value: 'reports' }
				]
			});
		}
	});

})();