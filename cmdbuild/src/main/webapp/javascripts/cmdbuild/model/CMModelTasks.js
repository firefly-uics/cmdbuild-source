(function() {

	Ext.define('CMDBuild.model.CMModelTasks.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.TYPE, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.STATUS, type: 'string' }, //convert: function(newValue, model) { return (model.get('active')) ? '@@ Active' : '@@ Stopped'; }
		]
	});

})();
