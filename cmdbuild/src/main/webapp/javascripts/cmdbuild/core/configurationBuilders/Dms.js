(function () {

	Ext.define('CMDBuild.core.configurationBuilders.Dms', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.configuration.Dms'
		],

		/**
		 * @cfg {Function}
		 */
		callback: Ext.emptyFn,

		/**
		 * @cfg {Object}
		 */
		scope: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Function} configurationObject.callback
		 * @param {Options} configurationObject.scope
		 *
		 * @returns {Void}
		 */
		constructor: function (configurationObject) {
			Ext.apply(this, configurationObject); // Apply configurations

			Ext.ns('CMDBuild.configuration');
			CMDBuild.configuration.dms = Ext.create('CMDBuild.model.configuration.dms.Dms'); // DMS configuration model

			CMDBuild.core.proxy.configuration.Dms.read({
				loadMask: false,
				scope: this.scope || this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					CMDBuild.configuration.dms = Ext.create('CMDBuild.model.configuration.dms.Dms', decodedResponse); // DMS configuration model
				},
				callback: this.callback
			});
		}
	});

})();
