(function() {

	Ext.define('CMDBuild.controller.logout.Logout', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.session.JsonRpc',
			'CMDBuild.core.proxy.session.Rest'
		],

		/**
		 * @param {Object} configurationObject
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.doLogout();
		},

		/**
		 * @private
		 */
		doLogout: function () {
			var urlParams = {};
			urlParams[CMDBuild.core.constants.Proxy.TOKEN] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);

			CMDBuild.core.proxy.session.Rest.logout({
				urlParams: urlParams,
				scope: this,
				callback: function (options, success, response) {
					CMDBuild.core.proxy.session.JsonRpc.logout({
						scope: this,
						callback: function (options, success, response) {
							Ext.util.Cookies.clear(CMDBuild.core.constants.Proxy.SESSION_TOKEN);

							window.location = 'index.jsp';
						}
					});
				}
			});
		}
	});

})();
