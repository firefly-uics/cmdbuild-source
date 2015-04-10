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

	// Global object with runtime configuration
	CMDBuild.Config = {};

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

		_deprecated = function() {
			var name = '';

			try {
				name  = arguments.callee.caller.name;
			} catch (e) {
				CMDBuild.log.debug('DEPRECATED: ' + _trace());
			}

			CMDBuild.log.debug('DEPRECATED: ' + name, _trace());
		};

		/**
		 * @param {String} message
		 * @param {String} className
		 */
		_error = function(message, className) {
			if (!Ext.isEmpty(message))
				CMDBuild.log.error('ERROR: ' + className + ' - ' + message);
		};

		/**
		 * @param {String} message
		 */
		_msg = function(message) {
			if (!Ext.isEmpty(message))
				CMDBuild.log.info('INFO: ' + message);
		};

		_trace = function() {
			CMDBuild.log.trace('TRACE: ', arguments);

			if (console && typeof console.trace == 'function')
				console.trace();
		};

		/**
		 * @param {String} message
		 * @param {String} className
		 */
		_warning = function(message, className) {
			if (!Ext.isEmpty(message))
				CMDBuild.log.debug.apply(CMDBuild.log, 'WARNING: ' + className + ' - ' + message);
		};
	// END: Logger configuration

	// Setup Ext timeouts
		// TODO: Read from real configuration
		CMDBuild.Config.defaultTimeout = 90;
		Ext.Ajax.timeout = CMDBuild.Config.defaultTimeout * 1000;

		Ext.define('CMDBuild.data.Connection', {
			override: 'Ext.data.Connection',

			timeout: CMDBuild.Config.defaultTimeout * 1000
		});

		Ext.define('CMDBuild.data.proxy.Ajax', {
			override: 'Ext.data.proxy.Ajax',

			timeout: CMDBuild.Config.defaultTimeout * 1000
		});

		Ext.define('CMDBuild.form.Basic', {
			override: 'Ext.form.Basic',

			timeout: CMDBuild.Config.defaultTimeout
		});
	// END: Setup Ext timeouts

	// Component masks are shown at 20000 z-index. This oddly fixes the problem of masks appearing on top of new windows.
	// Ext.WindowMgr.zseed = 30000;

	Ext.WindowManager.getNextZSeed();	// To increase the default zseed. Is needed for the combo on windoows probably it fix also the prev problem
	Ext.enableFx = false;

})();