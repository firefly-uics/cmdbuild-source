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

	/**
	 * Used for admin report comboBox
	 */
	Ext.define('CMDBuild.model.widget.CMModelOpenReport.reportCombo', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TITLE, type: 'string' }
		]
	});

})();
