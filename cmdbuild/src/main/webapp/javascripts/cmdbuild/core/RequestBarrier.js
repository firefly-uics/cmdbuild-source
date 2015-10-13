(function() {

	/**
	 * A traffic light to
	 */
	Ext.define('CMDBuild.core.RequestBarrier', {

		/**
		 * @property {Function}
		 */
		callback: undefined,

		statics: {
			/**
			 * @property {Number}
			 */
			pendingCalls: 1
		},

		/**
		 * @param {Object} configurationObject
		 * @param {Function} configurationObject.callback
		 */
		constructor: function(configurationObject) {
			if (
				!Ext.Object.isEmpty(configurationObject)
				&& !Ext.isEmpty(configurationObject.callback)
			) {
				Ext.apply(this, {
					callback: function () {
						CMDBuild.core.RequestBarrier.pendingCalls--;

						if (CMDBuild.core.RequestBarrier.pendingCalls == 0)
							configurationObject.callback();
					}
				});
			} else {
				_error('Malformed configuration object', this);
			}
		},

		/**
		 * @returns {Function}
		 */
		getCallback: function() {
			CMDBuild.core.RequestBarrier.pendingCalls++;

			return this.callback;
		},

		start: function() {
			this.callback();
		}
	});

})();