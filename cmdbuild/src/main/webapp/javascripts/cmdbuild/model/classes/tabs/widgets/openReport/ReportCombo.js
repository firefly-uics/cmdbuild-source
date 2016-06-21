(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * @link CMDBuild.model.widget.openReport.ReportCombo
	 */
	Ext.define('CMDBuild.model.classes.tabs.widgets.openReport.ReportCombo', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.TITLE, type: 'string' }
		]
	});

})();
