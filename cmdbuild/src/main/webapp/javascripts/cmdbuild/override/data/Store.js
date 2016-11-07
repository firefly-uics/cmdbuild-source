(function () {

	Ext.require([
		'CMDBuild.core.interfaces.messages.Error',
		'CMDBuild.core.interfaces.messages.Warning',
		'CMDBuild.core.Message'
	]);

	Ext.define('CMDBuild.override.data.Store', {
		override: 'Ext.data.Store',

		/**
		 * Creates callback interceptor to print error message on store load - 02/10/2015
		 *
		 * @param {Object} options
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		load: function (options) {
			if (!Ext.isEmpty(CMDBuild.global) && !Ext.isEmpty(CMDBuild.global.Data))
				CMDBuild.global.Data.dataDefaultHeadersUpdate();

			if (!Ext.isEmpty(options)) {
				options.callback = Ext.isEmpty(options.callback) || !Ext.isFunction(options.callback) ? Ext.emptyFn : options.callback;
				options.callback = Ext.Function.createInterceptor(options.callback, this.interceptorFunction, this);
			}

			this.callParent(arguments);
		},

		/**
		 * @param {Array} records
		 * @param {Ext.data.Operation} operation
		 * @param {Boolean} success
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		interceptorFunction: function (records, operation, success) {
			var decodedResponse = undefined;

			if (!success) {
				if (
					!Ext.isEmpty(operation)
					&& !Ext.isEmpty(operation.response)
					&& !Ext.isEmpty(operation.response.responseText)
				) {
					decodedResponse = Ext.decode(operation.response.responseText);
				}

				if (!CMDBuild.global.interfaces.Configurations.get('disableAllMessages')) {
					if (!CMDBuild.global.interfaces.Configurations.get('disableWarnings'))
						CMDBuild.core.interfaces.messages.Warning.display(decodedResponse);

					if (!CMDBuild.global.interfaces.Configurations.get('disableErrors'))
						CMDBuild.core.interfaces.messages.Error.display(decodedResponse, operation.request);
				}
			}

			return true;
		}
	});

})();
