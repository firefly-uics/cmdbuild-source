(function() {

	Ext.define('CMDBuild.core.proxy.User', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.user.DefaultGroup',
			'CMDBuild.model.user.User'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		disable:function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.user.disable,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				model: 'CMDBuild.model.user.User',
				autoLoad: true,
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.user.getList,
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
				model: 'CMDBuild.model.user.DefaultGroup',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.user.getGroupList,
					reader: {
						root: 'result',
						type: 'json'
					}
				},
				sorters: [{
					property: CMDBuild.core.constants.Proxy.DESCRIPTION,
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
				url: CMDBuild.core.proxy.Index.user.save,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();