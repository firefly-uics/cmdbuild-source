(function() {

	/**
	 * CMDBuild cache v2
	 */
	Ext.define('CMDBuild.core.Cache', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy'
		],

		singleton: true,

		/**
		 * Object to save all proxy results. All properties are instances of CMDBuild.model.Cache
		 *
		 * @cfg {Object}
		 * 	Structure: {
		 * 		{Object} 'griupId': {
		 * 			{Object} 'endpoint-identifier': {
		 * 				{CMDBuild.model.Cache} 'encoded-parameters',
		 * 				...
		 * 			},
		 * 		},
		 * 		...
		 * 	}
		 *
		 * @private
		 */
		cachedValues: {},

		/**
		 * Enable/disable cache
		 *
		 * @cfg {Boolean}
		 *
		 * @private
		 */
		enabled: true,

		/**
		 * Managed group ids splits all cached functions in groups
		 *
		 * @cfg {Array}
		 *
		 * @private
		 */
		managedCacheGroupsArray: [
			CMDBuild.core.constants.Proxy.GENERIC, // Default
			CMDBuild.core.constants.Proxy.GROUP
		],

		/**
		 * @param {String} cacheGroupIdentifier
		 * @param {String} identifier
		 * @param {Object} parameters
		 * @param {String} propertyName
		 *
		 * @returns {Object} valuesFromCache
		 *
		 * @private
		 */
		get: function(cacheGroupIdentifier, identifier, parameters, propertyName) {
			cacheGroupIdentifier = Ext.isString(cacheGroupIdentifier) ? cacheGroupIdentifier : CMDBuild.core.constants.Proxy.GENERIC;
			parameters = Ext.isEmpty(parameters) ? CMDBuild.core.constants.Proxy.EMPTY : parameters;
			propertyName = Ext.isString(propertyName) ? propertyName : null;

			var valuesFromCache = {};

			if (
				!Ext.isEmpty(CMDBuild.core.Cache.cachedValues[cacheGroupIdentifier])
				&& !Ext.isEmpty(CMDBuild.core.Cache.cachedValues[cacheGroupIdentifier][identifier])
				&& !Ext.isEmpty(CMDBuild.core.Cache.cachedValues[cacheGroupIdentifier][identifier][Ext.encode(parameters)])
				&& !CMDBuild.core.Cache.isExpired(cacheGroupIdentifier,identifier, parameters)
			) {
				valuesFromCache = CMDBuild.core.Cache.cachedValues[cacheGroupIdentifier][identifier][Ext.encode(parameters)].get(CMDBuild.core.constants.Proxy.RESPONSE);

				if (!Ext.isEmpty(propertyName))
					valuesFromCache = valuesFromCache[propertyName];
			}

			return valuesFromCache;
		},

		/**
		 * Invalidate cache group (delete object)
		 *
		 * @param {String} cacheGroupIdentifier
		 */
		invalidate: function(cacheGroupIdentifier) {
			if (
				!Ext.isEmpty(cacheGroupIdentifier)
				&& Ext.Array.contains(CMDBuild.core.Cache.managedCacheGroupsArray, cacheGroupIdentifier)
			) {
				delete CMDBuild.core.Cache.cachedValues[cacheGroupIdentifier];
			}
		},

		/**
		 * Returns expired state and manage validity invalidate if expired
		 *
		 * @param {String} cacheGroupIdentifier
		 * @param {String} identifier
		 * @param {Object} parameters
		 *
		 * @returns {Boolean} result
		 *
		 * @private
		 */
		isExpired: function(cacheGroupIdentifier, identifier, parameters) {
			cacheGroupIdentifier = Ext.isString(cacheGroupIdentifier) ? cacheGroupIdentifier : CMDBuild.core.constants.Proxy.GENERIC;
			parameters = Ext.isEmpty(parameters) ? CMDBuild.core.constants.Proxy.EMPTY : parameters;

			var result = true;

			if (
				!Ext.isEmpty(CMDBuild.core.Cache.cachedValues[cacheGroupIdentifier])
				&& !Ext.isEmpty(CMDBuild.core.Cache.cachedValues[cacheGroupIdentifier][identifier])
				&& !Ext.isEmpty(CMDBuild.core.Cache.cachedValues[cacheGroupIdentifier][identifier][Ext.encode(parameters)])
			) {
				var cachedObject = CMDBuild.core.Cache.cachedValues[cacheGroupIdentifier][identifier][Ext.encode(parameters)];

				result = (
					Ext.isEmpty(cachedObject)
					|| cachedObject.get(CMDBuild.core.constants.Proxy.DATE) < (Date.now() - CMDBuild.core.configurations.Timeout.getCache())
				);

				if (result)
					delete cachedObject;
			}

			return result;
		},

		/**
		 * @param {String} cacheGroupIdentifier
		 * @param {Object} parameters
		 * @param {String} parameters.method
		 * @param {String} parameters.url
		 * @param {Object} parameters.params
		 * @param {Object} parameters.scope
		 * @param {Function} parameters.failure
		 * @param {Function} parameters.success
		 * @param {Function} parameters.callback
		 * @param {Boolean} invalidateOnSuccess
		 */
		request: function(cacheGroupIdentifier, parameters, invalidateOnSuccess) {
			cacheGroupIdentifier = Ext.isString(cacheGroupIdentifier) ? cacheGroupIdentifier : CMDBuild.core.constants.Proxy.GENERIC;
			invalidateOnSuccess = Ext.isBoolean(invalidateOnSuccess) ? invalidateOnSuccess : false;

			if (
				!Ext.Object.isEmpty(parameters)
				&& !Ext.isEmpty(parameters.url)
			) {
				// Set default values
				Ext.applyIf(parameters, {
					method: 'POST',
					loadMask: true,
					scope: this,
					failure: Ext.emptyFn,
					success: Ext.emptyFn,
					callback: Ext.emptyFn
				});

				if (Ext.Array.contains(CMDBuild.core.Cache.managedCacheGroupsArray, cacheGroupIdentifier)) { // Cacheable endpoints manage
					if (
						!CMDBuild.core.Cache.enabled
						|| CMDBuild.core.Cache.isExpired(cacheGroupIdentifier, parameters.url, parameters.params)
						|| invalidateOnSuccess
					) {
						parameters.success = Ext.Function.createSequence(function(result, options, decodedResult) {
							if (CMDBuild.core.Cache.enabled && !invalidateOnSuccess) // Don't cache if want to invalidate
								CMDBuild.core.Cache.set(cacheGroupIdentifier, parameters.url, parameters.params, {
									result: result,
									options: options,
									decodedResult: decodedResult
								});

							if (invalidateOnSuccess)
								CMDBuild.core.Cache.invalidate(cacheGroupIdentifier);
						}, parameters.success);

						CMDBuild.Ajax.request(parameters);
					} else { // Emulation of success and callback execution
						var cachedValues = CMDBuild.core.Cache.get(cacheGroupIdentifier, parameters.url, parameters.params);

						Ext.Function.createSequence(
							Ext.bind(parameters.success, parameters.scope, [
								cachedValues.result,
								cachedValues.options,
								cachedValues.decodedResult
							]),
							Ext.bind(parameters.callback, parameters.scope, [
								cachedValues.options,
								true,
								cachedValues.result,
							]),
							parameters.scope
						)();
					}
				} else { // Uncachable endpoints manage
					CMDBuild.Ajax.request(parameters);
				}
			} else {
				_error('invalid request parameters', 'CMDBuild.core.Cache');
			}
		},

		/**
		 * @param {String} cacheGroupIdentifier
		 * @param {String} identifier
		 * @param {Object} parameters
		 * @param {Object} values
		 *
		 * @private
		 */
		set: function(cacheGroupIdentifier, identifier, parameters, values) {
			cacheGroupIdentifier = Ext.isString(cacheGroupIdentifier) ? cacheGroupIdentifier : CMDBuild.core.constants.Proxy.GENERIC;
			parameters = Ext.isEmpty(parameters) ? CMDBuild.core.constants.Proxy.EMPTY : parameters;

			if (
				!Ext.isEmpty(identifier) && Ext.isString(identifier)
				&& !Ext.isEmpty(values)
				&& Ext.Array.contains(CMDBuild.core.Cache.managedCacheGroupsArray, cacheGroupIdentifier)
			) {
				var cacheObject = {};
				cacheObject[CMDBuild.core.constants.Proxy.DATE] = Date.now();
				cacheObject[CMDBuild.core.constants.Proxy.PARAMETERS] = parameters;
				cacheObject[CMDBuild.core.constants.Proxy.RESPONSE] = values;

				// Creates cache group object if not exists
				if (Ext.isEmpty(CMDBuild.core.Cache.cachedValues[cacheGroupIdentifier]))
					CMDBuild.core.Cache.cachedValues[cacheGroupIdentifier] = {};

				// Creates cache identifier object if not exists
				if (Ext.isEmpty(CMDBuild.core.Cache.cachedValues[cacheGroupIdentifier][identifier]))
					CMDBuild.core.Cache.cachedValues[cacheGroupIdentifier][identifier] = {};

				CMDBuild.core.Cache.cachedValues[cacheGroupIdentifier][identifier][Ext.encode(parameters)] = Ext.create('CMDBuild.model.Cache', cacheObject);
			}
		}
	});

})();