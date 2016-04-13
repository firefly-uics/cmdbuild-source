(function () {

	Ext.define('CMDBuild.controller.logout.Logout', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.CookiesManager',
			'CMDBuild.core.proxy.session.JsonRpc'
		],

		/**
		 * @param {Object} configurationObject
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

//			if (!Ext.isEmpty(Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN))) {
//				this.doRestLogout();
//			} else {
				this.doJsonRpcLogout();
//			}
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		doJsonRpcLogout: function () {
			CMDBuild.core.proxy.session.JsonRpc.logout({
				scope: this,
				callback: function (options, success, response) {
					CMDBuild.core.CookiesManager.authorizationClear();

					window.location = 'index.jsp';
				}
			});
		},

//		/**
//		 * @returns {Void}
//		 *
//		 * @private
//		 */
//		doRestLogout: function () {
//			var urlParams = {};
//			urlParams[CMDBuild.core.constants.Proxy.TOKEN] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);
//
//			CMDBuild.core.proxy.session.Rest.logout({
//				urlParams: urlParams,
//				scope: this,
//				callback: this.doJsonRpcLogout
//			});
//		}
	});

})();
