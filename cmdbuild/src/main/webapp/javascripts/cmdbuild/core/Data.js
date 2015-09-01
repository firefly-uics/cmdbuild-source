(function() {

	Ext.define('CMDBuild.core.Data', {

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * Setup with overrides of all data configurations (timeouts, defaultHeaders)
		 */
		constructor: function() {
			if (
				!Ext.isEmpty(CMDBuild)
				&& !Ext.isEmpty(CMDBuild.Config)
			) {
				var toLocalize = Ext.isEmpty(CMDBuild.app.Administration); // I'm on Management so i must localize

				var defaultHeaders = {};
				defaultHeaders[CMDBuild.core.proxy.Constants.LOCALIZED_HEADER_KEY] = toLocalize;

				CMDBuild.Config.defaultTimeout = 90; // TODO: Read from real configuration

				Ext.Ajax.timeout = CMDBuild.Config.defaultTimeout * 1000;
				Ext.Ajax[CMDBuild.core.proxy.Constants.LOCALIZED_HEADER_KEY] = toLocalize;

				Ext.define('CMDBuild.data.Connection', {
					override: 'Ext.data.Connection',

					timeout: CMDBuild.Config.defaultTimeout * 1000,
					defaultHeaders: defaultHeaders
				});

				Ext.define('CMDBuild.data.proxy.Ajax', {
					override: 'Ext.data.proxy.Ajax',

					timeout: CMDBuild.Config.defaultTimeout * 1000
				});

				Ext.define('CMDBuild.form.Basic', {
					override: 'Ext.form.Basic',

					timeout: CMDBuild.Config.defaultTimeout
				});
			} else {
				_error('CMDBuild object is empty', this);
			}
		}
	});

})();