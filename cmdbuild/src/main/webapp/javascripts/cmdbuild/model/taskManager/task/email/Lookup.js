(function () {

	Ext.define('CMDBuild.model.taskManager.task.email.Lookup', { // TODO: waiting for refactor (rename)
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'Description', type: 'string' },
			{ name: 'Id', type: 'int' }
		]
	});

})();
