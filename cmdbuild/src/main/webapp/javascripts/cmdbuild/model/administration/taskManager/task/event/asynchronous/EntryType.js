(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * @link CMDBuild.model.common.Class
	 */
	Ext.define('CMDBuild.model.administration.taskManager.task.event.asynchronous.EntryType', { // TODO: waiting for refactor (rename text)
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.SYSTEM, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.TEXT, type: 'string' }
		]
	});

})();
