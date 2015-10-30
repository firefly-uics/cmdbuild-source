(function() {

	Ext.ns('CMDBuild');

	// Global constants
	CMDBuild.LABEL_WIDTH = 150;

	CMDBuild.BIG_FIELD_ONLY_WIDTH = 475;
	CMDBuild.MEDIUM_FIELD_ONLY_WIDTH = 150;
	CMDBuild.SMALL_FIELD_ONLY_WIDTH = 100;
	CMDBuild.BIG_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + CMDBuild.BIG_FIELD_ONLY_WIDTH;
	CMDBuild.MEDIUM_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + CMDBuild.MEDIUM_FIELD_ONLY_WIDTH;
	CMDBuild.SMALL_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + CMDBuild.SMALL_FIELD_ONLY_WIDTH;

	CMDBuild.ADM_BIG_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + 250;
	CMDBuild.ADM_MEDIUM_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + 150;
	CMDBuild.ADM_SMALL_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + 80;

	CMDBuild.CFG_LABEL_WIDTH = 300;
	CMDBuild.CFG_BIG_FIELD_WIDTH = CMDBuild.CFG_LABEL_WIDTH + 450;
	CMDBuild.CFG_MEDIUM_FIELD_WIDTH = CMDBuild.CFG_LABEL_WIDTH + 150;

	// Custom widths
	CMDBuild.HTML_EDITOR_WIDTH = CMDBuild.LABEL_WIDTH + 600;

	// Global object with runtime configuration
	CMDBuild.configuration = {};
	CMDBuild.Config = {}; // @deprecated

	// Logger configuration
		CMDBuild.log = log4javascript.getLogger();
		CMDBuild.log.addAppender(new log4javascript.BrowserConsoleAppender());

		// Disable all console messages if IE8 or lower to avoid print window spam
		if (Ext.isIE9m) {
			var console = { log: function() {} };
			log4javascript.setEnabled(false);
			Ext.Error.ignore = true;
		}

		/**
		 * Convenience methods to debug
		 */
		_debug = function() {
			if (!Ext.isEmpty(arguments[0]) && typeof arguments[0] == 'string')
				arguments[0] = 'DEBUG: ' + arguments[0];

			CMDBuild.log.debug.apply(CMDBuild.log, arguments);
		};

		/**
		 * @param {String} message
		 * @param {Mixed} classWithError
		 */
		_deprecated = function(message, classWithError) {
			classWithError = typeof classWithError == 'string' ? classWithError : Ext.getClassName(classWithError);

			if (!Ext.isEmpty(message))
				CMDBuild.log.warn('DEPRECATED (' + classWithError + '): ' + message);
		};

		/**
		 * @param {String} message
		 * @param {Mixed} classWithError
		 */
		_error = function(message, classWithError) {
			classWithError = typeof classWithError == 'string' ? classWithError : Ext.getClassName(classWithError);

			if (!Ext.isEmpty(message))
				CMDBuild.log.error('ERROR (' + classWithError + '): ' + message);
		};

		/**
		 * @param {String} message
		 */
		_msg = function(message) {
			if (!Ext.isEmpty(arguments[0]) && typeof arguments[0] == 'string')
				arguments[0] = 'INFO: ' + arguments[0];

			CMDBuild.log.info.apply(CMDBuild.log, arguments);
		};

		_trace = function() {
			CMDBuild.log.trace('TRACE: ', arguments);

			if (console && typeof console.trace == 'function')
				console.trace();
		};

		/**
		 * @param {String} message
		 * @param {Mixed} classWithError
		 */
		_warning = function(message, classWithError) {
			classWithError = typeof classWithError == 'string' ? classWithError : Ext.getClassName(classWithError);

			if (!Ext.isEmpty(message))
				CMDBuild.log.warn('WARNING (' + classWithError + '): ' + message);
		};
	// END: Logger configuration

	// Component masks are shown at 20000 z-index. This oddly fixes the problem of masks appearing on top of new windows.
	// Ext.WindowMgr.zseed = 30000;

	Ext.WindowManager.getNextZSeed();	// To increase the default zseed. Is needed for the combo on windows probably it fix also the prev problem
	Ext.enableFx = false;

})();