(function() {

	Ext.define('CMDBuild.model.widget.openReport.ReportCombo', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.TITLE, type: 'string' }
		]
	});

})();
