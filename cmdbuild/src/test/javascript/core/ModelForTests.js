(function() {
	Ext.define('Test.User', {
		extend: 'Ext.data.Model',
		idProperty: "name",
		fields: [
			{name: 'name', type: 'string'},
			{name: 'email', type: 'string'},
			{name: 'phone', type: 'string'},
		]
	});
})();