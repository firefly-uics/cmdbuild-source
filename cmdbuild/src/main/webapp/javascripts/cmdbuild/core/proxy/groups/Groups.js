(function() {

	Ext.define('CMDBuild.core.proxy.groups.Groups', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.model.groups.StartingClass'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.group.create,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		enableDisable: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.group.enableDisableGroup,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @returns {Ext.data.Store}
		 */
		getStartingClassStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.groups.StartingClass',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.classes.read,
					reader: {
						type: 'json',
						root: 'classes'
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getTypeStore: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.proxy.CMProxyConstants.VALUE, CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION],
				data: [
					[ CMDBuild.core.proxy.CMProxyConstants.NORMAL, CMDBuild.Translation.administration.modsecurity.group.normal ],
					[ CMDBuild.core.proxy.CMProxyConstants.RESTRICTED_ADMIN, CMDBuild.Translation.administration.modsecurity.group.limited_admin ],
					[ CMDBuild.core.proxy.CMProxyConstants.ADMIN, CMDBuild.Translation.administration.modsecurity.group.administrator ]
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.group.update,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();