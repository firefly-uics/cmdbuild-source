(function() {

	Ext.define('CMDBuild.model.tasks.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.TYPE, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.STATUS, type: 'string' }, //convert: function(newValue, model) { return (model.get('active')) ? '@@ Active' : '@@ Stopped'; }
//			{ name: 'last', type: 'string' },
//			{ name: 'next', type: 'string' }
		]
	});

})();
