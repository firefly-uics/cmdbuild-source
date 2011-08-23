(function() {
	Ext.define("CMDBuild.cache.CMGroupModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "description", type: "string"},
			{name: "id", type: "string"},
			{name: "isActive", type: "boolean"},
			{name: "isAdministrator", type: "boolean"},
			{name: "name", type: "string"},
			{name: "text", type: "string"},
			{name: "startingClass", type: "string"}
		]
	});
	
	Ext.define("CMDBuild.cache.CMGroupModelForCombo", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "description", type: "string"},
			{name: "id", type: "int"},
			{name: "isdefault", type: "boolean"}
		]
	});
	
	Ext.define("CMDBuild.cache.CMPrivilegeModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "groupId", type: "string"},,
			{name: "classname", type: "string"},
			{name: "classid", type: "string"},			
			{name: "privilege_mode", type: "string"},
			
			{name: "none_privilege", type: "boolean"},
			{name: "read_privilege", type: "boolean"},
			{name: "write_privilege", type: "boolean"}
		]
	});
	
	Ext.define("CMDBuild.cache.CMUserForGridModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "description", type: "string"},
			{name: "username", type: "string"},
			{name: "isactive", type: "boolean"},
			{name: "userid", type: "string"},
			{name: "email", type: "string"},
			{name: "defaultgroup", type: "int"}
		]
	});

})();