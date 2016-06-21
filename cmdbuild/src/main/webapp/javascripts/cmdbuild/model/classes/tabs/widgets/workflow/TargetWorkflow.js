(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * @link CMDBuild.model.widget.workflow.TargetWorkflow
	 */
	Ext.define('CMDBuild.model.classes.tabs.widgets.workflow.TargetWorkflow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'superclass', type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ID,  type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TEXT,  type: 'string' }, // TODO: waiting for refactor (rename description)
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string' }
		]
	});

})();
