(function() {
	Ext.define("CMDomainModelForGrid", {
		extend: 'Ext.data.Model',
		fields: [
			{name : 'name', type : "string"},
			{name : 'idDomain', type : "string"},
			{name : 'description',type : "string"},
			{name : 'descrdir',type : "string"},
			{name : 'descrinv',type : "string"},
			{name : 'class', type : "string"},
			{name : 'class2',type : "string"},
			{name : 'cardinality', type : "string"},
			{name : 'md',type : "string"}
		]
	});
})();