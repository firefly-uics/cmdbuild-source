(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * @link CMDBuild.model.widget.calendar.AttributeCombo
	 */
	Ext.define('CMDBuild.model.classes.tabs.widgets.calendar.AttributeCombo', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION,  type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TYPE,  type: 'string' }
		]
	});

})();
