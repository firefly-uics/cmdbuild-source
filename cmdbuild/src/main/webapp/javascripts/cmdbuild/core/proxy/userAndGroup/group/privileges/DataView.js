(function() {

	Ext.define('CMDBuild.core.proxy.userAndGroup.group.privileges.DataView', {

		requires: [
			'CMDBuild.core.Cache',
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
					url: CMDBuild.core.proxy.Index.privileges.dataView.read,
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
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.privileges.dataView.update
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		}
	});

})();