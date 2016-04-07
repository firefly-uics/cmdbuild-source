(function() {

	/**
	 * To use only inside cache class
	 *
	 * @private
	 */
	Ext.define('CMDBuild.core.cache.Store', {
		extend: 'Ext.data.Store',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.Ajax'
		],

		/**
		 * @cfg {String}
		 */
		groupId: undefined,

		/**
		 * @cfg {String}
		 */
		type: 'store',

		/**
		 * @param {Array} records
		 * @param {Object} operation
		 * @param {Boolean} success
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		callbackInterceptor: function (records, operation, success) {
			var decodedResponse = CMDBuild.core.interfaces.Ajax.decodeJson(operation.response.responseText);

			CMDBuild.core.interfaces.messages.Warning.display(decodedResponse);
			CMDBuild.core.interfaces.messages.Error.display(decodedResponse, operation.request);

			return true;
		},

		/**
		 * @param {Function or Object} options
		 */
		load: function (options) {
			options = Ext.isEmpty(options) ? {} : options;

			Ext.applyIf(options, {
				callback: Ext.emptyFn,
				params: {},
				scope: this
			});

			if (
				CMDBuild.global.Cache.isEnabled()
				&& CMDBuild.global.Cache.isCacheable(this.groupId)
			) {
				var parameters = {
					type: this.type,
					groupId: this.groupId,
					serviceEndpoint: this.proxy.url,
					params: options.params
				};

				if (!CMDBuild.global.Cache.isExpired(parameters)) { // Emulation of success and callback execution
					var cachedValues = CMDBuild.global.Cache.get(parameters);

					this.loadData(cachedValues.records);

					// Interceptor to manage error/warning messages
					options.callback = Ext.Function.createInterceptor(options.callback, this.callbackInterceptor, this);

					return Ext.callback(options.callback, options.scope, [cachedValues.records, cachedValues.operation, cachedValues.success]);
				} else { // Execute real Ajax call
					options.callback = Ext.Function.createSequence(function (records, operation, success) {
						Ext.apply(parameters, {
							values: {
								records: records,
								operation: operation,
								success: success
							}
						});

						// Cache builder call
						CMDBuild.global.Cache.set(parameters);
					}, options.callback);
				}
			}

			// Interceptor to manage error/warning messages
			options.callback = Ext.Function.createInterceptor(options.callback, this.callbackInterceptor, this);

			// Uncachable endpoint manage
			this.callParent(arguments);
		}
	});

})();
