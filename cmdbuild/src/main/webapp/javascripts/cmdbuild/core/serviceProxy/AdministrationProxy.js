(function() {

CMDBuild.ServiceProxy.administration = {
	saveTable: 'services/json/schema/modclass/savetable',
	deleteTable: 'services/json/schema/modclass/deletetable',
	printSchema: 'services/json/schema/modreport/printclassschema',
	domain: {
		save: function(p) {
			p.method = "POST";
			p.url = "services/json/schema/modclass/savedomain";
			CMDBuild.ServiceProxy.core.submitForm(p);
		},
		remove: function(p) {
			p.method = "POST";
			p.url = "services/json/schema/modclass/deletedomain";
			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	}
}

})();