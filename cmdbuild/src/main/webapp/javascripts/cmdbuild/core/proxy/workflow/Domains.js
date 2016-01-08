(function() {

	Ext.define('CMDBuild.core.proxy.workflow.Domains', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.workflow.tabs.domains.Grid'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function() {
			return CMDBuild.core.cache.Cache.requestAsStore(CMDBuild.core.constants.Proxy.DOMAIN, {
				autoLoad: false,
				model: 'CMDBuild.model.workflow.tabs.domains.Grid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.domain.getDomainList,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.DOMAINS
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		}
	});

})();