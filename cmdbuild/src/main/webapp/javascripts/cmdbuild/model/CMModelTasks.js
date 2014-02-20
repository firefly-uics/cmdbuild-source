(function() {

	Ext.define('CMDBuild.model.tasks.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name:'id', type: 'int' },
			{ name:'type', type: 'string' },
			{ name:'active', type: 'boolean' },
			{ name:'status', type: 'int' }, //convert: function(newValue, model) { return (model.get('active')) ? '@@ Active' : '@@ Stopped'; }
			{ name:'last', type: 'string' },
			{ name:'next', type: 'string' }
		]
	});

})();
