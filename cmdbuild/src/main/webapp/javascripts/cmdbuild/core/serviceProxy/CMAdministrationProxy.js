(function() {

var domainGridStore = null;

CMDBuild.ServiceProxy.administration = {
	printSchema: 'services/json/schema/modreport/printclassschema',
	domain: {
		list: function(p) {
			p.method = "GET",
			p.url = "services/json/schema/modclass/getalldomains";
			
			CMDBuild.ServiceProxy.core.doRequest(p);
		},
		save: function(p) {
			p.method = "POST";
			p.url = "services/json/schema/modclass/savedomain";

			CMDBuild.ServiceProxy.core.doRequest(p);
		},
		remove: function(p) {
			p.method = "POST";
			p.url = "services/json/schema/modclass/deletedomain";

			CMDBuild.ServiceProxy.core.doRequest(p);
		},
		attribute: {
			save: function(p) {
				p.method = "POST";
				p.url = "services/json/schema/modclass/saveattribute";

				CMDBuild.ServiceProxy.core.doRequest(p);
			},
			remove: function(p) {
				p.method = "POST";
				p.url = "services/json/schema/modclass/deleteattribute";

				CMDBuild.ServiceProxy.core.doRequest(p);
			}
		},
		getGridStore: function() {
			if (domainGridStore == null) {
				domainGridStore =  new Ext.data.Store({
					model: "CMDomainModelForGrid",
					proxy: {
						type: "ajax",
						url: 'services/json/schema/modclass/getdomainlist',
						reader: {
							type: "json",
							root: "rows"
						}
					},
					autoLoad: false,
					sorters: {
						property: 'description',
						direction: 'ASC'
					}
				});
			}
			return domainGridStore
		}
	}
};

})();