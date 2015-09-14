(function() {

	/**
	 * FilterChooser proxy
	 */
	Ext.define('CMDBuild.core.proxy.Filter', {
		alternateClassName: 'CMDBuild.ServiceProxy.Filter', // Legacy class name

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		create: function(filter, config) {
			doRequest(filter, config, CMDBuild.core.proxy.Index.filters.create, 'POST', true);
		},

		update: function(filter, config) {
			doRequest(filter, config, CMDBuild.core.proxy.Index.filters.update, 'POST', true);
		},

		remove: function(filter, config) {
			doRequest(filter, config, CMDBuild.core.proxy.Index.filters.remove, 'POST', false);
		},

		/**
		 * Returns a store with the filters for a given group
		 *
		 * @return {Ext.data.Store}
		 */
		newGroupStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.CMFilterModel',
				pageSize: _CMUtils.grid.getPageSize(),
				proxy: {
					url: CMDBuild.core.proxy.Index.filters.groupStore,
					type: 'ajax',
					reader: {
						root: 'filters',
						type: 'json',
						totalProperty: 'count'
					}
				},
				sorters: [{
					property: CMDBuild.core.proxy.Constants.DESCRIPTION,
					direction: 'ASC'
				}]
			});
		},

		/**
		 * Return the store of the current logged user
		 *
		 * @returns {Ext.data.Store} store
		 */
		newUserStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.CMFilterModel',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.filters.userStore,
					reader: {
						idProperty: 'id',
						type: 'json',
						root: 'filters'
					}
				},
				sorters: [{
					property: CMDBuild.core.proxy.Constants.DESCRIPTION,
					direction: 'ASC'
				}]
			 });
		},

		/**
		 * @param {String} className
		 *
		 * @returns {Ext.data.Store}
		 */
		newSystemStore: function(className) {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.CMFilterModel',
				pageSize: _CMUtils.grid.getPageSize(),
				proxy: {
					url: CMDBuild.core.proxy.Index.filters.read,
					type: 'ajax',
					reader: {
						root: 'filters',
						type: 'json',
						totalProperty: 'count'
					},
					extraParams: {
						className: className
					}
				},
				sorters: [{
					property: CMDBuild.core.proxy.Constants.DESCRIPTION,
					direction: 'ASC'
				}]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getDefaults: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.Index.filter.defaultForGroups.read,
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
		setDefaults: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.Index.filter.defaultForGroups.update,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

	function doRequest(filter, config, url, method, fullParams) {
		if (Ext.getClassName(filter) == 'CMDBuild.model.CMFilterModel') {
			var request = config || {};

			request.url = url;
			request.method = method;
			request.params = getParams(filter, fullParams);

			CMDBuild.Ajax.request(config);
		}
	}

	function getParams(filter, full) {
		var params = {};

		params.id = filter.getId();

		if (full) {
			params.className = filter.getEntryType();
			params.configuration = Ext.encode(filter.getConfiguration());
			params.description = filter.getDescription();
			params.name = filter.getName();
			params.template = filter.isTemplate();
		}

		return params;
	}
})();