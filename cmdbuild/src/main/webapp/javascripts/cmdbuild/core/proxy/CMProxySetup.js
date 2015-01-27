(function() {

	CMDBuild.ServiceProxy.setup = {
		testDBConnection: function(p) {
			p.method = "GET";
			p.url = 'services/json/configure/testconnection';

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		applySetup: function(p) {
			p.method = "POST";
			p.url = 'services/json/configure/apply';
			p.timeout = 12000000;
			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};

})();