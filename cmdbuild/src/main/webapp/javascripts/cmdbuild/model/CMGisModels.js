(function() {
	Ext.define('IconsModel', {
		extend: 'Ext.data.Model',
		fields: [
			{name: 'name', type: 'string'},
			{name: 'description', type: 'string'},
			{name: 'path', type: 'string'}
		]
	});

	Ext.define('GISLayerModel', {
		extend: 'Ext.data.Model',
		fields: [
			{name: 'maxZoom', type: 'string'},
			{name: 'minZoom', type: 'string'},
			{name: 'style', type: 'string'},
			{name: 'description', type: 'string'},
			{name: 'index', type: 'string'},
			{name: 'name', type: 'string'},
			{name: 'type', type: 'string'},
			{name: "masterTableId", type: "string"},
			{name: "masterTableName", type: "string"},
			{name: "isvisible", type: "boolean"}
		]
	});
})();