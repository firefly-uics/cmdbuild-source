/* Define our namespace */
Ext.ns('CMDBuild');
Ext.ns('CMDBuild.form');
Ext.ns('CMDBuild.Config');
Ext.ns('CMDBuild.Management');
Ext.ns('CMDBuild.Administration');
Ext.ns('CMDBuild.Administration.Forms');
Ext.ns('CMDBuild.WidgetBuilders');

CMDBuild.log = log4javascript.getLogger();
CMDBuild.log.addAppender(new log4javascript.BrowserConsoleAppender());

//shortcut to debug log message
var _debug = function() {
	CMDBuild.log.debug('Debug: ', arguments);
};

var _trace = function() {
	_debug(arguments);
	if (console) {
		console.trace();
	}
};

function getCurrentLanguage() {
	var languageParam = Ext.urlDecode(window.location.search.substring(1))['language'];
	if (languageParam)
		return languageParam;
	else
		return CMDBuild.Config.cmdbuild.language;
}

(function(){

var DEFAULT_TIMEOUT_S = 90;

Ext.override(Ext.data.Connection, {
	timeout : DEFAULT_TIMEOUT_S * 1000
});

Ext.override(Ext.form.BasicForm, {
	timeout : DEFAULT_TIMEOUT_S
});

})();

// Component masks are shown at 20000 z-index. This oddly fixes
// the problem of masks appearing on top of new windows.
Ext.WindowMgr.zseed = 30000;
