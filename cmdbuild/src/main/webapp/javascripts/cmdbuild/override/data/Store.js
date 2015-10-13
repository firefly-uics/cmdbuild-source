(function() {

	Ext.define('CMDBuild.override.data.Store', {
		override: 'Ext.data.Store',

		/**
		 * Creates callback interceptor to print error message on store load
		 *
		 * @param {Object} options
		 */
		load: function(options) {
			if (!Ext.isEmpty(options) && !Ext.isEmpty(options.callback))
				options.callback = Ext.Function.createInterceptor(options.callback, this.interceptorFunction, this);

			this.callParent(arguments);
		},

		/**
		 * @param {Array} records
		 * @param {Ext.data.Operation} operation
		 * @param {Boolean} success
		 *
		 * @returns {Boolean}
		 */
		interceptorFunction: function(records, operation, success) {
			var decoded = undefined;

			if (!success) {
				if (
					!Ext.isEmpty(operation)
					&& !Ext.isEmpty(operation.response)
					&& !Ext.isEmpty(operation.response.responseText)
				) {
					decoded = Ext.decode(operation.response.responseText);
				}

				if (
					!Ext.isEmpty(decoded)
					&& !Ext.isEmpty(decoded.errors)
				) {
					Ext.Array.forEach(decoded.errors, function(error, i, allErrors) {
						operation.error = error;

						var detail = '';

						// Add the URL that generate the error
						if (
							!Ext.isEmpty(operation)
							&& !Ext.isEmpty(operation.request)
							&& !Ext.isEmpty(operation.request.url)
						) {
							detail = 'Call: ' + operation.request.url + '\n';

							var line = '';

							for (var i = 0; i < detail.length; ++i)
								line += '-';

							detail += line + '\n';
						}

						detail += 'Error: ' + error.stacktrace; // Add to the details the server stacktrace

						CMDBuild.core.Message.error(null, {
							text: CMDBuild.Translation.errors.unknown_error,
							detail: detail
						});
					}, this);
				}
			}

			return true;
		}
	});

})();