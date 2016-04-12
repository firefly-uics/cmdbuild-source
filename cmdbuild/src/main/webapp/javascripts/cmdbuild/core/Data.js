(function() {

	Ext.define('CMDBuild.core.Data', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * Enable/Disable header to get localized server responses
		 *
		 * @cfg {Boolean}
		 */
		enableLocalized: false,

		/**
		 * Setup with overrides of all data configurations (timeouts, defaultHeaders)
		 */
		constructor: function (configurationObject) {
			Ext.apply(this, configurationObject); // Apply configurations

			if (!Ext.isEmpty(CMDBuild)) {
				var defaultHeaders = {};
				defaultHeaders[CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);
				defaultHeaders[CMDBuild.core.constants.Proxy.LOCALIZED_HEADER_KEY] = this.enableLocalized;

				Ext.Ajax.timeout = CMDBuild.core.configurations.Timeout.getBase() * 1000;
				Ext.Ajax[CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);
				Ext.Ajax[CMDBuild.core.constants.Proxy.LOCALIZED_HEADER_KEY] = this.enableLocalized;

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
