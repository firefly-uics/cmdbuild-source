(function () {

	Ext.define('CMDBuild.core.LoggerManager', {

		requires: ['Logger.log4javascript'],

		/**
		 * Declares CMDBuild.log object
		 *
		 * @returns {Void}
		 */
		constructor: function () {
			Ext.ns('CMDBuild.log');
			CMDBuild.log = log4javascript.getLogger();
			CMDBuild.log.addAppender(new log4javascript.BrowserConsoleAppender());

			// Disable all console messages if IE8 or lower to avoid print window spam
			if (Ext.isIE9m) {
				var console = { log: function () {} };

				log4javascript.setEnabled(false);

				Ext.Error.ignore = true;
			}
		}
	});

	// Convenience methods to debug
		/**
		 * @returns {Void}
		 */
		_debug = function () {
			CMDBuild.log.debug.apply(CMDBuild.log, arguments);
		};

		/**
		 * @param {String} message
		 * @param {Mixed} classWithError
		 *
		 * @returns {Void}
		 */
		_deprecated = function (method, classWithError) {
			classWithError = typeof classWithError == 'string' ? classWithError : Ext.getClassName(classWithError);

			if (!Ext.isEmpty(method))
				CMDBuild.log.warn.apply(
					CMDBuild.log,
					Ext.Array.insert(Ext.Array.slice(arguments, 2), 0, ['DEPRECATED (' + classWithError + '): ' + method]) // Slice arguments and prepend custom error message
				);
		};

		/**
		 * @param {String} message
		 * @param {Mixed} classWithError
		 *
		 * @returns {Void}
		 */
		_error = function (message, classWithError) {
			classWithError = Ext.isString(classWithError) ? classWithError : Ext.getClassName(classWithError);

			if (!Ext.isEmpty(message))
				CMDBuild.log.error.apply(
					CMDBuild.log,
					Ext.Array.insert(Ext.Array.slice(arguments, 2), 0, [classWithError + ': ' + message]) // Slice arguments and prepend custom error message
				);
		};

		/**
		 * @returns {Void}
		 */
		_msg = function () {
			CMDBuild.log.info.apply(CMDBuild.log, arguments);
		};

		/**
		 * @returns {Void}
		 */
		_trace = function () {
			CMDBuild.log.trace(arguments);

			if (console && Ext.isFunction(console.trace))
				console.trace();
		};

		/**
		 * @param {String} message
		 * @param {Mixed} classWithError
		 *
		 * @returns {Void}
		 */
		_warning = function (message, classWithError) {
			classWithError = Ext.isString(classWithError) ? classWithError : Ext.getClassName(classWithError);

			if (!Ext.isEmpty(message))
				CMDBuild.log.warn.apply(
					CMDBuild.log,
					Ext.Array.insert(Ext.Array.slice(arguments, 2), 0, [classWithError + ': ' + message]) // Slice arguments and prepend custom error message
				);
		};

})();
