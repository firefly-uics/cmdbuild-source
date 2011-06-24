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

_CMDefault_timeout_s = 90;

Ext.override(Ext.data.Connection, {
	timeout : (_CMDefault_timeout_s * 1000)
});

Ext.override(Ext.form.BasicForm, {
	timeout : _CMDefault_timeout_s
});


// Component masks are shown at 20000 z-index. This oddly fixes
// the problem of masks appearing on top of new windows.
Ext.WindowMgr.zseed = 30000;
