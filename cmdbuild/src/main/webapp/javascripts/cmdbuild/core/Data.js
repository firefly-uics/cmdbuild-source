(function() {

	Ext.define('CMDBuild.core.Data', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * Setup with overrides of all data configurations (timeouts, defaultHeaders)
		 */
		constructor: function() {
			if (!Ext.isEmpty(CMDBuild)) {
				var toLocalize = Ext.isEmpty(CMDBuild) || Ext.isEmpty(CMDBuild.app) || Ext.isEmpty(CMDBuild.app.Administration); // I'm on Management so i must localize

				var defaultHeaders = {};
				defaultHeaders[CMDBuild.core.constants.Proxy.LOCALIZED_HEADER_KEY] = toLocalize;

				Ext.Ajax.timeout = CMDBuild.core.configurations.Timeout.getBase() * 1000;
				Ext.Ajax[CMDBuild.core.constants.Proxy.LOCALIZED_HEADER_KEY] = toLocalize;

				Ext.define('CMDBuild.data.Connection', {
					override: 'Ext.data.Connection',

					timeout: CMDBuild.core.configurations.Timeout.getBase() * 1000,
					defaultHeaders: defaultHeaders
				});

				Ext.define('CMDBuild.data.proxy.Ajax', {
					override: 'Ext.data.proxy.Ajax',

					timeout: CMDBuild.core.configurations.Timeout.getBase() * 1000
				});

				Ext.define('CMDBuild.form.Basic', {
					override: 'Ext.form.Basic',

					timeout: CMDBuild.core.configurations.Timeout.getBase()
				});
			} else {
				_error('CMDBuild object is empty', this);
			}
		}
	});

})();