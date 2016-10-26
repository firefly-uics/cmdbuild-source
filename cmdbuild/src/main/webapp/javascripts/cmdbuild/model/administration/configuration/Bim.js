(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.administration.configuration.Bim', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ENABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.PASSWORD, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.URL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.USERNAME, type: 'string' }
		],

		/**
		 * @returns {Object}
		 */
		getSubmitData: function () {
			return this.getData();
		}
	});

})();
