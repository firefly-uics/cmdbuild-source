(function() {

	Ext.define('CMDBuild.core.LoggerManager', {

		requires: ['Logger.log4javascript'],

		/**
		 * Declares CMDBuild.log object
		 */
		constructor: function() {
			if (!Ext.isEmpty(CMDBuild)) {
				CMDBuild.log = log4javascript.getLogger();
				CMDBuild.log.addAppender(new log4javascript.BrowserConsoleAppender());
			} else {
				_error('CMDBuild object is empty', this);
			}

			// Disable all console messages if IE8 or lower to avoid print window spam
			if (Ext.isIE9m) {
				var console = { log: function() {} };

				log4javascript.setEnabled(false);

				Ext.Error.ignore = true;
			}
		}
	});

	/**
	 * Convenience methods to debug
	 */
	_debug = function() {
		CMDBuild.log.debug.apply(CMDBuild.log, arguments);
	};

	/**
	 * @param {String} message
	 * @param {Mixed} classWithError
	 */
	_deprecated = function(method, classWithError) {
		classWithError = typeof classWithError == 'string' ? classWithError : Ext.getClassName(classWithError);

		if (!Ext.isEmpty(method))
			CMDBuild.log.warn('DEPRECATED (' + classWithError + '): ' + method);
	};

	/**
	 * @param {String} message
	 * @param {Mixed} classWithError
	 */
	_error = function(message, classWithError) {
		classWithError = Ext.isString(classWithError) ? classWithError : Ext.getClassName(classWithError);

		if (!Ext.isEmpty(message))
			CMDBuild.log.error(classWithError + ': ' + message);
	};

	/**
	 * @param {String} message
	 */
	_msg = function(message) {
		CMDBuild.log.info.apply(CMDBuild.log, arguments);
	};

	_trace = function() {
		CMDBuild.log.trace(arguments);

		if (console && Ext.isFunction(console.trace))
			console.trace();
	};

	/**
	 * @param {String} message
	 * @param {Mixed} classWithError
	 */
	_warning = function(message, classWithError) {
		classWithError = Ext.isString(classWithError) ? classWithError : Ext.getClassName(classWithError);

		if (!Ext.isEmpty(message))
			CMDBuild.log.warn(classWithError + ': ' + message);
	};

})();