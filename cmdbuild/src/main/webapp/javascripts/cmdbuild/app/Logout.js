(function() {

	Ext.define('CMDBuild.app.Logout', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.session.JsonRpc',
			'CMDBuild.core.proxy.session.Rest'
		],

		singleton: true,

		frame: false,
		border: false,

		doLogout: function() {
			CMDBuild.core.proxy.session.JsonRpc.logout({
				scope: this,
				success: function(response, options, decodedResponse) {
					if (!Ext.isEmpty(Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.REST_SESSION_TOKEN))) {
						var urlParams = {};
						urlParams[CMDBuild.core.constants.Proxy.TOKEN] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.REST_SESSION_TOKEN);

						CMDBuild.core.proxy.session.Rest.logout({ urlParams: urlParams });
					}
				},
				callback: function(records, operation, success) {
					window.location = 'index.jsp';
				}
			});
		}
	});

})();