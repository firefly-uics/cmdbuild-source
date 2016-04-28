(function () {

	/**
	 * Traffic light class with multiple instances support (id parameter)
	 */
	Ext.define('CMDBuild.core.RequestBarrier', {

		/**
		 * @cfg {Boolean}
		 */
		enableBufferReset: false,

		/**
		 * @property {Object}
		 * 	{
		 * 		{Function} callback,
		 * 		{Number} index
		 * 		{Object} scope
		 * 	}
		 *
		 * @private
		 */
		memorizationBuffer: {},

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Number} parameters.executionTimeout
		 * @param {Function} parameters.failure
		 * @param {String} parameters.id
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 */
		constructor: function (parameters) {
			if (
				Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
				&& !Ext.isEmpty(parameters.id) && Ext.isString(parameters.id)
				&& !Ext.isEmpty(parameters.callback) && Ext.isFunction(parameters.callback)
			) {
				this.memorizationBuffer[parameters.id] = {
					callback: parameters.callback,
					index: 0,
					scope: Ext.isEmpty(parameters.scope) ? this : parameters.scope
				};

				// Failure defered function initialization
				if (
					Ext.isNumber(parameters.executionTimeout) && parameters.executionTimeout > 0
					&& !Ext.isEmpty(parameters.failure) && Ext.isFunction(parameters.failure)
				) {
					Ext.defer(function () {
						if (!Ext.Object.isEmpty(this.memorizationBuffer[parameters.id]))
							Ext.callback(parameters[parameters.id].failure, parameters[parameters.id].scope);
					}, 5000, this);
				}
			} else {
				_error('invalid initialization parameters', this, parameters);
			}
		},

		/**
		 * @param {String} id
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		callback: function (id) {
			if (
				!Ext.isEmpty(id) && Ext.isString(id)
				&& !Ext.Object.isEmpty(this.memorizationBuffer[id])
			) {
				this.memorizationBuffer[id].index--;

				this.finalize(id);
			}
		},

		/**
		 * Check callback index and launch last callback
		 *
		 * @param {String} id
		 * @param {Boolean} enableBufferReset
		 *
		 * @returns {Void}
		 */
		finalize: function (id, enableBufferReset) {
			if (!this.enableBufferReset)
				this.enableBufferReset = Ext.isBoolean(enableBufferReset) ? enableBufferReset : false;

			if (
				!Ext.isEmpty(id) && Ext.isString(id)
				&& !Ext.Object.isEmpty(this.memorizationBuffer[id])
				&& this.memorizationBuffer[id].index == 0
			) {
				Ext.callback(
					this.memorizationBuffer[id].callback,
					this.memorizationBuffer[id].scope
				);

				// Buffer reset should be launched only after last getCallback, so at barrier's initialization end
				if (this.enableBufferReset)
					delete this.memorizationBuffer[id]; // Buffer reset
			}
		},

		/**
		 * @param {String} id
		 *
		 * @returns {Function}
		 */
		getCallback: function (id) {
			if (
				!Ext.isEmpty(id) && Ext.isString(id)
				&& !Ext.Object.isEmpty(this.memorizationBuffer[id])
			) {
				this.memorizationBuffer[id].index++;

				return Ext.bind(this.callback, this, [id]);
			}
		}
	});

})();
