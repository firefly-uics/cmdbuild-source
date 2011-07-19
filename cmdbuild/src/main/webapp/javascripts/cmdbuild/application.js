(function() {
	Ext.ns("CMDBuild");
	
	// global constants
	CMDBuild.CM_LABEL_WIDTH = 150;
	CMDBuild.CM_BIG_FIELD_WIDTH = 420;
	CMDBuild.CM_MIDDLE_FIELD_WIDTH = 280;
	CMDBuild.CM_SMALL_FIELD_WIDTH = 210;

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
	
	var DEFAULT_TIMEOUT_S = 90;

	Ext.override(Ext.data.Connection, {
		timeout : DEFAULT_TIMEOUT_S * 1000
	});
	
	Ext.override(Ext.form.BasicForm, {
		timeout : DEFAULT_TIMEOUT_S
	});
	
	// Component masks are shown at 20000 z-index. This oddly fixes
	// the problem of masks appearing on top of new windows.
	Ext.WindowMgr.zseed = 30000;
	
	Ext.enableFx = false;
})();

function getCurrentLanguage() {
	var languageParam = Ext.urlDecode(window.location.search.substring(1))['language'];
	if (languageParam) 
		return languageParam;
	else
		return CMDBuild.Config.cmdbuild.language;
}