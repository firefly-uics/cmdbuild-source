(function() {

	/**
	 * Traffic light class with multiple instances support (id parameter)
	 */
	Ext.define('CMDBuild.core.RequestBarrier', {

		singleton: true,

		/**
		 * @property {Object}
		 *
		 * @private
		 */
		barrierConfigurations: {},

		/**
		 * @param {String} id
		 *
		 * @private
		 */
		callback: function(id) {
			if (
				!Ext.isEmpty(id) && Ext.isString(id)
				&& !Ext.isEmpty(CMDBuild.core.RequestBarrier.barrierConfigurations[id])
			) {
				CMDBuild.core.RequestBarrier.barrierConfigurations[id].index--;

				CMDBuild.core.RequestBarrier.finalize(id);
			}
		},

		/**
		 * Check callback index and launch last callback
		 *
		 * @param {String} id
		 */
		finalize: function(id) {
			if (
				!Ext.isEmpty(id) && Ext.isString(id)
				&& !Ext.isEmpty(CMDBuild.core.RequestBarrier.barrierConfigurations[id])
				&& CMDBuild.core.RequestBarrier.barrierConfigurations[id].index == 0
			) {
				Ext.callback(
					CMDBuild.core.RequestBarrier.barrierConfigurations[id].callback,
					CMDBuild.core.RequestBarrier.barrierConfigurations[id].scope
				);

				delete CMDBuild.core.RequestBarrier.barrierConfigurations[id];
			}
		},

		/**
		 * @param {String} id
		 *
		 * @returns {Function}
		 */
		getCallback: function(id) {
			if (
				!Ext.isEmpty(id) && Ext.isString(id)
				&& !Ext.isEmpty(CMDBuild.core.RequestBarrier.barrierConfigurations[id])
			) {
				CMDBuild.core.RequestBarrier.barrierConfigurations[id].index++;

				return function(response, options, decodedResponse) {
					CMDBuild.core.RequestBarrier.callback(id);
				};
			}
		},

		/**
		 * @param {Function} callback
		 * @param {String} id
		 * @param {object} scope
		 */
		init: function(id, callback, scope) {
			if (
				!Ext.isEmpty(id) && Ext.isString(id)
				&& !Ext.isEmpty(callback) && Ext.isFunction(callback)
			) {
				CMDBuild.core.RequestBarrier.barrierConfigurations[id] = {
					callback: callback,
					index: 0,
					scope: scope || this
				};
			} else {
				_error('barrier identifier or callback not defined', this);
			}
		}
	});

})();