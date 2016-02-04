(function() {

	/**
	 * To use only inside cache. Stores aren't realy cached, they just have possibility to get data from cache.
	 *
	 * @private
	 */
	Ext.define('CMDBuild.core.cache.Store', {
		extend: 'Ext.data.Store',

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.Ajax'
		],

		/**
		 * @cfg {String}
		 */
		cacheGroupIdentifier: undefined,

		/**
		 * @param {Array} records
		 * @param {Object} operation
		 * @param {Boolean} success
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		callbackInterceptor: function(records, operation, success) {
			var decodedResponse = CMDBuild.core.interfaces.Ajax.decodeJson(operation.response.responseText);

			CMDBuild.core.interfaces.messages.Warning.display(decodedResponse);
			CMDBuild.core.interfaces.messages.Error.display(decodedResponse, operation.request);

			return true;
		},

		/**
		 * @param {Function or Object} options
		 */
		load: function(options) {
			options = Ext.isEmpty(options) ? {
				callback: Ext.emptyFn,
				params: {},
				scope: this
			} : options;

			if (
				Ext.Array.contains(CMDBuild.core.cache.Cache.managedCacheGroupsArray, this.cacheGroupIdentifier)
				&& CMDBuild.core.cache.Cache.enabled
				&& !CMDBuild.core.cache.Cache.isExpired(this.cacheGroupIdentifier, this.proxy.url, options.params)
			) {
				var cachedValues = CMDBuild.core.cache.Cache.get(this.cacheGroupIdentifier, this.proxy.url, options.params);

				// Adapter (Ajax response to store)
				var adaptedRecords = cachedValues.decodedResult[this.getProxy().getReader().root];
				var adaptedOperation = cachedValues.operation;
				var adaptedSuccess = true;

				this.loadData(adaptedRecords);

				Ext.callback(options.callback, options.scope, [adaptedRecords, adaptedOperation, adaptedSuccess]);
			} else { // Uncachable endpoints manage
				this.callParent(arguments);
			}
		}
	});

})();
