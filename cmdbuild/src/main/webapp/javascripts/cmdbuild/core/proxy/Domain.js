(function() {

	Ext.define('CMDBuild.core.proxy.Domain', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @property {Object} parameters
		 */
		getAll: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'GET',
				url: CMDBuild.core.proxy.Index.domain.read,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getCardinalityStore: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.proxy.CMProxyConstants.NAME, CMDBuild.core.proxy.CMProxyConstants.VALUE],
				data: [
					['1:1', '1:1'],
					['1:N', '1:N'],
					['N:1', 'N:1'],
					['N:N', 'N:N']
				]
			});
		},

		/**
		 * @property {Object} parameters
		 */
		getList: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.Index.domain.getDomainList,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.domain.remove,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.domain.update,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		}
	});

})();