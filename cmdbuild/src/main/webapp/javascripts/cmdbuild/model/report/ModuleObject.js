(function() {

	/**
	 * To build module report object on management side
	 *
	 * @management
	 */
	Ext.define('CMDBuild.model.report.ModuleObject', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.EXTENSION, type: 'string', defaultValue: CMDBuild.core.proxy.Constants.PDF },
			{ name: CMDBuild.core.proxy.Constants.FORCE_DOWNLOAD, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.TYPE, type: 'string', defaultValue: 'CUSTOM' }
		]
	});

})();