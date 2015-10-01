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
					var params = {};
					params[CMDBuild.core.proxy.CMProxyConstants.TOKEN] = Ext.util.Cookies.get('RestSessionToken');

					CMDBuild.core.proxy.session.Rest.logout({ params: params });
				},
				callback: function(records, operation, success) {
					window.location = 'index.jsp';
				}
			});
		}
	});

})();