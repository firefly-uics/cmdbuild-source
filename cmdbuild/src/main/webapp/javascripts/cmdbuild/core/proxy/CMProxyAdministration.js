(function() {

var domainGridStore = null;
var GET = "GET", POST = "POST";

CMDBuild.ServiceProxy.administration = {
	domain: {
		attribute: {
			save: function(p) {
				p.method = POST;
				p.url = "services/json/schema/modclass/saveattribute";

				CMDBuild.ServiceProxy.core.doRequest(p);
			},
			remove: function(p) {
				p.method = POST;
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
							root: "domains"
						}
					},
					autoLoad: false,
					sorters: {
						property: 'description',
						direction: 'ASC'
					}
				});
			}
			return domainGridStore;
		}
	}
};

})();