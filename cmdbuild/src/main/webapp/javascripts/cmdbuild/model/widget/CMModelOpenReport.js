(function() {

	/**
	 * Used for admin preset grid
	 */
	Ext.define('CMDBuild.model.widget.CMModelOpenReport.presetGrid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.VALUE, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.READ_ONLY, type: 'boolean' }
		]
	});

})();