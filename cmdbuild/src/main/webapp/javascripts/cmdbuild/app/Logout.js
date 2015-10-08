(function() {

	Ext.define('CMDBuild.app.Logout', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
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
					if (!Ext.isEmpty(Ext.util.Cookies.get(CMDBuild.core.proxy.CMProxyConstants.REST_SESSION_TOKEN))) {
						var urlParams = {};
						urlParams[CMDBuild.core.proxy.CMProxyConstants.TOKEN] = Ext.util.Cookies.get(CMDBuild.core.proxy.CMProxyConstants.REST_SESSION_TOKEN);

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