(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * @link CMDBuild.model.widget.createModifyCard.TargetClass
	 */
	Ext.define('CMDBuild.model.classes.tabs.widgets.createModifyCard.TargetClass', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ID,  type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TEXT,  type: 'string' } // TODO: waiting for refactor (rename description)
		]
	});

})();
