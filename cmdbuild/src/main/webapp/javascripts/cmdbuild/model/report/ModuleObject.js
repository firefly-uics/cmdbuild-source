(function() {

	/**
	 * To build module report object on management side
	 *
	 * @management
	 */
	Ext.define('CMDBuild.model.report.ModuleObject', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.EXTENSION, type: 'string', defaultValue: CMDBuild.core.proxy.CMProxyConstants.PDF },
			{ name: CMDBuild.core.proxy.CMProxyConstants.FORCE_DOWNLOAD, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TYPE, type: 'string', defaultValue: 'CUSTOM' }
		]
	});

})();