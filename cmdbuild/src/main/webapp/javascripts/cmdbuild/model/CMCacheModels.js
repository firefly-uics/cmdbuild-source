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
	
	Ext.define("CMDBuild.cache.CMDomainModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "active", type: "boolean"},
			{name: "cardinality", type: "string"},
			{name: "nameClass1", type: "string"},
			{name: "nameClass2", type: "string"},
			{name: "idClass1", type: "string"},
			{name: "idClass2", type: "string"},
			{name: "classType", type: "string"},
			{name: "name", type: "string"},
			{name: "createPrivileges", type: "string"},
			{name: "writePrivileges", type: "string"},
			{name: "isMasterDetail", type: "boolean"},
			{name: "description", type: "stirng"},
			{name: "directDescription", type: "stirng"},
			{name: "reverseDescription", type: "stirng"}
		]
	});
	
	Ext.define("CMDBuild.cache.CMReportModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "active", type: "boolean"},
			{name: "text", type: "string"},
			{name: "type", type: "string"},
			{name: "group", type: "string"}
		]
	});
	
	Ext.define("CMDBuild.cache.CMReporModelForGrid", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "type", type: "string"},
			{name: "groups", type: "string"},
			{name: "query", type: "string"},
			{name: "description", type: "string"},
			{name: "title", type: "string"}
		]
	});

})();