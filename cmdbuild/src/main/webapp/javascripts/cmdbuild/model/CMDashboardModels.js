Ext.define("CMDBuild.model.CMDashboardChart", {
	extend: 'Ext.data.Model',
	fields: [
		{name : 'name', type : "string"},
		{name : 'description', type : "string"},
		{name : 'id',type : "int"}
	]
});

Ext.define("CMDBuild.model.CMDashboard", {
	extend: 'Ext.data.Model',

	toString: function() {
		return Ext.getClassName(this) + " " + this.getName();
	},

	fields: [
		{name : 'name', type : "string"},
		{name : 'description', type : "string"},
		{name : 'id', type : "int"},
		{name : 'groups', type: 'auto'},
		{name : 'charts', type: 'auto'}
	],

	getName: function() {
		return this.get("name");
	},

	getDescription: function() {
		return this.get("description");
	},

	getGroups: function() {
		return this.get("groups");
	},

	setName: function(v) {
		this.set("name", v)
	},

	setDescription: function(v) {
		this.set("description", v);
	},

	setGroups: function(v) {
		return this.set("groups", v);
	}
});