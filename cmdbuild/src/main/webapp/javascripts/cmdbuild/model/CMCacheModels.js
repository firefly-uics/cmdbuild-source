(function() {

	Ext.define("CMDBuild.cache.CMLookupTypeModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "text",type: 'string'},
			{name: "parent",type: 'string'},
			{name: "type",type: 'string'}
		]
	});

	Ext.define("CMDBuild.cache.CMEntryTypeModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "text",type: 'string'},
			{name: "superclass",type: 'boolean'},
			{name: "active",type: 'boolean'},
			{name: "parent",type: 'string'},
			{name: "tableType",type: 'string'},
			{name: "type",type: 'string'},
			{name: "name",type: 'string'},
			{name: "priv_create",type: 'string'},
			{name: "priv_write",type: 'string'},
		]
	});
})();