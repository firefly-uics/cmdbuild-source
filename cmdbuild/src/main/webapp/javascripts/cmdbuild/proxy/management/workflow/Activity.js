(function () {

	Ext.define('CMDBuild.proxy.management.workflow.Activity', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		read: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.workflow.activity.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WORKFLOW_ACTIVITY, parameters);
		}
	});

})();
