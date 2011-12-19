(function() {
	Ext.ns("CMDBuild");

	// global constants
	CMDBuild.LABEL_WIDTH = 150;

	CMDBuild.BIG_FIELD_ONLY_WIDTH = 420;
	CMDBuild.MEDIUM_FIELD_ONLY_WIDTH = 150;
	CMDBuild.SMALL_FIELD_ONLY_WIDTH = 80;
	CMDBuild.BIG_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + CMDBuild.BIG_FIELD_ONLY_WIDTH;
	CMDBuild.MEDIUM_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + CMDBuild.SMALL_FIELD_ONLY_WIDTH;
	CMDBuild.SMALL_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + CMDBuild.SMALL_FIELD_ONLY_WIDTH;

	CMDBuild.ADM_BIG_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + 250;
	CMDBuild.ADM_MEDIUM_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + 150;
	CMDBuild.ADM_SMALL_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + 80;

	CMDBuild.CFG_LABEL_WIDTH = 300;
	CMDBuild.CFG_BIG_FIELD_WIDTH = CMDBuild.CFG_LABEL_WIDTH + 450;
	CMDBuild.CFG_MEDIUM_FIELD_WIDTH = CMDBuild.CFG_LABEL_WIDTH + 150;

	// global object with runtime configuration
	CMDBuild.Config = {}
	
	CMDBuild.log = log4javascript.getLogger();
	CMDBuild.log.addAppender(new log4javascript.BrowserConsoleAppender());
	
	// convenience methods to debug
	_debug = function() {
		var prefix = "Debug";
		if (typeof arguments[0] == "string") { 
			prefix += ": " + arguments[0];
		}
		CMDBuild.log.debug(prefix, arguments);
	};
	
	_trace = function() {
		_debug(arguments);
		if (console) {
			console.trace();
		}
	}

	_deprecated = function() {
		var name = "", className = "";

		try {
			name  = arguments.callee.caller.$name;
			className = arguments.callee.caller.$owner.$className;
		} catch (e) {
			_debug("DEPRECATED", _trace());
		}
		_debug("DEPRECATED: " + className + "." + name);
	}

	// TODO: Read from real configuration
	CMDBuild.Config.defaultTimeout = 90;

	Ext.override(Ext.data.Connection, {
		timeout : CMDBuild.Config.defaultTimeout * 1000
	});
	
	Ext.override(Ext.form.BasicForm, {
		timeout : CMDBuild.Config.defaultTimeout
	});
	
	// Component masks are shown at 20000 z-index. This oddly fixes
	// the problem of masks appearing on top of new windows.
	Ext.WindowMgr.zseed = 30000;
	
	Ext.enableFx = false;
})();
