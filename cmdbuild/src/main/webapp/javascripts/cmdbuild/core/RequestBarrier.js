(function() {

	/**
	 * Traffic light class with multiple instances support (id parameter)
	 */
	Ext.define('CMDBuild.core.RequestBarrier', {

		singleton: true,

		/**
		 * @property {Object}
		 */
		barrierConfigurations: {},

		/**
		 * @param {String} id
		 */
		callback: function(id) {
			CMDBuild.core.RequestBarrier.barrierConfigurations[id].index--;

			if (CMDBuild.core.RequestBarrier.barrierConfigurations[id].index == 0)
				Ext.callback(
					CMDBuild.core.RequestBarrier.barrierConfigurations[id].callback,
					CMDBuild.core.RequestBarrier.barrierConfigurations[id].scope
				);
		},

		/**
		 * @param {String} id
		 *
		 * @returns {Function}
		 */
		getCallback: function(id) {
			CMDBuild.core.RequestBarrier.barrierConfigurations[id].index++;

			return function(response, options, decodedResponse) {
				CMDBuild.core.RequestBarrier.callback(id);
			};
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