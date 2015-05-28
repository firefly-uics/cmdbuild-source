(function() {

	// TODO: should be deleted when refactor lookup module
	CMDBuild.ServiceProxy.LOOKUP_FIELDS = {
		Id: 'Id',
		Code: 'Code',
		Description: 'Description',
		ParentId: 'ParentId',
		Index: 'Number',
		Type: 'Type',
		ParentDescription: 'ParentDescription',
		Active: 'Active',
		Notes: 'Notes',
		TranslationUuid: 'TranslationUuid'
	};

	Ext.define('CMDBuild.core.proxy.Lookup', {
		alternateClassName: 'CMDBuild.ServiceProxy.lookup', // Legacy class name

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.model.Lookup'
		],

		singleton: true,

		/**
		 * @property {Object} parameters
		 */
		get: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.lookup.getList,
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @property {String} type
		 *
		 * @return {Ext.data.Store}
		 */
		getFieldStore: function(type) {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.Lookup.fieldStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.lookup.getList,
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
		getGridStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.Lookup.gridStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.lookup.getList,
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
		 * @property {Object} parameters
		 */
		readAll: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'GET',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.lookup.tree,
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @property {Object} parameters
		 */
		save: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.lookup.save,
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @property {Object} parameters
		 */
		saveType: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.lookup.saveType,
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @property {Object} parameters
		 */
		setDisabled: function(parameters, disable) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: disable ? CMDBuild.core.proxy.CMProxyUrlIndex.lookup.disable : CMDBuild.core.proxy.CMProxyUrlIndex.lookup.enable,
				headers: parameters.headers,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		// Deprecated methods
		/**
		 * @property {String} type
		 *
		 * @return {Ext.data.Store}
		 *
		 * @deprecated
		 */
		getLookupFieldStore: function(type) {
			_deprecated('getLookupFieldStore', this);

			return CMDBuild.core.proxy.Lookup.getFieldStore(type);
		},

		/**
		 * @property {Object} parameters
		 *
		 * @deprecated
		 */
		saveLookup: function(parameters) {
			_deprecated('saveLookup', this);

			CMDBuild.core.proxy.Lookup.save(parameters);
		},

		/**
		 * @property {Object} parameters
		 *
		 * @deprecated
		 */
		saveLookupType: function(parameters) {
			_deprecated('saveLookupType', this);

			CMDBuild.core.proxy.Lookup.saveType(parameters);
		},

		/**
		 * @property {Object} parameters
		 *
		 * @deprecated
		 */
		setLookupDisabled: function(parameters, disable) {
			_deprecated('setLookupDisabled', this);

			CMDBuild.core.proxy.Lookup.setDisabled(parameters, disable);
		}
	});

})();