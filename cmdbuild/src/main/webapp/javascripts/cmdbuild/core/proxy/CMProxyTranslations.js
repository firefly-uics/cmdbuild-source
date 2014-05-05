(function() {

	var GET = "GET", POST = "POST";
	CMDBuild.ServiceProxy.url.translations = {
			listAvailableTranslations :	"services/json/utils/listavailabletranslations",
			getConfiguration: "services/json/schema/setuptranslations/getconfiguration",
			setup : "services/json/schema/setuptranslations/",
			saveConfiguration: "services/json/schema/setuptranslations/saveconfiguration",
			
			createForClass:  "services/json/schema/setuptranslations/createforclass",
			createForClassAttribute:  "services/json/schema/setuptranslations/createforclassattribute",
			createForDomain:  "services/json/schema/setuptranslations/createfordomain",
			createForDomainAttribute:  "services/json/schema/setuptranslations/createfordomainattribute",
			createForFilterView:  "services/json/schema/setuptranslations/createforfilterview",
			createForSqlView:  "services/json/schema/setuptranslations/createforsqlview",
			createForFilter:  "services/json/schema/setuptranslations/createforfilter",
			createForInstanceName:  "services/json/schema/setuptranslations/createforinstancename",
			createForWidget:  "services/json/schema/setuptranslations/createforwidget",
			createForDashboard:  "services/json/schema/setuptranslations/createfordashboard",
			createForChart:  "services/json/schema/setuptranslations/createforchart",
			createForReport:  "services/json/schema/setuptranslations/createforreport",
			createForLookup:  "services/json/schema/setuptranslations/createforlookup",
			createForGisIcon: "services/json/schema/setuptranslations/createforgisicon",

			readForClass:  "services/json/schema/setuptranslations/readforclass",
			readForClassAttribute:  "services/json/schema/setuptranslations/readforclassattribute",
			readForDomain:  "services/json/schema/setuptranslations/readfordomain",
			readForDomainAttribute:  "services/json/schema/setuptranslations/readfordomainattribute",
			readForFilterView:  "services/json/schema/setuptranslations/readforfilterview",
			readForSqlView:  "services/json/schema/setuptranslations/readforsqlview",
			readForFilter:  "services/json/schema/setuptranslations/readforfilter",
			readForInstanceName:  "services/json/schema/setuptranslations/readforinstancename",
			readForWidget:  "services/json/schema/setuptranslations/readforwidget",
			readForDashboard:  "services/json/schema/setuptranslations/readfordashboard",
			readForChart:  "services/json/schema/setuptranslations/readforchart",
			readForReport:  "services/json/schema/setuptranslations/readforreport",
			readForLookup:  "services/json/schema/setuptranslations/readforlookup",
			readForGisIcon: "services/json/schema/setuptranslations/readforgisicon",

			updateForClass:  "services/json/schema/setuptranslations/updateforclass",
			updateForClassAttribute:  "services/json/schema/setuptranslations/updateforclassattribute",
			updateForDomain:  "services/json/schema/setuptranslations/updatefordomain",
			updateForDomainAttribute:  "services/json/schema/setuptranslations/updatefordomainattribute",
			updateForFilterView:  "services/json/schema/setuptranslations/updateforfilterview",
			updateForSqlView:  "services/json/schema/setuptranslations/updateforsqlview",
			updateForFilter:  "services/json/schema/setuptranslations/updateforfilter",
			updateForInstanceName:  "services/json/schema/setuptranslations/updateforinstancename",
			updateForWidget:  "services/json/schema/setuptranslations/updateforwidget",
			updateForDashboard:  "services/json/schema/setuptranslations/updatefordashboard",
			updateForChart:  "services/json/schema/setuptranslations/updateforchart",
			updateForReport:  "services/json/schema/setuptranslations/updateforreport",
			updateForLookup:  "services/json/schema/setuptranslations/updateforlookup",
			updateForGisIcon: "services/json/schema/setuptranslations/updateforgisicon",

			deleteForClass:  "services/json/schema/setuptranslations/deleteforclass",
			deleteForClassAttribute:  "services/json/schema/setuptranslations/deleteforclassattribute",
			deleteForDomain:  "services/json/schema/setuptranslations/deletefordomain",
			deleteForDomainAttribute:  "services/json/schema/setuptranslations/deletefordomainattribute",
			deleteForFilterView:  "services/json/schema/setuptranslations/deleteforfilterview",
			deleteForSqlView:  "services/json/schema/setuptranslations/deleteforsqlview",
			deleteForFilter:  "services/json/schema/setuptranslations/deleteforfilter",
			deleteForInstanceName:  "services/json/schema/setuptranslations/deleteforinstancename",
			deleteForWidget:  "services/json/schema/setuptranslations/deleteforwidget",
			deleteForDashboard:  "services/json/schema/setuptranslations/deletefordashboard",
			deleteForChart:  "services/json/schema/setuptranslations/deleteforchart",
			deleteForReport:  "services/json/schema/setuptranslations/deleteforreport",
			deleteForLookup:  "services/json/schema/setuptranslations/deleteforlookup",
			deleteForGisIcon: "services/json/schema/setuptranslations/deleteforgisicon",

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