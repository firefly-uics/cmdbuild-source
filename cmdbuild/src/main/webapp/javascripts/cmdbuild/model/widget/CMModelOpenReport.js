(function() {

	/**
	 * Used for admin preset grid
	 */
	Ext.define('CMDBuild.model.widget.CMModelOpenReport.presetGrid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.VALUE, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.READ_ONLY, type: 'boolean' }
		]
	});

	/**
	 * Used for admin report comboBox
	 */
	Ext.define('CMDBuild.model.widget.CMModelOpenReport.reportCombo', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.TITLE, type: 'string' }
		]
	});

})();
