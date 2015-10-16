(function() {

	Ext.define('CMDBuild.core.proxy.userAndGroup.group.privileges.Filter', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.userAndGroup.group.privileges.GridRecord'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.userAndGroup.group.privileges.GridRecord',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.privileges.filter.read,
					reader: {
						type: 'json',
						root: 'privileges'
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.privileges.filter.update
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		}
	});

})();