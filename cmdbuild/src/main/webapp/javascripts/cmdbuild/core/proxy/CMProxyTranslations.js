(function() {

	var GET = "GET", POST = "POST";
	CMDBuild.ServiceProxy.url.translations = {
			listAvailableTranslations :	"services/json/utils/listavailabletranslations",
			getConfiguration: "services/json/schema/translation/getconfiguration",
			setup : "services/json/schema/translation/",
			saveConfiguration: "services/json/schema/translation/saveconfiguration",
			
			createForClass:  "services/json/schema/translation/createforclass",
			createForClassAttribute:  "services/json/schema/translation/createforclassattribute",
			createForDomain:  "services/json/schema/translation/createfordomain",
			createForDomainAttribute:  "services/json/schema/translation/createfordomainattribute",
			createForFilterView:  "services/json/schema/translation/createforfilterview",
			createForSqlView:  "services/json/schema/translation/createforsqlview",
			createForFilter:  "services/json/schema/translation/createforfilter",
			createForInstanceName:  "services/json/schema/translation/createforinstancename",
			createForWidget:  "services/json/schema/translation/createforwidget",
			createForDashboard:  "services/json/schema/translation/createfordashboard",
			createForChart:  "services/json/schema/translation/createforchart",
			createForReport:  "services/json/schema/translation/createforreport",
			createForLookup:  "services/json/schema/translation/createforlookup",
			createForGisIcon: "services/json/schema/translation/createforgisicon",

			readForClass:  "services/json/schema/translation/readforclass",
			readForClassAttribute:  "services/json/schema/translation/readforclassattribute",
			readForDomain:  "services/json/schema/translation/readfordomain",
			readForDomainAttribute:  "services/json/schema/translation/readfordomainattribute",
			readForFilterView:  "services/json/schema/translation/readforfilterview",
			readForSqlView:  "services/json/schema/translation/readforsqlview",
			readForFilter:  "services/json/schema/translation/readforfilter",
			readForInstanceName:  "services/json/schema/translation/readforinstancename",
			readForWidget:  "services/json/schema/translation/readforwidget",
			readForDashboard:  "services/json/schema/translation/readfordashboard",
			readForChart:  "services/json/schema/translation/readforchart",
			readForReport:  "services/json/schema/translation/readforreport",
			readForLookup:  "services/json/schema/translation/readforlookup",
			readForGisIcon: "services/json/schema/translation/readforgisicon",

			updateForClass:  "services/json/schema/translation/updateforclass",
			updateForClassAttribute:  "services/json/schema/translation/updateforclassattribute",
			updateForDomain:  "services/json/schema/translation/updatefordomain",
			updateForDomainAttribute:  "services/json/schema/translation/updatefordomainattribute",
			updateForFilterView:  "services/json/schema/translation/updateforfilterview",
			updateForSqlView:  "services/json/schema/translation/updateforsqlview",
			updateForFilter:  "services/json/schema/translation/updateforfilter",
			updateForInstanceName:  "services/json/schema/translation/updateforinstancename",
			updateForWidget:  "services/json/schema/translation/updateforwidget",
			updateForDashboard:  "services/json/schema/translation/updatefordashboard",
			updateForChart:  "services/json/schema/translation/updateforchart",
			updateForReport:  "services/json/schema/translation/updateforreport",
			updateForLookup:  "services/json/schema/translation/updateforlookup",
			updateForGisIcon: "services/json/schema/translation/updateforgisicon",

			deleteForClass:  "services/json/schema/translation/deleteforclass",
			deleteForClassAttribute:  "services/json/schema/translation/deleteforclassattribute",
			deleteForDomain:  "services/json/schema/translation/deletefordomain",
			deleteForDomainAttribute:  "services/json/schema/translation/deletefordomainattribute",
			deleteForFilterView:  "services/json/schema/translation/deleteforfilterview",
			deleteForSqlView:  "services/json/schema/translation/deleteforsqlview",
			deleteForFilter:  "services/json/schema/translation/deleteforfilter",
			deleteForInstanceName:  "services/json/schema/translation/deleteforinstancename",
			deleteForWidget:  "services/json/schema/translation/deleteforwidget",
			deleteForDashboard:  "services/json/schema/translation/deletefordashboard",
			deleteForChart:  "services/json/schema/translation/deleteforchart",
			deleteForReport:  "services/json/schema/translation/deleteforreport",
			deleteForLookup:  "services/json/schema/translation/deleteforlookup",
			deleteForGisIcon: "services/json/schema/translation/deleteforgisicon",

};

	CMDBuild.ServiceProxy.translations = {
		readAvailableTranslations: function(p) {
			p.method = GET,
			p.url = CMDBuild.ServiceProxy.url.translations.listAvailableTranslations;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},	
		readActiveTranslations: function(p) {
			p.method = GET;
			p.url = CMDBuild.ServiceProxy.url.translations.getConfiguration;
	
			CMDBuild.ServiceProxy.core.doRequest(p);
		},
		saveActiveTranslations: function(p) {
			p.method = POST;
			p.url = CMDBuild.ServiceProxy.url.translations.saveConfiguration;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},
		/*Remove or save translations*/
		manageTranslations: function(p, url) {
			p.method = POST;
			p.url = url;
			CMDBuild.ServiceProxy.core.doRequest(p);
		}	
	};
})();