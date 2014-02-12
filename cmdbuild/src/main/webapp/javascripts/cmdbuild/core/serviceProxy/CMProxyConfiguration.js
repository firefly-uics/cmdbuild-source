(function() {

var configurationURL = "services/json/schema/setup/getconfiguration";

CMDBuild.ServiceProxy.configuration = {
	read: readConf,

	readMainConfiguration: function(p) {
		readConf(p,'cmdbuild');
	},

	readWFConfiguration: function(p) {
		readConf(p,'workflow');
	},

	readGisConfiguration: function(p) {
		readConf(p, 'gis');
	},

	save: function(p, name) {
		p.method = "POST";
		p.url = "services/json/schema/setup/saveconfiguration",
		p.params.name = name;
		CMDBuild.ServiceProxy.core.doRequest(p);
	}
};

function readConf(p, name) {
	p.method = "GET";
	p.url = configurationURL;
	p.params = { name: name };

	CMDBuild.ServiceProxy.core.doRequest(p);
}
})();