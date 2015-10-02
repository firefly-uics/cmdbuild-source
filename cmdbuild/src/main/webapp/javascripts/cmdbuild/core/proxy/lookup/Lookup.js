(function() {

	Ext.define('CMDBuild.core.proxy.lookup.Lookup', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.lookup.Lookup'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		disable: function(parameters, disable) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.lookup.disable,
				headers: parameters.headers,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		enable: function(parameters, disable) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.lookup.enable,
				headers: parameters.headers,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getParentStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.lookup.Lookup.parentComboStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.lookup.getParentList,
					reader: {
						type: 'json',
						root: 'rows'
					}
				},
				sorters: [
					{ property: 'ParentDescription', direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {String} type
		 *
		 * @return {Ext.data.Store}
		 *
		 * @deprecated use getStore()
		 */
		getFieldStore: function(type) {
			_deprecated('getFieldStore', this);

			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.lookup.Lookup.fieldStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.lookup.getList,
					reader: {
						type: 'json',
						root: 'rows'
					},
					extraParams: {
						type: type,
						active: true,
						short: true
					},
					actionMethods: 'POST' // Lookup types can have UTF-8 names  not handled correctly
				},
				sorters: [
					{ property: 'Number', direction: 'ASC' },
					{ property: 'Description', direction: 'ASC' }
				]
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.lookup.Lookup.gridStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.lookup.getList,
					reader: {
						type: 'json',
						root: 'rows'
					},
					actionMethods: 'POST' // Lookup types can have UTF-8 names not handled correctly
				},
				sorters: [
					{ property: 'Number', direction: 'ASC' },
					{ property: 'Description', direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		save: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.lookup.save,
				headers: parameters.headers,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		setOrder: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.lookup.setOrder,
				headers: parameters.headers,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		}
	});

})();