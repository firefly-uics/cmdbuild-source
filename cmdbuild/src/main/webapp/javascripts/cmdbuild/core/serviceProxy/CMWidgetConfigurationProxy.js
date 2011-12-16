(function() {
	CMDBuild.ServiceProxy.url.CMWidgetConfiguration = {
		save: "services/json/schema/modclass/savewidgetdefinition",
		read: "services/json/schema/modclass/readwidgetdefinition",
		remove: "services/json/schema/modclass/removewidgetdefinition",
		groupedByEntryType: "services/json/schema/modclass/getallwidgets"
	};

	var urls = CMDBuild.ServiceProxy.url.CMWidgetConfiguration;

	CMDBuild.ServiceProxy.CMWidgetConfiguration = {
		save: function(p) {
			p.method = "POST";
			p.url = urls.save;
	
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		remove: function(p) {
			p.method = "POST";
			p.url = urls.remove;
	
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		read: function(p) {
			p.method = "GET";
			p.url = urls.read;
	
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		groupedByEntryType: function(p) {
			p.method = "GET";
			p.url = urls.groupedByEntryType;
	
			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};

})();