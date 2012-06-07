(function() {

	CMDBuild.ServiceProxy.relations = {
		getList: function(p) {
			p.method = "GET";
			p.url = 'services/json/management/modcard/getrelationlist';

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		modify: function(p) {
			p.method = "POST";
			p.url = 'services/json/management/modcard/modifyrelation';

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		add: function(p) {
			p.method = "POST";
			p.url = 'services/json/management/modcard/createrelations';

			CMDBuild.ServiceProxy.core.doRequest(p);
		},
		
		remove: function(p) {
			p.method = "POST";
			p.url = 'services/json/management/modcard/deleterelation';

			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};

})();