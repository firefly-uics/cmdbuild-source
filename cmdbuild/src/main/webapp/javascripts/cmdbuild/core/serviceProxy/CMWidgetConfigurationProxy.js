(function() {
	CMDBuild.ServiceProxy.url.CMWidgetConfiguration = {
		save: "services/json/schema/modclass/savewidgetdefinition",
		remove: "services/json/schema/modclass/removewidgetdefinition",
		read: "services/json/schema/modclass/getallwidgets"
	};

	var urls = CMDBuild.ServiceProxy.url.CMWidgetConfiguration;

	CMDBuild.ServiceProxy.CMWidgetConfiguration = {
		/**
		 * 
		 * @param {object} p
		 * @param {object} p.params
		 * @param {string} p.params.className
		 * @param {string} p.params.widget the serialization of the widget configuration
		 */
		save: function(p) {
			p.method = "POST";
			p.url = urls.save;
	
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 * 
		 * @param p
		 * @param {object} p.params
		 * @param {string} p.params.className
		 * @param {string} p.params.widgetId the id of the widget (yes, is a string)
		 */
		remove: function(p) {
			p.method = "POST";
			p.url = urls.remove;
	
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		read: function(p) {
			p.method = "GET";
			p.url = urls.read;
	
			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};

})();