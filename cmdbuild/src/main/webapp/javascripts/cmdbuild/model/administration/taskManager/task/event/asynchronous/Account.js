(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.administration.taskManager.task.event.asynchronous.Account', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' }
		]
	});

})();
