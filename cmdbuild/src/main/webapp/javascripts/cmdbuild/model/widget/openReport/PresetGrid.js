(function() {

	Ext.define('CMDBuild.model.widget.openReport.PresetGrid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.READ_ONLY, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.VALUE, type: 'string' }
		]
	});

})();
