(function () {

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
		 * @param {Function or Object} options
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		load: function (options) {
			options = Ext.isObject(options) ? options : {};

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
					params: Ext.clone(options.params)
				};

				// Avoid different stores to join results adding store model to parameters
				parameters.params.modelName = this.model.getName();

				if (!CMDBuild.global.Cache.isExpired(parameters)) { // Emulation of success and callback execution
					var cachedValues = CMDBuild.global.Cache.get(parameters);

					if (!Ext.isEmpty(cachedValues.records) && Ext.isArray(cachedValues.records))
						this.loadData(cachedValues.records);

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

			// Uncachable endpoint manage
			this.callParent(arguments);
		}
	});

})();
