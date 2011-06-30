(function() {

	CMDBuild.ServiceProxy.relations = {
		getList: function(p) {
			p.method = "GET";
			p.url = 'services/json/management/modcard/getrelationlist';

			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};

})();