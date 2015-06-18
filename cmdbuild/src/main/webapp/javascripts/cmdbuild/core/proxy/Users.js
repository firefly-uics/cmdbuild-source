(function() {

	Ext.define('CMDBuild.core.proxy.Users', {

		requires: [
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.Users'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		disable:function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.users.disable,
				params: parameters.params,
				scope: parameters.scope,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				model: 'CMDBuild.model.Users.single',
				autoLoad: true,
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.users.getList,
					reader: {
						type: 'json',
						root: 'rows'
					}
				},
				sorters: [{
					property: 'username',
					direction: 'ASC'
				}]
			});
		},

		/**
		 * @return {Ext.data.JsonStore}
		 */
		getDefaultGroupStore: function() {
			return Ext.create('Ext.data.JsonStore', {
				autoLoad: false,
				model: 'CMDBuild.model.Users.defaultGroup',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.users.getGroupList,
					reader: {
						root: 'result',
						type: 'json'
					}
				},
				sorters: [{
					property: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
					direction: 'ASC'
				}]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		save:function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.users.save,
				params: parameters.params,
				scope: parameters.scope,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		}
	});

})();