(function() {

	Ext.define('CMDBuild.controller.logout.Logout', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.session.JsonRpc',
			'CMDBuild.core.proxy.session.Rest'
		],

		/**
		 * @param {Object} configurationObject
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.doLogout();
		},

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
				callback: function(options, success, response) {
					Ext.util.Cookies.clear(CMDBuild.core.constants.Proxy.REST_SESSION_TOKEN);

					window.location = 'index.jsp';
				}
			});
		}
	});

})();