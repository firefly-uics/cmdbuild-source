Ext.ns('CMDBuild');
Ext.ns('CMDBuild.Config');
Ext.ns('CMDBuild.Management');
Ext.ns('CMDBuild.Administration');
Ext.ns('CMDBuild.Administration.Forms');
Ext.ns('CMDBuild.WidgetBuilders');

CMDBuild.log = log4javascript.getLogger();
CMDBuild.log.addAppender(new log4javascript.BrowserConsoleAppender());

// shortcut to debug log message
var _debug = function() {
	CMDBuild.log.debug('@@ ', arguments);
};

CMDBuild.Translation = {
	"common": {
		"tree_names": {
		    "class": "Standard",
		    "simpletable": "Semplici",
		    "process": "Attività"
		}
	}
};

CMDBuild.Constants = {
	colors: {
		blue: {
			background: "#DFE8F6",
			border: "#99BBE8"
		}
	}
}

CMDBuild.Runtime = {
//	StartingClassId: 1011		
};
